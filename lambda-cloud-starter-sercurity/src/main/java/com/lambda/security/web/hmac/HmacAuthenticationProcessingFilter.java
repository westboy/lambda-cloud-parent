package com.lambda.security.web.hmac;

import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.cloud.core.utils.Assert;
import com.lambda.cloud.mvc.WebHttpUtils;
import com.lambda.cloud.core.principal.LoginType;
import com.lambda.security.exception.AuthenticationException;
import com.lambda.security.exception.BadCredentialsException;
import com.lambda.security.exception.UsernameNotFoundException;
import com.lambda.security.encoder.HmacShaEncoder;
import com.lambda.security.service.HmacClientService;
import com.lambda.security.web.hmac.utils.HmacUtils;
import com.lambda.security.web.AbstractAuthenticationProcessingFilter;
import com.lambda.security.web.hmac.model.HmacAuthorization;
import com.lambda.security.web.hmac.model.HmacClient;
import com.lambda.security.web.hmac.wrapper.HmacRequestWrapper;
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
