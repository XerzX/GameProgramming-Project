import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;

/**
 * UI Elements
 *
 * Renders all 2-D UI elements on top of the game world:
 * 1. Player health bar (top-left) – pulses red when low, glows gold when full
 * 2. Boss health bar (top-centre) – appears only while a fight is active
 * 3. Control buttons (bottom-right) – Z / X / SPACE hints
 */
public class HUD {

    // Player Health Bar
    private static final int P_BAR_X = 36;
    private static final int P_BAR_Y = 36;
    private static final int P_BAR_W = 360;
    private static final int P_BAR_H = 38;
    private static final int P_BAR_RADIUS = 19; // For Rounded Corners

    // Boss Health Bar
    private static final int B_BAR_W = 680;
    private static final int B_BAR_H = 38;
    private static final int B_BAR_Y = 36;
    private static final int B_BAR_RADIUS = 19;

    // Control Icons
    private static final int BTN_SIZE = 72;
    private static final int BTN_GAP = 18;
    private static final int BTN_MARGIN_R = 32;
    private static final int BTN_MARGIN_B = 32;

    // Ticks elapsed since the HUD was created
    private int tick = 0;

    private static final Font LABEL_FONT = new Font("Arial", Font.BOLD, 17);
    private static final Font BTN_KEY_FONT = new Font("Arial", Font.BOLD, 24);
    private static final Font BTN_DESC_FONT = new Font("Arial", Font.PLAIN, 13);

    // Collectibles tray
    private static final int SLOT_SIZE    = 56;  // px – each item slot square
    private static final int SLOT_GAP     = 10;
    private static final int SLOT_MARGIN_L = 36;
    private static final int SLOT_MARGIN_B = 32;
    private static final String[] DROP_PATHS = {
        "/Assets/Collectable/B1Drop-BgRemoved.png",
        "/Assets/Collectable/B2Drop.png",
        "/Assets/Collectable/B3Drop.png",
        "/Assets/Collectable/B4Drop.png"
    };
    // Pre-loaded sprites (one per boss slot, index 0-3)
    private final javax.swing.ImageIcon[] dropIcons = new javax.swing.ImageIcon[4];

    private final Player player;
    private final MiniBossManager bossManager;

    public HUD(Player player, MiniBossManager bossManager) {
        this.player = player;
        this.bossManager = bossManager;
        // Pre-load all four drop sprites so they are ready instantly when needed
        for (int i = 0; i < 4; i++) {
            dropIcons[i] = ImageManager.getInstance().loadImage(DROP_PATHS[i]);
        }
    }

