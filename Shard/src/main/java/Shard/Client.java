package Shard;

import Netta.Connection.Client.ClientTemplate;
import Netta.Connection.Packet;

import java.security.NoSuchAlgorithmException;

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

