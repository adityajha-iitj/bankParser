package com.bankParser.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankStatementDTO {
    private String name;
    private String email;
    private BigDecimal openingBalance;
    private BigDecimal closingBalance;
    private String generatedPassword;
    private LocalDate statementDate;
    private String accountNumberLastFourDigits;
}