    /**
     * Call this every frame AFTER the world-translation has been reset
     * so that (0,0) corresponds to the top-left corner of the viewport.
     */
    public void draw(Graphics2D g2, int vpWidth, int vpHeight) {
        tick++;

        // Save rendering quality settings
        Object prevAA = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        Object prevText = g2.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawPlayerHealthBar(g2);
        drawBossHealthBar(g2, vpWidth);
        drawFloorIndicator(g2, vpWidth);
        drawControlButtons(g2, vpWidth, vpHeight);
        drawCollectedDrops(g2, vpHeight);

        // Restore
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                prevAA != null ? prevAA : RenderingHints.VALUE_ANTIALIAS_DEFAULT);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                prevText != null ? prevText : RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT);
    }

    // 1 – PLAYER HEALTH BAR

    private void drawPlayerHealthBar(Graphics2D g2) {
        int hp = player.getHP();
        float pct = hp / 100f; // 0.0 → 1.0

        // Background Shell
        // Player Name Title Above the Bar
        g2.setFont(new Font("Arial", Font.BOLD, 19));
        String playerTitle = player.getName();
        FontMetrics pmf = g2.getFontMetrics();
        int ptx = P_BAR_X + (P_BAR_W - pmf.stringWidth(playerTitle)) / 2;
        int pty = P_BAR_Y - 8;

        // Shadow
        g2.setColor(new Color(0, 0, 0, 180));
        g2.drawString(playerTitle, ptx + 1, pty + 1);
        // Text
        g2.setColor(new Color(180, 255, 180));
        g2.drawString(playerTitle, ptx, pty);

        drawBarShell(g2, P_BAR_X, P_BAR_Y, P_BAR_W, P_BAR_H, P_BAR_RADIUS);

        // Determine Fill Colour Based on HP Percentage
        Color fillStart, fillEnd;

        if (pct >= 0.75f) {
            // Full / near-full: warm green → slightly lighter green
            fillStart = new Color(60, 200, 80);
            fillEnd = new Color(100, 230, 120);
        } else if (pct >= 0.4f) {
            // Mid health: amber/yellow
            fillStart = new Color(220, 170, 30);
            fillEnd = new Color(255, 210, 60);
        } else {
            // Low health: red tones – animated pulse
            float pulse = getPulse(6f); // 6 Hz pulse cycle
            int r = (int) (200 + 55 * pulse);
            int g = (int) (30 + 20 * pulse);
            fillStart = new Color(r, g, 30);
            fillEnd = new Color(Math.min(255, r + 40), g + 10, 20);
        }

        // Draw Fill
        int fillW = (int) (P_BAR_W * pct);
        if (fillW > 0) {
            drawBarFill(g2, P_BAR_X, P_BAR_Y, fillW, P_BAR_H, P_BAR_RADIUS, fillStart, fillEnd);
        }

        // Draw Glow / Color Pulse Overlay
        if (pct >= 0.85f) {
            // Full-health bright white/gold shimmer
            float shimmer = getPulse(1.5f); // slow gentle shimmer
            int alpha = (int) (40 + 60 * shimmer);
            drawGlowOverlay(g2, P_BAR_X, P_BAR_Y, (int) (P_BAR_W * pct), P_BAR_H, P_BAR_RADIUS,
                    new Color(255, 240, 160, alpha));
        } else if (pct < 0.4f && fillW > 0) {
            // Low-health pulsing red glow
            float pulse = getPulse(6f);
            int alpha = (int) (30 + 80 * pulse);
            drawGlowOverlay(g2, P_BAR_X, P_BAR_Y, fillW, P_BAR_H, P_BAR_RADIUS,
                    new Color(255, 60, 60, alpha));
        }

        // Label
        drawBarLabel(g2, P_BAR_X, P_BAR_Y, P_BAR_W, P_BAR_H,
                "HP  " + hp + " / 100");
    }

    // 2 – BOSS HEALTH BAR

    private void drawBossHealthBar(Graphics2D g2, int vpWidth) {
        // Find an active (started, not dead) boss on the current floor
        MiniBoss activeBoss = findActiveBoss();
        if (activeBoss == null)
            return;

        int bBarX = (vpWidth - B_BAR_W) / 2;

        // Background Shell
        drawBarShell(g2, bBarX, B_BAR_Y, B_BAR_W, B_BAR_H, B_BAR_RADIUS);

        // Boss HP fraction
        int maxHp = activeBoss.getMaxHP();
        int curHp = Math.max(0, activeBoss.hp);
        float pct = (maxHp > 0) ? (curHp / (float) maxHp) : 0f;

        // Fill Colour
        // Purple / Violet Colour Scheme For Bosses
        Color fillStart, fillEnd;
        if (pct >= 0.5f) {
            fillStart = new Color(150, 60, 220);
            fillEnd = new Color(190, 100, 255);
        } else if (pct >= 0.25f) {
            fillStart = new Color(200, 50, 100);
            fillEnd = new Color(240, 80, 130);
        } else {
            // Danger – dark pulsing crimson
            float pulse = getPulse(5f);
            int r = (int) (200 + 55 * pulse);
            fillStart = new Color(r, 20, 50);
            fillEnd = new Color(Math.min(255, r + 40), 30, 60);
        }

        int fillW = (int) (B_BAR_W * pct);
        if (fillW > 0) {
            drawBarFill(g2, bBarX, B_BAR_Y, fillW, B_BAR_H, B_BAR_RADIUS, fillStart, fillEnd);
        }

        // Glow / Color Pulse Overlay
        if (pct >= 0.85f) {
            float shimmer = getPulse(1.2f);
            int alpha = (int) (30 + 50 * shimmer);
            drawGlowOverlay(g2, bBarX, B_BAR_Y, fillW, B_BAR_H, B_BAR_RADIUS,
                    new Color(220, 160, 255, alpha));
        } else if (pct < 0.25f && fillW > 0) {
            float pulse = getPulse(5f);
            int alpha = (int) (30 + 80 * pulse);
            drawGlowOverlay(g2, bBarX, B_BAR_Y, fillW, B_BAR_H, B_BAR_RADIUS,
                    new Color(255, 80, 80, alpha));
        }

        // HP Numbers Inside The Bar
        drawBarLabel(g2, bBarX, B_BAR_Y, B_BAR_W, B_BAR_H, curHp + " / " + maxHp);

        // Boss Name Title Above The Bar
        g2.setFont(new Font("Arial", Font.BOLD, 19));
        String title = activeBoss.getName();
        FontMetrics fm = g2.getFontMetrics();
        int titleX = bBarX + (B_BAR_W - fm.stringWidth(title)) / 2;
        int titleY = B_BAR_Y - 8;

        // Shadow
        g2.setColor(new Color(0, 0, 0, 180));
        g2.drawString(title, titleX + 1, titleY + 1);
        // Text
        g2.setColor(new Color(230, 200, 255));
        g2.drawString(title, titleX, titleY);
    }

    // 3 – FLOOR INDICATOR

    private void drawFloorIndicator(Graphics2D g2, int vpWidth) {
        int floor = player.getFloor();

        // Badge dimensions
        int badgeW  = 140;
        int badgeH  = P_BAR_H;          // same height as the health bar
        int badgeX  = vpWidth - 36 - badgeW;
        int badgeY  = P_BAR_Y;
        int radius  = P_BAR_RADIUS;

        // Dark glass shell (same helper as health bars)
        drawBarShell(g2, badgeX, badgeY, badgeW, badgeH, radius);

        // Thin gold accent line along the top edge
        float shimmer = getPulse(1.2f);
        int accentAlpha = (int)(120 + 60 * shimmer);
        g2.setColor(new Color(255, 215, 80, accentAlpha));
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(badgeX + radius, badgeY + 1,
                    badgeX + badgeW - radius, badgeY + 1);
        g2.setStroke(new BasicStroke(1f));

        // Floor text – "FLOOR  3" style
        String floorLabel = "FLOOR  " + floor;
        g2.setFont(LABEL_FONT);
        FontMetrics fm = g2.getFontMetrics();
        int tx = badgeX + (badgeW - fm.stringWidth(floorLabel)) / 2;
        int ty = badgeY + (badgeH + fm.getAscent() - fm.getDescent()) / 2 - 1;

        // Shadow
        g2.setColor(new Color(0, 0, 0, 200));
        g2.drawString(floorLabel, tx + 1, ty + 1);
        // Gold text
        g2.setColor(new Color(255, 230, 120));
        g2.drawString(floorLabel, tx, ty);

        // "Level X" title above the badge, mirroring player / boss name style
        g2.setFont(new Font("Arial", Font.BOLD, 19));
        String title = "Level " + player.getLevel();
        FontMetrics tfm = g2.getFontMetrics();
        int ttx = badgeX + (badgeW - tfm.stringWidth(title)) / 2;
        int tty = badgeY - 8;

        g2.setColor(new Color(0, 0, 0, 180));
        g2.drawString(title, ttx + 1, tty + 1);
        g2.setColor(new Color(255, 230, 120));
        g2.drawString(title, ttx, tty);
    }

    // 4 – CONTROL BUTTONS

    private void drawControlButtons(Graphics2D g2, int vpWidth, int vpHeight) {
        // Three buttons: Z (Melee), X (Ranged), SPACE (Jump)
        // Laid out from right to left at the bottom right corner.

        String[] keys = { "Z", "X", "SPC" };
        String[] descs = { "Melee", "Ranged", "Jump" };
        // Accent colours per button
        Color[] accents = {
                new Color(255, 120, 60), // warm orange – melee
                new Color(60, 160, 255), // cool blue – ranged
                new Color(80, 220, 180), // teal – jump
        };

        int n = keys.length;
        int totalW = n * BTN_SIZE + (n - 1) * BTN_GAP;
        int startX = vpWidth - BTN_MARGIN_R - totalW;
        int startY = vpHeight - BTN_MARGIN_B - BTN_SIZE - 18; // 18 px for desc label below

        for (int i = 0; i < n; i++) {
            int bx = startX + i * (BTN_SIZE + BTN_GAP);
            int by = startY;
            drawButton(g2, bx, by, BTN_SIZE, keys[i], descs[i], accents[i]);
        }
    }

    // Draws a single key-cap button with a key label and a description below.
    private void drawButton(Graphics2D g2, int x, int y, int size,
            String key, String desc, Color accent) {

        // Soft hover-like glow behind the button
        float pulse = getPulse(1.8f);
        int glowAlpha = (int) (12 + 18 * pulse);
        g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), glowAlpha));
        g2.fillRoundRect(x - 4, y - 4, size + 8, size + 8, size / 2, size / 2);

        // Dark semi-transparent body
        GradientPaint body = new GradientPaint(
                x, y, new Color(30, 30, 40, 210),
                x, y + size, new Color(15, 15, 25, 220));
        g2.setPaint(body);
        g2.fillRoundRect(x, y, size, size, 14, 14);

        // Accent border
        g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 180));
        g2.setStroke(new BasicStroke(2.2f));
        g2.drawRoundRect(x, y, size, size, 14, 14);
        g2.setStroke(new BasicStroke(1f));

        // Top sheen highlight
        g2.setColor(new Color(255, 255, 255, 30));
        g2.fillRoundRect(x + 3, y + 3, size - 6, size / 3, 10, 10);

        // Key label (centred)
        g2.setFont(BTN_KEY_FONT);
        FontMetrics fmK = g2.getFontMetrics();
        int kx = x + (size - fmK.stringWidth(key)) / 2;
        int ky = y + (size + fmK.getAscent() - fmK.getDescent()) / 2 - 2;

        // Shadow
        g2.setColor(new Color(0, 0, 0, 160));
        g2.drawString(key, kx + 1, ky + 1);
        // Text
        g2.setColor(accent.brighter());
        g2.drawString(key, kx, ky);

        // Description below the button
        g2.setFont(BTN_DESC_FONT);
        FontMetrics fmD = g2.getFontMetrics();
        int dx = x + (size - fmD.stringWidth(desc)) / 2;
        int dy = y + size + fmD.getAscent() + 4;

        g2.setColor(new Color(0, 0, 0, 140));
        g2.drawString(desc, dx + 1, dy + 1);
        g2.setColor(new Color(200, 200, 220));
        g2.drawString(desc, dx, dy);
    }

    // 5 – COLLECTIBLES TRAY

    private void drawCollectedDrops(Graphics2D g2, int vpHeight) {
        boolean[] collected = player.getCollectedDrops();
        int startX = SLOT_MARGIN_L;
        int startY = vpHeight - SLOT_MARGIN_B - SLOT_SIZE;

        for (int i = 0; i < 4; i++) {
            int cx = startX + i * (SLOT_SIZE + SLOT_GAP);
            int cy = startY;

            // Background slot plate
            g2.setColor(new Color(20, 20, 30, 220));
            g2.fillRoundRect(cx, cy, SLOT_SIZE, SLOT_SIZE, 12, 12);
            g2.setColor(new Color(50, 50, 60, 180));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(cx, cy, SLOT_SIZE, SLOT_SIZE, 12, 12);
            g2.setStroke(new BasicStroke(1f));

            if (collected[i] && dropIcons[i] != null) {
                // Glow behind the item
                float pulse = getPulse(1.5f);
                int glowAlpha = (int) (20 + 40 * pulse);
                g2.setColor(new Color(255, 215, 80, glowAlpha));
                g2.fillRoundRect(cx + 2, cy + 2, SLOT_SIZE - 4, SLOT_SIZE - 4, 10, 10);

                // Draw the item sprite, scaled down to fit nicely in the slot
                Image img = dropIcons[i].getImage();
                int pad = 8;
                g2.drawImage(img, cx + pad, cy + pad, SLOT_SIZE - 2 * pad, SLOT_SIZE - 2 * pad, null);

                // Golden border around the gathered item slot
                g2.setColor(new Color(255, 215, 80, 200));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(cx, cy, SLOT_SIZE, SLOT_SIZE, 12, 12);
                g2.setStroke(new BasicStroke(1f));
            } else {
                // Draw a simple placeholder/shadow for uncollected item slots
                g2.setColor(new Color(0, 0, 0, 100));
                g2.fillOval(cx + SLOT_SIZE / 4, cy + SLOT_SIZE / 4, SLOT_SIZE / 2, SLOT_SIZE / 2);
            }
        }
    }

    // Shared bar-drawing helpers

    /** Dark glass-like shell (background track). */
    private void drawBarShell(Graphics2D g2, int x, int y, int w, int h, int r) {
        // Dark background
        g2.setColor(new Color(10, 10, 15, 200));
        g2.fillRoundRect(x, y, w, h, r, r);

        // Subtle inner shadow / border
        g2.setColor(new Color(0, 0, 0, 160));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(x, y, w, h, r, r);
        g2.setStroke(new BasicStroke(1f));

        // Top highlight (glass sheen)
        g2.setColor(new Color(255, 255, 255, 18));
        g2.fillRoundRect(x + 2, y + 2, w - 4, h / 2 - 2, r - 2, r - 2);
    }

    /** Gradient-filled bar fill, clipped to the bar's rounded shape. */
    private void drawBarFill(Graphics2D g2, int x, int y, int fillW, int h, int r,
            Color start, Color end) {
        // Clip to rounded pill shape of the bar
        Shape clip = new RoundRectangle2D.Float(x, y, fillW, h, r, r);
        Shape oldClip = g2.getClip();
        g2.setClip(clip);

        GradientPaint gp = new GradientPaint(x, y, start, x, y + h, end);
        g2.setPaint(gp);
        g2.fillRect(x, y, fillW, h);

        // Inner horizontal light streak
        g2.setColor(new Color(255, 255, 255, 35));
        g2.fillRect(x, y + 2, fillW, h / 2 - 2);

        g2.setClip(oldClip);
    }

    /** Translucent colour glow layered on top of the filled area. */
    private void drawGlowOverlay(Graphics2D g2, int x, int y, int fillW, int h, int r, Color glow) {
        Shape clip = new RoundRectangle2D.Float(x, y, fillW, h, r, r);
        Shape oldClip = g2.getClip();
        g2.setClip(clip);

        g2.setColor(glow);
        g2.fillRect(x, y, fillW, h);

        g2.setClip(oldClip);
    }

    /** Centred text label inside the bar, with drop-shadow. */
    private void drawBarLabel(Graphics2D g2, int x, int y, int w, int h, String text) {
        g2.setFont(LABEL_FONT);
        FontMetrics fm = g2.getFontMetrics();
        int tx = x + (w - fm.stringWidth(text)) / 2;
        int ty = y + (h + fm.getAscent() - fm.getDescent()) / 2 - 1;

        // Shadow
        g2.setColor(new Color(0, 0, 0, 200));
        g2.drawString(text, tx + 1, ty + 1);
        // Bright text
        g2.setColor(new Color(240, 240, 255));
        g2.drawString(text, tx, ty);
    }

    /**
     * Returns a value [0, 1] that oscillates at {@code hz} cycles per second
     * (assuming 60 fps). Used to drive pulse / shimmer animations.
     */
    private float getPulse(float hz) {
        double angle = (tick / 60.0) * hz * 2.0 * Math.PI;
        return (float) ((Math.sin(angle) + 1.0) / 2.0); // 0..1
    }

    /**
     * Finds the first MiniBoss that has started fighting and is still alive.
     * Returns null when no boss is currently engaged.
     */
    private MiniBoss findActiveBoss() {
        ArrayList<MiniBoss> bosses = bossManager.getBosses();
        for (MiniBoss b : bosses) {
            if (b.isStarted() && !b.isDead()) {
                return b;
            }
        }
        return null;
    }
}
