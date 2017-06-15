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

import java.io.IOException;

/**
 * UnZip
 * <p>
 * Utility to unzip zip files into output folders
 */
public class UnZip {

    /**
     * UnZip the given zip file into the given output folder
     *
     * @param source      input Zip file
     * @param destination output folder
     * @throws IOException thrown if there is an error spawning the script
     */
    public static void unZip(String source, String destination) throws IOException {
        String[] params = null;
        if (SystemInfo.getSystem_os() == SystemInfo.SYSTEM_OS.Windows)
            params = new String[]{"py", "../lib/unzip.py", source, destination};
        else if (SystemInfo.getSystem_os() == SystemInfo.SYSTEM_OS.Linux)
            params = new String[]{"python3", "../lib/unzip.py", source, destination};

        Runtime.getRuntime().exec(params);
    }
}
