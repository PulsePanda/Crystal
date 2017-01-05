/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shard;

import Netta.Connection.Packet;
import Netta.Exceptions.ConnectionException;
import Netta.Exceptions.SendPacketException;
import Utilities.UnZip;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Patcher class. Checks if there is an update to the Shard, and if there is
 * downloads the .zip from the server. From there, it unzips the download to the
 * home directory, and runs the ShardPatchInstaller.jar file.
 *
 * @author Austin
 */
public class ShardPatcher extends Thread {

	private Client client;
	private PATCHER_TYPE type;
	public byte[] updateFile = null;
	private String os;

	public ShardPatcher(Client client, PATCHER_TYPE type) {
		this.client = client;
		this.type = type;
		os = System.getProperty("os.name");
		os = os.split(" ")[0];
		os = os.toLowerCase();
	}

	@Override
	public void run() {
		if (type == PATCHER_TYPE.checkVersion) {
			checkVersionHelper();
		} else if (type == PATCHER_TYPE.downloadUpdate) {
			downloadUpdateHelper();
		} else if (type == PATCHER_TYPE.runUpdate) {
			runUpdateHelper();
		}
	}

	private void checkVersionHelper() {
		System.out.println("Initializing Patcher...");
		try {
			getVersion();
		} catch (SendPacketException ex) {
			System.err.println("Error getting Hearts version of the Shard. Error: " + ex.getMessage());
			System.err.println("Cancelling execution, unsafe to run unpatched Shard.");
			try {
				Shard_Core.GetShardCore().StopShardClient();
			} catch (ConnectionException ex1) {
			}
		}
	}

	public void getVersion() throws SendPacketException {
		System.out.println("Getting version from Heart.");
		Packet p = new Packet(Packet.PACKET_TYPE.Command, "");
		p.packetString = "Get Shard Version";
		try {
			Thread.sleep(2000);
		} catch (InterruptedException ex) {
		}
		client.SendPacket(p, true);
	}

	private void downloadUpdateHelper() {
		if (!Shard_Core.SHARD_VERSION.equals(Shard_Core.SHARD_VERSION_SERVER)) {
			System.out.println("Update required. Initializing update.");
			try {
				updateHelper();
			} catch (SendPacketException e) {
				System.err.println("Error sending update command to Heart. Details: " + e.getMessage());
				Shard_Core.patchReady = true;
				return;
			}
		} else {
			System.out.println("Shard is up to date!");
			Shard_Core.patchReady = true;
		}
		Shard_Core.patchReady = true;
	}

	private void updateHelper() throws SendPacketException {
		Packet p = new Packet(Packet.PACKET_TYPE.Command, null);
		p.packetString = "Patch";
		client.setPacketEncrypted(false);
		client.SendPacket(p, true);
		System.out.println("Requested patch from Heart.");

		while (updateFile == null) {
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
			}
		}
		client.setPacketEncrypted(true);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}

		try {
			System.out.println("Saving update file...");
			File file = new File(Shard_Core.shardDir + "Shard.zip");
			if (!file.exists())
				file.createNewFile();

			FileOutputStream fos = new FileOutputStream(Shard_Core.shardDir + "Shard.zip");
			fos.write(updateFile);
			fos.close();
			System.out.println("Update file saved.");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void runUpdateHelper() {
		if (!Shard_Core.SHARD_VERSION.equals(Shard_Core.SHARD_VERSION_SERVER)) {
			System.out.println("Installing update...");
			try {
				new UnZip(Shard_Core.shardDir + "Shard.zip", Shard_Core.baseDir).run();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
			}
			deleteDir(new File(Shard_Core.shardDir + "Shard.zip"));

			System.out.println("Launching new version of Shard.");
			try {
				// TODO: This is the install script for Shards
				if (os.equals("windows")) {
					// Runtime.getRuntime()
					// .exec(new String[] { "cmd", "/c", "start",
					// Shard_Core.shardDir + "bin/Shard.bat" });
					ProcessBuilder pb = new ProcessBuilder(Shard_Core.shardDir + "bin/Shard.bat");
					pb.start();
				} else if (os.equals("linux")) {
					// Runtime.getRuntime().exec(new String[] {});
					ProcessBuilder pb = new ProcessBuilder(Shard_Core.shardDir + "bin/Shard.sh");
					pb.start();
				}
			} catch (IOException e) {
				System.err.println("Error launching updated version of Shard.");
				e.printStackTrace();
			}
			System.exit(0);
		}
	}

	public static void deleteDir(File file) {
		File[] contents = file.listFiles();
		if (contents != null) {
			for (File f : contents) {
				deleteDir(f);
			}
		}
		file.delete();
	}

	public enum PATCHER_TYPE {
		checkVersion, downloadUpdate, runUpdate;
	}
}
