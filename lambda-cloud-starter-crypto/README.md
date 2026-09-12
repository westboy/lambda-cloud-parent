# lambda-cloud-starter-crypto

`lambda-cloud-starter-crypto` 提供统一的加解密与签名抽象，基于 Hutool + BouncyCastle 实现，覆盖 RSA / SM2 非对称加解密与加签验签、AES / SM4 对称加解密、信封（混合）加解密与常用摘要算法，密钥通过配置驱动解析并支持密钥库加载。

## 模块定位

- 对外暴露五类服务扩展点：`SignService`、`AsymmetricCryptoService`、`SymmetricCryptoService`、`HybridCryptoService`、`DigestService`，屏蔽算法差异。
- 通过 `KeyProvider` 扩展点解析密钥材料，默认实现 `PropertiesKeyProvider` 从 `lambda.crypto.keys` 配置解析。
- 所有 Bean 均为 `@ConditionalOnMissingBean` 装配，下游可注册自定义实现整体替换默认的 Hutool 实现（如对接 KMS、HSM）。
- 解析失败在启动期抛出 `CryptoException`（fail-fast），避免运行期才发现密钥配置错误。

## 目录结构（src/main）

```text
src/main/java/com/lambda/autoconfig/
├─ CryptoAutoConfiguration.java
└─ CryptoProperties.java

src/main/java/com/lambda/cloud/crypto/
├─ BcProvider.java
├─ CryptoConstants.java
├─ algorithm/
│  ├─ AlgorithmType.java
│  └─ DigestAlgorithm.java
├─ exception/
│  └─ CryptoException.java
├─ key/
│  ├─ KeyEntry.java
│  ├─ KeyProvider.java
│  └─ PropertiesKeyProvider.java
└─ service/
   ├─ SignService.java
   ├─ AsymmetricCryptoService.java
   ├─ SymmetricCryptoService.java
   ├─ HybridCryptoService.java
   ├─ DigestService.java
   └─ impl/
      ├─ HutoolSignService.java
      ├─ HutoolAsymmetricCryptoService.java
      ├─ HutoolSymmetricCryptoService.java
      ├─ HutoolHybridCryptoService.java
      └─ HutoolDigestService.java

src/main/resources/META-INF/spring/
└─ org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

自动装配注册项：

```text
com.lambda.autoconfig.CryptoAutoConfiguration
```

## 自动装配机制

`CryptoAutoConfiguration` 装配条件：

- `@ConditionalOnClass(cn.hutool.crypto.SecureUtil.class)`：classpath 存在 Hutool 时生效。
- `@ConditionalOnProperty(prefix = "lambda.crypto", name = "enabled", havingValue = "true", matchIfMissing = true)`：默认启用，`lambda.crypto.enabled=false` 可整体关闭。

装配 Bean：

| Bean | 默认实现 | 说明 |
| :--- | :--- | :--- |
| `KeyProvider` | `PropertiesKeyProvider` | 从 `lambda.crypto.keys` 解析密钥，构造期 fail-fast |
| `DigestService` | `HutoolDigestService` | 摘要计算 |
| `SignService` | `HutoolSignService` | 加签/验签 |
| `AsymmetricCryptoService` | `HutoolAsymmetricCryptoService` | 非对称加解密 |
| `SymmetricCryptoService` | `HutoolSymmetricCryptoService` | 对称加解密 |
| `HybridCryptoService` | `HutoolHybridCryptoService` | 信封（混合）加解密 |

## 算法矩阵

### 密钥算法（`AlgorithmType`）

| 类型 | 用途 | 签名算法 | 加密算法 |
| :--- | :--- | :--- | :--- |
| `RSA` | 非对称 | SHA256withRSA | RSA-OAEP（SHA-256） |
| `SM2` | 非对称（国密） | SM3withSM2 | SM2（C1C3C2） |
| `AES` | 对称 | — | AES-GCM，随机 IV 前置 |
| `SM4` | 对称（国密） | — | SM4-GCM，随机 IV 前置 |

签名/加密算法由密钥类型决定，调用方无需指定算法。

### 摘要算法（`DigestAlgorithm`）

`MD5`、`SHA1`（仅兼容存量数据，新场景禁用）、`SHA256`（默认）、`SHA512`、`SM3`。

## 服务接口约定

### SignService（加签/验签）

- `sign(data, keyId)` / `sign(data)`：私钥加签，返回签名值。
- `verify(data, signature, keyId)` / `verify(data, signature)`：公钥验签，返回是否通过。
- 密钥需为 RSA 或 SM2 类型。

### AsymmetricCryptoService（非对称加解密）

- `encrypt(data, keyId)` / `encrypt(data)`：公钥加密。
- `decrypt(data, keyId)` / `decrypt(data)`：私钥解密。
- 明文长度受非对称算法限制，大数据请使用 `HybridCryptoService`。

### SymmetricCryptoService（对称加解密）

- `encrypt(data, keyId)` / `encrypt(data)`：加密，返回 `iv || ciphertext`（随机 IV 前置）。
- `decrypt(data, keyId)` / `decrypt(data)`：解密 IV 前置密文。
- 密钥需为 AES 或 SM4 类型。

### HybridCryptoService（信封/混合加解密）

- 解决非对称算法明文长度受限问题：每次加密生成随机 AES-256 数据密钥，数据用 AES-GCM 加密，数据密钥用指定非对称密钥封装。
- 密文格式：`keyLen(2字节,大端) || encryptedKey || iv(12字节) || ciphertext`。
- `encrypt(data, keyId)` / `decrypt(data, keyId)`，均有使用默认密钥的重载。

### DigestService（摘要）

- `digest(data, algorithm)`：按指定算法计算摘要。
- `digest(data)`：默认 SHA-256。

未指定 `keyId` 的重载使用 `lambda.crypto.default-key-id` 指向的密钥。

## 配置模型

配置前缀：`lambda.crypto`

- `enabled`：是否启用加密组件，默认 `true`。
- `default-key-id`：默认密钥标识，未指定 `keyId` 调用时使用，默认 `default`。
- `keys[]`：密钥列表：
  - `id`：密钥标识（唯一，必填）。
  - `type`：算法类型 `RSA` / `SM2` / `AES` / `SM4`（必填）。
  - `public-key` / `private-key`：非对称内联密钥。RSA 支持 PEM（X.509/PKCS#8，不支持加密 PEM）或 Base64；SM2 为 hex 字符串。可只配公钥（验签/加密）或只配私钥（签名/解密）。
  - `secret-key`：对称密钥，优先按 hex 解析，否则按 Base64。AES 支持 16/24/32 字节，SM4 必须 16 字节。
  - `key-store`：密钥库加载（与内联密钥互斥，优先生效）：
    - `type`：`JKS` / `PKCS12`，默认 `JKS`。
    - `location`：密钥库位置，优先文件系统路径，找不到时回退 classpath 资源。
    - `password`：密钥库密码。
    - `alias`：密钥别名。

## 使用示例

### 配置

```yaml
lambda:
  crypto:
    enabled: true
    default-key-id: default
    keys:
      - id: default
        type: RSA
        public-key: ${CRYPTO_RSA_PUBLIC_KEY}      # PEM 或 Base64
        private-key: ${CRYPTO_RSA_PRIVATE_KEY}
      - id: sm2-sign
        type: SM2
        public-key: ${CRYPTO_SM2_PUBLIC_KEY}      # hex
        private-key: ${CRYPTO_SM2_PRIVATE_KEY}
      - id: aes-data
        type: AES
        secret-key: ${CRYPTO_AES_KEY}             # hex 或 Base64
      - id: sm2-cert
        type: SM2
        key-store:
          type: PKCS12
          location: ${CRYPTO_JKS_PATH}
          password: ${CRYPTO_JKS_PASSWORD}
          alias: sign-key
