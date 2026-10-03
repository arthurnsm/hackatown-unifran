package core.hackatown.elnino.model;

import java.time.LocalDate;

public record ForecastDay(LocalDate date, double meanTemperature, double precipitationSum) {
}
