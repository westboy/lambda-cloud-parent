package com.lambda.autoconfig;

import jakarta.validation.constraints.NotNull;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * @author Jin
 */
@Setter
@Getter
@Validated
@ConfigurationProperties(prefix = "lambda.lucene")
public class LuceneProperties {

    /** 是否启用 Lucene 本地索引基础设施。 */
    private boolean enabled = false;

    /** 所有逻辑索引的本地根目录。 */
    @NotNull
    private Path directory = Paths.get(System.getProperty("user.home"), ".lambda", "lucene");
}
