package org.opentripplanner.updater.environmental.updaters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.opentripplanner.updater.environmental.EnvironmentalStationAir;
import org.opentripplanner.updater.environmental.EnvironmentalStationMeasurement;
import org.opentripplanner.updater.environmental.EnvironmentalStation;
import org.opentripplanner.updater.environmental.ParameterNoise;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.opentripplanner.updater.environmental.ParameterNoise.*;

public class MedioAmbienteMadridNoiseDataSourceTest {

    private MedioAmbienteMadridNoiseDataSource medioAmbienteMadridNoiseDataSource = new MedioAmbienteMadridNoiseDataSource();

    private String[] stationValues = new String[]{"1", "Pº Recoletos", "Frente calle Almirante", "3º41'27'' Oº", "40º25'24'' N", "648", "07/03/2011"};

    private String[] measurementValues = new String[]{"001", "2017", "011", "09", "T", "067.3", "073.8", "071.3", "063.4", "057.1", "053.0"};

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void shouldMakeStation() throws Exception {
        final EnvironmentalStation environmentalStationNoise = medioAmbienteMadridNoiseDataSource.makeStation(stationValues);
        assertThat(environmentalStationNoise.getId()).isEqualTo("1");
        assertThat(environmentalStationNoise.getLocation()).isEqualTo("Pº Recoletos Frente calle Almirante");
        assertThat(environmentalStationNoise.getCity()).isEqualTo(MedioAmbienteMadridNoiseDataSource.STATION_CITY);
        assertThat(environmentalStationNoise.getCountry()).isEqualTo(MedioAmbienteMadridNoiseDataSource.STATION_COUNTRY);
        assertThat(environmentalStationNoise.getX()).isEqualTo(-3.6908333333333334);
        assertThat(environmentalStationNoise.getY()).isEqualTo(40.42333333333333);
    }

    @Test
    public void addStationMeasurements() throws Exception {
        final EnvironmentalStation environmentalStationNoise = medioAmbienteMadridNoiseDataSource.makeStation(stationValues);
        medioAmbienteMadridNoiseDataSource.addStationMeasurements(environmentalStationNoise, measurementValues);
        checkMeasurement(environmentalStationNoise.getMeasurements().get(0), LAeq.name(), 67.3, LocalDateTime.of(2017, 11, 9, 21, 0));
        checkMeasurement(environmentalStationNoise.getMeasurements().get(1), L01.name(), 73.8, LocalDateTime.of(2017, 11, 9, 21, 0));
        checkMeasurement(environmentalStationNoise.getMeasurements().get(2), L10.name(), 71.3, LocalDateTime.of(2017, 11, 9, 21, 0));
        checkMeasurement(environmentalStationNoise.getMeasurements().get(3), L50.name(), 63.4, LocalDateTime.of(2017, 11, 9, 21, 0));
        checkMeasurement(environmentalStationNoise.getMeasurements().get(4), L90.name(), 57.1, LocalDateTime.of(2017, 11, 9, 21, 0));
        checkMeasurement(environmentalStationNoise.getMeasurements().get(5), L99.name(), 53.0, LocalDateTime.of(2017, 11, 9, 21, 0));
    }

    @Test
    public void shouldDownloadBothFileAndMakeStations() throws Exception {
        String jsonConfigOpenAq = "{" +
                "\"type\": \"environmental-updater\"," +
                "\"frequencySec\": -1," +
                "\"sourceType\": \"medio-ambiente-madrid\"," +
                "\"urlStationsPosition\": \"http://datos.madrid.es/egob/catalogo/211346-1-estaciones-acusticas.csv\"," +
                "\"urlStationsData\": \"http://www.mambiente.munimadrid.es/opendata/ruido.txt\"" +
                "}";
        JsonNode jsonConfig = objectMapper.readTree(jsonConfigOpenAq);
        medioAmbienteMadridNoiseDataSource.configure(null, jsonConfig);
        medioAmbienteMadridNoiseDataSource.update();
        final List<EnvironmentalStation> stations = medioAmbienteMadridNoiseDataSource.getStations();
        assertThat(stations).isNotEmpty();
        assertThat(stations.stream().anyMatch(station ->
                !station.getMeasurements().isEmpty()));
        assertThat(stations.stream().allMatch(station ->
                station.getMeasurements().stream().allMatch(environmentalStationMeasurement ->
                    environmentalStationMeasurement.getValue() != null))).isTrue();
    }

    private void checkMeasurement(EnvironmentalStationMeasurement environmentalStationMeasurement, String parameterNoise, Double value, LocalDateTime dateTime) {
        assertThat(environmentalStationMeasurement.getParameter()).isEqualTo(parameterNoise);
        assertThat(environmentalStationMeasurement.getValue()).isEqualTo(value);
        assertThat(environmentalStationMeasurement.getSourceName()).isEqualTo(MedioAmbienteMadridNoiseDataSource.STATION_SOURCE_NAME);
        assertThat(environmentalStationMeasurement.getLastUpdated()).isEqualTo(dateTime);
        assertThat(environmentalStationMeasurement.getUnit()).isEqualTo(MedioAmbienteMadridNoiseDataSource.STATION_UNIT);
    }

}