/*
 * This file is part of Crystal Home Systems.
 *
 * Crystal Home Systems is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Crystal Home Systems is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Crystal Home Systems. If not, see http://www.gnu.org/licenses/.
 */

package Heart;

import Netta.Connection.Packet;
import Netta.Connection.Server.MultiClientServer;
import Netta.Exceptions.ConnectionException;
import Netta.Exceptions.ConnectionInitializationException;
import Netta.Exceptions.SendPacketException;

import java.io.IOException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

/**
 * Server wrapper. Handles socket availability and incoming connections
 */
public class Server extends MultiClientServer {

    private ArrayList<ClientConnection> clients;
    private ArrayList<Thread> clientThreads;

    /**
     * Server default constructor
     *
     * @param port int port to initialize the server on
     * @throws NoSuchAlgorithmException thrown if there is an error initializing network encryption
     */
    public Server(int port) throws NoSuchAlgorithmException {
        super(port);
        clients = new ArrayList<ClientConnection>();
        clientThreads = new ArrayList<Thread>();
    }

    /**
     * Called every time there is a new client connection
     *
     * @param cc Socket connection client socket
     */
    @Override
    public void clientConnected(Socket cc) {
        ClientConnection temp;
        try {
            temp = new ClientConnection(cc, kript);
            clients.add(temp);
            Thread t = new Thread(temp);
            clientThreads.add(t);
            t.start();
        } catch (ConnectionInitializationException e) {
            System.err.println("Error initializing connection with Shard. Error: " + e.getMessage());
        }
    }

    /**
     * close all server connections
     */
    public void closeConnections() {
        Packet p = new Packet(Packet.PACKET_TYPE.CloseConnection, Heart_Core.getCore().getUUID().toString());
        p.packetString = "Manual disconnect";

        for (int i = 0; i < clients.size(); i++) {
            ClientConnection temp = clients.get(i);
            try {
                temp.sendPacket(p, true);
            } catch (SendPacketException e) {
                System.err.println("Unable to send close Connection Packet to Shard. Error: " + e.getMessage());
            }
            try {
                temp.closeIOStreams();
            } catch (ConnectionException e) {
                System.err.println("Unable to close IO streams with Shard. Error: " + e.getMessage());
            }
        }

        try {
            this.closeServer();
        } catch (IOException e) {
            System.err.println("Error closing server socket.");
        } catch (NullPointerException e) {
        }
    }

    /**
     * Get all active clients
     *
     * @return ClientConnection[] containing all clients
     */
    public ClientConnection[] getClients() {
        ClientConnection[] clientConnections = new ClientConnection[clients.size()];
        return clients.toArray(clientConnections);
    }
}
