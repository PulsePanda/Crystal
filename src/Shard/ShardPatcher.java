/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shard;

import Netta.Connection.Packet;
import Netta.Exceptions.SendPacketException;

/**
 * Patcher class. Checks if there is an update to the Shard, and if there is
 * downloads the .zip from the server. From there, it unzips the download to the
 * home directory, and runs the ShardPatchInstaller.jar file.
 *
 * @author Austin
 */
public class ShardPatcher {

    private Client client;

    public ShardPatcher(Client client) {
        this.client = client;
    }

    public void getVersion() throws SendPacketException {
        Packet p = new Packet(Packet.PACKET_TYPE.Command, "");
        p.packetString = "Get Shard Version";
        client.SendPacket(p);
    }

    public void patch() {
        Shard_Core.patched = true;
    }
}
