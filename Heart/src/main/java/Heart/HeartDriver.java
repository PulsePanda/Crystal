package Heart;

import java.io.File;
import java.io.IOException;

import Exceptions.ServerInitializationException;

public class HeartDriver {

	private static Heart_Core heartCore;
	private static UpdateCheckerThread updateCheckerThread;
	private static int port = 6987;

	public static void main(String[] args) {

		boolean headlessArg = false;
		boolean patchArg = false;

		for (String s : args) {
			s = s.toLowerCase();
			switch (s) {
			case "-p":
				patchArg = true;
				break;
			case "-h":
				headlessArg = true;
				break;
			}
		}

		if (patchArg) {
			try {
				System.out.println("Patching...");
				UpdateCheckerThread.unZipIt(Heart_Core.baseDir + "patch/Heart.zip", Heart_Core.baseDir);
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
				}
				Runtime.getRuntime()
						.exec(new String[] { "cmd", "/c", "start", Heart_Core.baseDir + "Heart/bin/Heart.bat" });
				System.exit(0);
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		} else {
			removeHeartPatcher();
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

		// Init Patching Thread
		updateCheckerThread = new UpdateCheckerThread();
		updateCheckerThread.start();
	}

	private static void removeHeartPatcher() {
		UpdateCheckerThread.deleteDir(new File(Heart_Core.baseDir + "patch/Heart"));
		UpdateCheckerThread.deleteDir(new File(Heart_Core.baseDir + "patch/Heart.zip"));
	}
}
