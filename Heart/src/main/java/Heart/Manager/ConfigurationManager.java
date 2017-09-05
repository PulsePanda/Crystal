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

/*
 * This file is part of Crystal Home Systems.
 *
 * Crystal Home Systems is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Crystal Home Systems is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Crystal Home Systems. If not, see http://www.gnu.org/licenses/.
 */

package Heart.Manager;

import Exceptions.ConfigurationException;
import Heart.Heart_Core;
import Utilities.SettingsFileManager;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class ConfigurationManager {

    public final static String HEART_VERSION = "0.1.5";
    public static String SHARD_VERSION = "";
    public static String systemName = "CHS Heart", mediaDir = "", musicDir = "", movieDir = "", commandKey = "",
            baseDir = "/CrystalHomeSys/", heartDir = "Heart/", shardLogsDir = "Logs/", configDir = "heart_config.cfg",
            logBaseDir = "Logs/", shardFileDir = "Shard_Files/";
    public static boolean DEV_BUILD;
    private static boolean cfg_set = false;
    private static boolean logActive = false;
    protected SettingsFileManager cfg = null;
    protected UUID uuid;
    private Heart_Core c;

    public ConfigurationManager(Heart_Core heart_core) {
        this.c = heart_core;
    }

    public static boolean isLogActive() {
        return logActive;
    }

    public static void setLogActive(boolean logActive) {
        ConfigurationManager.logActive = logActive;
    }

    public static boolean isCfg_set() {
        return cfg_set;
    }

    /**
     * Set up the configuration file(s) for the server
     *
     * @throws ConfigurationException if there is an issue creating the configuration file. Details
     *                                will be in the exceptions message.
     */
    public void initCfg() {
        System.out.println("Loading configuration file...");

        try {
            // Try to load the configuration file
            cfg = new SettingsFileManager(configDir);
        } catch (ConfigurationException e) {
            // Create new base configuration file
            try {
                File configPath = new File(heartDir);
                configPath.mkdirs();
                configPath = new File(configDir);
                configPath.createNewFile();
                cfg = new SettingsFileManager(configDir);
                cfg.set("cfg_set", "False");
                cfg.save();
                System.out.println("Configuration file created.");
            } catch (IOException e1) {
                System.err.println("Unable to create configuration file!");
                return;
            } catch (ConfigurationException e1) {
                System.err.println("Unable to access configuration file. Error: " + e1.getMessage());
                return;
            }

        }

        // Load in the configuration settings
        cfg_set = Boolean.parseBoolean(cfg.get("cfg_set"));
        if (cfg_set) {
            loadCfg();
        } else {
            // If it doesn't exist, perform first time setup
            new FirstTimeSetupManager(c).start();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
            loadCfg();
        }
    }

    /**
     * Load the configuration file into appropriate variables
     */
    public void loadCfg() {
        systemName = cfg.get("systemName");
        mediaDir = cfg.get("mediaDir");
        musicDir = cfg.get("musicDir");
        movieDir = cfg.get("movieDir");
        commandKey = cfg.get("commandKey");
        uuid = UUID.fromString(cfg.get("uuid"));
        cfg_set = Boolean.valueOf(cfg.get("cfg_set"));

        // Load check
        if (systemName == null || systemName == "")
            System.err.println("Unable to load System Name from config file!");
        if (mediaDir == null || mediaDir == "")
            System.err.println("Unable to load root Media directory from config file!");
        if (musicDir == null || musicDir == "")
            System.err.println("Unable to load root Music directory from config file!");
        if (movieDir == null || movieDir == "")
            System.err.println("Unable to load root Movie directory from config file!");
        if (commandKey == null || commandKey == "")
            System.err.println("Unable to load Command Key from config file!");
        if (uuid == null || uuid.toString() == "")
            System.err.println("Unable to load UUID from config file!");
        if (cfg_set == false)
            System.err.println("Unable to load CFG_SET from config file!");


        System.out.println("Configuration file loaded.");
    }


    /**
     * Get the configuration object
     *
     * @return SettingsFileManager configuration object
     */
    public SettingsFileManager getCfg() {
        return cfg;
    }

    /**
     * get the Heart's UUID value
     *
     * @return UUID object that is equal to the Heart's UUID
     */
    public UUID getUUID() {
        return uuid;
    }


    /**
     * Checks whether the Heart configuration file is set up or not
     *
     * @return true if the configuration is set up, else false.
     */
    public boolean isConfigSet() {
        return cfg_set;
    }

}
