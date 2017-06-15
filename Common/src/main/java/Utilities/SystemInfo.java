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

/**
 * SystemInfo
 * <p>
 * Utility to get relevant system information
 */
public class SystemInfo {

    /**
     * Get the System's OS
     *
     * @return SYSTEM_OS if SYSTEM_OS.ERROR is returned, it did not match valid system operating systems
     */
    public static SYSTEM_OS getSystem_os() {
        String os = System.getProperty("os.name");
        os = os.toLowerCase();

        String[] params = null;
        if (os.contains("windows"))
            return SYSTEM_OS.Windows;
        else if (os.contains("linux"))
            return SYSTEM_OS.Linux;
        else
            return SYSTEM_OS.ERROR;
    }

    /**
     * Valid system values
     * <p>
     * ERROR is given when a valid system value wasn't found
     */
    public enum SYSTEM_OS {
        Windows, Linux, ERROR
    }
}
