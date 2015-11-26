package wallpaper_search;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.*;  

public class Core {

	private static JFrame mainFrame;
	private static DirectoryScanner directoryInput;
	private static JPanel searchColors;
	private static JPanel searchTags;
	private static JPanel resultsPanel;
	private static WP_Tree tree;
	private static int tolerance = 32;
	
	public static void main(String[] args) {
		mainFrame = new JFrame("Wallpaper Search");
		mainFrame.setSize(800,800);
		mainFrame.setLayout(new GridLayout(4, 1));
		
		tree = new WP_Tree();
		
		int sample = 128;
		int val = 0xff;
		System.out.println((sample & 0xff) );
		directoryInput = new DirectoryScanner(tree, tolerance);
		directoryInput.setLayout(new FlowLayout());
		directoryInput.scanDirectory("/Users/jameslevasseur/Desktop/testwp/");
		System.out.println("done.");
	}

}
