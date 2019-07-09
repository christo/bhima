package com.chromosundrift.bhima.dragonmind.program;

import com.chromosundrift.bhima.dragonmind.DragonMind;
import processing.core.PConstants;
import processing.core.PGraphics;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BallsProgram extends AbstractDragonProgram implements DragonProgram {

    private float t = 0;
    private List<Ball> balls = new ArrayList<>();
    private Random r = new Random();
    private boolean attract;

    @Override
    public void setup(DragonMind mind) {

        for (int i = 0; i < 10; i++) {
            balls.add(new Ball(r.nextInt(mind.width), r.nextInt(mind.height)));
        }
    }

    public PGraphics draw(DragonMind mind, int width, int height) {
        PGraphics pg = mind.createGraphics(width, height);
        pg.beginDraw();
        pg.ellipseMode(PConstants.CENTER);
        t += 1.9;
        pg.noStroke();
        pg.fill(0);
        pg.rect(0, 0, pg.width, pg.height);
        for (Ball ball : balls) {
            pg.fill(mind.color(255, t % 255, ball.y % 255));
            pg.ellipse(ball.x, ball.y, 200, 200);
            float newX = ball.x + r.nextFloat() * 2 - 1;
            float newY = ball.y + r.nextFloat() * 2 - 1;
            ball.x = (int) newX;
            ball.y = (int) newY;
            ball.move(pg.width, pg.height);
        }
        pg.endDraw();
        return pg;
    }


    @Override
    public void mouseClicked(DragonMind mind) {
        attract = !attract;
    }

}