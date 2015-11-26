package wallpaper_search;

import java.util.ArrayList;

public class ColorDataPair implements Comparable<Object> {

	private String hex;
	private ArrayList<Wallpaper> wallpapers;
	
	public ColorDataPair (String hex, String filePath, int width, int height) {
		this.hex = hex;
		this.wallpapers = new ArrayList<Wallpaper>();
		this.wallpapers.add(new Wallpaper(filePath, width, height));
	}
	
	@Override
	public String toString() {
		String ret = this.hex+" ->";
		for (int i=0; i<wallpapers.size(); i++) {
			ret+=" "+wallpapers.get(i).getFilePath();
		}
		return ret;
	}
	
	@Override
	public int compareTo(Object arg0) {
		ColorDataPair other = (ColorDataPair) arg0;
		int redDiff = Integer.parseInt(this.getHex().substring(0,2), 16) - Integer.parseInt(other.getHex().substring(0,2), 16);
		int greenDiff = Integer.parseInt(this.getHex().substring(2,4), 16) - Integer.parseInt(other.getHex().substring(2,4), 16);
		int blueDiff = Integer.parseInt(this.getHex().substring(4,6), 16) - Integer.parseInt(other.getHex().substring(4,6), 16);
		return redDiff+greenDiff+blueDiff;
	}
	
	public void addWallpaper (Wallpaper wp) {
		wallpapers.add(wp);
	}
	
	public ArrayList<Wallpaper> getPaths () {
		return this.wallpapers;
	}

	public String getHex() {
		return this.hex;
	}
	
}
