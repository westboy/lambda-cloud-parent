package com.lambda.security.web.hmac.handler;

import cn.dev33.satoken.secure.SaSecureUtil;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import com.lambda.cloud.core.Constants;
import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.cloud.core.utils.StpLogicUtils;
import com.lambda.security.handler.AuthenticationSuccessHandler;
import com.lambda.security.web.hmac.model.HmacAuthorization;
import com.lambda.security.web.hmac.utils.HmacUtils;
import com.lambda.security.web.hmac.wrapper.HmacRequestWrapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * HMAC认证成功处理器
 *
 * @author jpjoo
 */
@Slf4j
@RequiredArgsConstructor
public class HmacAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, LoginUser loginUser) {
        if (request instanceof HmacRequestWrapper hmacRequestWrapper) {
            HmacAuthorization hmacAuthorization = HmacUtils.getHmacAuthorization(hmacRequestWrapper);
            if (hmacAuthorization == null) {
                log.error(" hmacAuthorization is null");
                return;
            }
            String saToken = SaSecureUtil.sha256(loginUser.getUsername() + loginUser.getCredentials());
            StpLogic stpLogic = StpLogicUtils.getStpLogic(Constants.HMAC);
            SaSession saSession = stpLogic.getSessionByLoginId(loginUser.getName(), false);
            if (saSession != null) {
                String tokenName = stpLogic.getTokenName();
                hmacRequestWrapper.addHeader(
                        tokenName, stpLogic.getConfigOrGlobal().getTokenPrefix() + " " + saToken);
            } else {
                SaLoginParameter saLoginParameter = stpLogic.createSaLoginParameter();
                saLoginParameter.setToken(saToken);
                stpLogic.login(loginUser.getName(), saLoginParameter);
                stpLogic.getTokenSession().set(Constants.LOGIN_USER, loginUser);
            }
        }
    }
}
