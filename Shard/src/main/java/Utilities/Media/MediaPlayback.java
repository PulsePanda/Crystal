package Utilities.Media;

import Exceptions.MediaStartException;

import javax.swing.*;

public class MediaPlayback {

    protected static Media media;
    private static MediaPlaybackHelper mph;
    private static Thread mphThread = null;

    public MediaPlayback() {
        mph = new MediaPlaybackHelper(this);
    }

    public void start(Media media) throws MediaStartException {
        if (media == null || media.url.equals(""))
            throw new MediaStartException("Media to play hasn't been selected!");
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
        mphThread = new Thread(mph);
        mphThread.start();
    }

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

    public MediaPlaybackHelper(MediaPlayback mp) {
        this.mp = mp;
    }

    public void run() {
        mp.media.play();
    }

}