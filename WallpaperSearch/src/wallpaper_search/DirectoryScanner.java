package wallpaper_search;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.awt.image.DataBufferByte;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ArrayIndexOutOfBoundsException;
import java.awt.Container;

public class DirectoryScanner extends JPanel implements Runnable {

	private ColorMap hsb;
	private String dir;
	private int tolerance;
	JTextField tf;
	JButton browseButton,scanButton;
	JFrame progressFrame;
	JProgressBar progressBar;
	int fileIndex = 0;

	public DirectoryScanner(ColorMap hsb, int tolerance) {
		this.hsb = hsb;
		this.tolerance = tolerance;
		super.setLayout(new GridLayout(1, 4));

		tf = new JTextField(20);
		browseButton = new JButton("browse...");
		scanButton = new JButton("scan");

		super.add(new JLabel("dir: "));

		super.add(tf);

		DirectoryScanner that = this;
		browseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = chooser.showOpenDialog(that);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					dir = chooser.getSelectedFile().getAbsolutePath();
					tf.setText(dir);
				}
			}
		});
		super.add(browseButton);
	    
		scanButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				progressFrame = new JFrame("Scanning Folder");
			    Container content = progressFrame.getContentPane();
			    progressBar = new JProgressBar(0,100);
			    progressBar.setValue(0);
			    progressBar.setStringPainted(true);
			    //content.add(progressBar, BorderLayout.NORTH);
			    JLabel statusLabel = new JLabel("		Scanning Files");
			    content.add(statusLabel);
			    progressFrame.setSize(300, 100);
				progressFrame.setVisible(true);
			    fileIndex = 0;
			    Thread t = new Thread(that, "directoryscan");
			    t.start();
			}
		});
		super.add(scanButton);

	}
	
	public void run() {
		scanDirectory();
	}

	private void processImageColors(BufferedImage image, String path, int w, int h) {
		final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		int width = image.getWidth();
		int height = image.getHeight();
		String[] newValues = new String[width * height];
		int index = 0;
		System.out.println("pixels: " + pixels.length);
		final int pixelLength = 3;
		// color processing http://stackoverflow.com/questions/6524196/java-get-pixel-array-from-image
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
				newValues[index] = ColorDataPair.makeHexVal(argb);
				index++;
			} else {
				String current = ColorDataPair.makeHexVal(argb);
				Boolean exists = false;
				for (int i = 0; i < index; i++) {
					if ( ColorMap.isLikeColor(current, newValues[i], tolerance) ) {
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
		 * for (int col = 0; col < height; col++) {System.out.println("col: "
		 * +col); for (int row = 0; row < width; row++) { if (index == 0) {
		 * newValues[index] = makeHexVal(image.getRGB(row, col)); index++; }
		 * else { String current = makeHexVal(image.getRGB(row, col)); Boolean
		 * exists = false; for (int i=0; i<index; i++) { if
		 * (hexDiff(current,newValues[i])>tolerance ) { exists = true; break; }
		 * } if (!exists) { newValues[index] = current; index++; }
		 * 
		 * } } } //outer for
		 */
		System.out.println(index);
		for (int i = 0; i < index; i++) {

			ColorDataPair existing = hsb.find(new ColorDataPair(newValues[i], path, width, height));
			System.out.println(existing);
			if (existing == null) {
				System.out.println("INSERT");
				hsb.insert(new ColorDataPair(newValues[i], path, width, height));
			} else {
				existing.addWallpaper(new Wallpaper(path, width, height));
			}

		}
	}

	private boolean scanDirectory() {
		try {
			//File folder = new File(this.dir);
			//int fileNum = folder.listFiles().length;
			Files.walk(Paths.get(this.dir)).forEach(filePath -> {
				if (Files.isRegularFile(filePath)) {
					System.out.println(filePath);
					String[] fullPath = filePath.toString().split("/");
					if (fullPath.length < 1)
						return;
					if (fullPath[fullPath.length - 1].indexOf('.') == -1)
						return;
					String[] filename = fullPath[fullPath.length - 1].split("\\.");
					if (filename.length < 1)
						return;
					String fileExtension = filename[filename.length - 1];
					// System.out.println(filename.length);
					if (fileExtension.toLowerCase().equals("png") || fileExtension.toLowerCase().equals("jpg")
							|| fileExtension.toLowerCase().equals("jpeg")) {
						System.out.println("is image");
						BufferedImage bimg;
						try {
							bimg = ImageIO.read(new File(filePath.toString()));
							System.out.println("pre processing");
							this.processImageColors(bimg, filePath.toString(), bimg.getWidth(), bimg.getHeight());
							System.out.println("post processing");

						} catch (IOException e) {
							System.out.println(e.toString());
						}
					}
				}
			});
		} catch (IOException e) {
			// do error stuff
			System.out.println(e.toString());
		}
		if (progressFrame!=null) {
			progressFrame.dispose();
			progressFrame = null;
		}
		return true;
	}

}
