package Shard;

import java.security.NoSuchAlgorithmException;

import Netta.Connection.Packet;
import Netta.Connection.Client.ClientTemplate;
import Netta.Exceptions.ConnectionException;

public class Client extends ClientTemplate {

    private boolean conversation = false;

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
        String packetType = p.packetType.toString();
        if (!conversation) {
            switch (packetType) {
                case "CloseConnection":
                    System.out.println(
                            "Server requested connection termination. Reason: " + p.packetString + ". Closing connection.");
                    try {
                        CloseIOStreams();
//                    Shard_Core.GetShardCore().setInitializedToFalse();
//                    new Thread(new ReconnectShard()).start();
                    } catch (ConnectionException e) {
                        System.err.println("Error closing connection with Heart. Error: " + e.getMessage());
                    }
                    break;
                case "Message":
                    new HandleMessage(p);
                    break;
//                case "Shard Version":
//                    Shard_Core.SHARD_VERSION_SERVER = p.packetString;
//                    break;
            }
        } else {

        }
    }
}
