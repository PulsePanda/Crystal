/*
 * This file is part of Crystal Home Systems.
 *
 * Crystal Home Systems is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Crystal Home Systems is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Crystal Home Systems. If not, see http://www.gnu.org/licenses/.
 */

package Shard;

import Exceptions.ClientInitializationException;
import Exceptions.ConfigurationException;
import Netta.Connection.Packet;
import Netta.DNSSD;
import Netta.Exceptions.ConnectionException;
import Netta.Exceptions.SendPacketException;
import Utilities.Config;
import Utilities.Log;
import Utilities.Media.MediaPlayback;
import Utilities.ShardPatcher;
import Utilities.SystemInfo;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * Core class for the Shard. Handles everything the Shard does
 */
public class Shard_Core {

    // Shard version
    public static final String SHARD_VERSION = "0.1.6";
    // System elements
    public final static SystemInfo systemInfo = new SystemInfo();
    public static String SHARD_VERSION_SERVER = "";
    // Global variables
    public static String systemName = "CHS Shard", systemLocation = "", commandKey, baseDir = "/CrystalHomeSys/", shardDir = "Shard/",
            logBaseDir = "Logs/", configDir = "shard_config.cfg";
    public static boolean patchReady = false;
    private static boolean logActive = false, remoteLoggingInitialized = false;
    // private Client client;
    private static Shard_Core shard_core = null;
    private static JTextPane textArea;
    private final int dnssdPort = 6980;
    // Media Elements
    public MediaPlayback mediaPlayback;
    private ShardPatcher patcher;
    private boolean headless = false, cfg_set = false;
    private Log log;
    private Config cfg = null;
    private UUID uuid, heartUUID;
    private Client client = null;
    private Thread clientThread = null;
    private String IP = null;
    private int port;
    private DNSSD dnssd;
    // GUI elements
    private JFrame frame;
    private JPanel consolePanel, commandPanel;
    private JTabbedPane tabbedPane;
    private JLabel connectionStatus;

    public Shard_Core(boolean headless) throws ClientInitializationException {
        if (shard_core != null) {
            throw new ClientInitializationException("There can only be one instance of Shard Core!");
        }
        shard_core = this;
        this.headless = headless;
    }

    /**
     * Retrieve the object of ShardCore being used by the Shard. There can only
     * be one, it is static.
     *
     * @return Shard_Core object being used by the Shard
     */
    public static Shard_Core getShardCore() {
        return shard_core;
    }

