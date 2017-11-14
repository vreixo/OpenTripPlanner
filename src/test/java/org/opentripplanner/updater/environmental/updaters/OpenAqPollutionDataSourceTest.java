package org.opentripplanner.updater.environmental.updaters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.opentripplanner.routing.constraints.EnvironmentalFactorMeasurement;
import org.opentripplanner.routing.constraints.EnvironmentalFactorType;
import org.opentripplanner.updater.environmental.EnvironmentalStation;
import org.opentripplanner.updater.environmental.EnvironmentalStationMeasurement;
import org.opentripplanner.updater.environmental.ParameterAir;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.opentripplanner.updater.environmental.ParameterAir.*;

@RunWith(MockitoJUnitRunner.class)
public class OpenAqPollutionDataSourceTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    private final LocalDateTime lastMomentDayBeforeExtractionDate = LocalDateTime.of(2017, 11, 8, 23, 59, 59, 999999999);

    private OpenAqPollutionDataSource openAqPollutionDataSource = new OpenAqPollutionDataSource(lastMomentDayBeforeExtractionDate);

    private static final String STATION_STRING = "{\"location\":\"ES1422A\",\"city\":\"Madrid\",\"country\":\"ES\"," +
            "\"distance\":1057.9534670059588,\"measurements\":[{\"parameter\":\"co\",\"value\":200," +
            "\"lastUpdated\":\"2017-11-09T08:00:00.000Z\",\"unit\":\"µg/m³\",\"sourceName\":\"EEA Spain\"," +
            "\"averagingPeriod\":{\"value\":1,\"unit\":\"hours\"}},{\"parameter\":\"so2\",\"value\":7," +
            "\"lastUpdated\":\"2017-11-09T08:00:00.000Z\",\"unit\":\"µg/m³\",\"sourceName\":\"EEA Spain\"," +
            "\"averagingPeriod\":{\"value\":1,\"unit\":\"hours\"}},{\"parameter\":\"no2\",\"value\":61," +
            "\"lastUpdated\":\"2017-11-09T09:00:00.000Z\",\"unit\":\"µg/m³\",\"sourceName\":\"EEA Spain\"," +
            "\"averagingPeriod\":{\"value\":1,\"unit\":\"hours\"}},{\"parameter\":\"o3\",\"value\":7.480000019073486," +
            "\"lastUpdated\":\"2017-11-09T09:00:00.000Z\",\"unit\":\"µg/m³\",\"sourceName\":\"EEA Spain\"," +
            "\"averagingPeriod\":{\"value\":1,\"unit\":\"hours\"}}]," +
            "\"coordinates\":{\"latitude\":40.419166564941406,\"longitude\":-3.7033333778381348}}";

    private static final String API_TWO_RESULTS_STRING = "{\"meta\":{\"name\":\"openaq-api\",\"license\":\"CC BY 4.0\",\"website\":\"https://docs.openaq.org/\",\"page\":1,\"limit\":100,\"found\":2},\"results\":[{\"location\":\"ES1422A\",\"city\":\"Madrid\",\"country\":\"ES\",\"distance\":1057.9534670059588,\"measurements\":[{\"parameter\":\"co\",\"value\":200,\"lastUpdated\":\"2017-11-09T08:00:00.000Z\",\"unit\":\"µg/m³\",\"sourceName\":\"EEA Spain\",\"averagingPeriod\":{\"value\":1,\"unit\":\"hours\"}},{\"parameter\":\"so2\",\"value\":7,\"lastUpdated\":\"2017-11-09T08:00:00.000Z\",\"unit\":\"µg/m³\",\"sourceName\":\"EEA Spain\",\"averagingPeriod\":{\"value\":1,\"unit\":\"hours\"}},{\"parameter\":\"no2\",\"value\":61,\"lastUpdated\":\"2017-11-09T09:00:00.000Z\",\"unit\":\"µg/m³\",\"sourceName\":\"EEA Spain\",\"averagingPeriod\":{\"value\":1,\"unit\":\"hours\"}},{\"parameter\":\"o3\",\"value\":7.480000019073486,\"lastUpdated\":\"2017-11-09T09:00:00.000Z\",\"unit\":\"µg/m³\",\"sourceName\":\"EEA Spain\",\"averagingPeriod\":{\"value\":1,\"unit\":\"hours\"}}],\"coordinates\":{\"latitude\":40.419166564941406,\"longitude\":-3.7033333778381348}},{\"location\":\"PLAZA DEL CARMEN\",\"city\":\"Ayto Madrid\",\"country\":\"ES\",\"distance\":1057.9534670059588,\"measurements\":[{\"parameter\":\"no2\",\"value\":38,\"lastUpdated\":\"2017-07-20T18:00:00.000Z\",\"unit\":\"µg/m³\",\"sourceName\":\"EEA Spain\",\"averagingPeriod\":{\"value\":1,\"unit\":\"hours\"}},{\"parameter\":\"o3\",\"value\":97,\"lastUpdated\":\"2017-07-20T18:00:00.000Z\",\"unit\":\"µg/m³\",\"sourceName\":\"EEA Spain\",\"averagingPeriod\":{\"value\":1,\"unit\":\"hours\"}},{\"parameter\":\"co\",\"value\":300,\"lastUpdated\":\"2017-07-20T18:00:00.000Z\",\"unit\":\"µg/m³\",\"sourceName\":\"EEA Spain\",\"averagingPeriod\":{\"value\":1,\"unit\":\"hours\"}},{\"parameter\":\"so2\",\"value\":3,\"lastUpdated\":\"2017-07-20T18:00:00.000Z\",\"unit\":\"µg/m³\",\"sourceName\":\"EEA Spain\",\"averagingPeriod\":{\"value\":1,\"unit\":\"hours\"}}],\"coordinates\":{\"latitude\":40.419166564941406,\"longitude\":-3.7033333778381348}}]}";

    @Test
    public void shouldMakeStation() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(STATION_STRING);
        final EnvironmentalStation environmentalStationAir = openAqPollutionDataSource.makeStation(jsonNode);
        checkFirstStation(environmentalStationAir);
    }

    @Test
    public void shouldReturnListStationsAfterParseDataSecondWithoutMeasurementsBecauseTooOld() throws IOException {
        InputStream stream = new ByteArrayInputStream(API_TWO_RESULTS_STRING.getBytes(StandardCharsets.UTF_8.name()));
        openAqPollutionDataSource.parseData(stream);
        final List<EnvironmentalStation> stations = openAqPollutionDataSource.getStations();
        assertThat(stations).hasSize(2);
        final EnvironmentalStation environmentalStationAirFirst = stations.get(0);
        checkFirstStation(environmentalStationAirFirst);
        final EnvironmentalStation environmentalStationAirSecond = stations.get(1);
        checkSecondStation(environmentalStationAirSecond);
    }

    @Test
    public void shouldDownloadFileAndMakeStations() throws Exception {
        String jsonConfigOpenAq = "{" +
                "\"type\": \"environmental-updater\"," +
                "\"frequencySec\": -1," +
                "\"sourceType\": \"openaq\"," +
                "\"url\": \"https://api.openaq.org/v1/latest?coordinates=40.41,-3.70&radius=1300\"" +
                "}";
        JsonNode jsonConfig = objectMapper.readTree(jsonConfigOpenAq);
        openAqPollutionDataSource.configure(null, jsonConfig);
        openAqPollutionDataSource.update();
        final List<EnvironmentalStation> stations = openAqPollutionDataSource.getStations();
        List<EnvironmentalFactorMeasurement> factorMeasurements = stations.stream().map(environmentalStation -> {
            return !environmentalStation.getMeasurements().isEmpty() && !environmentalStation.calculateEnvironmentalFactorsMeasurements().isEmpty() ?
            environmentalStation.calculateEnvironmentalFactorsMeasurements().get(0)
            : null;
        }).collect(Collectors.toList());
        assertThat(stations).isNotEmpty();
        assertThat(stations.stream().noneMatch(station ->
                station.calculateEnvironmentalFactorsMeasurements().isEmpty()));
        assertThat(stations.stream().anyMatch(station -> {
            final List<EnvironmentalFactorMeasurement> environmentalFactorMeasurements = station.calculateEnvironmentalFactorsMeasurements();
            return environmentalFactorMeasurements.stream().anyMatch(
                    environmentalFactorMeasurement -> EnvironmentalFactorType.ALLERGIC == environmentalFactorMeasurement.getType());
        }));
        assertThat(stations.stream().anyMatch(station ->
                !station.getMeasurements().isEmpty()));
        assertThat(stations.stream().allMatch(station ->
                station.getMeasurements().stream().anyMatch(environmentalStationMeasurement ->
                        environmentalStationMeasurement.getValue() != null
                            && environmentalStationMeasurement.getLastUpdated().isAfter(LocalDate.now().minusDays(1).atTime(LocalTime.MAX)))));
    }

    @Test
    public void shouldNotThrowExceptionOnBadJsonButEmptyList() throws IOException {
        InputStream stream = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8.name()));
        openAqPollutionDataSource.parseData(stream);
        final List<EnvironmentalStation> stations = openAqPollutionDataSource.getStations();
        assertThat(stations).isEmpty();
    }

    @Test
    public void shouldNotThrowExceptionOnDifferentJson() throws IOException {
        InputStream stream = new FileInputStream(new File("src/test/resources/bike/bicimad.json"));
        openAqPollutionDataSource.parseData(stream);
        final List<EnvironmentalStation> stations = openAqPollutionDataSource.getStations();
        assertThat(stations).isEmpty();
    }

    @Test
    public void getStations() throws Exception {
    }

    @Test
    public void configure() throws Exception {
    }


    private void checkFirstStation(EnvironmentalStation environmentalStationAir) {
        checkStationBasicData(environmentalStationAir, "ES1422A", "Madrid", "ES"
                , -3.7033333778381348, 40.419166564941406, 4);
        checkMeasurement(environmentalStationAir.getMeasurements().get(0), co.name(), 200.0, LocalDateTime.of(2017, 11, 9, 8, 0));
        checkMeasurement(environmentalStationAir.getMeasurements().get(1), so2.name(), 7.0, LocalDateTime.of(2017, 11, 9, 8, 0));
        checkMeasurement(environmentalStationAir.getMeasurements().get(2), no2.name(), 61.0, LocalDateTime.of(2017, 11, 9, 9, 0));
        checkMeasurement(environmentalStationAir.getMeasurements().get(3), o3.name(), 7.480000019073486, LocalDateTime.of(2017, 11, 9, 9, 0));
    }

    private void checkSecondStation(EnvironmentalStation environmentalStationAir) {
        checkStationBasicData(environmentalStationAir, "PLAZA DEL CARMEN", "Ayto Madrid", "ES"
                , -3.7033333778381348, 40.419166564941406, 0);
    }

    private void checkMeasurement(EnvironmentalStationMeasurement environmentalStationMeasurement, String parameterAir, Double value, LocalDateTime dateTime) {
        assertThat(environmentalStationMeasurement.getParameter()).isEqualTo(parameterAir);
        assertThat(environmentalStationMeasurement.getValue()).isEqualTo(value);
        assertThat(environmentalStationMeasurement.getSourceName()).isEqualTo("EEA Spain");
        assertThat(environmentalStationMeasurement.getLastUpdated()).isEqualTo(dateTime);
        assertThat(environmentalStationMeasurement.getUnit()).isEqualTo("µg/m³");
    }

    private void checkStationBasicData(EnvironmentalStation environmentalStationAir, String location, String city, String country, double x, double y, int numberOfMeasurements) {
        assertThat(environmentalStationAir.getId()).isNotBlank();
        assertThat(environmentalStationAir.getLocation()).isEqualTo(location);
        assertThat(environmentalStationAir.getCity()).isEqualTo(city);
        assertThat(environmentalStationAir.getCountry()).isEqualTo(country);
        assertThat(environmentalStationAir.getX()).isEqualTo(x);
        assertThat(environmentalStationAir.getY()).isEqualTo(y);
        assertThat(environmentalStationAir.getMeasurements()).hasSize(numberOfMeasurements);
    }

}
