package Exceptions;

public class MediaStartException extends Exception {

	/**
	 * Media Start Exception. Used in Media Playback
	 */
	private static final long serialVersionUID = 1L;

	public MediaStartException(String message) {
		super(message);
	}
}
