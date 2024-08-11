package com.jingfang.security.web.hmac.service;

import com.jingfang.autoconfig.SecurityProperties;
import com.jingfang.cloud.core.principal.LoginUser;
import com.jingfang.security.exception.AuthenticationException;
import com.jingfang.security.service.HmacClientService;
import com.jingfang.security.service.UserDetailService;
import com.jingfang.security.web.hmac.model.HmacClient;

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

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return List.of();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return List.of();
    }
}
