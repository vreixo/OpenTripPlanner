package org.opentripplanner.routing.constraints;

public class EnvironmentalFactorMeasurement {

        private EnvironmentalFactorType type;

        private Double measurement;

        public EnvironmentalFactorType getType() {
                return type;
        }

        public void setType(EnvironmentalFactorType type) {
                this.type = type;
        }

        public Double getMeasurement() {
                return measurement;
        }

        public void setMeasurement(Double measurement) {
                this.measurement = measurement;
        }
}
