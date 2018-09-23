package com.chromosundrift.bhima.dragonmind.model;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigTest {
    @Test
    public void testSave() throws ConfigException {
        Config c = new Config("test proj", "123");
        c.setBrightnessThreshold(160);
        PixelPusherInfo ppi = new PixelPusherInfo("12:34:56:78:90", "PP1", "black, rectangular");
        c.setPixelPushers(Arrays.asList(ppi));
        String config = c.unParse(false);
        String expected = "{\"project\":\"test proj\",\"version\":\"123\",\"brightnessThreshold\":160," +
                "\"pixelPushers\":[{\"macAddress\":\"12:34:56:78:90\"," +
                "\"name\":\"PP1\",\"description\":\"black, rectangular\"}]}";
        Assert.assertEquals(expected, config);
    }

    @Test
    public void testSave2() throws ConfigException {
        Config c = new Config("Bhima 2018", "1.0");
        c.setBrightnessThreshold(150);
        PixelPusherInfo ppi = new PixelPusherInfo("12:34:56:78:90", "PP1", "black, rectangular");
        c.setPixelPushers(Arrays.asList(ppi));
        ArrayList<Segment> segments = new ArrayList();
        Segment segment = new Segment();
        segment.setBackground("bgImage.png");
        segment.setName("butthole");

        List<PixelPoint> pixels = Arrays.asList(
                new PixelPoint(0, 0, 500, 500),
                new PixelPoint(0, 1, 510, 500),
                new PixelPoint(0, 2, 520, 500),
                new PixelPoint(0, 3, 530, 500),
                new PixelPoint(0, 4, 500, 510),
                new PixelPoint(0, 5, 510, 510)
        );
        segment.setPixels(pixels);
        segments.add(segment);
        c.setPixelMap(segments);
        String json = c.unParse(true);

        System.out.println("json = \n" + json);
    }

    @Test
    public void testLoad() throws IOException {
        Config config = Config.load();
    }
}
