package com.lambda.cloud.processor;

import com.lambda.cloud.core.annotation.AutoConverter;
import com.lambda.cloud.core.annotation.AutoMapper;
import java.io.IOException;
import java.util.Set;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.javapoet.*;

/**
 *
 * AutoConverterProcessor
 *
 * @author jin
 */
@Slf4j
@SupportedAnnotationTypes("com.lambda.cloud.core.convert.AutoConverter")
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
        for (Element element : roundEnv.getElementsAnnotatedWith(AutoMapper.class)) {
            if (element.getKind() != ElementKind.CLASS) {
                continue;
            }
            TypeElement dtoClass = (TypeElement) element;
            AutoMapper anno = dtoClass.getAnnotation(AutoMapper.class);

            String dtoClassName = dtoClass.getQualifiedName().toString();
            String dtoSimpleName = dtoClass.getSimpleName().toString();
            String targetClassName = anno.target().getCanonicalName();
            String targetSimpleName = targetClassName.substring(targetClassName.lastIndexOf('.') + 1);

            String mapperName = dtoSimpleName.replace("DTO", "") + "Mapper";

            MethodSpec toEntity = MethodSpec.methodBuilder("convertTo")
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .returns(ClassName.bestGuess(targetClassName))
                    .addParameter(ClassName.bestGuess(dtoClassName), "dto")
                    .build();

            MethodSpec fromEntity = MethodSpec.methodBuilder("convertFrom")
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .returns(ClassName.bestGuess(dtoClassName))
                    .addParameter(ClassName.bestGuess(targetClassName), "entity")
                    .build();

            MethodSpec updateEntity = MethodSpec.methodBuilder("updateEntity")
                    .addAnnotation(AnnotationSpec.builder(BeanMapping.class)
                            .addMember(
                                    "nullValuePropertyMappingStrategy",
                                    "$T.IGNORE",
                                    NullValuePropertyMappingStrategy.class)
                            .build())
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .returns(TypeName.VOID)
                    .addParameter(ClassName.bestGuess(dtoClassName), "dto")
                    .addParameter(ParameterSpec.builder(ClassName.bestGuess(targetClassName), "entity")
                            .addAnnotation(MappingTarget.class)
                            .build())
                    .build();

            TypeSpec mapperInterface = TypeSpec.interfaceBuilder(mapperName)
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(AnnotationSpec.builder(AutoConverter.class).build())
                    .addMethod(toEntity)
                    .addMethod(fromEntity)
                    .addMethod(updateEntity)
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
}
