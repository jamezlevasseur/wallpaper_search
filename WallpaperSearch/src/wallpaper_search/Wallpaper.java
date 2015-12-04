package wallpaper_search;

import java.util.ArrayList;
import java.io.Serializable;

public class Wallpaper implements Serializable {

	private static final long serialVersionUID = 435662235;
	String filePath;
	String size;
	private ArrayList<String> tags;
	public int width;
	public int height;
	
	public Wallpaper (String filePath, int w, int h) {
		this.filePath = filePath;
		this.tags = new ArrayList<String>();
		this.width = w;
		this.height = h;
	}
	
	private void determineSize (int w, int h) {
		if (h>w) {
			size = "mobile";
		} else if (w<1024) {
			size = "small";
		} else if (w<1920) {
			size = "medium";
		} else {
			size = "large";
		}
	}
	
	public void setFilePath (String newFilePath) {
		this.filePath = newFilePath;
	}
	
	public String getFilePath () {
		return this.filePath;
	}
	
	public void addTag (String tag) {
		tags.add(tag);
	}
	
	public ArrayList<String> getTags () {
		return this.tags;
	}
	
}
