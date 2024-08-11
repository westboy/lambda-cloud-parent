package com.jingfang.security.service;

import com.jingfang.cloud.core.principal.LoginUser;

/**
 * HmacClientService
 *
 * @author jpjoo
 */
public interface HmacClientService extends UserDetailService {

    LoginUser loadClientByAppid(String appid);
}
