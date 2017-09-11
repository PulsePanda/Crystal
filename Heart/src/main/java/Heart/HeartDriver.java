/*
 * This file is part of Crystal Home Systems.
 *
 * Crystal Home Systems is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Crystal Home Systems is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Crystal Home Systems. If not, see http://www.gnu.org/licenses/.
 */

package Heart;

import Exceptions.ConfigurationException;
import Exceptions.ServerInitializationException;
import Heart.Manager.ConfigurationManager;
import Utilities.MediaManagerProcessBuilder;
import Utilities.SettingsFileManager;

import java.io.File;
import java.io.IOException;

/**
 * Heart Driver. Starts the Heart service
 */
public class HeartDriver {

    // TODO set dev to default false
    static boolean dev = true;
    private static Heart_Core heartCore;
    //    private static int corePort = 6987; // standard
    private static int corePort = 7789; // testing
    private static boolean headlessArg = false, connectionFileExists = true;
    static String fileName = System.getProperty("user.home") + ConfigurationManager.baseDir + "Heart/con_inf.ini";
    static SettingsFileManager sfm;

    public static void main(String[] args) {
        for (String s : args) {
            s = s.toLowerCase();
            switch (s) {
                case "-h":
                    headlessArg = true;
                    break;
                case "-dev":
                    dev = true;
            }
        }

        init();
    }

    private static void init() {
        heartCore = new Heart_Core(headlessArg, dev);
        heartCore.init();

        int hostPort = corePort;
        try {
            hostPort = getPort();
        } catch (ConfigurationException e) {
            System.err.println("Error retrieving the connection file information. Details: " + e.getMessage());
        }

        // init Heart networking, start listening for Shards
        try {
            System.out.println("Starting Heart server on heartPort " + hostPort);
            heartCore.getServerManager().startHeartServer(hostPort);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }

            // If the server is unable to host on the specified heartPort, try another one
            while (!heartCore.isServerActive()) {
                System.out.println("Attempting to host Heart server on heartPort " + ++hostPort);
                heartCore.getServerManager().stopHeartServer();
                heartCore.getServerManager().startHeartServer(hostPort);

                if (!heartCore.isServerActive())
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                    }
            }

            sfm.set("port", Integer.toString(hostPort));
            sfm.save();
            heartCore.getServerManager().initDNSSD();

//            try {
//                new MediaManagerProcessBuilder(new String[]{"-server", "-port", Integer.toString(hostPort), "-file", "E:/Media/music/Linkin Park/Not Alone.mp3"}).start();
//            } catch (IOException e) {
//                System.err.println("Unable to start MediaManager Process. StackTrace:");
//                e.printStackTrace();
//            }
        } catch (ServerInitializationException e) {
            System.err.println("Error starting Heart server! Error message: " + e.getMessage());
        } catch (ConfigurationException | NullPointerException e) {
            System.err.println("Error saving the connection information into the connection file.");
        }
    }

    /**
     * Try to read the connection file and retrieve the port. If the file cannot be read, it will return corePort
     *
     * @return Integer port value
     * @throws ConfigurationException if the connection file cannot be found/created/saved to
     */
    private static int getPort() throws ConfigurationException {
        int port = corePort;
        try {
            sfm = new SettingsFileManager(fileName);
        } catch (ConfigurationException e) {
            connectionFileExists = false;
            try {
                new File(fileName).createNewFile();
                return getPort();
            } catch (IOException e1) {
                throw e;
            }
        }

        if (!connectionFileExists) {
            sfm.set("port", Integer.toString(port));
            sfm.save();
            return port;
        }

        // if the connection file already existed, read from it
        if (connectionFileExists) {
            port = Integer.parseInt(sfm.get("port"));
        }

        return port;
    }
}
