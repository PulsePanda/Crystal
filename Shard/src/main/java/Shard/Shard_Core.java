/**
 * @file Shard_Core.java
 * @author Austin VanAlstyne
 */
package Shard;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import Exceptions.ClientInitializationException;
import Exceptions.ConfigurationException;
import Netta.Connection.Packet;
import Netta.Exceptions.ConnectionException;
import Netta.Exceptions.SendPacketException;
import Utilities.Config;
import Utilities.Log;
import Utilities.Media.MediaPlayback;
import Utilities.Media.Music;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class Shard_Core {

	public static final String SHARD_VERSION = "0.1.1";
	public static String SHARD_VERSION_SERVER = "";
	private ShardPatcher patcher;

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
	private String IP = "localhost";
	private int port = 6987;

	// GUI elements
	private JFrame frame;
	private static JTextArea textArea;
	private JPanel consolePanel, commandPanel;
	private JTabbedPane tabbedPane;

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
	public void Init() {
		if (initialized) {
			return;
		}

		if (!headless) {
			InitGUI();
			RedirectSystemStreams();
		}

		System.out.println("VERSION: " + SHARD_VERSION);

		InitVariables();

		InitLog();

		InitCfg();
	}

	/**
	 * Function to redirect standard output streams to the Write function
	 */
	private void RedirectSystemStreams() {
		OutputStream out = new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				Write(String.valueOf((char) b));
			}

			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				Write(new String(b, off, len));
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
	private void InitVariables() {
		baseDir = System.getProperty("user.home") + baseDir;
		shardDir = baseDir + shardDir;
		logBaseDir = shardDir + logBaseDir;
		configDir = shardDir + configDir;
	}

	/**
	 * Patcher helper method. Initializes the Patcher class, checks if there is
	 * an update to the Shard. GUI Elements will not be available until this
	 * method is finished.
	 *
	 * Called after clientThread is started
	 */
	public synchronized void InitPatcher() {
		if (!client.IsConnectionActive()) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
			}
			InitPatcher();
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
	private void InitGUI() {
		// Frame setup
		frame = new JFrame(systemName);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.setSize(800, 600);
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setLayout(null);
		tabbedPane = new JTabbedPane();
		tabbedPane.setBounds(0, 0, 800, 600);

		// Command panel setup
		commandPanel = new JPanel();
		commandPanel.setLayout(new FlowLayout());

		JButton checkUpdate = new JButton("Check for Updates");
		checkUpdate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!patchReady)
					return;
				InitPatcher();
			}
		});

		JButton goodMorning = new JButton("Good Morning");
		goodMorning.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!patchReady)
					return;

				Packet p = new Packet(Packet.PACKET_TYPE.Command, "");
				p.packetString = "Good Morning";
				try {
					client.SendPacket(p, true);
				} catch (SendPacketException ex) {
					System.err.println("Error sending Good Morning packet to Heart. Error: " + ex.getMessage());
				}
			}
		});

		JButton btcPrice = new JButton("BTC Price");
		btcPrice.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!patchReady)
					return;

				Packet p = new Packet(Packet.PACKET_TYPE.Command, "");
				p.packetString = "BTC Price";
				try {
					client.SendPacket(p, true);
				} catch (SendPacketException ex) {
					System.err.println("Error sending BTC Price packet to Heart. Error: " + ex.getMessage());
				}
			}
		});

		JButton weather = new JButton("Weather");
		weather.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!patchReady)
					return;

				Packet p = new Packet(Packet.PACKET_TYPE.Command, "");
				p.packetString = "Weather";
				try {
					client.SendPacket(p, true);
				} catch (SendPacketException ex) {
					System.err.println("Error sending Weather packet to Heart. Error: " + ex.getMessage());
				}
			}
		});

		JButton playMusic = new JButton("Play Music");
		playMusic.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Packet p = new Packet(Packet.PACKET_TYPE.Command, "");
				p.packetString = "Play Music";
				// try {
				// client.SendPacket(p, true);
				// } catch (SendPacketException e1) {
				// System.err.println("Error sending Weather packet to Heart.
				// Error: " + e1.getMessage());
				// }
				new MediaPlayback(new Music("http://www.ntonyx.com/mp3files/Morning_Flower.mp3")).start();
			}
		});

		commandPanel.add(checkUpdate);
		commandPanel.add(goodMorning);
		commandPanel.add(btcPrice);
		commandPanel.add(weather);
		commandPanel.add(playMusic);

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
					StopShardClient();
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

		// JTextField commandField = new JTextField();
		// commandField.setBounds(0, frame.getHeight() - 50, frame.getWidth() -
		// 5, 25);
		// commandField.addActionListener(new AbstractAction() {
		// @Override
		// public void actionPerformed(ActionEvent arg0) {
		// String commandText = commandField.getText();
		// System.out.println("Input Command: " + commandText);
		// command.AnalyzeCommand(commandText);
		// commandField.setText("");
		// }
		// });
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
	private void InitLog() {
		log = new Log();
		try {
			log.CreateLog(logBaseDir);
			logActive = true;

			// Start the log and initialize the text
			System.out.println("###############" + systemName + "###############");
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
	 * @throws ConfigurationException
	 *             if there is an issue creating the configuration file. Details
	 *             will be in the exceptions message.
	 */
	private void InitCfg() {
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

		// uuid = UUID.fromString(cfg.Get("uuid"));
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
	 * @param IP
	 *            IP address to connect to
	 * @param port
	 *            port to connect to
	 * @throws ClientInitializationException
	 *             thrown if there is an error creating the Client. Error
	 *             details will be in the getMessage()
	 */
	public void StartShardClient(String IP, int port) throws ClientInitializationException {
		try {
			if (client.IsConnectionActive()) {
				throw new ClientInitializationException(
						"Client is already initialized! Aborting attempt to create connection.");
			}
		} catch (NullPointerException e) {
			// If client is not initialized, initialize it
			try {
				client = new Client(IP, port);
			} catch (NoSuchAlgorithmException e1) {
				throw new ClientInitializationException(
						"Unable to initialize client. Likely an issue loading RSA cipher. Aborting creation.");
			}
			this.IP = IP;
			this.port = port;
		}

		try {
			if (clientThread != null || clientThread.isAlive()) {
				StopShardClient();
				throw new ClientInitializationException(
						"Client Thread is already initialized! Aborting attempt to create connection.");
			}
		} catch (NullPointerException e) {
			// If the clientThread isn't initialized, do nothing
			// ((re)-initialize below)
		} catch (ConnectionException ex) {
		}

		clientThread = new Thread(client);
		clientThread.start();
		InitPatcher();
	}

	public void StartShardClientSuppressed(String IP, int port) {
		try {
			StartShardClient(IP, port);
		} catch (ClientInitializationException ex) {
		}
	}

	/**
	 * Used to stop the Shard. Sends a close connection packet to the Heart to
	 * terminate connection, which will then terminate IO streams
	 *
	 * @throws ConnectionException
	 *             thrown when there is an issue closing the IO streams to the
	 *             Heart. Error will be in the getMessage()
	 */
	public void StopShardClient() throws ConnectionException {
		try {
			Packet p = new Packet(Packet.PACKET_TYPE.CloseConnection, null);
			p.packetString = "Manual disconnect";
			client.SendPacket(p, true);
			client.CloseIOStreams();
			clientThread.join();
			clientThread = null;
		} catch (SendPacketException e) {
			System.err.println("Error sending disconnect packet to Heart. Error: " + e.getMessage());
		} catch (InterruptedException ex) {
		}
	}

	/**
	 * Retrieve the object of ShardCore being used by the Shard. There can only
	 * be one, it is static.
	 *
	 * @return Shard_Core object being used by the Shard
	 */
	public static Shard_Core GetShardCore() {
		return shard_core;
	}

	/**
	 * Return the UUID of the Shard for use with networking with the Heart
	 *
	 * @return UUID of the Shard
	 */
	@Deprecated
	public UUID GetUUID() {
		return uuid;
	}

	/**
	 * Check whether the Shards connection to the Heart is active or not
	 *
	 * @return boolean whether the connection is active
	 */
	public boolean IsActive() {
		return client.IsConnectionActive();
	}

	/**
	 * Get the IP address being connected to by the Shard
	 *
	 * @return String IP address being connected to
	 */
	public String getIP() {
		return IP;
	}

	/**
	 * Get the Port being connected to by the Shard
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

	// public void setInitializedToFalse() {
	// initialized = false;
	// client = null;
	// try {
	// clientThread.join();
	// } catch (InterruptedException ex) {
	// }
	// clientThread = null;
	// }
	/**
	 * Send a packet to the Heart
	 *
	 * @param p
	 *            Packet to send
	 * @throws SendPacketException
	 *             thrown if there is an error sending the Packet to the Heart.
	 *             Error will be in the getMessage()
	 */
	public void SendPacket(Packet p) throws SendPacketException {
		client.SendPacket(p, true);
	}

	/**
	 * Writes to the Standard Output Stream, as well as calls 'write' on the
	 * local log object
	 *
	 * @param msg
	 *            Message to be displayed and written
	 * @return Returns TRUE if successful at writing to the log, FALSE if not
	 */
	private boolean Write(String msg) {
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
					client.SendPacket(p, true);
				}

				log.Write(msg);
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
