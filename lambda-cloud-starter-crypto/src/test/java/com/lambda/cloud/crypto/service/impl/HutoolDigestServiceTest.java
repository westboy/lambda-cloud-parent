package com.lambda.cloud.crypto.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import cn.hutool.core.util.HexUtil;
import com.lambda.cloud.crypto.algorithm.DigestAlgorithm;
import com.lambda.cloud.crypto.service.DigestService;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/**
 * {@link HutoolDigestService} 摘要测试（使用公开标准测试向量）
 *
 * @author Jin
 * @since 2026.1.1
 */
class HutoolDigestServiceTest {

    private static final byte[] DATA = "abc".getBytes(StandardCharsets.UTF_8);

    @Test
    void shouldDigestSha256() {
        DigestService service = new HutoolDigestService();

        // SHA-256("abc") 标准测试向量
        assertThat(HexUtil.encodeHexStr(service.digest(DATA, DigestAlgorithm.SHA256)))
                .isEqualToIgnoringCase("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
    }

    @Test
    void shouldDigestSm3() {
        DigestService service = new HutoolDigestService();

        // SM3("abc") 标准测试向量（GB/T 32905-2016）
        assertThat(HexUtil.encodeHexStr(service.digest(DATA, DigestAlgorithm.SM3)))
                .isEqualToIgnoringCase("66c7f0f462eeedd9d1f2d46bdc10e4e24167c4875cf2f7a2297da02b8f4ba8e0");
    }

    @Test
    void shouldDefaultToSha256() {
        DigestService service = new HutoolDigestService();

        assertThat(service.digest(DATA)).isEqualTo(service.digest(DATA, DigestAlgorithm.SHA256));
    }
}
