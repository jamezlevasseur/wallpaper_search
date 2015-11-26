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
	private static RedBlackTree rbtree;
	private static int tolerance = 150;
	
	public static void main(String[] args) {
		mainFrame = new JFrame("Wallpaper Search");
		mainFrame.setSize(800,800);
		mainFrame.setLayout(new GridLayout(4, 1));
		rbtree = new RedBlackTree();
		directoryInput = new DirectoryScanner(rbtree, tolerance);
		directoryInput.setLayout(new FlowLayout());
		directoryInput.scanDirectory("/Users/jameslevasseur/Desktop/wp/");
		rbtree.printTree();
		System.out.println("done.");
	}

}
