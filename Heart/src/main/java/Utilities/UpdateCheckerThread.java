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

import Exceptions.UpdateCheckerThreadException;
import Heart.Heart_Core;
import Heart.Manager.ConfigurationManager;

import java.io.*;
import java.net.URL;

/**
 * Thread to check for updates for Crystal from the GitHub repository.
 */
public class UpdateCheckerThread extends Thread {

    private static final int waitDelay = Integer.parseInt(Heart_Core.getCore().getConfigurationManager().getCfg().get("updateCheckDelay")) * 60 * 1000;
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
                    if (shardUpdate)
                        installShardPatch();

                    if (heartUpdate || forceUpdate) {
                        System.out.println("UPDATER: Update found. Updating...");
                        PatcherPython.patch(Heart_Core.getCore().getConfigurationManager().DEV_BUILD, true, true, false);
                        Heart_Core.getCore().shutdownHeart();
                        System.exit(0);
                    }
                } else {
                    System.out.println("UPDATER: System up to date.");
                }

                shardUpdate = false;
                heartUpdate = false;
            } catch (UpdateCheckerThreadException ex) {
                System.err.println("UPDATER: Problem checking for updates. Details: " + ex.getMessage());
                shardUpdate = false;
                heartUpdate = false;
                removeFiles();
            } catch (IOException e) {
                shardUpdate = false;
                heartUpdate = false;
                System.err.println("UPDATER: Problem patching system. Aborting.");
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
     * @throws UpdateCheckerThreadException thrown if there is an error getting the update information
     */
    private void checkForUpdate() throws UpdateCheckerThreadException {
        URL url;
        try {
            if (!ConfigurationManager.DEV_BUILD)
                url = new URL("https://raw.githubusercontent.com/PulsePanda/Crystal/master/HeartVersion");
            else
                url = new URL("https://raw.githubusercontent.com/PulsePanda/Crystal/dev/HeartVersion");
        } catch (IOException e) {
            throw new UpdateCheckerThreadException("Patch URL is unable to be created.");
        }

        BufferedInputStream bis;
        FileOutputStream fis;
        byte[] buffer = new byte[1024];
        int count = 0;

        try {
            bis = new BufferedInputStream(url.openStream());
            fis = new FileOutputStream(ConfigurationManager.baseDir + "HeartVersion.txt");
            while ((count = bis.read(buffer, 0, 1024)) != -1) {
                fis.write(buffer, 0, count);
            }
            fis.close();
            bis.close();
        } catch (IOException e) {
            throw new UpdateCheckerThreadException("Unable to update HeartVersion file.");
        }

        try {
            if (!ConfigurationManager.DEV_BUILD)
                url = new URL("https://raw.githubusercontent.com/PulsePanda/Crystal/master/ShardVersion");
            else
                url = new URL("https://raw.githubusercontent.com/PulsePanda/Crystal/dev/ShardVersion");
            bis = new BufferedInputStream(url.openStream());
            fis = new FileOutputStream(ConfigurationManager.baseDir + "ShardVersion.txt");
            buffer = new byte[1024];
            while ((count = bis.read(buffer, 0, 1024)) != -1) {
                fis.write(buffer, 0, count);
            }
            fis.close();
            bis.close();
        } catch (IOException e) {
            throw new UpdateCheckerThreadException("Unable to update ShardVersion file.");
        }

        try {
            String heartVersion = readVersionFile(ConfigurationManager.baseDir + "HeartVersion.txt");
            String shardVersion = readVersionFile(ConfigurationManager.baseDir + "ShardVersion.txt");
            if (heartVersion != null || shardVersion != null) {
                if (!shardVersion.equals(ConfigurationManager.SHARD_VERSION)) {
                    shardUpdate = true;
                }
                this.shardVersion = shardVersion;

                if (!heartVersion.equals(ConfigurationManager.HEART_VERSION))
                    heartUpdate = true;
            }
        } catch (UpdateCheckerThreadException e) {
            throw new UpdateCheckerThreadException("Unable to read version files.");
        }
    }

    /**
     * Read the update information version files downloaded from checkForUpdate()
     *
     * @param path String version file path
     * @return String version information
     * @throws UpdateCheckerThreadException thrown if there is an error reading or accessing the version file
     */
    private String readVersionFile(String path) throws UpdateCheckerThreadException {
        String line = null;
        try {
            FileReader fileReader = new FileReader(path);

            BufferedReader bufferedReader = new BufferedReader(fileReader);
            line = bufferedReader.readLine();
            bufferedReader.close();
        } catch (IOException e) {
            throw new UpdateCheckerThreadException("");
        }
        return line;
    }

    /**
     * Install the Shard patch
     * <p>
     * Updates the local ShardVersion file with correct Shard version, and calls Heart_Core.notifyShardsOfUpdate();
     */
    private void installShardPatch() {
        System.out.println("PATCHER: Updating local Shard version.");
        PrintWriter out;
        try {
            out = new PrintWriter(ConfigurationManager.heartDir + "ShardVersion");
            out.print(shardVersion);
            out.close();

            Heart_Core.getCore().getShardManager().updateShardVersionFromLocal();
        } catch (FileNotFoundException e) {
            System.err.println("UPDATE: Error writing new Shard version to ShardVersion file!");
        }
        Heart_Core.getCore().getShardManager().notifyShardsOfUpdate();
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
        deleteDir(new File(ConfigurationManager.baseDir + "HeartVersion.txt"));
        deleteDir(new File(ConfigurationManager.baseDir + "ShardVersion.txt"));
    }
}
