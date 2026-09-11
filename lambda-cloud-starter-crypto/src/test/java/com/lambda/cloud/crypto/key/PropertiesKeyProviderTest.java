package com.lambda.cloud.crypto.key;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.BCUtil;
import cn.hutool.crypto.KeyUtil;
import cn.hutool.crypto.PemUtil;
import com.lambda.autoconfig.CryptoProperties;
import com.lambda.cloud.crypto.algorithm.AlgorithmType;
import com.lambda.cloud.crypto.exception.CryptoException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPrivateKey;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * {@link PropertiesKeyProvider} 密钥解析测试
 *
 * @author Jin
 * @since 2026.1.1
 */
class PropertiesKeyProviderTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldResolveRsaKeysFromBase64() {
        KeyPair keyPair = KeyUtil.generateKeyPair("RSA", 2048);
        CryptoProperties properties = new CryptoProperties();
        properties
                .getKeys()
                .add(rsaKey(
                        "rsa-base64",
                        Base64.encode(keyPair.getPublic().getEncoded()),
                        Base64.encode(keyPair.getPrivate().getEncoded())));

        PropertiesKeyProvider provider = new PropertiesKeyProvider(properties);

        KeyEntry entry = provider.get("rsa-base64");
        assertThat(entry.getType()).isEqualTo(AlgorithmType.RSA);
        assertThat(entry.getPublicKey()).isEqualTo(keyPair.getPublic());
        assertThat(entry.getPrivateKey()).isEqualTo(keyPair.getPrivate());
    }

    @Test
    void shouldResolveRsaKeysFromPem() {
        KeyPair keyPair = KeyUtil.generateKeyPair("RSA", 2048);
        CryptoProperties properties = new CryptoProperties();
        properties
                .getKeys()
                .add(rsaKey(
                        "rsa-pem",
                        PemUtil.toPem("PUBLIC KEY", keyPair.getPublic().getEncoded()),
                        PemUtil.toPem("PRIVATE KEY", keyPair.getPrivate().getEncoded())));

        PropertiesKeyProvider provider = new PropertiesKeyProvider(properties);

        KeyEntry entry = provider.get("rsa-pem");
        assertThat(entry.getPublicKey()).isEqualTo(keyPair.getPublic());
        assertThat(entry.getPrivateKey()).isEqualTo(keyPair.getPrivate());
    }

    @Test
    void shouldResolveSm2KeysFromHex() {
        cn.hutool.crypto.asymmetric.SM2 sm2 = new cn.hutool.crypto.asymmetric.SM2();
        String publicHex = HexUtil.encodeHexStr(BCUtil.encodeECPublicKey(sm2.getPublicKey(), false));
        String privateHex = ((ECPrivateKey) sm2.getPrivateKey()).getS().toString(16);

        CryptoProperties properties = new CryptoProperties();
        CryptoProperties.KeyProperties key = new CryptoProperties.KeyProperties();
        key.setId("sm2-hex");
        key.setType(AlgorithmType.SM2);
        key.setPublicKey(publicHex);
        key.setPrivateKey(privateHex);
        properties.getKeys().add(key);

        PropertiesKeyProvider provider = new PropertiesKeyProvider(properties);

        KeyEntry entry = provider.get("sm2-hex");
        assertThat(entry.getPublicKey()).isEqualTo(sm2.getPublicKey());
        // BCECPrivateKey#equals 受算法名影响，比较私钥标量 D 验证解析正确性
        assertThat(((ECPrivateKey) entry.getPrivateKey()).getS())
                .isEqualTo(((ECPrivateKey) sm2.getPrivateKey()).getS());
    }

    @Test
    void shouldResolveSymmetricKeyFromBase64AndHex() {
        byte[] aesKey = randomBytes(32);
        byte[] sm4Key = randomBytes(16);

        CryptoProperties properties = new CryptoProperties();
        CryptoProperties.KeyProperties aes = new CryptoProperties.KeyProperties();
        aes.setId("aes");
        aes.setType(AlgorithmType.AES);
        aes.setSecretKey(Base64.encode(aesKey));
        properties.getKeys().add(aes);
        CryptoProperties.KeyProperties sm4 = new CryptoProperties.KeyProperties();
        sm4.setId("sm4");
        sm4.setType(AlgorithmType.SM4);
        sm4.setSecretKey(HexUtil.encodeHexStr(sm4Key));
        properties.getKeys().add(sm4);

        PropertiesKeyProvider provider = new PropertiesKeyProvider(properties);

        assertThat(provider.get("aes").getSecretKey()).isEqualTo(new SecretKeySpec(aesKey, "AES"));
        assertThat(provider.get("sm4").getSecretKey()).isEqualTo(new SecretKeySpec(sm4Key, "SM4"));
    }

    @Test
    void shouldResolveKeysFromKeyStore() throws Exception {
        KeyPair keyPair = KeyUtil.generateKeyPair("RSA", 2048);
        Path storePath = tempDir.resolve("test.p12");
        writeKeyStore(storePath, keyPair, "sign-key", "changeit");

        CryptoProperties properties = new CryptoProperties();
        CryptoProperties.KeyProperties key = new CryptoProperties.KeyProperties();
        key.setId("ks");
        key.setType(AlgorithmType.RSA);
        CryptoProperties.KeyStoreProperties store = new CryptoProperties.KeyStoreProperties();
        store.setType("PKCS12");
        store.setLocation(storePath.toString());
        store.setPassword("changeit");
        store.setAlias("sign-key");
        key.setKeyStore(store);
        properties.getKeys().add(key);

        PropertiesKeyProvider provider = new PropertiesKeyProvider(properties);

        KeyEntry entry = provider.get("ks");
        assertThat(entry.getPrivateKey()).isEqualTo(keyPair.getPrivate());
        assertThat(entry.getPublicKey()).isEqualTo(keyPair.getPublic());
    }

    @Test
    void shouldFailFastOnBlankSecretKey() {
        CryptoProperties properties = new CryptoProperties();
        CryptoProperties.KeyProperties key = new CryptoProperties.KeyProperties();
        key.setId("bad");
        key.setType(AlgorithmType.AES);
        properties.getKeys().add(key);

        assertThatThrownBy(() -> new PropertiesKeyProvider(properties))
                .isInstanceOf(CryptoException.class)
                .hasMessageContaining("bad");
    }

    @Test
    void shouldFailFastOnMissingType() {
        CryptoProperties properties = new CryptoProperties();
        CryptoProperties.KeyProperties key = new CryptoProperties.KeyProperties();
        key.setId("no-type");
        properties.getKeys().add(key);

        assertThatThrownBy(() -> new PropertiesKeyProvider(properties))
                .isInstanceOf(CryptoException.class)
                .hasMessageContaining("no-type");
    }

    @Test
    void shouldFailFastOnInvalidKeyLength() {
        CryptoProperties properties = new CryptoProperties();
        CryptoProperties.KeyProperties key = new CryptoProperties.KeyProperties();
        key.setId("short");
        key.setType(AlgorithmType.AES);
        key.setSecretKey(Base64.encode(randomBytes(8)));
        properties.getKeys().add(key);

        assertThatThrownBy(() -> new PropertiesKeyProvider(properties))
                .isInstanceOf(CryptoException.class)
                .hasMessageContaining("short");
    }

    @Test
    void shouldThrowOnUnknownKeyId() {
        PropertiesKeyProvider provider = new PropertiesKeyProvider(new CryptoProperties());

        assertThatThrownBy(() -> provider.get("missing"))
                .isInstanceOf(CryptoException.class)
                .hasMessageContaining("missing");
    }

    private CryptoProperties.KeyProperties rsaKey(String id, String publicKey, String privateKey) {
        CryptoProperties.KeyProperties key = new CryptoProperties.KeyProperties();
        key.setId(id);
        key.setType(AlgorithmType.RSA);
        key.setPublicKey(publicKey);
        key.setPrivateKey(privateKey);
        return key;
    }

    private byte[] randomBytes(int length) {
        byte[] bytes = new byte[length];
        ThreadLocalRandom.current().nextBytes(bytes);
        return bytes;
    }

    private void writeKeyStore(Path path, KeyPair keyPair, String alias, String password) throws Exception {
        X509Certificate certificate = selfSignedCertificate(keyPair);
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null, null);
        keyStore.setKeyEntry(alias, keyPair.getPrivate(), password.toCharArray(), new Certificate[] {certificate});
        try (var out = Files.newOutputStream(path)) {
            keyStore.store(out, password.toCharArray());
        }
    }

    private X509Certificate selfSignedCertificate(KeyPair keyPair) throws Exception {
        X500Name subject = new X500Name("CN=lambda-crypto-test");
        Date notBefore = new Date(System.currentTimeMillis() - 60_000);
        Date notAfter = new Date(System.currentTimeMillis() + 3_600_000);
        JcaX509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(
                subject, BigInteger.ONE, notBefore, notAfter, subject, keyPair.getPublic());
        JcaContentSignerBuilder signerBuilder = new JcaContentSignerBuilder("SHA256withRSA");
        return new JcaX509CertificateConverter()
                .getCertificate(builder.build(signerBuilder.build(keyPair.getPrivate())));
    }
}
