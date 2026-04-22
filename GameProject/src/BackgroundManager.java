import java.awt.Graphics2D;
import java.util.ArrayList;

public class BackgroundManager {
	
	private static BackgroundManager instance;
	private ArrayList<Background> floors;
	
	private int floorWidth =  1584;
	private int floorHeight = 672;
	
	public static BackgroundManager getInstance() {
		if (instance == null)
			instance = new BackgroundManager();
		
		return instance;
	}
	
	private BackgroundManager() {
		floors = new ArrayList<>();
		
		// Create, Initialize and Add Background Objects For Level One Floors To ArrayList
		
		Background groundFloor = new Background("/Assets/Background/Floor1.png", 0, 0);
		floors.add(groundFloor);
		
		Background firstFloor = new Background("/Assets/Background/Floor2.png", 0, 0 - floorHeight);
		floors.add(firstFloor);
		
		Background secondFloor = new Background("/Assets/Background/Floor3.png", 0, 0 - (floorHeight * 2));
		floors.add(secondFloor);
		
		Background thirdFloor = new Background("/Assets/Background/Floor4.png", 0, 0 - (floorHeight * 3));
		floors.add(thirdFloor);
		
		Background fourthFloor = new Background("/Assets/Background/Floor5.png", 0, 0 - (floorHeight * 4));
		floors.add(fourthFloor);
	}
	
	// This Method Loops Through The ArrayList floors And Calls Their Individual draw() Methods 
	public void drawBackgrounds(Graphics2D g2) {
		int len = floors.size();
		
		for (int i = 0; i < len; i++) {
			Background background = floors.get(i);
			background.draw(g2);
		}
	}
}
