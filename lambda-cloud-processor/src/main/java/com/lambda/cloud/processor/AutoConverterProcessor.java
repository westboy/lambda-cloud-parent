package com.lambda.cloud.processor;

import com.lambda.cloud.core.annotation.AutoConverter;
import java.io.IOException;
import java.util.Set;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
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
            TypeElement dtoClass = (TypeElement) element;
            AutoConverter anno = dtoClass.getAnnotation(AutoConverter.class);

            String dtoClassName = dtoClass.getQualifiedName().toString();
            String dtoSimpleName = dtoClass.getSimpleName().toString();

            String mapperName = dtoSimpleName + "Converter";
            String targetClassName = getTargetClassName(anno);

            ParameterizedTypeName superInterface = getParameterizedTypeName(anno, dtoClassName, targetClassName);

            AnnotationSpec.Builder builder = AnnotationSpec.builder(ClassName.get("org.mapstruct", "Mapper"))
                    .addMember("componentModel", "$S", "spring")
                    .addMember("nullValuePropertyMappingStrategy", "$T.IGNORE", NullValuePropertyMappingStrategy.class)
                    .addMember("nullValueCheckStrategy", "$T.ALWAYS", NullValueCheckStrategy.class)
                    .addMember("unmappedTargetPolicy", "$T.IGNORE", ReportingPolicy.class);

            if (anno.uses().length > 0) {
                CodeBlock.Builder usesBlock = CodeBlock.builder().add("{ ");
                for (int i = 0; i < anno.uses().length; i++) {
                    if (i > 0) usesBlock.add(", ");
                    usesBlock.add("$T.class", anno.uses()[i]);
                }
                usesBlock.add(" }");
                builder.addMember("uses", usesBlock.build());
            }

            if (anno.config() != Void.class) {
                builder.addMember("config", "$T.class", anno.config());
            }

            AnnotationSpec annotationSpec = builder.build();

            TypeSpec mapperInterface = TypeSpec.interfaceBuilder(mapperName)
                    .addModifiers(Modifier.PUBLIC)
                    .addSuperinterface(superInterface)
                    .addAnnotation(annotationSpec)
                    .build();

            String packageName =
                    elementUtils.getPackageOf(dtoClass).getQualifiedName().toString();

            JavaFile javaFile = JavaFile.builder(packageName, mapperInterface).build();

            try {
                javaFile.writeTo(filer);
            } catch (IOException e) {
                log.error("generate mapper interface error", e);
            }
        }
        return true;
    }

    private ParameterizedTypeName getParameterizedTypeName(
            AutoConverter anno, String dtoClassName, String targetClassName) {
        ParameterizedTypeName superInterface;
        if (anno.converter() == Void.class) {
            superInterface = ParameterizedTypeName.get(
                    ClassName.get("com.lambda.cloud.core.shared", "BaseConverter"),
                    ClassName.bestGuess(dtoClassName),
                    ClassName.bestGuess(targetClassName));
        } else {
            TypeMirror sourceMirror;
            try {
                Class<?> sourceClass = anno.converter();
                sourceMirror = processingEnv
                        .getElementUtils()
                        .getTypeElement(sourceClass.getCanonicalName())
                        .asType();
            } catch (MirroredTypeException mte) {
                sourceMirror = mte.getTypeMirror();
            }
            superInterface = ParameterizedTypeName.get(
                    (ClassName) ClassName.get(sourceMirror),
                    ClassName.bestGuess(dtoClassName),
                    ClassName.bestGuess(targetClassName));
        }
        return superInterface;
    }

    private String getTargetClassName(AutoConverter anno) {
        try {
            Class<?> target = anno.target();
            return target.getName();
        } catch (MirroredTypeException mte) {
            return mte.getTypeMirror().toString();
        }
    }
}
