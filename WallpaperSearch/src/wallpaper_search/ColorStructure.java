package wallpaper_search;

import java.awt.Color;

public abstract class ColorStructure {
	
	/**
	 * Inserts a value into the ColorMap in a null spot or adds wallpapers
	 * to existing object.
	 * @param ins - the object attempting to insert
	 * @return the object that now contains the wallpapers of ins
	 */
	
	public abstract ColorDataPair insert (ColorDataPair ins);
	
	/**
	 * Attempts to find a ColorDataPair object in the ColorMap
	 * @param find - the object being searched for, search specifically for the hex value
	 * @return the object with hex value matching find or null
	 */
	public abstract ColorDataPair find (ColorDataPair find);
	
	/**
	 * Attempts to find colors similar to find, within a range of tolerance
	 * @param find - The ColorDataPair to base the search off of
	 * @param tolerance - int value that determines how similar a color needs to be
	 * in order to be returned. Low value requiring high similarity, high opposite.
	 * tolerance > 0
	 * @return an array, potentially empty, of ColorDataPair objects deemed similar to find
	 */
	public abstract ColorDataPair[] findLikeColors (ColorDataPair find, int tolerance);
	
	/**
	 * determines if color1 is similar enough to color2 based on tolerance
	 * @param color1 - first color to compare
	 * @param color2 - second color to compare
	 * @param tolerance - value that will determine similarity goals
	 * @return true if colors are similar enough, false otherwise
	 */
	public static boolean isLikeColor (ColorDataPair color1, ColorDataPair color2, int tolerance) {
		int[] cindexes1 = getIndexesForHex(color1.getHex());
		int[] cindexes2 = getIndexesForHex(color2.getHex());
		for (int i=0; i<cindexes1.length; i++) {
			if (Math.abs(cindexes1[i]-cindexes2[i])>tolerance)
				return false;
		}
		return true;
	}
	
	/**
	 * determines if color1 is similar enough to color2 based on tolerance
	 * this version converts the hex strings to ColorDataPairs internally
	 * @param color1 - first color to compare, must be a hex string
	 * @param color2 - second color to compare, must be a hex string
	 * @param tolerance - value that will determine similarity goals
	 * @return true if colors are similar enough, false otherwise
	 */
	public static boolean isLikeColor (String color1, String color2, int tolerance) {
		ColorDataPair first = new ColorDataPair(color1);
		ColorDataPair second = new ColorDataPair(color2);
		
		int[] cindexes1 = getIndexesForHex(first.getHex());
		int[] cindexes2 = getIndexesForHex(second.getHex());
		for (int i=0; i<cindexes1.length; i++) {
			if (Math.abs(cindexes1[i]-cindexes2[i])>tolerance)
				return false;
		}
		return true;
	}
	
	/**
	 * Gets the indexes for a 6 length color hex value based on hue, saturation, and brightness.
	 * @param hex - the hex to use for the indexes
	 * @return an array of ints that can be used to store a value in ColorMaps hsb array
	 */
	private static int[] getIndexesForHex (String hex) {
		
		int red = Integer.parseInt( hex.substring(0, 2), 16);
		int green = Integer.parseInt( hex.substring(2, 4), 16);
		int blue = Integer.parseInt( hex.substring(4, 6), 16);
		float[] convert = Color.RGBtoHSB(red, green, blue, null);
		int[] indexes =
				new int[] {
						(int) (convert[0]*100),
						(int) (convert[1]*100),
						(int) (convert[2]*100)
				};
		return indexes;
	}
	

}
