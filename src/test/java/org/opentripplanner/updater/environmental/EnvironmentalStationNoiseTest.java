package org.opentripplanner.updater.environmental;

import org.junit.Before;
import org.junit.Test;
import org.opentripplanner.routing.constraints.EnvironmentalFactorMeasurement;
import org.opentripplanner.routing.constraints.EnvironmentalFactorType;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class EnvironmentalStationNoiseTest {

    private EnvironmentalStationNoise environmentalStationNoise;

    @Before
    public void setUp() {
        EnvironmentalStationMeasurement environmentalStationNoiseMeasurementLAeq = EnvironmentalStationMeasurement.builder()
                .parameter(ParameterNoise.LAeq.name())
                .value(67.3)
                .build();
        EnvironmentalStationMeasurement environmentalStationNoiseMeasurementL01 = EnvironmentalStationMeasurement.builder()
                .parameter(ParameterNoise.L01.name())
                .value(73.8)
                .build();
        EnvironmentalStationMeasurement environmentalStationNoiseMeasurementL10 = EnvironmentalStationMeasurement.builder()
                .parameter(ParameterNoise.L10.name())
                .value(71.3)
                .build();
        EnvironmentalStationMeasurement environmentalStationNoiseMeasurementL50 = EnvironmentalStationMeasurement.builder()
                .parameter(ParameterNoise.L50.name())
                .value(63.4)
                .build();
        EnvironmentalStationMeasurement environmentalStationNoiseMeasurementL90 = EnvironmentalStationMeasurement.builder()
                .parameter(ParameterNoise.L90.name())
                .value(57.1)
                .build();
        EnvironmentalStationMeasurement environmentalStationNoiseMeasurementL99 = EnvironmentalStationMeasurement.builder()
                .parameter(ParameterNoise.L99.name())
                .value(53.0)
                .build();
        environmentalStationNoise = EnvironmentalStationNoise.builder()
                .measurements(Arrays.asList(environmentalStationNoiseMeasurementLAeq,
                        environmentalStationNoiseMeasurementL01,
                        environmentalStationNoiseMeasurementL10,
                        environmentalStationNoiseMeasurementL50,
                        environmentalStationNoiseMeasurementL90,
                        environmentalStationNoiseMeasurementL99))
                .build();
    }

    @Test
    public void calculateEnvironmentalFactorsMeasurements() throws Exception {
        final List<EnvironmentalFactorMeasurement> environmentalFactorMeasurements = environmentalStationNoise.calculateEnvironmentalFactorsMeasurements();
        final EnvironmentalFactorMeasurement environmentalFactorMeasurement = environmentalFactorMeasurements.get(0);
        assertThat(environmentalFactorMeasurement.getType()).isEqualTo(EnvironmentalFactorType.NOISE);
        assertThat(environmentalFactorMeasurement.getMeasurement()).isEqualTo((environmentalStationNoise.getMeasurements().get(0).getValue()
                + environmentalStationNoise.getMeasurements().get(1).getValue()
                + environmentalStationNoise.getMeasurements().get(2).getValue()
                + environmentalStationNoise.getMeasurements().get(3).getValue()
                + environmentalStationNoise.getMeasurements().get(4).getValue()
                + environmentalStationNoise.getMeasurements().get(5).getValue())
                / environmentalStationNoise.getMeasurements().size()
        );
    }

}