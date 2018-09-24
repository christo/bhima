package com.chromosundrift.bhima.dragonmind.model;

import com.chromosundrift.bhima.dragonmind.model.Config;
import com.chromosundrift.bhima.dragonmind.model.PixelPoint;
import com.chromosundrift.bhima.dragonmind.model.Segment;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Integer.parseInt;

public class LegacyMappingSlurper {
    public static void main(String[] args) throws IOException {
        if (args.length > 0) {
            Config config = Config.load();
            for (String file : args) {
                String path = "Mappings/" + file;
                Segment segment = new Segment();
                segment.setName("wings mapping from " + path);
                List<PixelPoint> pixelPoints = readPointsFromMappingFile(path);
                segment.setPixels(pixelPoints);
                config.addSegment(segment);
            }
            config.save();
        } else {
            System.out.println("usage: LegacyMappingSlurper <csvfile1.csv> <csvfile2.csv> ...");
        }
    }

    private static List<PixelPoint> readPointsFromMappingFile(String path) throws IOException {
        List<PixelPoint> pixelPoints = new ArrayList<>();
        LineIterator lineIterator = FileUtils.lineIterator(new File(path));
        boolean firstLine = true;
        while (lineIterator.hasNext()) {
            String[] s = lineIterator.next().split(",");
            if (!firstLine) {
                pixelPoints.add(new PixelPoint(parseInt(s[0]), parseInt(s[1]), parseInt(s[2]), parseInt(s[3])));
            } else {
                firstLine = false;
            }
        }
        return pixelPoints;
    }
}