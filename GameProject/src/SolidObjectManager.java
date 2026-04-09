import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JFrame;

public class SolidObjectManager {
	
	private JFrame gameWindow;
	private SolidObject[] solidObjects;
  
   public SolidObjectManager(JFrame gameWindow) {
	  this.gameWindow = gameWindow;
      
	  solidObjects = new SolidObject[1];
      
	  // Floor
      solidObjects[0] = new SolidObject (0, gameWindow.getHeight() - 50, gameWindow.getWidth(), 50, new Color(0, 0, 0, 0));
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