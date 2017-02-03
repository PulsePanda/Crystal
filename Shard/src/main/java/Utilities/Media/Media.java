package Utilities.Media;

abstract class Media {
    String url;

    /**
     * Media default constructor
     *
     * @param url String URL of the media to play. Replaces all "/" with "\\"
     */
    public Media(String url) {
        this.url = url.replace("/", "\\");
    }

    /**
     * Play the media
     */
    public void play() {

    }

    /**
     * Stops the media
     */
    public void stop() {

    }

    /**
     * Pause the media
     */
    public void pause() {

    }

    /**
     * Restarts the media
     */
    public void restart() {

    }

    /**
     * Plays the next media file
     */
    public void next() {

    }

    /**
     * Plays the previous media file
     */
    public void previous() {

    }

    /**
     * Lowers the volume of the media
     */
    public void volumeDown() {

    }

    /**
     * Raises the volume of the media
     */
    public void volumeUp() {

    }
}
