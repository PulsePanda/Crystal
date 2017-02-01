package Utilities.Media;

import javax.print.attribute.standard.Media;
import java.io.File;

public class MediaManager {

    protected String mediaDriveLetter, musicDir, movieDir;

    private MediaIndexer mediaIndexer;
    protected MediaList songList, movieList;
    private Thread indexThread;

    boolean isIndexed = false, keepIndexing;

    public MediaManager(String mediaDir, String musicDir, String movieDir) {
        System.out.println("MEDIA_MANAGER: Initializing media manager...");

        String[] driveLetter = mediaDir.split("/"); // separates out the drive letter
        this.mediaDriveLetter = driveLetter[0];
        this.musicDir = musicDir;
        this.movieDir = movieDir;

        movieList = new MediaList();
        songList = new MediaList();
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

    public MediaList getSongList() {
        return songList;
    }

    public String[] getSong(String songName) {
        ListItem[] items = songList.get(songName);
        String[] files = new String[items.length];
        for (int i = 0; i < files.length; i++) {
            files[i] = items[i].getPath();
        }
        return files;
    }

    public MediaList getMovieList(){
        return movieList;
    }

    public String[] getMovie(String movieName){
        ListItem[] items = movieList.get(movieName);
        String[] files = new String[items.length];
        for (int i = 0; i < files.length; i++) {
            files[i] = items[i].getPath();
        }
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
            movieIndexHelper(new File(mm.movieDir));
            System.out.println("MEDIA_MANAGER: Indexing music...");
            songIndexHelper(new File(mm.musicDir));

            mm.isIndexed = true;
            System.out.println("MEDIA_MANAGER: Index complete! List size: " + (mm.songList.size() + mm.movieList.size()));

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

    private void movieIndexHelper(File file) {
        if (file.isDirectory()) {
            File[] listOfFiles = file.listFiles();
            for (File item : listOfFiles) {
                movieIndexHelper(item);
            }
        } else if (file.isFile()) {
            if (isMovieOrMusic(file.getName())) {
                String filePath = file.getPath();
                // removes the drive letter from the file's path, as the folder is shared and the drive isn't needed
                filePath = filePath.replaceFirst(mm.mediaDriveLetter, "");
                mm.movieList.addItem(file.getName(), filePath);
            }
        }
    }

    private void songIndexHelper(File file) {
        if (file.isDirectory()) {
            File[] listOfFiles = file.listFiles();
            for (File item : listOfFiles) {
                songIndexHelper(item);
            }
        } else if (file.isFile()) {
            if (isMovieOrMusic(file.getName())) {
                // Remove the drive letter from the path
                String filePath = file.getPath();
                filePath = filePath.replaceFirst(mm.mediaDriveLetter, "");

                // Remove the file extension from the name
                String fileName = file.getName();
//                String[] fileNameSplit = fileName.split(".");
//                fileName = fileNameSplit[0];

                mm.songList.addItem(fileName, filePath);
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
