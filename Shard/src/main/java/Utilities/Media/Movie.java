package Utilities.Media;

import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class Movie extends Media {

    /**
     * Movie default constructor
     *
     * @param url String url of the movie
     */
    public Movie(String url) {
        super(url);
    }

    @Override
    public void play() {
        try {
            URI uri = new URL(url.toLowerCase()).toURI();
            Desktop.getDesktop().browse(uri);
        } catch (URISyntaxException e) {
            System.err.println("Error playing back video. Given file path is invalid!");
        } catch (MalformedURLException e) {
            System.err.println("Error playing back video. Given file path is invalid!");
        } catch (IOException e) {
            System.err.println("Error playing back video. Unable to access file!");
        }
    }
}
