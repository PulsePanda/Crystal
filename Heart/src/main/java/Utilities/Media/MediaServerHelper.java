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
import Netta.Exceptions.ConnectionException;
import Netta.Exceptions.SendPacketException;
import Utilities.Media.Exceptions.ServerHelperException;
import javazoom.jl.converter.Converter;
import javazoom.jl.decoder.JavaLayerException;

import java.io.File;
import java.io.IOException;
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
    private boolean converted = false;

    public MediaServerHelper(ListItem media, ClientConnection client) throws ServerHelperException {
        this.media = media;
        this.client = client;

        Packet packet = new Packet(Packet.PACKET_TYPE.Message, Heart_Core.getCore().getUUID().toString());
        packet.packetString = "media server";

        // Verify the media file is a valid file
        mediaFile = new File(media.getPath());
//        mediaFile = new File("C:/Users/Austin/Desktop/piano2.wav");
        if (!mediaFile.exists() || !mediaFile.isFile()) {
            throw new ServerHelperException("MediaServer: Invalid music file. File does not exist!\nMediaServer: Aborting Media Server.");
        }

        // Create server
        try {
            System.out.println("Starting media server for media file " + media.getName());
            System.out.println("Attempting to host media server on port " + mediaServerPort);

            // Convert mp3 file to wav for streaming
            if (mediaFile.getPath().contains(".mp3")) {
                mediaFile = convertMp3ToWave();
                converted = true;
                if (mediaFile == null) {
                    System.err.println("Error converting mp3 file to wav format. Aborting streaming.");
                    converted = false;
                    return;
                }
            }

            serverHelper = new ServerHelper(mediaServerPort, this, mediaFile);
            serverHelperThread = new Thread(serverHelper);
            serverHelperThread.start();

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
            }

            while (!serverHelper.isServerActive()) {
                System.out.println("Attempting to host media server on port " + ++mediaServerPort);
                stopMediaServer();

                serverHelper = new ServerHelper(mediaServerPort, this, mediaFile);
                serverHelperThread = new Thread(serverHelper);
                serverHelperThread.start();

                if (!serverHelper.isServerActive())
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                    }
            }

            packet.packetInt = mediaServerPort;
            packet.packetStringArray = new String[]{media.getPath()};
            client.sendPacket(packet, true);
            if (converted) {
                ConvertedDeleterHelper cdh = new ConvertedDeleterHelper(mediaFile, serverHelper);
                cdh.start();
            }
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

    private File convertMp3ToWave() {
        Converter converter = new Converter();
        try {
            System.out.println("Converting mediaFile from .mp3 to .wav for streaming...");
            converter.convert(mediaFile.getPath(), mediaFile.getPath().replace(".mp3", ".wav"));
            return new File(mediaFile.getPath().replace(".mp3", ".wav"));
        } catch (JavaLayerException e) {
            return null;
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

        if (converted) {
            try {
                mediaFile.delete();
            } catch (Exception e) {
            }
        }
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

class ConvertedDeleterHelper extends Thread {

    private File file;
    private ServerHelper helper;

    public ConvertedDeleterHelper(File file, ServerHelper helper) {
        this.file = file;
        this.helper = helper;
    }

    public void run() {
        while (helper.isStreaming()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }

        try {
            file.delete();
        } catch (Exception e) {
        }
    }
}
