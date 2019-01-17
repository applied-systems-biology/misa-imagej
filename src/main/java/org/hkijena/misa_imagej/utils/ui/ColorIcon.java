package org.hkijena.misa_imagej.utils.ui;

import javax.swing.*;
import java.awt.*;

public class ColorIcon implements Icon
{
    private int imageWidth;
    private int imageHeight;

    private Color color;
    private Color  border;
    private Insets insets;

    public ColorIcon()
    {
        this(32, 16);
    }

    public ColorIcon(int width, int height)
    {
        this(width, height, Color.black);
    }

    public ColorIcon(int width, int height, Color c)
    {
        imageWidth = width;
        imageHeight = height;

        color   = c;
        border  = Color.black;
        insets  = new Insets(1,1,1,1);
    }

    public void setColor(Color c)
    {
        color = c;
    }

    public Color getColor()
    {
        return color;
    }

    public void setBorderColor(Color c)
    {
        border = c;
    }

    public int getIconWidth()
    {
        return imageWidth;
    }

    public int getIconHeight()
    {
        return imageHeight;
    }

    public void paintIcon(Component c, Graphics g, int x, int y)
    {
        g.setColor(border);
        g.drawRect(x,y, imageWidth -1, imageHeight -2);

        x += insets.left;
        y += insets.top;

        int w = imageWidth - insets.left - insets.right;
        int h = imageHeight - insets.top  - insets.bottom -1;

        g.setColor(color);
        g.fillRect(x,y, w,h);
    }
}