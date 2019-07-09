package com.chromosundrift.sketches;

import processing.core.PApplet;
import processing.core.PVector;
import processing.event.MouseEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CirclePacker extends PApplet {

    private static final int MAX_CIRCLES = 5000;
    private static final float GROWTH_RATE = 0.2f;
    private static final float MIN_RADIUS = 5f;
    private static final boolean DO_MOVES = true;
    private static final boolean POP_GAUSSIAN = true;
    private static final float POP_CHANCE = 0.009f;
    private static final int UPDATES_PER_FRAME = 4;

    private ArrayList<Circle> circles = new ArrayList<>();
    private List<PVector> pops = new ArrayList<>();
    private int showPopsUntil;

    @Override
    public void settings() {
        fullScreen(P3D, 1);
        //        size(1200, 1000, P3D);

        pixelDensity(2);
        smooth();
    }

    @Override
    public void setup() {
        frameRate(60);
        showPopsUntil = 0;
    }

    @Override
    public void mouseClicked(MouseEvent event) {
        pop(event.getX(), event.getY());
    }

    @Override
    public void mouseDragged(MouseEvent event) {
        pop(event.getX(), event.getY());
    }

    private void pop(float x, float y) {
        circles.removeIf(c -> c.contains(x, y));
    }

    @Override
    public void draw() {
        if (circles.size() < MAX_CIRCLES) {
            findAvailablePoint().ifPresent(v -> circles.add(new Circle(v)));
        }

        circles.forEach(circle -> {
            circle.grow();
            if (DO_MOVES) {
                // see if we can move to new location
                doMoves(circle);
            }

        });
        if (POP_GAUSSIAN && random(1f) < POP_CHANCE) {
            float theta = random(TWO_PI);
            float mag = randomGaussian() * min(width, height) / 2;
            float x = width / 2 + sin(theta) * mag;
            float y = height / 2 + cos(theta) * mag;
            pops.add(new PVector(x, y));
            showPopsUntil = millis() + 3000;
            pop(x, y);
        }
        if (frameCount % UPDATES_PER_FRAME == 0) {
            background(0);
            circles.forEach(Circle::draw);
            if (showPopsUntil > millis()) {
                drawPops();
            }
        }
    }

    private void doMoves(Circle circle) {
        float newX = circle.x + random(-1f, 1f);
        float newY = circle.y + random(-1f, 1f);
        if (onScreen(newX, newY, circle.r) && circles.stream().filter(c -> c != circle).noneMatch(c -> c.intersects(newX, newY, circle.r))) {
            circle.x = newX;
            circle.y = newY;
        }
    }

    private boolean onScreen(float x, float y, float r) {
        return !(x < r) && !(x > width - r) && !(y < r) && !(y > height - r);
    }

    private void drawPops() {
        pops.forEach(v -> {
            noStroke();
            fill(0, 40);
            ellipse(v.x, v.y, 50, 50);
        });
    }

    private int randomLightColour() {
        return color(random(128) + 128, random(128) + 128, random(128) + 128);
    }

    private Optional<PVector> findAvailablePoint() {
        PVector point = new PVector(random(width), random(height));

        if (circles.stream().noneMatch(c -> c.intersects(point.x, point.y, MIN_RADIUS))) {
            return Optional.of(point);
        } else {
            return Optional.empty();
        }
    }

    public static void main(String[] args) {
        PApplet.main(CirclePacker.class, args);
    }

    @SuppressWarnings("WeakerAccess")
    private class Circle {

        public int colour;
        private float r;
        private float x;
        private float y;

        public Circle(float x, float y, float r) {
            this.x = x;
            this.y = y;
            this.r = r;
            this.colour = randomLightColour();
        }

        public Circle(PVector vector) {
            this(vector.x, vector.y, MIN_RADIUS);
        }

        boolean contains(float x, float y) {
            return dist(x, y, this.x, this.y) <= r;
        }

        public void grow() {
            if (onScreen() && circles.stream().filter(c -> this != c).noneMatch(this::intersects)) {
                this.r += GROWTH_RATE;
            }
        }

        private boolean onScreen() {
            return CirclePacker.this.onScreen(x,y,r);
        }

        private boolean intersects(Circle circle) {
            return intersects(circle.x, circle.y, circle.r);
        }

        private boolean intersects(float x, float y, float r) {
            return dist(x, y, this.x, this.y) <= this.r + r;
        }

        private void draw() {
            float d = r * 2;
            CirclePacker.this.fill(this.colour);
            CirclePacker.this.stroke(0);
            CirclePacker.this.strokeWeight(2f);
            CirclePacker.this.ellipse(x, y, d, d);
        }

    }
}


