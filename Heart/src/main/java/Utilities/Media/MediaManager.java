package Utilities.Media;

import java.io.File;

public class MediaManager {

	String musicDir, movieDir;

	private MediaIndexer mediaIndexer;
	MediaList mediaList;
	private Thread indexThread;

	boolean isIndexed = false, keepIndexing;

	public MediaManager(String musicDir, String movieDir, boolean keepIndexing) {
		System.out.println("MEDIA_MANAGER: Initializing media manager...");

		this.musicDir = musicDir;
		this.movieDir = movieDir;
		this.keepIndexing = keepIndexing;

		mediaList = new MediaList();

		mediaIndexer = new MediaIndexer(this, 30 * 60 * 1000);
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

			System.gc();

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
				item = null;
			}
		} else if (file.isFile()) {
			mm.mediaList.addItem(file.getName(), file.getPath());
			file = null;
		}
	}
}
