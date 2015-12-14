package wallpaper_search;

import java.awt.Color;
import java.util.ArrayList;

public class ColorTree extends ColorStructure {
	
	private RedBlackTree h;
	private final static int HIGH_INDEX = 101;
	
	public ColorTree () {
		h = new RedBlackTree();
	}
	
	/**
	 * Inserts a value into the ColorMap in a null spot or adds wallpapers
	 * to existing object.
	 * @param ins - the object attempting to insert
	 * @return the object that now contains the wallpapers of ins
	 */
	public ColorDataPair insert (ColorDataPair ins) {
		int[] indexes = getIndexesForHex(ins.getHex());
		TreeDataPair hueFindings = (TreeDataPair) h.find(new TreeDataPair(indexes[0]));
		if (hueFindings==null) {
			TreeDataPair saturation = new TreeDataPair(indexes[0]);
			TreeDataPair brightness = new TreeDataPair(indexes[1]);
			brightness.getTree().insert(new ColorTreePair(indexes[2], ins));
			saturation.getTree().insert(brightness);
			h.insert(saturation);
		} else {
			TreeDataPair saturationFindings =
					(TreeDataPair) hueFindings.getTree().find(new TreeDataPair(indexes[1]));
			if (saturationFindings==null) {
				TreeDataPair brightness = new TreeDataPair(indexes[1]);
				brightness.getTree().insert(new ColorTreePair(indexes[2], ins));
				hueFindings.getTree().insert(brightness);
			} else {
				ColorTreePair brightnessFindings =
						(ColorTreePair) saturationFindings.getTree().find(new ColorTreePair(indexes[2]));
				if (brightnessFindings==null) {
					saturationFindings.getTree().insert(new ColorTreePair(indexes[2], ins));
				} else {
					for (Wallpaper wp: ins.getWallpapers())
						brightnessFindings.data.addWallpaper(wp);
				}
			}
		}
		return null;
	}
	
	/**
	 * Attempts to find a ColorDataPair object in the ColorMap
	 * @param find - the object being searched for, search specifically for the hex value
	 * @return the object with hex value matching find or null
	 */
	private ColorDataPair find (int[] indexes) {
		TreeDataPair s = (TreeDataPair) h.find(new TreeDataPair(indexes[0]));
		if (s==null)
			return null;
		TreeDataPair b = (TreeDataPair) s.getTree().find(new TreeDataPair(indexes[1]));
		if (b==null)
			return null;
		ColorTreePair findings = (ColorTreePair) b.getTree().find(new ColorTreePair(indexes[2]));
		if (findings==null)
			return null;
		return findings.data;
	}
	
	/**
	 * Attempts to find a ColorDataPair object in the ColorMap
	 * @param find - the object being searched for, search specifically for the hex value
	 * @return the object with hex value matching find or null
	 */
	public ColorDataPair find (ColorDataPair find) {
		int[] indexes = getIndexesForHex(find.getHex());
		TreeDataPair s = (TreeDataPair) h.find(new TreeDataPair(indexes[0]));
		if (s==null)
			return null;
		TreeDataPair b = (TreeDataPair) s.getTree().find(new TreeDataPair(indexes[1]));
		if (b==null)
			return null;
		ColorTreePair findings = (ColorTreePair) b.getTree().find(new ColorTreePair(indexes[2]));
		if (findings==null)
			return null;
		return findings.data;
	}
	
	/**
	 * Attempts to find colors similar to find, within a range of tolerance
	 * @param find - The ColorDataPair to base the search off of
	 * @param tolerance - int value that determines how similar a color needs to be
	 * in order to be returned. Low value requiring high similarity, high opposite.
	 * tolerance > 0
	 * @return an array, potentially empty, of ColorDataPair objects deemed similar to find
	 */
	public ColorDataPair[] findLikeColors (ColorDataPair find, int tolerance) {
		ArrayList<ColorDataPair> results = new ArrayList<ColorDataPair>();
		int[] indexes = getIndexesForHex(find.getHex());
		//System.out.println("find hex: "+find.getHex());
		//System.out.println("find indexes: "+indexes[0]+" "+indexes[1]+" "+indexes[2]);
		
		//starting index for hue
		int hueIndex = (indexes[0]-tolerance) < 0 ?
								(indexes[0]-tolerance)+(HIGH_INDEX-1) 
									:(indexes[0]-tolerance) ;
		//termination value for hue
		int hueTerminate = indexes[0]+tolerance+1 > HIGH_INDEX-1 ?
								(indexes[0]+tolerance)-HIGH_INDEX 
									:indexes[0]+tolerance+1 ;
		//starting index for saturation and brightness
		int lowerIndex = (indexes[1]-tolerance/2) < 0 ? 
								(indexes[1]-tolerance/2)+(HIGH_INDEX-1) 
									:(indexes[1]-tolerance/2) ;
		
		//hue is a radial value such as colors on a color wheel and must be cycled 
		//thusly needs a pre-determined termination index as it may start at 97
		//and need to end at 3
		for (int h=hueIndex; h!=hueTerminate; h++ ) {
			h = h==HIGH_INDEX ? 0 : h;
			for (int s=lowerIndex; s<HIGH_INDEX; s++ ) {
				for (int b=lowerIndex; b<HIGH_INDEX; b++ ) {
					//System.out.println("h: "+h+" s: "+s+" b: "+b);
					ColorDataPair findings = this.find(new int[] {h,s,b});
					if (findings != null) {
						System.out.println("ADDED: "+findings);
						
						results.add( findings );
					}
				}
			}
		}
		ColorDataPair[] returnableResults = new ColorDataPair[results.size()];
		for (int i=0; i<returnableResults.length; i++)
			returnableResults[i] = results.get(i);
		return returnableResults;
	}
	
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
	protected static int[] getIndexesForHex (String hex) {
		
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

class TreeDataPair implements Comparable<Object> {
	
	private int index;
	private RedBlackTree tree;
	
	public TreeDataPair (int index, RedBlackTree tree) {
		this.index = index;
		this.tree = tree;
	}
	
	public TreeDataPair (int index) {
		this.index = index;
		this.tree = new RedBlackTree();
	}
	
	public void setIndex (int index) {
		this.index = index;
	}
	
	public int getIndex () {
		return this.index;
	}
	
	public void setTree (RedBlackTree tree) {
		this.tree = tree;
	}
	
	public RedBlackTree getTree () {
		return this.tree;
	}
	
	@Override
	public int compareTo(Object o) {
		TreeDataPair other = (TreeDataPair) o;
		if (this.index>other.getIndex())
			return 1;
		else if (this.index<other.getIndex())
			return -1;
		return 0;
	}

}

class ColorTreePair implements Comparable<Object> {
	
	public int index;
	public ColorDataPair data;
	
	public ColorTreePair (int index, ColorDataPair data) {
		this.index = index;
		this.data = data;
	}

	public ColorTreePair (int index) {
		this.index = index;
	}
	
	@Override
	public int compareTo(Object o) {
		ColorTreePair other = (ColorTreePair) o;
		if (index > other.index)
			return 1;
		if (index < other.index)
			return -1;
		return 0;
	}
}
