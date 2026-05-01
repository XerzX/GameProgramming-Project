import java.awt.image.BufferedImage;

/**
 * Singleton image effect that applies a blue tint to any BufferedImage.
 *
 * Usage:
 * BufferedImage tinted = BlueTintFX.getInstance().apply(originalImage);
 *
 * The original image is never modified; a tinted copy is returned.
 */
public class BlueTintFX {

	private static BlueTintFX instance;

	private BlueTintFX() {

	}

	public static BlueTintFX getInstance() {
		if (instance == null) {
			instance = new BlueTintFX();
		}
		return instance;
	}

	/**
	 * Returns a blue-tinted copy of src.
	 * The original image is left untouched.
	 *
	 * src the source image to tint
	 * returns a new BufferedImage with the blue tint applied
	 */
	public BufferedImage apply(BufferedImage src) {
		// Create an independent copy so the original stays untouched
		BufferedImage copy = ImageManager.getInstance().copyImage(src);

		int imWidth = copy.getWidth();
		int imHeight = copy.getHeight();

		int[] pixels = new int[imWidth * imHeight];
		copy.getRGB(0, 0, imWidth, imHeight, pixels, 0, imWidth);

		for (int i = 0; i < pixels.length; i++) {
			int alpha = (pixels[i] >> 24) & 255;
			int red = (pixels[i] >> 16) & 255;
			int green = (pixels[i] >> 8) & 255;
			int blue = pixels[i] & 255;

			// Boost blue channel, halve green and red
			blue = Math.min(255, blue + 100);
			green = green / 2;
			red = red / 2;

			pixels[i] = (alpha << 24) | (red << 16) | (green << 8) | blue;
		}

		copy.setRGB(0, 0, imWidth, imHeight, pixels, 0, imWidth);
		return copy;
	}
}
