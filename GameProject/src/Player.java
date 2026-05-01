import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.JFrame;

public class Player {

	private enum PlayerState {
		DEFAULT, // Normal idle - TestPlayer.png
		MELEE, // After melee - MeleeIdle.png (while attack plays: Melee.png)
		RANGED // After ranged - RangedIdle.png (while attack plays: Ranged.png)
	}

	private PlayerState state = PlayerState.DEFAULT;

	// Reference To Window Player Will Drawn On
	private JFrame gameWindow;
	private SolidObjectManager soManager;

	private ImageManager imageManager;

	// Walk Animation
	private StripAnimationHV walkAnimation;

	// Idle Sprites
	private BufferedImage defaultIdleSprite;
	private BufferedImage meleeIdleSprite;
	private BufferedImage rangedIdleSprite;

	// Attack Sprites (static single-frame images)
	private BufferedImage meleeAttackSprite;
	private BufferedImage rangedAttackSprite;

	// Counts down game ticks for how long the attack pose is displayed
	// When it reaches 0, isAttacking clears.
	private int attackTimer = 0;
	private static final int MELEE_ATTACK_DISPLAY_TICKS = 30;
	private static final int RANGED_ATTACK_DISPLAY_TICKS = 15;

	// Counts down game ticks for how long the damage tint is shown
	private int damageTimer = 0;
	private static final int DAMAGE_TINT_TICKS = 20;

	// Pre-computed red-tinted copy of the sprite active when damage was taken
	private BufferedImage tintedSprite = null;

	// Current Sprite Displayed On Screen
	private Image currSprite;

	// Player's World Coordinates
	private int xPos;
	private int yPos;

	private int dx;
	private int dy;

	private int width;
	private int height;

	private int worldWidth;
	private int worldHeight;

	private boolean facingLeft = false;
	private boolean jumping = false;
	private boolean inAir = false;

	private int timeElapsed = 0;
	private int startY;
	private int initialVelocity = 0;

	
private int freezeTimer = 0;
private static final int FREEZE_DURATION = 120; // 2 seconds at 60fps




	private ElevatorManager elevatorManager;
	private boolean nearElevator = false;

	private boolean isIdle = true;
	private boolean isAttacking = false;

	private int currentLevel = 1;
private boolean[] collectedDrops = new boolean[4];

	// This Represents The Player's Health
	private int HP = 100;

	private ArrayList<PaperBall> projectiles = new ArrayList<>();

	private boolean spawnProjectile = false;

	public Player(JFrame gameWindow, SolidObjectManager soManager, int worldWidth, int worldHeight) {

		this.gameWindow = gameWindow;
		this.soManager = soManager;
		this.worldWidth = worldWidth;
		this.worldHeight = worldHeight;

		imageManager = ImageManager.getInstance();

		// Set Player's World Coordinates
		xPos = 0;
		yPos = 0;

		dx = 10;
		dy = 10;

		width = 408 / 3;
		height = 612 / 3;

		// Load Idle Animations
		defaultIdleSprite = imageManager.loadBufferedImage("/Assets/Player/TestPlayer.png");
		meleeIdleSprite = imageManager.loadBufferedImage("/Assets/Player/MeleeIdle.png");
		rangedIdleSprite = imageManager.loadBufferedImage("/Assets/Player/RangedIdle.png");

		// Load Walk Animation
		walkAnimation = new StripAnimationHV(true, "/Assets/Player/Strip.png", 2, 4);

		// Load Attack Sprites (single static images, displayed for a time equal to
		// ATTACK_DISPLAY_TICKS)
		meleeAttackSprite = imageManager.loadBufferedImage("/Assets/Player/Melee.png");
		rangedAttackSprite = imageManager.loadBufferedImage("/Assets/Player/Ranged.png");

		// PRELOAD HEAVY ASSETS:
		// Proactively instantiate a dummy paperball strip animation. This forces the
		// JVM to unpack
		// the heavy 16.8MB image file into memory during the boot sequence instead of
		// during gameplay!
		new StripAnimationHV(true, "/Assets/Projectiles/paperball.png", 24, 1);

		elevatorManager = ElevatorManager.getInstance();
	}

	public void draw(Graphics2D g2) {

		// Draw logic for projectiles
		for (int i = 0; i < projectiles.size(); i++) {
			projectiles.get(i).draw(g2);
		}

		// Attack Frame
		if (isAttacking) {
			if (state == PlayerState.MELEE) {
				drawImage(g2, meleeAttackSprite);
			} else if (state == PlayerState.RANGED) {
				drawImage(g2, rangedAttackSprite);
			}
			return;
		}

		// Move Frame
		if (!isIdle) {
			drawImage(g2, walkAnimation.getCurrentFrame());
			return;
		}

		// Idle Frame
		switch (state) {
			case MELEE -> drawImage(g2, meleeIdleSprite);
			case RANGED -> drawImage(g2, rangedIdleSprite);
			default -> drawImage(g2, defaultIdleSprite);
		}
	}

