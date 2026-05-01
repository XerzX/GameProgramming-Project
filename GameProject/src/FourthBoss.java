import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

public class FourthBoss extends MiniBoss {

    // The single wall that grows from x=1100 toward x=0
    private int wallX      = 1100;
    private int wallY      = 0;
    private int wallWidth  = 0;
    private int wallHeight = 0;

    private boolean wallActive = false;

    // How many pixels the wall grows per special attack trigger
    private static final int GROW_AMOUNT = 100;
    private static final int WORLD_WIDTH = 1584;

    public FourthBoss(int x, int y) {
        super(x, y, 160, 240, 120, 2);
        specialCooldownMax = 360;
        walkAnimation = loadStripAnimation("/Assets/MiniBoss/FourthBossWalk.png", 8, true);
        currentAnimation = walkAnimation;
    }

    @Override
    public void meleeAttack() {
        if (meleeAnimation != null) currentAnimation = meleeAnimation;
    }

     public String getName() { return "Shadow Tyrant"; }

     
    @Override
    public void projectileAttack() {
        if (projectileAnimation != null) currentAnimation = projectileAnimation;
    }

    @Override
    public void specialAttack(Player player) {
        if (!wallActive) {
            // First trigger — start the wall at x=1100, anchored to boss floor
            wallY      = yPos;
            wallHeight = height;
            wallWidth  = GROW_AMOUNT;
            wallX      = 1100 - wallWidth;
            wallActive = true;
        } else {
            // Grow the wall to the left by GROW_AMOUNT
            wallWidth += GROW_AMOUNT;
            wallX      = 1100 - wallWidth;

            // Clamp so wall never goes past x=0
            if (wallX < 0) {
                wallX     = 0;
                wallWidth = 1100;
            }
        }

        System.out.println("[FourthBoss] Wall x=" + wallX + " width=" + wallWidth + " y=" + wallY);
    }

    // Resolve collision between the wall and any entity (player or boss)
    // Pass in the entity's x, y, width, height — returns corrected x, y in posXY
    public void resolveWallCollision(int[] posXY, int entityWidth, int entityHeight) {
        if (!wallActive) return;

        int ex = posXY[0];
        int ey = posXY[1];

        Rectangle2D.Double wall       = new Rectangle2D.Double(wallX, wallY, wallWidth, wallHeight);
        Rectangle2D.Double entityRect = new Rectangle2D.Double(ex, ey, entityWidth, entityHeight);

        if (!entityRect.intersects(wall)) return;

        // Push out from left side of wall
        if (ex + entityWidth > wallX && ex < wallX) {
            ex = wallX - entityWidth;
        }
        // Push out from right side of wall
        else if (ex < wallX + wallWidth && ex + entityWidth > wallX + wallWidth) {
            ex = (int)(wallX + wallWidth);
        }
        // Push out from top of wall
        else if (ey + entityHeight > wallY && ey < wallY) {
            ey = wallY - entityHeight;
        }
        // Push out from bottom of wall
        else if (ey < wallY + wallHeight && ey + entityHeight > wallY + wallHeight) {
            ey = (int)(wallY + wallHeight);
        }

        posXY[0] = ex;
        posXY[1] = ey;
    }

    @Override
    public void draw(Graphics2D g2) {
        // Draw the growing wall
        if (wallActive) {
            g2.setColor(Color.DARK_GRAY);
            g2.fillRect(wallX, wallY, wallWidth, wallHeight);

            // Red outline so it's clearly visible
            g2.setColor(Color.RED);
            g2.drawRect(wallX, wallY, wallWidth, wallHeight);
        }

        super.draw(g2);
    }

    public boolean isWallActive() {
        return wallActive;
    }

    public Rectangle2D.Double getWall() {
        if (!wallActive) return null;
        return new Rectangle2D.Double(wallX, wallY, wallWidth, wallHeight);
    }

    @Override
    public void update(Player player) {
        super.update(player);

        // Block boss from walking through the wall
        if (wallActive) {
            int[] pos = { xPos, yPos };
            resolveWallCollision(pos, width, height);
            xPos = pos[0];
            yPos = pos[1];
        }

        // Clear wall when boss dies
        if (dead) {
            wallActive = false;
        }
    }
}