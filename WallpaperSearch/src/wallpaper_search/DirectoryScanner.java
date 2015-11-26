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
import java.lang.ArrayIndexOutOfBoundsException;

public class DirectoryScanner extends JPanel {

	private RedBlackTree rbtree;
	private String treeType;
	private String dir;
	private int tolerance;

	public DirectoryScanner (RedBlackTree tree, int tolerance) {
		this.rbtree = tree;
		this.tolerance = tolerance;
		this.treeType = "rb";
	}


	private void processImageColors(BufferedImage image, String path, int w, int h) {
		final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		int width = image.getWidth();
		int height = image.getHeight();
		String[] newValues = new String[width*height];
		int index = 0;
		System.out.println("pixels: "+pixels.length);
		final int pixelLength = 3;
		for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
			int argb = 0;
			try {
				argb += -16777216; // 255 alpha
				argb += ((int) pixels[pixel] & 0xff); // blue
				argb += (((int) pixels[pixel + 1] & 0xff) << 8); // green
				argb += (((int) pixels[pixel + 2] & 0xff) << 16); // red
			} catch (ArrayIndexOutOfBoundsException e) {
				continue;
			}
			if (index == 0) {
				newValues[index] = makeHexVal(argb);
				index++;
			} else {
				String current = makeHexVal(argb);
				Boolean exists = false;
				for (int i=0; i<index; i++) {
					if (hexDiff(current,newValues[i])<tolerance ) {
						exists = true;
						break;
					}
				}
				if (!exists) {
					newValues[index] = current;
					index++;
				}

			}
			row++;
			if (row == width) {
				row = 0;
				col++;
			}
		}

		/*
		for (int col = 0; col < height; col++) {System.out.println("col: "+col);
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
		 */
		System.out.println(index);
		for (int i=0; i<index; i++) {
			if (treeType.equals("rb")) {
				ColorDataPair existing = (ColorDataPair) rbtree.find(new ColorDataPair(newValues[i], path, width, height));
				if (existing==null)
					this.rbtree.insert(new ColorDataPair(newValues[i], path, width, height));
				else
					existing.addWallpaper(new Wallpaper(path,width,height));
			}
		}
	}

	private int hexDiff (String hex1, String hex2) {
		int redDiff = Math.abs( Integer.parseInt(hex1.substring(0,2), 16) - Integer.parseInt(hex2.substring(0,2), 16) );
		int greenDiff = Math.abs( Integer.parseInt(hex1.substring(2,4), 16) - Integer.parseInt(hex2.substring(2,4), 16) );
		int blueDiff = Math.abs( Integer.parseInt(hex1.substring(4,6), 16) - Integer.parseInt(hex2.substring(4,6), 16) );
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
							System.out.println("pre processing");
							this.processImageColors(bimg, filePath.toString(), bimg.getWidth(), bimg.getHeight());
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
