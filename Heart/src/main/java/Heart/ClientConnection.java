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

import Kript.Kript;
import Netta.Connection.Packet;
import Netta.Connection.Server.ConnectedClient;
import Netta.Exceptions.ConnectionException;
import Netta.Exceptions.ConnectionInitializationException;
import Netta.Exceptions.SendPacketException;
import Utilities.Command;
import Utilities.Log;

import java.io.IOException;
import java.net.Socket;

/**
 * Client Connection. Wrapper for accepted client connected to the Heart server
 */
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
