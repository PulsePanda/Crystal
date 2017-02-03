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

    /**
     * Default constructor
     *
     * @param client Client to access for sending and receiving
     * @param type   PATCHER_TYPE of this object
     */
    public ShardPatcher(Client client, PATCHER_TYPE type) {
        this.client = client;
        this.type = type;
        os = System.getProperty("os.name");
        os = os.split(" ")[0];
        os = os.toLowerCase();
    }

    /**
     * Thread run method. Handles the current step of the patch
     */
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

    /**
     * Checks the current version of the Shard from the Heart by calling getVersion()
     */
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

    /**
     * Gets the Shards version from the Heart
     *
     * @throws SendPacketException thrown if unable to send request to the Heart.
     *                             Details will be in the getMessage()
     */
    public void getVersion() throws SendPacketException {
        System.out.println("Getting version from Heart.");
        Packet p = new Packet(Packet.PACKET_TYPE.Command, "");
        p.packetString = "get Shard Version";
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
        }
        client.SendPacket(p, true);
    }

    /**
     * Downloads the update from the Heart by calling updateHelper()
     */
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

    /**
     * Downloads the update from the Heart
     *
     * @throws SendPacketException thrown if there is an error sending the request.
     *                             Details will be in the getMessage()
     */
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

    /**
     * Runs and installs the update
     */
    private void runUpdateHelper() {
        if (!Shard_Core.SHARD_VERSION.equals(Shard_Core.SHARD_VERSION_SERVER)) {
            System.out.println("Installing update...");
            try {
                UnZip.unZip(Shard_Core.shardDir + "Shard.zip", Shard_Core.baseDir);
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
                if (os.equals("windows")) {
                    Runtime.getRuntime()
                            .exec(new String[]{"cmd", "/c", "start", Shard_Core.shardDir + "bin/Shard.bat"});
                } else if (os.equals("linux")) {
                    ProcessBuilder pb = new ProcessBuilder(Shard_Core.shardDir + "bin/Shard");
                    pb.start();
                }
            } catch (IOException e) {
                System.err.println("Error launching updated version of Shard.");
                e.printStackTrace();
            }
            System.exit(0);
        }
    }

    /**
     * Deletes the update directories and file recursively
     *
     * @param file File to delete. Should be the root of the patch directory
     */
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
