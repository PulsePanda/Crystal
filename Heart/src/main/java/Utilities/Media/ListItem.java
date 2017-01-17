package Utilities.Media;

public class ListItem {
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
