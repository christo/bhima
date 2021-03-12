package com.chromosundrift.bhima.api;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.Assert;
import org.junit.Test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.StringWriter;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;

public class ImageSerializerTest {

    public static final String BASE64_CHARS = "^\"[A-Za-z0-9+/=]+\"$";

    @Test
    public void testSerialize() throws IOException {
        BufferedImage bi = new BufferedImage(100, 100, TYPE_INT_RGB);
        Graphics2D g2d = bi.createGraphics();
        g2d.setColor(Color.gray);
        g2d.fillRect(0, 0, 100, 100);
        g2d.setColor(Color.white);
        g2d.drawLine(0, 0, 100, 100);
        g2d.drawLine(0, 100, 100, 0);
        g2d.drawOval(0, 0, 100, 100);
        g2d.setColor(Color.cyan);
        g2d.fillOval(25, 25, 50, 50);
        g2d.dispose();


        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("ImageSerializer", new Version(1, 0, 0, null, null, null));
        module.addSerializer(BufferedImage.class, new ImageSerializer());
        mapper.registerModule(module);
        StringWriter w = new StringWriter();
        mapper.writeValue(w, bi);
        String encoded = w.toString();
        Assert.assertTrue("expected base 64 string: " + encoded, encoded.matches(BASE64_CHARS));

    }
}
