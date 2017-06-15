/*
 * This file is part of Crystal Home Systems.
 *
 * Crystal Home Systems is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Crystal Home Systems is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Crystal Home Systems. If not, see http://www.gnu.org/licenses/.
 */

package Utilities.Media;

import java.io.File;

/**
 * Media manager. Indexes all media found in given directories and stores them for later use
 */
public class MediaManager {

    protected String mediaDriveLetter, musicDir, movieDir;
    protected MediaList songList, movieList;
    protected boolean isIndexed = false, keepIndexing;
    private MediaIndexer mediaIndexer;
    private Thread indexThread;

    /**
     * Media Manager default constructor
     *
     * @param mediaDir String root media directory. Must contain both musicDir and movieDir within the directory
     * @param musicDir String root music directory. Must contain all music, can be organized into other folders
     * @param movieDir String root movie directory. Must contain all movies, can be organized into other folders
     */
    public MediaManager(String mediaDir, String musicDir, String movieDir) {
        System.out.println("MEDIA_MANAGER: Initializing media manager...");

        // Remove drive letter from paths
//        if (SystemInfo.getSystem_os() == SystemInfo.SYSTEM_OS.Windows) {
//            String[] driveLetter = mediaDir.split(":"); // separates out the drive letter
//            this.mediaDriveLetter = driveLetter[0];
//            this.mediaDriveLetter = mediaDriveLetter + ":\\";
//        } else if (SystemInfo.getSystem_os() == SystemInfo.SYSTEM_OS.Linux) {
//            System.err.println("Crystal doesn't know how to handle the media paths on your system! Media playback will not work!");
//        } else if (SystemInfo.getSystem_os() == SystemInfo.SYSTEM_OS.ERROR) {
//            System.err.println("Crystal doesn't know how to handle the media paths on your system! Media playback will not work!");
//        }
        this.musicDir = musicDir;
        this.movieDir = movieDir;

        movieList = new MediaList();
        songList = new MediaList();
    }

    /**
     * Index media
     *
     * @param keepIndexing boolean false indexing once, true if indexing every 30 minutes
     * @param delayMinutes int delay between indexing attempts in minutes
     */
    public void index(boolean keepIndexing, int delayMinutes) {
        this.keepIndexing = keepIndexing;

        clearIndex();

        mediaIndexer = new MediaIndexer(this, delayMinutes * 60 * 1000); // 30 minutes
        indexThread = new Thread(mediaIndexer);
        indexThread.start();
    }

    /**
     * Check if indexing is complete
     *
     * @return boolean indexing complete
     */
    public boolean isIndexed() {
        return isIndexed;
    }

    /**
     * Stop indexing media. Uses thread.stop() (which is depreciated)
     */
    @SuppressWarnings("deprecation")
    public void close() {
        indexThread.stop();
    }

    /**
     * Retrieve the Song List
     *
     * @return MediaList song list
     */
    public MediaList getSongList() {
        return songList;
    }

    /**
     * Retrieve all songs matching a given name
     *
     * @param songName String song name
     * @return String[] all song paths (without drive letter) matching songName
     */
    public String[] getSong(String songName) {
        ListItem[] items = songList.get(songName);
        String[] files = new String[items.length];
        for (int i = 0; i < files.length; i++) {
            files[i] = items[i].getPath();
        }
        return files;
    }

    /**
     * Retrieve the Movie List
     *
     * @return MediaList movie list
     */
    public MediaList getMovieList() {
        return movieList;
    }

    /**
     * Retrieve all movies matching a given name
     *
     * @param movieName String movie name
     * @return String[] all movie paths (without drive letter) matching movieName
     */
    public String[] getMovie(String movieName) {
        ListItem[] items = movieList.get(movieName);
        String[] files = new String[items.length];
        for (int i = 0; i < files.length; i++) {
            files[i] = items[i].getPath();
        }
        return files;
    }

    /**
     * Clears out the index to re-index
     */
    public void clearIndex() {
        movieList.delete();
        songList.delete();
    }
}

class MediaIndexer implements Runnable {

    private MediaManager mm;
    private int delay;

    /**
     * Media Indexer. Implements Runnable, threaded.
     *
     * @param mm    MediaManager object
     * @param delay int thread sleep delay. How long between indexing attempts
     */
    public MediaIndexer(MediaManager mm, int delay) {
        this.mm = mm;
        this.delay = delay;
    }

    /**
     * Thread run() method.
     * <p>
     * Indexes movie directory into MediaManager.movieList and music directory into MediaManager.songList
     * Then delays for given delay in minutes before looping again.
     */
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

    /**
     * Recursive method for indexing movies
     *
     * @param file File to index. Start with the root movie directory, and it iterates through the directory until it has each file
     */
    private void movieIndexHelper(File file) {
        if (file.isDirectory()) {
            File[] listOfFiles = file.listFiles();
            for (File item : listOfFiles) {
                movieIndexHelper(item);
            }
        } else if (file.isFile()) {
            if (isMovieOrMusic(file.getName())) {
                String filePath = file.getPath();
                String fileName = file.getName();

                mm.movieList.addItem(fileName, filePath, ListItem.MEDIA_TYPE.Movie);
            }
        }
    }

    /**
     * Recursive method for indexing songs
     *
     * @param file File to index. Start with the root music directory, and it iterates through the directory until it has each file
     */
    private void songIndexHelper(File file) {
        if (file.isDirectory()) {
            File[] listOfFiles = file.listFiles();
            for (File item : listOfFiles) {
                songIndexHelper(item);
            }
        } else if (file.isFile()) {
            if (isMovieOrMusic(file.getName())) {
                String filePath = file.getPath();
                String fileName = file.getName();

                mm.songList.addItem(fileName, filePath, ListItem.MEDIA_TYPE.Music);
            }
        }
    }

    /**
     * Check if the given file name is a valid media file
     *
     * @param fileName String file name. Includes file extension
     * @return boolean True if valid, false if else
     */
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
