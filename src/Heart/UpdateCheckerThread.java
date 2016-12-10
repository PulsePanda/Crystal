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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
//					downloadUpdate();
					System.out.println("UPDATER: Update is downloaded. Packing for client and installing for Heart...");
					System.out.println("UPDATER: Preparing patch...");
					preparePatch();
					System.out.println("UPDATER: Patch is ready.");
				}
				// removeFiles();
				System.out.println("UPDATER: All software is up to date!");
				shardUpdate = false;
				heartUpdate = false;
			} catch (Exception ex) {
				// throw download exception
				ex.printStackTrace();
				System.err.println("UPDATER: ERROR");
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
		FileOutputStream fis = new FileOutputStream(Heart_Core.GetCore().baseDir + "HeartVersion.txt");
		byte[] buffer = new byte[1024];
		int count = 0;
		while ((count = bis.read(buffer, 0, 1024)) != -1) {
			fis.write(buffer, 0, count);
		}
		fis.close();
		bis.close();

		url = new URL("https://raw.githubusercontent.com/PulsePanda/Crystal/master/ShardVersion");
		bis = new BufferedInputStream(url.openStream());
		fis = new FileOutputStream(Heart_Core.GetCore().baseDir + "ShardVersion.txt");
		buffer = new byte[1024];
		count = 0;
		while ((count = bis.read(buffer, 0, 1024)) != -1) {
			fis.write(buffer, 0, count);
		}
		fis.close();
		bis.close();

		String heartVersion = readVersionFile(Heart_Core.GetCore().baseDir + "HeartVersion.txt");
		String shardVersion = readVersionFile(Heart_Core.GetCore().baseDir + "ShardVersion.txt");
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
		FileOutputStream fis = new FileOutputStream(Heart_Core.GetCore().baseDir + "patch.zip");
		byte[] buffer = new byte[1024];
		int count = 0;
		while ((count = bis.read(buffer, 0, 1024)) != -1) {
			fis.write(buffer, 0, count);
		}
		fis.close();
		bis.close();
	}

	private void preparePatch() {
		File patchDir = new File(Heart_Core.GetCore().baseDir + "patch");
		deleteDir(patchDir);

		try {
			unZipIt(Heart_Core.GetCore().baseDir + "patch.zip", Heart_Core.GetCore().baseDir + "patch");
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (shardUpdate) {

		}

		if (heartUpdate) {

		}
	}

	private void deleteDir(File file) {
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
	 * @param zipFile
	 *            input zip file
	 * @param output
	 *            zip file output folder
	 * @throws IOException
	 *             thrown if there is an issue spawning the script.
	 */
	public void unZipIt(String zf, String outputFolder) throws IOException {
		String[] params = { "py", ".\\src\\Utilities\\unzip.py", zf, outputFolder };
		Process p = Runtime.getRuntime().exec(params);
	}

	private void removeFiles() {
		new File(Heart_Core.GetCore().baseDir + "HeartVersion.txt").delete();
		new File(Heart_Core.GetCore().baseDir + "ShardVersion.txt").delete();
		new File(Heart_Core.GetCore().baseDir + "patch.zip").delete();
		new File(Heart_Core.GetCore().baseDir + "patch").delete();
	}
}
