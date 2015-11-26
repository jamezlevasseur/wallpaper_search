package wallpaper_search;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.awt.image.DataBufferByte;
import java.awt.Color;
import java.math.*;


public class DirectoryScanner extends JPanel {

	private WP_Tree tree;
	private String dir;
	private int tolerance;

	public DirectoryScanner (WP_Tree tree, int tolerance) {
		this.tree = tree;
		this.tolerance = tolerance;
	}


	private void processImageColors(BufferedImage image) {
		int width = image.getWidth();
		int height = image.getHeight();
		String[] newValues = new String[width*height];
		int index = 0;
		
		for (int col = 0; col < height; col++) {
			for (int row = 0; row < width; row++) {
				if (index == 0) {
					newValues[index] = makeHexVal(image.getRGB(row, col));
					index++;
				} else {
					String current = makeHexVal(image.getRGB(row, col));
					Boolean exists = false;
					for (int i=0; i<index; i++) {
						if (hexDiff(current,newValues[i])>tolerance ) {
							exists = true;
							break;
						}
					}
					if (!exists) {
						newValues[index] = current;
						index++;
					}
					
				}
			}
		} //outer for
		
		//TODO insert new vals into tree
	}
	
	private int hexDiff (String hex1, String hex2) {
		int redDiff = Math.abs( Integer.parseInt(hex1.substring(0,2)) - Integer.parseInt(hex2.substring(0,2)) );
		int greenDiff = Math.abs( Integer.parseInt(hex1.substring(2,4)) - Integer.parseInt(hex2.substring(2,4)) );
		int blueDiff = Math.abs( Integer.parseInt(hex1.substring(4,6)) - Integer.parseInt(hex2.substring(4,6)) );
		return redDiff+greenDiff+blueDiff;
	}

	private String makeHexVal (int val) {
		String hexColour = Integer.toHexString(val & 0xffffff);
		if (hexColour.length() < 6) {
			hexColour = "000000".substring(0, 6 - hexColour.length()) + hexColour;
		}
		return hexColour;
	}

	public boolean scanDirectory (String newDir) {
		dir = newDir;
		try {
			Files.walk(Paths.get(this.dir)).forEach(filePath -> {
				if (Files.isRegularFile(filePath)) {
					System.out.println(filePath);
					String[] fullPath =  filePath.toString().split("/");
					if (fullPath.length<1)
						return;
					if (fullPath[fullPath.length-1].indexOf('.')==-1)
						return;
					String[] filename = fullPath[fullPath.length-1].split("\\.");
					if (filename.length<1)
						return;
					String fileExtension = filename[filename.length-1];
					//System.out.println(filename.length);
					if (fileExtension.toLowerCase().equals("png") ||
							fileExtension.toLowerCase().equals("jpg") ||
							fileExtension.toLowerCase().equals("jpeg")) {
						System.out.println("is image");
						BufferedImage bimg;
						try {
							bimg = ImageIO.read(new File(filePath.toString()));
							

						} catch (IOException e) {
							System.out.println(e.toString());
						}
					}
				}
			});
		}
		catch (IOException e) {
			//do error stuff
			System.out.println(e.toString());
		}
		return false;
	}

}
