/* This program is free software: you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public License
 as published by the Free Software Foundation, either version 3 of
 the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>. */

package org.opentripplanner.updater.environmental;

import org.opentripplanner.routing.constraints.EnvironmentalFactorMeasurement;
import org.opentripplanner.routing.constraints.EnvironmentalFactorType;
import org.opentripplanner.util.I18NString;

import java.util.*;

public class EnvironmentalStationAir extends EnvironmentalStation {

        private Map<String, Double> weightsPerPollutionSubFactors = Collections.unmodifiableMap();

        private Map<String, Double> weightsPerAllergicSubFactors = Collections.unmodifiableMap();

        public EnvironmentalStationAir(String id, I18NString location, I18NString city,
                I18NString country, List<EnvironmentalStationMeasurement> measurements, double x,
                double y) {
                super(id, location, city, country, measurements, x, y);
        }

        @Override public List<EnvironmentalFactorMeasurement> calculateEnvironmentalFactorsMeasurements() {
                return Arrays.asList(getFactor(weightsPerPollutionSubFactors, EnvironmentalFactorType.POLLUTION),
                        getFactor(weightsPerAllergicSubFactors, EnvironmentalFactorType.ALLERGIC));
        }

        private EnvironmentalFactorMeasurement getFactor(
                Map<String, Double> weightsPerSubFactors,
                EnvironmentalFactorType type) {
                double accumulatedValue = 0;
                for (EnvironmentalStationMeasurement measurement : measurements) {
                        Double weight = weightsPerSubFactors
                                .get(measurement.getParameter());
                        if (weight != null) {
                                accumulatedValue += weight * Double.valueOf(measurement.getValue());
                        }
                }
                EnvironmentalFactorMeasurement environmentalFactorMeasurement = new EnvironmentalFactorMeasurement();
                environmentalFactorMeasurement.setType(type);
                environmentalFactorMeasurement.setMeasurement(accumulatedValue);
                return environmentalFactorMeasurement;
        }

}
