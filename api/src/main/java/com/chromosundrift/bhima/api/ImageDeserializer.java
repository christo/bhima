package com.chromosundrift.bhima.api;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

@SuppressWarnings("unused")
public class ImageDeserializer extends StdDeserializer<BufferedImage> {

    public ImageDeserializer() {
        super(BufferedImage.class);
    }

    @Override
    public BufferedImage deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        final ObjectCodec codec = p.getCodec();
        final JsonNode node = codec.readTree(p);
        final String encoded = node.asText();
        final byte[] bytes = Base64.getDecoder().decode(encoded);
        final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        return ImageIO.read(bais);
    }
}
