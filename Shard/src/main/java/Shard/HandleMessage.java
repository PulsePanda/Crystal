/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shard;

import javax.swing.JOptionPane;

import Exceptions.MediaStartException;
import Netta.Connection.Packet;
import Utilities.Media.MediaPlayback;
import Utilities.Media.Movie;
import Utilities.Media.Music;

/**
 * This class handles messages received from the Heart.
 *
 * @author Austin
 */
public class HandleMessage {

    private String message;
    private Packet packet;

    /**
     * Default constructor
     * <p>
     * Currently only shows the results of the message in a JOptionPane Message
     * box
     */
    public HandleMessage(Packet packet) {
        this.message = packet.packetString;
        this.packet = packet;
        System.out.println("Message from server: " + message);
        handle();
    }

    private void handle() {
        if (message.startsWith("version:")) {
            String[] split = message.split(":");
            String version = split[1];
            Shard_Core.SHARD_VERSION_SERVER = version;
            // Shard_Core.GetShardCore().InitPatcher();
        } else if (message.equals("update")) {
            Shard_Core.GetShardCore().getPatcher().updateFile = packet.packetByteArray;
        } else if (message.equals("new patch")) {
            ShardPatcher patcher = new ShardPatcher(Shard_Core.GetShardCore().getClient(),
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
    }
}
