/*
 * This file is part of Crystal Home Systems.
 *
 * Crystal Home Systems is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Crystal Home Systems is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Crystal Home Systems. If not, see http://www.gnu.org/licenses/.
 */

import Netta.Connection.Packet;
import Netta.Connection.Server.SingleClientServer;
import Netta.Exceptions.SendPacketException;

import java.security.NoSuchAlgorithmException;

public class Server extends SingleClientServer {

    private String filePath;

    public Server(int port, String filePath) throws NoSuchAlgorithmException {
        super(port, null);
        this.filePath = filePath;
        Packet p = new Packet(Packet.PACKET_TYPE.Message, null);
        p.packetString = filePath;
        try {
            sendPacket(p);
        } catch (SendPacketException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void packetReceived(Packet packet) {

    }
}
