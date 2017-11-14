package org.opentripplanner.updater.environmental;

import com.fasterxml.jackson.databind.JsonNode;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.updater.JsonConfigurable;
import org.opentripplanner.util.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Fetch Stations from Http data source
 *
 * @see StationsDataSource
 */
public abstract class HttpDataSource
        implements StationsDataSource, JsonConfigurable {

    private static final Logger log = LoggerFactory
            .getLogger(HttpDataSource.class);

    private String url;

    private String headerName;

    private String headerValue;

    public HttpDataSource() {
        this.headerName = "Default";
        this.headerValue = null;
    }

    public HttpDataSource(String headerName,
                          String headerValue) {
        this.headerName = headerName;
        this.headerValue = headerValue;
    }

    @Override
    public boolean update() {
        try {
            InputStream data = null;

            URL url2 = new URL(url);

            String proto = url2.getProtocol();
            if (proto.equals("http") || proto.equals("https")) {
                data = HttpUtils.getData(url, headerName, headerValue);
            } else {
                // Local file probably, try standard java
                data = url2.openStream();
            }

            if (data == null) {
                log.warn("Failed to get data from url " + url);
                return false;
            }
            parseData(data);
            data.close();
        } catch (IllegalArgumentException e) {
            log.warn("Error parsing bike rental feed from " + url, e);
            return false;
        } catch (IOException e) {
            log.warn("Error reading bike rental feed from " + url, e);
            return false;
        }
        return true;
    }

    protected abstract void parseData(InputStream data) throws IOException;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public abstract EnvironmentalStation makeStation(JsonNode rentalStationNode);

    @Override
    public String toString() {
        return getClass().getName() + "(" + url + ")";
    }

    /**
     * Note that the JSON being passed in here is for configuration of the OTP component, it's completely separate
     * from the JSON coming in from the update source.
     */
    @Override
    public void configure(Graph graph, JsonNode jsonNode) {
        String url = jsonNode.path("url").asText(); // path() returns MissingNode not null.
        if (url == null) {
            throw new IllegalArgumentException(
                    "Missing mandatory 'url' configuration.");
        }
        this.url = url;
    }
}
