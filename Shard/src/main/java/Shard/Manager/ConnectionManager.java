/*
 * This file is part of Crystal Home Systems.
 *
 * Crystal Home Systems is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Crystal Home Systems is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Crystal Home Systems. If not, see http://www.gnu.org/licenses/.
 */

package Shard.Manager;

import Exceptions.ClientInitializationException;
import Netta.Connection.Packet;
import Netta.DNSSD;
import Netta.Exceptions.ConnectionException;
import Netta.Exceptions.SendPacketException;
import Netta.ServiceEntry;
import Shard.Client;
import Shard.ShardConnectionThread;
import Shard.Shard_Core;
import Utilities.Media.Client.MediaClientHelper;
import Utilities.ShardPatcher;

import java.awt.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import static Shard.Manager.ConfigurationManager.systemName;

public class ConnectionManager {

    public static boolean patchReady = false;
    private Shard_Core c;
    private Client client = null;
    private Thread clientThread = null;
    private int port;
    private DNSSD dnssd;
    private MediaClientHelper mediaClient;
    //    public MediaPlayback mediaPlayback;
    private ShardPatcher patcher;
    private Thread mediaClientThread;
    private Thread shardConnectionThread;

    public ConnectionManager(Shard_Core shard_core) {
        c = shard_core;
        dnssd = new DNSSD();
//        mediaPlayback = new MediaPlayback();
    }

    /**
     * Start the Shards connection thread to connect to the Heart server. Tries to connect every 10 seconds.
     * If the connection is already active, nothing happens. Used to automatically reconnect to the Heart on
     * disconnect.
     */
    public void startConnectionThread() {
        ShardConnectionThread sct = new ShardConnectionThread(true, false);
        shardConnectionThread = new Thread(sct);
        shardConnectionThread.start();
    }


    /**
     * Used to start the Shard, create connection to it's Heart and initialize
     * the running thread.
     *
     * @throws ClientInitializationException thrown if there is an error creating the Client. Error
     *                                       details will be in the getMessage()
     */
    public void startShardClient() throws ClientInitializationException {
        try {
            if (client.isConnectionActive()) {
                throw new ClientInitializationException(
                        "Client is already initialized! Aborting attempt to create connection.");
            }
        } catch (NullPointerException e) {
            // If client is not remoteLoggingInitialized, initialize it
            try {
                // Start the search for dnssd service
                try {
                    System.out.println("Searching for DNS_SD service on local network.");
                    dnssd.discoverService("_http._tcp.local.", InetAddress.getLocalHost());
                } catch (UnknownHostException e1) {
                }

                ServiceEntry heartService = null;
                while (heartService == null) {
                    ArrayList<ServiceEntry> entries = dnssd.getServiceList();
                    for (ServiceEntry temp : entries) {
                        if (temp.getServiceName().equals("Crystal Heart Server"))
                            heartService = temp;
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                    }
                }

                // after search has finished, close the search
                dnssd.closeServiceDiscovery();

                // load the service info
                // service will be loaded as http://192.168.0.2:6666
                String serviceInfo = heartService.getServiceInfo();
                String[] serviceSplit = serviceInfo.split("http://");
                String ipPort = serviceSplit[1]; // removes http://
                String[] ipPortSplit = ipPort.split(":"); // splits IP and port
                c.getConfigurationManager().IP = ipPortSplit[0];
                port = Integer.parseInt(ipPortSplit[1]);

                client = new Client(c.getConfigurationManager().IP, port);
            } catch (NoSuchAlgorithmException e1) {
                throw new ClientInitializationException(
                        "Unable to initialize client. Likely an issue loading RSA cipher. Aborting creation.");
            }
        }

        try {
            if (clientThread != null || clientThread.isAlive()) {
                stopShardClient();
                throw new ClientInitializationException(
                        "Client Thread is already remoteLoggingInitialized! Aborting attempt to create connection.");
            }
        } catch (NullPointerException e) {
            // If the clientThread isn't remoteLoggingInitialized, do nothing
            // ((re)-initialize below)
        } catch (ConnectionException ex) {
        }

        System.out.println("Connecting to Heart. IP: " + c.getConfigurationManager().IP + " Port: " + port);
        clientThread = new Thread(client);
        clientThread.start();
    }

    public void connectToMediaServer(int mediaServerPort, String mediaType) {
        try {
            mediaClient = new MediaClientHelper(c.getConfigurationManager().IP, mediaServerPort, mediaType);
            mediaClientThread = new Thread(mediaClient);
            mediaClientThread.start();
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Error starting MediaClient. Kript was unable to set up encryption keys. Aborting creation.");
        }
    }

    @Deprecated
    public void startShardClientSuppressed() {
        try {
            startShardClient();
        } catch (ClientInitializationException ex) {
        }
    }

