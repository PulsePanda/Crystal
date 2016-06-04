package Shard;

import Netta.Connection.Packet;
import Netta.Connection.Client.ClientTemplate;
import Netta.Exceptions.ConnectionException;

public class Client extends ClientTemplate {

	public Client(String serverIP, int port) {
		super(serverIP, port);
	}

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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		}
	}
}
