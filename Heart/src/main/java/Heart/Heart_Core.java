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
 * @file Heart_Core.java
 * @author Austin VanAlstyne
 */
package Heart;

import Exceptions.ConfigurationException;
import Exceptions.ServerInitializationException;
import Netta.Connection.Packet;
import Netta.DNSSD;
import Netta.Exceptions.SendPacketException;
import Utilities.*;
import Utilities.Media.MediaManager;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * Core Heart class. Handles every operation of the Server
 */
public class Heart_Core {

    public final static boolean DEBUG = false;

    public final static String HEART_VERSION = "0.1.4";
    // System elements
    public final static SystemInfo systemInfo = new SystemInfo();
    public static String SHARD_VERSION = "";
    public static String systemName = "CHS Heart", mediaDir = "", musicDir = "", movieDir = "", commandKey = "",
            baseDir = "/CrystalHomeSys/", heartDir = "Heart/", shardLogsDir = "Logs/", configDir = "heart_config.cfg",
            logBaseDir = "Logs/", shardFileDir = "Shard_Files/";
    public static boolean DEV_BUILD;
    private static boolean cfg_set = false, logActive = false, initialized = false;
    // Server elements
    private static Heart_Core heart_core;
    private static Log log;
    private static JTextPane textArea;
    private Server server = null;
    private boolean headless = false;
    private UUID uuid;
    private Config cfg = null;
    private Thread serverThread = null;
    private Thread updateCheckerThread = null;
    private DNSSD dnssd;
    private int port;
    // GUI elements
    private JFrame frame;
    private JLabel shardVersionLabel;
    // Media elements
    private MediaManager mediaManager;

    /**
     * Heart Core Default Constructor
     *
     * @param headless  boolean run in GUI mode
     * @param DEV_BUILD boolean run on dev build
     */
    public Heart_Core(boolean headless, boolean DEV_BUILD) {
        heart_core = this;
        this.headless = headless;
        this.DEV_BUILD = DEV_BUILD;
    }

    /**
     * get the Heart_Core object
     *
     * @return Heart_Core object
     */
    public static Heart_Core getCore() {
        return heart_core;
    }

    /**
     * Checks whether the Heart configuration file is set up or not
     *
     * @return true if the configuration is set up, else false.
     */
    public static boolean isConfigSet() {
        return cfg_set;
    }

    /**
     * Initialize the Heart Server
     */
    public void init() {
        if (initialized) {
            return;
        }

        if (!headless) {
            initGUI();
            redirectSystemStreams();
        }

        initVariables();

        initCfg();

        initLog();

        shareMediaDir();

        initMediaManager();

        initPatchThread();
    }

    /**
     * Starts up the Server for this Heart Core object.
     *
     * @throws ServerInitializationException if there is an error creating the server for any reason. If
     *                                       the exception is thrown, abort attempt to create server
     */
    public void startHeartServer(int port) throws ServerInitializationException {
        this.port = port;
        try {
            // If the server object already has been initialized, or the server object has active connection
            if (server != null || server.isServerActive()) {
                throw new ServerInitializationException(
                        "Server is already initialized. Cannot create new server on this object. Aborting creation.");
            }
        } catch (NullPointerException e) {
            // If server is not set, this will execute.
            try {
                server = new Server(port);
            } catch (NoSuchAlgorithmException e1) {
                throw new ServerInitializationException(
                        "Unable to initialize server. Likely an issue loading RSA cipher. Aborting creation.");
            }
        }
        try {
            // If the server thread is initialized or alive
            if (serverThread != null || serverThread.isAlive()) {
                System.err.println(
                        "This instance of Heart Core already has an active Server Thread. Attempting to close the thread...");
                // Try to close the server thread
                // TODO using a depreciated method to stop the server, not necessarily the best option
                serverThread.stop();
            }
        } catch (NullPointerException e) {
            // If serverThread is not set, this will throw.
            // That's fine, we don't need to do anything
        }

        // Start the server
        serverThread = new Thread(server);
        serverThread.start();
    }

    /**
     * Function to redirect standard output streams to the write function
     */
    private void redirectSystemStreams() {
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                println(String.valueOf((char) b), Color.BLACK);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                println(new String(b, off, len), Color.BLACK);
            }

