import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class CheckImage {
    public static void main(String[] args) throws Exception {
        System.out.println("Reading image...");
        BufferedImage img = ImageIO.read(new File("Assets/Projectiles/paperball.png"));
        System.out.println("Width: " + img.getWidth() + " Height: " + img.getHeight());
    }
}
