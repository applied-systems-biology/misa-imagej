package org.hkijena.misa_imagej.utils.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Icon that takes an existing image icon and uses it as mask for recoloring
 */
public class MonochromeColorIcon implements Icon
{
    private BufferedImage template;
    private Color color;

    public MonochromeColorIcon(ImageIcon template) {
        this(template, Color.WHITE);
    }

    public MonochromeColorIcon(ImageIcon template, Color color) {
        this.template = new BufferedImage(template.getIconWidth(), template.getIconHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D graphics2D = this.template.createGraphics();
        graphics2D.drawImage(template.getImage(), 0, 0, null);
        this.color = color;
    }

    public void setColor(Color c)
    {
        color = c;
    }

    public Color getColor()
    {
        return color;
    }

    public void paintIcon(Component c, Graphics g, int x, int y)
    {
        int[] buf = new int[1];

        for(int i = 0; i < getIconWidth(); ++i) {
            for(int j = 0; j < getIconHeight(); ++j) {
                template.getData().getPixel(j, i, buf);
                Color tmp = new Color(color.getRed(), color.getGreen(), color.getBlue(), buf[0]);
                g.setColor(tmp);
                g.drawRect(x + j, y + i, 1, 1);
            }
        }
    }

    @Override
    public int getIconWidth() {
        return template.getWidth();
    }

    @Override
    public int getIconHeight() {
        return template.getHeight();
    }
}