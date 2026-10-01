package core.hackatown.elnino.model;

import java.time.LocalDate;

public record DailyWeather(LocalDate date, double meanTemperature, double precipitation) {
}
