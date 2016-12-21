/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Heart;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * @author Austin
 */
public class UpdateCheckerThread extends Thread {

	private static final String gitAddress = "https://github.com/PulsePanda/Crystal/archive/master.zip";
	private static final int waitDelay = 1080000; // Checks every 3 hours
	private boolean running = false, shardUpdate = false, heartUpdate = false;

	public UpdateCheckerThread() {

	}

	@Override
	public void run() {
		running = true;
		while (running) {
			try {
				System.out.println("UPDATER: Checking for update...");
				checkForUpdate();
				if (shardUpdate || heartUpdate) {
					System.out.println("UPDATER: There is a new version of the build. Downloading...");
					downloadUpdate();
					System.out.println("UPDATER: Update is downloaded. Packing for client and installing for Heart...");
					System.out.println("UPDATER: Preparing patch...");
					preparePatch();
					System.out.println("UPDATER: Patch is ready.");
					if (heartUpdate)
						installHeartPatch();
					if (shardUpdate)
						Heart_Core.GetCore().notifyShardsOfUpdate();
				}
				removeFiles();
				System.out.println("UPDATER: All software is up to date!");
				shardUpdate = false;
				heartUpdate = false;
			} catch (Exception ex) {
				System.err.println("UPDATER: Issue downloading patch from GitHub. Aborting patch.");
				ex.printStackTrace();
				shardUpdate = false;
				heartUpdate = false;
				removeFiles();
			}

			try {
				if (running) {
					Thread.sleep(waitDelay);
				}
			} catch (InterruptedException ex) {
			}
		}
	}

	private void checkForUpdate() throws MalformedURLException, FileNotFoundException, IOException {
		URL url = new URL("https://raw.githubusercontent.com/PulsePanda/Crystal/master/HeartVersion");
		BufferedInputStream bis = new BufferedInputStream(url.openStream());
		FileOutputStream fis = new FileOutputStream(Heart_Core.baseDir + "HeartVersion.txt");
		byte[] buffer = new byte[1024];
		int count = 0;
		while ((count = bis.read(buffer, 0, 1024)) != -1) {
			fis.write(buffer, 0, count);
		}
		fis.close();
		bis.close();

		url = new URL("https://raw.githubusercontent.com/PulsePanda/Crystal/master/ShardVersion");
		bis = new BufferedInputStream(url.openStream());
		fis = new FileOutputStream(Heart_Core.baseDir + "ShardVersion.txt");
		buffer = new byte[1024];
		count = 0;
		while ((count = bis.read(buffer, 0, 1024)) != -1) {
			fis.write(buffer, 0, count);
		}
		fis.close();
		bis.close();

		String heartVersion = readVersionFile(Heart_Core.baseDir + "HeartVersion.txt");
		String shardVersion = readVersionFile(Heart_Core.baseDir + "ShardVersion.txt");
		if (heartVersion != null || shardVersion != null) {

			if (!shardVersion.equals(Heart_Core.SHARD_VERSION))
				shardUpdate = true;

			if (!heartVersion.equals(Heart_Core.HEART_VERSION))
				heartUpdate = true;
		}
	}

	private String readVersionFile(String path) {
		String line = null;
		try {
			FileReader fileReader = new FileReader(path);

			BufferedReader bufferedReader = new BufferedReader(fileReader);
			line = bufferedReader.readLine();
			bufferedReader.close();
		} catch (FileNotFoundException ex) {
		} catch (IOException ex) {
		}

		return line;
	}

	private void downloadUpdate() throws MalformedURLException, FileNotFoundException, IOException {
		URL url = new URL(gitAddress);
		BufferedInputStream bis = new BufferedInputStream(url.openStream());
		FileOutputStream fis = new FileOutputStream(Heart_Core.baseDir + "patch.zip");
		byte[] buffer = new byte[1024];
		int count = 0;
		while ((count = bis.read(buffer, 0, 1024)) != -1) {
			fis.write(buffer, 0, count);
		}
		fis.close();
		bis.close();
	}

	private synchronized void preparePatch() throws IOException {
		unZipIt(Heart_Core.baseDir + "patch.zip", Heart_Core.baseDir + "patch");

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
		}

		File patchDir = new File(Heart_Core.baseDir + "patch/Crystal-master/");

		if (shardUpdate) {
			String[] params = new String[] { "cmd.exe", "/c", "gradlew Shard:build" };
			ProcessBuilder builder = new ProcessBuilder(params);
			builder.directory(patchDir);
			builder.start();

			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
			}

			File dir = new File(Heart_Core.baseDir + "patch/Crystal-master/Shard/build/distributions/Shard.zip");
			dir.renameTo(new File(Heart_Core.baseDir + "patch/Shard.zip"));
		}

		if (heartUpdate) {
			String[] params = new String[] { "cmd.exe", "/c", "gradlew Heart:build" };
			ProcessBuilder builder = new ProcessBuilder(params);
			builder.directory(patchDir);
			builder.start();

			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
			}

			File dir = new File(Heart_Core.baseDir + "patch/Crystal-master/Heart/build/distributions/Heart.zip");
			dir.renameTo(new File(Heart_Core.baseDir + "patch/Heart.zip"));
		}
	}

	private void installHeartPatch() throws IOException {
		Heart_Core.GetCore().StopHeartServer();

		unZipIt(Heart_Core.baseDir + "patch/Heart.zip", Heart_Core.baseDir);

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
		}

		System.out.println("UPDATER: Starting Heart Patcher...");
		Runtime.getRuntime().exec(new String[] { "cmd", "/c", "start", Heart_Core.heartDir + "bin/Heart.bat" });
		System.exit(0);
	}

	public static void deleteDir(File file) {
		File[] contents = file.listFiles();
		if (contents != null) {
			for (File f : contents) {
				deleteDir(f);
			}
		}
		file.delete();
	}

	/**
	 * Unzip it
	 *
	 * @param zf
	 *            input zip file
	 * @param outputFolder
	 *            zip file output folder
	 * @throws IOException
	 *             thrown if there is an issue spawning the script.
	 */
	public static void unZipIt(String zf, String outputFolder) throws IOException {
		String[] params = { "py", "../lib/unzip.py", zf, outputFolder };
		Runtime.getRuntime().exec(params);
	}

	private void removeFiles() {
		deleteDir(new File(Heart_Core.baseDir + "HeartVersion.txt"));
		deleteDir(new File(Heart_Core.baseDir + "ShardVersion.txt"));
		deleteDir(new File(Heart_Core.baseDir + "patch.zip"));
		deleteDir(new File(Heart_Core.baseDir + "patch/Crystal-master"));
	}
}
