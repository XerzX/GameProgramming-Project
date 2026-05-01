
public class SecondBoss extends MiniBoss {

    private int dir;

    private int attackRange = 100;

    public SecondBoss(int x, int y) {
        super(x, y, 80, 120, 100, 2);
        walkAnimation = loadStripAnimation("/Assets/MiniBoss/SecondBossWalk.png", 8, true);
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
