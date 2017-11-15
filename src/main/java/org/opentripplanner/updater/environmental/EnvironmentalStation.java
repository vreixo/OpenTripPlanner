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
import org.opentripplanner.util.ResourceBundleSingleton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

public abstract class EnvironmentalStation {

    private final String id;

    private final I18NString location;

    private final I18NString city;

    private final I18NString country;

    private List<EnvironmentalStationMeasurement> measurements = new ArrayList<>();

    private final double x, y; //longitude, latitude

    public String getId() {
        return id;
    }

    public List<EnvironmentalStationMeasurement> getMeasurements() {
        return measurements;
    }

    public void addMeasurements(List<EnvironmentalStationMeasurement> measurements) {
        this.measurements.addAll(measurements);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public EnvironmentalStation(String id, I18NString location, I18NString city,
                                I18NString country, List<EnvironmentalStationMeasurement> measurements, double x, double y) {
        this.id = id;
        this.location = location;
        this.city = city;
        this.country = country;
        this.measurements = measurements;
        this.x = x;
        this.y = y;
    }

    protected EnvironmentalFactorMeasurement getFactor(Map<String, Double> weightsPerSubFactors,
                                                       EnvironmentalFactorType type, Function<List<Double>, Double> aggregator) {
        int numberOfSubFactors = 0;
        List<Double> weightedValues = new ArrayList<>();
        for (EnvironmentalStationMeasurement measurement : getMeasurements()) {
            Double weight = weightsPerSubFactors.get(measurement.getParameter());
            if (weight != null) {
                weightedValues.add(weight * measurement.getValue());
                numberOfSubFactors += 1;
            }
        }
         if (numberOfSubFactors > 1) {
            EnvironmentalFactorMeasurement environmentalFactorMeasurement = new EnvironmentalFactorMeasurement();
            environmentalFactorMeasurement.type = type;
            final double measurement;
            if (aggregator != null) {
                measurement = aggregator.apply(weightedValues);
            }
            else {
                measurement = getAverage(numberOfSubFactors, weightedValues);
            }
            environmentalFactorMeasurement.measurement = measurement;
            return environmentalFactorMeasurement;
        }
        else {
            return null;
        }
    }

    private double getAverage(int numberOfSubFactors, List<Double> weightedValues) {
        return weightedValues.stream().mapToDouble(Double::doubleValue).sum() / numberOfSubFactors;
    }

    public Locale locale = ResourceBundleSingleton.INSTANCE.getLocale(null);

    /**
     * Gets translated location of environmental station based on locale
     */
    public String getLocation() {
        return location.toString(locale);
    }

    /**
     * Gets translated city of environmental station based on locale
     */
    public String getCity() {
        return city.toString(locale);
    }

    /**
     * Gets translated country of environmental station based on locale
     */
    public String getCountry() {
        return country.toString(locale);
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public abstract Map<EnvironmentalFactorType, EnvironmentalFactorMeasurement> calculateEnvironmentalFactorsMeasurements();

}
