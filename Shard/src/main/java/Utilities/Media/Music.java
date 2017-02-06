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

import Exceptions.MediaStartException;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Music
 * <p>
 * Handles the format of music playback
 */
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
