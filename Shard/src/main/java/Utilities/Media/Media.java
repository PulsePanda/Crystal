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

/**
 * Media class template. Cannot be instantiated, every other media type will extend this
 */
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
