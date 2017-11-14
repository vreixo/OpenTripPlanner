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
import org.opentripplanner.updater.environmental.*;
import org.opentripplanner.util.NonLocalizedString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Implementation of a StationsDataSource for the generic openaq Open-Data API.
 *
 * @link https://developer.jcdecaux.com
 * @see StationsDataSource
 */
public class OpenAqPollutionDataSource extends GenericJsonDataSource
        implements StationsDataSource {

    private LocalDateTime timeToFilter = LocalDateTime.now().minusHours(24);

    public OpenAqPollutionDataSource() {
        super("results");
    }

    public OpenAqPollutionDataSource(LocalDateTime timeToFilter) {
        this();
        this.timeToFilter = timeToFilter;
    }

    /**
     * JSON openaq API v1 format:
     * <p>
     * <pre>
     * {
     * "meta": {
     * "name": "openaq-api",
     * "license": "CC BY 4.0",
     * "website": "https://docs.openaq.org/",
     * "page": 1,
     * "limit": 100,
     * "found": 2
     * },
     * "results": [
     * {
     * "location": "ES1422A",
     * "city": "Madrid",
     * "country": "ES",
     * "distance": 1057.9534670059588,
     * "measurements": [
     * {
     * "parameter": "so2",
     * "value": 6,
     * "lastUpdated": "2017-11-04T06:00:00.000Z",
     * "unit": "µg/m³",
     * "sourceName": "EEA Spain",
     * "averagingPeriod": {
     * "value": 1,
     * "unit": "hours"
     * }
     * },
     * {},
     * {},
     * {}
     * ],
     * "coordinates": {
     * "latitude": 40.419166564941406,
     * "longitude": -3.7033333778381348
     * }
     * },
     * {}
     * ]
     * }
     * </pre>
     */
    @Override
    public EnvironmentalStationAir makeStation(JsonNode node) {
        return EnvironmentalStationAir.builder().id(UUID.randomUUID().toString())
                .location(new NonLocalizedString(node.path("location").asText()))
                .city(new NonLocalizedString(node.path("city").asText()))
                .country(new NonLocalizedString(node.path("country").asText()))
                .measurements(
                        StreamSupport.stream(node.path("measurements").spliterator(), false)
                                .filter(measurementNode -> ZonedDateTime.parse(measurementNode.path("lastUpdated").asText()).toLocalDateTime().isAfter(timeToFilter))
                                .map(measurementNode -> EnvironmentalStationMeasurement.builder()
                                        .parameter(ParameterAir.valueOf(measurementNode.path("parameter").asText()).name())
                                        .value(measurementNode.path("value").asDouble())
                                        .lastUpdated(ZonedDateTime.parse(measurementNode.path("lastUpdated").asText()).toLocalDateTime())
                                        .unit(measurementNode.path("unit").asText())
                                        .sourceName(measurementNode.path("sourceName").asText())
                                        .build()).collect(Collectors.toList()))
                .y(node.path("coordinates").path("latitude").asDouble())
                .x(node.path("coordinates").path("longitude").asDouble())
                .build();
    }

}
