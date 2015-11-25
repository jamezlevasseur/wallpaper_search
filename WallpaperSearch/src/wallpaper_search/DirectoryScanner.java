package wallpaper_search;

import javax.swing.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;


public class DirectoryScanner extends JPanel {
	
	private WP_Tree tree;
	private String dir;
	
	public DirectoryScanner (WP_Tree tree) {
		this.tree = tree;
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
		        		System.out.println(bimg.getHeight());
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
