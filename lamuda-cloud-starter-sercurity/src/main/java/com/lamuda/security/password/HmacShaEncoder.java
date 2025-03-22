package com.lamuda.security.password;

import com.lamuda.cloud.core.utils.Assert;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;

@Slf4j
public class HmacShaEncoder implements PasswordEncoder {

	private static boolean check(String expected, String actual) {
		char[] caa = expected.toCharArray();
		char[] cab = actual.toCharArray();

		if (caa.length != cab.length) {
			return false;
		}

		byte ret = 0;
		for (int i = 0; i < caa.length; i++) {
			ret |= (byte) (caa[i] ^ cab[i]);
		}
		return ret == 0;
	}

	@Override
	public String encode(CharSequence rawPassword) {
		Assert.notNull(rawPassword, "rawPassword must not be null");
		return rawPassword.toString();
	}

	public String encode(String password, String salt) {
		Assert.notNull(salt, "salt must not be null");
		Assert.notNull(password, "password must not be null");
		final byte[] key = password.getBytes(StandardCharsets.UTF_8);
		final byte[] digest = salt.getBytes(StandardCharsets.UTF_8);
		byte[] bytes = HmacUtils.getInitializedMac(HmacAlgorithms.HMAC_SHA_1, key).doFinal(digest);
		return Base64.encodeBase64String(bytes);
	}

	@Override
	public boolean matches(CharSequence rawPassword, String encodedPassword) {
		if (!StringUtils.hasText(rawPassword) || !StringUtils.hasText(encodedPassword)) {
			return false;
		}
		String actual = encode(rawPassword);
		if (log.isDebugEnabled()) {
			log.debug("actual: {}, expected: {}", actual, encodedPassword);
		}
		return check(actual, encodedPassword);
	}
}