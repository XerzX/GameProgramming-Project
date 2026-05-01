import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class ImageManager {
	
	private static ImageManager instance;
	
	private ImageManager( ) {
		
	}
	
	public static ImageManager getInstance() {
		if (instance == null) {
			instance = new ImageManager();
		}
		return instance;
	}
	
	public ImageIcon loadImage(String filePath) {
		return new ImageIcon(getClass().getResource(filePath));
	}
	
	public BufferedImage loadBufferedImage(String path) {
	    BufferedImage bi = null;

	    try {
			
	        bi = ImageIO.read(getClass().getResource(path));
	    } 
	    catch (IOException | IllegalArgumentException e) {
	        System.out.println("Error loading resource " + path + ": " + e);
	    }

	    return bi;
	}

	public BufferedImage copyImage(BufferedImage src) {
		if (src == null)
			return null;


		int imWidth = src.getWidth();
		int imHeight = src.getHeight();

		BufferedImage copy = new BufferedImage (imWidth, imHeight,
							BufferedImage.TYPE_INT_ARGB);

    		Graphics2D g2d = copy.createGraphics();

    		// copy image
    		g2d.drawImage(src, 0, 0, null);
    		g2d.dispose();

    		return copy; 
	}
	
	public BufferedImage toBufferedImage(Image image) {
		if (image instanceof BufferedImage) return (BufferedImage) image;
		
		BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = bufferedImage.createGraphics();
		
		g2.drawImage(image, 0, 0, null);
		g2.dispose();
		
		return bufferedImage;
	}
}