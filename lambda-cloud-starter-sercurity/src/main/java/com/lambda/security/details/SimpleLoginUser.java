package com.lambda.security.details;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lambda.cloud.core.principal.LoginUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Set;

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
