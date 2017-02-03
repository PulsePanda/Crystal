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
    public void play() throws Exception {

    }

    /**
     * Stops the media
     */
    public void stop() throws Exception {

    }

    /**
     * Pause the media
     */
    public void pause() throws Exception {

    }

    /**
     * Restarts the media
     */
    public void restart() throws Exception {

    }

    /**
     * Plays the next media file
     */
    public void next() throws Exception {

    }

    /**
     * Plays the previous media file
     */
    public void previous() throws Exception {

    }

    /**
     * Lowers the volume of the media
     */
    public void volumeDown() throws Exception {

    }

    /**
     * Raises the volume of the media
     */
    public void volumeUp() throws Exception {

    }
}
