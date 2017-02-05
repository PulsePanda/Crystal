package Heart;

import Netta.Connection.Packet;
import Netta.Connection.Server.MultiClientServer;
import Netta.Exceptions.ConnectionException;
import Netta.Exceptions.ConnectionInitializationException;
import Netta.Exceptions.SendPacketException;

import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

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
    public void ThreadAction(Socket cc) {
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
        Packet p = new Packet(Packet.PACKET_TYPE.CloseConnection, null);
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
                System.out.println("Unable to close IO streams with Shard. Error: " + e.getMessage());
            }
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
