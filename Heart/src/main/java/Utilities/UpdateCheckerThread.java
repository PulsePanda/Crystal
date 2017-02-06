/*
 * This file is part of Crystal Home Systems.
 *
 * Crystal Home Systems is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Crystal Home Systems is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Crystal Home Systems. If not, see http://www.gnu.org/licenses/.
 */

/*
 * This file is part of Crystal Home Systems.
 *
 * Crystal Home Systems is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Crystal Home Systems is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Crystal Home Systems. If not, see http://www.gnu.org/licenses/.
 */

package Utilities;

import Heart.Heart_Core;

import java.io.*;
import java.net.URL;

/**
 * Thread to check for updates for Crystal from the GitHub repository.
 */
public class UpdateCheckerThread extends Thread {

    private static final String gitAddressMaster = "https://github.com/PulsePanda/Crystal/archive/master.zip";
    private static final String gitAddressDev = "https://github.com/PulsePanda/Crystal/archive/dev.zip";
    private static final int waitDelay = 1080000; // Checks every 3 hours
    private boolean running = false, shardUpdate = false, heartUpdate = false, keepRunning, forceUpdate;
    private String shardVersion;

    /**
     * Update Checker Thread default constructor
     *
     * @param keepRunning boolean keep checking for updates
     * @param forceUpdate boolean force an update
     */
    public UpdateCheckerThread(boolean keepRunning, boolean forceUpdate) {
        this.keepRunning = keepRunning;
        this.forceUpdate = forceUpdate;
        shardUpdate = forceUpdate;
    }

