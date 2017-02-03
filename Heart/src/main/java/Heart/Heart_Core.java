/**
 * @file Heart_Core.java
 * @author Austin VanAlstyne
 */
package Heart;

import Exceptions.ConfigurationException;
import Exceptions.ServerInitializationException;
import Netta.Connection.Packet;
import Netta.Exceptions.SendPacketException;
import Utilities.Config;
import Utilities.DNSSD;
import Utilities.Log;
import Utilities.Media.MediaManager;
import Utilities.SystemInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class Heart_Core {

    public final static boolean DEBUG = false;

    public final static String HEART_VERSION = "0.1.3";
    public static String SHARD_VERSION = "";

    public static String systemName = "CHS Heart", mediaDir = "", musicDir = "", movieDir = "", commandKey = "",
            baseDir = "/CrystalHomeSys/", heartDir = "Heart/", shardLogsDir = "Logs/", configDir = "heart_config.cfg",
            logBaseDir = "Logs/", shardFileDir = "Shard_Files/";
    public static boolean DEV_BUILD;
    private static boolean cfg_set = false, logActive = false, initialized = false;

    private boolean headless = false;

    // Server elements
    private static Heart_Core heart_core;
    private static Log log;
    private UUID uuid;
    private Config cfg = null;
    public Server server = null;
    private Thread serverThread = null;
    private Thread updateCheckerThread = null;
    private DNSSD dnssd;
    private int port;

    // GUI elements
    private JFrame frame;
    private static JTextArea textArea;
    private JLabel shardVersionLabel;

    // Media elements
    private MediaManager mediaManager;

    // System elements
    public final static SystemInfo systemInfo = new SystemInfo();

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
     * Initialize the Heart Server
     */
    public void Init() {
        if (initialized) {
            return;
        }

        if (!headless) {
            InitGUI();
            RedirectSystemStreams();
        }

        InitVariables();

        InitLog();

        InitCfg();

        InitMediaManager();

        InitPatchThread();
    }

    /**
     * Starts up the Server for this Heart Core object.
     *
     * @throws ServerInitializationException if there is an error creating the server for any reason. If
     *                                       the exception is thrown, abort attempt to create server
     */
    public void StartServer(int port) throws ServerInitializationException {
        this.port = port;
        try {
            // If the server object already has been initialized, or the server object has active connection
            if (server != null || server.IsConnectionActive()) {
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
                // TODO using a depreciated method to stop the server, not necissarily the best option
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
    private void RedirectSystemStreams() {
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                Write(String.valueOf((char) b), Color.BLACK);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                Write(new String(b, off, len), Color.BLACK);
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
                Write(String.valueOf((char) b), Color.RED);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                Write(new String(b, off, len), Color.RED);
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
    private void InitVariables() {
        // Sets the baseDir to the home directory
        baseDir = System.getProperty("user.home") + baseDir;

        heartDir = baseDir + heartDir;

        shardLogsDir = baseDir + shardLogsDir;

        logBaseDir = heartDir + logBaseDir;

        configDir = heartDir + configDir;

        shardFileDir = heartDir + shardFileDir;

        // TODO init music/movie dir's based on config
        mediaDir = "F:/Media";
        musicDir = "F:/Media/music";
        movieDir = "F:/Media/movies";

        // Share media folder with the network
        if (SystemInfo.getSystem_os() == SystemInfo.SYSTEM_OS.Windows) {
            String shareMediaFolder = "net share Media=" + mediaDir.replace("/", "\\") + " /GRANT:Everyone,FULL";
            try {
                Runtime.getRuntime().exec(shareMediaFolder);
            } catch (IOException e) {
                System.err.println("Error sharing the media folder with the network! Media access may not be available for Shards!");
            }
        } else if (SystemInfo.getSystem_os() == SystemInfo.SYSTEM_OS.Linux) {
            // TODO add linux folder sharing
        } else if (SystemInfo.getSystem_os() == SystemInfo.SYSTEM_OS.ERROR) {
            // TODO if not on a valid system
        }

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
    private void InitGUI() {
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
                StopHeartServer();
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

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(true);

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
    private void InitLog() {
        log = new Log();
        try {
            log.createLog(logBaseDir);
            logActive = true;

            // Start the log and initialize the text
            System.out.println("###############" + systemName + "###############");
            System.out.println("System logging enabled");
        } catch (SecurityException e) {
            System.err.println(
                    "Unable to access log file or directory because of permission settings. Will continue running without logs, however please reboot to set logs.\n");
        } catch (IOException e) {
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
    private void InitCfg() {
        System.out.println("Loading configuration file...");
        try {
            cfg = new Config(configDir);
        } catch (ConfigurationException e) {
            // TODO if the configuration isn't found, create and init it
        }

        // cfg_set = Boolean.parseBoolean(cfg.get("cfg_set"));
        if (cfg_set) {
            systemName = cfg.get("systemName");
            musicDir = cfg.get("musicDir");
            movieDir = cfg.get("moveDir");
            commandKey = cfg.get("commandKey");
            uuid = UUID.fromString(cfg.get("uuid"));
            System.out.println("Configuration file loaded.");
        } else {
            System.err.println("Please initialize the Heart Config with Nerv before proceeding!");
        }
    }

    /**
     * Initializes the media index thread to provide a usable list for shards
     */
    private void InitMediaManager() {
        mediaManager = new MediaManager(mediaDir, musicDir, movieDir);
        mediaManager.index(true, 30);
    }

    /**
     * Initialize and register the DNS_SD for the server
     */
    public void InitDNSSD() {
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
    private void InitPatchThread() {
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
    private boolean Write(final String msg, Color color) {
        boolean success = true;

        SwingUtilities.invokeLater(() -> {
            textArea.append(msg);
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

    /**
     * get the Heart_Core object
     *
     * @return Heart_Core object
     */
    public static Heart_Core GetCore() {
        return heart_core;
    }

    /**
     * get the Heart's UUID value
     *
     * @return UUID object that is equal to the Heart's UUID
     */
    public UUID GetUUID() {
        return uuid;
    }

    public MediaManager getMediaManager() {
        return mediaManager;
    }

    /**
     * Checks whether the Heart configuration file is set up or not
     *
     * @return true if the configuration is set up, else false.
     */
    public static boolean IsConfigSet() {
        return cfg_set;
    }

    /**
     * @return true if the server is still active, else false.
     */
    public boolean IsServerActive() {
        return server.IsConnectionActive();
    }

    public void notifyShardsOfUpdate() {
        for (ClientConnection cc : server.getClients()) {
            Packet p = new Packet(Packet.PACKET_TYPE.Message, null);
            p.packetString = "new patch";
            try {
                cc.SendPacket(p, true);
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
    public void StopHeartServer() {
        try {
            dnssd.closeRegisteredService();
        } catch (NullPointerException e) {
        }
        server.CloseConnections();
        dnssd = null;
        server = null;
        serverThread.stop();
        serverThread = null;
        mediaManager.close();
        new Thread(() -> {
            try {
                updateCheckerThread.join();
                updateCheckerThread = null;
            } catch (InterruptedException e) {
            }
        }).start();
    }
}
