package wallpaper_search;

import java.util.ArrayList;
import java.io.Serializable;

public class ColorDataPair implements Comparable<Object>, Serializable {

	private static final long serialVersionUID = 435662235;
	private String hex;
	private ArrayList<Wallpaper> wallpapers;
	
	public ColorDataPair (String hex, String filePath, int width, int height) {
		this.hex = hex;
		this.wallpapers = new ArrayList<Wallpaper>();
		this.wallpapers.add(new Wallpaper(filePath, width, height));
	}
	
	public ColorDataPair (String hex) {
		this.hex = hex;
		this.wallpapers = new ArrayList<Wallpaper>();
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
	
	public ArrayList<Wallpaper> getWallpapers () {
		return this.wallpapers;
	}

	public String getHex() {
		return this.hex;
	}
	
	public int[] getLowHigh (int tolerance) {
		int val = Integer.parseInt(this.getHex().substring(0,2), 16)
				+ Integer.parseInt(this.getHex().substring(2,4), 16)
				+ Integer.parseInt(this.getHex().substring(4,6), 16);
		int[] lowHigh = new int[] {val-tolerance, val+tolerance};
		if (lowHigh[0]<0)
			lowHigh[0] = 0;
		if (lowHigh[1]>765)
			lowHigh[1] = 765;
		return new int[] {val-tolerance, val+tolerance};
	}
	
	public static String makeHexVal (int val) {
		String hexColour = Integer.toHexString(val & 0xffffff);
		if (hexColour.length() < 6) {
			hexColour = "000000".substring(0, 6 - hexColour.length()) + hexColour;
		}
		return hexColour;
	}
	
}
