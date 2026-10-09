import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

public class ChromeTabUI extends BasicTabbedPaneUI {

    private Image imgActive;
    private Image imgInactive;
    private Image imgPlusBtn;

    public ChromeTabUI() {
        try {
            File fileActive = new File("pngs/tab_default_normal.png");
            if (fileActive.exists()) imgActive = ImageIO.read(fileActive);

            File fileInactive = new File("pngs/tab_default_inactive.png");
            if (fileInactive.exists()) imgInactive = ImageIO.read(fileInactive);

            File filePlus = new File("pngs/btn_default_small_normal_disable.9.png");
            if (filePlus.exists()) imgPlusBtn = ImageIO.read(filePlus);
        } catch (Exception e) {
            System.out.println("[UI ERROR] Asset loading failed: " + e.getMessage());
        }
    }

    @Override
    protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
        return 34;
    }

    @Override
    protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
        if (tabPane.getTitleAt(tabIndex).equals("     ")) {
            return 32; 
        }
        return 130; 
    }

    @Override
    protected Insets getTabInsets(int tabPlacement, int tabIndex) {
        return new Insets(4, 12, 4, 12);
    }

    @Override
    protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (tabPane.getTitleAt(tabIndex).equals("     ")) {
            if (imgPlusBtn != null) {
                g2d.drawImage(imgPlusBtn, x + 4, y + 6, 22, 22, null);
            } else {
                g2d.setColor(new Color(70, 70, 70));
                g2d.fillRoundRect(x + 4, y + 6, 22, 22, 4, 4);
            }
            
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Roboto", Font.BOLD, 14));
            FontMetrics fm = g2d.getFontMetrics();
            int textX = x + 4 + (22 - fm.stringWidth("+")) / 2;
            int textY = y + 6 + ((22 - fm.getHeight()) / 2) + fm.getAscent();
            g2d.drawString("+", textX, textY);
        } else {
            Image targetImage = isSelected ? imgActive : imgInactive;
            if (targetImage != null) {
                g2d.drawImage(targetImage, x, y, w, h, null);
            } else {
                g2d.setColor(isSelected ? Color.WHITE : Color.LIGHT_GRAY);
                g2d.fillRect(x, y, w, h);
            }
        }
        g2d.dispose();
    }

    @Override
    protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
    }

    @Override
    protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
    }
}
