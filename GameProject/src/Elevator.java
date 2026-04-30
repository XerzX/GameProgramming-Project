import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class Elevator {

    private int x, y;
    private int width, height;
    private int targetFloor; // floor surface Y of the destination floor

    private BufferedImage image;

    // Interaction zone — slightly larger than the elevator hitbox
    private static final int INTERACT_RANGE = 80;

    public Elevator(String imagePath, int x, int y, int width, int height, int targetFloor) {
        this.x = x;
        this.y = y;
        this.width  = width;
        this.height = height;
        this.targetFloor = targetFloor;

        image = ImageManager.getInstance().loadBufferedImage(imagePath);
        // image may be null if the asset isn't found — draw() handles that gracefully
    }

    public void draw(Graphics2D g2) {
        
        if (image != null) {
            g2.drawImage(image, x, y, width, height, null);
        } 
    }

   
    public boolean canInteract(Rectangle2D.Double playerRect) {
        Rectangle2D.Double zone = new Rectangle2D.Double(
            x - INTERACT_RANGE, y - INTERACT_RANGE,
            width  + INTERACT_RANGE * 2,
            height + INTERACT_RANGE * 2
        );
        return zone.intersects(playerRect);
    }

  
    public int getTargetFloor() {
        return targetFloor;
    }

    public Rectangle2D.Double getBoundingRectangle() {
        return new Rectangle2D.Double(x, y, width, height);
    }
}