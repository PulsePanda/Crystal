/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shard;

import javax.swing.JOptionPane;

import Netta.Connection.Packet;

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
	 *
	 * Currently only shows the results of the message in a JOptionPane Message
	 * box
	 *
	 * @param message
	 *            String message to be handled from Heart.
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
			ShardPatcher patcher = new ShardPatcher(Shard_Core.GetShardCore().getClient(), ShardPatcher.PATCHER_TYPE.runUpdate);
			patcher.start();
		} else {
			JOptionPane.showMessageDialog(null, message);
		}
	}
}
