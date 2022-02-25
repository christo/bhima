package com.chromosundrift.bhima.api;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;

public class ImageUtils {

    /**
     * The image you have when you're not having an image.
     */
    static BufferedImage generateNullImage(int width, int height) {
        BufferedImage bi = new BufferedImage(width, height, TYPE_INT_RGB);
        Graphics2D graphics = bi.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(Color.gray);
        graphics.fillRect(0, 0, width, height);
        graphics.setColor(Color.white);
        graphics.drawLine(0, 0, width, height);
        graphics.drawLine(0, height, width, 0);
        graphics.drawOval(0, 0, width, height);
        graphics.setColor(Color.red);
        graphics.fillOval(width / 4, height / 4, width / 2, height / 2);
        graphics.setColor(Color.white);
        final FontMetrics fontMetrics = graphics.getFontMetrics();
        final String mesg = "NOT AVAILABLE";
        final Rectangle2D stringBounds = fontMetrics.getStringBounds(mesg, graphics);
        final int sx = (int) (width / 2 - stringBounds.getWidth() / 2);
        final int sy = (int) (height / 2 - stringBounds.getHeight() / 2);
        graphics.drawString(mesg, sx, sy);
        graphics.dispose();
        return bi;
    }
}
