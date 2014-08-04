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

package org.opentripplanner.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.Header;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

public class HttpUtils {

    public static InputStream getData(String url) throws ClientProtocolException, IOException {
        HttpGet httpget = new HttpGet(url);
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response = httpclient.execute(httpget);
        if(response.getStatusLine().getStatusCode() != 200)
            return null;

        HttpEntity entity = response.getEntity();
        if (entity == null) {
            return null;
        }
        InputStream instream = entity.getContent();
        return instream;
    }

    public static InputStream getDataPost(String url, Header[] headers,
                                               HttpEntity postData) throws IOException {
        HttpPost httppost = new HttpPost(url);
        for (Header h : headers){
            httppost.addHeader(h);
        }
        httppost.setEntity(postData);
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response = httpclient.execute(httppost);
        if(response.getStatusLine().getStatusCode() != 200)
            return null;

        HttpEntity entity = response.getEntity();
        if (entity == null) {
            return null;
        }
        return entity.getContent();
    }


    public static void testUrl(String url) throws ClientProtocolException, IOException {
        HttpHead head = new HttpHead(url);
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response = httpclient.execute(head);

        StatusLine status = response.getStatusLine();
        if (status.getStatusCode() == 404) {
            throw new FileNotFoundException();
        }

        if (status.getStatusCode() != 200) {
            throw new RuntimeException("Could not get URL: " + status.getStatusCode() + ": "
                    + status.getReasonPhrase());
        }
    }
}
