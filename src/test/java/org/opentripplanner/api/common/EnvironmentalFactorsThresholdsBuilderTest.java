package org.opentripplanner.api.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;
import org.opentripplanner.routing.constraints.EnvironmentalFactorThreshold;
import org.opentripplanner.routing.constraints.EnvironmentalFactorType;

public class EnvironmentalFactorsThresholdsBuilderTest {

    @Test
    public void shouldReturnEmptyListWhenStringIsNull() {
        List<EnvironmentalFactorThreshold> result = EnvironmentalFactorsThresholdsBuilder.build(null);

        assertThat(result).hasSize(0);
    }

    @Test
    public void shouldReturnEmptyListWhenStringIsEmpty() {
        List<EnvironmentalFactorThreshold> result = EnvironmentalFactorsThresholdsBuilder.build("");

        assertThat(result).hasSize(0);
    }

    @Test
    public void shouldReturnOneElementWithMaxAverage() {
        final String averagePollution = "ENVIRONMENTAL_POLLUTION_MAX_AVERAGE=10.0";

        List<EnvironmentalFactorThreshold> result = EnvironmentalFactorsThresholdsBuilder.build(averagePollution);

        final EnvironmentalFactorThreshold pollution = new EnvironmentalFactorThreshold(
                EnvironmentalFactorType.POLLUTION, 10.0, null);
        assertThat(result).hasSize(1);
        assertThat(result).contains(pollution);
    }

    @Test
    public void shouldReturnOneElementWithMaxPeak() {
        final String averagePollution = "ENVIRONMENTAL_POLLUTION_MAX_PEAK=10.0";

        List<EnvironmentalFactorThreshold> result = EnvironmentalFactorsThresholdsBuilder.build(averagePollution);

        assertThat(result).hasSize(1);
        final EnvironmentalFactorThreshold pollution = new EnvironmentalFactorThreshold(
                EnvironmentalFactorType.POLLUTION, null, 10.0);
        assertThat(result).contains(pollution);
    }

    @Test
    public void shouldReturnZeroElementsWhenStringDoesntContainEnviromentalFactor() {
        final String noEnviromental = "NO_ENVIRONMENTAL_POLLUTION_MAX_AVERAGE=10.0";

        List<EnvironmentalFactorThreshold> result = EnvironmentalFactorsThresholdsBuilder.build(noEnviromental);

        assertThat(result).hasSize(0);
    }

    @Test
    public void shouldReturnZeroElementsWhenStringDoesntContainKnownEnviromentalFactor() {
        final String unknownFactor = "ENVIRONMENTAL_GARBAGE_MAX_AVERAGE=10.0";

        List<EnvironmentalFactorThreshold> result = EnvironmentalFactorsThresholdsBuilder.build(unknownFactor);

        assertThat(result).hasSize(0);
    }

    @Test
    public void shouldReturnZeroElementsWhenStringDoesntContainKnownProperty() {
        final String unknownProperty = "ENVIRONMENTAL_POLLUTION_MAX_NUMBER=10.0";

        List<EnvironmentalFactorThreshold> result = EnvironmentalFactorsThresholdsBuilder.build(unknownProperty);

        assertThat(result).hasSize(0);
    }

    @Test
    public void shouldReturnOneElementWithBothProperties() {
        final String averagePollution = "ENVIRONMENTAL_POLLUTION_MAX_AVERAGE=5.0&ENVIRONMENTAL_POLLUTION_MAX_PEAK=10.0";

        List<EnvironmentalFactorThreshold> result = EnvironmentalFactorsThresholdsBuilder.build(averagePollution);

        assertThat(result).hasSize(1);
        final EnvironmentalFactorThreshold pollution = new EnvironmentalFactorThreshold(
                EnvironmentalFactorType.POLLUTION, 5.0, 10.0);
        assertThat(result).contains(pollution);
    }

    @Test
    public void shouldReturnListWithKnownEnvironmentalFactors() {
        String inputWithTwoWholeFactors = "ENVIRONMENTAL_POLLUTION_MAX_AVERAGE=5.0&ENVIRONMENTAL_POLLUTION_MAX_PEAK=15.0&"
                + "ENVIRONMENTAL_ALLERGIC_MAX_AVERAGE=10.0&"
                + "ENVIRONMENTAL_GARBAGE_MAX_AVERAGE=5.0&" + "ENVIRONMENTAL_NOISE_MAX_PEAK=7.0";

        List<EnvironmentalFactorThreshold> result = EnvironmentalFactorsThresholdsBuilder.build(inputWithTwoWholeFactors);

        assertThat(result).hasSize(3);
        final EnvironmentalFactorThreshold pollution = new EnvironmentalFactorThreshold(
                EnvironmentalFactorType.POLLUTION, 5.0, 15.0);
        final EnvironmentalFactorThreshold allergic = new EnvironmentalFactorThreshold(
                EnvironmentalFactorType.ALLERGIC, 10.0, null);
        final EnvironmentalFactorThreshold noise = new EnvironmentalFactorThreshold(
                EnvironmentalFactorType.NOISE, null, 7.0);
        assertThat(result).contains(pollution, allergic, noise);
    }

}
