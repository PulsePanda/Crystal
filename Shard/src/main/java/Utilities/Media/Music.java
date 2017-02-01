package Utilities.Media;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

public class Music extends Media {

    public Music(String url) {
        super(url);
    }

    @Override
    public void play() {
        String song = url;
        Player mp3player = null;
        BufferedInputStream in = null;
        try {
            in = new BufferedInputStream(new URL(song).openStream());
            mp3player = new Player(in);
            mp3player.play();
        } catch (MalformedURLException ex) {
        } catch (IOException e1) {
        } catch (JavaLayerException e2) {
        } catch (NullPointerException ex) {
        }
    }
}
