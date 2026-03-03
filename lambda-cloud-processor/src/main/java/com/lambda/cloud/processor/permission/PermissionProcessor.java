package com.lambda.cloud.processor.permission;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.lambda.cloud.processor.permission.cache.PermissionCache;
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
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

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
    private PermissionCache cache;
    private long startTime;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.filer = processingEnv.getFiler();
        this.messager = processingEnv.getMessager();
        this.startTime = System.currentTimeMillis();

        // 加载配置
        this.config = loadConfig();

        // 初始化 JSON 序列化器
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // 初始化提取器和扫描器
        this.extractor = new MetadataExtractor();
        this.scanner = new AnnotationScanner();

        // 初始化缓存（优先使用配置路径，其次尝试探测输出目录，最后回退到 user.dir）
        String buildDir = processingEnv.getOptions().get("permission.build.dir");
        if (buildDir == null || buildDir.isEmpty()) {
            try {
                // 尝试创建一个临时资源文件来定位输出目录
                FileObject resource = filer.createResource(StandardLocation.CLASS_OUTPUT, "", ".permission-cache-probe");
                // 转换为 File 对象并获取父目录
                java.io.File file = new java.io.File(resource.toUri());
                buildDir = file.getParent();
                // 尝试删除探测文件（如果支持）
                resource.delete();
            } catch (Exception e) {
                // 如果失败，回退到 user.dir + /target (可能不准确)
                buildDir = System.getProperty("user.dir") + "/target";
                printWarning("Failed to determine build directory, fallback to user.dir: " + buildDir);
            }
        }
        this.cache = new PermissionCache(buildDir);

        printNote("Permission processor initialized, cache dir: " + buildDir);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // 检查是否启用
        if (!config.isEnabled()) {
            printNote("Permission extraction is disabled");
            return false;
        }

        // 只在最后一轮处理
        if (!roundEnv.processingOver()) {
            return false;
        }

        try {
            // 收集所有 API 权限信息
            List<ApiPermissionMetadata> allApis = new ArrayList<>();

            // 扫描所有 Controller 类
            Set<TypeElement> controllers = findControllers(roundEnv);
            printNote("Found " + controllers.size() + " controller classes");

            for (TypeElement controller : controllers) {
                List<ApiPermissionMetadata> apis = processController(controller);
                allApis.addAll(apis);
            }

            // 生成 JSON 文件
            if (!allApis.isEmpty()) {
                generateJsonFile(allApis);
                printNote("Generated permission file with " + allApis.size() + " APIs");
            }

            // 保存缓存
            cache.saveCache();

            // 输出性能统计
            long duration = System.currentTimeMillis() - startTime;
            printNote("Permission extraction completed in " + duration + "ms");

            // 输出缓存统计
            Map<String, Object> cacheStats = cache.getStatistics();
            printNote("Cache statistics: " + cacheStats.get("totalEntries") + " entries");

        } catch (Exception e) {
            printError("Failed to process permissions: " + e.getMessage());
        }

        return false;
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

        // 检查缓存，如果类未变更则使用缓存
        if (!cache.isClassChanged(controller)) {
            List<ApiPermissionMetadata> cached = cache.getCachedPermissions(className);
            if (!cached.isEmpty()) {
                printNote("Using cached permissions for " + className + " (" + cached.size() + " APIs)");
                return cached;
            }
        }

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

        // 更新缓存
        if (!apis.isEmpty()) {
            cache.updateCache(controller, apis);
            printNote("Processed " + className + " (" + apis.size() + " APIs)");
        }

        return apis;
    }

    /**
     * 处理单个方法
     */
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
    private void printWarning(String message) {
        messager.printMessage(Diagnostic.Kind.WARNING, "[PermissionProcessor] " + message);
    }

    /**
     * 输出错误信息
     */
    private void printError(String message) {
        messager.printMessage(Diagnostic.Kind.ERROR, "[PermissionProcessor] " + message);
    }
}
