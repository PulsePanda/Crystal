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
import Shard.Shard_Core;

import javax.swing.*;
import java.util.UUID;

/**
 * Packet Handler. Figures out what to do with incoming packets and continues from there
 */
public class HandlePacket {

    private String message;
    private Packet packet;
    private Client client;

    /**
     * Default constructor
     * <p>
     * Currently only shows the results of the message in a JOptionPane Message
     * box
     *
     * @param packet Packet to handle
     * @param client Client to access for handling the packet request
     */
    public HandlePacket(Packet packet, Client client) {
        this.message = packet.packetString;
        this.packet = packet;
        this.client = client;
        System.out.println(packet.packetType.toString() + " from server: " + message);
        handle();
    }

    /**
     * Handle the packet
     */
    private void handle() {
        switch (packet.packetType.toString().toLowerCase()) {
            case "message":
                if (message.startsWith("version:")) {
                    String[] split = message.split(":");
                    String version = split[1];
                    Shard_Core.getShardCore().getConfigurationManager().SHARD_VERSION_SERVER = version;
                } else if (message.equals("update")) {
                    Shard_Core.getShardCore().getConnectionManager().getPatcher().updateFile = packet.packetByteArray;
                } else if (message.equals("new patch")) {
                    ShardPatcher patcher = new ShardPatcher(Shard_Core.getShardCore().getConnectionManager().getClient(),
                            ShardPatcher.PATCHER_TYPE.downloadUpdate);
                    patcher.start();
                } else if (message.equals("patch file send error")) {
                    System.err.println("Heart was unable to send the patch. This Shard will be unusable until issue is resolved.\n" +
                            "Common causes: Heart does not have Python installed, or Python is not set as a System Path\n" +
                            "The filepath is inaccessible to the Heart, likely because of permissions issues. File path is /%userhome%/CrystalHomeSys/\n" +
                            "For further information, refer to the README provided with Crystal, or at http://github.com/PulsePanda/Crystal\n" +
                            "\nShard is now closing connections.");
                    Shard_Core.getShardCore().getConnectionManager().stopShardConnectionThread();
                    try {
                        Shard_Core.getShardCore().getConnectionManager().stopShardClient();
                    } catch (ConnectionException e) {
                        System.err.println("Error closing Shard. Details: " + e.getMessage());
                    }
                } else if (message.equals("which song")) {
                    try {
                        String songPath = "";
                        if (packet.packetStringArray.length > 1) {
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < packet.packetStringArray.length; i++) {
                                sb.append("" + i + ": " + packet.packetStringArray[i] + "\n");
                            }
                            String options = sb.toString();
                            int index = Integer.parseInt(JOptionPane.showInputDialog(null, "Which file did you mean? Please enter an integer\n" + options));
                            songPath = packet.packetStringArray[index];
                        } else {
                            songPath = packet.packetStringArray[0];
                        }
                        Packet p = new Packet(Packet.PACKET_TYPE.Command, Shard_Core.getShardCore().getUUID().toString());
                        p.packetString = "Play Music";
                        p.packetStringArray = new String[]{songPath};
                        Shard_Core.getShardCore().getConnectionManager().getClient().sendPacket(p, true);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        JOptionPane.showMessageDialog(null, "Heart was unable to find a song matching that name!");
                        System.err.println("Heart was unable to find a song matching that name!");
                    } catch (SendPacketException e) {
                        System.err.println("Error sending song response packet to Heart. Details: " + e.getMessage());
                    }
                } else if (message.equals("which movie")) {
                    try {
                        String moviePath = "";
                        if (packet.packetStringArray.length > 1) {
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < packet.packetStringArray.length; i++) {
                                sb.append("" + i + ": " + packet.packetStringArray[i] + "\n");
                            }
                            String options = sb.toString();
                            int index = Integer.parseInt(JOptionPane.showInputDialog(null, "Which file did you mean? Please enter an integer\n" + options));
                            moviePath = packet.packetStringArray[index];
                        } else {
                            moviePath = packet.packetStringArray[0];
                        }
                        Packet p = new Packet(Packet.PACKET_TYPE.Command, Shard_Core.getShardCore().getUUID().toString());
                        p.packetString = "Play Movie";
                        p.packetStringArray = new String[]{moviePath};
                        Shard_Core.getShardCore().getConnectionManager().getClient().sendPacket(p, true);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        JOptionPane.showMessageDialog(null, "Heart was unable to find a movie matching that name!");
                        System.err.println("Heart was unable to find a movie matching that name!");
                    } catch (SendPacketException e) {
                        System.err.println("Error sending movie response packet to Heart. Details: " + e.getMessage());
                    }
                } else if (message.equals("media server")) {
                    int mediaServerPort = packet.packetInt;
                    System.out.println("Received media server information. Port: " + mediaServerPort + " Address: " + Shard_Core.getShardCore().getConnectionManager().getIP());
                    Shard_Core.getShardCore().getConnectionManager().connectToMediaServer(mediaServerPort, "music"); // TODO the Music param needs to be dynamic
                } else if (message.equals("uuid")) {
                    Shard_Core.getShardCore().setHeartUUID(UUID.fromString(packet.senderID));
                } else {
                    JOptionPane.showMessageDialog(null, message);
                }
                break;
            case "closeconnection":
                System.out.println(
                        "Server requested connection termination. Reason: " + packet.packetString + ". Closing connection.");
                Shard_Core.getShardCore().getConnectionManager().resetConnectionData();

                break;
        }
    }
}