    /**
     * Used to stop the Shard nicely. Sends a close connection packet to the Heart to
     * terminate connection, which will then terminate IO streams
     *
     * @throws ConnectionException thrown when there is an issue closing the IO streams to the
     *                             Heart. Error will be in the getMessage()
     */
    public void stopShardClient() throws ConnectionException {
        c.getGuiManager().connectionStatus.setText("DISCONNECTED");
        c.getGuiManager().connectionStatus.setForeground(Color.RED);
        Packet p = new Packet(Packet.PACKET_TYPE.CloseConnection, c.getConfigurationManager().uuid.toString());
        try {
            p.packetString = "Manual disconnect";
            client.sendPacket(p, true);
        } catch (SendPacketException e) {
            System.err.println("Error sending disconnect packet to Heart. Error: " + e.getMessage());
        }
        client.closeIOStreams();
        clientThread.stop();
        clientThread = null;
        c.getConfigurationManager().IP = "";
        port = 0;

        try {
            p.packetString = "Manual disconnect";
            client.sendPacket(p, true);
        } catch (SendPacketException e) {
            System.err.println("Error sending disconnect packet to Heart. Error: " + e.getMessage());
        }
        mediaClient.closeIOStreams();
        mediaClientThread.stop();
        mediaClient = null;
        mediaClientThread = null;
    }

    /**
     * Patcher helper method. Initializes the Patcher class, checks if there is
     * an update to the Shard. GUI Elements will not be available until this
     * method is finished.
     * <p>
     * Called after clientThread is started
     */
    public synchronized void initPatcher() {
        if (!client.isConnectionActive()) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
            initPatcher();
            return;
        }

        ConfigurationManager.SHARD_VERSION_SERVER = "";

        // Check shard version
        patcher = new ShardPatcher(client, ShardPatcher.PATCHER_TYPE.checkVersion);
        patcher.start();
        while (ConfigurationManager.SHARD_VERSION_SERVER == "") {
            try {
                Thread.sleep(5);
            } catch (InterruptedException ex) {
            }
        }

        // download shard update (ShardPatcher will not download anything if
        // there's no update)
        patcher = new ShardPatcher(client, ShardPatcher.PATCHER_TYPE.downloadUpdate);
        patcher.start();
        while (patchReady == false) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException ex) {
            }
        }

        c.getGuiManager().connectionStatus.setText("CONNECTED");
        c.getGuiManager().connectionStatus.setForeground(Color.GREEN);
        ConfigurationManager.remoteLoggingInitialized = true;
        try {
            swapID();
        } catch (SendPacketException e) {
            System.err.println("Error sending Heart ID information. Details: " + e.getMessage());
        }

        // Run shard update
        patcher = new ShardPatcher(client, ShardPatcher.PATCHER_TYPE.runUpdate);
        patcher.start();
    }

    /**
     * Exchange ID information with the Heart
     *
     * @throws SendPacketException thrown if there is an issue sending packets to Heart.
     *                             Details will be in getMessage()
     */
    public void swapID() throws SendPacketException {
        Packet p = new Packet(Packet.PACKET_TYPE.Command, c.getConfigurationManager().uuid.toString());
        p.packetString = "uuid";
        p.packetStringArray = new String[]{systemName, ConfigurationManager.systemLocation};
        client.sendPacket(p, true);
    }


    /**
     * Used to stop the Shard without warning. Does the same thing as stopShardClient() without sending a packet to the
     * Heart
     */
    public void resetConnectionData() {
        c.getGuiManager().connectionStatus.setText("DISCONNECTED");
        c.getGuiManager().connectionStatus.setForeground(Color.RED);
        try {
            client.closeIOStreams();
        } catch (ConnectionException e) {
            System.err.println("Error closing client IO streams. Error: " + e.getMessage());
        }
        client = null;
        clientThread.stop();
        clientThread = null;
        c.getConfigurationManager().IP = "";
        port = 0;
        ConfigurationManager.remoteLoggingInitialized = false;
    }

    /**
     * Stop the Shard from continuously trying to connect to the Heart
     */
    public void stopShardConnectionThread() {
        shardConnectionThread.stop();
    }


    /**
     * Send a packet to the Heart
     *
     * @param p         Packet to send
     * @param encrypted boolean True to encrypt packet, else false
     * @throws SendPacketException thrown if there is an error sending the Packet to the Heart.
     *                             Error will be in the getMessage()
     */
    public void sendPacket(Packet p, boolean encrypted) throws SendPacketException {
        client.sendPacket(p, encrypted);
    }

    /**
     * Check whether the Shards connection to the Heart is active or not
     *
     * @return boolean whether the connection is active
     */
    public boolean isActive() {
        return client.isConnectionActive();
    }

    /**
     * get the Port being connected to by the Shard
     *
     * @return int Port being connected to
     */
    public int getPort() {
        return port;
    }


    /**
     * get the IP address being connected to by the Shard
     *
     * @return String IP address being connected to
     */
    public String getIP() {
        return c.getConfigurationManager().IP;
    }


    public ShardPatcher getPatcher() {
        return patcher;
    }

    public Client getClient() {
        return client;
    }
}
