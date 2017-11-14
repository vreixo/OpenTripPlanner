package org.opentripplanner.updater.environmental;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opentripplanner.updater.JsonConfigurable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Fetch Bike Rental JSON feeds and pass each record on to the specific rental subclass
 *
 * @see StationsDataSource
 */
public abstract class GenericJsonDataSource extends HttpDataSource
        implements JsonConfigurable {

    private static final Logger log = LoggerFactory
            .getLogger(GenericJsonDataSource.class);


    private String jsonParsePath;

    private List<EnvironmentalStation> stations = new ArrayList<>();

    /**
     * Construct superclass
     *
     * @param jsonPath path to get from enclosing elements to nested rental list.
     *                 Separate path levels with '/' For example "d/list"
     */
    public GenericJsonDataSource(String jsonPath) {
        super("Default", null);
        jsonParsePath = jsonPath;
    }

    /**
     * @param jsonPath    path to get from enclosing elements to nested rental list.
     *                    Separate path levels with '/' For example "d/list"
     * @param headerName  header name
     * @param headerValue header value
     */
    public GenericJsonDataSource(String jsonPath, String headerName,
                                 String headerValue) {
        super(headerName, headerValue);
        jsonParsePath = jsonPath;
    }

    /**
     * Construct superclass where rental list is on the top level of JSON code
     */
    public GenericJsonDataSource() {
        super("Default", null);
        jsonParsePath = "";
    }

    @Override
    public void parseData(InputStream dataStream) {

        try {
            ArrayList<EnvironmentalStation> out = new ArrayList<>();

            String jsonString = convertStreamToString(dataStream);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(jsonString);

            if (!jsonParsePath.equals("")) {
                String delimiter = "/";
                String[] parseElement = jsonParsePath.split(delimiter);
                for (int i = 0; i < parseElement.length; i++) {
                    rootNode = rootNode.path(parseElement[i]);
                }

                if (rootNode.isMissingNode()) {
                    throw new IllegalArgumentException(
                            "Could not find jSON elements " + jsonParsePath);
                }
            }

            for (int i = 0; i < rootNode.size(); i++) {
                JsonNode node = rootNode.get(i);
                if (node == null) {
                    continue;
                }
                EnvironmentalStation brstation = makeStation(node);
                if (brstation != null)
                    out.add(brstation);
            }
            synchronized (this) {
                stations = out;
            }

        } catch (IOException | IllegalArgumentException e) {
            log.warn("Error parsing station feed (bad JSON of some sort)", e);
        }
    }

    private String convertStreamToString(InputStream is) {
        java.util.Scanner scanner = null;
        String result = "";
        try {

            scanner = new java.util.Scanner(is).useDelimiter("\\A");
            result = scanner.hasNext() ? scanner.next() : "";
            scanner.close();
        } finally {
            if (scanner != null)
                scanner.close();
        }
        return result;

    }

    public abstract EnvironmentalStation makeStation(JsonNode stationNode);

    @Override
    public synchronized List<EnvironmentalStation> getStations() {
        return stations;
    }

    @Override
    public String toString() {
        return getClass().getName();
    }

}
