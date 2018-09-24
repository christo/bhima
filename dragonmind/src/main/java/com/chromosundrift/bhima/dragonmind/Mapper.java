package com.chromosundrift.bhima.dragonmind;

import processing.core.PApplet;

import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

public class Mapper {
    public static String highestBandwidthCamera(String[] cameraspew) {
        Set<String> names = new TreeSet<>();
        int highest = 0;
        Pattern cameraParser = compile("name=([^,]+),size=(\\d+)x(\\d+),fps=(\\d+)");
        String bestest = "none";
        for (String camera : cameraspew) {
            Matcher m = cameraParser.matcher(camera);
            if (m.matches()) {
                names.add(m.group(1));
                int x = PApplet.parseInt(m.group(2));
                int y = PApplet.parseInt(m.group(3));
                int fps = PApplet.parseInt(m.group(4));
                int bandWidth = x * y * fps;
                System.out.println("camera = " + camera);
                if (bandWidth > highest && fps >= 30) { //HACK
                    bestest = camera;
                    highest = bandWidth;
                    System.out.println("new highest");
                }
            }
        }
        System.out.println(names.size() + " camera(s): " + names);
        return bestest;
    }

    public enum Mode {
        CAM_SCAN, WAIT, PATTERN, TEST, EDIT
    }
}
