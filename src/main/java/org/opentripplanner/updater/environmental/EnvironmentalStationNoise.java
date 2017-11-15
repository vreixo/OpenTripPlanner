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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.AbstractMap.SimpleEntry;
import static org.opentripplanner.updater.environmental.ParameterNoise.*;

public class EnvironmentalStationNoise extends EnvironmentalStation {

    private Map<String, Double> weightsPerNoiseSubFactors = Stream
            .of(new SimpleEntry<>(LAeq.name(), 1.0),
                    new SimpleEntry<>(L01.name(), 1.0),
                    new SimpleEntry<>(L10.name(), 1.0),
                    new SimpleEntry<>(L50.name(), 1.0),
                    new SimpleEntry<>(L90.name(), 1.0),
                    new SimpleEntry<>(L99.name(), 1.0))
            .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));

    public EnvironmentalStationNoise(String id, I18NString location, I18NString city,
                                     I18NString country, List<EnvironmentalStationMeasurement> measurements, double x,
                                     double y) {
        super(id, location, city, country, measurements, x, y);
    }

    @Override
    public Map<EnvironmentalFactorType, EnvironmentalFactorMeasurement> calculateEnvironmentalFactorsMeasurements() {
        return Collections.singletonMap(EnvironmentalFactorType.NOISE, 
                getFactor(weightsPerNoiseSubFactors,  EnvironmentalFactorType.NOISE, null));
    }

    public static EnvironmentalStationNoiseBuilder builder() {
        return new EnvironmentalStationNoiseBuilder();
    }

    public static class EnvironmentalStationNoiseBuilder {
        private String id;

        private I18NString location;

        private I18NString city;

        private I18NString country;

        private List<EnvironmentalStationMeasurement> measurements;

        private double x;

        private double y;

        public EnvironmentalStationNoiseBuilder id(String id) {
            this.id = id;
            return this;
        }

        public EnvironmentalStationNoiseBuilder location(I18NString location) {
            this.location = location;
            return this;
        }

        public EnvironmentalStationNoiseBuilder city(I18NString city) {
            this.city = city;
            return this;
        }

        public EnvironmentalStationNoiseBuilder country(I18NString country) {
            this.country = country;
            return this;
        }

        public EnvironmentalStationNoiseBuilder measurements(
                List<EnvironmentalStationMeasurement> measurements) {
            this.measurements = measurements;
            return this;
        }

        public EnvironmentalStationNoiseBuilder x(double x) {
            this.x = x;
            return this;
        }

        public EnvironmentalStationNoiseBuilder y(double y) {
            this.y = y;
            return this;
        }

        public EnvironmentalStationNoise build() {
            return new EnvironmentalStationNoise(id, location, city, country, measurements, x, y);
        }
    }

}
