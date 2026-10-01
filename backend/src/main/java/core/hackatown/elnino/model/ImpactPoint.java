package core.hackatown.elnino.model;

public record ImpactPoint(
        String id,
        String city,
        String state,
        double latitude,
        double longitude,
        double currentValue,
        double historicalValue,
        double anomaly,
        double intensity,
        String direction
) {
}
