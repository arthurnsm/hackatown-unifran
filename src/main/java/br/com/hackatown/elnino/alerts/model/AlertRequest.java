package br.com.hackatown.elnino.alerts.model;

public class AlertRequest {
    private final String phoneNumber;
    private final String location;
    private final String riskLevel;

    public AlertRequest(String phoneNumber, String location, String riskLevel) {
        this.phoneNumber = phoneNumber;
        this.location = location;
        this.riskLevel = riskLevel;
    }

    public String getPhoneNumber() { return phoneNumber; }
    public String getLocation() { return location; }
    public String getRiskLevel() { return riskLevel; }
}
