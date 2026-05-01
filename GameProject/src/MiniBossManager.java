import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

public class MiniBossManager {

    private static MiniBossManager instance;
    private ArrayList<MiniBoss> bosses;

    private int attackTimer = 0;
    private int attackCooldown = 120;

    private int[] specialTimers    = { 0, 0, 0, 0 };
    private int[] specialIntervals = { 480, 600, 300, 360 };

    public static MiniBossManager getInstance() {
        if (instance == null)
            instance = new MiniBossManager();
        return instance;
    }

    private MiniBossManager() {
        bosses = new ArrayList<>();

        bosses.add(new FirstBoss(800, 422));
        bosses.add(new SecondBoss(200, -250));
        bosses.add(new ThirdBoss(100, -922));
        bosses.add(new FourthBoss(300, -1594));
    }

    public void update(int worldWidth, Player player) {
        attackTimer++;

        Rectangle2D.Double playerRect = player.getBoundingRectangle();
        int playerX = (int) playerRect.x;
        int playerY = (int) playerRect.y;

        for (int i = 0; i < bosses.size(); i++) {
            MiniBoss boss = bosses.get(i);

            if (!boss.isDead()) {

                // Activate boss when player intersects its bounding rectangle
                if (!boss.isStarted() && boss.getTriggerZone().intersects(playerRect)) {
                    boss.startFight(worldWidth);
                    // boss.specialAttack(player);
                }

                double dist = Math.abs(boss.getBoundingRectangle().x - playerX);

                boss.chasePlayer(playerX, playerY);

                if (boss.isStarted() && dist <= boss.getAttackRange() && attackTimer >= attackCooldown) {
                    decideAttack(boss, dist);
                    attackTimer = 0;
                }

                        if (boss.isStarted()) {
            specialTimers[i]++;
            if (specialTimers[i] >= specialIntervals[i]) {
                boss.triggerSpecial(player);
                specialTimers[i] = 0;
            }
        }

            }
            boss.update(player);
        }
    }

    private void decideAttack(MiniBoss boss, double dist) {

        if (boss instanceof FirstBoss || boss instanceof ThirdBoss) {
            boss.meleeAttack();
        } else if (boss instanceof SecondBoss || boss instanceof FourthBoss) {
            boss.projectileAttack();
        }
    }

    public void draw(Graphics2D g2) {
        for (int i = 0; i < bosses.size(); i++)
            bosses.get(i).draw(g2);
    }

    public ArrayList<MiniBoss> getBosses() {
        return bosses;
    }
}