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

import com.thanglequoc.aqicalculator.AQICalculator;
import com.thanglequoc.aqicalculator.AQIResult;
import com.thanglequoc.aqicalculator.PollutantCode;
import org.opentripplanner.routing.constraints.EnvironmentalFactorMeasurement;
import org.opentripplanner.routing.constraints.EnvironmentalFactorType;
import org.opentripplanner.util.I18NString;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.AbstractMap.SimpleEntry;
import static org.opentripplanner.updater.environmental.ParameterAir.*;

public class EnvironmentalStationAir extends EnvironmentalStation {

    //MeteoGalicia AQI http://www.meteogalicia.gal/datosred/infoweb/caire/informes/MANUALES/ES/IT_31_CALCULO_DO_ICA.pdf
    private Map<String, Double> weightsPerPollutionSubFactors = Stream
            .of(new SimpleEntry<>(co.name(), 6.67466986795), // Eight hour limit applying 0.667466986795 percentage
                    new SimpleEntry<>(no2.name(), 0.500),
                    new SimpleEntry<>(so2.name(), 0.800),
                    new SimpleEntry<>(o3.name(), 0.556),
                    new SimpleEntry<>(pm10.name(), 0.67),
                    new SimpleEntry<>(pm25.name(), 0.67)) // Same value as for pm10
            .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));

    //Not real allergic value, just using pm10 as it's closer particle size, although pollen particles are usually bigger
    private Map<String, Double> weightsPerAllergicSubFactors = Stream
            .of(new SimpleEntry<>(pm10.name(), 1.0))
            .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));

    public EnvironmentalStationAir(String id, I18NString location, I18NString city,
                                   I18NString country, List<EnvironmentalStationMeasurement> measurements, double x,
                                   double y) {
        super(id, location, city, country, measurements, x, y);
    }

    @Override
    public List<EnvironmentalFactorMeasurement> calculateEnvironmentalFactorsMeasurements() {
        List<EnvironmentalFactorMeasurement> environmentalFactorMeasurements = new ArrayList<>();
        final EnvironmentalFactorMeasurement pollutionFactor = getFactor(weightsPerPollutionSubFactors, EnvironmentalFactorType.POLLUTION, this::maxValue);
        if (pollutionFactor != null) {
            environmentalFactorMeasurements.add(pollutionFactor);
        }
        final EnvironmentalFactorMeasurement allergicFactor = getFactor(weightsPerAllergicSubFactors, EnvironmentalFactorType.ALLERGIC, null);
        if (allergicFactor != null) {
            environmentalFactorMeasurements.add(allergicFactor);
        }
        return environmentalFactorMeasurements;
    }

    private Double maxValue(List<Double> values) {
        return values.stream().max(Double::compareTo).orElse(0.0);
    }

    public static EnvironmentalStationAirBuilder builder() {
        return new EnvironmentalStationAirBuilder();
    }

    public static class EnvironmentalStationAirBuilder {
        private String id;

        private I18NString location;

        private I18NString city;

        private I18NString country;

        private List<EnvironmentalStationMeasurement> measurements;

        private double x;

        private double y;

        public EnvironmentalStationAirBuilder id(String id) {
            this.id = id;
            return this;
        }

        public EnvironmentalStationAirBuilder location(I18NString location) {
            this.location = location;
            return this;
        }

        public EnvironmentalStationAirBuilder city(I18NString city) {
            this.city = city;
            return this;
        }

        public EnvironmentalStationAirBuilder country(I18NString country) {
            this.country = country;
            return this;
        }

        public EnvironmentalStationAirBuilder measurements(
                List<EnvironmentalStationMeasurement> measurements) {
            this.measurements = measurements;
            return this;
        }

        public EnvironmentalStationAirBuilder x(double x) {
            this.x = x;
            return this;
        }

        public EnvironmentalStationAirBuilder y(double y) {
            this.y = y;
            return this;
        }

        public EnvironmentalStationAir build() {
            return new EnvironmentalStationAir(id, location, city, country, measurements, x, y);
        }
    }

}
