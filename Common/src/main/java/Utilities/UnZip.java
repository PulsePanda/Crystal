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
		String[] params = { "py", "../lib/unzip.py", source, destination };
		Runtime.getRuntime().exec(params);
	}
}
