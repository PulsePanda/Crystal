/*
 * This file is part of Crystal Home Systems.
 *
 * Crystal Home Systems is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Crystal Home Systems is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Crystal Home Systems. If not, see http://www.gnu.org/licenses/.
 */

package Shard;

import Exceptions.ClientInitializationException;
import Shard.Manager.ConfigurationManager;
import Shard.Manager.ConnectionManager;
import Shard.Manager.GUIManager;
import Utilities.LogManager;
import Utilities.SystemInfo;

import java.io.IOException;
import java.util.UUID;

import static Shard.Manager.ConfigurationManager.systemName;

//import Utilities.Media.MediaPlayback;

/**
 * Core class for the Shard. Handles everything the Shard does
 */
public class Shard_Core {

    public final static SystemInfo systemInfo = new SystemInfo();
    private static Shard_Core shard_core = null;
    private LogManager logManager;
    private GUIManager guiManager;
    private ConfigurationManager configurationManager;
    private ConnectionManager connectionManager;

    public Shard_Core(boolean headless, boolean dev) throws ClientInitializationException {
        if (shard_core != null) {
            throw new ClientInitializationException("There can only be one instance of Shard Core!");
        }
        shard_core = this;
        guiManager = new GUIManager(this);
        configurationManager = new ConfigurationManager(this);
        configurationManager.DEV_BUILD = dev;
        connectionManager = new ConnectionManager(this);
        configurationManager.headless = headless;
    }

    /**
     * Retrieve the object of ShardCore being used by the Shard. There can only
     * be one, it is static.
     *
     * @return Shard_Core object being used by the Shard
     */
    public static Shard_Core getShardCore() {
        return shard_core;
    }

    /**
     * Begin initialization of the Shard. When this method is done executing,
     * the Shard will be ready to connect to a Heart.
     */
    public void init() {
        if (ConfigurationManager.remoteLoggingInitialized) {
            return;
        }

        if (!configurationManager.headless) {
            guiManager.initGUI();
        }

        System.out.println("VERSION: " + configurationManager.SHARD_VERSION);

        initVariables();

        initLog();

        System.out.println("###############" + systemName + "###############");

        configurationManager.initCfg();
    }


    /**
     * Initialize variables being used for configuration files and logManager systems.
     * Other variables can be remoteLoggingInitialized here too.
     */
    private void initVariables() {
        ConfigurationManager.baseDir = System.getProperty("user.home") + ConfigurationManager.baseDir;
        ConfigurationManager.shardDir = ConfigurationManager.baseDir + ConfigurationManager.shardDir;
        ConfigurationManager.logBaseDir = ConfigurationManager.shardDir + ConfigurationManager.logBaseDir;
        ConfigurationManager.configDir = ConfigurationManager.shardDir + ConfigurationManager.configDir;
    }


    /**
     * Sets up the logManager system
     */
    private void initLog() {
        logManager = new LogManager();
        try {
            logManager.createLog(ConfigurationManager.logBaseDir);
            ConfigurationManager.logActive = true;

            // Start the logManager and initialize the text
            System.out.println("System logging enabled");
        } catch (SecurityException e) {
            System.out.println(
                    "Unable to access logManager file or directory because of permission settings. Will continue running without logs, however please reboot to set logs.\n");
        } catch (IOException e) {
            System.out.println(
                    "Unable to access find or create logManager on object creation. Will continue running without logs, however please reboot to set logs.\n");
        }
    }


    /**
     * Return the UUID of the Shard for use with networking with the Heart
     *
     * @return UUID of the Shard
     */
    public UUID getUUID() {
        return configurationManager.getUUID();
    }

    /**
     * Get the UUID of the Heart for network verification
     *
     * @return UUID of the Heart
     */
    public UUID getHeartUUID() {
        return configurationManager.heartUUID;
    }

    /**
     * Set the UUID of the Heart server
     *
     * @param uuid UUID of the Heart
     */
    public void setHeartUUID(UUID uuid) {
        configurationManager.heartUUID = uuid;
    }


    /**
     * Get active GUI Manager
     *
     * @return GUIManager object
     */
    public GUIManager getGuiManager() {
        return guiManager;
    }

    /**
     * Get active configuration manager
     *
     * @return ConfigurationManager object
     */
    public ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }

    /**
     * Get active connection manager
     *
     * @return ConnectionManager object
     */
    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    /**
     * Get active system info object
     *
     * @return SystemInfo object
     */
    public SystemInfo getSystemInfo() {
        return systemInfo;
    }

    /**
     * Get the active system log manager
     *
     * @return LogManager object
     */
    public LogManager getLogManager() {
        return logManager;
    }
}
