package wallpaper_search;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import java.io.Serializable;

//code from http://stackoverflow.com/questions/299495/how-to-add-an-image-to-a-jpanel
public class ImagePanel extends JPanel implements Serializable {

	private static final long serialVersionUID = 435662233;
	private BufferedImage image;
	private int width, height;

	public ImagePanel(String path, int w, int h) {
		super.setPreferredSize(new Dimension(w, h));
		width = w;
		height = h;
		try {                
			image = ImageIO.read(new File(path));
		} catch (IOException ex) {
			// handle exception...
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(image, 0, 0, width, height, null); // see javadoc for more info on the parameters            
	}

}