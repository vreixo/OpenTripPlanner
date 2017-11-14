package org.opentripplanner.updater.environmental;

import java.time.LocalDateTime;

public class EnvironmentalStationMeasurement {

    protected String parameter;

    protected Double value;

    protected LocalDateTime lastUpdated;

    protected String unit;

    protected String sourceName;

    public String getParameter() {
        return parameter;
    }

    public Double getValue() {
        return value;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public String getUnit() {
        return unit;
    }

    public String getSourceName() {
        return sourceName;
    }

    public EnvironmentalStationMeasurement(String parameter, Double value, LocalDateTime lastUpdated, String unit,
                                           String sourceName) {
        this.parameter = parameter;
        this.value = value;
        this.lastUpdated = lastUpdated;
        this.unit = unit;
        this.sourceName = sourceName;
    }

    public static MeasurementBuilder builder() {
        return new MeasurementBuilder();
    }

    public static class MeasurementBuilder {

        private String parameter;

        private Double value;

        private LocalDateTime lastUpdated;

        private String unit;

        private String sourceName;

        public MeasurementBuilder parameter(String parameter) {
            this.parameter = parameter;
            return this;
        }

        public MeasurementBuilder value(Double value) {
            this.value = value;
            return this;
        }

        public MeasurementBuilder lastUpdated(LocalDateTime lastUpdated) {
            this.lastUpdated = lastUpdated;
            return this;
        }

        public MeasurementBuilder unit(String unit) {
            this.unit = unit;
            return this;
        }

        public MeasurementBuilder sourceName(String sourceName) {
            this.sourceName = sourceName;
            return this;
        }

        public EnvironmentalStationMeasurement build() {
            return new EnvironmentalStationMeasurement(parameter, value, lastUpdated, unit, sourceName);
        }

    }

}
