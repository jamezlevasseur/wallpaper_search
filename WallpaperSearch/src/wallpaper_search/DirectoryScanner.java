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

	private ColorStructure hsb;
	private String dir;
	private int tolerance;
	private JTextField tf;
	private JButton browseButton,scanButton;
	private JFrame progressFrame;
	private CSVWriter csv;
	int insertCount = 1;
	
	private static final int PIXEL_SKIP = 10;

	public DirectoryScanner(ColorStructure hsb, int tolerance) throws IOException {
		csv = new CSVWriter("/Users/jameslevasseur/Desktop/processing.csv", "ImageProcessingTime");
		
		this.hsb = hsb;
		this.tolerance = tolerance;
		//to reference later where "this" is not the instance of DirectoryScanner
		DirectoryScanner that = this;
		
		super.setLayout(new GridLayout(1, 4));

		tf = new JTextField(20);
		browseButton = new JButton("browse...");
		scanButton = new JButton("scan");

		super.add(new JLabel("dir: "));
		super.add(tf);
		
		//browse button for adding directories to scan
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
	    
		//scan button that runs the scan and gives feedbacl
		scanButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (that.dir!=null && !that.dir.equals("")) {
					//for user feedback
					progressFrame = new JFrame("Scanning Folder");
				    Container content = progressFrame.getContentPane();
				    JLabel statusLabel = new JLabel("Scanning Files");
				    content.add(statusLabel);
				    progressFrame.setSize(300, 100);
					progressFrame.setVisible(true);
					//the scanning is done in a separate thread so as not to lock up the GUI
				    Thread t = new Thread(that, "directoryscan");
				    t.start();
				}
			}
		});
		super.add(scanButton);

	}
	
	//implemented as a part of runnable
	//runs scanDirectory for the separate thread
	public void run() {
		long start = System.nanoTime();
		scanDirectory();
		System.out.println("Full Dir Scan Process Time: "+(System.nanoTime()-start));
	}

	/**
	 * @param image - image to be scanned
	 * @param path - path to that image
	 * @param w - width of image
	 * @param h - height of image
	 * Description - This method scans an images colors via pixel array and stores colors
	 * that are determined to vary enough by tolerance into the objects ColorMap
	 * Code Attribution - http://stackoverflow.com/questions/6524196/java-get-pixel-array-from-image
	 */
	private void processImageColors(BufferedImage image, String path, int w, int h) throws IOException {
		//get pixels
		final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		int width = image.getWidth();
		int height = image.getHeight();
		//new values is the array of potential new hex strings to be added
		String[] newValues = new String[width * height];
		//index of new values
		int index = 0;
		System.out.println("pixels: " + pixels.length);
		//TODO remove after testing
		long start = System.nanoTime();
		//to improve running time for such large images, a certain amount of pixels
		//are skipped still giving a broad sample size out of a (example) 500,000+ pixel array
		final int pixelLength = 3*PIXEL_SKIP;
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
			//if no new values yet, the color cannot already be in the array
			if (index == 0) {
				newValues[index] = ColorDataPair.makeHexVal(argb);
				index++;
			} else {
				String current = ColorDataPair.makeHexVal(argb);
				Boolean exists = false;
				//compare to existing values
				for (int i = 0; i < index; i++) {
					if ( ColorMap.isLikeColor(current, newValues[i], tolerance) ) {
						exists = true;
						break;
					}
				}
				//add if color varies from existing colors enough
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
		//TODO remove after testing
		try {
		csv.write(""+(System.nanoTime()-start));
		}catch (IOException e) {}
		//System.out.println("Single Image Process Time: "+(System.nanoTime()-start));
		System.out.println("Index "+index);
		int count = 0;
		CSVWriter insertTime = new CSVWriter("/Users/jameslevasseur/Desktop/insert"+insertCount+".csv", "Insert Time");
		insertCount++;
		//for new values
		for (int i = 0; i < index; i++) {
			count = i;
			ColorDataPair existing = hsb.find(new ColorDataPair(newValues[i], path, width, height));
			//if hex string does not exist in color map add new, else add new wallpaper obj
			//to existing ColorDataPair
			if (existing == null) {
				long istart = System.nanoTime();
				System.out.println("Value: "+newValues[i]+" Path:"+path);
				hsb.insert(new ColorDataPair(newValues[i], path, width, height));
				//System.out.println("InsertTime: "+(System.nanoTime()-istart));
				insertTime.write(""+(System.nanoTime()-istart));
			} else {
				existing.addWallpaper(new Wallpaper(path, width, height));
			}

		}
		insertTime.end();
		System.out.println("Inserted: "+count);
	}

	/**
	 * Description - scans a directory and processes the image within
	 */
	private void scanDirectory() {
		try {
			//walk through files in stored directory
			Files.walk(Paths.get(this.dir)).forEach(filePath -> {
				if (Files.isRegularFile(filePath)) {
					String[] fullPath = filePath.toString().split("/");
					if (fullPath.length < 1)
						return;
					if (fullPath[fullPath.length - 1].indexOf('.') == -1)
						return;
					String[] filename = fullPath[fullPath.length - 1].split("\\.");
					if (filename.length < 1)
						return;
					String fileExtension = filename[filename.length - 1];
					//check if file is image
					if (fileExtension.toLowerCase().equals("png") || fileExtension.toLowerCase().equals("jpg")
							|| fileExtension.toLowerCase().equals("jpeg")) {
						//if so read the image and scan it into ColorMap
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
			//TODO add error things
			System.out.println(e.toString());
		}
		if (progressFrame!=null) {
			progressFrame.dispose();
			progressFrame = null;
		}try{csv.end();}catch(IOException e){}
	}

}
