package Utilities;

import java.io.IOException;

public class UnZip {

	private String source, destination;

	/**
	 * UnZip
	 *
	 * @param source
	 *            input zip file
	 * @param destination
	 *            output folder
	 */
	public UnZip(String source, String destination) {
		this.source = source;
		this.destination = destination;
	}

	/**
	 * Execute the UnZip function
	 * 
	 * @throws IOException
	 *             thrown if there is an error spawning the script
	 */
	public void run() throws IOException {
		String[] params = null;
		if (SystemInfo.system_os == SystemInfo.SYSTEM_OS.Windows)
			params = new String[] { "py", "../lib/unzip.py", source, destination };
		else if (SystemInfo.system_os == SystemInfo.SYSTEM_OS.Linux)
			params = new String[] { "python3", "../lib/unzip.py", source, destination };

		Runtime.getRuntime().exec(params);
	}
}
