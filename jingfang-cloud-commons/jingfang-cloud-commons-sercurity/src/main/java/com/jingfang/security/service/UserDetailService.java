package com.jingfang.security.service;

import cn.dev33.satoken.stp.StpInterface;
import com.jingfang.cloud.core.principal.Principal;

/**
 * UserDetailService
 *
 * @author jin
 */
public interface UserDetailService extends StpInterface {


    /**
     * 用户登录
     *
     * @param username
     * @param loginType
     * @return
     */
    Principal loginByUsername(String username, String loginType);

}