    /**
     * Begin initialization of the Shard. When this method is done executing,
     * the Shard will be ready to connect to a Heart.
     */
    public void init() {
        if (remoteLoggingInitialized) {
            return;
        }

        if (!headless) {
            initGUI();
            redirectSystemStreams();
        }

        System.out.println("VERSION: " + SHARD_VERSION);

        initVariables();

        initLog();

        System.out.println("###############" + systemName + "###############");

        initCfg();
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

        // TODO have the error stream print red text
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
     * Initialize variables being used for configuration files and log systems.
     * Other variables can be remoteLoggingInitialized here too.
     */
    private void initVariables() {
        baseDir = System.getProperty("user.home") + baseDir;
        shardDir = baseDir + shardDir;
        logBaseDir = shardDir + logBaseDir;
        configDir = shardDir + configDir;

        dnssd = new DNSSD();

        mediaPlayback = new MediaPlayback();
    }

    /**
     * Patcher helper method. Initializes the Patcher class, checks if there is
     * an update to the Shard. GUI Elements will not be available until this
     * method is finished.
     * <p>
     * Called after clientThread is started
     */
    public synchronized void initPatcher() {
        if (!client.isConnectionActive()) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
            initPatcher();
            return;
        }

        SHARD_VERSION_SERVER = "";

        // Check shard version
        patcher = new ShardPatcher(client, ShardPatcher.PATCHER_TYPE.checkVersion);
        patcher.start();
        while (SHARD_VERSION_SERVER == "") {
            try {
                Thread.sleep(5);
            } catch (InterruptedException ex) {
            }
        }

        // download shard update (ShardPatcher will not download anything if
        // there's no update)
        patcher = new ShardPatcher(client, ShardPatcher.PATCHER_TYPE.downloadUpdate);
        patcher.start();
        while (patchReady == false) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException ex) {
            }
        }

        connectionStatus.setText("CONNECTED");
        connectionStatus.setForeground(Color.GREEN);
        remoteLoggingInitialized = true;
        try {
            swapID();
        } catch (SendPacketException e) {
            System.err.println("Error sending Heart ID information. Details: " + e.getMessage());
        }

        // Run shard update
        patcher = new ShardPatcher(client, ShardPatcher.PATCHER_TYPE.runUpdate);
        patcher.start();
    }

    /**
     * Create the GUI for the Shard. The GUI will handle everything from the
     * console output, being the System.out and System.err output, displaying
     * information from the Heart, and handling input from the user.
     */
    private void initGUI() {
        // Frame setup
        frame = new JFrame(systemName);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize((int) screenSize.getWidth(), (int) screenSize.getHeight());
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setLayout(null);
        tabbedPane = new JTabbedPane();
        tabbedPane.setBounds(0, 0, frame.getWidth(), frame.getHeight() - 10);

        // Command panel setup
        commandPanel = new JPanel();
        commandPanel.setLayout(new FlowLayout());

        connectionStatus = new JLabel("DISCONNECTED");
        connectionStatus.setForeground(Color.RED);

        JButton checkUpdate = new JButton("Check for Updates");
        checkUpdate.addActionListener(e -> {
            if (!patchReady)
                return;
            new Thread(new ShardConnectionThread(false, true)).start();
        });

        JButton goodMorning = new JButton("Good Morning");
        goodMorning.addActionListener(e -> {
            if (!patchReady)
                return;

            Packet p = new Packet(Packet.PACKET_TYPE.Command, uuid.toString());
            p.packetString = "Good Morning";
            try {
                client.sendPacket(p, true);
            } catch (SendPacketException ex) {
                System.err.println("Error sending Good Morning packet to Heart. Error: " + ex.getMessage());
            }
        });

        JButton btcPrice = new JButton("BTC Price");
        btcPrice.addActionListener(e -> {
            if (!patchReady)
                return;

            Packet p = new Packet(Packet.PACKET_TYPE.Command, uuid.toString());
            p.packetString = "BTC Price";
            try {
                client.sendPacket(p, true);
            } catch (SendPacketException ex) {
                System.err.println("Error sending BTC Price packet to Heart. Error: " + ex.getMessage());
            }
        });

        JButton weather = new JButton("Weather");
        weather.addActionListener(e -> {
            if (!patchReady)
                return;

            Packet p = new Packet(Packet.PACKET_TYPE.Command, uuid.toString());
            p.packetString = "Weather";
            try {
                client.sendPacket(p, true);
            } catch (SendPacketException ex) {
                System.err.println("Error sending Weather packet to Heart. Error: " + ex.getMessage());
            }
        });

        JButton playMusic = new JButton("Play Music");
        playMusic.addActionListener(e -> {
            if (!patchReady)
                return;

            Packet p = new Packet(Packet.PACKET_TYPE.Command, uuid.toString());
            p.packetString = "Play Music";
            String song = JOptionPane.showInputDialog(null, "Song name");
            p.packetStringArray = new String[]{song};
            try {
                client.sendPacket(p, true);
            } catch (SendPacketException e1) {
                System.err.println("Error sending Play Music packet to Heart. Error: " + e1.getMessage());
            }
        });

        JButton playMovie = new JButton("Play Movie");
        playMovie.addActionListener(e -> {
            if (!patchReady)
                return;

            Packet p = new Packet(Packet.PACKET_TYPE.Command, uuid.toString());
            p.packetString = "Play Movie";
            String movie = JOptionPane.showInputDialog(null, "Movie name");
            p.packetStringArray = new String[]{movie};
            try {
                client.sendPacket(p, true);
            } catch (SendPacketException e1) {
                System.err.println("Error sending Play Movie packet to Heart. Error: " + e1.getMessage());
            }
        });

        JButton stopMedia = new JButton("Stop Media");
        stopMedia.addActionListener(e -> {
            if (!patchReady)
                return;

            mediaPlayback.stop();
        });

        commandPanel.add(connectionStatus);
        commandPanel.add(checkUpdate);
        commandPanel.add(goodMorning);
        commandPanel.add(btcPrice);
        commandPanel.add(weather);
        commandPanel.add(playMusic);
        commandPanel.add(playMovie);
        commandPanel.add(stopMedia);

        // Console panel setup
        consolePanel = new JPanel();
        consolePanel.setLayout(new BorderLayout());

        JButton exitButton = new JButton("Shutdown");
        exitButton.addActionListener(new ActionListener() {
            boolean allowShutdown = true;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!allowShutdown) {
                    return;
                }

                allowShutdown = false;
                try {
                    stopShardClient();
                } catch (Exception ex) {
                    System.err.println("Error when closing the connection to the Heart. Error: " + ex.getMessage());
                }
                System.out.println("Shard is shut down and exiting");
                System.exit(0);
            }
        });
        exitButton.setBounds(new Rectangle(10, 10, 100, 40));

        textArea = new JTextPane();
        textArea.setEditable(false);
