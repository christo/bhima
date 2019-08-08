package com.chromosundrift.bhima.dragonmind.program;

import java.util.Random;

public class Ball {
    public int x;
    public int y;
    float dx;
    float dy;

    public Ball(int x, int y) {
        Random r = new Random();
        this.x = x;
        this.y = y;
        this.dx = r.nextFloat() * 8;
        this.dy = r.nextFloat() * 8;
    }

    public void move(int maxX, int maxY) {
        this.x += this.dx;
        this.y += this.dy;
        // if ball is out of bounds, drift into range.
        if (x < 0) {
            dx = Math.abs(dx);
        } else if (x > maxX) {
            dy = -Math.abs(dx);
        }
        if (y < 0) {
            dy = Math.abs(dy);
        } else if (y > maxY) {
            dy = -Math.abs(dy);
        }
    }

}
