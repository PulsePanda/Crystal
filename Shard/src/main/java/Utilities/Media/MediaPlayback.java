package Utilities.Media;

import Exceptions.MediaStartException;

import javax.swing.*;

public class MediaPlayback {

    protected static Media media;
    private static MediaPlaybackHelper mph;
    private static Thread mphThread = null;

    /**
     * Media Playback default constructor
     */
    public MediaPlayback() {
        mph = new MediaPlaybackHelper(this);
    }

    /**
     * Start the playback of provided media
     *
     * @param media Media to be played
     * @throws MediaStartException thrown if there is an issue starting media playback.
     *                             Details will be in the getMessage()
     */
    public void start(Media media) throws MediaStartException {
        // If the media provided is null or the medias URL is null
        if (media == null || media.url.equals(""))
            throw new MediaStartException("Media to play hasn't been selected!");
        // If the MediaPlaybackHelper thread already exists
        if (mphThread != null) {
            int reply = JOptionPane.showConfirmDialog(null,
                    "There is currently something already playing.\nAre you sure you want to start something else?",
                    "Warning", JOptionPane.YES_NO_OPTION);
            if (reply == JOptionPane.YES_OPTION) {
                stop();
                this.media = media;
            } else {
                return;
            }
        } else {
            this.media = media;
        }

        // Start the media playback helper thread
        mphThread = new Thread(mph);
        mphThread.start();
    }

    /**
     * Stop the media playback
     */
    public void stop() {
        mphThread.stop();
        mphThread = null;
        media = null;
    }

    public void pause() {

    }

    public void restart() {

    }

    public void next() {

    }

    public void previous() {

    }

    public void volumeDown() {

    }

    public void volumeUp() {

    }
}

class MediaPlaybackHelper implements Runnable {

    private MediaPlayback mp;

    /**
     * Media playback helper default constructor
     *
     * @param mp MediaPlayback object to access data
     */
    public MediaPlaybackHelper(MediaPlayback mp) {
        this.mp = mp;
    }

    /**
     * Thread run method. Starts playing the media
     */
    public void run() {
        try {
            mp.media.play();
        } catch (Exception e) {
            System.err.println("Unable to play media. Details: " + e.getMessage());
        }
    }

}