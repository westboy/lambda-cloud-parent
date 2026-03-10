package com.lambda.cloud.processor.permission;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lambda.cloud.processor.permission.config.ProcessorConfig;
import com.lambda.cloud.processor.permission.extractor.MetadataExtractor;
import com.lambda.cloud.processor.permission.model.ApiPermissionMetadata;
import com.lambda.cloud.processor.permission.model.PermissionFileMetadata;
import com.lambda.cloud.processor.permission.scanner.AnnotationScanner;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.io.Writer;
import java.time.Instant;
import java.util.*;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * 权限注解处理器
 *
 * <p>在编译期扫描 Controller 类和方法上的权限注解，提取接口权限信息，生成 JSON 文件。
 *
 * @author Jin
 */
@SupportedAnnotationTypes({
    "org.springframework.web.bind.annotation.RestController",
    "org.springframework.stereotype.Controller",
    "org.springframework.web.bind.annotation.RequestMapping",
    "org.springframework.web.bind.annotation.GetMapping",
    "org.springframework.web.bind.annotation.PostMapping",
    "org.springframework.web.bind.annotation.PutMapping",
    "org.springframework.web.bind.annotation.DeleteMapping",
    "org.springframework.web.bind.annotation.PatchMapping"
})
@SuppressFBWarnings("DLS_DEAD_LOCAL_STORE")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class PermissionProcessor extends AbstractProcessor {

    private Filer filer;
    private Messager messager;
    private ProcessorConfig config;
    private ObjectMapper objectMapper;
    private MetadataExtractor extractor;
    private AnnotationScanner scanner;
    private long startTime;

    // 用于收集所有轮次的权限信息
    private final List<ApiPermissionMetadata> collectedPermissions = new ArrayList<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.filer = processingEnv.getFiler();
        this.messager = processingEnv.getMessager();
        this.startTime = System.currentTimeMillis();

        // 确保每次初始化时清空收集列表，防止在 Reactor 构建中实例复用导致数据污染
        this.collectedPermissions.clear();

        // 加载配置
        this.config = loadConfig();

        // 初始化 JSON 序列化器
        this.objectMapper = JsonMapper.builder()
                .changeDefaultPropertyInclusion(inc -> inc.withValueInclusion(JsonInclude.Include.NON_NULL))
                .enable(SerializationFeature.INDENT_OUTPUT)
                .build();

        // 初始化提取器和扫描器
        this.extractor = new MetadataExtractor();
        this.scanner = new AnnotationScanner();

        printNote("Permission processor initialized");
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // 检查是否启用
        if (!config.isEnabled()) {
            return false;
        }

        // 1. 在每一轮（非结束轮）扫描并处理注解
        if (!roundEnv.processingOver()) {
            processRound(roundEnv);
            return false; // 继续让其他处理器处理
        }

        // 2. 在结束轮生成文件
        generateFiles();
        return false;
    }

    /**
     * 处理每一轮的扫描
     */
    private void processRound(RoundEnvironment roundEnv) {
        try {
            // 扫描所有 Controller 类
            Set<TypeElement> controllers = findControllers(roundEnv);
            if (!controllers.isEmpty()) {
                printNote("Found " + controllers.size() + " controller classes in this round");
                for (TypeElement controller : controllers) {
                    List<ApiPermissionMetadata> apis = processController(controller);
                    if (!apis.isEmpty()) {
                        collectedPermissions.addAll(apis);
                    }
                }
            }
        } catch (Exception e) {
            printError("Failed to process permissions in round: " + e.getMessage());
        }
    }

    /**
     * 生成最终文件
     */
    private void generateFiles() {
        try {
            // 生成 JSON 文件
            if (!collectedPermissions.isEmpty()) {
                generateJsonFile(collectedPermissions);
                printNote("Generated permission file with " + collectedPermissions.size() + " APIs");
            } else {
                printWarning();
            }

            // 输出性能统计
            long duration = System.currentTimeMillis() - startTime;
            printNote("Permission extraction completed in " + duration + "ms");

        } catch (Exception e) {
            printError("Failed to generate permission files: " + e.getMessage());
        } finally {
            // 生成文件后清空收集列表，防止实例复用导致数据累积到下一个模块
            collectedPermissions.clear();
        }
    }

    /**
     * 查找所有 Controller 类
     */
    private Set<TypeElement> findControllers(RoundEnvironment roundEnv) {
        Set<TypeElement> controllers = new HashSet<>();

        // 查找 @RestController
        for (Element element : roundEnv.getElementsAnnotatedWith(processingEnv
                .getElementUtils()
                .getTypeElement("org.springframework.web.bind.annotation.RestController"))) {
            if (element.getKind() == ElementKind.CLASS) {
                controllers.add((TypeElement) element);
            }
        }

        // 查找 @Controller
        TypeElement controllerAnnotation =
                processingEnv.getElementUtils().getTypeElement("org.springframework.stereotype.Controller");
        if (controllerAnnotation != null) {
            for (Element element : roundEnv.getElementsAnnotatedWith(controllerAnnotation)) {
                if (element.getKind() == ElementKind.CLASS) {
                    controllers.add((TypeElement) element);
                }
            }
        }

        return controllers;
    }

    /**
     * 处理单个 Controller 类
     */
    private List<ApiPermissionMetadata> processController(TypeElement controller) {
        String className = controller.getQualifiedName().toString();
        List<ApiPermissionMetadata> apis = new ArrayList<>();

        // 提取类级别的 @RequestMapping
        String classPath = extractClassPath(controller);
        String classGroup = extractClassGroup(controller);

        // 遍历所有方法（包括父类方法）
        List<ExecutableElement> methods = getAllMethods(controller);
        for (ExecutableElement method : methods) {
            ApiPermissionMetadata api = processMethod(controller, method, classPath, classGroup);
            if (api != null) {
                apis.add(api);
            }
        }

        if (!apis.isEmpty()) {
            printNote("Processed " + className + " (" + apis.size() + " APIs)");
        }

        return apis;
    }

    /**
     * 处理单个方法
     */
    @SuppressWarnings("unused")
    private ApiPermissionMetadata processMethod(
            TypeElement controller, ExecutableElement method, String classPath, String classGroup) {
        // 检查方法是否有路径映射注解
        String methodPath = extractor.extractMethodPath(method);
        String httpMethod = extractor.extractHttpMethod(method);

        // 如果没有路径映射注解，跳过
        if ((methodPath == null || methodPath.isEmpty())
                && !scanner.hasAnnotation(method, "org.springframework.web.bind.annotation.RequestMapping")
                && !scanner.hasAnnotation(method, "org.springframework.web.bind.annotation.GetMapping")
                && !scanner.hasAnnotation(method, "org.springframework.web.bind.annotation.PostMapping")
                && !scanner.hasAnnotation(method, "org.springframework.web.bind.annotation.PutMapping")
                && !scanner.hasAnnotation(method, "org.springframework.web.bind.annotation.DeleteMapping")
                && !scanner.hasAnnotation(method, "org.springframework.web.bind.annotation.PatchMapping")) {
            return null;
        }

        // 使用提取器提取完整的元数据（带模块名称）
        return extractor.extract(controller, method);
    }

    /**
     * 获取类及其父类的所有方法
     */
    private List<ExecutableElement> getAllMethods(TypeElement controller) {
        List<ExecutableElement> methods = new ArrayList<>();
        TypeElement current = controller;

        while (current != null && !current.getQualifiedName().toString().equals("java.lang.Object")) {
            for (Element element : current.getEnclosedElements()) {
                if (element.getKind() == ElementKind.METHOD) {
                    ExecutableElement method = (ExecutableElement) element;
                    // Check if overridden by any method already collected
                    boolean isOverridden = false;
                    for (ExecutableElement existing : methods) {
                        if (processingEnv.getElementUtils().overrides(existing, method, controller)) {
                            isOverridden = true;
                            break;
                        }
                    }
                    if (!isOverridden) {
                        methods.add(method);
                    }
                }
            }

            TypeMirror superclass = current.getSuperclass();
            if (superclass.getKind() == TypeKind.DECLARED) {
                current = (TypeElement) ((DeclaredType) superclass).asElement();
            } else {
                current = null;
            }
        }
        return methods;
    }

    /**
     * 提取类级别的路径
     */
    private String extractClassPath(TypeElement controller) {
        return extractor.extractClassPath(controller);
    }

    /**
     * 提取类级别的分组
     */
    private String extractClassGroup(TypeElement controller) {
        return extractor.extractGroup(controller);
    }

    /**
     * 生成 JSON 文件
     */
    private void generateJsonFile(List<ApiPermissionMetadata> apis) throws IOException {
        PermissionFileMetadata metadata = new PermissionFileMetadata();
        metadata.setVersion("1.0.0");
        metadata.setGeneratedAt(Instant.now().toString());
        metadata.setModule(config.getModuleName());
        metadata.setBasePackage(config.getBasePackage());
        metadata.setTotalApis(apis.size());
        metadata.setApis(apis);

        FileObject resource = filer.createResource(StandardLocation.CLASS_OUTPUT, "", config.getOutputPath());

        try (Writer writer = resource.openWriter()) {
            objectMapper.writeValue(writer, metadata);
        }
    }

    /**
     * 加载配置
     */
    private ProcessorConfig loadConfig() {
        ProcessorConfig config = new ProcessorConfig();
        Map<String, String> options = processingEnv.getOptions();

        config.setEnabled(Boolean.parseBoolean(options.getOrDefault("permission.enabled", "true")));
        config.setOutputPath(
                options.getOrDefault("permission.output.path", "META-INF/permissions/api-permissions.json"));
        config.setOutputFormat(options.getOrDefault("permission.output.format", "json"));
        config.setBasePackage(options.get("permission.base.package"));
        config.setModuleName(options.get("permission.module.name"));

        String includePatterns = options.get("permission.include.patterns");
        if (includePatterns != null && !includePatterns.isEmpty()) {
            config.setIncludePatterns(Arrays.asList(includePatterns.split(",")));
        }

        String excludePatterns = options.get("permission.exclude.patterns");
        if (excludePatterns != null && !excludePatterns.isEmpty()) {
            config.setExcludePatterns(Arrays.asList(excludePatterns.split(",")));
        }

        return config;
    }

    /**
     * 输出提示信息
     */
    private void printNote(String message) {
        messager.printMessage(Diagnostic.Kind.NOTE, "[PermissionProcessor] " + message);
    }

    /**
     * 输出警告信息
     */
    private void printWarning() {
        messager.printMessage(
                Diagnostic.Kind.WARNING, "[PermissionProcessor] " + "No APIs found to generate permission file");
    }

    /**
     * 输出错误信息
     */
    private void printError(String message) {
        messager.printMessage(Diagnostic.Kind.ERROR, "[PermissionProcessor] " + message);
    }
}
