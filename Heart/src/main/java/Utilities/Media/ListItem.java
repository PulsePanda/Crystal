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
 * Item wrapper for each media file found from MediaManager
 */
public class ListItem {
    private String name, path;
    private MEDIA_TYPE itemType;

    /**
     * List Item default constructor
     *
     * @param name     String item name
     * @param path     String item path
     * @param itemType MEDIA_TYPE item's media type
     */
    public ListItem(String name, String path, MEDIA_TYPE itemType) {
        this.name = name;
        this.path = path;
        this.itemType = itemType;
    }

    /**
     * Get the item's name
     *
     * @return String item name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the item's path
     *
     * @return String item path
     */
    public String getPath() {
        return path;
    }

    /**
     * Get what type of media the item is
     *
     * @return String media type
     */
    public String getMediaType() {
        switch (itemType) {
            case Movie:
                return "movie";
            case Music:
                return "music";
            default:
                return "";
        }
    }

    public enum MEDIA_TYPE {
        Movie, Music
    }
}
