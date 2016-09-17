package Heart;

import java.net.Socket;

import Netta.Connection.Packet;
import Netta.Connection.Server.ConnectedClient;
import Netta.Exceptions.ConnectionException;
import Netta.Exceptions.ConnectionInitializationException;
import Netta.Exceptions.SendPacketException;

public class ClientConnection extends ConnectedClient {

    private Command command;

    public ClientConnection(Socket socket) throws ConnectionInitializationException {
        super(socket);

        command = new Command(this);
        // Start connection procedures
    }

    /**
     * This method is called every time the shard receives a packet from it's
     * heart.
     *
     * @param p packet received from the heart
     */
    @Override
    public void ThreadAction(Packet p) {
        String packetType = p.packetType.toString();

        switch (packetType) {
            case "CloseConnection":
                System.out
                        .println("Shard requested connection closure. Reason: " + p.packetString + ". Closing connection.");
                try {
                    CloseIOStreams();
                } catch (ConnectionException e) {
                    System.err.println("Unable to close IO streams with Shard. Error: " + e.getMessage());
                }
                break;
            case "Command": {
                try {
                    command.AnalyzeCommand(p.packetString);
                } catch (SendPacketException ex) {
                    System.err.println("Error sending response packet to Shard. Error: " + ex.getMessage());
                }
            }
            break;
        }
    }
}
