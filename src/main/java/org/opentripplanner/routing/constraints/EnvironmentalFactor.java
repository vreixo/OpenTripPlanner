package org.opentripplanner.routing.constraints;

public class EnvironmentalFactor {

        private EnvironmentalFactorType type;

        private Double accumulated;

        private Double peak;

        public EnvironmentalFactorType getType() {
                return type;
        }

        public void setType(EnvironmentalFactorType type) {
                this.type = type;
        }

        public Double getAccumulated() {
                return accumulated;
        }

        public void setAccumulated(Double accumulated) {
                this.accumulated = accumulated;
        }

        public Double getPeak() {
                return peak;
        }

        public void setPeak(Double peak) {
                this.peak = peak;
        }
}
