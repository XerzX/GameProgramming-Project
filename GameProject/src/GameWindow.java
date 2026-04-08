import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

public class GameWindow extends JFrame implements Runnable, KeyListener,
													MouseListener, MouseMotionListener
{
	
	private static final int NUM_BUFFERS = 2;
	
	private GraphicsDevice userDevice;
	private int pWidth, pHeight;
	
	private Graphics gScr;
	private BufferStrategy bufferStrategy;
	
	private BufferedImage bufferedImage;
	
	private Thread gameThread = null;
	
	private volatile Boolean isRunning = false;
	private volatile Boolean isPaused = false;
	
	private Player player;
	
	public GameWindow() {
		super("Insert Title Here");
		initFullScreen();
		
		addKeyListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
		
		// Instantiate Singleton Utility Classes
		SoundManager.getInstance();
		ImageManager.getInstance();
		
		// Buffer For Each Frame
		bufferedImage = new BufferedImage(pWidth, pHeight, BufferedImage.TYPE_INT_RGB);
		
		startGame();
	}
	
	// Setup FullScreen Exclusive Mode
	private void initFullScreen() {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		userDevice = ge.getDefaultScreenDevice();
		
		setUndecorated(true);
		setIgnoreRepaint(true);
		setResizable(false);
		
		if(!userDevice.isFullScreenSupported()) {
			System.out.println("Full-screen Exclusive Mode Not Supported");
			System.exit(0);
		}
		userDevice.setFullScreenWindow(this);
		
		pWidth = getBounds().width;
		pHeight = getBounds().height;
		
		try {
			createBufferStrategy(NUM_BUFFERS);
		}
		catch (Exception e) {
			System.out.println("Error Creating Buffer Strategy");
			System.exit(0);
		}
		
		bufferStrategy = getBufferStrategy();
	}
	
	private void createEntities() {
		player = new Player(this);
	}
	
	// Updates The Position Of Game Entities
	private void gameUpdate() {
		
	}
	
	// Update Player Position
	private void updatePlayer(int direction) {
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
		}
		catch (Exception e) { 
			e.printStackTrace();  
			isRunning = false; 
		}
	}
	
	// Renders Updated Entities To The Screen
	public void gameRender (Graphics gScr) {
		Graphics2D imageContext = (Graphics2D) bufferedImage.getGraphics();
		
		// 1 - Render Background
		imageContext.clearRect(0, 0, getWidth(), getHeight());
		
		// N - Render Player
		player.draw(imageContext);
		
		Graphics2D g2 = (Graphics2D) gScr;
		g2.drawImage(bufferedImage, 0, 0, pWidth, pHeight, null);
		
		imageContext.dispose();
		g2.dispose();
	}
	
	private void startGame() {
		createEntities();
		
		if (gameThread == null) {
			gameThread = new Thread(this);
			gameThread.start();
		}
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
				Thread.sleep(50);
			}
		}
		catch (InterruptedException e) {
			System.out.println(e);
		}
		
		// Do Something When Game Loop Terminates (Handle Game Over Logic Here)
	}
	
	/**
	The Following Methods Are Listener Methods For Dynamic Event Handling
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
		
		if (code == KeyEvent.VK_LEFT) {
			updatePlayer(1);
		}
		if (code == KeyEvent.VK_RIGHT) {
			updatePlayer(2);
		}
		if (code == KeyEvent.VK_UP) {
			updatePlayer(3);
		}
		if (code == KeyEvent.VK_DOWN) {
			updatePlayer(4);
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		
	}
}
