package br.com.hackatown.elnino.alerts.model;

public class AlertResponse {
    private final String phoneNumber;
    private final String message;
    private final String messageSid;

    public AlertResponse(String phoneNumber, String message, String messageSid) {
        this.phoneNumber = phoneNumber;
        this.message = message;
        this.messageSid = messageSid;
    }

    public String getPhoneNumber() { return phoneNumber; }
    public String getMessage() { return message; }
    public String getMessageSid() { return messageSid; }
}
