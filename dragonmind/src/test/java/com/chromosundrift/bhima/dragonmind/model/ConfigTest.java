package com.chromosundrift.bhima.dragonmind.model;

import org.junit.Assert;
import org.junit.Test;

import static java.util.Collections.singletonList;


public class ConfigTest {
    @Test
    public void testSave() throws ConfigException {
        Config c = new Config("test proj", "123");
        c.setBrightnessThreshold(160);
        PixelPusherInfo ppi = new PixelPusherInfo("12:34:56:78:90", "PP1", "black, rectangular");
        c.setPixelPushers(singletonList(ppi));
        String config = c.unParse();
        String expected = "{\"project\":\"test proj\",\"version\":\"123\"," +
                "\"pixelPushers\":[{\"macAddress\":\"12:34:56:78:90\",\"name\":\"PP1\",\"description\":\"black, rectangular\"}]," +
                "\"brightnessThreshold\":160}";
        Assert.assertEquals(expected, config);
    }
}
