package com.chromosundrift.bhima.api;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.apache.commons.io.output.ByteArrayOutputStream;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Base64;

public class ImageSerializer extends StdSerializer<BufferedImage> {

    public ImageSerializer() {
        super(BufferedImage.class);
    }

    static String imgToString(BufferedImage img) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "jpg", baos);
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    /**
     * Base64 encodes image as jpeg.
     *
     * @throws IOException TODO: figure out how/why/what-to-do.
     */
    @Override
    public void serialize(BufferedImage img, JsonGenerator gen, SerializerProvider provider) throws IOException {
        // very memory hungry - should make this fully streaming but right now it doesn't matter
        String encoded = imgToString(img);
        gen.writeString(encoded);
    }
}