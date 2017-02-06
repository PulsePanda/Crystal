
/*
 * This file is part of Crystal Home Systems.
 *
 * Crystal Home Systems is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Crystal Home Systems is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Crystal Home Systems. If not, see http://www.gnu.org/licenses/.
 */

package Utilities;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

/**
 * API manager. Wrapper to handle all JSON api's
 */
public class APIHandler {

    private JSONObject jobj;
    private String url;

    /**
     * APIHandler
     * <p>
     * Utility for handling remote API's
     *
     * @param URL API url
     * @throws IOException if the URL is invalid
     */
    public APIHandler(String URL) throws IOException {
        url = URL;
        loadFromURL(url);
    }

    /**
     * Load API data from URL. Called by default by the constructor
     *
     * @param URL API url
     * @throws IOException if URL is invalid
     */
    public void loadFromURL(String URL) throws IOException {
        url = URL;
        jobj = getJSONfromURL(URL);
    }

    /**
     * Get the JSON information from an API url
     *
     * @param URL API url
     * @return JSONObject JSON information object
     * @throws IOException thrown when unable to load from given URL
     */
    private JSONObject getJSONfromURL(String URL) throws IOException {

        URL url = new URL(URL);

        // read from the URL
        Scanner scan = new Scanner(url.openStream());
        String str = new String();
        while (scan.hasNext()) {
            str += scan.nextLine();
        }
        scan.close();

        return new JSONObject(str);
    }

    /**
     * Get a String item from the retrieved API
     *
     * @param key String item key
     * @return String data retrieved from key
     */
    public String getStringItem(String key) {
        return jobj.getString(key);
    }

    /**
     * Get an int item from the retrieved API
     *
     * @param key String item key
     * @return int data retrieved from key
     */
    public int getIntItem(String key) {
        return jobj.getInt(key);
    }

    /**
     * Get a JSONArray item from the retrieved API
     *
     * @param key String item key
     * @return JSONArray data retrieved from key
     */
    public JSONArray getJSONArray(String key) {
        return jobj.getJSONArray(key);
    }

    /**
     * Get a JSONObject item from the retrieved API
     *
     * @param key String item key
     * @return JSONObject data retrieved from key
     */
    public JSONObject getJSONObject(String key) {
        return jobj.getJSONObject(key);
    }

    @Deprecated
    public void printObject() {
        System.out.println(jobj.toString());
    }

    /**
     * Get the API URL currently being used
     *
     * @return String API URL
     */
    public String getCurrentURL() {
        return url;
    }
}
