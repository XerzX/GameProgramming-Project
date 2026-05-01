
public class FirstBoss extends MiniBoss {

    private int dir;

    public FirstBoss(int x, int y) {
        super(x, y, 160, 240, 100, 2);
        walkAnimation = loadStripAnimation("/Assets/MiniBoss/FirstBossWalk.png", 8, true);
        currentAnimation = walkAnimation;

    }

    @Override
    public void meleeAttack() {
        if (meleeAnimation != null) currentAnimation = meleeAnimation;
    }

    @Override
    public void projectileAttack() {
        if (projectileAnimation != null) currentAnimation = projectileAnimation;
        
        if (facingLeft) {
            dir = 1;
        } else {
            dir = 2;
        }

    }

    // @Override
    // public void specialAttack() {
    //     currentFrames = specialFrames;
    //     currentFrame  = 0;
    //     // BossA special: fires 3 projectiles spread vertically
    //     int dir = facingLeft ? 1 : 2;
    //     ProjectileManager.getInstance().spawn(xPos, yPos - 60, width, height, dir);
    //     ProjectileManager.getInstance().spawn(xPos, yPos,      width, height, dir);
    //     ProjectileManager.getInstance().spawn(xPos, yPos + 60, width, height, dir);
    // }
}
