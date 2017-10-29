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

package org.opentripplanner.updater.example;

import java.util.prefs.Preferences;

import com.fasterxml.jackson.databind.JsonNode;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.updater.GraphUpdaterManager;
import org.opentripplanner.updater.GraphWriterRunnable;
import org.opentripplanner.updater.PollingGraphUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class shows an example of how to implement a polling graph updater. Besides implementing the
 * methods of the interface PollingGraphUpdater, the updater also needs to be registered in the
 * function GraphUpdaterConfigurator.applyConfigurationToGraph.
 * 
 * This example is suited for polling updaters. For streaming updaters (aka push updaters) it is
 * better to use the GraphUpdater interface directly for this purpose. The class ExampleGraphUpdater
 * shows an example of how to implement this.
 * 
 * Usage example ('polling-example' name is an example) in file 'Graph.properties':
 * 
 * <pre>
 * polling-example.type = example-polling-updater
 * polling-example.frequencySec = 60
 * polling-example.url = https://api.updater.com/example-polling-updater
 * </pre>
 * 
 * @see ExampleGraphUpdater
 * @see GraphUpdaterConfigurator.applyConfigurationToGraph
 */
public class ExamplePollingGraphUpdater extends PollingGraphUpdater {

    private static Logger LOG = LoggerFactory.getLogger(ExamplePollingGraphUpdater.class);

    private GraphUpdaterManager updaterManager;

    private String url;

    // Here the updater can be configured using the properties in the file 'Graph.properties'.
    // The property frequencySec is already read and used by the abstract base class.
    @Override
    protected void configurePolling(Graph graph, JsonNode config) throws Exception {
        url = config.path("url").asText();
        LOG.info("Configured example polling updater: frequencySec={} and url={}", frequencySec, url);
    }

    // Here the updater gets to know its parent manager to execute GraphWriterRunnables.
    @Override
    public void setGraphUpdaterManager(GraphUpdaterManager updaterManager) {
        LOG.info("Example polling updater: updater manager is set");
        this.updaterManager = updaterManager;
    }

    // Here the updater can be initialized.
    @Override
    public void setup() {
        LOG.info("Setup example polling updater");
    }

    // This is where the updater thread receives updates and applies them to the graph.
    // This method will be called every frequencySec seconds.
    @Override
    protected void runPolling() {
        LOG.info("Run example polling updater with hashcode: {}", this.hashCode());
        // Execute example graph writer
        updaterManager.execute(new ExampleGraphWriter());
    }

    // Here the updater can cleanup after itself.
    @Override
    public void teardown() {
        LOG.info("Teardown example polling updater");
    }
    
    // This is a private GraphWriterRunnable that can be executed to modify the graph
    private class ExampleGraphWriter implements GraphWriterRunnable {
        @Override
        public void run(Graph graph) {
            LOG.info("ExampleGraphWriter {} runnable is run on the "
                    + "graph writer scheduler.", this.hashCode());

            //Sketch of graph Updater

            //MOCK randomly evaluate stops
//            int i = 0;
//            final Random random = new Random();
//            int jump = random.nextInt(30);
//            if (jump == 0){
//                jump = 1;
//            }
//            for (Stop stop : graph.index.stopForId.values()) {
//                if (i % jump == 0) {
//                    stop.setWheelchairBoarding(((int)Math.random() * 30));
//                }
//                i++;
//            }

            //MOCK update retrieving from server
            //Agency and StopId AGENCYID_STOPID, EX for Metro: 4_est_4_1 (plaza de castilla)
//            ObjectMapper mapper = new ObjectMapper();
//            try {
//                JsonNode actualObj = mapper.readTree("{\"agencyId\":\"4\", \"stopId\":\"est_4_1\", \"wheelchair\":2}");
//                String agencyID = actualObj.get("agencyId").asText();
//                String stopId = actualObj.get("stopId").asText();
//                Integer wheelchair = actualObj.get("wheelchair").asInt();
//
//                Stop stop = graph.index.stopForId.get(new AgencyAndId("4", "est_4_1"));
//                if (stop != null) {
//                    stop.setWheelchairBoarding(3);
//                }
//
//
//                actualObj = mapper.readTree("{\"latitude\":\"40.3885381716506\", \"longitude\":\"-3.73150693296924\", \"aditionalStationData\":2}");
//                Double latitude = actualObj.get("latitude").asDouble();
//                Double longitude = actualObj.get("longitude").asDouble();
//                Double pollutionLevel = actualObj.get("aditionalStationData").asDouble();
//
//                Coordinate lowerLeftCorner = new Coordinate(-1.7318, 35.3862);
//                Coordinate upperRightCorner = new Coordinate(-9.7298, 45.3886);
//
//                Envelope envelope = new Envelope(upperRightCorner, lowerLeftCorner);
//
//                Collection<Edge> closeStreets = graph.streetIndex.getEdgesForEnvelope(new Envelope(new Coordinate(-3.5937729000000003, 40.4037839)));
//                for (Edge closeStreet : closeStreets) {
//                    if (closeStreet instanceof StreetEdge){
//                        ((StreetEdge) closeStreet).setNoise(3000);
//                    }
//                }
//
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            // Do some writing to the graph here
        }
    }
}
