/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shard;

import Exceptions.ClientInitializationException;

/**
 * Shard Connection Thread
 * <p>
 * Handles the connection for the Shard
 *
 * @author Austin
 */
public class ShardConnectionThread implements Runnable {

    private Shard_Core sc;
    private boolean keepRunning, patchOnly;

    /**
     * Default constructor
     *
     * @param keepRunning Boolean keep trying to connect
     * @param patchOnly   Boolean is the connection only for patching
     */
    public ShardConnectionThread(boolean keepRunning, boolean patchOnly) {
        sc = Shard_Core.getShardCore();
        this.keepRunning = keepRunning;
        this.patchOnly = patchOnly;
    }

    /**
     * Run constantly. Every 10 seconds, if the Shard is not connected to the
     * Heart, try to connect again.
     */
    @Override
    public void run() {
        while (true) {
            try {
                if (!patchOnly)
                    sc.startShardClient();
                sc.initPatcher();
            } catch (ClientInitializationException e) {
                // If the shard is already connected, this is executed. Don't need to do anything, just keep looping
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
