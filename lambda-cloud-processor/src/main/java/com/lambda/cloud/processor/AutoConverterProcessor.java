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

            TypeSpec mapperInterface = TypeSpec.interfaceBuilder(mapperName)
                    .addModifiers(Modifier.PUBLIC)
                    .addSuperinterface(ParameterizedTypeName.get(
                            ClassName.get("com.lambda.cloud.core.convert", "BaseConverter"),
                            ClassName.bestGuess(dtoClassName),
                            ClassName.bestGuess(targetClassName)))
                    .addAnnotation(AnnotationSpec.builder(ClassName.get("org.mapstruct", "Mapper"))
                            .addMember("componentModel", "$S", "spring")
                            .addMember(
                                    "nullValuePropertyMappingStrategy",
                                    "$T.IGNORE",
                                    NullValuePropertyMappingStrategy.class)
                            .addMember("nullValueCheckStrategy", "$T.ALWAYS", NullValueCheckStrategy.class)
                            .addMember("unmappedTargetPolicy", "$T.IGNORE", ReportingPolicy.class)
                            .build())
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

    private String getTargetClassName(AutoConverter anno) {
        try {
            Class<?> target = anno.target();
            return target.getName();
        } catch (MirroredTypeException mte) {
            return mte.getTypeMirror().toString();
        }
    }
}