	// Handling Drawing Player Sprite and Mirror It To Face Opposite Direction
	private void drawImage(Graphics2D g2, Image img) {
		currSprite = img;

		// Substitute the tinted copy while the damage flash is active
		Image drawImg = (damageTimer > 0 && tintedSprite != null) ? tintedSprite : img;

		if (facingLeft) {
			// Mirror horizontally: draw from right edge leftward with negative width
			g2.drawImage(drawImg, xPos + width, yPos, -width, height, null);
		} else {
			// Normal draw: sprite naturally faces right
			g2.drawImage(drawImg, xPos, yPos, width, height, null);
		}
	}

	public void move(int direction) {
		 if (freezeTimer > 0) {
			return;}

		// Restart the walk animation if it stopped (key was released previously)
		if (!walkAnimation.isAnimationActive()) {
			walkAnimation.start();
		}

		// Move Left
		if (direction == 1) {
			xPos -= dx;
			facingLeft = true;
			if (xPos <= 0)
				xPos = 0;
		}

		// Move Right
		if (direction == 2) {
			xPos += dx;
			facingLeft = false;
			if (xPos + width > worldWidth)
				xPos = worldWidth - width;
		}

		// Jump
		if (direction == 3) {
			startJump();
		}
	}

	public void attackMelee() {
		if (isAttacking)
			return;
		state = PlayerState.MELEE;
		isAttacking = true;

		Rectangle2D.Double meleeHitBox = null;

		int attackDamage = 10;
		int attackRange = 100;
		int attackHeight = 100;

		if (facingLeft)
			meleeHitBox = new Rectangle2D.Double(xPos - attackRange, yPos + 100, attackRange, attackHeight);
		else
			meleeHitBox = new Rectangle2D.Double(xPos + width, yPos + 100, attackRange, attackHeight);

		// Check For AttackHitBox Collision w/ Bosses
		for (MiniBoss boss : MiniBossManager.getInstance().getBosses()) {
			if (!boss.isDead() && meleeHitBox.intersects(boss.getBoundingRectangle())) {
				boss.takeDamage(attackDamage);
			}
		}

		attackTimer = MELEE_ATTACK_DISPLAY_TICKS; // start countdown
	}

	public void attackRanged() {
		if (isAttacking)
			return;
		state = PlayerState.RANGED;
		isAttacking = true;

		spawnProjectile = true;

		attackTimer = RANGED_ATTACK_DISPLAY_TICKS; // start countdown
	}

	public void startAnimation() {
		walkAnimation.start();
	}

	public void stopAnimation() {
		walkAnimation.stop();
	}

	public void updateAnimation() {
		if (!isIdle && walkAnimation.isAnimationActive()) {
			walkAnimation.update();
		}

		if (isAttacking) {
			attackTimer--;
			if (attackTimer <= 0) {
				isAttacking = false;
				attackTimer = 0;
			}
		}

		if (damageTimer > 0) {
			damageTimer--;
			if (damageTimer <= 0) {
				tintedSprite = null;
			}
		}
	}

	public void startFall() {
		jumping = false;
		inAir = true;
		timeElapsed = 0;
		startY = yPos;
		initialVelocity = 0; // Start Falling With 0 Upward Momentum
	}

	public void startJump() {
		if (!jumping && !inAir) {
			jumping = true;
			timeElapsed = 0;
			startY = yPos;
			initialVelocity = 70; // Adjust For Jump Height
		}
	}

	private void checkFalling() {
		if (!jumping && !inAir) {
			// Create A Small Rectangle Below The Player's Feet
			Rectangle2D.Double feet = new Rectangle2D.Double(xPos, yPos + (height + 1), width, 2);
			if (soManager.collidesWith(feet) == null) {
				startFall();
			}
		}
	}

	public void update() {
		 if (freezeTimer > 0) {
        freezeTimer--;
        return; // skip everything while frozen
    }
		if (spawnProjectile) {
			int projX = facingLeft ? xPos - 50 : xPos + width;
			PaperBall paperBall = new PaperBall(projX, yPos + 50, worldWidth, facingLeft);
			paperBall.startAnimation();
			projectiles.add(paperBall);
			spawnProjectile = false;
		}

		// Apply Gravity/Jumping Physics
		if (jumping || inAir) {
			timeElapsed++;
			int distance = (int) (initialVelocity * timeElapsed - 4.9 * timeElapsed * timeElapsed);
			yPos = startY - distance;
		}

		// ALWAYS Resolve Collisions After Moving
		resolveCollisions();

		// HARD LIMIT: Prevent falling below the bottom of Floor 1
		int absoluteBottom = worldHeight - height;
		if (yPos > absoluteBottom) {
			yPos = absoluteBottom;
			jumping = false;
			inAir = false;
			timeElapsed = 0;
		}

		// Check If Player Is In Mid-Air
		checkFalling();

		nearElevator = elevatorManager.isNearElevator(getBoundingRectangle());

		// Manage Projectiles Update Loop
		for (int i = projectiles.size() - 1; i >= 0; i--) {
			PaperBall p = projectiles.get(i);
			p.update();
			p.checkCollision();
			if (!p.isActive()) {
				projectiles.remove(i);
			}
		}
	}

