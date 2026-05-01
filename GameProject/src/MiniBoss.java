import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class MiniBoss implements MiniBossBehaviour {

    protected int xPos, yPos;
    protected int width, height;
    protected int hp;
    protected int maxHP;   // set once at construction, never changes
    protected int dx;
    protected boolean facingLeft = false;
    protected boolean dead = false;

    private int damageTimer = 0;
    private BufferedImage tintedSprite = null;
    private static final int DAMAGE_TINT_TICKS = 20;

    protected int specialWindupTimer = 0;
    protected int goldGlowTimer = 0;
    protected BufferedImage specialTintedSprite = null;
    protected Player specialTarget = null;

    protected Animation walkAnimation;
    protected Animation meleeAnimation;
    protected Animation projectileAnimation;
    protected Animation currentAnimation;

    protected BufferedImage[] currentFrames;
    protected int currentFrame = 0;
    protected int frameTimer = 0;
    protected int frameDelay = 6;

    protected int attackRange = 200;
    protected int startBoundary = 300;
    protected boolean fightStarted = false;

    protected int specialCooldown    = 0;
    protected int specialCooldownMax = 300;

    // 1-based index (1 = FirstBoss … 4 = FourthBoss) used to pick the drop sprite
    private int bossIndex;

    public MiniBoss(int x, int y, int width, int height, int hp, int speed, int bossIndex) {
        xPos = x;
        yPos = y;
        this.width = width;
        this.height = height;
        this.hp    = hp;
        this.maxHP = hp;   // snapshot full health
        dx = speed;
        this.attackRange = attackRange;
        this.bossIndex = bossIndex;
    }

    @Override
    public void meleeAttack() {
    }

    @Override
    public void projectileAttack() {
    }

    @Override
    public void specialAttack(Player player) {}

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

        // chasing algorithm
        if (dist > attackRange) {
            if (facingLeft) {
                xPos -= dx;

            } else {
                xPos += dx;
            }

            currentAnimation = walkAnimation;
        }

        if (xPos < 0)
            xPos = 0;
        if (xPos + width > 1584)
            xPos = 1584 - width;
    }

    public void takeDamage(int amount) {
        hp -= amount;
        if (hp <= 0) {
            hp = 0;
            if (!dead) {
                dead = true;
                // Spawn the boss-specific drop at the boss's feet
                bossDrop = new BossDrop(bossIndex, xPos + width / 2, yPos + height);
            }
        }

        if (!dead && currentAnimation != null) {
            Image currentImage = currentAnimation.getImage();
            if (currentImage != null) {
                BufferedImage sprite = ImageManager.getInstance().toBufferedImage(currentImage);
                tintedSprite = RedTintFX.getInstance().apply(sprite);
                damageTimer = DAMAGE_TINT_TICKS;
            }
        }
    }

    private HealthDrop healthDrop = null;
    private BossDrop   bossDrop   = null;

    public void triggerSpecial(Player player) {
        if (specialWindupTimer <= 0) {
            specialTarget = player;
            specialWindupTimer = 60; // 1 second windup phase before execute
            goldGlowTimer = 180; // Glow during windup and 2s of post-attack leeway
        }
    }

    public void update(Player player) {
        if (healthDrop != null) {
            healthDrop.update(player);
        }
        if (bossDrop != null) {
            bossDrop.update(player);
        }

        if (dead) {
            return;
        }

        if (specialCooldown > 0) {
            specialCooldown--;
        }

        if (specialWindupTimer > 0) {
            specialWindupTimer--;
            if (specialWindupTimer <= 0 && specialTarget != null) {
                specialAttack(specialTarget);
                specialTarget = null;
            }
        }

        if (currentAnimation != null)
            currentAnimation.update();

        if (damageTimer > 0) {
            damageTimer--;
            if (damageTimer <= 0) {
                tintedSprite = null;
            }
        }

        if (goldGlowTimer > 0) {
            goldGlowTimer--;
            if (goldGlowTimer > 0 && currentAnimation != null) {
                Image currentImage = currentAnimation.getImage();
                if (currentImage != null) {
                    BufferedImage sprite = ImageManager.getInstance().toBufferedImage(currentImage);
                    specialTintedSprite = GoldTintFX.getInstance().apply(sprite);
                }
            } else {
                specialTintedSprite = null;
            }
        }
    }

    public void draw(Graphics2D g2) {
        if (healthDrop != null) {
            healthDrop.draw(g2);
        }
        if (bossDrop != null) {
            bossDrop.draw(g2);
        }

        if (dead) {
            return;
        }
        if (currentAnimation == null) {
            return;
        }

        Image frame = currentAnimation.getImage();
        if (frame == null)
            return;

        Image drawFrame = frame;
        if (damageTimer > 0 && tintedSprite != null) {
            drawFrame = tintedSprite;
        } else if (goldGlowTimer > 0 && specialTintedSprite != null) {
            drawFrame = specialTintedSprite;
        }

        if (facingLeft)
            g2.drawImage(drawFrame, xPos, yPos, width, height, null);
        else
            g2.drawImage(drawFrame, xPos + width, yPos, -width, height, null); // No need for other images, just flip image
                                                                               // using - sign
    }

    // Load strip animation
    protected Animation loadStripAnimation(String path, int frameCount, boolean loop) {
        Animation anim = new Animation(loop);

        BufferedImage sheet = ImageManager.getInstance().loadBufferedImage(path);

        if (sheet == null) {
            System.out.println("Could not load strip: " + path);
            return anim;
        }

        int frameWidth = sheet.getWidth() / frameCount;
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

    public void startFight(int worldWidth) {
        if (!fightStarted) {
            // Pick a completely random X-position ranging across the width of the given floor bounds
            int randomX = (int) (Math.random() * (worldWidth - 50)); 
            
            // The floor rests exactly at the feet (yPos + height) of the natively spawned boss
            int floorY = yPos + height;
            
            healthDrop = new HealthDrop(randomX, floorY);
        }
        fightStarted = true;
    }

    public boolean isStarted() {
        return fightStarted;
    }

    /** Override in subclasses to return a display name for the HUD. */
    public String getName() {
        return "Mini Boss";
    }

    /** The HP value this boss started with (never changes). */
    public int getMaxHP() {
        return maxHP;
    }

    public Rectangle2D.Double getTriggerZone() {
        return new Rectangle2D.Double(xPos - startBoundary, yPos, width + (startBoundary * 2), height);
    }

}
