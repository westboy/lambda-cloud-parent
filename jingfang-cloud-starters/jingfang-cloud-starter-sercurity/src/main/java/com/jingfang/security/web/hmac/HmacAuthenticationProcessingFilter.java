package com.jingfang.security.web.hmac;

import com.jingfang.cloud.core.principal.LoginUser;
import com.jingfang.cloud.core.utils.Assert;
import com.jingfang.cloud.mvc.WebHttpUtils;
import com.jingfang.security.enums.LoginType;
import com.jingfang.security.exception.AuthenticationException;
import com.jingfang.security.exception.BadCredentialsException;
import com.jingfang.security.exception.UsernameNotFoundException;
import com.jingfang.security.password.HmacShaEncoder;
import com.jingfang.security.service.HmacClientService;
import com.jingfang.security.utils.HmacUtils;
import com.jingfang.security.web.AbstractAuthenticationProcessingFilter;
import com.jingfang.security.web.hmac.model.HmacAuthorization;
import com.jingfang.security.web.hmac.model.HmacClient;
import com.jingfang.security.web.hmac.wrapper.HmacRequestWrapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.Set;

/**
 * HmacAuthenticationProcessingFilter
 *
 * @author jpjoo
 */
@Slf4j
public class HmacAuthenticationProcessingFilter extends AbstractAuthenticationProcessingFilter {

    private static final String REQUEST_HMAC_RUN_USER = "hmac-run-user";
    private static final String REQUEST_HMAC_TYPE = "hmac-run-type";
    private final HmacClientService hmacClientService;
    private final HmacShaEncoder passwordEncoder;

    public HmacAuthenticationProcessingFilter(HmacClientService hmacClientService, HmacShaEncoder passwordEncoder) {
        super(null);
        this.hmacClientService = hmacClientService;
        Assert.notNull(hmacClientService, "hmacClientService must not be null");
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    protected boolean doNextFilter() {
        return true;
    }

    protected HmacClient retrieveUser(String username, String remoteAddr) {
        LoginUser loadedUser = this.hmacClientService.loadClientByAppid(username);
        if (loadedUser instanceof HmacClient client) {
            if (StringUtils.isNotBlank(client.getHosts())) {
                Set<String> hosts = client.getWhitelist();
                if (!hosts.contains(remoteAddr)) {
                    log.debug("actual: {}, expected: {}", remoteAddr, hosts);
                    throw new BadCredentialsException("Password does not match stored value");
                }
            }
            return client;
        }
        throw new UsernameNotFoundException("Client " + username + "not found.");
    }

    @Override
    public LoginUser attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            if (request instanceof HmacRequestWrapper requestWrapper) {
                HmacAuthorization authorization = HmacUtils.getHmacAuthorization(requestWrapper);
                if (authorization == null) {
                    throw new BadCredentialsException("Password does not match stored value");
                }
                String appid = authorization.getAppid();
                String digest = authorization.getDigest();
                String timestamp = authorization.getTimestamp();
                String salt = HmacUtils.getHmacSaltValue(requestWrapper, appid, timestamp);
                LoginUser hmacClient = retrieveUser(appid, requestWrapper.getRemoteAddr());
                String encodedPassword = passwordEncoder.encode(hmacClient.getCredentials(), salt);
                if (!passwordEncoder.matches(digest, encodedPassword)) {
                    log.error("actual: {}, expected: {}, salt: {}", digest, encodedPassword, salt);
                    throw new BadCredentialsException("Password does not match stored value");
                }

                String runUserType = requestWrapper.getParameter(REQUEST_HMAC_TYPE);
                if (runUserType == null) {
                    runUserType = LoginType.ADMIN.getCode();
                }

                String runUserId = requestWrapper.getParameter(REQUEST_HMAC_RUN_USER);
                if (runUserId != null) {
                    hmacClient = hmacClientService.loginByUsername(runUserId, runUserType);
                    log.debug("hmac user {} changed to user: {}", hmacClient.getUsername(), runUserId);
                }

                request.setAttribute("loginType", runUserType);
                request.setAttribute("loginDevice", "default");
                return hmacClient;
            } else {
                throw new BadCredentialsException("此请求不支持 Hmac 认证失败！");
            }
        } catch (Exception failed) {
            throw new AuthenticationException("Hmac 认证失败！", failed);
        }

    }


    @Override
    protected HttpServletRequest wrapRequest(HttpServletRequest request) throws IOException {
        return new HmacRequestWrapper(request);
    }

    @Override
    protected boolean nonRequiresAuthentication(HttpServletRequest request) {
        return WebHttpUtils.isNotHmacRequest(request);
    }
}