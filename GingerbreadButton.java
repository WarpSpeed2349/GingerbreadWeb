import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JButton;


public class GingerbreadButton extends JButton {
    private BufferedImage normalImg;
    private BufferedImage pressedImg;
    private BufferedImage focusedImg;
    private final int cornerEdge;

    public GingerbreadButton(String text, String normalPath, String pressedPath, String focusedPath, int cornerEdge) {
        super(text);
        this.cornerEdge = cornerEdge;
        
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setForeground(Color.WHITE);
        setFont(new Font("SansSerif", Font.BOLD, 13));

        try {
            BufferedImage rawNormal = ImageIO.read(new File(normalPath));
            BufferedImage rawPressed = ImageIO.read(new File(pressedPath));
            BufferedImage rawFocused = ImageIO.read(new File(focusedPath));

            normalImg = rawNormal.getSubimage(1, 1, rawNormal.getWidth() - 2, rawNormal.getHeight() - 2);
            pressedImg = rawPressed.getSubimage(1, 1, rawPressed.getWidth() - 2, rawPressed.getHeight() - 2);
            focusedImg = rawFocused.getSubimage(1, 1, rawFocused.getWidth() - 2, rawFocused.getHeight() - 2);
        } catch (Exception e) {
            System.err.println("Error loading Gingerbread button assets: " + e.getMessage());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        BufferedImage currentImg = normalImg;
        if (getModel().isPressed()) {
            currentImg = pressedImg;
        } else if (isFocusOwner() || getModel().isRollover()) {
            currentImg = focusedImg;
        }

        if (currentImg != null) {
            int iw = currentImg.getWidth();
            int ih = currentImg.getHeight();
            int w = getWidth();
            int h = getHeight();

            g2.drawImage(currentImg, 0, 0, cornerEdge, cornerEdge, 0, 0, cornerEdge, cornerEdge, null);
            g2.drawImage(currentImg, w - cornerEdge, 0, w, cornerEdge, iw - cornerEdge, 0, iw, cornerEdge, null);
            g2.drawImage(currentImg, 0, h - cornerEdge, cornerEdge, h, 0, ih - cornerEdge, cornerEdge, ih, null);
            g2.drawImage(currentImg, w - cornerEdge, h - cornerEdge, w, h, iw - cornerEdge, ih - cornerEdge, iw, ih, null);

            g2.drawImage(currentImg, cornerEdge, 0, w - cornerEdge, cornerEdge, cornerEdge, 0, iw - cornerEdge, cornerEdge, null);
            g2.drawImage(currentImg, cornerEdge, h - cornerEdge, w - cornerEdge, h, cornerEdge, ih - cornerEdge, iw - cornerEdge, ih, null);
            g2.drawImage(currentImg, 0, cornerEdge, cornerEdge, h - cornerEdge, 0, cornerEdge, cornerEdge, ih - cornerEdge, null);
            g2.drawImage(currentImg, w - cornerEdge, cornerEdge, w, h - cornerEdge, iw - cornerEdge, cornerEdge, iw, ih - cornerEdge, null);

            g2.drawImage(currentImg, cornerEdge, cornerEdge, w - cornerEdge, h - cornerEdge, cornerEdge, cornerEdge, iw - cornerEdge, ih - cornerEdge, null);
        }

        super.paintComponent(g);
        g2.dispose();
    }
}
