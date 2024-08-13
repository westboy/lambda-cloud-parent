package com.jingfang.security.web.hmac.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jingfang.cloud.core.principal.LoginUser;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * HmacClient
 *
 * @author jpjoo
 */
@Getter
@Setter
public class HmacClient implements LoginUser {

    private String appid;
    private String secret;
    private String nickname;
    private String hosts;
    private Date expired;
    private boolean enabled;
    private String tenantId;
    private Set<String> roles = Set.of("ROLE_HMAC");
    private Set<String> permissions = Set.of();

    public HmacClient() {
    }

    public HmacClient(String appid, String secret) {
        this.appid = appid;
        this.secret = secret;
        this.enabled = true;
        this.expired = Date.from(LocalDate.of(9999, 12, 31).atStartOfDay(ZoneId.systemDefault()).toInstant());
    }


    @Override
    public String getUsername() {
        return this.appid;
    }

    @Override
    public String getCredentials() {
        return this.secret;
    }

    @Override
    public Boolean getAccountLocked() {
        return false;
    }

    @Override
    public Boolean getAccountExpired() {
        return false;
    }


    @JsonIgnore
    public Set<String> getWhitelist() {
        if (StringUtils.isBlank(hosts)) {
            return new HashSet<>();
        }
        return new HashSet<>(Arrays.asList(hosts.split(",")));
    }

}
