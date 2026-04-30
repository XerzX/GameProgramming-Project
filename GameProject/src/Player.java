import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

public class Player {
	
	// Reference To Window Player Will Drawn On
	private JFrame gameWindow;
	private SolidObjectManager soManager;
	
	private ImageIcon playerSprite;
	private ImageManager imageManager;
	
	// Player's World Coordinates
	private int xPos;
	private int yPos;
	
	private int dx;
	private int dy;
	
	private int width;
	private int height;
	
	private boolean facingLeft = false;
	private boolean jumping = false;
	private boolean inAir = false;
	
	private int timeElapsed = 0;
	private int startY;
	private int initialVelocity = 0;

	private ProjectileManager projectileManager;


	/////////////////////////
    private ElevatorManager elevatorManager;
    private boolean nearElevator = false;
	
	public Player(JFrame gameWindow, SolidObjectManager soManager) {

		projectileManager = ProjectileManager.getInstance();
		
		this.gameWindow = gameWindow;
		this.soManager = soManager;
		
		imageManager = ImageManager.getInstance();
		playerSprite = imageManager.loadImage("/Assets/Player/TestPlayer.png");
		
		// Set Player's World Coordinates
		xPos = 0;
		yPos = 0;
		
		dx = 10;
		dy = 10;
		
		width = 408 / 3;
		height = 612 / 3;


		elevatorManager = ElevatorManager.getInstance();
	}
	
	public void draw(Graphics2D g2) {
		if (facingLeft) {
			g2.drawImage(playerSprite.getImage(), xPos, yPos, width, height, null);
			return;
		}
		
		// Mirror The Image To Make The Player Change Directions
		g2.drawImage(playerSprite.getImage(), xPos + width, yPos, -width, height, null);
	}
	
	public void move(int direction) {
		// Move Left
		if (direction == 1) {
			xPos -= dx;
			facingLeft = true;
			
			if (xPos <= 0) {
				xPos = 0;
			}
		}
		
		// Move Right
		if (direction == 2) {
			xPos += dx;
			facingLeft = false;
			
			if (xPos + width > gameWindow.getWidth()) {
				xPos = gameWindow.getWidth() - width;
			}
		}
		
		// Jump
		if (direction == 3) {
			startJump();
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
	        initialVelocity = 50; // Adjust This For Jump Height. If Too Will Cause Bug Preventing Collision Checks
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
		return new Rectangle2D.Double (xPos, yPos, width, height);
	}
	
	public void resolveCollisions() {
	    SolidObject hit = soManager.collidesWith(getBoundingRectangle());
	    
	    if (hit != null) {
	        Rectangle2D.Double playerRect = getBoundingRectangle();
	        Rectangle2D.Double objectRect = hit.getBoundingRectangle();

	        // Calculate Overlap On All Sides
	        double overlapLeft   = playerRect.x + playerRect.width - objectRect.x;
	        double overlapRight  = objectRect.x + objectRect.width - playerRect.x;
	        double overlapTop    = playerRect.y + playerRect.height - objectRect.y;
	        double overlapBottom = objectRect.y + objectRect.height - playerRect.y;

	        // Find The Smallest Overlap
	        // That's The Direction The Collision Occurred
	        double minOverlap = Math.min(Math.min(overlapLeft, overlapRight), 
	                                     Math.min(overlapTop, overlapBottom));

	        if (minOverlap == overlapTop) {
	            // Fell OnTop Of Object (Landed)
	            yPos = (int)objectRect.y - height;
	            jumping = false;
	            inAir = false;
	        } 
	        else if (minOverlap == overlapBottom) {
	            // Hit Bottom Of Object (Bumped Head)
	            yPos = (int)(objectRect.y + objectRect.height);
	            startFall(); // Stop Upward Momentum And Start Falling
	        } 
	        else if (minOverlap == overlapLeft) {
	            // Hit Left Side Of Object
	            xPos = (int)objectRect.x - width;
	        } 
	        else {
	            // Hit Right Side Of Object
	            xPos = (int)(objectRect.x + objectRect.width);
	        }
	    }
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





///////////////////////////////

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

public void fire() {
    int direction = facingLeft ? 1 : 2;
    projectileManager.spawn(xPos, yPos, width, height, direction);
}
}


// Add field:




// Add method:
