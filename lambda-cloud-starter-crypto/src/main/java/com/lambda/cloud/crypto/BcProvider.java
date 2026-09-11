package com.lambda.cloud.crypto;

import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * BouncyCastle provider 注册工具
 * <p>
 * SM2/SM3/SM4 与 SM3withSM2 签名依赖 BC provider，各服务使用前调用 {@link #ensureRegistered()}（幂等）。
 *
 * @author Jin
 * @since 2026.1.1
 */
public final class BcProvider {

    private BcProvider() {}

    /**
     * 确保 BC provider 已注册（幂等）
     */
    public static void ensureRegistered() {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }
}
