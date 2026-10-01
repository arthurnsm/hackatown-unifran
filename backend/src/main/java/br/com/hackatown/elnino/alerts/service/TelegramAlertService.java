package br.com.hackatown.elnino.alerts.service;

import br.com.hackatown.elnino.alerts.model.AlertRequest;
import br.com.hackatown.elnino.alerts.model.AlertResponse;

public class TelegramAlertService {
    private final TelegramBotClient telegramBotClient;

    public TelegramAlertService(TelegramBotClient telegramBotClient) {
        this.telegramBotClient = telegramBotClient;
    }

    public AlertResponse prepareFloodRiskAlert(AlertRequest request) {
        validate(request);
        String message = String.format(
                "%s ALERT: flood risk in %s. Avoid flooded areas and seek a safe location.",
                request.getRiskLevel().toUpperCase(), request.getLocation());
        String chatId = validateChatId(request.getChatId());
        String messageId = telegramBotClient.send(chatId, message);
        return new AlertResponse(chatId, message, messageId);
    }

    private void validate(AlertRequest request) {
        if (request == null) throw new IllegalArgumentException("Alert request is required");
        if (request.getLocation() == null || request.getLocation().isBlank()) {
            throw new IllegalArgumentException("Location is required");
        }
        if (request.getRiskLevel() == null || request.getRiskLevel().isBlank()) {
            throw new IllegalArgumentException("Risk level is required");
        }
    }

    private String validateChatId(String chatId) {
        if (chatId == null || chatId.isBlank()) throw new IllegalArgumentException("Telegram chat ID is required");
        return chatId;
    }
}
