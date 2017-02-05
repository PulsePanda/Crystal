package Utilities;

import java.io.IOException;

/**
 * UnZip
 *
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
