/**
 * @file Config.java
 * @author Austin VanAlstyne
 */

package Utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import Exceptions.ConfigurationException;

public class Config {

	private Properties config = new Properties();
	private File configFile;

	/**
	 * Default Constructor
	 * 
	 * @param fullDir
	 *            Full path to the configuration file, including file name. Ex:
	 *            'C:\Users\Default\Desktop\config.cfg'
	 * @throws ConfigurationException
	 *             Throws if there is an issue creating or handling the
	 *             configuration file. Details of the error are found in the
	 *             message.
	 */
	public Config(String fullDir) throws ConfigurationException {
		configFile = new File(fullDir);

		Reload();
	}

	/**
	 * Check if the configuration file exists
	 * 
	 * @return true if it exists, else false.
	 */
	public boolean Exists() {
		return configFile.exists();
	}

	/**
	 * Retrieve the value of a setting in the config file.
	 * 
	 * @param key
	 *            associated with the setting
	 * @return String containing the value of the setting
	 */
	public String Get(String key) {
		return config.getProperty(key);
	}

	/**
	 * Set the value of a setting, or add a new setting, to the config file
	 * 
	 * @param key
	 *            you want associated with the setting. Ex: 'musicDir'
	 * @param value
	 *            you want stored. Ex: 'F:\Media\Music'
	 */
	public void Set(String key, String value) {
		config.setProperty(key, value);
	}

	/**
	 * Saves the configuration file
	 * 
	 * @throws ConfigurationException
	 *             if there is an issue saving the file. Details are in the
	 *             message.
	 */
	public synchronized void Save() throws ConfigurationException {
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
	 * Reload the configuration file and all of its settings.
	 * 
	 * @throws ConfigurationException
	 *             if there is an issue loading the file. Details are in the
	 *             message.
	 */
	public synchronized void Reload() throws ConfigurationException {
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
	 * Get the number of settings stored in the configuration file.
	 * 
	 * @return int value of number of settings.
	 */
	public int NumberOfSettings() {
		return config.size();
	}
}
