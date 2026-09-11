package com.lambda.cloud.crypto.key;

import com.lambda.cloud.crypto.exception.CryptoException;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试用固定密钥解析器
 *
 * @author Jin
 * @since 2026.1.1
 */
public class FixedKeyProvider implements KeyProvider {

    private final Map<String, KeyEntry> entries = new HashMap<>();

    public FixedKeyProvider(KeyEntry... keyEntries) {
        for (KeyEntry entry : keyEntries) {
            entries.put(entry.getId(), entry);
        }
    }

    @Override
    public KeyEntry get(String keyId) {
        KeyEntry entry = entries.get(keyId);
        if (entry == null) {
            throw new CryptoException("Crypto key not found: " + keyId);
        }
        return entry;
    }

    @Override
    public KeyEntry get() {
        return get("default");
    }
}
