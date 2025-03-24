package com.bankParser.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class PasswordGenerator {

    private static final String CHAR_LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHAR_UPPERCASE = CHAR_LOWERCASE.toUpperCase();
    private static final String DIGIT = "0123456789";
    private static final String PUNCTUATION = "!@#$%^&*()_+-=[]?";
    private static final String ALL_CHARS = CHAR_LOWERCASE + CHAR_UPPERCASE + DIGIT + PUNCTUATION;

    public String generatePassword(String firstName, LocalDate dateOfBirth) {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        // Use first name and DOB as seed
        if (firstName != null && dateOfBirth != null) {
            password.append(firstName.substring(0, Math.min(3, firstName.length())));
            password.append(dateOfBirth.format(DateTimeFormatter.BASIC_ISO_DATE.ofPattern("MMdd")));
        }

        // Add random characters to meet complexity
        while (password.length() < 12) {
            password.append(ALL_CHARS.charAt(random.nextInt(ALL_CHARS.length())));
        }

        return password.toString();
    }
}
