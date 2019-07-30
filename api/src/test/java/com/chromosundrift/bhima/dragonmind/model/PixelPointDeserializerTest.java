package com.chromosundrift.bhima.dragonmind.model;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class PixelPointDeserializerTest {
    @Test
    public void testIt() throws IOException {
        String json = "[6, 124, 446, 501]";
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module =
                new SimpleModule("PixelPointDeserializer", new Version(1, 0, 0, null, null, null));
        module.addDeserializer(PixelPoint.class, new PixelPointDeserializer());
        mapper.registerModule(module);
        PixelPoint pp = mapper.readValue(json, PixelPoint.class);
        PixelPoint expected = new PixelPoint(6, 124, 446, 501);
        Assert.assertEquals(expected, pp);
    }
}
