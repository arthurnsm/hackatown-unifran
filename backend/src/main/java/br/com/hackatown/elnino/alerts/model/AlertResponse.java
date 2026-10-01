package br.com.hackatown.elnino.alerts.model;

public class AlertResponse {
    private final String chatId;
    private final String message;
    private final String messageId;

    public AlertResponse(String chatId, String message, String messageId) {
        this.chatId = chatId;
        this.message = message;
        this.messageId = messageId;
    }

    public String getChatId() { return chatId; }
    public String getMessage() { return message; }
    public String getMessageId() { return messageId; }
}
