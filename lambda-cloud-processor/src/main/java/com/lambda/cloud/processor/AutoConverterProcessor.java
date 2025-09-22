package com.lambda.cloud.processor;

import com.lambda.cloud.core.annotation.AutoConverter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
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
            AutoConverter anno = typeElement.getAnnotation(AutoConverter.class);

            String sourceClassName = typeElement.getQualifiedName().toString();
            String sourceSimpleName = typeElement.getSimpleName().toString();

            AnnotationSpec.Builder builder = AnnotationSpec.builder(ClassName.get("org.mapstruct", "Mapper"))
                    .addMember("componentModel", "$S", "spring")
                    .addMember("nullValuePropertyMappingStrategy", "$T.IGNORE", NullValuePropertyMappingStrategy.class)
                    .addMember("nullValueCheckStrategy", "$T.ALWAYS", NullValueCheckStrategy.class)
                    .addMember("unmappedTargetPolicy", "$T.IGNORE", ReportingPolicy.class);

            List<TypeMirror> uses = getTypeMirrors(typeElement, "uses");
            if (!uses.isEmpty()) {
                CodeBlock.Builder usesBlock = CodeBlock.builder().add("{ ");
                for (int i = 0; i < uses.size(); i++) {
                    if (i > 0) usesBlock.add(", ");
                    usesBlock.add("$T.class", ClassName.get((TypeElement)
                            processingEnv.getTypeUtils().asElement(uses.get(i))));
                }
                usesBlock.add(" }");
                builder.addMember("uses", usesBlock.build());
            }

            TypeMirror config = getTypeMirror(typeElement, "config");
            if (config != null) {
                builder.addMember("config", "$T.class", anno.config());
            }

            AnnotationSpec annotationSpec = builder.build();

            String mapperName = sourceSimpleName + "Converter";
            ParameterizedTypeName superInterface = getParameterizedTypeName(typeElement, sourceClassName);

            TypeSpec mapperInterface = TypeSpec.interfaceBuilder(mapperName)
                    .addModifiers(Modifier.PUBLIC)
                    .addSuperinterface(superInterface)
                    .addAnnotation(annotationSpec)
                    .build();

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

    private ParameterizedTypeName getParameterizedTypeName(TypeElement typeElement, String sourceClassName) {
        ParameterizedTypeName superInterface;

        TypeMirror targetMirror = getTypeMirror(typeElement, "target");
        if (targetMirror == null) {
            throw new RuntimeException("target class not found");
        }

        TypeMirror sourceMirror = getTypeMirror(typeElement, "converter");

        if (sourceMirror == null) {
            superInterface = ParameterizedTypeName.get(
                    ClassName.get("com.lambda.cloud.core.convert", "BaseConverter"),
                    ClassName.bestGuess(sourceClassName),
                    ClassName.bestGuess(targetMirror.toString()));
        } else {
            superInterface = ParameterizedTypeName.get(
                    (ClassName) ClassName.get(sourceMirror),
                    ClassName.bestGuess(sourceClassName),
                    ClassName.bestGuess(targetMirror.toString()));
        }
        return superInterface;
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

    private List<TypeMirror> getTypeMirrors(Element element, String name) {
        for (AnnotationMirror am : element.getAnnotationMirrors()) {
            if (am.getAnnotationType().toString().equals(AutoConverter.class.getCanonicalName())) {
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
                        am.getElementValues().entrySet()) {
                    if (name.equals(entry.getKey().getSimpleName().toString())) {
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
}