	public Rectangle2D.Double getBoundingRectangle() {
		return new Rectangle2D.Double(xPos, yPos, width, height);
	}

	public void resolveCollisions() {
		SolidObject hit = soManager.collidesWith(getBoundingRectangle());

		if (hit != null) {
			Rectangle2D.Double playerRect = getBoundingRectangle();
			Rectangle2D.Double objectRect = hit.getBoundingRectangle();

			// Calculate Overlap On All Sides
			double overlapLeft = playerRect.x + playerRect.width - objectRect.x;
			double overlapRight = objectRect.x + objectRect.width - playerRect.x;
			double overlapTop = playerRect.y + playerRect.height - objectRect.y;
			double overlapBottom = objectRect.y + objectRect.height - playerRect.y;

			// Find The Smallest Overlap — That's The Direction The Collision Occurred
			double minOverlap = Math.min(Math.min(overlapLeft, overlapRight),
					Math.min(overlapTop, overlapBottom));

			if (minOverlap == overlapTop) {
				// Fell OnTop Of Object (Landed)
				yPos = (int) objectRect.y - height;
				jumping = false;
				inAir = false;
			} else if (minOverlap == overlapBottom) {
				// Hit Bottom Of Object (Bumped Head)
				yPos = (int) (objectRect.y + objectRect.height);
				startFall();
			} else if (minOverlap == overlapLeft) {
				// Hit Left Side Of Object
				xPos = (int) objectRect.x - width;
			} else {
				// Hit Right Side Of Object
				xPos = (int) (objectRect.x + objectRect.width);
			}
		}


		for (MiniBoss boss : MiniBossManager.getInstance().getBosses()) {
        if (!(boss instanceof FourthBoss)) continue;
        FourthBoss fb = (FourthBoss) boss;

        int[] pos = { xPos, yPos };
        fb.resolveWallCollision(pos, width, height);
        xPos = pos[0];
        yPos = pos[1];
    }
}


	

	public void applyDamage(int damage) {
		HP -= damage;
		if (HP <= 0) {
			HP = 0;
		}

		// Generate Tinted Copy of Currently Displaying Sprite
		BufferedImage playerSprite = ImageManager.getInstance().toBufferedImage(currSprite);
		tintedSprite = RedTintFX.getInstance().apply(playerSprite);
		damageTimer = DAMAGE_TINT_TICKS; // start the flash countdown
	}

	public void heal(int amount) {
		HP += amount;
		if (HP > 100)
			HP = 100;
	}

	public int getHP() {
		return HP;
	}
public void addCollectedDrop(int bossIndex) {
		if (bossIndex >= 1 && bossIndex <= 4) {
			collectedDrops[bossIndex - 1] = true;
		}
	}

	/**
	 * Returns a copy of the collected-drop flags so the HUD can read them safely.
	 */
	public boolean[] getCollectedDrops() {
		return collectedDrops.clone();
	}

	public int getXPos() {
		return xPos;
	}

	public void setXPos(int xPos) {
		this.xPos = xPos;
	}

	public int getYPos() {
		return yPos;
	}

	public void setYPos(int yPos) {
		this.yPos = yPos;
		this.startY = yPos;
	}

	public void setWorldDimensions(int width, int height) {
		this.worldWidth = width;
		this.worldHeight = height;
	}

	public int getLevel() {
		return currentLevel;
	}

	public void setLevel(int level) {
		this.currentLevel = level;
	}


	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public void setIsIdle(boolean isIdle) {
		this.isIdle = isIdle;
	}

	public void resetToDefaultIdle() {
		// Reset If No Attack Is Occuring
		if (!isAttacking) {
			state = PlayerState.DEFAULT;
		}
	}

	public void interactWithElevator() {
		int targetSurfaceY = elevatorManager.tryInteract(getBoundingRectangle());
		if (targetSurfaceY != Integer.MIN_VALUE) {
			yPos = targetSurfaceY - height;
			jumping = false;
			inAir = false;
			timeElapsed = 0;
			initialVelocity = 0;
			startY = yPos;
		}
	}

	public boolean isNearElevator() {
		return elevatorManager.isNearElevator(getBoundingRectangle());
	}

	public void setPosition(int x, int y) {
    xPos = x;
    yPos = y;
}


public void freeze() {
    freezeTimer = FREEZE_DURATION;
}

public String getName() {
		return "Player";
	}

	// Returns The Current Floor Number. Ground Floor = 0, First Floor = 1, etc.
	public int getFloor() {
		int rawFloor = (int) Math.floor((double) yPos / worldHeight);
		return Math.abs(rawFloor) + 0;
	}




}
