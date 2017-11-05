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

import org.opentripplanner.routing.constraints.EnvironmentalFactor;
import org.opentripplanner.routing.constraints.EnvironmentalFactorMeasurement;
import org.opentripplanner.util.I18NString;
import org.opentripplanner.util.ResourceBundleSingleton;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EnvironmentalStationNoise extends EnvironmentalStation {

        private Map<String, Double> weightsPerNoiseSubFactors = Collections.unmodifiableMap();

        public EnvironmentalStationNoise(String id, I18NString location, I18NString city,
                I18NString country, List<EnvironmentalStationMeasurement> measurements, double x,
                double y) {
                super(id, location, city, country, measurements, x, y);
        }

        @Override public List<EnvironmentalFactorMeasurement> calculateEnvironmentalFactorsMeasurements() {
                return list of both factors;
        }
}
