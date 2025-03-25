package com.bankParser.service;

import com.bankParser.model.BankStatementDTO;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class LLMIntegrationService {
    private static final Logger logger = LoggerFactory.getLogger(LLMIntegrationService.class);

    private final WebClient webClient;
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    public LLMIntegrationService(WebClient.Builder webClientBuilder) {
        String geminiApiKey = System.getenv("GEMINI_API_KEY");
        if (geminiApiKey == null || geminiApiKey.isEmpty()) {
            throw new IllegalStateException("Missing GEMINI_API_KEY in environment variables.");
        }

        this.webClient = webClientBuilder
                .baseUrl(GEMINI_API_URL + "?key=" + geminiApiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public BankStatementDTO extractBankStatementDetails(String extractedText) {
        String prompt = "Extract structured JSON with these bank statement details: " +
                "Full Name, Email, Opening Balance, Closing Balance, Account Number (last 4 digits)\n\n" +
                "Bank Statement Text:\n" + extractedText;

        try {
            String llmResponse = webClient.post()
                    .bodyValue(Map.of(
                            "contents", new Object[]{
                                    Map.of("parts", new Object[]{
                                            Map.of("text", prompt)
                                    })
                            }
                    ))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .map(response -> {
                        if (response.containsKey("candidates")) {
                            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
                            if (!candidates.isEmpty()) {
                                Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                                List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                                if (!parts.isEmpty()) {
                                    return (String) parts.get(0).get("text");
                                }
                            }
                        }
                        return null;
                    })
                    .block();

            if (llmResponse == null || llmResponse.isEmpty()) {
                logger.error("Gemini API returned an empty response.");
                throw new RuntimeException("API issue: Empty response from Gemini API.");
            }

            return parseResponse(llmResponse);

        } catch (WebClientResponseException e) {
            logger.error("Gemini API request failed: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            System.out.println("API Issue: Unable to fetch data from Gemini API. Please check the logs.");
            throw new RuntimeException("API issue: Failed to get response from Gemini API.", e);
        } catch (ClassCastException e) {
            logger.error("API response format error: {}", e.getMessage());
            System.out.println("API Issue: Unexpected response structure from Gemini API.");
            throw new RuntimeException("API issue: Incorrect response format from Gemini API.", e);
        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage());
            System.out.println("API Issue: Unexpected error while calling Gemini API.");
            throw new RuntimeException("Unexpected error while calling Gemini API.", e);
        }
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
