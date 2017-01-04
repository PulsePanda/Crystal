/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shard;

import Exceptions.ClientInitializationException;

/**
 *
 * @author Austin
 */
public class ShardConnectionThread implements Runnable {

	private Shard_Core sc;
	private String IP;
	private int port;
	private boolean keepRunning, patchOnly;

	public ShardConnectionThread(String IP, int port, boolean keepRunning, boolean patchOnly) {
		sc = Shard_Core.GetShardCore();
		this.IP = IP;
		this.port = port;
		this.keepRunning = keepRunning;
		this.patchOnly = patchOnly;
	}

	/**
	 * Run constantly. Every 5 seconds, if the Shard is not connected to the
	 * Heart, try to connect again.
	 */
	@Override
	public void run() {
		while (true) {
			try {
				if (!patchOnly)
					sc.StartShardClient(IP, port);
				sc.InitPatcher();
			} catch (ClientInitializationException e) {
			}
			if (!keepRunning)
				return;
			try {
				Thread.sleep(10000);
			} catch (InterruptedException ex) {
			}
		}
	}
}
