/**
 * @file Shard_Core.java
 * @author Austin VanAlstyne
 */
package Shard;

import Exceptions.ClientInitializationException;
import Exceptions.ConfigurationException;
import Netta.Connection.Packet;
import Netta.Exceptions.ConnectionException;
import Netta.Exceptions.SendPacketException;
import Utilities.Config;
import Utilities.DNSSD;
import Utilities.Log;
import Utilities.Media.MediaPlayback;
import Utilities.SystemInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class Shard_Core {

    // Shard version
    public static final String SHARD_VERSION = "0.1.5";
    public static String SHARD_VERSION_SERVER = "";
    private ShardPatcher patcher;

    // Global variables
    public static String systemName = "CHS Shard", commandKey, baseDir = "/CrystalHomeSys/", shardDir = "Shard/",
            logBaseDir = "Logs/", configDir = "shard_config.cfg";
    private static boolean logActive = false, initialized = false;
    public static boolean patchReady = false;

    private boolean headless = false;

    // private Client client;
    private static Shard_Core shard_core = null;
    private Log log;
    private Config cfg = null;
    private UUID uuid;
    private Client client = null;
    private Thread clientThread = null;
    private String IP = null;
    private int port;
    private final int dnssdPort = 6980;
    private DNSSD dnssd;

    // GUI elements
    private JFrame frame;
    private static JTextArea textArea;
    private JPanel consolePanel, commandPanel;
    private JTabbedPane tabbedPane;

    // Media Elements
    public MediaPlayback mediaPlayback;

    // System elements
    public final static SystemInfo systemInfo = new SystemInfo();

    public Shard_Core(boolean headless) throws ClientInitializationException {
        if (shard_core != null) {
            throw new ClientInitializationException("There can only be one instance of Shard Core!");
        }
        shard_core = this;
        this.headless = headless;
    }

    /**
     * Begin initialization of the Shard. When this method is done executing,
     * the Shard will be ready to connect to a Heart.
     */
    public void init() {
        if (initialized) {
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
                println(String.valueOf((char) b));
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                println(new String(b, off, len));
            }

            @Override
            public void write(byte[] b) throws IOException {
                write(b, 0, b.length);
            }
        };

        System.setOut(new PrintStream(out, true));
        System.setErr(new PrintStream(out, true));
    }

    /**
     * Initialize variables being used for configuration files and log systems.
     * Other variables can be initialized here too.
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
        tabbedPane.setBounds(0, 0, frame.getWidth(), frame.getHeight());

        // Command panel setup
        commandPanel = new JPanel();
        commandPanel.setLayout(new FlowLayout());

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

            Packet p = new Packet(Packet.PACKET_TYPE.Command, "");
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

            Packet p = new Packet(Packet.PACKET_TYPE.Command, "");
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

            Packet p = new Packet(Packet.PACKET_TYPE.Command, "");
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

            Packet p = new Packet(Packet.PACKET_TYPE.Command, "");
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

            Packet p = new Packet(Packet.PACKET_TYPE.Command, "");
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

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(true);

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
            // TODO if the configuration isn't found, create and init it
        }

        // uuid = UUID.fromString(cfg.get("uuid"));
        if (uuid != null) {
            System.out.println("Configuration file loaded.");
        } else {
            System.err.println("Please initialize the Config with Nerv before proceeding!");
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
                        "Client is already initialized! Aborting attempt to create connection.");
            }
        } catch (NullPointerException e) {
            // If client is not initialized, initialize it
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
                        "Client Thread is already initialized! Aborting attempt to create connection.");
            }
        } catch (NullPointerException e) {
            // If the clientThread isn't initialized, do nothing
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
     * Used to stop the Shard. Sends a close connection packet to the Heart to
     * terminate connection, which will then terminate IO streams
     *
     * @throws ConnectionException thrown when there is an issue closing the IO streams to the
     *                             Heart. Error will be in the getMessage()
     */
    public void stopShardClient() throws ConnectionException {
        try {
            Packet p = new Packet(Packet.PACKET_TYPE.CloseConnection, null);
            p.packetString = "Manual disconnect";
            client.sendPacket(p, true);
            client.closeIOStreams();
            clientThread.join();
            clientThread = null;
            IP = "";
            port = 0;
        } catch (SendPacketException e) {
            System.err.println("Error sending disconnect packet to Heart. Error: " + e.getMessage());
        } catch (InterruptedException ex) {
        }
    }

    // TODO javadoc
    public void resetConnectionData() {
        IP = "";
        port = 0;
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
     * Return the UUID of the Shard for use with networking with the Heart
     *
     * @return UUID of the Shard
     */
    @Deprecated
    public UUID getUUID() {
        return uuid;
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
    private boolean println(String msg) {
        boolean success = true;

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                textArea.append(msg);
                textArea.setCaretPosition(textArea.getDocument().getLength());
                // textArea.append("\n");
            }
        });

        if (logActive) {
            try {
                if (initialized) {
                    // Log packet to Heart
                    Packet p = new Packet(Packet.PACKET_TYPE.Message, "");
                    p.packetString = msg;
                    client.sendPacket(p, true);
                }

                log.write(msg);
            } catch (IOException e) {
                logActive = false;
                System.err.println(
                        "Unable to write to log. IOException thrown. Deactivating log file, please reboot to regain access.");
                success = false;
            } catch (SendPacketException ex) {
                System.err.println("Unable to send log packet to Heart. Error: " + ex.getMessage());
            }
        }

        return success;
    }
}
