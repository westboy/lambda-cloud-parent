package com.lambda.autoconfig;

import com.lambda.cloud.lucene.manager.LuceneManager;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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

    @SuppressFBWarnings(value = {"EI_EXPOSE_REP2"})
    private LuceneProperties luceneProperties;

    @Autowired
    public void setLuceneProperties(LuceneProperties luceneProperties) {
        this.luceneProperties = luceneProperties;
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
