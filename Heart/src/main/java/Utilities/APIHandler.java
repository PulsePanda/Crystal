/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utilities;

import java.net.URL;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class APIHandler {

    private JSONObject jobj;
    private String url;

    public APIHandler(String URL) {
        url = URL;
        loadFromURL(url);
    }

    public void loadFromURL(String URL) {
        url = URL;
        try {
            jobj = getJSONfromURL(URL);
        } catch (Exception ex) {
            Logger.getLogger(APIHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private JSONObject getJSONfromURL(String URL) throws Exception {

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

    public String getStringItem(String key) {
        return jobj.getString(key);
    }

    public int getIntItem(String key) {
        return jobj.getInt(key);
    }

    public JSONArray getJSONArray(String key) {
        return jobj.getJSONArray(key);
    }

    public JSONObject getJSONObject(String key) {
        return jobj.getJSONObject(key);
    }

    public void printObject() {
        System.out.println(jobj.toString());
    }

    public String getCurrentURL() {
        return url;
    }
}
