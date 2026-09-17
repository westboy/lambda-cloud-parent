package com.lambda.security.web.verify.service.captcha;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.lambda.autoconfig.SecurityProperties;
import com.lambda.security.web.verify.service.captcha.store.CaptchaStore;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.ObjectMapper;

class CaptchaVerifyCodeGenerateImplTest {

    @Test
    void generatesLettersByDefault() throws Exception {
        assertCaptcha(null, "[A-HJ-NP-Z]{4}");
    }

    @Test
    void keepsMathModeAvailable() throws Exception {
        assertCaptcha("math", "[0-9]+");
    }

    private void assertCaptcha(String type, String expectedCodePattern) throws Exception {
        SecurityProperties properties = new SecurityProperties();
        if (type != null) {
            properties.getVerify().setCaptchaType(type);
        }
        CaptchaStore store = mock(CaptchaStore.class);
        MockHttpServletResponse response = new MockHttpServletResponse();
        new CaptchaVerifyCodeGenerateImpl(properties, new ObjectMapper(), store)
                .writeCaptcha(new MockHttpServletRequest("POST", "/jcaptcha"), response);

        var result = new ObjectMapper().readTree(response.getContentAsString());
        String token = result.get("__token").asText();
        assertThat(token).isNotBlank();
        assertThat(result.get("verifyImage").asText()).startsWith("data:image/png;base64,");

        ArgumentCaptor<String> code = ArgumentCaptor.forClass(String.class);
        verify(store).store(eq(token), code.capture(), eq(TimeUnit.SECONDS), eq(180));
        assertThat(code.getValue()).matches(expectedCodePattern);
    }
}
