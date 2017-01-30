package Utilities.Media;

import java.io.File;
import java.net.MalformedURLException;

public class MediaManager {

	protected String mediaDir, musicDir, movieDir;

	private MediaIndexer mediaIndexer;
	MediaList mediaList;
	private Thread indexThread;

	boolean isIndexed = false, keepIndexing;

	public MediaManager(String mediaDir, String musicDir, String movieDir) {
		System.out.println("MEDIA_MANAGER: Initializing media manager...");

		String[] temp = mediaDir.substring(1).split("/");
		this.mediaDir = temp[0];
		this.musicDir = musicDir;
		this.movieDir = movieDir;

		mediaList = new MediaList();
	}

	public void index(boolean keepIndexing){
		this.keepIndexing = keepIndexing;

		mediaIndexer = new MediaIndexer(this, 30 * 60 * 1000); // 30 minutes
		indexThread = new Thread(mediaIndexer);
		indexThread.start();
	}

	public boolean isIndexed() {
		return isIndexed;
	}

	@SuppressWarnings("deprecation")
	public void close() {
		indexThread.stop();
	}

	public MediaList getMediaList() {
		return mediaList;
	}
}

class MediaIndexer implements Runnable {

	private MediaManager mm;
	private int delay;

	public MediaIndexer(MediaManager mm, int delay) {
		this.mm = mm;
		this.delay = delay;
	}

	public void run() {
		while (true) {
			System.out.println("MEDIA_MANAGER: Indexing movies...");
			indexHelper(new File(mm.movieDir));
			System.out.println("MEDIA_MANAGER: Indexing music...");
			indexHelper(new File(mm.musicDir));

			mm.isIndexed = true;
			System.out.println("MEDIA_MANAGER: Index complete!");

			if (mm.keepIndexing)
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
				}
			else {
				return;
			}
		}
	}

	private void indexHelper(File file) {
		if (file.isDirectory()) {
			File[] listOfFiles = file.listFiles();
			for (File item : listOfFiles) {
				indexHelper(item);
			}
		} else if (file.isFile()) {
		    // TODO make a list of all valid media files
		    if(file.getName().endsWith(".mp3") || file.getName().endsWith(".wav") || file.getName().endsWith("") || file.getName().endsWith(".mkv")) {
                String filePath = file.getPath();
                filePath = filePath.replaceFirst(mm.mediaDir, "");
                mm.mediaList.addItem(file.getName(), filePath);
            }
		}
	}
}
