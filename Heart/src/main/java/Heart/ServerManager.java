/*
 * This file is part of Crystal Home Systems.
 *
 * Crystal Home Systems is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Crystal Home Systems is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Crystal Home Systems. If not, see http://www.gnu.org/licenses/.
 */

package Heart;

import Exceptions.ServerInitializationException;
import Netta.DNSSD;
import Utilities.Media.Exceptions.ServerHelperException;
import Utilities.Media.ListItem;
import Utilities.Media.MediaServerHelper;
import Utilities.SystemInfo;

import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class ServerManager {

    private Heart_Core c;
    public int heartPort;
    protected Server server = null;
    private Thread serverThread = null;
    protected ArrayList<MediaServerHelper> mediaServerArrayList;
    private DNSSD dnssd;

    public ServerManager(Heart_Core heart_core) {
        c = heart_core;
    }

    /**
     * Starts up the Server for this Heart Core object.
     *
     * @throws ServerInitializationException if there is an error creating the server for any reason. If
     *                                       the exception is thrown, abort attempt to create server
     */
    public void startHeartServer(int port) throws ServerInitializationException {
        heartPort = port;
        try {
            // If the server object already has been initialized, or the server object has active connection
            if (server != null || server.isServerActive()) {
                throw new ServerInitializationException(
                        "Server is already initialized. Cannot create new server on this object. Aborting creation.");
            }
        } catch (NullPointerException e) {
            // If server is not set, this will execute.
            try {
                server = new Server(port);
            } catch (NoSuchAlgorithmException e1) {
                throw new ServerInitializationException(
                        "Unable to initialize server. Likely an issue loading RSA cipher. Aborting creation.");
            }
        }
        try {
            // If the server thread is initialized or alive
            if (serverThread != null || serverThread.isAlive()) {
                System.err.println(
                        "This instance of Heart Core already has an active Server Thread. Attempting to close the thread...");
                // Try to close the server thread
                // TODO using a depreciated method to stop the server, not necessarily the best option
                serverThread.stop();
            }
        } catch (NullPointerException e) {
            // If serverThread is not set, this will throw.
            // That's fine, we don't need to do anything
        }

        // Start the server
        serverThread = new Thread(server);
        serverThread.start();
    }

    /**
     * Start the server the Shard will connect to allowing for Media Streaming
     *
     * @param media  String chosen media URL
     * @param client ClientConnection Shard client object requesting media playback
     * @throws ServerHelperException Thrown when there is an issue creating the MediaServerHelper.
     *                               Details will be in the getMessage()
     */
    public void startMediaServer(ListItem media, ClientConnection client) throws ServerHelperException {
        MediaServerHelper mediaServer = new MediaServerHelper(media, client);
        client.setMediaServer(mediaServer);
        mediaServerArrayList.add(mediaServer);
    }


    /**
     * Initialize and register the DNS_SD for the server
     */
    public void initDNSSD() {
        dnssd = new DNSSD();
        try {
            dnssd.registerService("_http._tcp.local.", "Crystal Heart Server", heartPort,
                    "Heart Core Server DNS Service", SystemInfo.getSystemLocalIP());
        } catch (UnknownHostException e) {
            System.err.println("DNSSD: Unable to initialize DNSSD Service. Unknown Host");
        }
    }

    /**
     * Stop the Heart server.
     * <p>
     * Unregisters the DNS_SD service on the network
     * Closes server connections
     * Stops the server thread
     * Closes the Media Manager
     * Stops the Update Checker
     * Sets all variables to null
     */
    @SuppressWarnings("deprecation")
    public void stopHeartServer() {
        for (MediaServerHelper ms : mediaServerArrayList) {
            ms.stopMediaServer();
            ms = null;
        }
        mediaServerArrayList = null;

        try {
            dnssd.closeRegisteredService();
        } catch (NullPointerException e) {
        }
        try {
            server.closeConnections();
        } catch (NullPointerException e) {
        }
        dnssd = null;
        server = null;

        try {
            serverThread.stop();
        } catch (NullPointerException e) {
        }
        serverThread = null;

        try {
            c.getMediaManager().close();
        } catch (NullPointerException e) {
        }

        try {
            c.updateCheckerThread.stop();
        } catch (NullPointerException e) {
        }
        c.updateCheckerThread = null;
    }

}
