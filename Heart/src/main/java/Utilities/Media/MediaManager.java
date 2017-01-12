package Utilities.Media;

import java.io.File;

public class MediaManager {

	String musicDir, movieDir;

	private MediaIndexer mediaIndexer;
	MediaList mediaList;
	private Thread indexThread;

	boolean isIndexed = false;

	public MediaManager(String musicDir, String movieDir) {
		System.out.println("MEDIA_MANAGER: Initializing media manager...");

		this.musicDir = musicDir;
		this.movieDir = movieDir;

		mediaList = new MediaList();

		mediaIndexer = new MediaIndexer(this, 30 * 60 * 1000);
		indexThread = new Thread(mediaIndexer);
		indexThread.start();
	}

	public boolean isIndexed() {
		return isIndexed;
	}

	public void close() {
		indexThread.stop();
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

			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
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
			mm.mediaList.addItem(file.getName(), file.getPath());
		}
	}
}
