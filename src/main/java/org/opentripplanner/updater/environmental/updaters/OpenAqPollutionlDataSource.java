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

package org.opentripplanner.updater.environmental.updaters;

import com.fasterxml.jackson.databind.JsonNode;
import org.opentripplanner.updater.environmental.EnvironmentalDataSource;
import org.opentripplanner.updater.environmental.EnvironmentalStation;
import org.opentripplanner.updater.environmental.EnvironmentalStationMeasurement;
import org.opentripplanner.updater.environmental.GenericJsonPollutionDataSource;
import org.opentripplanner.util.NonLocalizedString;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of a EnvironmentalDataSource for the generic openaq Open-Data API.
 *
 * @link https://developer.jcdecaux.com
 * @see EnvironmentalDataSource
 */
public class OpenAqPollutionlDataSource extends GenericJsonPollutionDataSource {

        public OpenAqPollutionlDataSource() {
                super("");
        }

        /**
         * JSON openaq API v1 format:
         * <p>
         * <pre>
         {
                "meta": {
                        "name": "openaq-api",
                        "license": "CC BY 4.0",
                        "website": "https://docs.openaq.org/",
                         "page": 1,
                         "limit": 100,
                         "found": 2
                },
                "results": [
                        {
                         "location": "ES1422A",
                         "city": "Madrid",
                         "country": "ES",
                         "distance": 1057.9534670059588,
                         "measurements": [
                                {
                                 "parameter": "so2",
                                 "value": 6,
                                 "lastUpdated": "2017-11-04T06:00:00.000Z",
                                 "unit": "µg/m³",
                                 "sourceName": "EEA Spain",
                                 "averagingPeriod": {
                                         "value": 1,
                                         "unit": "hours"
                                        }
                                },
                         {},
                         {},
                         {}
                         ],
                         "coordinates": {
                                 "latitude": 40.419166564941406,
                                 "longitude": -3.7033333778381348
                                }
                         },
                         {}
                        ]
                 }
         * </pre>
         */
        public EnvironmentalStation makeStation(JsonNode node) {
                return EnvironmentalStation.builder().id(UUID.randomUUID().toString())
                        .location(new NonLocalizedString(node.path("location").asText()))
                        .city(new NonLocalizedString(node.path("position").path("lat").asText()))
                        .country(new NonLocalizedString(node.path("name").asText())).measurements(
                                Stream.generate(node.path("measurements").iterator()::next)
                                        .map(measurement -> EnvironmentalStationMeasurement.builder()
                                                .parameter(node.path("parameter").asText())
                                                .value(node.path("value").asText()).lastUpdated(
                                                        LocalDateTime.parse(node.path("lastUpdated")
                                                                .asText()))
                                                .unit(node.path("unit").asText())
                                                .sourceName(node.path("sourceName").asText())
                                                .build()).collect(Collectors.toList()))
                        .x(node.path("coordinates").path("longitude").asInt())
                        .x(node.path("coordinates").path("latitude").asInt())
                        .build();
        }
}
