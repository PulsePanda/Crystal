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

import Exceptions.ConfigurationException;

import java.io.*;
import java.util.Properties;

/**
 * Settings File Manager.
 * <p>
 * Used to handle configuration files for software
 */
public class SettingsFileManager {

    private Properties config = new Properties();
    private File configFile;

    /**
     * Default Constructor
     *
     * @param fullDir Full path to the configuration file, including file name. Ex:
     *                'C:\Users\Default\Desktop\config.cfg'
     * @throws ConfigurationException Throws if there is an issue creating or handling the
     *                                configuration file. Details of the error are found in the
     *                                message.
     */
    public SettingsFileManager(String fullDir) throws ConfigurationException {
        configFile = new File(fullDir);

        reload();
    }

    /**
     * Check if the configuration file exists
     *
     * @return true if it exists, else false.
     */
    public boolean exists() {
        return configFile.exists();
    }

    /**
     * Retrieve the value of a setting in the config file.
     *
     * @param key associated with the setting
     * @return String containing the value of the setting
     */
    public String get(String key) {
        return config.getProperty(key);
    }

    /**
     * set the value of a setting, or add a new setting, to the config file
     *
     * @param key   you want associated with the setting. Ex: 'musicDir'
     * @param value you want stored. Ex: 'F:\Media\Music'
     */
    public void set(String key, String value) {
        config.setProperty(key, value);
    }

    /**
     * Saves the configuration file
     *
     * @throws ConfigurationException if there is an issue saving the file. Details are in the
     *                                message.
     */
    public synchronized void save() throws ConfigurationException {
        try {
            FileWriter writer = new FileWriter(configFile);
            config.store(writer, "");
            writer.close();
        } catch (FileNotFoundException e) {
            throw new ConfigurationException("Unable to save the configuration file. File does not exist.");
        } catch (IOException e) {
            throw new ConfigurationException("Unable to save the configuration file. Error with the FileWriter.");
        }
    }

    /**
     * reload the configuration file and all of its settings.
     *
     * @throws ConfigurationException if there is an issue loading the file. Details are in the
     *                                message.
     */
    public synchronized void reload() throws ConfigurationException {
        try {
            FileReader reader = new FileReader(configFile);
            config.load(reader);
            reader.close();
        } catch (FileNotFoundException e) {
            throw new ConfigurationException("Reloading the configuration file failed. Unable to find the file.");
        } catch (IOException e) {
            throw new ConfigurationException("Reloading the configuration file failed. Error with the FileReader.");
        }
    }

    /**
     * get the number of settings stored in the configuration file.
     *
     * @return int value of number of settings.
     */
    public int numberOfSettings() {
        return config.size();
    }
}
