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
import Netta.Connection.Packet;
import Netta.Exceptions.SendPacketException;
import Shard.Manager.ConfigurationManager;
import Shard.Manager.ConnectionManager;
import Shard.Manager.GUIManager;
import Utilities.LogManager;
import Utilities.SystemInfo;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.UUID;

import static Shard.Manager.ConfigurationManager.systemName;

//import Utilities.Media.MediaPlayback;

/**
 * Core class for the Shard. Handles everything the Shard does
 */
public class Shard_Core {

    public static final String SHARD_VERSION = "0.1.6";
    public final static SystemInfo systemInfo = new SystemInfo();
    private static Shard_Core shard_core = null;
    private LogManager logManager;
    private GUIManager guiManager;
    private ConfigurationManager configurationManager;
    private ConnectionManager connectionManager;

    public Shard_Core(boolean headless) throws ClientInitializationException {
        if (shard_core != null) {
            throw new ClientInitializationException("There can only be one instance of Shard Core!");
        }
        shard_core = this;
        guiManager = new GUIManager(this);
        configurationManager = new ConfigurationManager(this);
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
        if (configurationManager.remoteLoggingInitialized) {
            return;
        }

        if (!configurationManager.headless) {
            guiManager.initGUI();
            redirectSystemStreams();
        }

        System.out.println("VERSION: " + SHARD_VERSION);

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
        configurationManager.baseDir = System.getProperty("user.home") + configurationManager.baseDir;
        configurationManager.shardDir = configurationManager.baseDir + configurationManager.shardDir;
        configurationManager.logBaseDir = configurationManager.shardDir + configurationManager.logBaseDir;
        configurationManager.configDir = configurationManager.shardDir + configurationManager.configDir;
    }


    /**
     * Sets up the logManager system
     */
    private void initLog() {
        logManager = new LogManager();
        try {
            logManager.createLog(configurationManager.logBaseDir);
            configurationManager.logActive = true;

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
     * Function to redirect standard output streams to the write function
     */
    private void redirectSystemStreams() {
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
     * @param msg Message to be displayed and written
     * @return Returns TRUE if successful at writing to the logManager, FALSE if not
     */
    private boolean println(String msg, Color color) {
        boolean success = true;

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                guiManager.appendToPane(guiManager.textArea, msg, color);
                guiManager.textArea.setCaretPosition(guiManager.textArea.getDocument().getLength());
                // textArea.append("\n");
            }
        });

        if (configurationManager.logActive) {
            try {
                logManager.write(msg);

                if (configurationManager.remoteLoggingInitialized) {
                    // LogManager packet to Heart
                    Packet p = new Packet(Packet.PACKET_TYPE.Message, configurationManager.uuid.toString());
                    p.packetString = msg;
                    connectionManager.sendPacket(p, true);
                }
            } catch (IOException e) {
                configurationManager.logActive = false;
                System.err.println(
                        "Unable to write to logManager. IOException thrown. Deactivating logManager file, please reboot to regain access.");
                success = false;
            } catch (SendPacketException ex) {
                configurationManager.remoteLoggingInitialized = false;
                System.err.println("Unable to send logManager packet to Heart. Error: " + ex.getMessage());
            }
        }

        return success;
    }
}
