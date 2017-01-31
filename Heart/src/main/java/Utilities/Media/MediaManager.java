package Utilities.Media;

import java.io.File;

public class MediaManager {

    protected String mediaDriveLetter, musicDir, movieDir;

    private MediaIndexer mediaIndexer;
    protected MediaList mediaList;
    private Thread indexThread;

    boolean isIndexed = false, keepIndexing;

    public MediaManager(String mediaDir, String musicDir, String movieDir) {
        System.out.println("MEDIA_MANAGER: Initializing media manager...");

        String[] driveLetter = mediaDir.split("/"); // separates out the drive letter
        this.mediaDriveLetter = driveLetter[0];
        this.musicDir = musicDir;
        this.movieDir = movieDir;

        mediaList = new MediaList();
    }

    public void index(boolean keepIndexing) {
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

    public String[] getMedia(String mediaName) {
        ListItem[] items = mediaList.get(mediaName);
        String[] files = new String[items.length];
        for (int i = 0; i < files.length; i++) {
            files[i] = items[i].getPath();
        }
//        return mediaList.get(mediaName).getPath();
        return files;
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
            System.out.println("MEDIA_MANAGER: Index complete! List size: " + mm.mediaList.size());

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
            if (isMovieOrMusic(file.getName())) {
                String filePath = file.getPath();
                // removes the drive letter from the file's path, as the folder is shared and the drive isn't needed
                filePath = filePath.replaceFirst(mm.mediaDriveLetter, "");
                mm.mediaList.addItem(file.getName(), filePath);
            }
        }
    }

    private boolean isMovieOrMusic(String fileName) {
        if (fileName.endsWith(".pcm") || fileName.endsWith(".wav") || fileName.endsWith(".aiff") ||
                fileName.endsWith(".mp3") || fileName.endsWith(".aac") || fileName.endsWith(".ogg") ||
                fileName.endsWith(".wma") || fileName.endsWith(".flac") || fileName.endsWith(".alac") || // valid music files
                fileName.endsWith(".mp4") || fileName.endsWith(".avi") || fileName.endsWith(".asf") || fileName.endsWith(".mov")
                || fileName.endsWith(".qt") || fileName.endsWith(".avchd") || fileName.endsWith(".flv") || fileName.endsWith(".swf")
                || fileName.endsWith(".mpg") || fileName.endsWith(".mpeg") || fileName.endsWith(".wmv") || fileName.endsWith(".mkv")) // valid movie files
            return true;
        else
            return true;
    }
}
