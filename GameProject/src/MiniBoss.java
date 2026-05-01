import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class MiniBoss implements MiniBossBehaviour {

    protected int xPos, yPos;
    protected int width, height;
    protected int hp;
    protected int dx;
    protected boolean facingLeft = false;
    protected boolean dead       = false;


     protected Animation walkAnimation;
    protected Animation meleeAnimation;
    protected Animation projectileAnimation;
    protected Animation currentAnimation;


    protected BufferedImage[] currentFrames;
    protected int currentFrame = 0;
    protected int frameTimer   = 0;
    protected int frameDelay   = 6;

    protected int attackRange = 200; 
    protected int startBoundary = 300;
    protected boolean fightStarted = false;






 public MiniBoss(int x, int y, int width, int height, int hp, int speed) {
        xPos        = x;
        yPos        = y;
        this.width  = width;
        this.height = height;
        this.hp     = hp;
        dx          = speed;
        this.attackRange = attackRange;
    }

    

    @Override
    public void meleeAttack() {}

    @Override
    public void projectileAttack() {}

    @Override
    public void specialAttack() {}


     public void chasePlayer(int playerX, int playerY) {

         double dist = Math.abs(playerX - xPos);

    if (!fightStarted) {
        currentAnimation = walkAnimation; // stand idle .
        return;
    }





    // Have MB Face toward player
    if (playerX < xPos)
        facingLeft = true;
    else
        facingLeft = false;

    // Move toward player, stop when close enough to attack
   

    //chasing algorithm
    if (dist > attackRange) {
        if (facingLeft){
            xPos -= dx;

        } 
        else{
            xPos += dx;
        }            

      currentAnimation = walkAnimation;
    }

    if (xPos < 0) xPos = 0;
    if (xPos + width > 1584) xPos = 1584 - width; 
    }




 public void takeDamage(int amount) {
        hp -= amount;
        if (hp <= 0)
            dead = true;
        //then drop key 
    }

    public void update() {
        if (dead){
             return;
            }

      if (currentAnimation != null)
            currentAnimation.update();
        
    }

    public void draw(Graphics2D g2) {
        if (dead){
            return;
        } 
        if (currentAnimation == null){
             return;
        }


        Image frame = currentAnimation.getImage();
        if (frame == null) return;

        if (facingLeft)
            g2.drawImage(frame, xPos, yPos, width, height, null);
        else
            g2.drawImage(frame, xPos + width, yPos, -width, height, null); //No need for other images, just flip image using - sign
    }




    //Load strip animation 
  protected Animation loadStripAnimation(String path, int frameCount, boolean loop) {
    Animation anim = new Animation(loop);

    BufferedImage sheet = ImageManager.getInstance().loadBufferedImage(path);

    if (sheet == null) {
        System.out.println("Could not load strip: " + path);
        return anim; 
    }

    int frameWidth  = sheet.getWidth() / frameCount;
    int frameHeight = sheet.getHeight();

    for (int i = 0; i < frameCount; i++) {
        BufferedImage frameImage = new BufferedImage(frameWidth, frameHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = frameImage.createGraphics();
        g.drawImage(sheet,
                0, 0, frameWidth, frameHeight,
                i * frameWidth, 0, (i + 1) * frameWidth, frameHeight,
                null);
        g.dispose();
        anim.addFrame(frameImage, 100);
    }

    anim.start();
    return anim;
}


    public boolean isDead() {
        return dead;
    }

    public Rectangle2D.Double getBoundingRectangle() {
        return new Rectangle2D.Double(xPos, yPos, width, height);
    }

    public int getAttackRange() {
    return attackRange;
}


public void startFight() {
    fightStarted = true;
}

public boolean isStarted() {
    return fightStarted;
}

public Rectangle2D.Double getTriggerZone() {
    return new Rectangle2D.Double(xPos - startBoundary, yPos, width + (startBoundary * 2), height);
}




}
