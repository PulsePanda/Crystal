package Heart;

import Kript.Kript;
import Netta.Connection.Packet;
import Netta.Connection.Server.ConnectedClient;
import Netta.Exceptions.ConnectionException;
import Netta.Exceptions.ConnectionInitializationException;
import Netta.Exceptions.SendPacketException;
import Utilities.Log;

import java.io.IOException;
import java.net.Socket;

public class ClientConnection extends ConnectedClient {

    private Command command;
    private Log clientLog;
    private boolean clientLogCreated = false, conversation = false;

    /**
     * Client Connection default constructor
     *
     * @param socket Socket client socket
     * @param kript  Kript kript object for connection encryption
     * @throws ConnectionInitializationException thrown if there is an error initializing client.
     *                                           Details will be in the getMessage()
     */
    public ClientConnection(Socket socket, Kript kript) throws ConnectionInitializationException {
        super(socket, kript);

        command = new Command(this);
        clientLog = new Log();
        try {
            clientLog.createLog(Heart_Core.shardLogsDir);
            clientLogCreated = true;
        } catch (IOException ex) {
            System.err.println("Unable to create log for connected Shard. Ignoring logging.");
        }

        // Start connection procedures
    }

    /**
     * This method is called every time the shard receives a packet from the heart.
     *
     * @param p Packet received from the heart
     */
    @Override
    public void ThreadAction(Packet p) {
        String packetType = p.packetType.toString();

        // If the previous message(s) dont expect a conversation, treat as such.
        if (!getConversation()) {
            switch (packetType) {
                case "CloseConnection":
                    System.out.println(
                            "Shard requested connection closure. Reason: " + p.packetString + ". Closing connection.");
                    try {
                        closeIOStreams();
                    } catch (ConnectionException e) {
                        System.err.println("Unable to close IO streams with Shard. Error: " + e.getMessage());
                    }
                    break;
                case "Command":
                    try {
                        command.analyzeCommand(p);
                    } catch (SendPacketException ex) {
                        System.err.println("Error sending response packet to Shard. Error: " + ex.getMessage());
                    }
                    break;
                case "Message":
                    if (clientLogCreated) {
                        try {
                            clientLog.write(p.packetString);
                        } catch (IOException ex) {
                            System.err.println("Unable to write to Shard Log.");
                        }
                    }
                    break;
            }
        } else { // Else, if there is a conversation going on

        }
    }

    @Deprecated
    public void setConversation(boolean b) {
        conversation = b;
    }

    @Deprecated
    public boolean getConversation() {
        return conversation;
    }
}
