package Heart;

import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import Netta.Connection.Packet;
import Netta.Connection.Server.MultiClientServer;
import Netta.Exceptions.ConnectionException;
import Netta.Exceptions.ConnectionInitializationException;
import Netta.Exceptions.SendPacketException;

public class Server extends MultiClientServer {

	public ArrayList<ClientConnection> clients;
	private ArrayList<Thread> clientThreads;

	public Server(int port) throws NoSuchAlgorithmException {
		super(port);
		clients = new ArrayList<ClientConnection>();
		clientThreads = new ArrayList<Thread>();
	}

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

	public void CloseConnections() {
		Packet p = new Packet(Packet.PACKET_TYPE.CloseConnection, null);
		p.packetString = "Manual disconnect";

		for (int i = 0; i < clients.size(); i++) {
			ClientConnection temp = clients.get(i);
			try {
				temp.SendPacket(p, true);
			} catch (SendPacketException e) {
				System.err.println("Unable to send Close Connection Packet to Shard. Error: " + e.getMessage());
			}
			try {
				temp.CloseIOStreams();
			} catch (ConnectionException e) {
				System.out.println("Unable to close IO streams with Shard. Error: " + e.getMessage());
			}
		}
	}
}
