/*
 * This file is part of Crystal Home Systems.
 *
 * Crystal Home Systems is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Crystal Home Systems is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Crystal Home Systems. If not, see http://www.gnu.org/licenses/.
 */

package Shard;

import Netta.Connection.Client.ClientTemplate;
import Netta.Connection.Packet;
import Utilities.HandlePacket;

import java.security.NoSuchAlgorithmException;

/**
 * Client wrapper. Handles connection and packets received from Heart
 */
public class Client extends ClientTemplate {

    public Client(String serverIP, int port) throws NoSuchAlgorithmException {
        super(serverIP, port);
    }

    /**
     * This method is called every time the shard sends something to the heart.
     *
     * @param p packet received from shard.
     */
    @Override
    public void ThreadAction(Packet p) {
        new HandlePacket(p, this);
    }
}

