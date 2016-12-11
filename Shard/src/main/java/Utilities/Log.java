/**
 * Built to be used as a log system for CHS. Creates and edits log files, with file names based on the current system date.
 * 
 * @file Log.java
 * @author Austin VanAlstyne
 */

package Utilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {
	private BufferedWriter writer;

	/**
	 * Default Constructor
	 */
	public Log() {
	}

	/**
	 * Creates the log file for CHS
	 * 
	 * @param directory
	 *            Path to the directory the log will be located. Must end with
	 *            '/'.
	 * @throws SecurityException
	 *             Throws if the function is unable to access the log file
	 *             because of permission settings.
	 * @throws IOException
	 *             if the named file exists but is a directory rather than a
	 *             regular file, does not exist but cannot be created, or cannot
	 *             be opened for any other reason
	 */
	public void CreateLog(String directory) throws IOException {
		// Create currentDate
		String currentDate = "";
		DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
		Date date = new Date();
		currentDate = dateFormat.format(date);

		File file = new File(directory);
		if (!file.exists()) {
			file.mkdirs();
		}

		String finalDir = directory + currentDate + ".log";

		file = new File(finalDir);
		if (!file.exists()) {
			file.createNewFile();
		}

		writer = new BufferedWriter(new FileWriter(finalDir, true));
	}

	/**
	 * Write lines to the log file. Passed String is what gets written. This
	 * does not add a new line at the end of the write!
	 * 
	 * @param entry
	 *            String value of what needs to be written to the file
	 * @throws IOException
	 *             If an I/O error occurs
	 */
	public void Write(String entry) throws IOException {
		// Create currentTime
		String currentTime = "";
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		Date date = new Date();
		currentTime = dateFormat.format(date);

		if (writer != null) {
			writer.write(currentTime + "- " + entry);
			// writer.newLine();
			writer.flush();
		}
	}

	/**
	 * Closes all resources and objects associated with the class
	 * 
	 * @throws IOException
	 *             If an I/O error occurs
	 */
	public void Close() throws IOException {
		if (writer != null) {
			writer.close();
			writer = null;
		}
	}
}
