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

import com.fasterxml.jackson.databind.JsonNode;
import com.vividsolutions.jts.geom.Coordinate;
import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.updater.GraphUpdaterManager;
import org.opentripplanner.updater.GraphWriterRunnable;
import org.opentripplanner.updater.JsonConfigurable;
import org.opentripplanner.updater.PollingGraphUpdater;
import org.opentripplanner.updater.environmental.updaters.MedioAmbienteMadridNoiseDataSource;
import org.opentripplanner.updater.environmental.updaters.OpenAqPollutionDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Pollution edge updater which encapsulate one StationsDataSource.
 * <p>
 * Usage example ('pollution' name is an example) in the file 'Graph.properties':
 * <p>
 * <pre>
 * pollution.type = environmental-updater
 * pollution.frequencySec = 60
 * pollution.sourceType = openaq
 * pollution.url = https://api.openaq.org/v1/latest?coordinates=40.41,-3.70&radius=35000
 * </pre>
 */
public class EnvironmentalUpdater extends PollingGraphUpdater {

    private static final Logger LOG = LoggerFactory.getLogger(
            EnvironmentalUpdater.class);

    private static final int AFFECTED_AREA_RADIUS_IN_METERS = 100;

    private GraphUpdaterManager updaterManager;

    private StationsDataSource source;

    private Graph graph;

    @Override
    public void setGraphUpdaterManager(GraphUpdaterManager updaterManager) {
        this.updaterManager = updaterManager;
    }

    @Override
    protected void configurePolling(Graph graph, JsonNode config) throws Exception {

        // Set data source type from config JSON
        String sourceType = config.path("sourceType").asText();
        String apiKey = config.path("apiKey").asText();
        StationsDataSource source = null;
        if (sourceType != null) {
            if (sourceType.equals("openaq")) {
                source = new OpenAqPollutionDataSource();
            }
            if (sourceType.equals("medio-ambiente-madrid")) {
                source = new MedioAmbienteMadridNoiseDataSource();
            }
        }

        if (source == null) {
            throw new IllegalArgumentException("Unknown environmental source type: " + sourceType);
        } else if (source instanceof JsonConfigurable) {
            ((JsonConfigurable) source).configure(graph, config);
        }

        // Configure updater
        LOG.info("Setting up environmental updater.");
        this.graph = graph;
        this.source = source;
        LOG.info("Creating environmental updater running every {} seconds : {}", frequencySec, source);
    }

    @Override
    public void setup() throws InterruptedException, ExecutionException {
    }

    @Override
    protected void runPolling() throws Exception {
        LOG.debug("Updating environmental stations from " + source);
        if (!source.update()) {
            LOG.debug("No updates");
            return;
        }
        List<EnvironmentalStation> stations = source.getStations();

        // Create graph writer runnable to apply these stations to the graph
        EnvironmentalFactorsGraphWriterRunnable graphWriterRunnable = new EnvironmentalFactorsGraphWriterRunnable(stations);
        updaterManager.execute(graphWriterRunnable);
    }

    @Override
    public void teardown() {
    }

    protected class EnvironmentalFactorsGraphWriterRunnable implements GraphWriterRunnable {

        protected List<EnvironmentalStation> stations;

        public EnvironmentalFactorsGraphWriterRunnable(List<EnvironmentalStation> stations) {
            this.stations = stations;
        }

        @Override
        public void run(Graph graph) {
            stations.stream()
                    .filter(environmentalStation -> !environmentalStation.getMeasurements().isEmpty())
                    .forEach(this::updateStreetsNearStation);
        }

        private void updateStreetsNearStation(EnvironmentalStation station) {
            List<Edge> nearbyEdges = graph.streetIndex
                    .getNearbyEdges(new Coordinate(station.getX(), station.getY()),
                            AFFECTED_AREA_RADIUS_IN_METERS);
            nearbyEdges.stream().filter(edge -> edge instanceof StreetEdge).forEach(
                    edge -> ((StreetEdge) edge).addEnvironmentalFactors(station.calculateEnvironmentalFactorsMeasurements()));
        }
    }
}
