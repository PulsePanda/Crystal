/**
 * @file Heart_Core.java
 * @author Austin VanAlstyne
 */

package Heart;

import java.awt.Color;

// TODO get rid of the extra time stamps in logs

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

import Exceptions.ConfigurationException;
import Exceptions.ServerInitializationException;
import Utilities.Config;
import Utilities.Log;

public class Heart_Core {
	public final static boolean DEBUG = false;

	public static String systemName = "CHS Heart", musicDir = "", movieDir = "", commandKey = "",
			baseDir = "/CrystalHomeSys/", heartDir = "Heart/", shardLogsDir = "Shard_Logs/",
			configDir = "heart_config.cfg", logBaseDir = "Logs/", shardFileDir = "Shard_Files/";
	private static boolean cfg_set = false, logActive = false, initialized = false;

	private boolean headless = false;

	private static Heart_Core heart_core;
	private static Log log;
	private UUID uuid;
	private Config cfg = null;
	private Server server = null;
	private Thread serverThread = null;

	private JFrame frame;
	private static JTextArea textArea;

	/**
	 * Default Constructor. Server Port defaults to 6976
	 */
	public Heart_Core(boolean headless) {
		heart_core = this;
		this.headless = headless;
	}

	/**
	 * Initialize the Heart Server
	 * 
	 * @throws ConfigurationException
	 *             if there is an issue creating the Configuration file
	 */
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
	 * Starts up the Server for this Heart Core object.
	 * 
	 * @param port
	 *            the server needs to host on.
	 * 
	 * @throws ServerInitializationException
	 *             if there is an error creating the server for any reason. If
	 *             the exception is thrown, abort attempt to create server
	 */
	public void StartServer(int port) throws ServerInitializationException {
		try {
			if (server != null || server.IsConnectionActive()) {
				throw new ServerInitializationException(
						"Server is already initialized. Cannot create new server on this object. Aborting creation.");
			}
		} catch (NullPointerException e) {
			// If server is not set, this will throw.
			server = new Server(port);
		}
		try {
			if (serverThread != null || serverThread.isAlive()) {
				System.err.println(
						"This instance of Heart Core already has an active Server Thread. Attempting to close the thread...");
				try {
					serverThread.join();
				} catch (InterruptedException e) {
					throw new ServerInitializationException(
							"Unable to initialize new Server Thread object within Heart Core. Aborting Creation.");
				}
			}
		} catch (NullPointerException e) {
			// If serverThread is not set, this will throw.
			// That's fine, we dont need it to do anything
		}

		serverThread = new Thread(server);
		serverThread.start();

	}

	/**
	 * Function to redirect standard output streams to the Write function
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

			@Override
			public void actionPerformed(ActionEvent e) {
				if (!allowShutdown)
					return;

				allowShutdown = false;
				StopHeartServer();
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
			System.err.println(
					"Unable to access log file or directory because of permission settings. Will continue running without logs, however please reboot to set logs.\n");
		} catch (IOException e) {
			System.err.println(
					"Unable to access find or create log on object creation. Will continue running without logs, however please reboot to set logs.\n");
		}
	}

	/**
	 * Sets up the configuration file(s) for the server
	 * 
	 * @throws ConfigurationException
	 *             if there is an issue creating the configuration file. Details
	 *             will be in the exceptions message.
	 */
	private void InitCfg() {
		System.out.println("Loading configuration file...");
		try {
			cfg = new Config(configDir);
		} catch (ConfigurationException e) {
			// TODO if the configuration isn't found, create and init it
		}

		cfg_set = Boolean.parseBoolean(cfg.Get("cfg_set"));

		if (cfg_set) {
			systemName = cfg.Get("systemName");
			musicDir = cfg.Get("musicDir");
			movieDir = cfg.Get("moveDir");
			commandKey = cfg.Get("commandKey");
			uuid = UUID.fromString(cfg.Get("uuid"));
			System.out.println("Configuration file loaded.");
		} else {
			System.err.println("Please initialize the Heart Config with Nerv before proceeding!");
		}
	}

	/**
	 * Writes to the Standard Output Stream, as well as calls 'write' on the
	 * local log object
	 * 
	 * @param msg
	 *            Message to be displayed and written
	 * @return Returns TRUE if successful at writing to the log, FALSE if not
	 */
	private boolean Write(String msg, Color color) {
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
				System.err.println(
						"Unable to write to log. IOException thrown. Deactivating log file, please reboot to regain access.");
				success = false;
			}
		}

		return success;
	}

	/**
	 * Get the Heart_Core object
	 * 
	 * @return Heart_Core object
	 */
	@Deprecated
	public static Heart_Core GetCore() {
		return heart_core;
	}

	/**
	 * Get the Heart's UUID value
	 * 
	 * @return UUID object that is equal to the Heart's UUID
	 */
	public UUID GetUUID() {
		return uuid;
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
	 * 
	 * @return true if the server is still active, else false.
	 */
	public boolean IsServerActive() {
		return server.IsConnectionActive();
	}

	private void StopHeartServer() {
		server.CloseConnections();
	}
}
