package com.lambda.security.web.hmac.handler;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.session.SaTerminalInfo;
import cn.dev33.satoken.stp.StpLogic;
import com.lambda.autoconfig.SecurityProperties;
import com.lambda.cloud.core.Constants;
import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.cloud.core.utils.StpLogicUtils;
import com.lambda.security.handler.AuthenticationSuccessHandler;
import com.lambda.security.web.hmac.wrapper.HmacRequestWrapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * HMAC认证成功处理器
 *
 * @author jpjoo
 */
@SuppressFBWarnings("EI_EXPOSE_REP2")
@Slf4j
@RequiredArgsConstructor
public class HmacAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final SecurityProperties securityProperties;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, LoginUser loginUser) {
        if (request instanceof HmacRequestWrapper hmacRequestWrapper) {
            StpLogic stpLogic = StpLogicUtils.getStpLogic(Constants.HMAC);
            SaSession saSession = stpLogic.getSessionByLoginId(loginUser.getName(), false);
            if (saSession != null) {
                SaTerminalInfo terminalInfo = saSession.getTerminalList().getFirst();
                String tokenValue = terminalInfo.getTokenValue();
                hmacRequestWrapper.addHeader(
                        securityProperties.getSaToken().getTokenName(),
                        securityProperties.getSaToken().getTokenPrefix() + " " + tokenValue);
            } else {
                stpLogic.login(loginUser.getName());
                stpLogic.getTokenSession().set(Constants.LOGIN_USER, loginUser);
            }
        } else {
            log.error("HmacAuthenticationSuccessHandler onAuthenticationSuccess request is not HmacRequestWrapper");
        }
    }
}
