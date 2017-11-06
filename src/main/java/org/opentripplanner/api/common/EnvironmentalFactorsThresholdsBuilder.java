package org.opentripplanner.api.common;

import java.util.ArrayList;
import java.util.List;

import org.opentripplanner.routing.constraints.EnvironmentalFactorThreshold;
import org.opentripplanner.routing.constraints.EnvironmentalFactorType;

/**
 * Builder that creates a list of EnvironmentalFactorThreshold from an input String of ampersand
 * separated elements that represent the threshold of one of the allowed properties (average or
 * peak) for the specific environmental factor.
 * 
 * Each of the elements of the given string should have the following format:
 * ENVIROMENTAL_{factorType}_{propertyName (MAX_AVERAGE or MAX_PEAK)}:{value}
 * 
 * Any element with other format will be ignored.
 * 
 * Here is an example of a valid input:
 * ENVIRONMENTAL_POLLUTION_MAX_AVERAGE=5.0&ENVIRONMENTAL_POLLUTION_MAX_PEAK=15.0&ENVIRONMENTAL_ALLERGIC_MAX_AVERAGE=10.0
 */
public class EnvironmentalFactorsThresholdsBuilder {
   
    public static List<EnvironmentalFactorThreshold> build(String inputString) {
        final List<EnvironmentalFactorThreshold> thresholds = new ArrayList<>();

        if (inputString != null) {
            String[] factorProperties = inputString.split("&");
            for (String factorProperty : factorProperties) {
                processNewProperty(thresholds, factorProperty);
            }
        }

        return thresholds;
    }

    private static void processNewProperty(final List<EnvironmentalFactorThreshold> thresholds,
            String factorProperty) {
        String[] tokens = factorProperty.split("_");
        if (tokens.length == 4 && "ENVIRONMENTAL".equals(tokens[0])) {
            try {
                EnvironmentalFactorType type = EnvironmentalFactorType.valueOf(tokens[1]);
                int thresholdIndex = getPositionOfThresholdOfType(thresholds, type);
                String[] fieldValue = tokens[3].split("=");

                if (thresholdIndex == -1) {
                    addNewFactorThreshold(thresholds, type, fieldValue);
                } else {
                    replaceWithCopyWithNewProperty(thresholds, type, thresholdIndex, fieldValue);
                }
            } catch (IllegalArgumentException e) {
                // Do nothing for we want to continue.
            }
        }
    }

    private static int getPositionOfThresholdOfType(List<EnvironmentalFactorThreshold> thresholds,
            EnvironmentalFactorType type) {
        int i = 0;
        for (EnvironmentalFactorThreshold threshold : thresholds) {
            if (type.equals(threshold.getType())) {
                return i;
            }
            i++;
        }
        return -1;
    }

    private static void addNewFactorThreshold(final List<EnvironmentalFactorThreshold> thresholds,
            EnvironmentalFactorType type, String[] fieldValue) {
        if ("AVERAGE".equals(fieldValue[0])) {
            thresholds.add(new EnvironmentalFactorThreshold(type, new Double(fieldValue[1]), null));
        } else if ("PEAK".equals(fieldValue[0])) {
            thresholds.add(new EnvironmentalFactorThreshold(type, null, new Double(fieldValue[1])));
        }
    }

    private static void replaceWithCopyWithNewProperty(final List<EnvironmentalFactorThreshold> thresholds,
            EnvironmentalFactorType type, int thresholdIndex, String[] fieldValue) {
        EnvironmentalFactorThreshold existingThreshold = thresholds.get(thresholdIndex);
        thresholds.remove(thresholdIndex);

        if ("AVERAGE".equals(fieldValue[0])) {
            thresholds.add(new EnvironmentalFactorThreshold(type, new Double(fieldValue[1]),
                    existingThreshold.getMaxPeak()));
        } else if ("PEAK".equals(fieldValue[0])) {
            thresholds.add(new EnvironmentalFactorThreshold(type, existingThreshold.getMaxAverage(),
                    new Double(fieldValue[1])));
        }
    }

}
