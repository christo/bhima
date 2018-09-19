package com.chromosundrift.bhima.dragonmind;

import processing.core.PApplet;

public class Experiment extends PApplet {

    @Override
    public void settings() {
        size(800, 800, P2D);
    }

    @Override
    public void setup() {
        super.setup();
    }

    @Override
    public void draw() {
        background(255, 160, 90);
    }

    public static void main(String[] args) {
        String name = Experiment.class.getName();
        System.out.println(name);
        PApplet.main(name, args);
    }
}
