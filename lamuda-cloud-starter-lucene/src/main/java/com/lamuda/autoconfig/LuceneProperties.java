package com.lamuda.autoconfig;

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
@ConfigurationProperties(prefix = "lamuda.lucene")
public class LuceneProperties {
    private String directory;
    private Analyzer analyzer = new SmartChineseAnalyzer();
}
