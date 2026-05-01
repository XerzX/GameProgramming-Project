import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

public class PaperBall {

    private int xPos;
    private int yPos;

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
        paperBallSprite = new StripAnimationHV(true, "/Assets/Player/paperball.png", 24, 1);

        isActive = true;
    }

    public void draw(Graphics2D g2) {
        if (!isActive)
            return;

        if (facingLeft)
            g2.drawImage(paperBallSprite.getCurrentFrame(), xPos + paperBallSprite.getImageWidth(), yPos,
                    -paperBallSprite.getImageWidth(), paperBallSprite.getImageHeight(), null);
        else
            g2.drawImage(paperBallSprite.getCurrentFrame(), xPos, yPos, paperBallSprite.getImageWidth(),
                    paperBallSprite.getImageHeight(), null);
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

    public boolean collidesWithEnemy() {
        Rectangle2D.Double paperBallRect = new Rectangle2D.Double(xPos, yPos, paperBallSprite.getImageWidth(),
                paperBallSprite.getImageHeight());

        // Fix Check For Enemy HitBox Collision w/ PaperBall Here When Enemies Are Added
        return paperBallRect.intersects(null);
    }

    public void checkCollision() {
        if (collidesWithEnemy()) {
            stopAnimation();
            isActive = false;
        }

        // (Optional) Add Check For SolidObject Collision w/ PaperBall
    }
}
