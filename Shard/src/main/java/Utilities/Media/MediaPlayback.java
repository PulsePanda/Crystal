package Utilities.Media;

public class MediaPlayback {

	protected static Media media;
	private static MediaPlaybackHelper mph;
	private static Thread mphThread = null;

	public MediaPlayback(Media media) {
		this.media = media;
		mph = new MediaPlaybackHelper(this);
	}

	public void start() {
		if (mphThread != null) {

			// TODO have a confirmation prompt
			stop();
		}
		mphThread = new Thread(mph);
		mphThread.start();
	}

	public void stop() {
		mphThread.stop();
		mphThread = null;
	}
}

class MediaPlaybackHelper implements Runnable {

	MediaPlayback mp;

	public MediaPlaybackHelper(MediaPlayback mp) {
		this.mp = mp;
	}

	public void run() {
		mp.media.play();
	}

}