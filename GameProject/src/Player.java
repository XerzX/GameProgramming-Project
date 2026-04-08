import javax.swing.ImageIcon;
import javax.swing.JFrame;
import java.awt.Graphics2D;

public class Player {
	
	// Reference To Window Player Will Drawn On
	private JFrame gameWindow;
	
	private ImageIcon playerSprite;
	private ImageManager imageManager;
	
	private int xPos;
	private int yPos;
	
	private int dx;
	private int dy;
	
	private int width;
	private int height;
	
	private boolean facingLeft = false;
	
	public Player(JFrame gameWindow) {
		this.gameWindow = gameWindow;
		
		imageManager = ImageManager.getInstance();
		playerSprite = imageManager.loadImage("/Assets/Player/TestPlayer.png");
		
		xPos = 0;
		yPos = 0;
		
		dx = 10;
		dy = 10;
		
		width = 408;
		height = 612;
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
			if (xPos <= 0) {
				xPos = 0;
			}
		}
		
		// Move Right
		if (direction == 2) {
			xPos += dx;
			if (xPos + width > gameWindow.getWidth()) {
				xPos = gameWindow.getWidth() - width;
			}
		}
		
		// Move Up
		if (direction == 3) {
			yPos -= dy;
			if (yPos <= 0) {
				yPos = 0;
			}
		}
		
		// Move Down
		if (direction == 4) {
			yPos += dy;
			if (yPos + height > gameWindow.getHeight()) {
				yPos = gameWindow.getHeight() - height;
			}
		}
	}
}
