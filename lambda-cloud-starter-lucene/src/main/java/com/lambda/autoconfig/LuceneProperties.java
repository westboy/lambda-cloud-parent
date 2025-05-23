package com.lambda.autoconfig;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;
import lombok.Setter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Jin
 */
@Setter
@Getter
@SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "springboot properties class")
@ConfigurationProperties(prefix = "lambda.lucene")
public class LuceneProperties {
    private String directory;
    private Analyzer analyzer = new SmartChineseAnalyzer();
}
