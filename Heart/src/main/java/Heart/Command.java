package Heart;

import Netta.Connection.Packet;
import Netta.Exceptions.SendPacketException;
import Utilities.APIHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Calendar;

public class Command {

    private APIHandler api;
    private ClientConnection connection;

    /**
     * Default Constructor
     *
     * @param connection ClientConnection object to be used for sending packets to the
     *                   Shard
     */
    public Command(ClientConnection connection) {
        this.connection = connection;
    }

    /**
     * Analyze given packet, and determine what to do with it
     *
     * @param packet Packet to analyze
     */
    public void AnalyzeCommand(Packet packet) throws SendPacketException {
        String c = packet.packetString;
        System.out.println("Received command from Shard. Command: " + c);

        switch (c) {
            case "Good Morning":
                try {
                    goodMorning();
                } catch (IOException e) {
                    System.err.println("Error retrieving API information for goodMorning command!");
                    // TODO have error packet sent to shard
                }
                break;
            case "BTC Price":
                try {
                    btcPrice();
                } catch (IOException e) {
                    System.err.println("Error retrieving API information for btcPrice command!");
                    // TODO have error packet sent to shard
                }
                break;
            case "Weather":
                try {
                    weather();
                } catch (IOException e) {
                    System.err.println("Error retrieving API information for weather command!");
                    // TODO have error packet sent to shard
                }
                break;
            case "Patch":
                byte[] file = null;
                try {
                    file = Files.readAllBytes(Paths.get(Heart_Core.baseDir + "patch/Shard.zip"));
                } catch (IOException e1) {
                    System.err.println("Error reading Shard.zip to send to shards. Aborting.");
                    return;
                }
                // dummy packet to allow client's readpacket to reset
                Packet blank = new Packet(Packet.PACKET_TYPE.NULL, null);
                sendToClient(blank, true);

                Packet p = new Packet(Packet.PACKET_TYPE.Message, null);
                p.packetString = "update";
                p.packetByteArray = file;
                System.out.println("Sending patch to Shard...");
                sendToClient(p, false);
                System.out.println("Sent patch to Shard.");

                // second dummy packet
                Packet blank2 = new Packet(Packet.PACKET_TYPE.NULL, null);
                sendToClient(blank2, false);
                break;
            case "get Shard Version":
                System.out.println("Shard requested version information.");
                sendToClient("version:" + Heart_Core.SHARD_VERSION, true);
                break;
            case "Play Music":
                System.out.println("Shard requested to play music. Song Name: " + packet.packetStringArray[0]);
                Packet music = new Packet(Packet.PACKET_TYPE.Message, null);
                music.packetString = "music";
                String requestedSong = packet.packetStringArray[0];
                String[] musicPaths = Heart_Core.GetCore().getMediaManager().getSong(requestedSong);
                for (int i = 0; i < musicPaths.length; i++) { // edit each path to be a reachable address
                    try {
                        musicPaths[i] = "file://" + InetAddress.getLocalHost().getHostName() + musicPaths[i];
                    } catch (UnknownHostException e) {
                        System.err.println("Error getting local hostname! Unable to provide correct path for Shard music playback!");
                    }
                }
                music.packetStringArray = musicPaths;
                sendToClient(music, true);
                break;
            case "Play Movie":
                System.out.println("Shard requested to play a movie. Movie Name: " + packet.packetStringArray[0]);
                Packet movie = new Packet(Packet.PACKET_TYPE.Message, null);
                movie.packetString = "movie";
                String requestedMovie = packet.packetStringArray[0];
                String[] moviePaths = Heart_Core.GetCore().getMediaManager().getMovie(requestedMovie);
                for (int i = 0; i < moviePaths.length; i++) {
                    try {
                        moviePaths[i] = "file://" + InetAddress.getLocalHost().getHostName() + moviePaths[i];
                    } catch (UnknownHostException e) {
                        System.err.println("Error getting local hostname! Unable to provide correct path for Shard movie playback!");
                    }
                }
                movie.packetStringArray = moviePaths;
                sendToClient(movie, true);
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
     * @throws SendPacketException thrown if there is an issue sending the packet to the Shard.
     *                             Details will be in the getMessage()
     * @throws IOException         thrown if there is an error with APIHandler
     */
    private void goodMorning() throws SendPacketException, IOException {
        System.out.println("Shard requested Good Morning info.");

        // Bitcoin price
        api = new APIHandler("https://blockchain.info/ticker");
        Double btcPrice = api.getJSONObject("USD").getDouble("buy");

        // Current Weather
        api.loadFromURL(
                "http://api.openweathermap.org/data/2.5/weather?id=5275191&appid=70546178bd3fbec19e717d754e53b129");
        String wCoverage = api.getJSONArray("weather").getJSONObject(0).getString("description");
        int wTemp = (int) kelvinToF(api.getJSONObject("main").getDouble("temp"));
        sendToClient("Good Morning.\n\nBTC Price: $" + btcPrice + ".\n\nCurrent Weather: " + wCoverage + " and " + wTemp
                + " degrees.\n", true);
    }

    /**
     * BTC Price command. Pulls the current bitcoin price data from
     * Blockchain.info and sends the data to the Shard.
     *
     * @throws SendPacketException thrown if there is an error sending the packet to the Shard.
     *                             Details will be in the getMessage()
     * @throws IOException         thrown if there is an error with APIHandler
     */
    private void btcPrice() throws SendPacketException, IOException {
        System.out.println("Shard requested BTC Price info.");

        api = new APIHandler("https://blockchain.info/ticker");
        Double btcPrice = api.getJSONObject("USD").getDouble("buy");

        sendToClient("BTC Price today: $" + btcPrice.toString(), true);
    }

    /**
     * Weather command. Pulls the 5 day weather forecast from
     * openweathermap.org, formats it into info about sky conditions and temp,
     * and sends the packet to the Shard.
     *
     * @throws SendPacketException thrown if there is an issue sending the packet to the Shard.
     *                             Details will be in the getMessage()
     * @throws IOException         thrown if there is an error with API handler
     */
    private void weather() throws SendPacketException, IOException {
        System.out.println("Shard requested Weather info.");
        api = new APIHandler(
                "http://api.openweathermap.org/data/2.5/forecast?id=5275191&appid=70546178bd3fbec19e717d754e53b129");
        StringBuilder forecast = new StringBuilder();

        JSONArray array = api.getJSONArray("list");
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);

            // Filter out every 3 hour value other than the mid day
            if (!obj.getString("dt_txt").contains("12:00:00")) {
                continue;
            }

            int apiDate = obj.getInt("dt"); // Date pulled from forecast

            // set up the calendar object for the given date
            Calendar date = Calendar.getInstance();
            java.util.Date time = new java.util.Date((long) apiDate * 1000);
            date.setTime(time);

            String weather = obj.getJSONArray("weather").getJSONObject(0).getString("description");
            int temp = (int) kelvinToF(obj.getJSONObject("main").getDouble("temp"));

            String calendarMonth = getCalendarMonth(date.get(Calendar.MONTH));

            forecast.append("Date: " + calendarMonth + " " + date.get(Calendar.DAY_OF_MONTH) + "\nSky: " + weather
                    + "\nTemperature: " + temp + "\n\n");
        }

        sendToClient(forecast.toString(), true);
    }

    /**
     * Send a string to the Shard.
     *
     * @param s String being sent to the Shard
     * @throws SendPacketException thrown if there is an issue sending the packet to the Shard.
     *                             Details will be in the getMessage()
     */
    private void sendToClient(String s, boolean encrypted) throws SendPacketException {
        Packet p = new Packet(Packet.PACKET_TYPE.Message, null);
        p.packetString = s;
        connection.SendPacket(p, encrypted);
    }

    /**
     * Send a packet to the Shard.
     *
     * @param p         Packet being sent to the shard
     * @param encrypted
     * @throws SendPacketException thrown if there is an issue sending the packet to the Shard.
     *                             Details will be in the getMessage()
     */
    private void sendToClient(Packet p, boolean encrypted) throws SendPacketException {
        connection.SendPacket(p, encrypted);
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
