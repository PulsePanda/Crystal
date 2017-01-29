package Shard;

import Exceptions.ClientInitializationException;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import java.io.IOException;

public class ShardDriver {

	private static Shard_Core shardCore;
	private static boolean headlessArg = false;

	public static void main(String[] args) {
		for (String s : args) {
			s = s.toLowerCase();
			switch (s) {
			case "-h":
				headlessArg = true;
				break;
			}
		}

		try {
			shardCore = new Shard_Core(headlessArg);
			shardCore.Init();
		} catch (ClientInitializationException ex) {
			System.err.println("Error starting Shard Core. Error: " + ex.getMessage());
		}

		ShardConnectionThread sct = new ShardConnectionThread(true, false);
		Thread t = new Thread(sct);
		t.start();
	}
}
