package com.bankParser.controller;

import com.bankParser.model.BankStatementDTO;
import com.bankParser.service.PDFParserService;
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

@RestController
@RequestMapping("/api/bank-statement")
public class PDFParserController {

    private final PDFParserService parserService;

    @Autowired
    public PDFParserController(PDFParserService parserService) {
        this.parserService = parserService;
    }

    @PostMapping("/parse")
    public ResponseEntity<BankStatementDTO> parseStatement(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "firstName", required = false) String firstName,
            @RequestParam(value = "dateOfBirth", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateOfBirth
    ) {
        try {
            BankStatementDTO statementDetails = parserService.parseBankStatement(
                    file, firstName, dateOfBirth
            );
            return ResponseEntity.ok(statementDetails);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
