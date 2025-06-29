package com.lambda.security.service;

import cn.dev33.satoken.stp.StpInterface;
import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.security.exception.AuthenticationException;
import java.util.List;

/**
 * UserDetailService
 *
 * @author jin
 */
public interface UserDetailService extends StpInterface {

    default LoginUser loginByUsername(String username, String loginType) throws AuthenticationException {
        throw new AuthenticationException("用户名登录暂未实现！");
    }

    default LoginUser loginByMobile(String mobile, String loginType) throws AuthenticationException {
        throw new AuthenticationException("手机号登录暂未实现！");
    }

    @Override
    default List<String> getPermissionList(Object loginId, String loginType) {
        return List.of();
    }

    @Override
    default List<String> getRoleList(Object loginId, String loginType) {
        return List.of();
    }
}
