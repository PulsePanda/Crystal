/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utilities;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

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
