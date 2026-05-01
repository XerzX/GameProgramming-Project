import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import javax.swing.*;

public class GameWindow extends JFrame implements Runnable, KeyListener,
		MouseListener, MouseMotionListener {

	private static final int NUM_BUFFERS = 2;

	private GraphicsDevice userDevice;
	private int pWidth, pHeight;

	private Graphics gScr;
	private BufferStrategy bufferStrategy;

	private BufferedImage bufferedImage;

	private int virtualWidth;
	private int virtualHeight;

	private Thread gameThread = null;

	private volatile Boolean isRunning = false;
	private volatile Boolean isPaused = false;

	private Player player;
	private SolidObjectManager soManager;
	private HUD hud;

	private Background background;
	private BackgroundManager backgroundManager;

	// Define The World Dimensions
	private int worldWidth = 1584;
	private int worldHeight = 672;

	// 1st Floor Is In Positive Coordinate Space 0 - FloorHeight
	// The Other 4 Floors Are In Negative Coordinate Space -(FloorHeight * 4) - 0
	// Therefore, The Total Height The Player Or Camera Can Move To Is -(FloorHeight
	// * 4)

	private int levelOneHeight = -(worldHeight * 4);

	// Level State Tracking
	private int currLevel = 1;

	// Encounter States
	private boolean isGameOver = false;
	private boolean isVictory = false;
	private boolean isStartScreen = true;

	private Image startScreenImage;

	// Level 2 Specific Entities
	private Background levelTwoBackground;
	private FinalBoss finalBoss;

	public GameWindow() {
		super("Graduation");
		initFullScreen();

		addKeyListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);

		// Instantiate Singleton Classes

		SoundManager.getInstance();
		ImageManager.getInstance();
		ElevatorManager.getInstance();
		backgroundManager = BackgroundManager.getInstance();

		// Buffer For Each Frame

		// A Virtual Resolution Is Used To Achieve Scrolling With FSEM
		// The BufferedImage Is A Smaller Resolution Than The World
		// Entities Are Drawn To The World, But This Smaller "Camera" Sees Only Part Of
		// The World
		// The Entire Thing Will Be Scaled Up To Match The Monitor's Resolution

		virtualWidth = worldWidth - 300;
		virtualHeight = worldHeight + 145;

		bufferedImage = new BufferedImage(virtualWidth, virtualHeight, BufferedImage.TYPE_INT_RGB);

		startGame();
	}

	// Setup FullScreen Exclusive Mode
	private void initFullScreen() {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		userDevice = ge.getDefaultScreenDevice();

		setUndecorated(true);
		setIgnoreRepaint(true);
		setResizable(false);

		if (!userDevice.isFullScreenSupported()) {
			System.out.println("Full-screen Exclusive Mode Not Supported");
			System.exit(0);
		}
		userDevice.setFullScreenWindow(this);

		pWidth = getBounds().width;
		pHeight = getBounds().height;

		try {
			createBufferStrategy(NUM_BUFFERS);
		} catch (Exception e) {
			System.out.println("Error Creating Buffer Strategy");
			System.exit(0);
		}

		bufferStrategy = getBufferStrategy();
	}

	private void createEntities() {
		// Initialize Common Elements
		soManager = new SolidObjectManager(this, worldWidth, worldHeight);
		player = new Player(this, soManager, worldWidth, worldHeight);

		ElevatorManager.getInstance();
		MiniBossManager.getInstance();

		hud = new HUD(player, MiniBossManager.getInstance());

		// Initialize Backgrounds
		background = new Background("/Assets/Background/Floor1.png", 0, 0);
		levelTwoBackground = new Background("/Assets/Background/Office.png", 0, 0);
		startScreenImage = ImageManager.getInstance().loadImage("/Assets/Background/StartScreen.png").getImage();
	}

	public void changeLevel(int newLevel) {
		this.currLevel = newLevel;
		soManager.loadLevel(newLevel);
		player.setLevel(newLevel);

		if (newLevel == 2) {
			// Update player boundaries so physics limits behave as expected in Office
			player.setWorldDimensions(2560, 1440);
			player.setXPos(150);
			player.setYPos(1440 - player.getHeight());

			// Spawn Final Boss Randomly In Right-Most Half of Level
			int startX = (int) (1280 + Math.random() * 1120);
			int startY = 1390 - 240;
			finalBoss = new FinalBoss(startX, startY);
			finalBoss.startFight(2560);
			hud.setFinalBoss(finalBoss);
		}
	}

	// Updates The Position Of Game Entities
	private void gameUpdate() {
		if (isStartScreen || isGameOver || isVictory) {
			return; // Freeze the game loop
		}

		if (player.getHP() <= 0) {
			isGameOver = true;
			return;
		}

		if (currLevel == 2 && finalBoss != null && finalBoss.isDead()) {
			isVictory = true;
			return;
		}

		player.update();
		player.updateAnimation();

		if (currLevel == 1) {
			MiniBossManager.getInstance().update(worldWidth, player);
		} else if (currLevel == 2) {
			// Update Level 2 Enemies Here
			if (finalBoss != null) {
				finalBoss.chasePlayer(player.getXPos(), player.getYPos());
				finalBoss.update(player);
			}
		}
	}

	// Update Player Position
	private void updatePlayer(int direction) {
		player.setIsIdle(false);
		player.move(direction);
	}

	private void screenUpdate() {
		try {
			gScr = bufferStrategy.getDrawGraphics();
			gameRender(gScr);
			gScr.dispose();
			if (!bufferStrategy.contentsLost())
				bufferStrategy.show();
			else
				System.out.println("Contents of buffer lost.");

			// Sync the display on some systems.
			// (on Linux, this fixes event queue problems)
			Toolkit.getDefaultToolkit().sync();
		} catch (Exception e) {
			e.printStackTrace();
			isRunning = false;
		}
	}

	// Renders Updated Entities To The Screen
	public void gameRender(Graphics gScr) {
		if (isStartScreen) {
			Graphics2D g2 = (Graphics2D) gScr;
			if (startScreenImage != null) {
				g2.drawImage(startScreenImage, 0, 0, pWidth, pHeight, null);
			}
			
			// Draw Title
			g2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 120));
			String title = "GRADUATION";
			int titleW = g2.getFontMetrics().stringWidth(title);
			
			int tX = (pWidth - titleW) / 2;
			int tY = (pHeight / 2) - 50; // Dropped closer to center

			// Thick black outline for readability
			g2.setColor(java.awt.Color.BLACK);
			g2.drawString(title, tX - 4, tY - 4);
			g2.drawString(title, tX + 4, tY - 4);
			g2.drawString(title, tX - 4, tY + 4);
			g2.drawString(title, tX + 4, tY + 4);
			g2.drawString(title, tX - 4, tY);
			g2.drawString(title, tX + 4, tY);
			g2.drawString(title, tX, tY - 4);
			g2.drawString(title, tX, tY + 4);
			
			// Main title text (Gold)
			g2.setColor(new java.awt.Color(255, 215, 0));
			g2.drawString(title, tX, tY);

			// Draw Prompt
			g2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 40));
			String msg = "PRESS SPACE TO START";
			int msgW = g2.getFontMetrics().stringWidth(msg);
			
			g2.setColor(new java.awt.Color(0, 0, 0, 150));
			g2.fillRect((pWidth - msgW) / 2 - 20, pHeight - 140, msgW + 40, 60);

			g2.setColor(java.awt.Color.WHITE);
			g2.drawString(msg, (pWidth - msgW) / 2, pHeight - 100);
			g2.dispose();
			return;
		}

		Graphics2D imageContext = (Graphics2D) bufferedImage.getGraphics();

		// Calculate Camera X Position Relative To Player (Centered On Player)
		int camX = (player.getXPos() + (player.getWidth() / 2)) - (virtualWidth / 2);
		int camY = 0;

		if (currLevel == 1) {
			// Calculate Camera Y Position Relative To Each Floor
			int floor = (int) Math.floor((double) player.getYPos() / worldHeight);
			int floorTopY = floor * worldHeight;
			int offset = 145;

			camY = floorTopY - offset;

			// Clamp Camera (Prevent Camera From Going Beyond World Dimensions)
			if (camX < 0)
				camX = 0;

			if (camY < levelOneHeight)
				camY = levelOneHeight;

			if (camX > worldWidth - virtualWidth)
				camX = worldWidth - virtualWidth;

			if (camY > 0)
				camY = 0;
		} else if (currLevel == 2) {
			// Level 2 Tracking (Smoothly tracks the player's Y position rather than
			// snapping floors)
			camY = (player.getYPos() + (player.getHeight() / 2)) - (virtualHeight / 2);

			if (camX < 0)
				camX = 0;

			if (camY < 0)
				camY = 0;

			if (camX > 2560 - virtualWidth)
				camX = 2560 - virtualWidth;

			if (camY > 1440 - virtualHeight)
				camY = 1440 - virtualHeight;
		}

		// Shift The World And Entities Drawn On For Scrolling
		imageContext.translate(-camX, -camY);

		// Z-Ordering Of Entities

		if (currLevel == 1) {
			// 1 - Render Background
			background.draw(imageContext);
			backgroundManager.drawBackgrounds(imageContext);

			// 2 - Render Solid Objects
			soManager.draw(imageContext);

			// 3 - Render Elevators
			ElevatorManager.getInstance().draw(imageContext);

			// 4 - Render Player & Boss
			player.draw(imageContext);
			MiniBossManager.getInstance().draw(imageContext);

			// "Press E" prompt if near elevator
			if (player.isNearElevator()) {
				imageContext.setColor(java.awt.Color.WHITE);
				imageContext.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 20));
				imageContext.drawString("Press E to use elevator", player.getXPos() - 30, player.getYPos() - 20);
			}
		} else if (currLevel == 2) {
			// Level 2 Rendering
			levelTwoBackground.draw(imageContext);
			soManager.draw(imageContext);

			if (finalBoss != null) {
				finalBoss.draw(imageContext);
			}

			player.draw(imageContext);
		}

		// Reset Translation For HUD/UI Elements
		imageContext.translate(camX, camY);

		imageContext.dispose();

		// Scale the world to fill the physical screen
		Graphics2D g2 = (Graphics2D) gScr;
		g2.drawImage(bufferedImage, 0, 0, pWidth, pHeight, null);

		// Drawing Hud Here Prevents It From Appearing Stretched Or Blurry As It Is
		// Drawn On The Native Resolution Context
		hud.draw(g2, pWidth, pHeight);

		// Draw Encounter Outcomes
		if (isGameOver) {
			g2.setColor(new java.awt.Color(10, 0, 0, 180));
			g2.fillRect(0, 0, pWidth, pHeight);
			g2.setColor(new java.awt.Color(200, 30, 30));
			g2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 100));
			String msg = "GAME OVER";
			int msgW = g2.getFontMetrics().stringWidth(msg);
			g2.drawString(msg, (pWidth - msgW) / 2, pHeight / 2);
			
			g2.setColor(java.awt.Color.WHITE);
			g2.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 30));
			String subMsg = "You failed the assignment.";
			int subW = g2.getFontMetrics().stringWidth(subMsg);
			g2.drawString(subMsg, (pWidth - subW) / 2, (pHeight / 2) + 60);
		} else if (isVictory) {
			g2.setColor(new java.awt.Color(255, 255, 255, 180));
			g2.fillRect(0, 0, pWidth, pHeight);
			g2.setColor(new java.awt.Color(50, 180, 50));
			g2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 100));
			String msg = "GRADUATED!";
			int msgW = g2.getFontMetrics().stringWidth(msg);
			g2.drawString(msg, (pWidth - msgW) / 2, (pHeight / 2) - 20);
			
			g2.setColor(java.awt.Color.BLACK);
			g2.setFont(new java.awt.Font("Arial", java.awt.Font.ITALIC, 40));
			String subMsg = "The Dean was defeated.";
			int subW = g2.getFontMetrics().stringWidth(subMsg);
			g2.drawString(subMsg, (pWidth - subW) / 2, (pHeight / 2) + 60);
		}

		g2.dispose();
	}

	private void startGame() {
		createEntities();
		player.startAnimation();

		if (gameThread == null) {
			gameThread = new Thread(this);
			gameThread.start();
		}
	}

	private void stopGame() {
		if (isRunning)
			isRunning = false;
	}

	// The run() Method Serves As The Game Loop
	@Override
	public void run() {
		try {
			isRunning = true;
			while (isRunning) {
				if (!isPaused) {
					gameUpdate();
				}
				screenUpdate();
				Thread.sleep(16); // 16 ms Translates To Approximately 60 FPS
			}
		} catch (InterruptedException e) {
			System.out.println(e);
		}

		// Do Something When Game Loop Terminates (Handle Game Over Logic Here)
	}

	/**
	 * The Following Methods Are Listener Methods For Dynamic Event Handling
	 **/

	@Override
	public void mouseDragged(MouseEvent e) {

	}

	@Override
	public void mouseMoved(MouseEvent e) {

	}

	@Override
	public void mouseClicked(MouseEvent e) {

	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

	}

	@Override
	public void mousePressed(MouseEvent e) {

	}

	@Override
	public void mouseReleased(MouseEvent e) {

	}

	@Override
	public void keyPressed(KeyEvent e) {
		int code = e.getKeyCode();

		if (code == KeyEvent.VK_C) {
			stopGame();
		}

		if (isStartScreen) {
			if (code == KeyEvent.VK_SPACE) {
				isStartScreen = false;
			}
			return;
		}

		if (code == KeyEvent.VK_LEFT) {
			updatePlayer(1);
		}
		if (code == KeyEvent.VK_RIGHT) {
			updatePlayer(2);
		}
		if (code == KeyEvent.VK_SPACE) {
			updatePlayer(3);
		}

		if (code == KeyEvent.VK_E) {
			// If on the top floor (Floor 5) and we try to use the elevator to go higher,
			// checking the transition condition
			if (currLevel == 1 && player.getFloor() == 4 && player.isNearElevator()) {
				// Let's check if player has collected all 4 drops
				boolean[] drops = player.getCollectedDrops();
				boolean hasAll = true;
				for (boolean b : drops)
					if (!b)
						hasAll = false;

				if (hasAll) {
					changeLevel(2);
					return; // Done
				}
			}

			player.interactWithElevator();
		}

		// Z — Melee attack
		if (code == KeyEvent.VK_Z) {
			player.attackMelee();
		}

		// X — Ranged attack
		if (code == KeyEvent.VK_X) {
			player.attackRanged();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		int code = e.getKeyCode();

		if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_RIGHT
				|| code == KeyEvent.VK_SPACE) {
			player.setIsIdle(true);
			player.stopAnimation();
			player.resetToDefaultIdle(); // revert to TestPlayer.png when movement stops
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {

	}
}
