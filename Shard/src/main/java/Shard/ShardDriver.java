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
import Exceptions.ConfigurationException;
import Shard.Manager.ConfigurationManager;
import Utilities.SettingsFileManager;

import java.io.File;
import java.io.IOException;

/**
 * Driver for Shard. Initializes needed objects and starts threads
 */
public class ShardDriver {

    private static Shard_Core shardCore;
    private static boolean headlessArg = false, dev = false;
    public static boolean connectionFileExists = true;
    private static SettingsFileManager sfm;
    public static String fileName = System.getProperty("user.home") + ConfigurationManager.baseDir + "Shard/con_inf.ini";

    public static void main(String[] args) {
        for (String s : args) {
            s = s.toLowerCase();
            switch (s) {
                case "-h":
                    headlessArg = true;
                    break;
                case "-dev":
                    dev = true;
                    break;
            }
        }

        startShard();
    }

    private static void startShard() {
        try {
            shardCore = new Shard_Core(headlessArg, dev);
            shardCore.init();
        } catch (ClientInitializationException ex) {
            System.err.println("Error starting Shard Core. Error: " + ex.getMessage());
        }
        String[] connectionInfo = new String[]{"localhost", "6987"};
        try {
            connectionInfo = getConnectionInfo();
        } catch (ConfigurationException e) {
            System.err.println("Error reading the connection information file. Details: " + e.getMessage());
        }
        try {
            shardCore.getConnectionManager().startConnectionThread(connectionInfo[0], Integer.parseInt(connectionInfo[1]));
        } catch (NumberFormatException e) {
            System.err.println("Unable to read connection file. Rewriting it.");
            connectionFileExists = false;
            shardCore.getConnectionManager().startConnectionThread("localhost", 6987);
        }
    }

    /**
     * Pull connection info from the connection information file for faster connection to heart. If there is no file, or no data,
     * default localhost information will be returned, which will then be ignored by the ConnectionManager
     *
     * @return String[] containing IP in index 0, and Port in index 1
     * @throws ConfigurationException thrown if there is an error creating/finding the connection file
     */
    private static String[] getConnectionInfo() throws ConfigurationException {
        String[] connectionInfo = new String[]{"localhost", "6987"};

        try {
            sfm = new SettingsFileManager(fileName);
        } catch (ConfigurationException e) {
            connectionFileExists = false;
            try {
                new File(fileName).createNewFile();
            } catch (IOException e1) {
                e1.printStackTrace();
                throw e;
            }
        }

        // if the connection file already existed, read from it
        if (connectionFileExists) {
            connectionInfo[0] = sfm.get("IP");
            connectionInfo[1] = sfm.get("port");
        }

        return connectionInfo;
    }

    public static SettingsFileManager getSFM() {
        return sfm;
    }
}
