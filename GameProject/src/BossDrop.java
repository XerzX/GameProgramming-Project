import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import javax.swing.ImageIcon;

/**
 * BossDrop
 *
 * A collectible item that is spawned at the location where a MiniBoss dies.
 * Each boss (1–4) drops its own unique sprite. The item appears immediately
 * and stays on the floor until the player walks over it, at which point it
 * deactivates
 */
public class BossDrop {

    private static final String[] SPRITE_PATHS = {
            "/Assets/Collectable/B1Drop-BgRemoved.png",
            "/Assets/Collectable/B2Drop.png",
            "/Assets/Collectable/B3Drop.png",
            "/Assets/Collectable/B4Drop.png"
    };

    // Rendered Size (scale down to a reasonable in-world size)
    private static final int DISPLAY_SIZE = 64;

    private final ImageIcon sprite;

    private int xPos;
    private int yPos;
    private int width;
    private int height;

    private boolean active = true;
    private final int bossIndex;  // 1-based, kept so Player can record which drop was collected

    /**
     * bossIndex - Index of the boss (1 = FirstBoss … 4 = FourthBoss)
     * dropX - World X position (typically the boss's centre X)
     * dropFloorY - World Y of the floor surface where the drop should rest
     */
    public BossDrop(int bossIndex, int dropX, int dropFloorY) {
        this.bossIndex = Math.max(1, Math.min(bossIndex, SPRITE_PATHS.length));
        sprite = ImageManager.getInstance().loadImage(SPRITE_PATHS[this.bossIndex - 1]);

        if (sprite != null) {
            // Preserve aspect ratio, fit inside DISPLAY_SIZE
            int origW = sprite.getIconWidth();
            int origH = sprite.getIconHeight();
            if (origW >= origH) {
                width = DISPLAY_SIZE;
                height = (origH * DISPLAY_SIZE) / Math.max(origW, 1);
            } else {
                height = DISPLAY_SIZE;
                width = (origW * DISPLAY_SIZE) / Math.max(origH, 1);
            }
        } else {
            width = DISPLAY_SIZE;
            height = DISPLAY_SIZE;
        }

        // Centre the drop horizontally at dropX and sit it on the floor
        this.xPos = dropX - width / 2;
        this.yPos = dropFloorY - height;
    }

    /** Called every game tick to check for player pickup. */
    public void update(Player player) {
        if (!active)
            return;

        if (getBoundingRectangle().intersects(player.getBoundingRectangle())) {
            player.addCollectedDrop(bossIndex); // register in player's inventory
            active = false;
        }
    }

    /** Returns the sprite so the HUD can render it in the collectibles tray. */
    public ImageIcon getSprite() {
        return sprite;
    }

    public int getBossIndex() {
        return bossIndex;
    }

    /** Draws the drop into the world context. */
    public void draw(Graphics2D g2) {
        if (!active || sprite == null)
            return;
        g2.drawImage(sprite.getImage(), xPos, yPos, width, height, null);
    }

    public Rectangle2D.Double getBoundingRectangle() {
        return new Rectangle2D.Double(xPos, yPos, width, height);
    }

    public boolean isActive() {
        return active;
    }
}
