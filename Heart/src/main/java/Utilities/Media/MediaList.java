package Utilities.Media;

import java.util.ArrayList;

public class MediaList {

	public ArrayList<ListItem> list;

	public MediaList() {
		list = new ArrayList<ListItem>();
	}

	public boolean addItem(String name, String path) {
		if (contains(name))
			return false;

		list.add(new ListItem(name, path));
		return true;
	}

	public boolean removeItem(String nameOrPath) {
		for (int i = 0; i < list.size(); i++) {
			ListItem li = list.get(i);
			if (li.getName().equals(nameOrPath) || li.getPath().equals(nameOrPath)) {
				list.remove(i);
				return true;
			}
		}

		return false;
	}

	public boolean contains(String nameOrPath) {
		for (ListItem li : list) {
			if (li.getName().equals(nameOrPath))
				return true;
			if (li.getPath().equals(nameOrPath))
				return true;
		}

		return false;
	}

	public int size() {
		return list.size();
	}
}

class ListItem {
	private String name, path;
	private MEDIA_TYPE itemType;

	public ListItem(String name, String path) {
		this.name = name;
		this.path = path;

		if (path.toLowerCase().contains("music"))
			itemType = MEDIA_TYPE.Music;
		if (path.toLowerCase().contains("movie"))
			itemType = MEDIA_TYPE.Movie;
	}

	public String getName() {
		return name;
	}

	public String getPath() {
		return path;
	}

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
		Movie, Music;
	}
}