```

### 调用

```java
@Service
public class CredentialService {

    private final SignService signService;
    private final HybridCryptoService hybridCryptoService;

    public CredentialService(SignService signService, HybridCryptoService hybridCryptoService) {
        this.signService = signService;
        this.hybridCryptoService = hybridCryptoService;
    }

    public byte[] sign(byte[] data) {
        // 使用默认密钥加签
        return signService.sign(data);
    }

    public boolean verify(byte[] data, byte[] signature) {
        // 指定密钥验签
        return signService.verify(data, signature, "sm2-sign");
    }

    public byte[] encryptCredential(byte[] plaintext) {
        // 信封加密，长度不限
        return hybridCryptoService.encrypt(plaintext, "default");
    }
}
```

## 扩展点

- **自定义 `KeyProvider`**：注册自定义 `KeyProvider` Bean（如从 KMS / 配置中心加载密钥）即可覆盖默认的 `PropertiesKeyProvider`，其余服务不受影响。
- **自定义服务实现**：注册任意服务接口的自定义实现 Bean（如对接 HSM 的 `SignService`），默认 Hutool 实现自动让位。

## 依赖说明

关键依赖（见 `pom.xml`）：

- `com.lambda.cloud:lambda-cloud-core`
- `cn.hutool:hutool-all`（版本由 parent pom 管理）
- `org.bouncycastle:bcprov-jdk18on` / `bcpkix-jdk18on`（SM2/SM3/SM4 与证书加载的底层 provider）
- `spring-boot-configuration-processor`（optional）

## 安全建议

- 不要在代码仓库中提交真实密钥。所有密钥值建议经环境变量注入（如 `${CRYPTO_RSA_PRIVATE_KEY}`），或对接 KMS / 配置中心加密能力。
- 生产环境推荐使用密钥库（JKS / PKCS12）或自定义 `KeyProvider` 对接 KMS，避免私钥以明文形式出现在配置文件中。
- MD5 / SHA-1 仅用于兼容存量数据，新场景必须使用 SHA-256 及以上强度算法。

## 当前实现约束

- `PropertiesKeyProvider` 在构造期完成全部密钥解析（fail-fast），配置错误会导致应用启动失败；密钥列表不支持运行期热更新，变更需重启。
- RSA 内联密钥不支持加密 PEM（无口令保护的 PKCS#8/X.509）。
- 非对称密钥只配单侧时的行为由操作决定：只配公钥的密钥用于签名/解密、只配私钥的密钥用于验签/加密时会在运行期抛 `CryptoException`。
- 密钥库加载仅提取别名对应的私钥与证书公钥，不支持对称密钥库存储。
- 默认实现不含密钥轮换、操作审计与限流策略，需要时请通过扩展点自定义。