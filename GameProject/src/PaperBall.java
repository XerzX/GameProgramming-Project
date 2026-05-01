import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

public class PaperBall {

    private int xPos;
    private int yPos;
    
    private int width = 50;
    private int height = 50;

    private int worldWidth;

    private boolean facingLeft;
    private boolean isActive;

    private StripAnimationHV paperBallSprite;

    public PaperBall(int xPos, int yPos, int worldWidth, boolean facingLeft) {
        this.xPos = xPos;
        this.yPos = yPos;

        this.facingLeft = facingLeft;

        this.worldWidth = worldWidth;

        // Load PaperBall Rotating Animation
        paperBallSprite = new StripAnimationHV(true, "/Assets/Projectiles/paperball.png", 24, 1);

        isActive = true;
    }

    public void draw(Graphics2D g2) {
        if (!isActive)
            return;

        if (facingLeft)
            g2.drawImage(paperBallSprite.getCurrentFrame(), xPos + width, yPos,
                    -width, height, null);
        else
            g2.drawImage(paperBallSprite.getCurrentFrame(), xPos, yPos, width,
                    height, null);
    }

    public void startAnimation() {
        paperBallSprite.start();
    }

    public void stopAnimation() {
        paperBallSprite.stop();
    }

    public void update() {
        if (!isActive)
            return;

        if (facingLeft)
            xPos -= 10;
        else
            xPos += 10;

        paperBallSprite.update();

        if (xPos < 0 || xPos > worldWidth) {
            stopAnimation();
            isActive = false;
        }
    }

    public MiniBoss collidesWithEnemy() {
        Rectangle2D.Double paperBallRect = new Rectangle2D.Double(xPos, yPos, width, height);

        // Check For Enemy HitBox Collision w/ PaperBall
        for (MiniBoss boss : MiniBossManager.getInstance().getBosses()) {
            if (!boss.isDead() && paperBallRect.intersects(boss.getBoundingRectangle())) {
                return boss;
            }
        }
        return null;
    }

    public void checkCollision() {
        MiniBoss hitBoss = collidesWithEnemy();
        if (hitBoss != null) {
            stopAnimation();
            isActive = false;
            hitBoss.takeDamage(15);
        }
    }

    public boolean isActive() {
        return isActive;
    }
}
