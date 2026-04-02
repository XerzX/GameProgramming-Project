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
	
	private BufferStrategy bufferStrategy;
	private BufferedImage bufferedImage;
	
	private Thread gameThread = null;
	
	private volatile Boolean isRunning = false;
	private volatile Boolean isPaused = false;
	
	public GameWindow() {
		super("Insert Title Here");
		initFullScreen();
		
		addKeyListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
		
		bufferedImage = new BufferedImage(pWidth, pHeight, BufferedImage.TYPE_INT_RGB);
		
		// Instantiate Singleton Utility Classes
		SoundManager.getInstance();
		ImageManager.getInstance();
		
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
	
	private void startGame() {
		if (gameThread == null) {
			gameThread = new Thread(this);
			gameThread.start();
		}
	}
	
	// Updates The Position Of Game Entities
	private void gameUpdate() {
		// TODO Auto-generated method stub
			
	}
		
	// Renders Updated Entities To The Screen
	private void screenUpdate() {
		// TODO Auto-generated method stub
			
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
}
