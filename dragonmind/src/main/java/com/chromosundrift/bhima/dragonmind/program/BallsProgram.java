package com.chromosundrift.bhima.dragonmind.program;

import com.chromosundrift.bhima.api.ProgramInfo;
import com.chromosundrift.bhima.dragonmind.DragonMind;
import processing.core.PConstants;
import processing.core.PGraphics;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class BallsProgram extends AbstractDragonProgram implements DragonProgram {

    public static final String NAME = "Floaty Balls";
    private float t = 0;
    private List<Ball> balls = new ArrayList<>();
    private Random r = new Random();
    private boolean attract;
    private DragonMind mind;

    @Override
    public void setup(DragonMind mind) {
        this.mind = mind;
        t = 0;
        balls = new ArrayList<>();
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

    @Override
    public List<ProgramInfo> getProgramInfos(int x, int y, int w, int h) {
        return Collections.singletonList(getCurrentProgramInfo(x, y, w, h));
    }

    @Override
    public ProgramInfo getCurrentProgramInfo(int x, int y, int w, int h) {
        PGraphics graphics = draw(mind, w, h);
        BufferedImage thumbnail = imageToBufferedImage(graphics.getImage(), x, y, w, h);
        String id = BallsProgram.class.getName();
        return new ProgramInfo(id, NAME, "Algorithm", thumbnail);
    }

    /**
     * Restarts.
     *
     * @param id currently ignored
     * @return the ProgramInfo.
     */
    @Override
    public ProgramInfo runProgram(String id) {
        t = 0;
        PGraphics graphics = draw(mind, 400, 100);
        BufferedImage thumbnail = imageToBufferedImage(graphics.getImage(), 0, 0, 400, 100);
        return new ProgramInfo(BallsProgram.class.getName(), NAME, "Algorithm", thumbnail);
    }
}
