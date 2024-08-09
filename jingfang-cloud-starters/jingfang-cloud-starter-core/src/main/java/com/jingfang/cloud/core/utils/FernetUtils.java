package com.jingfang.cloud.core.utils;

import cn.hutool.core.io.FileUtil;
import com.macasaet.fernet.Key;
import com.macasaet.fernet.Token;
import com.macasaet.fernet.Validator;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.Base64;
import java.util.function.Function;

/**
 * Fernet工具类
 *
 * @author 熊猫de 2018/7/12
 */
public class FernetUtils {

    private static final Validator<byte[]> byteValidator = new Validator<byte[]>() {
        public Function<byte[], byte[]> getTransformer() {
            return bytes -> bytes;
        }

        public TemporalAmount getTimeToLive() {
            return Duration.ofSeconds(Instant.MAX.getEpochSecond());
        }
    };

    /**
     * fernetDecrypt
     *
     * @param keyPath       key
     * @param encryptedPath encrypted
     * @return byte[]
     */
    public static byte[] fernetDecrypt(String keyPath, String encryptedPath) {
        Assert.isTrue(FileUtil.exist(keyPath), "key file is not exist");
        Assert.isTrue(FileUtil.exist(encryptedPath), "encrypt file is not exist");
        return fernetDecrypt(FileUtil.readString(keyPath, StandardCharsets.UTF_8), FileUtil.readBytes(encryptedPath));
    }

    /**
     * fernetDecrypt
     *
     * @param keyBody        keyBody
     * @param encryptedBytes encryptedBytes
     * @return byte[]
     */
    public static byte[] fernetDecrypt(String keyBody, byte[] encryptedBytes) {
        Assert.notNull(keyBody, "key is not exist");
        Assert.notNull(encryptedBytes, "encrypt is not exist");
        final Token token = Token.fromBytes(Base64.getUrlDecoder().decode(encryptedBytes));
        return token.validateAndDecrypt(new Key(keyBody), byteValidator);
    }


    public static void main(String[] args) {
        final byte[] payload = FernetUtils.fernetDecrypt("F:\\Documents\\WeChat Files\\westboynet\\FileStorage\\File\\2024-08\\filekey.key", "F:\\Documents\\WeChat Files\\westboynet\\FileStorage\\File\\2024-08\\encrypted.csv");
        System.out.println(new String(payload, StandardCharsets.UTF_8));
    }

}
