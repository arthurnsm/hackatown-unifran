package core.hackatown.elnino.model;

import java.util.List;

public record ImpactResponse(
        String metric,
        String unit,
        String description,
        PeriodInfo comparison,
        String generatedAt,
        List<ImpactPoint> points
) {
    public record PeriodInfo(String event, String baseline) {
    }
}
