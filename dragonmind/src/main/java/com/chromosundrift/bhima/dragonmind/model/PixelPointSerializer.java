package com.chromosundrift.bhima.dragonmind.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class PixelPointSerializer extends StdSerializer<PixelPoint> {

    protected PixelPointSerializer(Class<PixelPoint> t) {
        super(t);
    }

    @Override
    public void serialize(PixelPoint pp, JsonGenerator gen, SerializerProvider provider) throws IOException {
        int x = pp.getPoint().getX();
        int y = pp.getPoint().getY();
        int[] vals = new int[]{pp.getStrip(), pp.getPixel(), x, y};
        gen.writeArray(vals, 0, vals.length);

    }
}
