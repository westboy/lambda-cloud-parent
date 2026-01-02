package com.lambda.security.encode;

import com.lambda.cloud.test.assertion.LambdaAssertions;
import org.junit.jupiter.api.Test;

class StandardPasswordEncoderTest {
    @Test
    void passwordTest() {
        StandardPasswordEncoder standardPasswordEncoder = new StandardPasswordEncoder();

        String rawPassword = "123456";

        String encoded = standardPasswordEncoder.encode(rawPassword);
        LambdaAssertions.assertThat(encoded).isNotNull().isNotEmpty();

        boolean matches = standardPasswordEncoder.matches(rawPassword, encoded);
        LambdaAssertions.assertThat(matches).isTrue();

        boolean wrongMatches = standardPasswordEncoder.matches("wrongPassword", encoded);
        LambdaAssertions.assertThat(wrongMatches).isFalse();

        boolean fixedMatches = standardPasswordEncoder.matches(
                rawPassword, "dd8b4d24b2aff492b1c893894af1f54fe610435d8152678697e055307029e509ebd6ff9dfb1e1e71");
        LambdaAssertions.assertThat(fixedMatches).isTrue();
    }
}
