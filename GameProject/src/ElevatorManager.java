import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

public class ElevatorManager {

    private static ElevatorManager instance;
    private Elevator[] elevators;

    private static final int FLOOR_HEIGHT = 672;
    private static final int NUM_FLOORS = 5;

    private static final int ELEV_W = 120;
    private static final int ELEV_H = 180;
    private static final int ELEV_X = 1100;

    public static ElevatorManager getInstance() {
        if (instance == null)
            instance = new ElevatorManager();
        return instance;
    }

    private ElevatorManager() {
        elevators = new Elevator[NUM_FLOORS];

        for (int i = 0; i < NUM_FLOORS; i++) {

            int floorSurfaceY = FLOOR_HEIGHT * (1 - i) - 10;
            int elevY = floorSurfaceY - ELEV_H;

            int targetIndex = (i + 1) % NUM_FLOORS;
            int targetSurfaceY = FLOOR_HEIGHT * (1 - targetIndex) - 10;

            System.out.println("Floor " + i + ": elevY=" + elevY
                    + "  ->  targetIndex=" + targetIndex
                    + ", targetSurfaceY=" + targetSurfaceY);

            elevators[i] = new Elevator(
                    "/Assets/Elevator/elevator.png",
                    ELEV_X, elevY,
                    ELEV_W, ELEV_H,
                    targetSurfaceY);
        }
    }

    public void draw(Graphics2D g2) {
        for (Elevator e : elevators)
            e.draw(g2);
    }

    public int tryInteract(Rectangle2D.Double playerRect) {
        for (Elevator e : elevators) {
            if (e.canInteract(playerRect))
                return e.getTargetFloor();
        }
        return Integer.MIN_VALUE;
    }

    public boolean isNearElevator(Rectangle2D.Double playerRect) {
        for (Elevator e : elevators)
            if (e.canInteract(playerRect))
                return true;
        return false;
    }
}