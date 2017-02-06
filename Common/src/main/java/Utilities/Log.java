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
 * Log manager. Creates and edits log files, with file names based on the current system date.
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
     * Creates the log file
     *
     * @param directory Path to the directory the log will be located. Must end with
     *                  '/'.
     * @return boolean True if log was created, else false
     * @throws IOException thrown if the named file exists but is a directory rather than a
     *                     regular file, does not exist but cannot be created, or cannot
     *                     be opened for any other reason
     */
    public void createLog(String directory) throws IOException {
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
     * write lines to the log file. Passed String is what gets written. This
     * does not add a new line at the end of the write!
     *
     * @param entry String value of what needs to be written to the file
     * @throws IOException If an I/O error occurs
     */
    public void write(String entry) throws IOException {
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
     * @throws IOException If an I/O error occurs
     */
    public void close() throws IOException {
        if (writer != null) {
            writer.close();
            writer = null;
        }
    }
}
