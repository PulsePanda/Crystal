package Utilities.Media;

import java.util.ArrayList;

public class MediaList {

    public ArrayList<ListItem> list;

    /**
     * Media List default constructor
     */
    public MediaList() {
        list = new ArrayList<ListItem>();
    }

    /**
     * Add item to list. Checks for duplicate paths
     *
     * @param name     String item name
     * @param path     String item path
     * @param itemType ListItem.MEDIA_TYPE item's media type
     * @return boolean True if success, false if else
     */
    public boolean addItem(String name, String path, ListItem.MEDIA_TYPE itemType) {
        if (contains(path))
            return false;

        list.add(new ListItem(name, path, itemType));
        return true;
    }

    /**
     * Remove item from list
     *
     * @param nameOrPath String file name or file path
     * @return boolean True if success, false if else
     */
    public boolean removeItem(String nameOrPath) {
        nameOrPath = nameOrPath.toLowerCase();
        for (int i = 0; i < list.size(); i++) {
            ListItem li = list.get(i);
            if (li.getName().toLowerCase().equals(nameOrPath) || li.getPath().toLowerCase().equals(nameOrPath)) {
                list.remove(i);
                return true;
            }
        }

        return false;
    }

    /**
     * Check if the list contains a specific file name or file path
     *
     * @param nameOrPath String file name or file path
     * @return boolean True if contains, false if else
     */
    public boolean contains(String nameOrPath) {
        nameOrPath = nameOrPath.toLowerCase();
        for (ListItem li : list) {
            if (li.getName().toLowerCase().equals(nameOrPath))
                return true;
            if (li.getPath().toLowerCase().equals(nameOrPath))
                return true;
        }

        return false;
    }

    /**
     * Retrieve all matches for a given file name or file path
     *
     * @param nameOrPath String file name or file path
     * @return ListItem[] containing all files matching nameOrPath
     */
    public ListItem[] get(String nameOrPath) {
        nameOrPath = nameOrPath.toLowerCase();
        ArrayList<ListItem> items = new ArrayList<>();
        for (ListItem li : list) {
            if (li.getName().toLowerCase().equals(nameOrPath))
                items.add(li);
            if (li.getPath().toLowerCase().equals(nameOrPath))
                items.add(li);
        }

        ListItem[] listItems = new ListItem[items.size()];

        listItems = items.toArray(listItems);

        return listItems;
    }

    /**
     * Retrieve a specific item at a given index
     *
     * @param index int index to retrieve
     * @return ListItem item listed at that index
     */
    public ListItem get(int index) {
        return list.get(index);
    }

    /**
     * Get list size
     *
     * @return int list size
     */
    public int size() {
        return list.size();
    }
}