    /**
     * Threads run() method
     * <p>
     * Checks for update. If there is an update for either Heart or Shard, or forceUpdate is true,
     * downloads update, prepares the patch(es), and installs the appropriate patches. Then removes the update
     * files, and then waits for 3 hours before checking again.
     * <p>
     * If there is an error with checking for, downloading, preparing, or installing an update an error will be
     * printed and nothing more will continue. All files related to the update are removed and the thread waits to
     * iterate again.
     */
    @Override
    public void run() {
        running = true;
        while (running) {
            running = keepRunning;
            try {
                System.out.println("UPDATER: Checking for update...");
                checkForUpdate();
                if (shardUpdate || heartUpdate || forceUpdate) {
                    System.out.println("UPDATER: There is a new version of the build. Downloading...");
                    downloadUpdate();
                    System.out.println("UPDATER: Update is downloaded. Packing for distribution...");
                    System.out.println("UPDATER: Preparing patch...");
                    preparePatch();
                    System.out.println("UPDATER: Patch is ready.");
                    if (heartUpdate)
                        installHeartPatch();
                    if (shardUpdate)
                        installShardPatch();
                }
                removeFiles();
                System.out.println("UPDATER: All software is up to date!");
                shardUpdate = false;
                heartUpdate = false;
            } catch (Exception ex) {
                System.err.println("UPDATER: Issue downloading patch from GitHub. Aborting patch.");
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

    /**
     * Check for an available update
     *
     * @throws IOException thrown if there is an error getting the update information
     */
    private void checkForUpdate() throws IOException {
        URL url;
        if (!Heart_Core.DEV_BUILD)
            url = new URL("https://raw.githubusercontent.com/PulsePanda/Crystal/master/HeartVersion");
        else
            url = new URL("https://raw.githubusercontent.com/PulsePanda/Crystal/dev/HeartVersion");
        BufferedInputStream bis = new BufferedInputStream(url.openStream());
        FileOutputStream fis = new FileOutputStream(Heart_Core.baseDir + "HeartVersion.txt");
        byte[] buffer = new byte[1024];
        int count = 0;
        while ((count = bis.read(buffer, 0, 1024)) != -1) {
            fis.write(buffer, 0, count);
        }
        fis.close();
        bis.close();

        if (!Heart_Core.DEV_BUILD)
            url = new URL("https://raw.githubusercontent.com/PulsePanda/Crystal/master/ShardVersion");
        else
            url = new URL("https://raw.githubusercontent.com/PulsePanda/Crystal/dev/ShardVersion");
        bis = new BufferedInputStream(url.openStream());
        fis = new FileOutputStream(Heart_Core.baseDir + "ShardVersion.txt");
        buffer = new byte[1024];
        while ((count = bis.read(buffer, 0, 1024)) != -1) {
            fis.write(buffer, 0, count);
        }
        fis.close();
        bis.close();

        String heartVersion = readVersionFile(Heart_Core.baseDir + "HeartVersion.txt");
        String shardVersion = readVersionFile(Heart_Core.baseDir + "ShardVersion.txt");
        if (heartVersion != null || shardVersion != null) {
            if (!shardVersion.equals(Heart_Core.SHARD_VERSION)) {
                shardUpdate = true;
            }
            this.shardVersion = shardVersion;

            if (!heartVersion.equals(Heart_Core.HEART_VERSION))
                heartUpdate = true;
        }
    }

    /**
     * Read the update information version files downloaded from checkForUpdate()
     *
     * @param path String version file path
     * @return String version information
     * @throws IOException thrown if there is an error reading or accessing the version file
     */
    private String readVersionFile(String path) throws IOException {
        String line = null;
        FileReader fileReader = new FileReader(path);

        BufferedReader bufferedReader = new BufferedReader(fileReader);
        line = bufferedReader.readLine();
        bufferedReader.close();

        return line;
    }

    /**
     * Download the repository for the update
     *
     * @throws IOException thrown if there is an error accessing or downloading the repository
     */
    private void downloadUpdate() throws IOException {
        URL url;
        if (!Heart_Core.DEV_BUILD)
            url = new URL(gitAddressMaster);
        else
            url = new URL(gitAddressDev);
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

    /**
     * Prepare the patch
     *
     * @throws IOException thrown if there is an error accessing or reading the patch data
     */
    private synchronized void preparePatch() throws IOException {
        UnZip.unZip(Heart_Core.baseDir + "patch.zip", Heart_Core.baseDir + "patch");

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }

        File patchDir;
        if (!Heart_Core.DEV_BUILD)
            patchDir = new File(Heart_Core.baseDir + "patch/Crystal-master/");
        else
            patchDir = new File(Heart_Core.baseDir + "patch/Crystal-dev/");

        if (shardUpdate) {
            String[] params = new String[]{"cmd.exe", "/c", "gradlew Shard:build"};
            ProcessBuilder builder = new ProcessBuilder(params);
            builder.directory(patchDir);
            builder.start();

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
            }

            File dir;
            if (!Heart_Core.DEV_BUILD)
                dir = new File(Heart_Core.baseDir + "patch/Crystal-master/Shard/build/distributions/Shard.zip");
            else
                dir = new File(Heart_Core.baseDir + "patch/Crystal-dev/Shard/build/distributions/Shard.zip");
            dir.renameTo(new File(Heart_Core.baseDir + "patch/Shard.zip"));
        }

        if (heartUpdate) {
            String[] params = new String[]{"cmd.exe", "/c", "gradlew Heart:build"};
            ProcessBuilder builder = new ProcessBuilder(params);
            builder.directory(patchDir);
            builder.start();

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
            }

            File dir;
            if (!Heart_Core.DEV_BUILD)
                dir = new File(Heart_Core.baseDir + "patch/Crystal-master/Heart/build/distributions/Heart.zip");
            else
                dir = new File(Heart_Core.baseDir + "patch/Crystal-dev/Heart/build/distributions/Heart.zip");
            dir.renameTo(new File(Heart_Core.baseDir + "patch/Heart.zip"));
        }
    }

    /**
     * Install the Heart patch
     *
     * @throws IOException thrown if there is an error accessing or reading patch data
     */
    private void installHeartPatch() throws IOException {
        Heart_Core.getCore().stopHeartServer();

        UnZip.unZip(Heart_Core.baseDir + "patch/Heart.zip", Heart_Core.baseDir);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
        }

        System.out.println("UPDATER: Starting Heart Patcher...");
        Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", Heart_Core.heartDir + "bin/Heart.bat"});
        System.exit(0);
    }

    /**
     * Install the Shard patch
     * <p>
     * Updates the local ShardVersion file with correct Shard version, and calls Heart_Core.notifyShardsOfUpdate();
     */
    private void installShardPatch() {
        PrintWriter out;
        try {
            out = new PrintWriter(Heart_Core.heartDir + "ShardVersion");
            out.print(shardVersion);
            out.close();

            Heart_Core.getCore().updateShardVersion();
        } catch (FileNotFoundException e) {
            System.err.println("UPDATE: Error writing new Shard version to ShardVersion file!");
        }
        Heart_Core.getCore().notifyShardsOfUpdate();
    }

    /**
     * Delete a directory recursively
     *
     * @param file File directory to delete
     */
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
     * Remove patch files
     */
    private void removeFiles() {
        deleteDir(new File(Heart_Core.baseDir + "HeartVersion.txt"));
        deleteDir(new File(Heart_Core.baseDir + "ShardVersion.txt"));
        deleteDir(new File(Heart_Core.baseDir + "patch.zip"));
        deleteDir(new File(Heart_Core.baseDir + "patch/Crystal-master"));
        deleteDir(new File(Heart_Core.baseDir + "patch/Crystal-dev"));
    }
}