//        textArea.setLineWrap(true);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBounds(0, 60, frame.getWidth() - 5, frame.getHeight() - 115);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        consolePanel.add(exitButton, BorderLayout.NORTH);
        consolePanel.add(scrollPane, BorderLayout.CENTER);

        commandPanel.add(exitButton);

        // Finish GUI
        tabbedPane.addTab("Commands", commandPanel);
        tabbedPane.addTab("Console", consolePanel);
        frame.add(tabbedPane);
        frame.setVisible(true);
    }

    /**
     * Sets up the log system
     */
    private void initLog() {
        log = new Log();
        try {
            log.createLog(logBaseDir);
            logActive = true;

            // Start the log and initialize the text
            System.out.println("System logging enabled");
        } catch (SecurityException e) {
            System.out.println(
                    "Unable to access log file or directory because of permission settings. Will continue running without logs, however please reboot to set logs.\n");
        } catch (IOException e) {
            System.out.println(
                    "Unable to access find or create log on object creation. Will continue running without logs, however please reboot to set logs.\n");
        }
    }

    /**
     * Sets up the configuration file(s) for the Shard
     *
     * @throws ConfigurationException if there is an issue creating the configuration file. Details
     *                                will be in the exceptions message.
     */
    private void initCfg() {
        // TODO: This method is for loading local configuration files. However,
        // the Shard will have both local and "cloud" based
        // configuration files, making this method out of date. Update to solve
        // this issue
        System.out.println("Loading configuration file...");
        try {
            cfg = new Config(configDir);
        } catch (ConfigurationException e) {
            try {
                new File(configDir).createNewFile();
                cfg = new Config(configDir);
                cfg.set("cfg_set", "false");
                System.out.println("Configuration file created.");
            } catch (IOException e1) {
                System.err.println("Unable to create configuration file!");
            } catch (ConfigurationException e1) {
                System.err.println("Unable to access configuration file. Error: " + e1.getMessage());
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
        uuid = UUID.fromString(cfg.get("uuid"));
        systemName = cfg.get("systemName");
        systemLocation = cfg.get("systemLocation");

        System.out.println("Configuration file loaded.");
    }

    /**
     * Walk the user through the creation of the configuration values
     */
    private void createCfg() {
        cfg.set("uuid", UUID.randomUUID().toString());
        cfg.set("systemName", JOptionPane.showInputDialog(frame, "What do you want to call this device?"));
        cfg.set("systemLocation", JOptionPane.showInputDialog(frame, "Where is this device located in your home?"));
        cfg.set("cfg_set", "true");
        try {
            cfg.save();
        } catch (ConfigurationException e) {
            System.err.println("Error saving configuration file! Error: " + e.getMessage());
        }
    }

    /**
     * Used to start the Shard, create connection to it's Heart and initialize
     * the running thread.
     *
     * @throws ClientInitializationException thrown if there is an error creating the Client. Error
     *                                       details will be in the getMessage()
     */
    public void startShardClient() throws ClientInitializationException {
        try {
            if (client.isConnectionActive()) {
                throw new ClientInitializationException(
                        "Client is already remoteLoggingInitialized! Aborting attempt to create connection.");
            }
        } catch (NullPointerException e) {
            // If client is not remoteLoggingInitialized, initialize it
            try {
                // Start the search for dnssd service
                try {
                    dnssd.discoverService("_http._tcp.local.", InetAddress.getLocalHost());
                } catch (UnknownHostException e1) {
                }
                while (!dnssd.getServiceName().equals("Crystal Heart Server")) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                    }
                }

                // after search has finished, close the search
                dnssd.closeServiceDiscovery();

                // load the service info
                // service will be loaded as http://192.168.0.2:6666
                String serviceInfo = dnssd.getServiceInfo();
                String[] serviceSplit = serviceInfo.split("http://");
                String ipPort = serviceSplit[1]; // removes http://
                String[] ipPortSplit = ipPort.split(":"); // splits IP and port
                IP = ipPortSplit[0];
                port = Integer.parseInt(ipPortSplit[1]);

                client = new Client(IP, port);
            } catch (NoSuchAlgorithmException e1) {
                throw new ClientInitializationException(
                        "Unable to initialize client. Likely an issue loading RSA cipher. Aborting creation.");
            }
        }

        try {
            if (clientThread != null || clientThread.isAlive()) {
                stopShardClient();
                throw new ClientInitializationException(
                        "Client Thread is already remoteLoggingInitialized! Aborting attempt to create connection.");
            }
        } catch (NullPointerException e) {
            // If the clientThread isn't remoteLoggingInitialized, do nothing
            // ((re)-initialize below)
        } catch (ConnectionException ex) {
        }

        System.out.println("Connecting to Heart. IP: " + IP + " Port: " + port);
        clientThread = new Thread(client);
        clientThread.start();
    }

    public void startShardClientSuppressed() {
        try {
            startShardClient();
        } catch (ClientInitializationException ex) {
        }
    }

    /**
     * Exchange ID information with the Heart
     *
     * @throws SendPacketException thrown if there is an issue sending packets to Heart.
     *                             Details will be in getMessage()
     */
    public void swapID() throws SendPacketException {
        Packet p = new Packet(Packet.PACKET_TYPE.Command, uuid.toString());
        p.packetString = "uuid";
        p.packetStringArray = new String[]{systemName, systemLocation};
        client.sendPacket(p, true);
    }

    /**
     * Used to stop the Shard nicely. Sends a close connection packet to the Heart to
     * terminate connection, which will then terminate IO streams
     *
     * @throws ConnectionException thrown when there is an issue closing the IO streams to the
     *                             Heart. Error will be in the getMessage()
     */
    public void stopShardClient() throws ConnectionException {
        connectionStatus.setText("DISCONNECTED");
        connectionStatus.setForeground(Color.RED);
        try {
            Packet p = new Packet(Packet.PACKET_TYPE.CloseConnection, uuid.toString());
            p.packetString = "Manual disconnect";
            client.sendPacket(p, true);
            client.closeIOStreams();
            clientThread.stop();
            clientThread = null;
            IP = "";
            port = 0;
        } catch (SendPacketException e) {
            System.err.println("Error sending disconnect packet to Heart. Error: " + e.getMessage());
        }
    }

    /**
     * Used to stop the Shard without warning. Does the same thing as stopShardClient() without sending a packet to the
     * Heart
     */
    public void resetConnectionData() {
        connectionStatus.setText("DISCONNECTED");
        connectionStatus.setForeground(Color.RED);
        try {
            client.closeIOStreams();
        } catch (ConnectionException e) {
            System.err.println("Error closing client IO streams. Error: " + e.getMessage());
        }
        client = null;
        clientThread.stop();
        clientThread = null;
        IP = "";
        port = 0;
        remoteLoggingInitialized = false;
    }

    /**
     * Return the UUID of the Shard for use with networking with the Heart
     *
     * @return UUID of the Shard
     */
    public UUID getUUID() {
        return uuid;
    }

    /**
     * Get the UUID of the Heart for network verification
     *
     * @return UUID of the Heart
     */
    public UUID getHeartUUID() {
        return heartUUID;
    }

    /**
     * Set the UUID of the Heart server
     *
     * @param uuid UUID of the Heart
     */
    public void setHeartUUID(UUID uuid) {
        heartUUID = uuid;
    }

    /**
     * Check whether the Shards connection to the Heart is active or not
     *
     * @return boolean whether the connection is active
     */
    public boolean isActive() {
        return client.isConnectionActive();
    }

    /**
     * get the IP address being connected to by the Shard
     *
     * @return String IP address being connected to
     */
    public String getIP() {
        return IP;
    }

    /**
     * get the Port being connected to by the Shard
     *
     * @return int Port being connected to
     */
    public int getPort() {
        return port;
    }

    public ShardPatcher getPatcher() {
        return patcher;
    }

    public Client getClient() {
        return client;
    }

    /**
     * Send a packet to the Heart
     *
     * @param p         Packet to send
     * @param encrypted boolean True to encrypt packet, else false
     * @throws SendPacketException thrown if there is an error sending the Packet to the Heart.
     *                             Error will be in the getMessage()
     */
    public void sendPacket(Packet p, boolean encrypted) throws SendPacketException {
        client.sendPacket(p, encrypted);
    }

    /**
     * Writes to the Standard Output Stream, as well as calls 'write' on the
     * local log object
     *
     * @param msg Message to be displayed and written
     * @return Returns TRUE if successful at writing to the log, FALSE if not
     */
    private boolean println(String msg, Color color) {
        boolean success = true;

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                appendToPane(textArea, msg, color);
                textArea.setCaretPosition(textArea.getDocument().getLength());
                // textArea.append("\n");
            }
        });

        if (logActive) {
            try {
                log.write(msg);

                if (remoteLoggingInitialized) {
                    // Log packet to Heart
                    Packet p = new Packet(Packet.PACKET_TYPE.Message, uuid.toString());
                    p.packetString = msg;
                    client.sendPacket(p, true);
                }
            } catch (IOException e) {
                logActive = false;
                System.err.println(
                        "Unable to write to log. IOException thrown. Deactivating log file, please reboot to regain access.");
                success = false;
            } catch (SendPacketException ex) {
                remoteLoggingInitialized = false;
                System.err.println("Unable to send log packet to Heart. Error: " + ex.getMessage());
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
}
