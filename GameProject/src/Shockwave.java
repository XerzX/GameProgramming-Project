import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

public class Shockwave {

    private int xPos;
    private int yPos;

    private int width = 40;
    private int height = 150;

    private int worldWidth;

    private boolean facingLeft;
    private boolean isActive;

    private int pulseRadius = 0;

    public Shockwave(int xPos, int yPos, int worldWidth, boolean facingLeft) {
        this.xPos = xPos;
        this.yPos = yPos - 45;
        this.facingLeft = facingLeft;
        this.worldWidth = worldWidth;
        isActive = true;
    }

    public void draw(Graphics2D g2) {
        if (!isActive)
            return;

        int directionMultiplier = facingLeft ? -1 : 1;

        g2.setColor(new Color(255, 60, 20, 180));
        g2.setStroke(new BasicStroke(8));

        for (int i = 0; i < 3; i++) {
            int offset = i * 20;
            g2.drawArc(xPos + (directionMultiplier * offset), yPos, width, height, facingLeft ? 90 : 270, 180);
        }

        g2.setStroke(new BasicStroke(1));
    }

    public void update() {
        if (!isActive)
            return;

        if (facingLeft)
            xPos -= 15;
        else
            xPos += 15;

        // We can use pulseRadius later for visual expansions, but right now it's just
        // pushing forward
        pulseRadius++;

        if (xPos < 0 || xPos > worldWidth) {
            isActive = false;
        }
    }

    public Rectangle2D.Double getBoundingRectangle() {
        return new Rectangle2D.Double(xPos, yPos, width, height);
    }

    public void checkCollision(Player player) {
        if (!isActive || player == null)
            return;

        if (getBoundingRectangle().intersects(player.getBoundingRectangle())) {
            isActive = false;
            player.applyDamage(15);
        }
    }

    public boolean isActive() {
        return isActive;
    }
}
