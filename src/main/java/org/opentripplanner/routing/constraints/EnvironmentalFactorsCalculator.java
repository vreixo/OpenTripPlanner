package org.opentripplanner.routing.constraints;

import org.opentripplanner.routing.core.State;

import java.util.List;

public class EnvironmentalFactorsCalculator {

        public void updateStatePeak(List<EnvironmentalFactorMeasurement> environmentalFactorsMeasurements, State state) {
                for (EnvironmentalFactorMeasurement environmentalFactorMeasurement : environmentalFactorsMeasurements) {
                        environmentalFactorMeasurement.getType()
                }
        }

        public void updateStateAccumulatedValue(List<EnvironmentalFactorMeasurement> environmentalFactorsMeasurements, State state, double length){

        }

        public double calculateOverageWeight(List<EnvironmentalFactorThreshold> environmentalFactorsThresholds, State state) {
             if (weHaveTooMuch()) {

             }
             return 0;
        }

        private boolean weHaveTooMuch(List<EnvironmentalFactorThreshold> environmentalFactorsThresholds, State state) {

        }


}
