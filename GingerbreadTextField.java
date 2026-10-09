import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JTextField;

public class GingerbreadTextField extends JTextField {
    private BufferedImage defaultImg;
    private BufferedImage selectedImg;
    private final int cornerEdge;

    public GingerbreadTextField(String text, String defaultPath, String selectedPath, int cornerEdge) {
        super(text);
        this.cornerEdge = cornerEdge;
        
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        setForeground(Color.BLACK);
        setFont(new Font("SansSerif", Font.PLAIN, 14));

        try {
            BufferedImage rawDefault = ImageIO.read(new File(defaultPath));
            BufferedImage rawSelected = ImageIO.read(new File(selectedPath));

            defaultImg = rawDefault.getSubimage(1, 1, rawDefault.getWidth() - 2, rawDefault.getHeight() - 2);
            selectedImg = rawSelected.getSubimage(1, 1, rawSelected.getWidth() - 2, rawSelected.getHeight() - 2);
        } catch (Exception e) {
            System.err.println("Error loading Gingerbread textfield assets: " + e.getMessage());
        }

        addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) { repaint(); }
            @Override
            public void focusLost(FocusEvent e) { repaint(); }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        BufferedImage currentImg = isFocusOwner() ? selectedImg : defaultImg;

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
