/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shard;

import Exceptions.ClientInitializationException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Austin
 */
public class ShardConnectionThread implements Runnable {

    private Shard_Core sc;
    private String IP;
    private int port;

    public ShardConnectionThread(String IP, int port) {
        sc = Shard_Core.GetShardCore();
        this.IP = IP;
        this.port = port;
    }

    /**
     * Run constantly. Every 5 seconds, if the Shard is not connected to the
     * Heart, try to connect again.
     */
    @Override
    public void run() {
        while (true) {
            try {
                sc.StartShardClient(sc.getIP(), sc.getPort());
            } catch (ClientInitializationException ex) {
                System.err.println(ex.getMessage());
            }

            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
            }

        }
    }
}
