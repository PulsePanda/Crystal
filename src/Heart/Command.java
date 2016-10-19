package Heart;

import Netta.Connection.Packet;
import Netta.Exceptions.SendPacketException;
import Shard.Shard_Core;
import Utilities.APIHandler;
import java.util.Calendar;
import org.json.JSONArray;
import org.json.JSONObject;

public class Command {

    private APIHandler api;
    private ClientConnection connection;

    /**
     * Default Constructor
     *
     * @param connection ClientConnection object to be used for sending packets
     * to the Shard
     */
    public Command(ClientConnection connection) {
        this.connection = connection;
    }

    /**
     * Built to analyze any command given, to determine what to do with it. For
     * now, it will directly handle it without interpretation, because commands
     * are given with GUI buttons
     *
     * @param c command given to analyze
     */
    public void AnalyzeCommand(String c) throws SendPacketException {
        System.out.println("Received command from Shard.");
        switch (c) {
            case "Good Morning":
                goodMorning();
                break;
            case "BTC Price":
                btcPrice();
                break;
            case "Weather":
                weather();
                break;
            case "Patch":

                break;
            case "Get Shard Version":
                System.out.println("Shard requested version information.");
                sendToClient("version:" + Heart_Core.SHARD_VERSION);
                break;
            default:
                break;
        }
    }

    /**
     * Good Morning command. Pulls the current Bitcoin price data from
     * Blockchain.info. Pulls the current weather from openweathermap.org. Then
     * formats the information into a string, and sends that string to the Shard
     *
     * @throws SendPacketException thrown if there is an issue sending the
     * packet to the Shard. Details will be in the getMessage()
     */
    private void goodMorning() throws SendPacketException {
        System.out.println("Shard requested Good Morning info.");

        // Bitcoin price
        api = new APIHandler("https://blockchain.info/ticker");
        Double btcPrice = api.getJSONObject("USD").getDouble("buy");

        // Current Weather
        api.loadFromURL("http://api.openweathermap.org/data/2.5/weather?id=5275191&appid=70546178bd3fbec19e717d754e53b129");
        String wCoverage = api.getJSONArray("weather").getJSONObject(0).getString("description");
        int wTemp = (int) kelvinToF(api.getJSONObject("main").getDouble("temp"));
        sendToClient("Good Morning.\n\nBTC Price: $" + btcPrice + ".\n\nCurrent Weather: " + wCoverage + " and " + wTemp + " degrees.\n");
    }

    /**
     * BTC Price command. Pulls the current bitcoin price data from
     * Blockchain.info and sends the data to the Shard.
     *
     * @throws SendPacketException thrown if there is an error sending the
     * packet to the Shard. Details will be in the getMessage()
     */
    private void btcPrice() throws SendPacketException {
        System.out.println("Shard requested BTC Price info.");

        api = new APIHandler("https://blockchain.info/ticker");
        Double btcPrice = api.getJSONObject("USD").getDouble("buy");

        sendToClient("BTC Price today: $" + btcPrice.toString());
    }

    /**
     * Weather command. Pulls the 5 day weather forecast from
     * openweathermap.org, formats it into info about sky conditions and temp,
     * and sends the packet to the Shard.
     *
     * @throws SendPacketException thrown if there is an issue sending the
     * packet to the Shard. Details will be in the getMessage()
     */
    private void weather() throws SendPacketException {
        System.out.println("Shard requested Weather info.");
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

        sendToClient(forecast.toString());
    }

    /**
     * Sends the string generated from each command to the Shard.
     *
     * @param s string being sent to the Shard
     * @throws SendPacketException thrown if there is an issue sending the
     * packet to the Shard. Details will be in the getMessage()
     */
    private void sendToClient(String s) throws SendPacketException {
        Packet p = new Packet(Packet.PACKET_TYPE.Message, null);
        p.packetString = s;
        connection.SendPacket(p);
    }

    /**
     * Helper method, converts kelvin to Fahrenheit.
     *
     * @param kelvin double value of temperature in kelvin
     * @return double in Fahrenheit
     */
    private double kelvinToF(double kelvin) {
        return 1.8 * (kelvin - 273) + 32;
    }

    /**
     * Helper method, converts Calendar month value into a string
     *
     * @param a int value of the Calendar month
     * @return String with month abbreviation
     */
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
