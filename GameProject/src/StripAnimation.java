import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;

/**
    The StripAnimation class creates an animation from a strip file.
*/
public class StripAnimation {
	
	Animation animation;

	private int x;
	private int y;

	private int imageWidth;
	private int imageHeight;

	public StripAnimation(boolean Loop, String filePath, int outerBound) {
		// Instantiate Animation Object
		animation = new Animation(Loop);

		// Load Image From Strip File
		Image stripImage = ImageManager.getInstance().loadImage(filePath).getImage();

		imageWidth = (int) stripImage.getWidth(null) / 6;
		imageHeight = stripImage.getHeight(null);

		for (int i = 0; i < outerBound; i++) {

			BufferedImage frameImage = new BufferedImage (imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = (Graphics2D) frameImage.getGraphics();
     
			g.drawImage(stripImage, 
					0, 0, imageWidth, imageHeight,
					i*imageWidth, 0, (i*imageWidth)+imageWidth, imageHeight,
					null);

			animation.addFrame(frameImage, 100);
		}
	}

	public void start() {
		animation.start();
	}

	public void stop() {
		animation.stop();
	}
	
	public void update() {
		if (!animation.isStillActive())
			return;

		animation.update();
	}

	public boolean isAnimationActive() {
		return animation.isStillActive();
	}

	public Image getCurrentFrame() {
		return animation.getImage();
	}

	public void draw(Graphics2D g2) {
		if (!animation.isStillActive())
			return;

		g2.drawImage(animation.getImage(), x, y, 70, 50, null);
	}
}
