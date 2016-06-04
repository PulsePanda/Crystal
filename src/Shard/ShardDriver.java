package Shard;

import Exceptions.ClientInitializationException;

public class ShardDriver {

	private static Shard_Core shardCore;

	public static void main(String[] args) {
		// Init Shard graphics and settings
		// String arg = args[0];
		// arg = arg.toLowerCase();
		String arg = "-gui";

		if (arg == "-headless") {
			shardCore = new Shard_Core(true);
			shardCore.Init();
		} else if (arg == "-gui") {
			shardCore = new Shard_Core(false);
			shardCore.Init();
		} else {
			System.err.println("No arguement passed on call. Must specify if headless! Add -headless or -gui");
			System.exit(0);
		}

		// Init Shard networking, try to connect to the Heart
		try {
			shardCore.StartShardClient("localhost", 6987);
		} catch (ClientInitializationException e) {
			System.err.println("Error starting Shard client! Error message: " + e.getMessage());
		}

		// TESTING
		// try {
		// Thread.sleep(1000);
		// } catch (InterruptedException e1) {
		// // TODO Auto-generated catch block
		// e1.printStackTrace();
		// }
		//
		// for (int i = 0; i < 20; i++) {
		// Packet p = new Packet(Packet.PACKET_TYPE.Message, null);
		// p.packetString = "test" + i;
		// try {
		// shardCore.SendPacket(p);
		// System.out.println("packet sent");
		// } catch (SendPacketException e) {
		// e.printStackTrace();
		// }
		// }
		///////////////////////////////
	}
}