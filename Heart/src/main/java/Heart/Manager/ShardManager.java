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

package Heart.Manager;

import Heart.ClientConnection;
import Heart.Heart_Core;
import Netta.Connection.Packet;
import Netta.Exceptions.SendPacketException;

import java.io.*;

public class ShardManager {

    private Heart_Core c;

    public ShardManager(Heart_Core heart_core) {
        c = heart_core;
    }


    /**
     * Send "new patch" packets to each connected shard
     */
    public void notifyShardsOfUpdate() {
        System.out.println("Notifying Shards of update.");
        for (ClientConnection cc : c.getServerManager().getServer().getClients()) {
            Packet p = new Packet(Packet.PACKET_TYPE.Message, c.getConfigurationManager().uuid.toString());
            p.packetString = "new patch";
            try {
                cc.sendPacket(p, true);
            } catch (SendPacketException e) {
                System.err.println("Error sending update notification to shard. Details: " + e.getMessage());
            }
        }
    }

    /**
     * Pull the Shard version from the local ShardVersion file and apply it to the local variable for use
     */
    public void updateShardVersionFromLocal() {
        try {
            File file = new File(c.getConfigurationManager().heartDir + "ShardVersion");
            if (!file.exists())
                file.createNewFile();

            FileReader fileReader = new FileReader(c.getConfigurationManager().heartDir + "ShardVersion");

            BufferedReader bufferedReader = new BufferedReader(fileReader);
            c.getConfigurationManager().SHARD_VERSION = bufferedReader.readLine();
            bufferedReader.close();
            c.getGuiManager().shardVersionLabel.setText("Shard_Version: " + c.getConfigurationManager().SHARD_VERSION);
        } catch (FileNotFoundException ex) {
            System.err.println("Unable to find local ShardVersion file located at: " + c.getConfigurationManager().heartDir + "ShardVersion");
        } catch (IOException ex) {
            System.err.println("Unable to read local ShardVersion file located at: " + c.getConfigurationManager().heartDir + "ShardVersion");
        }
    }
}
