package Utilities;

/**
 * SystemInfo
 *
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
     *
     * ERROR is given when a valid system value wasn't found
     */
    public enum SYSTEM_OS {
        Windows, Linux, ERROR
    }
}
