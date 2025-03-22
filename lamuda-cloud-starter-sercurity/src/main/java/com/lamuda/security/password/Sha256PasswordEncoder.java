package com.lamuda.security.password;

import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.codec.Utf8;
import org.springframework.security.crypto.keygen.BytesKeyGenerator;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.MessageDigest;

import static org.springframework.security.crypto.util.EncodingUtils.concatenate;
import static org.springframework.security.crypto.util.EncodingUtils.subArray;

/**
 * This {@link PasswordEncoder} is provided for legacy purposes only and is not considered
 * secure.
 *
 * @author jpjoo
 */
public final class Sha256PasswordEncoder implements PasswordEncoder {

    private final Digester digester;

    private final byte[] secret;

    private final BytesKeyGenerator saltGenerator;

    /**
     * Constructs a standard password encoder with no additional secret value.
     */
    public Sha256PasswordEncoder() {
        this("");
    }

    /**
     * Constructs a standard password encoder with a secret value which is also included
     * in the password hash.
     *
     * @param secret the secret key used in the encoding process (should not be shared)
     */
    public Sha256PasswordEncoder(CharSequence secret) {
        this("SHA-256", secret);
    }

    @Override
    public String encode(CharSequence rawPassword) {
        return encode(rawPassword, saltGenerator.generateKey());
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        byte[] digested = decode(encodedPassword);
        byte[] salt = subArray(digested, 0, saltGenerator.getKeyLength());
        return MessageDigest.isEqual(digested, digest(rawPassword, salt));
    }

    // internal helpers

    private Sha256PasswordEncoder(String algorithm, CharSequence secret) {
        this.digester = new Digester(algorithm, DEFAULT_ITERATIONS);
        this.secret = Utf8.encode(secret);
        this.saltGenerator = KeyGenerators.secureRandom();
    }

    private String encode(CharSequence rawPassword, byte[] salt) {
        byte[] digest = digest(rawPassword, salt);
        return new String(Hex.encode(digest));
    }

    private byte[] digest(CharSequence rawPassword, byte[] salt) {
        byte[] digest = digester.digest(concatenate(salt, secret,
                Utf8.encode(rawPassword)));
        return concatenate(salt, digest);
    }

    private byte[] decode(CharSequence encodedPassword) {
        return Hex.decode(encodedPassword);
    }

    private static final int DEFAULT_ITERATIONS = 1024;

}
