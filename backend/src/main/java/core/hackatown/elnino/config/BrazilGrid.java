package core.hackatown.elnino.config;

import core.hackatown.elnino.model.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Grade de aproximadamente 59 amostras dentro do contorno brasileiro. */
public final class BrazilGrid {
    private static final double STEP = 3.5;

    private static final List<Coordinate> BORDER = List.of(
            new Coordinate(5, -60), new Coordinate(4, -51),
            new Coordinate(1, -50), new Coordinate(-2, -44),
            new Coordinate(-5, -35), new Coordinate(-10, -36),
            new Coordinate(-18, -39), new Coordinate(-23, -42),
            new Coordinate(-28, -48), new Coordinate(-32, -51),
            new Coordinate(-34, -53), new Coordinate(-32, -58),
            new Coordinate(-28, -58), new Coordinate(-24, -55),
            new Coordinate(-22, -58), new Coordinate(-18, -58),
            new Coordinate(-14, -60), new Coordinate(-11, -66),
            new Coordinate(-8, -73), new Coordinate(-4, -70),
            new Coordinate(-1, -67), new Coordinate(2, -64),
            new Coordinate(5, -60)
    );

    public static final List<Location> POINTS = createPoints();

    private BrazilGrid() {
    }

    private static List<Location> createPoints() {
        List<Location> points = new ArrayList<>();
        for (double latitude = -34; latitude <= 6; latitude += STEP) {
            for (double longitude = -74; longitude <= -34; longitude += STEP) {
                if (!isInsideBrazil(latitude, longitude)) continue;
                points.add(new Location(
                        "",
                        region(latitude, longitude),
                        String.format(Locale.forLanguageTag("pt-BR"), "%.1f°, %.1f°", latitude, longitude),
                        latitude,
                        longitude
                ));
            }
        }
        return List.copyOf(points);
    }

    private static boolean isInsideBrazil(double latitude, double longitude) {
        boolean inside = false;
        for (int i = 0, previous = BORDER.size() - 1; i < BORDER.size(); previous = i++) {
            Coordinate current = BORDER.get(i);
            Coordinate prior = BORDER.get(previous);
            boolean crosses = (current.latitude() > latitude) != (prior.latitude() > latitude);
            if (crosses) {
                double borderLongitude = (prior.longitude() - current.longitude())
                        * (latitude - current.latitude())
                        / (prior.latitude() - current.latitude())
                        + current.longitude();
                if (longitude < borderLongitude) inside = !inside;
            }
        }
        return inside;
    }

    private static String region(double latitude, double longitude) {
        if (latitude <= -23.5) return "Sul";
        if (longitude >= -52 && latitude <= -14) return "Sudeste";
        if (longitude >= -44) return "Nordeste";
        if (longitude >= -60 && latitude <= -8) return "Centro-Oeste";
        return "Norte";
    }

    private record Coordinate(double latitude, double longitude) {
    }
}
