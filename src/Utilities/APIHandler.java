/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utilities;

import java.net.URL;
import java.util.Calendar;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
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

    public static void main(String[] args) {
        APIHandler api = new APIHandler("http://api.openweathermap.org/data/2.5/forecast?id=5275191&appid=70546178bd3fbec19e717d754e53b129");
        StringBuilder forecast = new StringBuilder();

        JSONArray array = api.getJSONArray("list");
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);

            // Filter out every 3 hour value other than the mid day
            if (!obj.getString("dt_txt").contains("12:00:00")) {
                continue;
            }

            int apiDate = obj.getInt("dt"); // Date pulled from forecast

            // Set up the calendar object for the given date
            Calendar date = Calendar.getInstance();
            java.util.Date time = new java.util.Date((long) apiDate * 1000);
            date.setTime(time);

            //String weather = obj.getJSONObject("weather").getString("main");
            String weather = obj.getJSONArray("weather").getJSONObject(0).getString("description");
            int temp = (int) kelvinToF(obj.getJSONObject("main").getDouble("temp"));
            forecast.append("Date: " + date.get(Calendar.MONTH) + " " + date.get(Calendar.DAY_OF_MONTH) + "\nCloud Cover: " + weather + "\nTemperature: " + temp + "\n\n");
        }

        JOptionPane.showMessageDialog(null, forecast.toString());
    }

    private static double kelvinToF(double kelvin) {
        return 1.8 * (kelvin - 273) + 32;
    }
}