            @Override
            public void write(byte[] b) throws IOException {
                write(b, 0, b.length);
            }
        };

        OutputStream err = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                println(String.valueOf((char) b), Color.RED);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                println(new String(b, off, len), Color.RED);
            }

            @Override
            public void write(byte[] b) throws IOException {
                write(b, 0, b.length);
            }
        };

        System.setOut(new PrintStream(out, true));
        System.setErr(new PrintStream(err, true));
    }

    /**
     * Sets up the initial variables for directories
     */
    private void initVariables() {
        // Sets the baseDir to the home directory
        baseDir = System.getProperty("user.home") + baseDir;

        heartDir = baseDir + heartDir;

        shardLogsDir = baseDir + shardLogsDir;

        logBaseDir = heartDir + logBaseDir;

        shardFileDir = heartDir + shardFileDir;

        configDir = heartDir + configDir;

        updateShardVersion();
    }

    /**
     * Pull the Shard version from the local ShardVersion file and apply it to the local variable for use
     */
    public void updateShardVersion() {
        try {
            File file = new File(heartDir + "ShardVersion");
            if (!file.exists())
                file.createNewFile();

            FileReader fileReader = new FileReader(heartDir + "ShardVersion");

            BufferedReader bufferedReader = new BufferedReader(fileReader);
            SHARD_VERSION = bufferedReader.readLine();
            bufferedReader.close();
            shardVersionLabel.setText("Shard_Version: " + SHARD_VERSION);
        } catch (FileNotFoundException ex) {
            System.err.println("Unable to find local ShardVersion file located at: " + heartDir + "ShardVersion");
        } catch (IOException ex) {
            System.err.println("Unable to read local ShardVersion file located at: " + heartDir + "ShardVersion");
        }
    }

    /**
     * Sets up and starts the GUI associated with the Heart
     */
    private void initGUI() {
        frame = new JFrame(systemName);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setLayout(null);

        JButton exitButton = new JButton("Shutdown");
        exitButton.addActionListener(new ActionListener() {
            boolean allowShutdown = true;

            public void actionPerformed(ActionEvent e) {
                if (!allowShutdown) {
                    return;
                }

                allowShutdown = false;
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                stopHeartServer();
                System.out.println("");
                System.out.println("IT IS NOW SAFE TO CLOSE THE WINDOW");
                System.out.println("");
            }
        });
        exitButton.setBounds(new Rectangle(10, 10, 100, 40));

        JButton checkUpdate = new JButton("Check for Updates");
        checkUpdate.addActionListener(e -> new Thread(new UpdateCheckerThread(false, false)).start());
        checkUpdate.setBounds(new Rectangle(120, 10, 140, 40));

        JButton forceUpdate = new JButton("Force Update");
        forceUpdate.addActionListener(e -> new Thread(new UpdateCheckerThread(false, true)).start());
        forceUpdate.setBounds(new Rectangle(270, 10, 110, 40));

        JButton forceIndex = new JButton("Force Index");
        forceIndex.addActionListener(e -> mediaManager.index(false, 0));
        forceIndex.setBounds(new Rectangle(390, 10, 100, 40));

        textArea = new JTextPane();
        textArea.setEditable(false);
//        textArea.setLineWrap(true);

        JButton clearLog = new JButton("Clear Log");
        clearLog.addActionListener(e -> textArea.setText(""));
        clearLog.setBounds(new Rectangle(500, 10, 100, 40));

        JLabel heartVersionLabel = new JLabel("Heart_Version: " + HEART_VERSION);
        heartVersionLabel.setBounds(new Rectangle(650, 5, 120, 25));

        shardVersionLabel = new JLabel("Shard_Version: " + SHARD_VERSION);
        shardVersionLabel.setBounds(new Rectangle(650, 35, 120, 25));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBounds(0, 60, frame.getWidth() - 5, frame.getHeight() - 85);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        frame.getContentPane().add(exitButton);
        frame.getContentPane().add(checkUpdate);
        frame.getContentPane().add(forceUpdate);
        frame.getContentPane().add(forceIndex);
        frame.getContentPane().add(clearLog);
        frame.getContentPane().add(heartVersionLabel);
        frame.getContentPane().add(shardVersionLabel);
        frame.getContentPane().add(scrollPane);
        frame.setVisible(true);
    }

    /**
     * set up the logging system
     */
    private void initLog() {
        log = new Log();
        try {
            log.createLog(logBaseDir);
            logActive = true;

            // Start the log and initialize the text
            System.out.println("###############" + systemName + "###############");
            System.out.println("System logging enabled");
        } catch (SecurityException e) {
            System.out.println("###############" + systemName + "###############");
            System.err.println(
                    "Unable to access log file or directory because of permission settings. Will continue running without logs, however please reboot to set logs.\n");
        } catch (IOException e) {
            System.out.println("###############" + systemName + "###############");
            System.err.println(
                    "Unable to access find or create log on object creation. Will continue running without logs, however please reboot to set logs.\n");
        }
    }

    /**
     * set up the configuration file(s) for the server
     *
     * @throws ConfigurationException if there is an issue creating the configuration file. Details
     *                                will be in the exceptions message.
     */
    private void initCfg() {
        System.out.println("Loading configuration file...");
        try {
            cfg = new Config(configDir);
        } catch (ConfigurationException e) {
            try {
                File configPath = new File(heartDir);
                configPath.mkdirs();
                configPath = new File(configDir);
                configPath.createNewFile();
                cfg = new Config(configDir);
                cfg.set("cfg_set", "False");
                cfg.save();
                System.out.println("Configuration file created.");
            } catch (IOException e1) {
                System.err.println("Unable to create configuration file!");
                return;
            } catch (ConfigurationException e1) {
                System.err.println("Unable to access configuration file. Error: " + e1.getMessage());
                return;
            }

        }

        cfg_set = Boolean.parseBoolean(cfg.get("cfg_set"));
        if (cfg_set) {
            loadCfg();
        } else {
            createCfg();
            loadCfg();
        }
    }

    /**
     * Load the configuration file into appropriate variables
     */
    private void loadCfg() {
        systemName = cfg.get("systemName");
        mediaDir = cfg.get("mediaDir");
        musicDir = cfg.get("musicDir");
        movieDir = cfg.get("movieDir");
        commandKey = cfg.get("commandKey");
        uuid = UUID.fromString(cfg.get("uuid"));

        // Load check
        if (systemName == null || systemName == "")
            System.err.println("Unable to load System Name from config file!");
        if (mediaDir == null || mediaDir == "")
            System.err.println("Unable to load root Media directory from config file!");
        if (musicDir == null || musicDir == "")
            System.err.println("Unable to load root Music directory from config file!");
        if (movieDir == null || movieDir == "")
            System.err.println("Unable to load root Movie directory from config file!");
        if (commandKey == null || commandKey == "")
            System.err.println("Unable to load Command Key from config file!");
        if (uuid == null || uuid.toString() == "")
            System.err.println("Unable to load UUID from config file!");


        System.out.println("Configuration file loaded.");
    }

    /**
     * Walk the user through the creation of the configuration values
     */
    private void createCfg() {
        JOptionPane.showMessageDialog(frame, "The configuration file hasn't been set up.\nThis will walk through the setup.");
        String systemName = JOptionPane.showInputDialog(frame, "What do you want to call this device?");
        JOptionPane.showMessageDialog(frame, "Enter the root media folder.");
        String mediaDir = FileChooser.chooseFile();

        String musicDir;
        if (new File(mediaDir + "/music").exists()) {
            musicDir = mediaDir + "/music";
        } else if (new File(mediaDir + "/songs").exists()) {
            musicDir = mediaDir + "/songs";
        } else {
            JOptionPane.showMessageDialog(frame, "Enter the root music folder.");
            musicDir = FileChooser.chooseFile();
        }

        String movieDir;
        if (new File(mediaDir + "/movie").exists()) {
            movieDir = mediaDir + "/movie";
        } else if (new File(mediaDir + "/movies").exists()) {
            movieDir = mediaDir + "/movies";
        } else {
            JOptionPane.showMessageDialog(frame, "Enter the root movie folder.");
            movieDir = FileChooser.chooseFile();
        }

        String commandKey = JOptionPane.showInputDialog(frame, "What voice command will you use to wake up Crystal?");

        String uuid = UUID.randomUUID().toString();

        String mediaIndexDelay = JOptionPane.showInputDialog(frame, "How often do you want to index your media library? (In Minutes)");

        String updateCheckDelay = JOptionPane.showInputDialog(frame, "How often do you want to check for software updates? (In Minutes)");

        cfg.set("cfg_set", "True"); // TODO nullpointerexception thrown on kayleighs computer on config creation, before and after input helper
        cfg.set("systemName", systemName);
        cfg.set("mediaDir", mediaDir);
        cfg.set("musicDir", musicDir);
        cfg.set("movieDir", movieDir);
        cfg.set("commandKey", commandKey);
        cfg.set("uuid", uuid);
        cfg.set("mediaIndexDelay", mediaIndexDelay);
        cfg.set("updateCheckDelay", updateCheckDelay);
        try {
            cfg.save();
        } catch (ConfigurationException e) {
            System.err.println("Error saving settings to the config file. Error: " + e.getMessage());
            JOptionPane.showMessageDialog(frame, "Error saving settings to the config file. Error: " + e.getMessage());
        }
    }

    /**
     * Share the root media directory with the network
     */
    private void shareMediaDir() {
        // Share media folder with the network
        if (SystemInfo.getSystem_os() == SystemInfo.SYSTEM_OS.Windows) {
            String shareMediaFolder = "net share Media=" + mediaDir + " /GRANT:Everyone,FULL";
            try {
                Runtime.getRuntime().exec(shareMediaFolder);
            } catch (IOException e) {
                System.err.println("Error sharing the media folder with the network! Media access may not be available for Shards!");
            }
        } else if (SystemInfo.getSystem_os() == SystemInfo.SYSTEM_OS.Linux) {
            // TODO add linux folder sharing
            System.err.println("Crystal doesn't know how to share your media folder on this system! Please share folder manually!");
        } else if (SystemInfo.getSystem_os() == SystemInfo.SYSTEM_OS.ERROR) {
            System.err.println("Crystal doesn't know how to share your media folder on this system! Please share folder manually!");
        }
    }

    /**
     * Initializes the media index thread to provide a usable list for shards
     */
    private void initMediaManager() {
        if (cfg_set) {
            mediaManager = new MediaManager(mediaDir, musicDir, movieDir);
            mediaManager.index(true, Integer.parseInt(cfg.get("mediaIndexDelay")));
        } else {
            System.err.println("Configuration file was not found. Media management is unavailable until the configuration is set up.");
        }
    }

    /**
     * Initialize and register the DNS_SD for the server
     */
    public void initDNSSD() {
        dnssd = new DNSSD();
        try {
            dnssd.registerService("_http._tcp.local.", "Crystal Heart Server", port,
                    "Heart Core Server DNS Service", InetAddress.getLocalHost());
        } catch (UnknownHostException e) {
        }
    }

    /**
     * Initialize and start the UpdateCheckerThread for on-launch use
     */
    private void initPatchThread() {
        updateCheckerThread = new UpdateCheckerThread(true, false);
        updateCheckerThread.start();
    }

    /**
     * Writes to the Standard Output Stream, as well as calls 'write' on the
     * local log object
     *
     * @param msg   String message to be displayed and written
     * @param color Color to set the line of text
     * @return Returns TRUE if successful at writing to the log, FALSE if not
     */
    private boolean println(final String msg, Color color) {
        boolean success = true;

        SwingUtilities.invokeLater(() -> {
            appendToPane(textArea, msg, color);
            textArea.setCaretPosition(textArea.getDocument().getLength());
        });

        if (logActive) {
            try {
                log.write(msg);
            } catch (IOException e) {
                logActive = false;
                System.err.println(
                        "Unable to write to log. IOException thrown. Deactivating log file, please reboot to regain access.");
                success = false;
            }
        }

        return success;
    }

    private void appendToPane(JTextPane tp, String msg, Color c) {
        tp.setEditable(true);
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);

        aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Lucida Console");
        aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);

        int len = tp.getDocument().getLength();
        tp.setCaretPosition(len);
        tp.setCharacterAttributes(aset, false);
        tp.replaceSelection(msg);
        tp.setEditable(false);
    }

    /**
     * get the Heart's UUID value
     *
     * @return UUID object that is equal to the Heart's UUID
     */
    public UUID getUUID() {
        return uuid;
    }

    public MediaManager getMediaManager() {
        return mediaManager;
    }

    /**
     * Get the configuration object
     *
     * @return Config configuration object
     */
    public Config getCfg() {
        return cfg;
    }

    /**
     * @return true if the server is still active, else false.
     */
    public boolean isServerActive() {
        return server.isServerActive();
    }

    public void notifyShardsOfUpdate() {
        System.out.println("Notifying Shards of update.");
        for (ClientConnection cc : server.getClients()) {
            Packet p = new Packet(Packet.PACKET_TYPE.Message, uuid.toString());
            p.packetString = "new patch";
            try {
                cc.sendPacket(p, true);
            } catch (SendPacketException e) {
                System.err.println("Error sending update notification to shard. Details: " + e.getMessage());
            }
        }
    }

    /**
     * Stop the Heart server.
     * <p>
     * Unregisters the DNS_SD service on the network
     * Closes server connections
     * Stops the server thread
     * Closes the Media Manager
     * Stops the Update Checker
     * Sets all variables to null
     */
    @SuppressWarnings("deprecation")
    public void stopHeartServer() {
        try {
            dnssd.closeRegisteredService();
        } catch (NullPointerException e) {
        }
        try {
            server.closeConnections();
        } catch (NullPointerException e) {
        }
        dnssd = null;
        server = null;
        try {
            serverThread.stop();
        } catch (NullPointerException e) {
        }
        serverThread = null;
        try {
            mediaManager.close();
        } catch (NullPointerException e) {
        }
        try {
            updateCheckerThread.stop();
        } catch (NullPointerException e) {
        }
        updateCheckerThread = null;
    }
}
