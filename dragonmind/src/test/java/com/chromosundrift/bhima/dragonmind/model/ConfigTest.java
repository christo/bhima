package com.chromosundrift.bhima.dragonmind.model;

import com.chromosundrift.bhima.geometry.PixelPoint;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertTrue;

public class ConfigTest {
    @Test
    public void testSave() throws ConfigException {
        Config c = new Config("test proj", "123");
        c.setBrightnessThreshold(160);
        PixelPusherInfo ppi = new PixelPusherInfo("12:34:56:78:90", "PP1", "black, rectangular");
        c.setPixelPushers(singletonList(ppi));
        String config = c.unParse(false);
        String expected = "{\"project\":\"test proj\",\"version\":\"123\",\"brightnessThreshold\":160," +
                "\"pixelPushers\":[{\"macAddress\":\"12:34:56:78:90\"," +
                "\"name\":\"PP1\",\"description\":\"black, rectangular\"}]}";
        Assert.assertEquals(expected, config);
    }

    @Test
    public void testSaveNontrivial() throws ConfigException, IOException {
        Config c = new Config("Bhima 2018", "1.0");
        c.setBrightnessThreshold(150);
        PixelPusherInfo ppi = new PixelPusherInfo("12:34:56:78:90", "PP1", "black, rectangular");
        c.setPixelPushers(singletonList(ppi));
        ArrayList<Segment> segments = new ArrayList<>();
        Segment segment = new Segment();
        segment.setBackground(new Background("bgImage.png"));
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
        String actual = c.unParse(true);

        Assert.assertFalse(actual.toLowerCase().contains("segments"));

        ClassLoader cl = getClass().getClassLoader();
        InputStreamReader expectedR = new InputStreamReader(cl.getResourceAsStream("expected.config.json"));
        boolean areEqual = IOUtils.contentEquals(expectedR, new StringReader(actual));

        if (!areEqual) {
            System.out.println("expected = " + IOUtils.toString(new InputStreamReader(cl.getResourceAsStream("expected.config.json"))));
            System.out.println("actual = " + actual);
        }
        assertTrue("generated json not as expected", areEqual);
    }

    @Test
    public void testLoadNotAsplode() throws IOException {
        Config config = Config.load();
    }
}
