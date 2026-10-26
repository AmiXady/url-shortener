package kz.amixady.urlshortener.link.service;


import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class CodeGenerator {
    private static final String ALPHABET = "23456789abcdefghjkmnpqrstuvwxyz";
    private static final int CODE_LENGTH = 7;
    private final SecureRandom random = new SecureRandom();

    public String generate() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);

        for (int i = 0; i < CODE_LENGTH; i++) {
            int randomIndex = random.nextInt(ALPHABET.length());
            code.append(ALPHABET.charAt(randomIndex));
        }

        return code.toString();
    }
}
