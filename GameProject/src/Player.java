import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import java.awt.Image;
import java.awt.image.BufferedImage;

public class Player {

	private enum PlayerState {
		DEFAULT, // Normal idle → TestPlayer.png
		MELEE, // After melee → MeleeIdle.png (while attack plays: Melee.png)
		RANGED // After ranged → RangedIdle.png (while attack plays: Ranged.png)
	}

	private PlayerState state = PlayerState.DEFAULT;

	// Reference To Window Player Will Drawn On
	private JFrame gameWindow;
	private SolidObjectManager soManager;

	private ImageManager imageManager;

	// Walk Animation
	private StripAnimationHV walkAnimation;

	// Idle Sprites
	private ImageIcon defaultIdleSprite;
	private ImageIcon meleeIdleSprite;
	private ImageIcon rangedIdleSprite;

	// Attack Sprites (static single-frame images)
	private ImageIcon meleeAttackSprite;
	private ImageIcon rangedAttackSprite;

	// Counts down game ticks for how long the attack pose is displayed
	// (30 ticks ≈ 500 ms at 60 fps). When it reaches 0, isAttacking clears.
	private int attackTimer = 0;
	private static final int ATTACK_DISPLAY_TICKS = 30;

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

	private ElevatorManager elevatorManager;
	private boolean nearElevator = false;

	private boolean isIdle = true;
	private boolean isAttacking = false;

	// This Represents The Player's Health
	private int HP = 100;

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
		defaultIdleSprite = imageManager.loadImage("/Assets/Player/TestPlayer.png");
		meleeIdleSprite = imageManager.loadImage("/Assets/Player/MeleeIdle.png");
		rangedIdleSprite = imageManager.loadImage("/Assets/Player/RangedIdle.png");

		// Load Walk Animation
		walkAnimation = new StripAnimationHV(true, "/Assets/Player/Strip.png", 2, 4);

		// Load Attack Sprites (single static images, displayed for a time equal to
		// ATTACK_DISPLAY_TICKS)
		meleeAttackSprite = imageManager.loadImage("/Assets/Player/Melee.png");
		rangedAttackSprite = imageManager.loadImage("/Assets/Player/Ranged.png");

		elevatorManager = ElevatorManager.getInstance();
	}

	public void draw(Graphics2D g2) {

		// Move Frame
		if (!isIdle) {
			drawImage(g2, walkAnimation.getCurrentFrame());
			return;
		}

		// Attack Frame
		if (isAttacking) {
			if (state == PlayerState.MELEE) {
				drawImage(g2, meleeAttackSprite.getImage());
			} else if (state == PlayerState.RANGED) {
				drawImage(g2, rangedAttackSprite.getImage());
			}
			return;
		}

		// Idle Frame
		switch (state) {
			case MELEE -> drawImage(g2, meleeIdleSprite.getImage());
			case RANGED -> drawImage(g2, rangedIdleSprite.getImage());
			default -> drawImage(g2, defaultIdleSprite.getImage());
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

		int attackRange = 100;
		int attackHeight = 100;

		if (facingLeft)
			meleeHitBox = new Rectangle2D.Double(xPos + width, yPos + 100, attackRange, attackHeight);
		else
			meleeHitBox = new Rectangle2D.Double(xPos - attackRange, yPos + 100, attackRange, attackHeight);

		// Add Check For AttackHitBox Collision w/ Player Here

		attackTimer = ATTACK_DISPLAY_TICKS; // start countdown
	}

	public void attackRanged() {
		if (isAttacking)
			return;
		state = PlayerState.RANGED;
		isAttacking = true;

		// Call Projectile Start() Function Here

		attackTimer = ATTACK_DISPLAY_TICKS; // start countdown
	}

	public void startAnimation() {
		walkAnimation.start();
	}

	public void stopAnimation() {
		walkAnimation.stop();
	}

	public void updateAnimation() {
		// Update walk animation while the player is moving
		if (!isIdle && walkAnimation.isAnimationActive()) {
			walkAnimation.update();
		}

		// Count down the attack display timer each game tick.
		// When it reaches 0 the attack pose ends and the
		// matching idle sprite (MeleeIdle / RangedIdle) takes over.
		if (isAttacking) {
			attackTimer--;
			if (attackTimer <= 0) {
				isAttacking = false;
				attackTimer = 0;
			}
		}

		// Count down the damage tint timer each game tick.
		// When it reaches 0 the tinted sprite is discarded.
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
			initialVelocity = 50; // Adjust For Jump Height
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
		// Apply Gravity/Jumping Physics
		if (jumping || inAir) {
			timeElapsed++;
			int distance = (int) (initialVelocity * timeElapsed - 4.9 * timeElapsed * timeElapsed);
			yPos = startY - distance;
		}

		// ALWAYS Resolve Collisions After Moving
		resolveCollisions();

		// HARD LIMIT: Prevent falling below the bottom of Floor 1
		int absoluteBottom = 672 - height;
		if (yPos > absoluteBottom) {
			yPos = absoluteBottom;
			jumping = false;
			inAir = false;
			timeElapsed = 0;
		}

		// Check If Player Is In Mid-Air
		checkFalling();

		nearElevator = elevatorManager.isNearElevator(getBoundingRectangle());
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

	public int getXPos() {
		return xPos;
	}

	public int getYPos() {
		return yPos;
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
        yPos = targetSurfaceY - height; // ← NEW: feet land exactly on floor surface
        jumping         = false;
        inAir           = false;
        timeElapsed     = 0;
        initialVelocity = 0;  // ← NEW: prevents leftover momentum
        startY          = yPos; // ← NEW: anchors gravity formula to new position
    }
}


public boolean isNearElevator() {
    return elevatorManager.isNearElevator(getBoundingRectangle());
}
}
