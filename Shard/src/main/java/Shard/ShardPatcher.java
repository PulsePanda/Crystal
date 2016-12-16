/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shard;

import Netta.Connection.Packet;
import Netta.Exceptions.ConnectionException;
import Netta.Exceptions.SendPacketException;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    public ShardPatcher(Client client, PATCHER_TYPE type) {
        this.client = client;
        this.type = type;
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
        Packet p = new Packet(Packet.PACKET_TYPE.Command, "");
        p.packetString = "Get Shard Version";
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            Logger.getLogger(ShardPatcher.class.getName()).log(Level.SEVERE, null, ex);
        }
        client.SendPacket(p);
    }

    private void downloadUpdateHelper() {
        if (!Shard_Core.GetShardCore().SHARD_VERSION.equals(Shard_Core.GetShardCore().SHARD_VERSION_SERVER)) {
            updateHelper();
        } else {
            System.out.println("Shard is up to date!");
            Shard_Core.GetShardCore().patched = true;
        }
        Shard_Core.patched = true;
    }

    private void updateHelper() {
        /*
        This will download the shard source zip from the server, unzip
         */
    }

    private void runUpdateHelper() {
        /*
        run the patcher script file. Close Shard
        
        Patcher file will remove old files, replace them with new files, launch
        the new shard
         */
    }

    public enum PATCHER_TYPE {
        checkVersion, downloadUpdate, runUpdate;
    }
}