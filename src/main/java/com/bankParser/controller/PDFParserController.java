package com.bankParser.controller;

import com.bankParser.model.BankStatementDTO;
import com.bankParser.service.PDFParserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/bank-statement")
public class PDFParserController {
    private static final Logger logger = LoggerFactory.getLogger(PDFParserController.class);
    private final PDFParserService parserService;

    @Autowired
    public PDFParserController(PDFParserService parserService) {
        this.parserService = parserService;
    }

    @PostMapping("/parse")
    public ResponseEntity<BankStatementDTO> parseStatement(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "firstName", required = false) String firstName,
            @RequestParam(value = "dateofBirth", required = false) String dateOfBirthString // Ensure same name as Postman request
    ) {
        logger.info("Received API call: firstName={}, dateofBirth={}", firstName, dateOfBirthString);

        LocalDate dateOfBirth = null;
        if (dateOfBirthString != null && !dateOfBirthString.trim().isEmpty()) {
            try {
                dateOfBirth = LocalDate.parse(dateOfBirthString, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                logger.info("Parsed dateOfBirth successfully: {}", dateOfBirth);
            } catch (Exception e) {
                logger.error("Invalid date format received: {}", dateOfBirthString, e);
                return ResponseEntity.badRequest().body(null);
            }
        } else {
            logger.warn("Received null or empty dateOfBirth");
        }

        try {
            BankStatementDTO statementDetails = parserService.parseBankStatement(file, firstName, dateOfBirth);
            return ResponseEntity.ok(statementDetails);
        } catch (IOException e) {
            logger.error("Error processing bank statement: ", e);
            return ResponseEntity.badRequest().build();
        }
    }
}
