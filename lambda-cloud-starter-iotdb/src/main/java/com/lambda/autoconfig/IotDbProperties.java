package com.lambda.autoconfig;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.Data;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * IotDbProperties
 *
 * @author Jin
 */
@Data
@ConfigurationProperties(prefix = "lambda.iotdb")
public class IotDbProperties {
    private Boolean treeDialect;
    private Boolean tableDialect;
    private Boolean enableSubscription;
    private int maxSize = 10;
    private String host;
    private int port;
    private String[] nodeUrls;
    private String user;
    private String password;
    private String database;
    private long ttl;
    private int thriftMaxFrameSize;

    public List<String> getNodeUrls() {
        if (ArrayUtils.isNotEmpty(nodeUrls)) {
            return Arrays.asList(nodeUrls);
        } else {
            return Collections.singletonList(host + ":" + port);
        }
    }
}
