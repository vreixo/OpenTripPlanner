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

package org.opentripplanner.updater.bike_rental;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.opentripplanner.routing.bike_rental.BikeRentalStation;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.updater.PreferencesConfigurable;
import org.opentripplanner.util.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.prefs.Preferences;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public abstract class BiciCorunaXmlPostBikeRentalDataSource implements BikeRentalDataSource, PreferencesConfigurable {

    private static final Logger log = LoggerFactory.getLogger(BixiBikeRentalDataSource.class);

    private String url;

    ArrayList<BikeRentalStation> stations = new ArrayList<BikeRentalStation>();

    private XPathExpression xpathExpr;

    public BiciCorunaXmlPostBikeRentalDataSource(String path) {
        XPathFactory factory = XPathFactory.newInstance();

        XPath xpath = factory.newXPath();

        xpath.setNamespaceContext(new NamespaceContext() {

            public String getNamespaceURI(String prefix) {
                if (prefix.equals("soap")) return "http://www.w3.org/2003/05/soap-envelope";
                else if (prefix.equals("xsi")) return "http://www.w3.org/2001/XMLSchema-instance";
                else if (prefix.equals("xsd")) return "http://www.w3.org/2001/XMLSchema";
                else if (prefix.equals("")) return "http://aparcabicis.nextgal.es/";
                else return XMLConstants.NULL_NS_URI;
            }

            public String getPrefix(String namespace) {
                if (namespace.equals("http://www.w3.org/2003/05/soap-envelope")) return "soap";
                else if (namespace.equals("http://www.w3.org/2001/XMLSchema-instance")) return "xsi";
                else if (namespace.equals("http://www.w3.org/2001/XMLSchema")) return "xsd";
                else if (namespace.equals("http://aparcabicis.nextgal.es/")) return "";
                else return null;
            }

            public Iterator getPrefixes(String namespace) {return null;}

        });
        try {
            xpathExpr = xpath.compile(path);
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean update() {
        try {
            Header[] headers = new Header[1];
            Header header = new BasicHeader("Content-Type", "application/soap+xml");
            headers[0] = header;
            HttpEntity postData = new StringEntity(
                    "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                    "<soap12:Envelope " +
                        "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                        "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
                        "xmlns:soap12=\"http://www.w3.org/2003/05/soap-envelope\">\n" +
                        "<soap12:Body>\n" +
                            "<GetEstaciones xmlns=\"http://aparcabicis.nextgal.es/\" />\n" +
                        "</soap12:Body>\n" +
                    "</soap12:Envelope>");
            InputStream data = HttpUtils.getDataPost(url, headers, postData);
            if (data == null) {
                log.warn("Failed to get data from url " + url);
                return false;
            }
            parseXML(data);
        } catch (IOException e) {
            log.warn("Error reading bike rental feed from " + url, e);
            return false;
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            log.warn("Error parsing bike rental feed from " + url + "(bad XML of some sort)", e);
            return false;
        }
        return true;
    }

    private void parseXML(InputStream data) throws ParserConfigurationException, SAXException,
            IOException {
        ArrayList<BikeRentalStation> out = new ArrayList<BikeRentalStation>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true); // never forget this!
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(data);

        NodeList nodes;
        try {
            Object result = xpathExpr.evaluate(doc, XPathConstants.NODESET);
            nodes = (NodeList) result;
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }

        for (int i = 0; i < nodes.getLength(); ++i) {
            Node node = nodes.item(i);
            if (!(node instanceof Element)) {
                continue;
            }
            HashMap<String, String> attributes = new HashMap<String, String>();
            Node child = node.getFirstChild();
            while (child != null) {
                if (!(child instanceof Element)) {
                    child = child.getNextSibling();
                    continue;
                }
                attributes.put(child.getNodeName(), child.getTextContent());
                child = child.getNextSibling();
            }
            BikeRentalStation brstation = makeStation(attributes);
            if (brstation != null)
                out.add(brstation);
        }
        synchronized(this) {
            stations = out;
        }
    }

    @Override
    public synchronized List<BikeRentalStation> getStations() {
        return stations;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public abstract BikeRentalStation makeStation(Map<String, String> attributes);

    @Override
    public String toString() {
        return getClass().getName() + "(" + url + ")";
    }
    
    @Override
    public void configure(Graph graph, Preferences preferences) {
        String url = preferences.get("url", null);
        if (url == null)
            throw new IllegalArgumentException("Missing mandatory 'url' configuration.");
        setUrl(url);
    }
}
