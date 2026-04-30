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

	public GameWindow() {
		super("Insert Title Here");
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
		background = new Background("/Assets/Background/Floor1.png", 0, 0);
		soManager = new SolidObjectManager(this, worldWidth, worldHeight);
		player = new Player(this, soManager, worldWidth, worldHeight);
	}

	// Updates The Position Of Game Entities
	private void gameUpdate() {
		player.update();
		player.updateAnimation();
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
		Graphics2D imageContext = (Graphics2D) bufferedImage.getGraphics();

		// Calculate Camera X Position Relative To Player (Centered On Player)
		int camX = (player.getXPos() + (player.getWidth() / 2)) - (virtualWidth / 2);

		// Calculate Camera Y Position Relative To Each Floor
		int floor = (int) Math.floor((double) player.getYPos() / worldHeight);
		int floorTopY = floor * worldHeight;
		int offset = 145;

		int camY = floorTopY - offset;

		// Clamp Camera (Prevent Camera From Going Beyond World Dimensions)
		if (camX < 0)
			camX = 0;

		if (camY < levelOneHeight)
			camY = levelOneHeight;

		if (camX > worldWidth - virtualWidth)
			camX = worldWidth - virtualWidth;

		if (camY > 0)
			camY = 0;

		// Shift The World And Entities Drawn On For Scrolling
		imageContext.translate(-camX, -camY);

		// Z-Ordering Of Entities

		// 1 - Render Background
		background.draw(imageContext);
		backgroundManager.drawBackgrounds(imageContext);

		// 2 - Render Solid Objects
		soManager.draw(imageContext);

		// 3 - Render Elevators
		ElevatorManager.getInstance().draw(imageContext);

		// 4 - "Press E" prompt if near elevator
		if (player.isNearElevator()) {
			imageContext.setColor(java.awt.Color.WHITE);
			imageContext.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 20));
			imageContext.drawString("Press E to use elevator", player.getXPos() - 30, player.getYPos() - 20);
		}

		// N - Render Player
		player.draw(imageContext);

		// Reset Translation For HUD/UI Elements
		imageContext.translate(camX, camY);

		//
		// Render UI Elements Here
		//

		imageContext.dispose();

		Graphics2D g2 = (Graphics2D) gScr;

		g2.drawImage(bufferedImage, 0, 0, pWidth, pHeight, null);
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
