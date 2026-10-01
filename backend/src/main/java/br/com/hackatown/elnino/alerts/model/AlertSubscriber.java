package br.com.hackatown.elnino.alerts.model;
import core.hackatown.elnino.model.Location;
public record AlertSubscriber(String chatId, String city, String state, double latitude, double longitude) {
    public Location toLocation() { return new Location(chatId, city, state, latitude, longitude); }
}
