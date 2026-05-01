import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Random;
import javax.swing.ImageIcon;

/**
 * Spawns a HealthPotion collectible after a short random delay.
 * Once visible, it waits for the player to collide with it.
 * On pickup the player is healed and the drop deactivates itself.
 *
 * Typical usage (called each game tick):
 * healthDrop.update(player);
 * healthDrop.draw(g2);
 */
public class HealthDrop {

	/** HP restored when the player picks up the potion. */
	private static final int HEAL_AMOUNT = 25;

	/** Minimum ticks before the potion appears (~8 s at 60 fps). */
	private static final int SPAWN_TICKS_MIN = 480;

	/** Maximum ticks before the potion appears (~20 s at 60 fps). */
	private static final int SPAWN_TICKS_MAX = 1200;

	private int xPos;
	private int yPos;

	private ImageIcon sprite;

	private int spawnTimer; // counts down; potion appears when it hits 0
	private boolean visible; // true once the potion has appeared on screen
	private boolean active; // false after the player picks it up

	private int width;
	private int height;

	/**
	 * xPos world X position where the potion will appear
	 * floorY world Y ground position where the potion's bottom should rest
	 */
	public HealthDrop(int xPos, int floorY) {
		sprite = ImageManager.getInstance().loadImage("/Assets/Collectable/HealthPotion.png");

		// The user requested height scaled down to 1/5th
		width = sprite.getIconWidth() / 5;
		height = sprite.getIconHeight() / 5;

		this.xPos = xPos;
		this.yPos = floorY - height;

		Random random = new Random();
		spawnTimer = random.nextInt(SPAWN_TICKS_MIN, SPAWN_TICKS_MAX + 1);

		visible = false;
		active = true;
	}

	/**
	 * Ticks the spawn countdown and checks for player collision once visible
	 */
	public void update(Player player) {
		if (!active)
			return;

		// Count down until the potion appears
		if (!visible) {
			spawnTimer--;
			if (spawnTimer <= 0)
				visible = true;
			return;
		}

		// Check if the player walked onto the potion
		if (getBoundingRectangle().intersects(player.getBoundingRectangle())) {
			player.heal(HEAL_AMOUNT);
			active = false;
		}
	}

	public void draw(Graphics2D g2) {
		if (!active || !visible)
			return;

		g2.drawImage(sprite.getImage(), xPos, yPos,
				width, height, null);
	}

	public Rectangle2D.Double getBoundingRectangle() {
		return new Rectangle2D.Double(xPos, yPos,
				width, height);
	}

	public boolean isActive() {
		return active;
	}

	public boolean isVisible() {
		return visible;
	}
}
