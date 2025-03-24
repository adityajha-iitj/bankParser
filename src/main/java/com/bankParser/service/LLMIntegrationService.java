package com.bankParser.service;

import com.bankParser.model.BankStatementDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class LLMIntegrationService {
    @Value("${OPENAI_API_KEY}")
    private String openaiApiKey;


    private final WebClient webClient;

    public LLMIntegrationService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader("Authorization", "Bearer " + openaiApiKey)
                .build();
    }

    public BankStatementDTO extractBankStatementDetails(String extractedText) {
        String prompt = "Extract structured JSON with these bank statement details: " +
                "Full Name, Email, Opening Balance, Closing Balance, Account Number (last 4 digits)\n\n" +
                "Bank Statement Text:\n" + extractedText;

        String llmResponse = webClient.post()
                .uri("/completions")
                .bodyValue(Map.of(
                        "model", "gpt-3.5-turbo",
                        "prompt", prompt,
                        "max_tokens", 250
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> (String) ((Map<String, Object>) ((Map<String, Object>) response.get("choices")).get(0)).get("text"))
                .block();

        return parseResponse(llmResponse);
    }

    private BankStatementDTO parseResponse(String llmResponse) {
        BankStatementDTO dto = new BankStatementDTO();

        Pattern namePattern = Pattern.compile("\"Full Name\":\\s*\"([^\"]+)\"");
        Pattern emailPattern = Pattern.compile("\"Email\":\\s*\"([^\"]+)\"");
        Pattern openingBalancePattern = Pattern.compile("\"Opening Balance\":\\s*\"?([\\d.]+)\"?");
        Pattern closingBalancePattern = Pattern.compile("\"Closing Balance\":\\s*\"?([\\d.]+)\"?");
        Pattern accountNumPattern = Pattern.compile("\"Account Number\":\\s*\"?([\\d]{4})\"?");

        Matcher nameMatcher = namePattern.matcher(llmResponse);
        Matcher emailMatcher = emailPattern.matcher(llmResponse);
        Matcher openingBalanceMatcher = openingBalancePattern.matcher(llmResponse);
        Matcher closingBalanceMatcher = closingBalancePattern.matcher(llmResponse);
        Matcher accountNumMatcher = accountNumPattern.matcher(llmResponse);

        if (nameMatcher.find()) dto.setName(nameMatcher.group(1));
        if (emailMatcher.find()) dto.setEmail(emailMatcher.group(1));
        if (openingBalanceMatcher.find()) dto.setOpeningBalance(new BigDecimal(openingBalanceMatcher.group(1)));
        if (closingBalanceMatcher.find()) dto.setClosingBalance(new BigDecimal(closingBalanceMatcher.group(1)));
        if (accountNumMatcher.find()) dto.setAccountNumberLastFourDigits(accountNumMatcher.group(1));

        return dto;
    }

}
