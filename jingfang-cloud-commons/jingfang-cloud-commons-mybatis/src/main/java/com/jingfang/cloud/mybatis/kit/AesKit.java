package com.jingfang.cloud.mybatis.kit;

import cn.hutool.core.codec.Base64Decoder;
import cn.hutool.core.codec.Base64Encoder;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class AesKit {

    public static final AesEncoder aesEncoder = new AesEncoder();
    public static final AesDecoder aesDecoder = new AesDecoder();

    public static class AesEncoder {
        public String encryptForAes(String content, String aesKey) {
            try {
                byte[] src = content.getBytes(StandardCharsets.UTF_8);
                byte[] bytOut = encryptMode(src, aesKey);
                return base64encode(bytOut);

            } catch (Exception ignored) {
            }
            return null;
        }

        public byte[] encryptMode(byte[] src, String aesKey) {
            try {
                Cipher cip = Cipher.getInstance("AES");
                cip.init(Cipher.ENCRYPT_MODE, AesKit.getSecretKey(aesKey));
                return cip.doFinal(src);
            } catch (Exception ignored) {
            }
            return null;
        }

        public String base64encode(byte[] src) {
            if (src == null) {
                return null;
            }
            return Base64Encoder.encode(src);
        }
    }

    public static class AesDecoder {
        public String decryptForAesToStr(String base64Str, String aesKey) {
            String Result = null;
            try {
                byte[] dst = decryptForAes(base64Str, aesKey);
                if (null != dst) {
                    Result = new String(dst, StandardCharsets.UTF_8);
                }
            } catch (Exception ignored) {
            }
            return Result;

        }

        public byte[] decryptForAes(String base64Str, String aesKey) {
            try {
                byte[] src = base64decode(base64Str);
                return decryptMode(src, aesKey);
            } catch (Exception ignored) {
            }
            return null;

        }

        public byte[] decryptMode(byte[] src, String aesKey) {
            try {
                Cipher cip = Cipher.getInstance("AES");
                cip.init(Cipher.DECRYPT_MODE, AesKit.getSecretKey(aesKey));
                return cip.doFinal(src);
            } catch (Exception ignored) {
            }
            return null;

        }

        public byte[] base64decode(String s) {
            if (s == null)
                return null;
            try {
                return Base64Decoder.decode(s);
            } catch (Exception e) {
                return null;
            }
        }
    }

    private static byte[] getKeyByStr(String str) {
        byte[] bRet = new byte[str.length() / 2];
        for (int i = 0; i < str.length() / 2; i++) {
            int itg = 16 * getChrInt(str.charAt(2 * i)) + getChrInt(str.charAt(2 * i + 1));
            bRet[i] = (byte) itg;
        }
        return bRet;
    }

    private static int getChrInt(char chr) {
        switch (chr) {
            case '1':
                return 1;
            case '2':
                return 2;
            case '3':
                return 3;
            case '4':
                return 4;
            case '5':
                return 5;
            case '6':
                return 6;
            case '7':
                return 7;
            case '8':
                return 8;
            case '9':
                return 9;
            case 'A':
                return 10;
            case 'B':
                return 11;
            case 'C':
                return 12;
            case 'D':
                return 13;
            case 'E':
                return 14;
            case 'F':
                return 15;
            default:
                return 0;
        }
    }

    private static SecretKey getSecretKey(String aesKey) throws NoSuchAlgorithmException {
        byte[] keynote = AesKit.getKeyByStr(aesKey);
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        secureRandom.setSeed(keynote);
        KeyGenerator keygen = KeyGenerator.getInstance("AES");
        keygen.init(secureRandom);
        return keygen.generateKey();
    }
}
