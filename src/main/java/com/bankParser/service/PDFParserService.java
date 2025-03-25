package com.bankParser.service;

import com.bankParser.model.BankStatementDTO;
import com.bankParser.util.PasswordGenerator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PDFParserService {
    private static final Logger logger = LoggerFactory.getLogger(PDFParserService.class);

    private final LLMIntegrationService llmService;
    private final PasswordGenerator passwordGenerator;

    public BankStatementDTO parseBankStatement(
            MultipartFile file,
            String firstName,
            LocalDate dateOfBirth
    ) throws IOException {
        // Send PDF to LLM
        logger.info("Parsing bank statement: firstName={}, dateOfBirth={}", firstName, dateOfBirth);

        if (dateOfBirth == null) {
            logger.error("dateOfBirth is null in PDFParserService! Check if it's correctly passed from Controller.");
        }

        BankStatementDTO statementDetails = llmService.extractBankStatementDetails(file);

        // Generate password if firstName and dateOfBirth provided
        if (firstName != null && dateOfBirth != null) {
            if (statementDetails.getAccountType() == null || statementDetails.getAccountType().isEmpty()) {
                statementDetails.setAccountType("other"); // Default to "other" if missing
            }

            logger.info("Generating password with: firstName={}, dateOfBirth={}, accountType={}",
                    firstName, dateOfBirth, statementDetails.getAccountType());

            try {
                String generatedPassword = passwordGenerator.generatePassword(
                        firstName, dateOfBirth, statementDetails.getAccountType()
                );
                statementDetails.setGeneratedPassword(generatedPassword);
            } catch (Exception e) {
                logger.error("Error generating password", e);
                statementDetails.setGeneratedPassword("ERROR");
            }
        } else {
            logger.warn("Skipping password generation - missing firstName or dateOfBirth");
        }

        return statementDetails;
    }
}
