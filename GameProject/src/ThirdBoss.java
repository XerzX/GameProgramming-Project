
public class ThirdBoss extends MiniBoss {

    private int dir;

    private int attackRange = 100;

    public ThirdBoss(int x, int y) {
        super(x, y, 160, 240, 100, 3);
        walkAnimation = loadStripAnimation("/Assets/MiniBoss/ThirdBossWalk.png", 8, true);

        currentAnimation = walkAnimation;
    }

    @Override
    public void meleeAttack() {
        currentAnimation = meleeAnimation;
    }

    @Override
    public void projectileAttack() {
        currentAnimation = projectileAnimation;

        if (facingLeft) {
            dir = 1;
        } else {
            dir = 2;
        }
    }

}
