/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shard;

import javax.swing.JOptionPane;

/**
 * This class handles messages received from the Heart.
 *
 * @author Austin
 */
public class HandleMessage {

    private String message;

    /**
     * Default constructor
     *
     * Currently only shows the results of the message in a JOptionPane Message
     * box
     *
     * @param message String message to be handled from Heart.
     */
    public HandleMessage(String message) {
        this.message = message;

        if (message.startsWith("version:")) {
            String[] split = message.split(":");
            String version = split[1];
            Shard_Core.SHARD_VERSION_SERVER = version;
            Shard_Core.GetShardCore().notify();
        } else {
            JOptionPane.showMessageDialog(null, message);
        }
    }
}
