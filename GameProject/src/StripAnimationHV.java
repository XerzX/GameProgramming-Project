import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;

/**
    The StripAnimation class creates an animation from a strip file.
*/
public class StripAnimationHV {
	
	Animation animation;

	private int x;
	private int y;

	private int imageWidth;
	private int imageHeight;

	public StripAnimationHV(boolean Loop, String filePath, int outerBound, int innerBound) {
		// Instantiate Animation Object
		animation = new Animation(Loop);

		// Load Image From Strip File
		Image stripImage = ImageManager.getInstance().loadImage(filePath).getImage();

		// Derive frame dimensions from the actual grid layout passed in
		imageWidth  = stripImage.getWidth(null)  / innerBound; // columns
		imageHeight = stripImage.getHeight(null) / outerBound; // rows

		for (int i = 0; i < outerBound; i++) {       // row
			for (int j = 0; j < innerBound; j++) {   // column

				BufferedImage frameImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
				Graphics2D g = (Graphics2D) frameImage.getGraphics();

				g.drawImage(stripImage,
						0, 0, imageWidth, imageHeight,
						j * imageWidth, i * imageHeight,
						(j + 1) * imageWidth, (i + 1) * imageHeight,
						null);

				g.dispose(); // release native graphics resource
				animation.addFrame(frameImage, 100);
			}
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
