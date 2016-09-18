package Shard;

import Exceptions.ClientInitializationException;
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
//                    Shard_Core.GetShardCore().setInitializedToFalse();
//                    new Thread(new ReconnectShard()).start();
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

class ReconnectShard implements Runnable {

    Shard_Core sc;

    public ReconnectShard() {
        sc = Shard_Core.GetShardCore();
    }

    @Override
    public void run() {
        while (sc.IsActive() == false) {
            try {
                sc.StartShardClient(sc.getIP(), sc.getPort());
            } catch (ClientInitializationException ex) {
                System.err.println("Error in initializing client in the reconnection class. Error: " + ex.getMessage());
            }

            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
            }
        }
    }

}
