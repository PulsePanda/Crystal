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

import Exceptions.ServerInitializationException;

import java.awt.*;

/**
 * Heart Driver. Starts the Heart service
 */
public class HeartDriver {

    private static Heart_Core heartCore;
    //    private static int corePort = 6987; // standard
    private static int corePort = 7789; // testing
    private static boolean headlessArg = false;
    // TODO set dev to default false
    static boolean dev = true;

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

        // init Heart networking, start listening for Shards
        try {
            System.out.println("Starting Heart server on heartPort " + corePort);
            heartCore.getServerManager().startHeartServer(corePort);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }

            // If the server is unable to host on the specified heartPort, try another one
            while (!heartCore.isServerActive()) {
                System.out.println("Attempting to host Heart server on heartPort " + ++corePort);
                heartCore.getServerManager().stopHeartServer();
                heartCore.getServerManager().startHeartServer(corePort);

                if (!heartCore.isServerActive())
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                    }
            }

            heartCore.getServerManager().initDNSSD();
        } catch (ServerInitializationException e) {
            System.err.println("Error starting Heart server! Error message: " + e.getMessage());
        }
    }
}
