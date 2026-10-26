package kz.amixady.urlshortener.link.security;

import kz.amixady.urlshortener.link.exception.InvalidTokenException;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

@Component
public class TokenService {

    private static final String TOKEN_PREFIX = "mt_";
    private static final int TOKEN_LENGTH_BYTES = 32;
    private static final String HASH_ALGORITHM = "SHA-256";

    private static final Base64.Encoder TOKEN_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final HexFormat HEX = HexFormat.of();

    private final SecureRandom random = new SecureRandom();

    public String generate() {
        return TOKEN_PREFIX + TOKEN_ENCODER.encodeToString(randomBytes());
    }

    public String hash(String token) {
        return HEX.formatHex(sha256(token));
    }

    public void verify(String token, String storedHash) {
        if (!matches(token, storedHash)) {
            throw new InvalidTokenException();
        }
    }

    private boolean matches(String token, String storedHash) {
        if (token == null || token.isBlank() || storedHash == null) {
            return false;
        }
        return MessageDigest.isEqual(sha256(token), HEX.parseHex(storedHash));
    }

    private byte[] randomBytes() {
        byte[] bytes = new byte[TOKEN_LENGTH_BYTES];
        random.nextBytes(bytes);
        return bytes;
    }

    private static byte[] sha256(String value) {
        return newDigest().digest(value.getBytes(StandardCharsets.UTF_8));
    }

    private static MessageDigest newDigest() {
        try {
            return MessageDigest.getInstance(HASH_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(HASH_ALGORITHM + " is not available", e);
        }
    }
}