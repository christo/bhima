package com.chromosundrift.bhima.dragonmind.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

@SuppressWarnings("WeakerAccess")
public final class PixelPointDeserializer extends StdDeserializer<PixelPoint> {

    public PixelPointDeserializer() {
        super(PixelPoint.class);
    }

    @Override
    public PixelPoint deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

        ObjectCodec codec = p.getCodec();
        JsonNode node = codec.readTree(p);

        int strip = node.get(0).asInt();
        int pixel = node.get(1).asInt();
        int x = node.get(2).asInt();
        int y = node.get(3).asInt();

        return new PixelPoint(strip, pixel, x, y);

    }
}
