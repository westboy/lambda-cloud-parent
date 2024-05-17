package com.lambda.autoconfig;

import com.lambda.cloud.lucene.manager.LuceneManager;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Jin
 */
@Configuration
@EnableConfigurationProperties({LuceneProperties.class})
public class LuceneAutoConfig {

    private LuceneProperties luceneProperties;

    @Autowired
    public LuceneAutoConfig setLuceneProperties(LuceneProperties luceneProperties) {
        this.luceneProperties = luceneProperties;
        return this;
    }

    @Bean
    public LuceneManager luceneManager() {
        String luceneFolder = luceneProperties.getDirectory();
        LuceneManager luceneManager = new LuceneManager();
        luceneManager.setDirectoryPath(Paths.get(luceneFolder));
        luceneManager.setAnalyzer(luceneProperties.getAnalyzer());
        return luceneManager;
    }
}
