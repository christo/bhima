package com.chromosundrift.bhima.dragonmind;

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
        if (x < 0 || x > maxX) {
            dx = -dx;
        }
        if (y < 0 || y > maxY) {
            dy = -dy;
        }
    }

}
