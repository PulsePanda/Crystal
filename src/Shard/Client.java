package Shard;

import Netta.Connection.Packet;
import Netta.Connection.Client.ClientTemplate;
import Netta.Exceptions.ConnectionException;

public class Client extends ClientTemplate {

    public Client(String serverIP, int port) {
        super(serverIP, port);
    }

    /**
     * This method is called every time the shard sends something to the heart.
     *
     * @param p packet received from shard.
     */
    @Override
    public void ThreadAction(Packet p) {
        String packetType = p.packetType.toString();

        switch (packetType) {
            case "CloseConnection":
                System.out.println(
                        "Server requested connection termination. Reason: " + p.packetString + ". Closing connection.");
                try {
                    CloseIOStreams();
                } catch (ConnectionException e) {
                    System.err.println("Error closing connection with Heart. Error: " + e.getMessage());
                }
                break;
            case "Message":
                new HandleMessage(p.packetString);
                break;
        }
    }
}
