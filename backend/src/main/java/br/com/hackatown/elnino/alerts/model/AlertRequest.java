package br.com.hackatown.elnino.alerts.model;

public class AlertRequest {
    private final String chatId;
    private final String location;
    private final String riskLevel;

    public AlertRequest(String chatId, String location, String riskLevel) {
        this.chatId = chatId;
        this.location = location;
        this.riskLevel = riskLevel;
    }

    public String getChatId() { return chatId; }
    public String getLocation() { return location; }
    public String getRiskLevel() { return riskLevel; }
}
