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
    private static final int waitDelay = 180000;
    private boolean running = false, shardUpdate = false;

    public UpdateCheckerThread() {

    }

    @Override
    public void run() {
        running = true;
        while (running) {
            try {
                System.out.println("UPDATER: Checking for update...");
                if (checkForUpdate()) { ///////////////////////////////// This is returning true when versions match
                    System.out.println("UPDATER: There is a new version of the build. Downloading...");
                    downloadUpdate();
                    System.out.println("UPDATER: Update is downloaded. Packing for client and installing for Heart...");
                    running = false;

                    System.out.println("UPDATER: Preparing patch...");
                    preparePatch();
                    System.out.println("UPDATER: Patch is ready.");
                }
                //removeFiles();
                System.out.println("UPDATER: All software is up to date!");
            } catch (Exception ex) {
                // throw download exception
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

    private boolean checkForUpdate() throws MalformedURLException, FileNotFoundException, IOException {
        /*
        if there's a new commit, return true
         */
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
            if (heartVersion != Heart_Core.HEART_VERSION) {
                // if the shard has an update, the heart will always have an update for the shardversion
                if (shardVersion != Heart_Core.SHARD_VERSION) {
                    shardUpdate = true;
                }
                return true;
            }
        }

        return false;
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
        // if this method is called, heart will always have a patch.
        // Unzip patch files
        unZipIt(Heart_Core.GetCore().baseDir + "patch.zip", Heart_Core.GetCore().baseDir + "patch");

        if (shardUpdate) {

        }
    }

    /**
     * Unzip it
     *
     * @param zipFile input zip file
     * @param output zip file output folder
     */
    public void unZipIt(String zf, String outputFolder) {
        outputFolder += "/";
        try (ZipFile file = new ZipFile(zf)) {
            FileSystem fileSystem = FileSystems.getDefault();
            //Get file entries
            Enumeration<? extends ZipEntry> entries = file.entries();

            //We will unzip files in this folder
            String uncompressedDirectory = outputFolder;
            Files.createDirectory(fileSystem.getPath(uncompressedDirectory));

            //Iterate over entries
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                //If directory then create a new directory in uncompressed folder
                if (entry.isDirectory()) {
                    Files.createDirectories(fileSystem.getPath(uncompressedDirectory + entry.getName()));
                } //Else create the file
                else {
                    InputStream is = file.getInputStream(entry);
                    BufferedInputStream bis = new BufferedInputStream(is);
                    String uncompressedFileName = uncompressedDirectory + entry.getName();
                    Path uncompressedFilePath = fileSystem.getPath(uncompressedFileName);
                    Files.createFile(uncompressedFilePath);
                    FileOutputStream fileOutput = new FileOutputStream(uncompressedFileName);
                    while (bis.available() > 0) {
                        fileOutput.write(bis.read());
                    }
                    fileOutput.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void removeFiles() {
        new File(Heart_Core.GetCore().baseDir + "HeartVersion.txt").delete();
        new File(Heart_Core.GetCore().baseDir + "ShardVersion.txt").delete();
        new File(Heart_Core.GetCore().baseDir + "patch.zip").delete();
        new File(Heart_Core.GetCore().baseDir + "patch").delete();
    }
}
