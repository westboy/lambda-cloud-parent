package com.lambda.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lambda.cloud.core.principal.LoginUser;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@SuppressFBWarnings(
        value = {"EI_EXPOSE_REP"},
        justification = "springboot properties")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SimpleLoginUser implements LoginUser {

    private String id;

    private String username;

    @JsonIgnore
    private String password;

    private Boolean accountExpired;

    private Boolean accountLocked;

    private Set<String> roles = Set.of();

    private Set<String> permissions = Set.of();

    @JsonIgnore
    @Override
    public String getCredentials() {
        return password;
    }

    @Override
    public String getOrgId() {
        return "";
    }

    @JsonIgnore
    @Override
    public String getName() {
        return id;
    }
}
