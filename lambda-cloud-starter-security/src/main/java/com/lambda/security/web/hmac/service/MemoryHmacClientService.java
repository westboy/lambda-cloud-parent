package com.lambda.security.web.hmac.service;

import com.lambda.autoconfig.SecurityProperties;
import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.security.exception.AuthenticationException;
import com.lambda.security.service.HmacClientService;
import com.lambda.security.service.UserDetailService;
import com.lambda.security.web.hmac.model.HmacClient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemoryHmacClientService implements HmacClientService {
    private final Map<String, LoginUser> users = new HashMap<>();
    private final UserDetailService userDetailService;

    public MemoryHmacClientService(UserDetailService userDetailService, List<SecurityProperties.Hmac.Client> clients) {
        for (SecurityProperties.Hmac.Client client : clients) {
            users.put(client.getAppid(), new HmacClient(client.getAppid(), client.getSecret()));
        }
        this.userDetailService = userDetailService;
    }

    @Override
    public LoginUser loadClientByAppid(String appid) {
        return users.get(appid);
    }

    @Override
    public LoginUser loginByUsername(String username, String loginType) throws AuthenticationException {
        return userDetailService.loginByUsername(username, loginType);
    }
}
