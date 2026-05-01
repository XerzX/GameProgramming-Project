import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class FinalBoss {

    protected int xPos, yPos;
    protected int width, height;

    protected int hp;
    protected int maxHP;

    protected int dx;

    protected boolean facingLeft = false;
    protected boolean dead = false;

    private int damageTimer = 0;
    private BufferedImage tintedSprite = null;
    private static final int DAMAGE_TINT_TICKS = 20;

    protected Animation walkAnimation;
    protected Animation meleeAnimation;
    protected Animation projectileAnimation;
    protected Animation specialAnimation;
    protected Animation currentAnimation;

    protected int attackRange = 100;
    protected int startBoundary = 400;
    protected boolean fightStarted = false;

    private static FinalBoss instance;

    public static FinalBoss getInstance() {
        return instance;
    }

    public FinalBoss(int x, int y) {
        instance = this;
        this.xPos = x;
        this.yPos = y;

        this.width = 160;
        this.height = 240;

        this.hp = 500;
        this.maxHP = hp;

        this.dx = 4;

        // Load Sprite Sheet / Grid File
        walkAnimation = loadGridAnimation("/Assets/FinalBoss/Strip.png", 2, 6, true);

        // Load Static Attack Frames
        meleeAnimation = loadStaticAnimation("/Assets/FinalBoss/Melee.png");
        projectileAnimation = loadStaticAnimation("/Assets/FinalBoss/Ranged.png");

        currentAnimation = walkAnimation;
    }

    private int attackCooldownTimer = 0;
    private int attackDurationTimer = 0;
    private java.util.ArrayList<Shockwave> shockwaves = new java.util.ArrayList<>();

    public void meleeAttack() {
        if (meleeAnimation != null)
            currentAnimation = meleeAnimation;
        attackDurationTimer = 30; // Time locked in the melee pose
        attackCooldownTimer = 100; // Time before next attack
    }

    public void projectileAttack() {
        if (projectileAnimation != null)
            currentAnimation = projectileAnimation;
        attackDurationTimer = 40; // Time locked in the ranged pose
        attackCooldownTimer = 120; // Time before next attack

        // Spawn a new shockwave slightly in front of the boss
        int spawnX = facingLeft ? xPos - 20 : xPos + width + 20;
        int spawnY = yPos + (height / 2);
        shockwaves.add(new Shockwave(spawnX, spawnY, 2560, facingLeft));
    }

    public void specialAttack() {
        if (specialAnimation != null)
            currentAnimation = specialAnimation;
    }

    public void chasePlayer(int playerX, int playerY) {
        double dist = Math.abs(playerX - xPos);

        if (!fightStarted || dead) {
            currentAnimation = walkAnimation;
            return;
        }

        // If the boss is currently engaged in an active attack animation, freeze
        // movement
        if (attackDurationTimer > 0) {
            return;
        }

        // Face Player
        facingLeft = playerX < xPos;

        // If cooldown has finished, decide on an attack
        if (attackCooldownTimer <= 0) {
            if (dist < 180) { // Close range threshold
                meleeAttack();
                return;
            } else if (dist > 350 && dist < 1200) { // Long range threshold
                projectileAttack();
                return;
            }
        }

        // Chasing Algorithm
        if (dist > attackRange && attackDurationTimer <= 0) {
            if (facingLeft) {
                xPos -= dx;
            } else {
                xPos += dx;
            }
            currentAnimation = walkAnimation;
        }

        // Bounds Clamping
        if (xPos < 0)
            xPos = 0;
        if (xPos + width > 2560)
            xPos = 2560 - width;
    }

    public void takeDamage(int amount) {
        hp -= amount;
        if (hp <= 0) {
            hp = 0;
            if (!dead) {
                dead = true;

                // Add Final Boss Death Logic Here
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

    public void update(Player player) {
        if (dead)
            return;

        // Timer decrement loops
        if (attackCooldownTimer > 0)
            attackCooldownTimer--;

        if (attackDurationTimer > 0) {
            attackDurationTimer--;

            // If the duration finishes inside this tick, revert to walking state
            if (attackDurationTimer <= 0) {
                currentAnimation = walkAnimation;
            }

            // If performing a Melee Attack, pulse a massive hitbox continuously while
            // attack lasts
            if (currentAnimation == meleeAnimation) {
                Rectangle2D.Double meleeHitBox;
                if (facingLeft) {
                    meleeHitBox = new Rectangle2D.Double(xPos - 80, yPos, 80 + width, height);
                } else {
                    meleeHitBox = new Rectangle2D.Double(xPos, yPos, 80 + width, height);
                }

                if (meleeHitBox.intersects(player.getBoundingRectangle())) {
                    player.applyDamage(10);
                }
            }
        }

        // Update Projectiles (Shockwaves)
        for (int i = 0; i < shockwaves.size(); i++) {
            Shockwave s = shockwaves.get(i);
            s.update();
            s.checkCollision(player);
            if (!s.isActive()) {
                shockwaves.remove(i);
                i--;
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
    }

    public void draw(Graphics2D g2) {
        if (dead || currentAnimation == null)
            return;

        Image frame = currentAnimation.getImage();
        if (frame == null)
            return;

        Image drawFrame = (damageTimer > 0 && tintedSprite != null) ? tintedSprite : frame;

        int rWidth = width;
        int rHeight = height;
        int rX = xPos;
        int rY = yPos;

        // The Ranged Sprite is Natively 558 x 447.
        // Scale It naturally To ~300 Width To Maintain Aspect Ratio
        if (currentAnimation == projectileAnimation) {
            rWidth = 300;
            if (facingLeft) {
                // If facing left, lock the back edge to xPos + 160 and thrust forwards
                rX = xPos - (rWidth - width);
            }
        }

        if (facingLeft)
            g2.drawImage(drawFrame, rX + rWidth, rY, -rWidth, rHeight, null);
        else
            g2.drawImage(drawFrame, rX, rY, rWidth, rHeight, null);

        // Render Active Shockwaves
        for (Shockwave s : shockwaves) {
            s.draw(g2);
        }
    }

    // Load Static Animation helper for discrete singular frames
    protected Animation loadStaticAnimation(String path) {
        Animation anim = new Animation(false);
        BufferedImage frameImage = ImageManager.getInstance().loadBufferedImage(path);

        if (frameImage != null) {
            anim.addFrame(frameImage, 500);
        } else {
            System.out.println("Could not load static file: " + path);
        }

        anim.start();
        return anim;
    }

    // Load Grid Animation
    protected Animation loadGridAnimation(String path, int rows, int columns, boolean loop) {
        Animation anim = new Animation(loop);
        BufferedImage sheet = ImageManager.getInstance().loadBufferedImage(path);

        if (sheet == null) {
            System.out.println("Could not load grid: " + path);
            return anim;
        }

        int frameWidth = sheet.getWidth() / columns;
        int frameHeight = sheet.getHeight() / rows;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                BufferedImage frameImage = new BufferedImage(frameWidth, frameHeight, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = frameImage.createGraphics();
                g.drawImage(sheet,
                        0, 0, frameWidth, frameHeight,
                        j * frameWidth, i * frameHeight, (j + 1) * frameWidth, (i + 1) * frameHeight,
                        null);
                g.dispose();
                anim.addFrame(frameImage, 100);
            }
        }

        anim.start();
        return anim;
    }

    public void startFight(int worldWidth) {
        fightStarted = true;
    }

    public boolean isStarted() {
        return fightStarted;
    }

    public boolean isDead() {
        return dead;
    }

    public String getName() {
        return "The Dean";
    }

    public int getHP() {
        return hp;
    }

    public int getMaxHP() {
        return maxHP;
    }

    public Rectangle2D.Double getBoundingRectangle() {
        return new Rectangle2D.Double(xPos, yPos, width, height);
    }

    public Rectangle2D.Double getTriggerZone() {
        return new Rectangle2D.Double(xPos - startBoundary, yPos, width + (startBoundary * 2), height);
    }
}
