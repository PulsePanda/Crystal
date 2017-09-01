/*
 * This file is part of Crystal Home Systems.
 *
 * Crystal Home Systems is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Crystal Home Systems is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Crystal Home Systems. If not, see http://www.gnu.org/licenses/.
 */

/**
 * @file Heart_Core.java
 * @author Austin VanAlstyne
 */
package Heart;

import Heart.Manager.ConfigurationManager;
import Heart.Manager.GUIManager;
import Heart.Manager.ServerManager;
import Heart.Manager.ShardManager;
import Utilities.LogManager;
import Utilities.Media.MediaManager;
import Utilities.SystemInfo;
import Utilities.UpdateCheckerThread;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Core Heart class. Handles every operation of the Server
 */
public class Heart_Core {

    protected static boolean initialized = false;
    private static Heart_Core heart_core;
    private static LogManager logManager;
    private boolean headless = false;
    protected Thread updateCheckerThread = null;
    private MediaManager mediaManager;
    private GUIManager guiManager;
    private ShardManager shardManager;
    private ConfigurationManager configurationManager;
    private ServerManager serverManager;
    private static SystemInfo systemInfo;

    /**
     * Heart Core Default Constructor
     *
     * @param headless  boolean run in GUI mode
     * @param DEV_BUILD boolean run on dev build
     */
    public Heart_Core(boolean headless, boolean DEV_BUILD) {
        shardManager = new ShardManager(this);
        configurationManager = new ConfigurationManager(this);
        systemInfo = new SystemInfo();
        heart_core = this;
        this.headless = headless;
        configurationManager.DEV_BUILD = DEV_BUILD;
        serverManager = new ServerManager(this);
    }


    /**
     * Initialize the Heart Server
     */
    public void init() {
        if (initialized) {
            return;
        }

        if (!headless) {
            guiManager = new GUIManager(this);
            guiManager.initGUI();

            redirectSystemStreams();
        }

        initVariables();

        configurationManager.initCfg();

        initLog();

        initMediaManager();

        initPatchThread();
    }


    //TODO Removing this method for service migration

    /**
     * Sets up the initial variables for directories
     */
    private void initVariables() {
        // Sets the baseDir to the home directory
        configurationManager.baseDir = System.getProperty("user.home") + configurationManager.baseDir;

        configurationManager.heartDir = configurationManager.baseDir + configurationManager.heartDir;

        configurationManager.shardLogsDir = configurationManager.baseDir + configurationManager.shardLogsDir;

        configurationManager.logBaseDir = configurationManager.heartDir + configurationManager.logBaseDir;

        configurationManager.shardFileDir = configurationManager.heartDir + configurationManager.shardFileDir;

        configurationManager.configDir = configurationManager.heartDir + configurationManager.configDir;

        shardManager.updateShardVersionFromLocal();
    }


    /**
     * set up the logging system
     */
    private void initLog() {
        logManager = new LogManager();
        try {
            logManager.createLog(configurationManager.logBaseDir);
            configurationManager.setLogActive(true);

            // Start the logManager and initialize the text
            System.out.println("###############" + configurationManager.systemName + "###############");
            System.out.println("System logging enabled");
        } catch (SecurityException e) {
            System.out.println("###############" + configurationManager.systemName + "###############");
            System.err.println(
                    "Unable to access logManager file or directory because of permission settings. Will continue running without logs, however please reboot to set logs.\n");
        } catch (IOException e) {
            System.out.println("###############" + configurationManager.systemName + "###############");
            System.err.println(
                    "Unable to access find or create logManager on object creation. Will continue running without logs, however please reboot to set logs.\n");
        }
    }

    /**
     * Initializes the media index thread to provide a usable list for shards
     */
    private void initMediaManager() {
        if (configurationManager.isCfg_set()) {
            mediaManager = new MediaManager(configurationManager.mediaDir, configurationManager.musicDir, configurationManager.movieDir);
            mediaManager.index(true, Integer.parseInt(configurationManager.getCfg().get("mediaIndexDelay")));
        } else {
            System.err.println("MEDIA_MANAGER: Configuration file was not found. Media management is unavailable until the configuration is set up.");
        }
    }

    /**
     * Initialize and start the UpdateCheckerThread for on-launch use
     */
    private void initPatchThread() {
        updateCheckerThread = new UpdateCheckerThread(true, false);
        updateCheckerThread.start();
    }

    /**
     * Function to redirect standard output streams to the write function
     */
    public void redirectSystemStreams() {
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) throws IOException {

                println(String.valueOf((char) b), Color.BLACK);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {

                println(new String(b, off, len), Color.BLACK);
            }

            @Override
            public void write(byte[] b) throws IOException {
                write(b, 0, b.length);
            }
        };

        OutputStream err = new OutputStream() {
            @Override
            public void write(int b) throws IOException {

                println(String.valueOf((char) b), Color.RED);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {

                println(new String(b, off, len), Color.RED);
            }

            @Override
            public void write(byte[] b) throws IOException {
                write(b, 0, b.length);
            }
        };

        System.setOut(new PrintStream(out, true));
        System.setErr(new PrintStream(err, true));
    }

    /**
     * Writes to the Standard Output Stream, as well as calls 'write' on the
     * local logManager object
     *
     * @param msg   String message to be displayed and written
     * @param color Color to set the line of text
     * @return Returns TRUE if successful at writing to the logManager, FALSE if not
     */
    public boolean println(final String msg, Color color) {
        boolean success = true;

        SwingUtilities.invokeLater(() -> {
            guiManager.appendToPane(msg, color);
        });

        if (configurationManager.isLogActive()) {
            try {
                logManager.write(msg);
            } catch (IOException e) {
                configurationManager.setLogActive(false);
                System.err.println(
                        "Unable to write to log. IOException thrown. Deactivating log file, please reboot to regain access.");
                success = false;
            }
        }

        return success;
    }

    /**
     * Get the Heart_Core object
     *
     * @return Heart_Core object
     */
    public static Heart_Core getCore() {
        return heart_core;
    }


    /**
     * Get the media manager object
     *
     * @return MediaManager object
     */
    public MediaManager getMediaManager() {
        return mediaManager;
    }


    /**
     * Get the Shard Manager
     *
     * @return ShardManager
     */
    public ShardManager getShardManager() {
        return shardManager;
    }

    /**
     * Get Configuration Manager
     *
     * @return ConfigurationManager
     */
    public ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }

    /**
     * Get Server Manager
     *
     * @return ServerManager
     */
    public ServerManager getServerManager() {
        return serverManager;
    }

    /**
     * Get System Info object
     *
     * @return SystemInfo object
     */
    public static SystemInfo getSystemInfo() {
        return systemInfo;
    }


    /**
     * Get GUI Manager
     *
     * @return GUIManager
     */
    public GUIManager getGuiManager() {
        return guiManager;
    }

    /**
     * Get Update Checker Thread
     *
     * @return Thread Update checker thread
     */
    public Thread getUpdateCheckerThread() {
        return updateCheckerThread;
    }

    /**
     * Set Update Checker Thread
     *
     * @param updateCheckerThread Thread to set it to
     */
    public void setUpdateCheckerThread(Thread updateCheckerThread) {
        this.updateCheckerThread = updateCheckerThread;
    }

    /**
     * Check if the Heart server is active
     *
     * @return true if the server is still active, else false.
     */
    public boolean isServerActive() {
        return serverManager.getServer().isServerActive();
    }

}
