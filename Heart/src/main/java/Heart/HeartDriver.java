package Heart;

import Exceptions.ServerInitializationException;

public class HeartDriver {

	private static Heart_Core heartCore;
	public static UpdateCheckerThread updateCheckerThread;
	private static int port = 6987;

	public static void main(String[] args) {

		boolean headlessArg = false;

		for (String s : args) {
			s = s.toLowerCase();
			switch (s) {
			case "-h":
				headlessArg = true;
				break;
			}
		}

		heartCore = new Heart_Core(headlessArg);
		heartCore.Init();

		// Init Heart networking, start listening for Shards
		try {
			System.out.println("Starting Heart server on port " + port);
			heartCore.StartServer(port);
		} catch (ServerInitializationException e) {
			System.err.println("Error starting Heart server! Error message: " + e.getMessage());
		}
	}
}
