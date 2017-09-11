/*
 * This file is part of Crystal Home Systems.
 *
 * Crystal Home Systems is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Crystal Home Systems is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Crystal Home Systems. If not, see http://www.gnu.org/licenses/.
 */

/*
 * This file is part of Crystal Home Systems.
 *
 * Crystal Home Systems is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Crystal Home Systems is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Crystal Home Systems. If not, see http://www.gnu.org/licenses/.
 */

import java.security.NoSuchAlgorithmException;

public class MediaManager {

    public static boolean isServer = false, isClient = false;
    private static String IP = "", portS = "", filePath = "";
    private static int port;

    /**
     * Launch the MediaManager software. Command line args are listed below.
     *
     * @param args Command line initialization arguments. THIS ORDER MUST BE FOLLOWED! <br>
     *             -server Specifies if the software is a server.<br>
     *             -client Specifies if the software is a client.<br>
     *             -port xxxx Specifies the port being used by the software. MUST INCLUDE THIS WITH EITHER CLIENT OR SERVER <br>
     *             -ip xxx.xxx.x.xxx Specifies the IP address being used by the Client. MUST BE INCLUDED WITH CLIENT ONLY <br>
     *             -file /path/to/file Specifies the file path for the media being accessed. MUST BE INCLUDED WITH HEART ONLY <br>
     */
    public static void main(String[] args) {
        pullConnectionData(args);

        if (isServer) {
            try {
                new Thread(new Server(port, filePath)).start();
            } catch (NoSuchAlgorithmException e) {
                System.err.println("Unable to create media server on this port.");
            }
        }

        if (isClient) {
            try {
                new Thread(new Client(IP, port)).start();
            } catch (NoSuchAlgorithmException e) {
                System.err.println("Unable to create media client.");
            }
        }
    }

    /**
     * Pull and assign the command line arguments to specified variables
     *
     * @param args Command line arguments
     */
    private static void pullConnectionData(String[] args) {
        try {
            if (args[0].equals("-server")) {
                isServer = true;
            } else if (args[0].equals("-client")) {
                isClient = true;
            } else {
                System.err.println("You must define whether this is a client or server manager with -client or -server");
                System.exit(1);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("You must define whether this is a client or server manager with -client or -server");
            System.exit(1);
        }

        try {
            if (args[1].equals("-port")) {
                portS = args[2];
                try {
                    port = Integer.parseInt(portS);
                } catch (Exception e) {
                    System.err.println("Please specify a port number with -port xxxx");
                    System.exit(1);
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Please specify a port number with -port xxxx");
            System.exit(1);
        }

        if (isClient) {
            try {
                if (args[3].equals("-ip")) {
                    if (!args[4].contains(".")) {
                        System.err.println("Please provide a valid IP address after the -ip call.");
                        System.exit(1);
                    } else {
                        IP = args[4];
                    }
                } else {
                    System.err.println("Please specify an IP address with -ip xxx.xxx.x.xxx");
                    System.exit(1);
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                System.err.println("Please specify an IP address with -ip xxx.xxx.x.xxx");
                System.exit(1);
            }
        }

        if (isServer) {
            try {
                if (args[3].equals("-file")) {
                    filePath = args[4];
                } else {
                    System.err.println("Please specify a file path with -file /Path/To/File else");
                    System.exit(1);
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                System.err.println("Please specify a file path with -file /Path/To/File aioobe");
                System.exit(1);
            }
        }
    }
}
