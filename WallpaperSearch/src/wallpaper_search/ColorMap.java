package wallpaper_search;
import java.awt.Color;
import java.io.Serializable;


public class ColorMap implements Serializable {

	private static final long serialVersionUID = 435662231;
	private ColorDataPair[][][] hsb;
	public String dir;
	private final static int ARRAY_SIZE = 101;
	
	public ColorMap () {
		hsb = new ColorDataPair[ARRAY_SIZE][ARRAY_SIZE][ARRAY_SIZE];
	}
	
	public ColorDataPair insert (ColorDataPair ins) {
		int[] indexes = getIndexesForHex(ins.getHex());
		if (hsb[indexes[0]][indexes[1]][indexes[2]]==null) {
			hsb[indexes[0]][indexes[1]][indexes[2]] = ins;
			return ins;
		} else {
			for (Wallpaper wp: ins.getWallpapers())
				hsb[indexes[0]][indexes[1]][indexes[2]].addWallpaper(wp);
			return hsb[indexes[0]][indexes[1]][indexes[2]];
		}
	}
	
	public ColorDataPair find (ColorDataPair find) {
		int[] indexes = getIndexesForHex(find.getHex());
		if (hsb[indexes[0]][indexes[1]][indexes[2]]!=null) {
			return hsb[indexes[0]][indexes[1]][indexes[2]];
		}
		return null;
	}
	
	public ColorDataPair[] findLikeColors (ColorDataPair find, int tolerance) {
		int size = (int) ( (int)(2+tolerance*2) * Math.pow( (2+(tolerance/2)*2), 2) );
		ColorDataPair[] results = new ColorDataPair[ size ];
		int index = 0;
		int[] indexes = getIndexesForHex(find.getHex());
		System.out.println(find.getHex());
		
		int hueIndex = (indexes[0]-tolerance) < 0 ?
								(indexes[0]-tolerance)+(ARRAY_SIZE-1) 
									:(indexes[0]-tolerance) ;
								
		int hueTerminate = indexes[0]+tolerance+1 > ARRAY_SIZE-1 ?
								(indexes[0]+tolerance)-ARRAY_SIZE 
									:indexes[0]+tolerance+1 ;
								
		int lowerIndex = (indexes[1]-tolerance/2) < 0 ? 
								(indexes[1]-tolerance/2)+(ARRAY_SIZE-1) 
									:(indexes[1]-tolerance/2) ;
		
		//hue is a radial value such as colors on a color wheel and must be cycled 
		for (int h=hueIndex; h!=hueTerminate; h++ ) {
			h = h==ARRAY_SIZE ? 0 : h;
			for (int s=lowerIndex; s<ARRAY_SIZE; s++ ) {
				for (int b=lowerIndex; b<ARRAY_SIZE; b++ ) {
					System.out.println("h: "+h+" s: "+s+" b: "+b);
					
					if (hsb[h][s][b] != null) {
						System.out.println("ADDED");
						System.out.println(hsb[h][s][b]);
						results[index] = hsb[h][s][b];
						index++;
					}
				}
			}
		}
		ColorDataPair[] returnableResults = new ColorDataPair[index];
		for (int i=0; i<returnableResults.length; i++)
			returnableResults[i] = results[i];
		return returnableResults;
	}
	
	public static boolean isLikeColor (ColorDataPair color1, ColorDataPair color2, int tolerance) {
		int[] cindexes1 = getIndexesForHex(color1.getHex());
		int[] cindexes2 = getIndexesForHex(color2.getHex());
		for (int i=0; i<cindexes1.length; i++) {
			if (Math.abs(cindexes1[i]-cindexes2[i])>tolerance)
				return false;
		}
		return true;
	}
	
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
	
	private static int[] getIndexesForHex (String hex) {
		int red = Integer.parseInt( hex.substring(0, 2), 16);
		int green = Integer.parseInt( hex.substring(2, 4), 16);
		int blue = Integer.parseInt( hex.substring(4, 6), 16);
		float[] convert = Color.RGBtoHSB(red, green, blue, null);
		int[] indexes =
				new int[] {
						(int) convert[0]*100,
						(int) convert[1]*100,
						(int) convert[2]*100
				};
		return indexes;
	}
	
}
