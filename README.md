# GameProject Object-Oriented Class Diagram

Below is a generated UML representation of the core objects, interactions, and inheritance relationships that power your campus climber video game.

```mermaid
classDiagram
    
    %% Game System & Orchestration
    class GameApplication {
        +main(args)
    }

    class GameWindow {
        -currLevel : int
        -worldWidth : int
        -worldHeight : int
        +changeLevel(newLevel)
        +gameUpdate()
        +gameRender()
    }

    GameApplication ..> GameWindow : Instantiates

    %% Core Entities & Managers
    GameWindow *-- Player : Composites
    GameWindow *-- HUD : Composites
    GameWindow *-- SolidObjectManager : Composites
    GameWindow *-- BackgroundManager : Composites
    GameWindow *-- ElevatorManager : Composites
    GameWindow *-- FinalBoss : Composites
    GameWindow ..> MiniBossManager : Uses Singleton
    GameWindow ..> SoundManager : Uses Singleton

    %% UI
    class HUD {
        -Player player
        -MiniBossManager bossManager
        -FinalBoss finalBoss
        +draw(Graphics2D)
        +drawPlayerHealthBar()
        +drawFinalBossBar()
    }
    HUD --> Player : Tracks
    HUD --> MiniBossManager : Tracks
    HUD --> FinalBoss : Tracks

    %% Player & Abilities
    class Player {
        -HP : int
        -state : PlayerState
        +applyDamage(int)
        +attackMelee()
        +attackRanged()
        +update()
    }

    class PaperBall {
        -isActive : boolean
        +update()
        +checkCollision()
    }
    Player ..> PaperBall : Spawns

    %% Enemy Framework (Inheritance)
    class MiniBossBehaviour {
        <<interface>>
        +meleeAttack()
        +projectileAttack()
        +specialAttack()
    }

    class MiniBoss {
        <<abstract-like>>
        -hp : int
        -maxHP : int
        +takeDamage(int)
        +chasePlayer(playerX, playerY)
    }
    MiniBoss ..|> MiniBossBehaviour : Implements

    class FirstBoss { }
    class SecondBoss { }
    class ThirdBoss { }
    class FourthBoss { }
    
    FirstBoss --|> MiniBoss : Extends
    SecondBoss --|> MiniBoss : Extends
    ThirdBoss --|> MiniBoss : Extends
    FourthBoss --|> MiniBoss : Extends

    class MiniBossManager {
        <<Singleton>>
        -ArrayList~MiniBoss~ bosses
        +getInstance()
    }
    MiniBossManager *-- MiniBoss : Manages

    %% Level 2 Final Boss Entity
    class FinalBoss {
        <<Singleton>>
        -hp : int
        -shockwaves : ArrayList
        +getInstance()
        +chasePlayer()
        +meleeAttack()
        +projectileAttack()
    }

    class Shockwave {
        -isActive : boolean
        +update()
        +checkCollision(Player)
    }
    FinalBoss *-- Shockwave : Spawns

    %% World & Collectibles
    class SolidObjectManager {
        -ArrayList~SolidObject~ solidObjects
    }
    class SolidObject {
        +getHitBox()
    }
    SolidObjectManager *-- SolidObject

    class BossDrop {
        +update()
        +draw(Graphics2D)
    }
    class HealthDrop {
        +update()
        +draw(Graphics2D)
    }
    MiniBoss ..> BossDrop : Spawns on internal Death
    MiniBoss ..> HealthDrop : Spawns on startFight

    %% Animation Systems
    class Animation {
        +addFrame()
        +start()
        +update()
    }
    class StripAnimationHV {
        +update()
    }
    class StripAnimation { }

    Player *-- Animation
    Player *-- StripAnimationHV
    MiniBoss *-- Animation
    FinalBoss *-- Animation

    %% Graphic Filtering
    class ImageFX {
        <<interface>>
        +apply(BufferedImage)
    }
    class RedTintFX {
        <<Singleton>>
    }
    RedTintFX ..|> ImageFX
    Player ..> RedTintFX : Uses on Damage
    MiniBoss ..> RedTintFX : Uses on Damage
    FinalBoss ..> RedTintFX : Uses on Damage
```

### Design Notes:
* **Singleton Patterrns:** Utility classes that only run once per application stream (like `ImageManager`, `MiniBossManager`, `FinalBoss`, and `RedTintFX`) are built as strictly encapsulated singletons.
* **Component Encapsulation:** Systems like the `SolidObjectManager` map geometric terrain and aggregate instances of `SolidObject` safely away from the player classes, preventing collision leaks. 
* **Scalable Inheritance:** Floor-bosses universally rely on the `MiniBoss` parent superclass which universally abides by the `MiniBossBehaviour` interface—ensuring all enemies safely comply with identical HP routing and physics without reinventing combat handlers.
