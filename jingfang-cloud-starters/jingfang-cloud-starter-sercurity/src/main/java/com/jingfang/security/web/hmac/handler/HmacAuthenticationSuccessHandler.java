package com.jingfang.security.web.hmac.handler;

import cn.dev33.satoken.stp.StpLogic;
import com.jingfang.cloud.core.principal.LoginUser;
import com.jingfang.cloud.core.principal.LoginType;
import com.jingfang.security.handler.AuthenticationSuccessHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * HMAC认证成功处理器
 *
 * @author jpjoo
 */
@Slf4j
public class HmacAuthenticationSuccessHandler implements AuthenticationSuccessHandler {


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, LoginUser loginUser) {
        String device = (String) request.getAttribute("loginDevice");
        //多用户类型支持
        String loginType = (String) request.getAttribute("loginType");
        //获取stpLogic
        StpLogic stpLogic = LoginType.getStpLogic(loginType);
        //用户登录
        stpLogic.login(loginUser.getUsername(), device);
        //持久化当前用户
        stpLogic.getTokenSession().set("loginUser", loginUser);
    }
}
