import java.awt.Graphics2D;
import java.awt.Dimension;
import java.awt.Image;
import javax.swing.JFrame;

public class Background {
	
	private Image bgImage;
  	
	private int xPos;
	private int yPos;
	
	private int width;
	private int height;

	public Background(String imageFile, int xPos, int yPos) {
    	this.bgImage = ImageManager.getInstance().loadImage(imageFile).getImage();
    	
    	this.xPos = xPos;
    	this.yPos = yPos;
    	
    	width = bgImage.getWidth(null);
    	height = bgImage.getHeight(null);
  	}

  	public void move() {
  		// Move World Relative To Player
  	}

  	public void draw (Graphics2D g2) {
		g2.drawImage(bgImage, xPos, yPos, null);
  	}
  	
  	public int getWidth() {
  		return width;
  	}
  	public int getHeight() {
  		return height;
  	}
}