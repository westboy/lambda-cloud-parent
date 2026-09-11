package com.lambda.cloud.crypto.key;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.BCUtil;
import cn.hutool.crypto.KeyUtil;
import cn.hutool.crypto.PemUtil;
import cn.hutool.crypto.SmUtil;
import com.lambda.autoconfig.CryptoProperties;
import com.lambda.autoconfig.CryptoProperties.KeyProperties;
import com.lambda.autoconfig.CryptoProperties.KeyStoreProperties;
import com.lambda.cloud.crypto.BcProvider;
import com.lambda.cloud.crypto.algorithm.AlgorithmType;
import com.lambda.cloud.crypto.exception.CryptoException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.util.PrivateKeyInfoFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemObject;

/**
 * 配置驱动的密钥解析器
 * <p>
 * 从 {@code lambda.crypto.keys} 配置解析密钥，构造期完成全部解析（fail-fast）。
 * 解析优先级：密钥库（key-store）&gt; 内联密钥（public-key/private-key/secret-key）。
 * <ul>
 * <li>RSA：公私钥支持 PEM（X.509/PKCS#8，不支持加密 PEM）或 Base64</li>
 * <li>SM2：公私钥为 hex 字符串</li>
 * <li>AES/SM4：对称密钥优先按 hex 解析，否则按 Base64</li>
 * <li>密钥库：JKS / PKCS12，location 支持文件路径与 classpath 资源</li>
 * </ul>
 *
 * @author Jin
 * @since 2026.1.1
 */
public final class PropertiesKeyProvider implements KeyProvider {

    static {
        BcProvider.ensureRegistered();
    }

    /**
     * AES 合法密钥长度（字节）
     */
    private static final Set<Integer> AES_KEY_LENGTHS = Set.of(16, 24, 32);

    /**
     * SM4 密钥长度（字节）
     */
    private static final int SM4_KEY_LENGTH = 16;

    private final Map<String, KeyEntry> entries;

    private final String defaultKeyId;

    public PropertiesKeyProvider(CryptoProperties properties) {
        this.defaultKeyId = properties.getDefaultKeyId();
        Map<String, KeyEntry> resolved = new LinkedHashMap<>();
        for (KeyProperties keyProperties : properties.getKeys()) {
            KeyEntry entry = resolve(keyProperties);
            resolved.put(entry.getId(), entry);
        }
        this.entries = Collections.unmodifiableMap(resolved);
    }

    @Override
    public KeyEntry get(String keyId) {
        KeyEntry entry = entries.get(keyId);
        if (entry == null) {
            throw new CryptoException(
                    "Crypto key not found: %s, configured keys: %s".formatted(keyId, entries.keySet()));
        }
        return entry;
    }

    @Override
    public KeyEntry get() {
        return get(defaultKeyId);
    }

    private KeyEntry resolve(KeyProperties properties) {
        if (StrUtil.isBlank(properties.getId())) {
            throw new CryptoException("Crypto key id must not be blank");
        }
        if (properties.getType() == null) {
            throw new CryptoException("Crypto key type must not be null, keyId: " + properties.getId());
        }
        if (properties.getKeyStore() != null) {
            return resolveFromKeyStore(properties);
        }
        return switch (properties.getType()) {
            case RSA ->
                KeyEntry.of(
                        properties.getId(),
                        AlgorithmType.RSA,
                        parseRsaPublicKey(properties.getPublicKey(), properties.getId()),
                        parseRsaPrivateKey(properties.getPrivateKey(), properties.getId()));
            case SM2 ->
                KeyEntry.of(
                        properties.getId(),
                        AlgorithmType.SM2,
                        parseSm2PublicKey(properties.getPublicKey(), properties.getId()),
                        parseSm2PrivateKey(properties.getPrivateKey(), properties.getId()));
            case AES, SM4 ->
                KeyEntry.of(
                        properties.getId(),
                        properties.getType(),
                        parseSecretKey(properties.getSecretKey(), properties.getType(), properties.getId()));
        };
    }

