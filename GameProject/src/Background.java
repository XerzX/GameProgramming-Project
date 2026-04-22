import java.awt.Graphics2D;
import java.awt.Dimension;
import java.awt.Image;
import javax.swing.JFrame;

public class Background {
	
	private Image bgImage;
  	
	private int width;
	private int height;

	public Background(String imageFile) {
    	this.bgImage = ImageManager.getInstance().loadImage(imageFile).getImage();
    	
    	width = bgImage.getWidth(null);
    	height = bgImage.getHeight(null);
  	}

  	public void move() {
  		// Move World Relative To Player
  	}

  	public void draw (Graphics2D g2) {
		g2.drawImage(bgImage, 0, 0, null);
  	}
  	
  	public int getWidth() {
  		return width;
  	}
  	public int getHeight() {
  		return height;
  	}
}