import java.awt.image.BufferedImage;

/**
 * Singleton image effect that applies a gold tint to any BufferedImage.
 *
 * Usage:
 * BufferedImage tinted = GoldTintFX.getInstance().apply(originalImage);
 */
public class GoldTintFX {

	private static GoldTintFX instance;

	private GoldTintFX() {

	}

	public static GoldTintFX getInstance() {
		if (instance == null) {
			instance = new GoldTintFX();
		}
		return instance;
	}

	public BufferedImage apply(BufferedImage src) {
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

			// Subtle boost to red and green to create a soft gold tone, slightly drop blue
			red = Math.min(255, red + 50);
			green = Math.min(255, green + 40);
			blue = (int)(blue / 1.5);

			pixels[i] = (alpha << 24) | (red << 16) | (green << 8) | blue;
		}

		copy.setRGB(0, 0, imWidth, imHeight, pixels, 0, imWidth);
		return copy;
	}
}
