/*
 * This file is part of Crystal Home Systems.
 *
 * Crystal Home Systems is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Crystal Home Systems is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Crystal Home Systems. If not, see http://www.gnu.org/licenses/.
 */

/*
 * This file is part of Crystal Home Systems.
 *
 * Crystal Home Systems is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Crystal Home Systems is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Crystal Home Systems. If not, see http://www.gnu.org/licenses/.
 */

package Utilities;

import Netta.Connection.Packet;
import Netta.Exceptions.ConnectionException;
import Netta.Exceptions.SendPacketException;
import Shard.Client;
import Shard.Manager.ConfigurationManager;
import Shard.Manager.ConnectionManager;
import Shard.Shard_Core;

import java.io.File;
import java.io.IOException;

/**
 * Patcher class. Checks if there is an update to the Shard, and if there is
 * downloads the .zip from the server. From there, it unzips the download to the
 * home directory, and runs the ShardPatchInstaller.jar file.
 */
public class ShardPatcher extends Thread {

    public byte[] updateFile = null;
    private Client client;
    private PATCHER_TYPE type;
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
        os = SystemInfo.getSystem_os().toString().toLowerCase();
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

    /**
     * Thread run method. Handles the current step of the patch
     */
    @Override
    public void run() {
        if (type == PATCHER_TYPE.checkVersion) {
            checkVersionHelper();
        } else if (type == PATCHER_TYPE.downloadUpdate) {
            try {
                downloadUpdateHelper();
            } catch (IOException e) {
                System.err.println("Error spawning update script!");
            }
        } else if (type == PATCHER_TYPE.runUpdate) {
//            runUpdateHelper();
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
                Shard_Core.getShardCore().getConnectionManager().stopShardClient();
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
        Packet p = new Packet(Packet.PACKET_TYPE.Command, Shard_Core.getShardCore().getUUID().toString());
        p.packetString = "get Shard Version";
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
        }
        client.sendPacket(p);
    }

    /**
     * Downloads the update from the Heart by calling updateHelper()
     *
     * @throws IOException if there is an error launching the patch script
     */
    private void downloadUpdateHelper() throws IOException {
        if (!Shard_Core.getShardCore().getConfigurationManager().SHARD_VERSION.equals(ConfigurationManager.SHARD_VERSION_SERVER)) {
            System.out.println("Update required. Initializing update.");
            PatcherPython.patch(Shard_Core.getShardCore().getConfigurationManager().DEV_BUILD, true, false, true);
            try {
                Shard_Core.getShardCore().getConnectionManager().stopShardClient();
            } catch (ConnectionException e) {
            }
            System.exit(0);
        } else {
            System.out.println("Shard is up to date!");
            ConnectionManager.patchReady = true;
        }
    }

    public enum PATCHER_TYPE {
        checkVersion, downloadUpdate, runUpdate
    }
}
