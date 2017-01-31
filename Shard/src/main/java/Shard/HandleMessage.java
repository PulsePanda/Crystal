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
                String mediaPath = packet.packetStringArray[0];
                try {
                    new MediaPlayback().start(new Music(mediaPath));
                } catch (MediaStartException e) {
                    System.err.println("Error starting music playback. Details: " + e.getMessage());
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                JOptionPane.showMessageDialog(null, "Heart was unable to find a song matching that name!");
                System.err.println("Heart was unable to find a song matching that name!");
            }
        } else {
            JOptionPane.showMessageDialog(null, message);
        }
    }
}
