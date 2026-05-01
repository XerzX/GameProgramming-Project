import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

public class MiniBossManager {

    private static MiniBossManager instance;
    private ArrayList<MiniBoss> bosses;

    private int attackTimer    = 0;
    private int attackCooldown = 120;

    public static MiniBossManager getInstance() {
        if (instance == null)
            instance = new MiniBossManager();
        return instance;
    }

    private MiniBossManager() {
        bosses = new ArrayList<>();

        bosses.add(new FirstBoss(800, 672 - 150));
        bosses.add(new SecondBoss(200, 672 - 150 - 672));
        bosses.add(new ThirdBoss(100, 672 - 150 - 1344));
        bosses.add(new FourthBoss(300, 672 - 150 - 2016));
    }

   public void update(int worldWidth, Rectangle2D.Double playerRect) {
    attackTimer++;

    int playerX = (int) playerRect.x;
    int playerY = (int) playerRect.y;

    for (int i = 0; i < bosses.size(); i++) {
        MiniBoss boss = bosses.get(i);

        if (!boss.isDead()) {

            // Activate boss when player intersects its bounding rectangle
            if (!boss.isStarted() && boss.getTriggerZone().intersects(playerRect)) {
    boss.startFight();
}

            double dist = Math.abs(boss.getBoundingRectangle().x - playerX);

            boss.chasePlayer(playerX, playerY);

            if (boss.isStarted() && dist <= boss.getAttackRange() && attackTimer >= attackCooldown) {
                decideAttack(boss, dist);
                attackTimer = 0;
            }

            boss.update();
        }
    }
}

private void decideAttack(MiniBoss boss, double dist) {

    if (boss instanceof FirstBoss || boss instanceof ThirdBoss) {
        boss.meleeAttack();
    } 
    else if (boss instanceof SecondBoss || boss instanceof FourthBoss) {
        boss.projectileAttack();
    }
}
    public void draw(Graphics2D g2) {
        for (int i = 0; i < bosses.size(); i++)
            bosses.get(i).draw(g2);
    }
}