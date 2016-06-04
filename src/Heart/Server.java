package Heart;

import java.net.Socket;
import java.util.ArrayList;

import Netta.Connection.Server.MultiClientServer;
import Netta.Exceptions.ConnectionInitializationException;

public class Server extends MultiClientServer {

	private ArrayList<ClientConnection> clients;
	private ArrayList<Thread> clientThreads;

	public Server(int port) {
		super(port);
		clients = new ArrayList<ClientConnection>();
		clientThreads = new ArrayList<Thread>();
	}

	@Override
	public void ThreadAction(Socket cc) {
		ClientConnection temp;
		try {
			temp = new ClientConnection(cc);
			clients.add(temp);
			Thread t = new Thread(temp);
			clientThreads.add(t);
			t.start();
		} catch (ConnectionInitializationException e) {
			e.printStackTrace();
		}
	}
}
