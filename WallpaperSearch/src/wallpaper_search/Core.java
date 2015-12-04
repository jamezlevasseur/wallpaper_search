package wallpaper_search;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
	private static JPanel searchColors,resultsPanel,selectColorsPanel,searchTags,tolerancePanel;
	private static JScrollPane resultsScroll;
	private static ArrayList<JPanel> colorPanels;
	private static ColorMap hsb;
	private static int scanTolerance = 50;
	private static int searchTolerance = 3;
	
	private final static int WALLPAPER_SCALE = 10;

	public static void main(String[] args) {
		mainFrame = new JFrame("Wallpaper Search");
		mainFrame.setSize(800,800);
		GridLayout mfLayout = new GridLayout(6, 1);
		mfLayout.setHgap(10);
		System.out.println( mfLayout.getHgap() );
		mainFrame.setLayout(mfLayout);
		
		mainFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent windowEvent){
				System.exit(0);
			}        
		});
		hsb = new ColorMap();
		//Directory Input
		directoryInput = new DirectoryScanner(hsb, scanTolerance);
		directoryInput.setLayout(new FlowLayout());
		mainFrame.add(directoryInput);

		//Color Panel
		colorPanels = new ArrayList<JPanel>();

		searchColors = new JPanel();
		searchColors.setLayout(new FlowLayout());
		searchColors.add(new JLabel("Search Colors:"));
		selectColorsPanel = new JPanel();
		selectColorsPanel.setLayout(new FlowLayout());
		searchColors.add(selectColorsPanel);

		JButton chooseColorButton = new JButton("add");
		chooseColorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				JFrame popup = new JFrame();
				popup.setSize(800,800);
				popup.setLayout(new GridLayout(2, 1));

				JColorChooser chooser = new JColorChooser();
				AbstractColorChooserPanel[] oldPanels = chooser.getChooserPanels();
				chooser.removeChooserPanel(oldPanels[0]);
				chooser.removeChooserPanel(oldPanels[1]);
				chooser.removeChooserPanel(oldPanels[2]);
				chooser.removeChooserPanel(oldPanels[4]);

				colorPanels.add(new JPanel());
				colorPanels.get(colorPanels.size()-1).addMouseListener(new MouseAdapter() {
					@Override
					public void mouseReleased(MouseEvent e) {
						Container parent = e.getComponent().getParent();
						parent.remove(e.getComponent());
						parent.validate();
						parent.repaint();
						System.out.println("remove");
					}
				});
				colorPanels.get(colorPanels.size()-1).setPreferredSize(new Dimension(20, 20));

				ColorSelectionModel model = chooser.getSelectionModel();
				ChangeListener changeListener = new ChangeListener() {
					public void stateChanged(ChangeEvent changeEvent) {
						Color newForegroundColor = chooser.getColor();
						colorPanels.get(colorPanels.size()-1).setBackground(newForegroundColor);
					}
				};

				model.addChangeListener(changeListener);
				popup.add(chooser);
				JButton popupButton = new JButton("done");
				popupButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						selectColorsPanel.add(colorPanels.get(colorPanels.size()-1), 0);
						selectColorsPanel.updateUI();
						showSearchResults();
						popup.dispose();
					}
				});

				JPanel pan = new JPanel();
				pan.setLayout(new FlowLayout());
				pan.add(popupButton);
				popup.add(pan);
				popup.setVisible(true);
			}
		});
		searchColors.add(chooseColorButton);
		mainFrame.add(searchColors);

		//Search Tags Panel
		searchTags = new JPanel();
		searchTags.setLayout(new FlowLayout());
		searchTags.add(new JLabel("Search Tags:"));
		JTextField tf = new JTextField(20);
		searchTags.add(tf);
		mainFrame.add(searchTags);

		//scanTolerance Panel
		tolerancePanel = new JPanel();
		tolerancePanel.add(new JLabel("search tolerance: "));
		JSlider toleranceSlider = new JSlider(0, 100, searchTolerance);
		JLabel sliderValLabel = new JLabel(""+toleranceSlider.getValue());
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
		mainFrame.add(tolerancePanel);

		resultsPanel = new JPanel();
		resultsPanel.setLayout(new FlowLayout());
		resultsPanel.setMinimumSize(new Dimension(600, 200));
		resultsScroll = new JScrollPane(resultsPanel);
		resultsScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		resultsPanel.setBackground(Color.white);
		mainFrame.add(resultsScroll);
		
		System.out.println("done.");
		mainFrame.setVisible(true);  
	}
	
	public static void showSearchResults () {
		System.out.println("show search");
		System.out.println("colorPanels: "+colorPanels.size());
		//colorList
		ArrayList<ColorDataPair> searchResults = new ArrayList<ColorDataPair>();
		if (colorPanels.size()>0) {
			for (int i=0; i<colorPanels.size(); i++) {
				ColorDataPair[] results = hsb.findLikeColors(
						new ColorDataPair(
								ColorDataPair.makeHexVal( colorPanels.get(i).getBackground().hashCode() ))
								,searchTolerance );
				for (ColorDataPair current: results) {
					searchResults.add(current);
				}
			}
			
		}
		
		ArrayList<Wallpaper> wallpapers = new ArrayList<Wallpaper>();
		System.out.println("results: "+searchResults.size());
		for (ColorDataPair current: searchResults) {
			System.out.println(current.toString());
			ArrayList<Wallpaper> currentWallpapers = current.getWallpapers();
			for (Wallpaper wp: currentWallpapers) {
				boolean exists = false;
				for (Wallpaper existing: wallpapers) {
					if (existing.getFilePath().equals(wp.getFilePath())) {
						exists = true;
						break;
					}
				}
				if (!exists) {
					wallpapers.add(wp);
				}
			}
		}
		resultsPanel.removeAll();
		for (int i=0; i<wallpapers.size(); i++) {
			System.out.println(wallpapers.get(i).getFilePath());
			resultsPanel.add(new ImagePanel(wallpapers.get(i).getFilePath(),wallpapers.get(i).width/WALLPAPER_SCALE,wallpapers.get(i).height/WALLPAPER_SCALE));
		}
	}



}
