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

import java.util.List;
import java.util.Locale;

public abstract class EnvironmentalStation {

    private final String id;

    private final I18NString location;

    private final I18NString city;

    private final I18NString country;

    protected final List<EnvironmentalStationMeasurement> measurements;

    private final double x, y; //longitude, latitude

        public String getId() {
                return id;
        }

        public List<EnvironmentalStationMeasurement> getMeasurements() {
                return measurements;
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

        public abstract List<EnvironmentalFactorMeasurement> calculateEnvironmentalFactorsMeasurements();

        public static EnvironmentalStationBuilder builder() {
                return new EnvironmentalStationBuilder();
        }

        public static class EnvironmentalStationBuilder {
                private String id;

                private I18NString location;

                private I18NString city;

                private I18NString country;

                private List<EnvironmentalStationMeasurement> measurements;

                private double x;

                private double y;

                public EnvironmentalStationBuilder id(String id) {
                        this.id = id;
                        return this;
                }

                public EnvironmentalStationBuilder location(I18NString location) {
                        this.location = location;
                        return this;
                }

                public EnvironmentalStationBuilder city(I18NString city) {
                        this.city = city;
                        return this;
                }

                public EnvironmentalStationBuilder country(I18NString country) {
                        this.country = country;
                        return this;
                }

                public EnvironmentalStationBuilder measurements(List<EnvironmentalStationMeasurement> measurements) {
                        this.measurements = measurements;
                        return this;
                }

                public EnvironmentalStationBuilder x(double x) {
                        this.x = x;
                        return this;
                }

                public EnvironmentalStationBuilder y(double y) {
                        this.y = y;
                        return this;
                }
        }
}
