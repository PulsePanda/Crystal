package Utilities;

/**
 * Created by Austin on 1/31/2017.
 */
public class SystemInfo {

    public static SYSTEM_OS system_os;

    public SystemInfo() {
        String os = System.getProperty("os.name");
        os = os.toLowerCase();

        String[] params = null;
        if (os.contains("windows"))
            system_os = SYSTEM_OS.Windows;
        else if (os.contains("linux"))
            system_os = SYSTEM_OS.Linux;
    }

    public static enum SYSTEM_OS {
        Windows, Linux
    }
}
