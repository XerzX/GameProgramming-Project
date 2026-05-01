
public class FourthBoss extends MiniBoss {

    private int dir;

    private int attackRange = 100;

    public FourthBoss(int x, int y) {
        super(x, y, 160, 240, 100, 3);
        walkAnimation = loadStripAnimation("/Assets/MiniBoss/FourthBossWalk.png", 8, true);

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

}
