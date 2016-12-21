package Heart;

import Exceptions.ServerInitializationException;

public class HeartDriver {

	private static Heart_Core heartCore;
	private static UpdateCheckerThread updateCheckerThread;
	private static int port = 6987;

	public static void main(String[] args) {

		// Init the Heart window and settings
		// String patchArg = args[0];
		// patchArg = patchArg.toLowerCase();
		// String guiArg= args[1];
		// guiArg = guiArg.toLowerCase();
		String guiArg = "-gui";
		String patchArg = "false";

		if (patchArg.equals("true")) {
			// do patching shit
		}

		if (guiArg.equals("-headless")) {
			heartCore = new Heart_Core(true);
			heartCore.Init();
		} else if (guiArg.equals("-gui")) {
			heartCore = new Heart_Core(false);
			heartCore.Init();
		} else {
			System.err.println("No argument passed on call. Must specify if headless! Add -headless or -gui");
			System.exit(0);
		}

		// Init Heart networking, start listening for Shards
		try {
			System.out.println("Starting Heart server on port " + port);
			heartCore.StartServer(port);
		} catch (ServerInitializationException e) {
			System.err.println("Error starting Heart server! Error message: " + e.getMessage());
		}

		// Init Patching Thread
		updateCheckerThread = new UpdateCheckerThread();
		updateCheckerThread.start();
	}
}
