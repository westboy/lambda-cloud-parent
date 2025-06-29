package com.lambda.security.web.hmac.handler;

import cn.dev33.satoken.session.SaTerminalInfo;
import cn.dev33.satoken.stp.StpLogic;
import cn.hutool.core.collection.CollUtil;
import com.lambda.autoconfig.SecurityProperties;
import com.lambda.cloud.core.Constants;
import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.cloud.core.utils.StpLogicUtils;
import com.lambda.security.handler.AuthenticationSuccessHandler;
import com.lambda.security.web.hmac.wrapper.HmacRequestWrapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
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
        if (request instanceof HmacRequestWrapper wrapper) {
            StpLogic stpLogic = StpLogicUtils.getStpLogic(Constants.HMAC);
            List<SaTerminalInfo> terminalInfoList = stpLogic.getTerminalListByLoginId(loginUser.getName());
            if (CollUtil.isNotEmpty(terminalInfoList)) {
                String tokenValue = terminalInfoList.getFirst().getTokenValue();
                wrapper.addHeader(
                        securityProperties.getSaToken().getTokenName(),
                        securityProperties.getSaToken().getTokenPrefix() + " " + tokenValue);
            } else {
                stpLogic.login(loginUser.getName());
                stpLogic.getTokenSession().set(Constants.LOGIN_USER, loginUser);
            }
        }
    }
}
