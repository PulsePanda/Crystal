/**
 * @file Shard_Core.java
 * @author Austin VanAlstyne
 */

package Shard;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
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
import Netta.Exceptions.SendPacketException;
import Utilities.Config;
import Utilities.Log;

public class Shard_Core {
	public static String systemName = "CHS Shard", commandKey, baseDir = "/CrystalHomeSys/", logBaseDir = "Logs/",
			configDir = "shard_config.cfg";
	private static boolean logActive = false, initialized = false;

	private boolean headless = false;

	// private Client client;
	private static Shard_Core shard_core;
	private Log log;
	private Config cfg = null;
	private JFrame frame;
	private UUID uuid;
	private static JTextArea textArea;
	private Client client = null;
	private Thread clientThread = null;

	public Shard_Core(boolean headless) {
		shard_core = this;
		this.headless = headless;
	}

	public void Init() {
		if (initialized)
			return;

		if (!headless) {
			InitGUI();
			RedirectSystemStreams();
		}

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

	private void InitVariables() {
		baseDir = System.getProperty("user.home") + baseDir;
		logBaseDir = baseDir + logBaseDir;
		configDir = baseDir + configDir;
	}

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

			@Override
			public void actionPerformed(ActionEvent e) {
				if (!allowShutdown)
					return;

				allowShutdown = false;
				StopShardClient();
				System.out.println("");
				System.out.println("IT IS NOW SAFE TO CLOSE THE WINDOW");
				System.out.println("");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			}
		});
		exitButton.setBounds(new Rectangle(10, 10, 100, 40));

		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setLineWrap(true);

		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setBounds(0, 60, frame.getWidth() - 5, frame.getHeight() - 85);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		frame.getContentPane().add(exitButton);
		frame.getContentPane().add(scrollPane);
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

		uuid = UUID.fromString(cfg.Get("uuid"));
		if (uuid != null) {
			System.out.println("Configuration file loaded.");
		} else {
			System.err.println("Please initialize the Config with Nerv before proceeding!");
		}
	}

	public void StartShardClient(String IP, int port) throws ClientInitializationException {
		try {
			if (client != null || client.IsConnectionActive()) {
				throw new ClientInitializationException(
						"Client is already initialized! Aborting attempt to create connection.");
			}
		} catch (NullPointerException e) {
			// If client is not initialized, initialize it
			client = new Client(IP, port);
		}

		try {
			if (clientThread != null || clientThread.isAlive()) {
				throw new ClientInitializationException(
						"Client Thread is already initialized! Aborting attempt to create connection.");
			}
		} catch (NullPointerException e) {
			// If the clientThread isn't initialized, do nothing
			// ((re)-initialize below)
		}

		clientThread = new Thread(client);
		clientThread.start();
	}

	private void StopShardClient() {
		try {
			Packet p = new Packet(Packet.PACKET_TYPE.CloseConnection, null);
			p.packetString = "Manual disconnect";
			client.SendPacket(p);
		} catch (SendPacketException e) {
			e.printStackTrace();
		}
	}

	public Shard_Core GetShardCore() {
		return shard_core;
	}

	public UUID GetUUID() {
		return uuid;
	}

	public void SendPacket(Packet p) throws SendPacketException {
		client.SendPacket(p);
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
				log.Write(msg);
			} catch (IOException e) {
				logActive = false;
				System.out.println(
						"Unable to write to log. IOException thrown. Deactivating log file, please reboot to regain access.");
				success = false;
			}
		}

		return success;
	}
}
