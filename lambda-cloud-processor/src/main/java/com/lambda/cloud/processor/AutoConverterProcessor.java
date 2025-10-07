package com.lambda.cloud.processor;

import com.lambda.cloud.core.annotation.AutoConverter;
import com.lambda.cloud.core.annotation.FieldMapping;
import com.lambda.cloud.core.annotation.FieldMappings;
import java.io.IOException;
import java.util.*;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.javapoet.*;

/**
 *
 * AutoConverterProcessor
 *
 * @author jin
 */
@Slf4j
@SupportedAnnotationTypes("com.lambda.cloud.core.annotation.AutoConverter")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class AutoConverterProcessor extends AbstractProcessor {

    private Filer filer;
    private Elements elementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
        elementUtils = processingEnv.getElementUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(AutoConverter.class)) {
            if (element.getKind() != ElementKind.CLASS) {
                continue;
            }
            TypeElement typeElement = (TypeElement) element;

            String sourceClassName = typeElement.getQualifiedName().toString();
            String sourceSimpleName = typeElement.getSimpleName().toString();

            AnnotationSpec.Builder builder = AnnotationSpec.builder(ClassName.get("org.mapstruct", "Mapper"))
                    .addMember("componentModel", "$S", "spring")
                    .addMember("nullValuePropertyMappingStrategy", "$T.IGNORE", NullValuePropertyMappingStrategy.class)
                    .addMember("nullValueCheckStrategy", "$T.ALWAYS", NullValueCheckStrategy.class)
                    .addMember("unmappedTargetPolicy", "$T.IGNORE", ReportingPolicy.class);

            List<TypeMirror> uses = getTypeMirrors(typeElement);
            CodeBlock.Builder usesBlock = CodeBlock.builder().add("{ ");
            usesBlock.add("$T.class", ClassName.get("com.lambda.cloud.core.convert", "ConvertFunctions"));
            if (!uses.isEmpty()) {
                int index = 1;
                // 添加原有的 uses 类
                for (TypeMirror use : uses) {
                    if (index > 0) usesBlock.add(", ");
                    usesBlock.add("$T.class", ClassName.get((TypeElement)
                            processingEnv.getTypeUtils().asElement(use)));
                    index++;
                }
            }
            usesBlock.add(" }");
            builder.addMember("uses", usesBlock.build());

            TypeMirror config = getTypeMirror(typeElement, "config");
            if (config != null) {
                builder.addMember("config", "$T.class", ClassName.get((TypeElement)
                        processingEnv.getTypeUtils().asElement(config)));
            }

            String mapperName = sourceSimpleName + "Converter";

            TypeMirror targetMirror = getTypeMirror(typeElement, "target");
            if (targetMirror == null) {
                throw new RuntimeException("target class not found");
            }

            TypeSpec.Builder addModifiers =
                    TypeSpec.interfaceBuilder(mapperName).addModifiers(Modifier.PUBLIC);

            TypeMirror sourceMirror = getTypeMirror(typeElement, "converter");

            if (sourceMirror == null) {
                AutoConverter anno = typeElement.getAnnotation(AutoConverter.class);
                ParameterizedTypeName superInterface;
                if (!anno.isReverse()) {
                    superInterface = ParameterizedTypeName.get(
                            ClassName.get("com.lambda.cloud.core.convert", "BaseConverter"),
                            ClassName.bestGuess(sourceClassName),
                            ClassName.bestGuess(targetMirror.toString()));
                } else {
                    superInterface = ParameterizedTypeName.get(
                            ClassName.get("com.lambda.cloud.core.convert", "BaseConverter"),
                            ClassName.bestGuess(targetMirror.toString()),
                            ClassName.bestGuess(sourceClassName));
                }
                addModifiers.addSuperinterface(superInterface);

            } else {
                TypeElement typeMirror = getClassNameFromTypeMirror(sourceMirror);
                addModifiers.addSuperinterface(ClassName.get(typeMirror));
            }

            // 提取字段映射配置
            List<FieldMapping> fieldMappings = extractFieldMappings(typeElement);

            // 如果有字段映射配置，则添加带有 @Mapping 注解的方法
            if (!fieldMappings.isEmpty()) {
                addMappingMethods(typeElement, addModifiers, fieldMappings, sourceClassName, targetMirror.toString());
            }

            TypeSpec mapperInterface =
                    addModifiers.addAnnotation(builder.build()).build();

            String packageName =
                    elementUtils.getPackageOf(typeElement).getQualifiedName().toString();

            JavaFile javaFile = JavaFile.builder(packageName, mapperInterface).build();

            try {
                javaFile.writeTo(filer);
            } catch (IOException e) {
                log.error("generate mapper interface error", e);
            }
        }
        return true;
    }

    private TypeElement getClassNameFromTypeMirror(TypeMirror typeMirror) {
        if (typeMirror.getKind() == TypeKind.DECLARED) {
            return (TypeElement) ((DeclaredType) typeMirror).asElement();
        }
        throw new IllegalArgumentException("Unsupported type mirror: " + typeMirror);
    }

    private TypeMirror getTypeMirror(Element element, String name) {
        for (AnnotationMirror am : element.getAnnotationMirrors()) {
            if (am.getAnnotationType().toString().equals(AutoConverter.class.getCanonicalName())) {
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
                        am.getElementValues().entrySet()) {
                    if (name.equals(entry.getKey().getSimpleName().toString())) {
                        return (TypeMirror) entry.getValue().getValue();
                    }
                }
            }
        }
        return null;
    }

    //noinspection used
    private List<TypeMirror> getTypeMirrors(Element element) {
        for (AnnotationMirror am : element.getAnnotationMirrors()) {
            if (am.getAnnotationType().toString().equals(AutoConverter.class.getCanonicalName())) {
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
                        am.getElementValues().entrySet()) {
                    if ("uses".equals(entry.getKey().getSimpleName().toString())) {
                        @SuppressWarnings("unchecked")
                        List<? extends AnnotationValue> values = (List<? extends AnnotationValue>)
                                entry.getValue().getValue();
                        return values.stream()
                                .map(v -> (TypeMirror) v.getValue())
                                .toList();
                    }
                }
            }
        }
        return List.of();
    }

    /**
     * 提取字段映射配置
     * <p>
     * 从 @AutoConverter 注解的 fieldMappings 属性和类上的 @FieldMapping/@FieldMappings 注解中提取字段映射配置
     *
     * @param typeElement 类型元素
     * @return 字段映射配置列表
     */
    private List<FieldMapping> extractFieldMappings(TypeElement typeElement) {
        List<FieldMapping> fieldMappings = new ArrayList<>();

        // 1. 从 @AutoConverter 注解的 fieldMappings 属性中提取
        AutoConverter autoConverter = typeElement.getAnnotation(AutoConverter.class);
        if (autoConverter != null) {
            fieldMappings.addAll(Arrays.asList(autoConverter.fieldMappings()));
        }

        // 2. 从类上的 @FieldMapping 注解中提取（单个）
        //noinspection DuplicatedCode
        FieldMapping singleMapping = typeElement.getAnnotation(FieldMapping.class);
        if (singleMapping != null) {
            fieldMappings.add(singleMapping);
        }

        // 3. 从类上的 @FieldMappings 注解中提取（多个）
        FieldMappings multipleMappings = typeElement.getAnnotation(FieldMappings.class);
        if (multipleMappings != null) {
            fieldMappings.addAll(Arrays.asList(multipleMappings.value()));
        }

        // 4. 从类字段上提取 @FieldMapping 注解
        for (Element enclosedElement : typeElement.getEnclosedElements()) {
            if (enclosedElement.getKind() == ElementKind.FIELD) {
                //noinspection DuplicatedCode
                FieldMapping fieldMapping = enclosedElement.getAnnotation(FieldMapping.class);
                if (fieldMapping != null) {
                    fieldMappings.add(fieldMapping);
                }

                FieldMappings fieldMappingsAnno = enclosedElement.getAnnotation(FieldMappings.class);
                if (fieldMappingsAnno != null) {
                    fieldMappings.addAll(Arrays.asList(fieldMappingsAnno.value()));
                }
            }
        }

        return fieldMappings;
    }

    /**
     * 生成 @Mapping 注解
     *
     * @param fieldMapping 字段映射配置
     * @return @Mapping 注解规范
     */
    private AnnotationSpec generateMappingAnnotation(TypeElement typeElement, FieldMapping fieldMapping) {
        AnnotationSpec.Builder builder = AnnotationSpec.builder(ClassName.get("org.mapstruct", "Mapping"));

        // target 属性（必需）
        builder.addMember("target", "$S", fieldMapping.target());

        // source 属性
        if (!fieldMapping.source().isEmpty()) {
            builder.addMember("source", "$S", fieldMapping.source());
        }

        // ignore 属性保持不变
        if (fieldMapping.ignore()) {
            builder.addMember("ignore", "$L", true);
        }

        // 其他属性（dateFormat, numberFormat 等）保持不变
        if (!fieldMapping.dateFormat().isEmpty()) {
            builder.addMember("dateFormat", "$S", fieldMapping.dateFormat());
        }

        if (!fieldMapping.numberFormat().isEmpty()) {
            builder.addMember("numberFormat", "$S", fieldMapping.numberFormat());
        }

        if (!fieldMapping.locale().isEmpty()) {
            builder.addMember("locale", "$S", fieldMapping.locale());
        }

        if (!fieldMapping.expression().isEmpty()) {
            builder.addMember("expression", "$S", fieldMapping.expression());
        }

        if (!fieldMapping.defaultExpression().isEmpty()) {
            builder.addMember("defaultExpression", "$S", fieldMapping.defaultExpression());
        }

        if (!fieldMapping.defaultValue().isEmpty()) {
            builder.addMember("defaultValue", "$S", fieldMapping.defaultValue());
        }

        if (!fieldMapping.qualifiedByName().isEmpty()) {
            builder.addMember("qualifiedByName", "$S", fieldMapping.qualifiedByName());
        }

        if (!fieldMapping.conditionExpression().isEmpty()) {
            builder.addMember("conditionExpression", "$S", fieldMapping.conditionExpression());
        }

        if (!fieldMapping.conditionQualifiedByName().isEmpty()) {
            builder.addMember("conditionQualifiedByName", "$S", fieldMapping.conditionQualifiedByName());
        }

        generateMappingAnnotation(fieldMapping, builder, "conditionQualifiedBy");
        generateMappingAnnotation(fieldMapping, builder, "qualifiedBy");

        return builder.build();
    }

    private void generateMappingAnnotation(
            FieldMapping fieldMapping, AnnotationSpec.Builder builder, String methodName) {
        try {
            Class<?>[] conditionQualifiedBy = fieldMapping.conditionQualifiedBy(); // 编译期调用可能触发异常
            if (conditionQualifiedBy.length > 0) {
                CodeBlock.Builder cb = CodeBlock.builder().add("{");
                for (int i = 0; i < conditionQualifiedBy.length; i++) {
                    if (i > 0) cb.add(", ");
                    cb.add("$T.class", ClassName.get(conditionQualifiedBy[i]));
                }
                cb.add("}");
                builder.addMember(methodName, "$L", cb.build());
            }
        } catch (MirroredTypesException e) {
            List<? extends TypeMirror> mirrors = e.getTypeMirrors();
            if (!mirrors.isEmpty()) {
                CodeBlock.Builder cb = CodeBlock.builder().add("{");
                for (int i = 0; i < mirrors.size(); i++) {
                    if (i > 0) cb.add(", ");
                    TypeElement te = (TypeElement) ((DeclaredType) mirrors.get(i)).asElement();
                    cb.add("$T.class", ClassName.get(te));
                }
                cb.add("}");
                builder.addMember(methodName, "$L", cb.build());
            }
        }
    }

    /**
     * 在生成的接口中添加带有 @Mapping 注解的方法
     *
     * @param typeBuilder     接口构建器
     * @param fieldMappings   字段映射配置列表
     * @param sourceClassName 源类名
     * @param targetClassName 目标类名
     */
    private void addMappingMethods(
            TypeElement typeElement,
            TypeSpec.Builder typeBuilder,
            List<FieldMapping> fieldMappings,
            String sourceClassName,
            String targetClassName) {

        // 生成 @Mapping 注解列表
        List<AnnotationSpec> mappingAnnotations = fieldMappings.stream()
                .map(fieldMapping -> generateMappingAnnotation(typeElement, fieldMapping))
                .toList();

        // 添加 convertTo 方法（源对象 -> 目标对象）
        MethodSpec.Builder convertToBuilder = MethodSpec.methodBuilder("convertTo");
        AutoConverter anno = typeElement.getAnnotation(AutoConverter.class);
        if (!anno.isReverse()) {
            convertToBuilder
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .addParameter(ClassName.bestGuess(sourceClassName), "source")
                    .returns(ClassName.bestGuess(targetClassName));
        } else {
            convertToBuilder
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .addParameter(ClassName.bestGuess(targetClassName), "source")
                    .returns(ClassName.bestGuess(sourceClassName));
        }

        // 添加所有 @Mapping 注解
        for (AnnotationSpec mappingAnnotation : mappingAnnotations) {
            convertToBuilder.addAnnotation(mappingAnnotation);
        }

        typeBuilder.addMethod(convertToBuilder.build());
    }

    private String getSuperclassName(TypeElement typeElement) {
        TypeMirror superclassMirror = typeElement.getSuperclass();
        if (superclassMirror.getKind() == TypeKind.DECLARED) {
            TypeElement superclassElement = (TypeElement) ((DeclaredType) superclassMirror).asElement();
            return superclassElement.getQualifiedName().toString();
        }
        return null;
    }
}
