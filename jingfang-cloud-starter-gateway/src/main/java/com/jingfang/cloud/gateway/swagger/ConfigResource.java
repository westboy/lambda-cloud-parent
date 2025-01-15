package com.jingfang.cloud.gateway.swagger;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

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
