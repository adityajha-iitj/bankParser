package com.bankParser.service;

import com.bankParser.model.BankStatementDTO;
import com.bankParser.util.PasswordGenerator;
import com.bankParser.util.PdfExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PDFParserService {

    private final PdfExtractor pdfExtractor;
    private final LLMIntegrationService llmService;
    private final PasswordGenerator passwordGenerator;

    public BankStatementDTO parseBankStatement(
            MultipartFile file,
            String firstName,
            LocalDate dateOfBirth
    ) throws IOException {
        // Extract text from PDF
        String extractedText = pdfExtractor.extractTextFromPDF(file);

        // Use LLM to extract structured details
        BankStatementDTO statementDetails = llmService.extractBankStatementDetails(extractedText);

        // Generate password if firstName and dateOfBirth provided
        if (firstName != null && dateOfBirth != null) {
            statementDetails.setGeneratedPassword(
                    passwordGenerator.generatePassword(firstName, dateOfBirth)
            );
        }

        return statementDetails;
    }
}
