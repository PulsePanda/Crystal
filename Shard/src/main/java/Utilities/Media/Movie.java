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

import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Movie
 * <p>
 * Extends Media, handles the format of movie playback
 */
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
