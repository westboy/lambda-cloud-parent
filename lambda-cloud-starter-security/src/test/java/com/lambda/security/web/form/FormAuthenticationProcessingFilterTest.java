package com.lambda.security.web.form;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cn.hutool.core.codec.Base64;
import cn.hutool.crypto.KeyUtil;
import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.cloud.crypto.algorithm.AlgorithmType;
import com.lambda.cloud.crypto.key.KeyEntry;
import com.lambda.cloud.crypto.key.KeyProvider;
import com.lambda.cloud.crypto.service.AsymmetricCryptoService;
import com.lambda.cloud.crypto.service.impl.HutoolAsymmetricCryptoService;
import com.lambda.security.exception.AuthenticationException;
import com.lambda.security.service.UserDetailService;
import com.lambda.security.web.form.locking.UserLoginLimitTracker;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * {@link FormAuthenticationProcessingFilter} 凭据解密集成测试
 *
 * @author Jin
 * @since 2026.1.1
 */
class FormAuthenticationProcessingFilterTest {

    private static final KeyPair RSA_PAIR = KeyUtil.generateKeyPair("RSA", 2048);
    private static final String PLAIN_USERNAME = "admin";
    private static final String PLAIN_PASSWORD = "secret123";

    @Test
    void shouldDecryptEncryptedCredentialsBeforeAuthenticate() {
        AtomicReference<String> loadedUsername = new AtomicReference<>();
        FormAuthenticationProcessingFilter filter = newFilter(userDetailService(loadedUsername));
        filter.setCredentialsDecryptor(new AsymmetricCredentialsDecryptor(asymmetricCryptoService(), "login"));

        MockHttpServletRequest request = postRequest(encrypt(PLAIN_USERNAME), encrypt(PLAIN_PASSWORD));
        MockHttpServletResponse response = new MockHttpServletResponse();

        LoginUser loginUser = filter.attemptAuthentication(request, response);

        assertThat(loginUser).isNotNull();
        assertThat(loadedUsername).hasValue(PLAIN_USERNAME);
    }

    @Test
    void shouldKeepPlaintextBehaviorWithoutDecryptor() {
        AtomicReference<String> loadedUsername = new AtomicReference<>();
        FormAuthenticationProcessingFilter filter = newFilter(userDetailService(loadedUsername));

        MockHttpServletRequest request = postRequest(PLAIN_USERNAME, PLAIN_PASSWORD);
        MockHttpServletResponse response = new MockHttpServletResponse();

        LoginUser loginUser = filter.attemptAuthentication(request, response);

        assertThat(loginUser).isNotNull();
        assertThat(loadedUsername).hasValue(PLAIN_USERNAME);
    }

    @Test
    void shouldFailAuthenticationOnDecryptFailure() {
        FormAuthenticationProcessingFilter filter = newFilter(userDetailService(new AtomicReference<>()));
        filter.setCredentialsDecryptor(new AsymmetricCredentialsDecryptor(asymmetricCryptoService(), "login"));

        MockHttpServletRequest request = postRequest("invalid-ciphertext", "invalid-ciphertext");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThatThrownBy(() -> filter.attemptAuthentication(request, response))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("登录凭据解密失败");
    }

    private String encrypt(String plaintext) {
        return Base64.encode(asymmetricCryptoService().encrypt(plaintext.getBytes(StandardCharsets.UTF_8), "login"));
    }

    private AsymmetricCryptoService asymmetricCryptoService() {
        KeyEntry entry = KeyEntry.of("login", AlgorithmType.RSA, RSA_PAIR.getPublic(), RSA_PAIR.getPrivate());
        return new HutoolAsymmetricCryptoService(new KeyProvider() {
            @Override
            public KeyEntry get(String keyId) {
                return entry;
            }

            @Override
            public KeyEntry get() {
                return entry;
            }
        });
    }

    private MockHttpServletRequest postRequest(String username, String password) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.addParameter("username", username);
        request.addParameter("password", password);
        return request;
    }

    private FormAuthenticationProcessingFilter newFilter(UserDetailService userDetailService) {
        FormAuthenticationProcessingFilter filter = new FormAuthenticationProcessingFilter("/login");
        filter.setUserDetailService(userDetailService);
        filter.setFormLockingStrategy(new NoOpLockingStrategy());
        filter.setPasswordEncoder(new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return rawPassword.toString();
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return rawPassword.toString().equals(encodedPassword);
            }
        });
        return filter;
    }

    private UserDetailService userDetailService(AtomicReference<String> loadedUsername) {
        return new UserDetailService() {
            @Override
            public LoginUser loginByUsername(String username, String loginType) {
                loadedUsername.set(username);
                return stubLoginUser();
            }
        };
    }

    private LoginUser stubLoginUser() {
        return new LoginUser() {
            @Override
            public String getCredentials() {
                return PLAIN_PASSWORD;
            }

            @Override
            public String getTenantId() {
                return "t1";
            }

            @Override
            public String getOrgId() {
                return null;
            }

            @Override
            public Boolean getAccountLocked() {
                return false;
            }

            @Override
            public Boolean getAccountExpired() {
                return false;
            }

            @Override
            public String getName() {
                return PLAIN_USERNAME;
            }
        };
    }

    /**
     * 无操作锁定策略
     */
    private static class NoOpLockingStrategy implements FormLockingStrategy {

        @Override
        public void loginSuccess(String username) {}

        @Override
        public void unlock(String username) {}

        @Override
        public boolean checkFailureTimes(String username) {
            return false;
        }

        @Override
        public UserLoginLimitTracker loginFailure(String username) {
            return null;
        }

        @Override
        public boolean getLockedState(String username) {
            return false;
        }

        @Override
        public int getDuration() {
            return 0;
        }

        @Override
        public TimeUnit getTimeUnit() {
            return TimeUnit.MINUTES;
        }
    }
}
