package Utilities.Media;

import java.util.ArrayList;

public class MediaList {

    public ArrayList<ListItem> list;

    public MediaList() {
        list = new ArrayList<ListItem>();
    }

    public boolean addItem(String name, String path) {
//        if (contains(name))
//            return false;

        list.add(new ListItem(name, path));
        return true;
    }

    public boolean removeItem(String nameOrPath) {
        for (int i = 0; i < list.size(); i++) {
            ListItem li = list.get(i);
            if (li.getName().toLowerCase().equals(nameOrPath) || li.getPath().toLowerCase().equals(nameOrPath)) {
                list.remove(i);
                return true;
            }
        }

        return false;
    }

    public boolean contains(String nameOrPath) {
        for (ListItem li : list) {
            if (li.getName().toLowerCase().equals(nameOrPath))
                return true;
            if (li.getPath().toLowerCase().equals(nameOrPath))
                return true;
        }

        return false;
    }

    public ListItem get(String nameOrPath) {
        for (ListItem li : list) {
            if (li.getName().toLowerCase().equals(nameOrPath))
                return li;
            if (li.getPath().toLowerCase().equals(nameOrPath))
                return li;
        }

        return null;
    }

    public ListItem get(int index) {
        return list.get(index);
    }

    public int size() {
        return list.size();
    }
}