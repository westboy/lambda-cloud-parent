package com.lambda.autoconfig;

import com.lambda.cloud.lucene.analysis.LuceneAnalyzerFactory;
import com.lambda.cloud.lucene.manager.LuceneManagerFactory;
import java.io.IOException;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/** Lucene 本地索引自动配置。 */
@AutoConfiguration
@ConditionalOnClass(IndexWriter.class)
@EnableConfigurationProperties(LuceneProperties.class)
@ConditionalOnProperty(prefix = "lambda.lucene", name = "enabled", havingValue = "true", matchIfMissing = false)
public class LuceneAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public LuceneAnalyzerFactory luceneAnalyzerFactory() {
        return SmartChineseAnalyzer::new;
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean
    public LuceneManagerFactory luceneManagerFactory(LuceneProperties properties, LuceneAnalyzerFactory analyzerFactory)
            throws IOException {
        return new LuceneManagerFactory(properties.getDirectory(), analyzerFactory);
    }
}
