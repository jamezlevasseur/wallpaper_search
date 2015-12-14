package wallpaper_search;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.colorchooser.ColorSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.*;  
import javax.swing.colorchooser.AbstractColorChooserPanel;
import java.util.ArrayList;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Container;

public class Core {

	private static JFrame mainFrame;
	private static DirectoryScanner directoryInput;
	private static JPanel rootPanel,sizePanel,searchColors,resultsPanel,selectColorsPanel,searchTags,tolerancePanel;
	private static JScrollPane resultsScroll;
	private static ArrayList<JPanel> colorPanels;
	private static ArrayList<Wallpaper> lastResults;
	private static ColorMap hsb;
	private static int scanTolerance = 30;
	private static int searchTolerance = 3;
	private static String imageSize;

	private final static int WALLPAPER_SCALE = 10;
	private final static int RESULTS_COL = 2;

	public static void main(String[] args) throws IOException {
		//mainFrame setup
		mainFrame = new JFrame("Wallpaper Search");
		mainFrame.setSize(800,800);

		mainFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent windowEvent){
				System.exit(0);
			}        
		});

		rootPanel = new JPanel(new SpringLayout());

		//Directory Input setup
		hsb = new ColorMap();
		directoryInput = new DirectoryScanner(hsb, scanTolerance);
		directoryInput.setLayout(new FlowLayout());
		rootPanel.add(directoryInput);

		//searchColors panel setup
		colorPanels = new ArrayList<JPanel>();

		searchColors = new JPanel();
		searchColors.setLayout(new FlowLayout());
		searchColors.add(new JLabel("Search Colors:"));
		selectColorsPanel = new JPanel();
		selectColorsPanel.setLayout(new FlowLayout());
		searchColors.add(selectColorsPanel);

		//choose color button allows user to add colors
		JButton chooseColorButton = new JButton("add");
		chooseColorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//creates a pop up for color chooser
				JFrame popup = new JFrame();
				popup.setSize(800,800);
				popup.setLayout(new GridLayout(2, 1));
				popup.setLocationByPlatform(true);

				JColorChooser chooser = new JColorChooser();
				//get and remove unwanted panels
				AbstractColorChooserPanel[] oldPanels = chooser.getChooserPanels();
				chooser.removeChooserPanel(oldPanels[0]);
				chooser.removeChooserPanel(oldPanels[1]);
				chooser.removeChooserPanel(oldPanels[2]);
				chooser.removeChooserPanel(oldPanels[4]);

				//add a new color panel and add listener
				colorPanels.add(new JPanel());
				colorPanels.get(colorPanels.size()-1).addMouseListener(new MouseAdapter() {
					@Override
					public void mouseReleased(MouseEvent e) {
						//when clicked remove this panel from search
						Container parent = e.getComponent().getParent();
						parent.remove(e.getComponent());
						colorPanels.remove((JPanel) e.getComponent());
						parent.validate();
						parent.repaint();
						System.out.println("remove");
						showSearchResults();
					}
				});
				//set size of color panel just added
				colorPanels.get(colorPanels.size()-1).setPreferredSize(new Dimension(20, 20));

				//setup change color listener
				ColorSelectionModel model = chooser.getSelectionModel();
				ChangeListener changeListener = new ChangeListener() {
					public void stateChanged(ChangeEvent changeEvent) {
						Color newForegroundColor = chooser.getColor();
						colorPanels.get(colorPanels.size()-1).setBackground(newForegroundColor);
					}
				};
				model.addChangeListener(changeListener);
				popup.add(chooser);

				//button to end color picking
				JButton popupButton = new JButton("done");
				popupButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						//add the new color panel to the main window
						selectColorsPanel.add(colorPanels.get(colorPanels.size()-1), 0);
						selectColorsPanel.updateUI();
						//update search results
						showSearchResults();
						//remove popup
						popup.dispose();
					}
				});

				//add panel with popup button
				JPanel pan = new JPanel();
				pan.setLayout(new FlowLayout());
				pan.add(popupButton);
				popup.add(pan);
				popup.setVisible(true);
			}
		});
		searchColors.add(chooseColorButton);
		rootPanel.add(searchColors);

		//Search Tags Panel
		searchTags = new JPanel();
		searchTags.setLayout(new FlowLayout());
		searchTags.add(new JLabel("Search Tags (separated by comma):"));
		JTextField tf = new JTextField(20);
		searchTags.add(tf);
		rootPanel.add(searchTags);

		//size panel
		JRadioButton anySizeButton = new JRadioButton("any size");
		anySizeButton.setActionCommand("any size");
		anySizeButton.setSelected(true);
		imageSize = "any size";
		
		JRadioButton mobileButton = new JRadioButton("mobile");
		mobileButton.setActionCommand("mobile");

		JRadioButton smallButton = new JRadioButton("small");
		smallButton.setActionCommand("small");

		JRadioButton mediumButton = new JRadioButton("medium");
		mediumButton.setActionCommand("medium");

		JRadioButton largeButton = new JRadioButton("large");
		largeButton.setActionCommand("large");

		ButtonGroup sizeGroup = new ButtonGroup();
		sizeGroup.add(anySizeButton);
		sizeGroup.add(mobileButton);
		sizeGroup.add(smallButton);
		sizeGroup.add(mediumButton);
		sizeGroup.add(largeButton);

		ActionListener radioListener = new ActionListener () {
			@Override
			public void actionPerformed(ActionEvent e) {
				imageSize = e.getActionCommand();
				updateResultsForSize();
			}
		};
		
		anySizeButton.addActionListener(radioListener);
		mobileButton.addActionListener(radioListener);
		smallButton.addActionListener(radioListener);
		mediumButton.addActionListener(radioListener);
		largeButton.addActionListener(radioListener);
		
		sizePanel = new JPanel(new GridLayout(1, 4));
		
		sizePanel.add(anySizeButton);
		sizePanel.add(mobileButton);
		sizePanel.add(smallButton);
		sizePanel.add(mediumButton);
		sizePanel.add(largeButton);
		
		rootPanel.add(sizePanel);

		//scanTolerance Panel
		tolerancePanel = new JPanel();
		tolerancePanel.add(new JLabel("search tolerance: "));
		//init slider
		JSlider toleranceSlider = new JSlider(1, 30, searchTolerance);
		//init label to show slider value
		JLabel sliderValLabel = new JLabel(""+toleranceSlider.getValue());
		//slider change listener
		toleranceSlider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				Object source = e.getSource();
				if (source instanceof JSlider) {
					JSlider theJSlider = (JSlider) source;
					if (!theJSlider.getValueIsAdjusting()) {
						searchTolerance = theJSlider.getValue();
						sliderValLabel.setText(""+searchTolerance);
					}
				}

			}
		});
		tolerancePanel.add(toleranceSlider);
		tolerancePanel.add(sliderValLabel);
		rootPanel.add(tolerancePanel);

		SpringUtilities.makeGrid(rootPanel,
				5, 1, //rows, cols
				2, 2, //initialX, initialY
				3, 3);//xPad, yPad
		System.out.println("done.");
		mainFrame.add(rootPanel);
		mainFrame.pack();
		mainFrame.setVisible(true);  
	}

	public static void showSearchResults () {
		System.out.println("show search");
		System.out.println("colorPanels: "+colorPanels.size());
		//arraylist for results
		ArrayList<ColorDataPair> searchResults = new ArrayList<ColorDataPair>();
		ColorDataPair[][] preResults = new ColorDataPair[colorPanels.size()][];
		//only display new if filter colors are selected
		if (colorPanels.size()>0) {
			//for colors selected
			for (int i=0; i<colorPanels.size(); i++) {
				//get results for colors like the ones selected
				preResults[i] = hsb.findLikeColors(
						new ColorDataPair(
								ColorDataPair.makeHexVal( colorPanels.get(i).getBackground().hashCode() ))
						,searchTolerance );
			}

			//if more than one color is selected
			if (colorPanels.size()>1) {
				//first color is primary
				ColorDataPair[] primary = preResults[0];
				//for other colors
				for (int s=1; s<colorPanels.size(); s++ ) {
					ColorDataPair[] secondary = preResults[s];
					for (int o=0; o<primary.length; o++) {
						for (int i=0; i<secondary.length; i++) {
							//if color two colors are similar then add both
							System.out.println(primary.length+" "+secondary.length);
							System.out.println(o+" "+i);
							if (ColorMap.isLikeColor(primary[o],secondary[i],searchTolerance)) {
								searchResults.add(secondary[i]);
								if (!searchResults.contains(primary[o]))
									searchResults.add(primary[o]);
							}
						}
					}
				}
			} else {
				//add all colors
				for (ColorDataPair current: preResults[0]) {
					searchResults.add(current);
				}
			}

		}

		//arraylist of wallpapers
		ArrayList<Wallpaper> wallpapers = new ArrayList<Wallpaper>();
		System.out.println("results: "+searchResults.size());
		//for all results
		for (ColorDataPair current: searchResults) {
			System.out.println(current.toString());
			ArrayList<Wallpaper> currentWallpapers = current.getWallpapers();
			//for wallpapers in results' wallpapers
			for (Wallpaper wp: currentWallpapers) {
				boolean exists = false;
				//for wallpapers already added
				for (Wallpaper existing: wallpapers) {
					//if another wallpaper has the same path it already exists
					if (existing.getFilePath().equals(wp.getFilePath())) {
						exists = true;
						break;
					}
				}
				//if the wallpaper doesn't already exist, add it
				if (!exists) {
					System.out.println(imageSize);
					if (imageSize.equals("any size") || imageSize.equals(wp.size))
						wallpapers.add(wp);
				}
			}
		}
		
		resultsPanel = new JPanel(new GridLayout(wallpapers.size(), 1));
		resultsPanel.setPreferredSize(new Dimension(800, 800));
		resultsScroll = new JScrollPane(resultsPanel);
		resultsScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		resultsPanel.setBackground(Color.white);
		
		//add results
		int count = 0;
		for (int i=0; i<wallpapers.size(); i++) {
			count = i+1;
			System.out.println("wallpaper file path: "+wallpapers.get(i).getFilePath());
			JPanel displayPanel = new JPanel();
			displayPanel.setMinimumSize(new Dimension(500, 200));
			displayPanel.setPreferredSize(new Dimension(500, 200));
			displayPanel.add(new ImagePanel(wallpapers.get(i).getFilePath(),wallpapers.get(i).width/WALLPAPER_SCALE,wallpapers.get(i).height/WALLPAPER_SCALE));
			displayPanel.add(new JLabel("<html><body style=\"width:200px;\">"+wallpapers.get(i).getFilePath()+"</body></html>"));
			resultsPanel.add(displayPanel);
		}
		if (wallpapers.size()==0) {
			resultsPanel.add(new JLabel("No results found :("));
		}
		
		lastResults = wallpapers;
		
		JFrame popupFrame = new JFrame ();
		popupFrame.setSize(800,800);
		popupFrame.add(resultsPanel);
		popupFrame.pack();
		popupFrame.setVisible(true);
		System.out.println(count);
	}
	
	private static void updateResultsForSize () {
		ArrayList<Wallpaper> wallpapers = new ArrayList<Wallpaper>();
		
		for (Wallpaper wp: lastResults) {
			if (imageSize.equals("any size") || imageSize.equals(wp.size))
				wallpapers.add(wp);
		}
		
		resultsPanel = new JPanel(new GridLayout(wallpapers.size(), 1));
		resultsPanel.setPreferredSize(new Dimension(800, 800));
		resultsScroll = new JScrollPane(resultsPanel);
		resultsScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		resultsPanel.setBackground(Color.white);
		
		//add results
		int count = 0;
		for (int i=0; i<wallpapers.size(); i++) {
			count = i+1;
			System.out.println("wallpaper file path: "+wallpapers.get(i).getFilePath());
			JPanel displayPanel = new JPanel();
			displayPanel.setMinimumSize(new Dimension(500, 200));
			displayPanel.setPreferredSize(new Dimension(500, 200));
			displayPanel.add(new ImagePanel(wallpapers.get(i).getFilePath(),wallpapers.get(i).width/WALLPAPER_SCALE,wallpapers.get(i).height/WALLPAPER_SCALE));
			displayPanel.add(new JLabel("<html><body style=\"width:200px;\">"+wallpapers.get(i).getFilePath()+"</body></html>"));
			resultsPanel.add(displayPanel);
		}
		if (wallpapers.size()==0) {
			resultsPanel.add(new JLabel("No results found :("));
		}
		
		JFrame popupFrame = new JFrame ();
		popupFrame.setSize(800,800);
		popupFrame.add(resultsPanel);
		popupFrame.pack();
		popupFrame.setVisible(true);
		System.out.println(count);
	}



}
