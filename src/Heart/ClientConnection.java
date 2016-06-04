package Heart;

import java.net.Socket;

import Netta.Connection.Packet;
import Netta.Connection.Server.ConnectedClient;
import Netta.Exceptions.ConnectionException;
import Netta.Exceptions.ConnectionInitializationException;

public class ClientConnection extends ConnectedClient {

	public ClientConnection(Socket socket) throws ConnectionInitializationException {
		super(socket);

		// Start connection procedures
	}

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
				e.printStackTrace();
			}
			break;
		}
	}

}
