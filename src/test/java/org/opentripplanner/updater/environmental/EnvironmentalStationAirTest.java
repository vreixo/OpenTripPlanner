package org.opentripplanner.updater.environmental;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.opentripplanner.routing.constraints.EnvironmentalFactorMeasurement;
import org.opentripplanner.routing.constraints.EnvironmentalFactorType;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

public class EnvironmentalStationAirTest {

    private EnvironmentalStationAir environmentalStationAir;

    @Before
    public void setUp() {
        EnvironmentalStationMeasurement environmentalStationAirMeasurementCo = EnvironmentalStationMeasurement.builder()
                .parameter(ParameterAir.co.name())
                .value(500.0)
                .build();
        EnvironmentalStationMeasurement environmentalStationAirMeasurementSo2 = EnvironmentalStationMeasurement.builder()
                .parameter(ParameterAir.so2.name())
                .value(2.0)
                .build();
        EnvironmentalStationMeasurement environmentalStationAirMeasurementO3 = EnvironmentalStationMeasurement.builder()
                .parameter(ParameterAir.o3.name())
                .value(4.0)
                .build();
        EnvironmentalStationMeasurement environmentalStationAirMeasurementNo2 = EnvironmentalStationMeasurement.builder()
                .parameter(ParameterAir.no2.name())
                .value(47.0)
                .build();
        EnvironmentalStationMeasurement environmentalStationAirMeasurementPm10 = EnvironmentalStationMeasurement.builder()
                .parameter(ParameterAir.pm10.name())
                .value(25.0)
                .build();
        EnvironmentalStationMeasurement environmentalStationAirMeasurementPm25 = EnvironmentalStationMeasurement.builder()
                .parameter(ParameterAir.pm25.name())
                .value(17.0)
                .build();
        environmentalStationAir = EnvironmentalStationAir.builder()
                .measurements(Arrays.asList(environmentalStationAirMeasurementCo,
                        environmentalStationAirMeasurementSo2,
                        environmentalStationAirMeasurementO3,
                        environmentalStationAirMeasurementNo2,
                        environmentalStationAirMeasurementPm10,
                        environmentalStationAirMeasurementPm25))
                .build();
    }

    @Test
    public void calculateEnvironmentalFactorsMeasurements() throws Exception {
        final List<EnvironmentalFactorMeasurement> environmentalFactorMeasurements = environmentalStationAir.calculateEnvironmentalFactorsMeasurements();
        final EnvironmentalFactorMeasurement environmentalFactorMeasurementPollution = environmentalFactorMeasurements.get(0);
        assertThat(environmentalFactorMeasurementPollution.getType()).isEqualTo(EnvironmentalFactorType.POLLUTION);
        assertThat(environmentalFactorMeasurementPollution.getMeasurement()).isEqualTo(3337.334933975);
        final EnvironmentalFactorMeasurement environmentalFactorMeasurementAllergic = environmentalFactorMeasurements.get(1);
        assertThat(environmentalFactorMeasurementAllergic.getType()).isEqualTo(EnvironmentalFactorType.ALLERGIC);
        assertThat(environmentalFactorMeasurementAllergic.getMeasurement()).isEqualTo(environmentalStationAir.getMeasurements().get(5).getValue());
    }

}