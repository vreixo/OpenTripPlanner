package org.opentripplanner.updater.environmental.updaters;

import org.opentripplanner.updater.environmental.*;
import org.opentripplanner.util.NonLocalizedString;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import static org.opentripplanner.updater.environmental.ParameterNoise.*;

/**
 * Fetch Bike Rental JSON feeds and pass each record on to the specific rental subclass
 *
 * @see StationsDataSource
 */
public class MedioAmbienteMadridNoiseDataSource extends CustomCsvDataSource
        implements StationsDataSource {

    public static final String STATION_SOURCE_NAME = "Ayuntamiento de Madrid";

    public static final String STATION_UNIT = "db";

    public static final String STATION_CITY = "Madrid";

    public static final String STATION_COUNTRY = "Spain";

    @Override
    public EnvironmentalStationNoise makeStation(String[] lineValues) {
        return EnvironmentalStationNoise.builder().id(lineValues[0])
                .location(new NonLocalizedString(lineValues[1] + " " + lineValues[2]))
                .city(new NonLocalizedString(STATION_CITY))
                .country(new NonLocalizedString(STATION_COUNTRY))
                .x(getCoordinate(lineValues[3]))
                .y(getCoordinate(lineValues[4]))
                .measurements(new ArrayList<>())
                .build();
    }

    private double getCoordinate(String coordinate) {
        final String coordinateWithoutCommas = coordinate.replace(',', '.');
        StringTokenizer stringTokenizer = new StringTokenizer(coordinateWithoutCommas, "º°'\" ");
        double grades = Double.parseDouble(stringTokenizer.nextToken());
        double minutes = Double.parseDouble(stringTokenizer.nextToken());
        double seconds = Double.parseDouble(stringTokenizer.nextToken());
        double factor = 1;
        if (stringTokenizer.hasMoreTokens()) {
            String direction = stringTokenizer.nextToken();
            switch (direction) {
                case "O":
                    factor = -1;
                    break;
                case "S":
                    factor = -1;
                    break;
            }
        }
        return (grades + (minutes * 60 + seconds) / 3600) * factor;
    }

    @Override
    public void addStationMeasurements(EnvironmentalStation environmentalStationNoise,
                                       String[] lineValues) throws IOException {
        LocalDate lastUpdated = LocalDate
                .of(Integer.parseInt(lineValues[1]), Integer.parseInt(lineValues[2]),
                        Integer.parseInt(lineValues[3]));
        LocalDateTime lastUpdatedTime = setTime(lineValues[4], lastUpdated);
        EnvironmentalStationMeasurement environmentalStationMeasurementAeq = getStationMeasurement(lastUpdatedTime, LAeq.name(), lineValues[5]);
        EnvironmentalStationMeasurement environmentalStationMeasurementL01 = getStationMeasurement(lastUpdatedTime, L01.name(), lineValues[6]);
        EnvironmentalStationMeasurement environmentalStationMeasurementL10 = getStationMeasurement(lastUpdatedTime, L10.name(), lineValues[7]);
        EnvironmentalStationMeasurement environmentalStationMeasurementL50 = getStationMeasurement(lastUpdatedTime, L50.name(), lineValues[8]);
        EnvironmentalStationMeasurement environmentalStationMeasurementL90 = getStationMeasurement(lastUpdatedTime, L90.name(), lineValues[9]);
        EnvironmentalStationMeasurement environmentalStationMeasurementL99 = getStationMeasurement(lastUpdatedTime, L99.name(), lineValues[10]);
        final List<EnvironmentalStationMeasurement> environmentalStationMeasurements =
                Arrays.asList(environmentalStationMeasurementAeq,
                        environmentalStationMeasurementL01,
                        environmentalStationMeasurementL10,
                        environmentalStationMeasurementL50,
                        environmentalStationMeasurementL90,
                        environmentalStationMeasurementL99);
        environmentalStationNoise.addMeasurements(environmentalStationMeasurements);
    }

    private LocalDateTime setTime(String momentOfDay, LocalDate localDate) throws IOException {
        switch (momentOfDay) {
            case "D":
                return localDate.atTime(13, 0);
            case "T":
                return localDate.atTime(21, 0);
            case "N":
                return localDate.atTime(3, 0);
        }
        return localDate.atTime(12, 0);
    }

    private EnvironmentalStationMeasurement getStationMeasurement(LocalDateTime lastUpdated,
                                                                  String parameter, String value) throws IOException {
        return EnvironmentalStationMeasurement.builder()
                .lastUpdated(lastUpdated)
                .sourceName(STATION_SOURCE_NAME)
                .unit(STATION_UNIT)
                .parameter(parameter)
                .value(Double.parseDouble(value))
                .build();
    }

}
