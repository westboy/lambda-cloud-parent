package com.lambda.cloud.lucene.manager;

import com.lambda.cloud.lucene.analysis.LuceneAnalyzerFactory;
import com.lambda.cloud.lucene.exception.LuceneOperationException;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** 在同一本地根目录下按逻辑名称创建并缓存相互隔离的 Lucene 索引。 */
public final class LuceneManagerFactory implements AutoCloseable {

    private final Path rootDirectory;
    private final LuceneAnalyzerFactory analyzerFactory;
    private final Map<String, LuceneManager> managers = new ConcurrentHashMap<>();

    public LuceneManagerFactory(Path rootDirectory, LuceneAnalyzerFactory analyzerFactory) throws IOException {
        this.rootDirectory = rootDirectory.toAbsolutePath().normalize();
        this.analyzerFactory = analyzerFactory;
        Files.createDirectories(this.rootDirectory);
    }

    public Path getRootDirectory() {
        return rootDirectory;
    }

    public LuceneManager get(String indexName) {
        String normalizedName = normalizeName(indexName);
        return managers.computeIfAbsent(normalizedName, name -> {
            Path indexDirectory = resolve(name);
            try {
                return new LuceneManager(indexDirectory, analyzerFactory.create());
            } catch (IOException | RuntimeException e) {
                throw new LuceneOperationException("创建 Lucene 索引失败: " + name, e);
            }
        });
    }

    public boolean exists(String indexName) {
        String normalizedName = normalizeName(indexName);
        return managers.containsKey(normalizedName) || Files.isDirectory(resolve(normalizedName));
    }

    public void close(String indexName) {
        String normalizedName = normalizeName(indexName);
        LuceneManager manager = managers.remove(normalizedName);
        if (manager == null) {
            return;
        }
        try {
            manager.close();
        } catch (IOException e) {
            throw new LuceneOperationException("关闭 Lucene 索引失败: " + normalizedName, e);
        }
    }

    /** 关闭索引并删除其位于根目录内的全部本地文件。 */
    public void delete(String indexName) {
        String normalizedName = normalizeName(indexName);
        close(normalizedName);
        Path indexDirectory = resolve(normalizedName);
        if (!Files.exists(indexDirectory)) {
            return;
        }
        try {
            Files.walkFileTree(indexDirectory, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path directory, IOException error) throws IOException {
                    if (error != null) {
                        throw error;
                    }
                    Files.delete(directory);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new LuceneOperationException("删除 Lucene 索引失败: " + normalizedName, e);
        }
    }

    @Override
    public void close() {
        List<RuntimeException> failures = new ArrayList<>();
        for (String indexName : List.copyOf(managers.keySet())) {
            try {
                close(indexName);
            } catch (RuntimeException e) {
                failures.add(e);
            }
        }
        if (!failures.isEmpty()) {
            LuceneOperationException failure = new LuceneOperationException("关闭 Lucene 索引工厂失败", failures.getFirst());
            failures.stream().skip(1).forEach(failure::addSuppressed);
            throw failure;
        }
    }

    private Path resolve(String normalizedName) {
        Path resolved = rootDirectory.resolve(normalizedName).normalize();
        if (!resolved.startsWith(rootDirectory) || resolved.equals(rootDirectory)) {
            throw new IllegalArgumentException("非法 Lucene 索引名称: " + normalizedName);
        }
        return resolved;
    }

    private static String normalizeName(String indexName) {
        if (indexName == null || indexName.isBlank()) {
            throw new IllegalArgumentException("Lucene indexName 不能为空");
        }
        Path path = Path.of(indexName).normalize();
        if (path.isAbsolute() || path.getNameCount() == 0 || path.startsWith("..")) {
            throw new IllegalArgumentException("非法 Lucene 索引名称: " + indexName);
        }
        return path.toString();
    }
}
