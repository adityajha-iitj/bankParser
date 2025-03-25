package com.bankParser.util;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class PasswordGenerator {

    public String generatePassword(String firstName, LocalDate dateOfBirth, String accountType) {

        // Validate inputs
        if (firstName == null || dateOfBirth == null) {
            throw new IllegalArgumentException("First name, date of birth, and account type are required");
        }

        String dobString = dateOfBirth.format(DateTimeFormatter.BASIC_ISO_DATE);

        String firstNamePart = firstName.substring(0, Math.min(4, firstName.length())).toLowerCase();

        //Account type suffix
        if (accountType == null || accountType.isEmpty()) {
            accountType = "other";
        }

        String accountSuffix = switch (accountType.toLowerCase()) {
            case "savings" -> "sb";
            case "current" -> "ca";
            case "regular" -> "rg";
            case "other" -> "ot";
            default -> "ac";
        };

        // Combine all parts
        return dobString + firstNamePart + accountSuffix;
    }
}
