
public class SecondBoss extends MiniBoss {

    private int dir;

    private int attackRange = 100;

    public SecondBoss(int x, int y) {
        super(x, y, 160, 240, 100, 2);
        specialCooldownMax = 600;
        walkAnimation = loadStripAnimation("/Assets/MiniBoss/SecondBossWalk.png", 8, true);
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

    @Override
    public void specialAttack(Player player) {
        if (specialCooldown > 0) return;
 
        player.freeze();
 
        specialCooldown = specialCooldownMax;
 
        System.out.println("[SecondBoss] Player frozen!");
    }

    

}
