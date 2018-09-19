package com.chromosundrift.bhima.dragonmind;

import com.heroicrobot.processing.examples.ArrayScanner2;
import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TestPattern extends AbstractDragonProgram implements DragonProgram {

    private float t = 0;
    private List<Ball> balls = new ArrayList<>();
    private Random r = new Random();
    private boolean attract;

    @Override
    public void setup(DragonMind mind) {

        for (int i = 0; i < 100; i++) {
            balls.add(new Ball(r.nextInt(mind.width), r.nextInt(mind.height)));
        }
    }

    public void draw(DragonMind mind) {

        mind.ellipseMode(mind.CENTER);
        t += 1.9;
        mind.noStroke();
        mind.fill(0);
        mind.rect(0, 0, mind.width, mind.height);
        for (Ball ball : balls) {
            mind.fill(mind.color(255, t % 255, ball.y % 255));
            mind.ellipse(ball.x, ball.y, 50, 50);
            float newX = ball.x + r.nextFloat() * 2 - 1;
            float newY = ball.y + r.nextFloat() * 2 - 1;
            if (attract) {
                newX += (float)(mind.mouseX - ball.x) / 100;
                newY += (float)(mind.mouseY - ball.y) / 100;
            }

            ball.x = (int) newX;
            ball.y = (int) newY;
            ball.move(mind.width, mind.height);
        }
    }


    @Override
    public void mouseClicked(DragonMind mind) {
        attract = !attract;
    }

}
