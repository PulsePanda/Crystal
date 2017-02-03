package Utilities.Media;

import Exceptions.MediaStartException;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class Music extends Media {

    /**
     * Music default constructor
     *
     * @param url String url of the music
     */
    public Music(String url) {
        super(url);
    }

    @Override
    public void play() throws MediaStartException {
        String song = url;
        Player mp3player = null;
        BufferedInputStream in = null;
        try {
            in = new BufferedInputStream(new URL(song).openStream());
            mp3player = new Player(in);
            mp3player.play();
        } catch (MalformedURLException ex) {
            throw new MediaStartException("URL is invalid.");
        } catch (IOException e1) {
            throw new MediaStartException("Error accessing media.");
        } catch (JavaLayerException e) {
            throw new MediaStartException("Could not start playback.");
        }
    }
}
