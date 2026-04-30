import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import javax.swing.JFrame;

public class SolidObjectManager {
	
	private JFrame gameWindow;
	private SolidObject[] solidObjects;
	
	private int worldWidth;
	private int worldHeight;

	private static final int NUM_FLOORS  = 5;
	private static final int FLOOR_HEIGHT = 672;
  
   public SolidObjectManager(JFrame gameWindow, int worldWidth, int worldHeight) {
		this.gameWindow = gameWindow;

	  	this.worldWidth = worldWidth;
	  	this.worldHeight = worldHeight;
      
	//   solidObjects = new SolidObject[1];
      
	//   // Floor
    //   solidObjects[0] = new SolidObject (0, this.worldHeight - 10, this.worldWidth, 50, new Color(0, 0, 0, 0));

		solidObjects = new SolidObject[NUM_FLOORS]; // ← NEW constant NUM_FLOORS = 5

		for (int i = 0; i < NUM_FLOORS; i++) {
			int floorY = FLOOR_HEIGHT * (1 - i) - 10; // ← NEW formula, matches BackgroundManager stacking
			solidObjects[i] = new SolidObject(0, floorY, this.worldWidth, 50, new Color(0,0,0,0));
		}
   }


   public void draw(Graphics2D g2) {
	   for (int i = 0; i < solidObjects.length; i++) {
    	  SolidObject solidObject = solidObjects[i];
    	  solidObject.draw (g2);
      }
   }

   public SolidObject collidesWith(Rectangle2D.Double boundingRectangle) {
	   for (int i = 0; i < solidObjects.length; i++) {
    	  
    	  SolidObject solidObject = solidObjects[i];
    	  Rectangle2D.Double rect = solidObject.getBoundingRectangle();
    	  
		  if (rect.intersects (boundingRectangle)) {
			return solidObjects[i];
		  }
      }
      return null;
   }
   
   // Helper Function To Determine If Player Is Standing On A Solid Object
   
   public boolean isStandingOn(Rectangle2D.Double playerRect) {
	    for (SolidObject so : solidObjects) {
	        // Check X-alignment
	        boolean xOverlap = playerRect.x + playerRect.width > so.getX() && 
	                           playerRect.x < so.getX() + so.getWidth();
	        
	        // Check if player's bottom is EXACTLY on the object's top
	        // We use a small buffer (1 or 2 pixels) for precision errors
	        boolean onTop = Math.abs((playerRect.y + playerRect.height) - so.getY()) < 2;

	        if (xOverlap && onTop) return true;
	    }
	    return false;
	}
}