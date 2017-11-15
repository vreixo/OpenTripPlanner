package org.opentripplanner.api.common;

import org.opentripplanner.routing.constraints.EnvironmentalFactorThreshold;
import org.opentripplanner.routing.constraints.EnvironmentalFactorType;

import java.util.HashMap;
import java.util.Map;

/**
 * Builder that creates a list of EnvironmentalFactorThreshold from an input String of ampersand
 * separated elements that represent the threshold of one of the allowed properties (average or
 * peak) for the specific environmental factor.
 * <p>
 * Each of the elements of the given string should have the following format:
 * ENVIROMENTAL_{factorType}_{propertyName (MAX_AVERAGE or MAX_PEAK)}:{value}
 * <p>
 * Any element with other format will be ignored.
 * <p>
 * Here is an example of a valid input:
 * ENVIRONMENTAL_POLLUTION_MAX_AVERAGE=5.0&ENVIRONMENTAL_POLLUTION_MAX_PEAK=15.0&ENVIRONMENTAL_ALLERGIC_MAX_AVERAGE=10.0
 */
public class EnvironmentalFactorsThresholdsBuilder {

    public static Map<EnvironmentalFactorType, EnvironmentalFactorThreshold> build(String inputString) {
        final Map<EnvironmentalFactorType, EnvironmentalFactorThreshold> thresholds = new HashMap<>();

        if (inputString != null) {
            String[] factorProperties = inputString.split("&");
            for (String factorProperty : factorProperties) {
                processNewProperty(thresholds, factorProperty);
            }
        }

        return thresholds;
    }

    private static void processNewProperty(final Map<EnvironmentalFactorType, EnvironmentalFactorThreshold> thresholds,
                                           String factorProperty) {
        String[] tokens = factorProperty.split("_");
        if (tokens.length == 4 && "ENVIRONMENTAL".equals(tokens[0])) {
            try {
                EnvironmentalFactorType type = EnvironmentalFactorType.valueOf(tokens[1]);
                String[] fieldValue = tokens[3].split("=");

                final EnvironmentalFactorThreshold environmentalFactorThreshold = thresholds.get(type);
                if (environmentalFactorThreshold == null) {
                    addNewFactorThreshold(thresholds, type, fieldValue);
                } else {
                    replaceWithCopyWithNewProperty(thresholds, type, fieldValue, environmentalFactorThreshold);
                }
            } catch (IllegalArgumentException e) {
                // Do nothing for we want to continue.
            }
        }
    }

    private static void addNewFactorThreshold(final Map<EnvironmentalFactorType, EnvironmentalFactorThreshold> thresholds,
                                              EnvironmentalFactorType type, String[] fieldValue) {
        if ("AVERAGE".equals(fieldValue[0])) {
            thresholds.put(type, new EnvironmentalFactorThreshold(type, new Double(fieldValue[1]), null));
        } else if ("PEAK".equals(fieldValue[0])) {
            thresholds.put(type, new EnvironmentalFactorThreshold(type, null, new Double(fieldValue[1])));
        }
    }

    private static void replaceWithCopyWithNewProperty(final Map<EnvironmentalFactorType, EnvironmentalFactorThreshold> thresholds,
                                                       EnvironmentalFactorType type, String[] fieldValue, EnvironmentalFactorThreshold existingThreshold) {
        if ("AVERAGE".equals(fieldValue[0])) {
            thresholds.put(type, new EnvironmentalFactorThreshold(type, new Double(fieldValue[1]),
                    existingThreshold.maxPeak));
        } else if ("PEAK".equals(fieldValue[0])) {
            thresholds.put(type, new EnvironmentalFactorThreshold(type, existingThreshold.maxAverage,
                    new Double(fieldValue[1])));
        }
    }

}
