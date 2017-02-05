/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shard;

import Exceptions.MediaStartException;
import Netta.Connection.Packet;
import Netta.Exceptions.ConnectionException;
import Utilities.Media.MediaPlayback;
import Utilities.Media.Movie;
import Utilities.Media.Music;

import javax.swing.*;

/**
 * This class handles messages received from the Heart.
 *
 * @author Austin
 */
public class HandlePacket {

    private String message;
    private Packet packet;
    private Client client;

    /**
     * Default constructor
     * <p>
     * Currently only shows the results of the message in a JOptionPane Message
     * box
     *
     * @param packet Packet to handle
     * @param client Client to access for handling the packet request
     */
    public HandlePacket(Packet packet, Client client) {
        this.message = packet.packetString;
        this.packet = packet;
        this.client = client;
        System.out.println(packet.packetType.toString() + " from server: " + message);
        handle();
    }

    /**
     * Handle the packet
     */
    private void handle() {
        switch (packet.packetType.toString().toLowerCase()) {
            case "message":
                if (message.startsWith("version:")) {
                    String[] split = message.split(":");
                    String version = split[1];
                    Shard_Core.SHARD_VERSION_SERVER = version;
                } else if (message.equals("update")) {
                    Shard_Core.getShardCore().getPatcher().updateFile = packet.packetByteArray;
                } else if (message.equals("new patch")) {
                    ShardPatcher patcher = new ShardPatcher(Shard_Core.getShardCore().getClient(),
                            ShardPatcher.PATCHER_TYPE.downloadUpdate);
                    patcher.start();
                } else if (message.equals("music")) {
                    try {
                        String songPath = packet.packetStringArray[0];
                        System.out.println(songPath);
                        try {
                            new MediaPlayback().start(new Music(songPath));
                        } catch (MediaStartException e) {
                            System.err.println("Error starting music playback. Details: " + e.getMessage());
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        JOptionPane.showMessageDialog(null, "Heart was unable to find a song matching that name!");
                        System.err.println("Heart was unable to find a song matching that name!");
                    }
                } else if (message.equals("movie")) {
                    try {
                        String moviePath = packet.packetStringArray[0];
                        System.out.println(moviePath);
                        try {
                            new MediaPlayback().start(new Movie(moviePath));
                        } catch (MediaStartException e) {
                            System.err.println("Error starting music playback. Details: " + e.getMessage());
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        JOptionPane.showMessageDialog(null, "Heart was unable to find a movie matching that name!");
                        System.err.println("Heart was unable to find a movie matching that name!");
                    }
                } else {
                    JOptionPane.showMessageDialog(null, message);
                }
                break;
            case "close connection":
                System.out.println(
                        "Server requested connection termination. Reason: " + packet.packetString + ". Closing connection.");
                try {
                    client.closeIOStreams();
                    Shard_Core.getShardCore().resetConnectionData();
                } catch (ConnectionException e) {
                    System.err.println("Error closing connection with Heart. Error: " + e.getMessage());
                }
                break;
        }
    }
}