    private KeyEntry resolveFromKeyStore(KeyProperties properties) {
        KeyStoreProperties store = properties.getKeyStore();
        if (StrUtil.isBlank(store.getLocation()) || StrUtil.isBlank(store.getAlias())) {
            throw new CryptoException(
                    "Crypto key-store location and alias must not be blank, keyId: " + properties.getId());
        }
        char[] password =
                store.getPassword() == null ? null : store.getPassword().toCharArray();
        try (InputStream in = openLocation(store.getLocation())) {
            KeyStore keyStore = KeyStore.getInstance(store.getType());
            keyStore.load(in, password);
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(store.getAlias(), password);
            PublicKey publicKey = null;
            if (keyStore.getCertificate(store.getAlias()) != null) {
                publicKey = keyStore.getCertificate(store.getAlias()).getPublicKey();
            }
            if (privateKey == null && publicKey == null) {
                throw new CryptoException("Crypto key-store alias not found: %s, keyId: %s"
                        .formatted(store.getAlias(), properties.getId()));
            }
            return KeyEntry.of(properties.getId(), properties.getType(), publicKey, privateKey);
        } catch (IOException e) {
            throw new CryptoException(
                    "Failed to load crypto key-store: %s, keyId: %s".formatted(store.getLocation(), properties.getId()),
                    e);
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | UnrecoverableKeyException e) {
            throw new CryptoException(
                    "Invalid crypto key-store: %s, keyId: %s".formatted(store.getLocation(), properties.getId()), e);
        }
    }

    /**
     * 打开密钥库位置：优先文件系统，回退 classpath 资源
     */
    private InputStream openLocation(String location) throws IOException {
        Path path = Path.of(location);
        if (Files.exists(path)) {
            return Files.newInputStream(path);
        }
        InputStream resource = Thread.currentThread().getContextClassLoader().getResourceAsStream(location);
        if (resource == null) {
            resource = PropertiesKeyProvider.class.getClassLoader().getResourceAsStream(location);
        }
        if (resource == null) {
            throw new CryptoException("Crypto key-store location not found: " + location);
        }
        return resource;
    }

    private PublicKey parseRsaPublicKey(String value, String keyId) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        try {
            return KeyUtil.generatePublicKey("RSA", decodeKeyMaterial(value));
        } catch (Exception e) {
            throw new CryptoException("Invalid RSA public key, keyId: " + keyId, e);
        }
    }

    private PrivateKey parseRsaPrivateKey(String value, String keyId) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        try {
            return KeyUtil.generatePrivateKey("RSA", decodeKeyMaterial(value));
        } catch (Exception e) {
            throw new CryptoException("Invalid RSA private key, keyId: " + keyId, e);
        }
    }

    private PublicKey parseSm2PublicKey(String value, String keyId) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        try {
            return BCUtil.decodeECPoint(value, SmUtil.SM2_CURVE_NAME);
        } catch (Exception e) {
            throw new CryptoException("Invalid SM2 public key (hex expected), keyId: " + keyId, e);
        }
    }

    private PrivateKey parseSm2PrivateKey(String value, String keyId) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        try {
            ECPrivateKeyParameters params = BCUtil.toSm2Params(value);
            PrivateKeyInfo info = PrivateKeyInfoFactory.createPrivateKeyInfo(params);
            return KeyFactory.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME)
                    .generatePrivate(new PKCS8EncodedKeySpec(info.getEncoded()));
        } catch (Exception e) {
            throw new CryptoException("Invalid SM2 private key (hex expected), keyId: " + keyId, e);
        }
    }

    private SecretKey parseSecretKey(String value, AlgorithmType type, String keyId) {
        if (StrUtil.isBlank(value)) {
            throw new CryptoException("Crypto secret-key must not be blank, keyId: " + keyId);
        }
        byte[] bytes = value.matches("[0-9a-fA-F]+") ? HexUtil.decodeHex(value) : decodeKeyMaterial(value);
        String algorithm = type == AlgorithmType.AES ? "AES" : "SM4";
        boolean validLength =
                type == AlgorithmType.AES ? AES_KEY_LENGTHS.contains(bytes.length) : bytes.length == SM4_KEY_LENGTH;
        if (!validLength) {
            throw new CryptoException(
                    "Invalid %s secret key length: %d bytes, keyId: %s".formatted(algorithm, bytes.length, keyId));
        }
        return new SecretKeySpec(bytes, algorithm);
    }

    /**
     * 解码密钥材料：PEM 优先，否则按 Base64 解码
     */
    private byte[] decodeKeyMaterial(String value) {
        if (value.contains("-----BEGIN")) {
            PemObject pemObject =
                    PemUtil.readPemObject(new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8)));
            if (pemObject == null) {
                throw new CryptoException("Invalid PEM key material");
            }
            return pemObject.getContent();
        }
        return Base64.decode(value);
    }
}
