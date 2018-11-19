package com.chromosundrift.bhima.dragonmind;

import com.chromosundrift.bhima.dragonmind.model.Config;
import com.chromosundrift.bhima.dragonmind.model.Segment;
import com.chromosundrift.bhima.geometry.PixelPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates a straight dragon model in a strict diamond grid, tapering from the bottom up.
 */
public class DragonGenerator {

    // TODO LHS
    // TODO finish tapering tail
    // TODO tail wiring
    // TODO neck (RHS only)
    // TODO exceptions
    // TODO test/verify

    private static final Logger logger = LoggerFactory.getLogger(DragonGenerator.class);

    private int xOrigin;
    private int yOrigin;
    int width;
    int height;
    int margin;
    int ledsPerPanel = 100;
    int panelsPerStrip = 3;

    public DragonGenerator(int xOrigin, int yOrigin, int width, int height, int margin) {
        this.xOrigin = xOrigin;
        this.yOrigin = yOrigin;
        this.width = width;
        this.height = height;
        this.margin = margin;
    }

    public Config generateDragon() {
        // draw neck

        // draw full-size body panels

        int ph = height / panelsPerStrip - margin;
        int pw = width / panelsPerStrip - margin;

        int x = xOrigin;
        int y = yOrigin;

        ArrayList<Segment> segments = new ArrayList<>();
        for (int sNum = 0; sNum < 3; sNum++) {
            int xOffset = panelsPerStrip * (pw + margin) * sNum;
            segments.add(generateSegment(panelsPerStrip, x - xOffset, y, pw, ph, sNum + 1));
        }

        // TODO draw tapered tail panels

        // starting from panel 11, segments have progressively fewer rows
        // panel 11 has 10 rows in the rightmost column, then 9 for the rest


        Config c = new Config("bhima generated model", "1.0");
        c.setPixelMap(segments);

        return c;
    }

    private Segment generateSegment(int nPanels, int x, int y, int pw, int ph, int sNum) {
        Segment s = new Segment();
        s.setName("generated segment " + sNum);

        // generate panels right to left
        for (int n = nPanels - 1; n >= 0; n--) {
            int gPanelNumber = sNum * nPanels + n;
            s.addPixelPoints(generatePanel(x + (pw + margin) * n, y, 10, 10, pw, ph, gPanelNumber));
        }
        return s;
    }

    public List<PixelPoint> generatePanel(int x, int y, int ledCountX, int ledCountY, int pw, int ph, int gPanelNumber) {
        ArrayList<PixelPoint> pps = new ArrayList<>();
        // start at the top right
        int pixelIndex = 0;
        int vStep = ph / ledCountY;
        int evenRowVerticalOffset = vStep / 2;
        int hStep = pw / ledCountX;
        int stripIndex = 0;
        // start at top left
        for (int yy = 0; yy < ledCountY; yy++) {
            // first zig
            for (int xx = 0; xx < ledCountX; xx++) {
                boolean evenColumn = xx % 2 == gPanelNumber % 2;
                int yOffset = evenColumn ? evenRowVerticalOffset : 0;
                PixelPoint pp = new PixelPoint(stripIndex, pixelIndex, x + hStep * xx, y + yOffset + vStep * yy);
                pps.add(pp);
                pixelIndex++;
            }
            yy++;
            // then zag
            for (int xx = ledCountX - 1; xx >= 0; xx--) {
                boolean evenColumn = xx % 2 == gPanelNumber % 2;
                int yOffset = evenColumn ? evenRowVerticalOffset : 0;
                PixelPoint pp = new PixelPoint(stripIndex, pixelIndex, x + hStep * xx, y + yOffset + vStep * yy);
                pps.add(pp);
                pixelIndex++;
            }
        }
        return pps;
    }
}
