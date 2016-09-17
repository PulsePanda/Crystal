package Shard;

import Utilities.APIHandler;
import java.util.Calendar;
import javax.swing.JOptionPane;
import org.json.JSONArray;
import org.json.JSONObject;

public class Command {

    private APIHandler api;

    /**
     *
     */
    public Command() {

    }

    /**
     * Built to analyze any command given, to determine what to do with it. For
     * now, it will directly handle it without interpretation, because commands
     * are given with GUI buttons
     *
     * @param c command given to analyze
     */
    public void AnalyzeCommand(String c) {
        switch (c) {
            case "Good Morning":
                goodMorning();
                break;
            case "Weather":
                weather();
                break;
            default:
                break;
        }
    }

    private void goodMorning() {
        System.out.println("Command Given: Good Morning");

        // Bitcoin price
        api = new APIHandler("https://blockchain.info/ticker");
        Double btcPrice = api.getJSONObject("USD").getDouble("buy");

        // Current Weather
        api.loadFromURL("http://api.openweathermap.org/data/2.5/weather?id=5275191&appid=70546178bd3fbec19e717d754e53b129");
        String wCoverage = api.getJSONArray("weather").getJSONObject(0).getString("description");
        int wTemp = (int) kelvinToF(api.getJSONObject("main").getDouble("temp"));
        JOptionPane.showMessageDialog(null, "Good Morning.\n\nBTC Price: $" + btcPrice + ".\n\nCurrent Weather: " + wCoverage + " and " + wTemp + " degrees.\n");
    }

    private void weather() {
        api = new APIHandler("http://api.openweathermap.org/data/2.5/forecast?id=5275191&appid=70546178bd3fbec19e717d754e53b129");
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

            String weather = obj.getJSONArray("weather").getJSONObject(0).getString("description");
            int temp = (int) kelvinToF(obj.getJSONObject("main").getDouble("temp"));

            String calendarMonth = getCalendarMonth(date.get(Calendar.MONTH));

            forecast.append("Date: " + calendarMonth + " " + date.get(Calendar.DAY_OF_MONTH) + "\nSky: " + weather + "\nTemperature: " + temp + "\n\n");
        }

        JOptionPane.showMessageDialog(null, forecast.toString());
    }

    private double kelvinToF(double kelvin) {
        return 1.8 * (kelvin - 273) + 32;
    }

    private String getCalendarMonth(int a) {
        String calendarMonth = "";
        switch (a) {
            case 0:
                calendarMonth = "Jan";
                break;
            case 1:
                calendarMonth = "Feb";
                break;
            case 2:
                calendarMonth = "Mar";
                break;
            case 3:
                calendarMonth = "Apr";
                break;
            case 4:
                calendarMonth = "May";
                break;
            case 5:
                calendarMonth = "Jun";
                break;
            case 6:
                calendarMonth = "Jul";
                break;
            case 7:
                calendarMonth = "Aug";
                break;
            case 8:
                calendarMonth = "Sep";
                break;
            case 9:
                calendarMonth = "Oct";
                break;
            case 10:
                calendarMonth = "Nov";
                break;
            case 11:
                calendarMonth = "Dec";
                break;
        }
        return calendarMonth;
    }
}
