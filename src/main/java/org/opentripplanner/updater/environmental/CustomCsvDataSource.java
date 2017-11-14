package org.opentripplanner.updater.environmental;

import com.csvreader.CsvReader;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.io.FileUtils;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.updater.JsonConfigurable;
import org.opentripplanner.util.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.LocalTime;
import java.util.*;

/**
 * Fetch Bike Rental JSON feeds and pass each record on to the specific rental subclass
 *
 * @see StationsDataSource
 */
public abstract class CustomCsvDataSource
        implements StationsDataSource, JsonConfigurable {

    private static final Logger LOG = LoggerFactory
            .getLogger(CustomCsvDataSource.class);

    private static final String REGEX_IS_NUMBER = "^\\d+$";

    private String urlStationsData;

    private String urlStationsPosition;

    private Map<Integer, EnvironmentalStation> stationsById = new HashMap<>();

    @Override
    public boolean update() {
        File temporalFile = new File("temporalStationData.csv");
        try {
            temporalFile.createNewFile();
        } catch (IOException e) {
            LOG.warn("Not possible to create stations positions file.");
            return false;
        }
        try (InputStream fileData = HttpUtils.getData(urlStationsPosition);
             InputStream positionData = new FileInputStream(temporalFile)) {
            FileUtils.copyInputStreamToFile(fileData, temporalFile);
            CsvReader reader = new CsvReader(positionData, ';',
                    Charset.forName("ISO-8859-15"));
            reader.readRecord();
            reader.readRecord();
            while (reader.readRecord()) {
                final String[] values = reader.getValues();
                if (values.length >= 4 && values[0].matches(REGEX_IS_NUMBER)) {
                    EnvironmentalStation environmentalStationNoise = makeStation(values);
                    stationsById.put(Integer.valueOf(environmentalStationNoise.getId()),
                            environmentalStationNoise);
                }
            }
            final boolean delete = temporalFile.delete();
            if (!delete) {
                LOG.warn("Not possible to remove stations positions file.");
            }
        } catch (IOException e) {
            LOG.warn("Exception while loading stations positions file.");
            return false;
        }
        try (InputStream data = HttpUtils.getData(urlStationsData)) {
            CsvReader reader = new CsvReader(data, Charset.forName("UTF-8"));
            while (reader.readRecord()) {
                final String momentOfDay = reader.get(4);
                if (!isNowMomentOfDay(momentOfDay)) {
                    continue;
                }
                EnvironmentalStation environmentalStationNoise = stationsById
                        .get(Integer.valueOf(reader.get(0)));
                addStationMeasurements(environmentalStationNoise, reader.getValues());
            }
        } catch (IOException ex) {
            LOG.warn("Exception while loading stations measurements.");
            return false;
        }
        return true;
    }

    public abstract EnvironmentalStation makeStation(String[] lineValues);

    public abstract void addStationMeasurements(EnvironmentalStation environmentalStationNoise,
                                                String[] lineValues) throws IOException;

    private boolean isNowMomentOfDay(String momentOfDay) throws IOException {
        final LocalTime nowTime = LocalTime.now();
        final LocalTime morningStartTime = LocalTime.of(7, 0);
        final LocalTime afternoonStartTime = LocalTime.of(19, 0);
        final LocalTime nightStartTime = LocalTime.of(23, 0);
        switch (momentOfDay) {
            case "D":
                if (nowTime.isAfter(morningStartTime)
                        && nowTime.isBefore(afternoonStartTime)) {
                    return true;
                }
                else {
                    return false;
                }
            case "T":
                if (nowTime.isAfter(afternoonStartTime)
                        && nowTime.isBefore(nightStartTime)) {
                    return true;
                }
                else {
                    return false;
                }
            case "N":
                if (nowTime.isAfter(nightStartTime)
                        || nowTime.isBefore(morningStartTime)) {
                    return true;
                }
                else {
                    return false;
                }
            default:
                return false;
        }
    }

    @Override
    public synchronized List<EnvironmentalStation> getStations() {
        return new ArrayList<>(stationsById.values());
    }

    @Override
    public String toString() {
        return getClass().getName() + "(" + urlStationsData + " " + urlStationsPosition
                + ")";
    }

    /**
     * Note that the JSON being passed in here is for configuration of the OTP component, it's completely separate
     * from the JSON coming in from the update source.
     */
    @Override
    public void configure(Graph graph, JsonNode jsonNode) {
        String urlStationsData = jsonNode.path("urlStationsData")
                .asText(); // path() returns MissingNode not null.
        String urlStationsPosition = jsonNode.path("urlStationsPosition")
                .asText(); // path() returns MissingNode not null.
        if (urlStationsData == null) {
            throw new IllegalArgumentException(
                    "Missing mandatory 'url' configuration.");
        }
        this.urlStationsData = urlStationsData;
        if (urlStationsPosition == null) {
            throw new IllegalArgumentException(
                    "Missing mandatory 'url' configuration.");
        }
        this.urlStationsPosition = urlStationsPosition;
    }
}
