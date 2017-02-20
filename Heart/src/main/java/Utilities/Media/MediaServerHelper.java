/*
 * This file is part of Crystal Home Systems.
 *
 * Crystal Home Systems is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Crystal Home Systems is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Crystal Home Systems. If not, see http://www.gnu.org/licenses/.
 */

package Utilities.Media;

import Heart.ClientConnection;
import Heart.Heart_Core;
import Netta.Connection.Packet;
import Netta.Connection.Server.MediaServer;
import Netta.Connection.Server.SingleClientServer;
import Netta.Exceptions.ConnectionException;
import Netta.Exceptions.SendPacketException;
import Utilities.Media.Exceptions.ServerHelperException;

import java.io.*;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Austin on 2/18/2017.
 */
public class MediaServerHelper {

    protected ListItem media;
    private int mediaServerPort = 9490;
    private ServerHelper serverHelper;
    private Thread serverHelperThread;
    private ClientConnection client;
    private File mediaFile;

    public MediaServerHelper(ListItem media, ClientConnection client) throws ServerHelperException {
        this.media = media;
        this.client = client;

        Packet packet = new Packet(Packet.PACKET_TYPE.Message, Heart_Core.getCore().getUUID().toString());
        packet.packetString = "media server";

        // Verify the media file is a valid file
        mediaFile = new File(media.getPath());
        if (!mediaFile.exists() || !mediaFile.isFile()) {
            throw new ServerHelperException("MediaServer: Invalid music file. File does not exist!\nMediaServer: Aborting Media Server.");
        }

        // Create server
        try {
            System.out.println("Starting media server for media file " + media.getName());
            System.out.println("Attempting to host media server on port " + mediaServerPort);
            serverHelper = new ServerHelper(mediaServerPort, this, mediaFile);
            serverHelperThread = new Thread(serverHelper);
            serverHelperThread.start();

//            try {
//                Thread.sleep(3000);
//            } catch (InterruptedException e) {
//            }
//
//            while (!serverHelper.isServerActive()) {
//                System.out.println("Attempting to host media server on port " + ++mediaServerPort);
//                stopMediaServer();
//
//                serverHelper = new ServerHelper(mediaServerPort, this, mediaFile);
//                serverHelperThread = new Thread(serverHelper);
//                serverHelperThread.start();
//
//                if (!serverHelper.isServerActive())
//                    try {
//                        Thread.sleep(3000);
//                    } catch (InterruptedException e) {
//                    }
//            }

            packet.packetInt = mediaServerPort;
            packet.packetStringArray = new String[]{media.getPath()};
            client.sendPacket(packet, true);
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Error starting Media Server. Error creating Kript object! Attempt has been aborted.");
            stopMediaServer();
            serverHelper = null;
        } catch (SendPacketException e) {
            System.err.println("Error sending media server information packet to Shard! Details: " + e.getMessage() + "\n Aborting media server.");
            stopMediaServer();
        } catch (ServerHelperException e) {
            System.err.println(e.getMessage());
            stopMediaServer();
        }
    }

    public void stopMediaServer() {
        try {
            serverHelper.closeIOStreams();
        } catch (ConnectionException e) {
        } catch (NullPointerException e) {
        }
        try {
            serverHelper.closeServer();
        } catch (IOException e) {
        } catch (NullPointerException e) {
        }

        try {
            serverHelperThread.stop();
            serverHelperThread = null;
        } catch (NullPointerException e) {
        }

        serverHelper = null;
    }

    public ClientConnection getClientConnection() {
        return client;
    }
}

class ServerHelper extends MediaServer {

    public ServerHelper(int port, MediaServerHelper mediaServerHelper, File mediaFile) throws NoSuchAlgorithmException, ServerHelperException {
        super(port, mediaFile);
    }
}
