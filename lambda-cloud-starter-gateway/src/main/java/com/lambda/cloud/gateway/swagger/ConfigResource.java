package com.lambda.cloud.gateway.swagger;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ConfigResource
 *
 * @author jpjoo
 */
@Data
public class ConfigResource {

    private String configUrl;

    private String oauth2RedirectUrl;

    private String validatorUrl;

    @SuppressFBWarnings(value = "EI_EXPOSE_REP")
    @JsonProperty("urls")
    private List<Group> groups = new ArrayList<>();

    @Data
    @NoArgsConstructor
    public static class Group {
        String name;
        String url;

        public Group(String name, String url) {
            this.name = name;
            this.url = url;
        }
    }
}
