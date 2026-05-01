
public class FirstBoss extends MiniBoss {

    private int dir;


    public FirstBoss(int x, int y) {
        super(x, y, 160, 240, 100, 2);
        walkAnimation = loadStripAnimation("/Assets/MiniBoss/FirstBossWalk.png", 8, true);
         specialCooldownMax = 480;
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
        if (specialCooldown > 0){
            return;
        }
 
        int oldBossX   = xPos;
        int oldBossY   = yPos;
 
        xPos = player.getXPos();
        yPos = player.getYPos();
 
        player.setPosition(oldBossX, yPos);
 
        specialCooldown = specialCooldownMax;
 
        System.out.println("[FirstBoss] Position swap!");
    }
 

}
