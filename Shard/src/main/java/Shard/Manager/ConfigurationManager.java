/*
 * This file is part of Crystal Home Systems.
 *
 * Crystal Home Systems is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Crystal Home Systems is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Crystal Home Systems. If not, see http://www.gnu.org/licenses/.
 */

package Shard.Manager;

import Exceptions.ConfigurationException;
import Shard.Shard_Core;
import Utilities.SettingsFileManager;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class ConfigurationManager {

    public static final String SHARD_VERSION = "0.1.7";
    public static String SHARD_VERSION_SERVER = "";
    // Global variables
    public static String systemName = "CHS Shard", systemLocation = "", commandKey, baseDir = "/CrystalHomeSys/", shardDir = "Shard/",
            logBaseDir = "Logs/", configDir = "shard_config.cfg";
    public static boolean logActive = false;
    public static boolean remoteLoggingInitialized = false;
    private final int dnssdPort = 6980;
    public boolean headless = false;
    public UUID uuid;
    public UUID heartUUID;
    public String IP = null;
    private Shard_Core c;
    private boolean cfg_set = false;
    private SettingsFileManager cfg = null;

    public ConfigurationManager(Shard_Core shard_core) {
        c = shard_core;
    }

    /**
     * Sets up the configuration file(s) for the Shard
     *
     * @throws ConfigurationException if there is an issue creating the configuration file. Details
     *                                will be in the exceptions message.
     */
    public void initCfg() {
        // TODO: This method is for loading local configuration files. However,
        // the Shard will have both local and "cloud" based
        // configuration files, making this method out of date. Update to solve
        // this issue
        System.out.println("Loading configuration file...");
        try {
            cfg = new SettingsFileManager(configDir);
        } catch (ConfigurationException e) {
            try {
                File configPath = new File(shardDir);
                configPath.mkdirs();
                configPath = new File(configDir);
                configPath.createNewFile();
                cfg = new SettingsFileManager(configDir);
                cfg.set("cfg_set", "false");
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

        cfg_set = Boolean.parseBoolean(cfg.get("cfg_set"));
        if (cfg_set) {
            loadCfg();
        } else {
            createCfg();
            loadCfg();
        }
    }

    /**
     * Load the configuration file into appropriate variables
     */
    private void loadCfg() {
        uuid = UUID.fromString(cfg.get("uuid"));
        systemName = cfg.get("systemName");
        systemLocation = cfg.get("systemLocation");

        System.out.println("Configuration file loaded.");
    }

    /**
     * Walk the user through the creation of the configuration values
     */
    private void createCfg() {
        cfg.set("uuid", UUID.randomUUID().toString());
        cfg.set("systemName", JOptionPane.showInputDialog(c.getGuiManager().frame, "What do you want to call this device?"));
        cfg.set("systemLocation", JOptionPane.showInputDialog(c.getGuiManager().frame, "Where is this device located in your home?"));
        cfg.set("cfg_set", "true");
        try {
            cfg.save();
        } catch (ConfigurationException e) {
            System.err.println("Error saving configuration file! Error: " + e.getMessage());
        }
    }

    /**
     * Return the UUID of the Shard for use with networking with the Heart
     *
     * @return UUID of the Shard
     */
    public UUID getUUID() {
        return uuid;
    }
}
