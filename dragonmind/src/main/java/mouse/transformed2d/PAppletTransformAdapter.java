package mouse.transformed2d;

import processing.core.PApplet;

public final class PAppletTransformAdapter implements TransformAdapter {

    private final PApplet pApplet;

    public PAppletTransformAdapter(PApplet pApplet) {
        this.pApplet = pApplet;
    }

    @Override
    public void pushMatrix() {
        pApplet.pushMatrix();
    }

    @Override
    public void popMatrix() {
        pApplet.popMatrix();
    }

    @Override
    public void translate(float offsetX, float offsetY) {
        pApplet.translate(offsetX, offsetY);
    }

    @Override
    public void scale(float scale) {
        pApplet.scale(scale);
    }

    @Override
    public void scale(float scaleX, float scaleY) {
        pApplet.scale(scaleX, scaleY);
    }

    @Override
    public void rotate(float angle) {
        pApplet.rotate(angle);
    }

    @Override
    public int getMouseX() {
        return pApplet.mouseX;
    }

    @Override
    public int getMouseY() {
        return pApplet.mouseY;
    }
}
