package org.opentripplanner.updater.environmental;

import com.fasterxml.jackson.databind.JsonNode;
import com.vividsolutions.jts.geom.Coordinate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opentripplanner.routing.constraints.EnvironmentalFactorType;
import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.location.StreetLocation;
import org.opentripplanner.routing.services.StreetVertexIndexFactory;
import org.opentripplanner.routing.services.StreetVertexIndexService;
import org.opentripplanner.updater.GraphUpdaterManager;

import java.util.Arrays;
import java.util.UUID;

import static com.conveyal.r5.common.JsonUtilities.objectMapper;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EnvironmentalUpdaterTest {

    private EnvironmentalUpdater environmentalUpdater = new EnvironmentalUpdater();

    Graph graph = new Graph();

    @Mock
    StreetVertexIndexService streetIndex;

    @Mock
    StreetVertexIndexFactory indexFactory;

    @Mock
    GraphUpdaterManager updaterManager;

    GraphUpdaterManager realUpdaterManager;


    @Before
    public void setUp() throws IllegalAccessException, NoSuchFieldException {
        when(indexFactory.newIndex(any(Graph.class))).thenReturn(streetIndex);
        graph.index(indexFactory);
        realUpdaterManager = new GraphUpdaterManager(graph);
    }

    @Test
    public void shouldDownloadBothFileAndMakeStations() throws Exception {
        String jsonConfigMadrid = "{" +
                "\"type\": \"environmental-updater\"," +
                "\"frequencySec\": -1," +
                "\"sourceType\": \"medio-ambiente-madrid\"," +
                "\"urlStationsPosition\": \"http://datos.madrid.es/egob/catalogo/211346-1-estaciones-acusticas.csv\"," +
                "\"urlStationsData\": \"http://www.mambiente.munimadrid.es/opendata/ruido.txt\"" +
                "}";
        runScheduling(jsonConfigMadrid, false);
    }

    @Test
    public void shouldDownloadFileAndMakeStations() throws Exception {
        final String jsonConfigString = "{" +
                "\"type\": \"environmental-updater\"," +
                "\"frequencySec\": -1," +
                "\"sourceType\": \"openaq\"," +
                "\"url\": \"https://api.openaq.org/v1/latest?coordinates=40.41,-3.70&radius=35000\"" +
                "}";
        runScheduling(jsonConfigString, false);
    }

    @Test
    public void shouldUpdateStreetsNearStation() throws Exception {
        final String jsonConfigString = "{" +
                "\"type\": \"environmental-updater\"," +
                "\"frequencySec\": -1," +
                "\"sourceType\": \"openaq\"," +
                "\"url\": \"https://api.openaq.org/v1/latest?coordinates=40.41,-3.70&radius=35000\"" +
                "}";
        final StreetEdge firstEdge = new StreetEdge(new StreetLocation(UUID.randomUUID().toString(), new Coordinate(0, 0), "firstOrigin"), new StreetLocation(UUID.randomUUID().toString(), new Coordinate(1, 1), "firstDestination"), null, "first", 50, null, false);
        final StreetEdge secondEdge = new StreetEdge(new StreetLocation(UUID.randomUUID().toString(), new Coordinate(0, 0), "secondOrigin"), new StreetLocation(UUID.randomUUID().toString(), new Coordinate(1, 1), "secondDestination"), null, "first", 50, null, false);
        when(streetIndex.getNearbyEdges(any(Coordinate.class), anyDouble())).thenReturn(Arrays.asList(firstEdge, secondEdge));
        runScheduling(jsonConfigString, true);
        assertThat(firstEdge.getEnvironmentalFactorsMeasurements()).isNotEmpty();
        assertThat(firstEdge.getEnvironmentalFactorsMeasurements().stream()
                .allMatch(environmentalFactorMeasurement -> environmentalFactorMeasurement.getType() == EnvironmentalFactorType.POLLUTION
                    || environmentalFactorMeasurement.getType() == EnvironmentalFactorType.ALLERGIC)).isTrue();
        assertThat(secondEdge.getEnvironmentalFactorsMeasurements()).isNotEmpty();
        assertThat(secondEdge.getEnvironmentalFactorsMeasurements().stream()
                .allMatch(environmentalFactorMeasurement -> environmentalFactorMeasurement.getType() == EnvironmentalFactorType.POLLUTION
                        || environmentalFactorMeasurement.getType() == EnvironmentalFactorType.ALLERGIC)).isTrue();
    }

    private void runScheduling(String jsonConfigString, boolean useRealUpdater) throws Exception {
        JsonNode jsonConfig = objectMapper.readTree(jsonConfigString);
        environmentalUpdater.setGraphUpdaterManager(useRealUpdater ? realUpdaterManager : updaterManager);
        environmentalUpdater.configure(graph, jsonConfig);
        environmentalUpdater.run();
        if (!useRealUpdater) {
            ArgumentCaptor<EnvironmentalUpdater.EnvironmentalFactorsGraphWriterRunnable> graphWriterRunnableArgumentCaptor = ArgumentCaptor.forClass(EnvironmentalUpdater.EnvironmentalFactorsGraphWriterRunnable.class);
            verify(updaterManager).execute(graphWriterRunnableArgumentCaptor.capture());
            assertThat(graphWriterRunnableArgumentCaptor.getValue().stations.size()).isGreaterThan(0);
        }
    }

}