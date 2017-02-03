package Utilities.Media;

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
