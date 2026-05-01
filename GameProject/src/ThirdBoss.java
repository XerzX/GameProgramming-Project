
public class ThirdBoss extends MiniBoss {

    private int dir;

    private int attackRange = 100;

    public ThirdBoss(int x, int y) {
        super(x, y, 160, 240, 100, 3);
         specialCooldownMax = 300;
        walkAnimation = loadStripAnimation("/Assets/MiniBoss/ThirdBossWalk.png", 8, true);

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

    public String getName() { return "Iron Crusher"; }



    @Override
    public void specialAttack(Player player) {
        if (specialCooldown > 0) return;
 
        player.applyDamage(20);
 
     
        currentAnimation = meleeAnimation;
        if (currentAnimation != null){
            currentAnimation.start();
        }
 
        specialCooldown = specialCooldownMax;
 
        System.out.println("[ThirdBoss] Heavy melee! dmg" );
    }

}
