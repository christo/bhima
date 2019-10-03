package mouse.transformed2d;

import processing.core.PApplet;

import java.util.ArrayDeque;

import static processing.core.PApplet.*;

/**
 * This is a processing library used to transform the mouseTransformed matrix
 * and get the transformed mouseTransformed coordinates.
 * <p>
 * Note: for multiple sketch windows, multiple instances of the MouseTransformed class must be created.
 */

public class MouseTransformed {
    private TransformAdapter adapter;
    private ArrayDeque<MouseParameters> mouseStack = new ArrayDeque<>();

    public MouseTransformed(TransformAdapter adapter, PApplet resetter) {
        this.adapter = adapter;
        mouseStack.addLast(new MouseParameters());

        resetter.registerMethod("post", this);
    }

    public void post() {
        this.resetMouse();
    }

    /**
     * Pushes current transformation matrix onto the animation and mouseTransformed stack.
     *
     * @see <a href="https://processing.org/reference/pushMatrix_.html">Processing's pushMatrix()</a>
     */
    public void pushMatrix() {
        adapter.pushMatrix();
        final MouseParameters last = mouseStack.getLast();
        MouseParameters params = new MouseParameters(last.totalOffsetX, last.totalOffsetY, last.totalRotate, last.totalScaleX, last.totalScaleY);
        mouseStack.addLast(params);
    }

    /**
     * Pops current transformation matrix off the animation and mouseTransformed stack.
     *
     * @see <a href="https://processing.org/reference/popMatrix_.html">Processing's popMatrix()</a>
     */
    public void popMatrix() {
        if (mouseStack.size() > 1) {
            adapter.popMatrix();
            if (mouseStack.size() > 1) {
                mouseStack.removeLast();
            }
        }
    }


    /**
     * Translate current animation and mouseTransformed matrices.
     *
     * @param offsetX Offset matrix origin to the left/right
     * @param offsetY Offset matrix origin up/down
     * @see <a href="https://processing.org/reference/translate_.html">Processing's translate()</a>
     */
    public void translate(float offsetX, float offsetY) {
        adapter.translate(offsetX, offsetY);
        final MouseParameters last = mouseStack.getLast();
        last.totalOffsetX += cos((float) last.totalRotate) * (last.totalScaleX) * offsetX - sin((float) last.totalRotate) * (last.totalScaleY) * offsetY;
        last.totalOffsetY += cos((float) last.totalRotate) * (last.totalScaleX) * offsetY + sin((float) last.totalRotate) * (last.totalScaleY) * offsetX;
    }

    /**
     * Scale current animation and mouseTransformed matrices.
     *
     * @param scale Percentage to scale the transformation matrices
     * @see <a href="https://processing.org/reference/scale_.html">Processing's scale()</a>
     */
    public void scale(float scale) {
        adapter.scale(scale);
        mouseStack.getLast().totalScaleX *= scale;
        mouseStack.getLast().totalScaleY *= scale;
    }

    /**
     * Scale current animation and mouseTransformed matrices.
     *
     * @param scaleX Percentage to scale the transformation matrices in the X-axis
     * @param scaleY Percentage to scale the transformation matrices in the Y-axis
     * @see <a href="https://processing.org/reference/scale_.html">Processing's scale()</a>
     */
    public void scale(float scaleX, float scaleY) {
        adapter.scale(scaleX, scaleY);
        mouseStack.getLast().totalScaleX *= scaleX;
        mouseStack.getLast().totalScaleY *= scaleY;
    }

    /**
     * Rotate current animation and mouseTransformed matrices.
     *
     * @param angle Angle of rotation (in rad)
     * @see <a href="https://processing.org/reference/rotate_.html">Processing's rotate()</a>
     */
    public void rotate(double angle) {
        adapter.rotate((float) angle);
        mouseStack.getLast().totalRotate += angle;
    }

    private void resetMouse() {
        mouseStack.getLast().reset();
    }

    /**
     * Calculates the mouseTransformed X-coordinate in current matrix (transformed or not).
     * <p>Extension of mouseX keyword.
     *
     * @return Transformed cursor's X-coordinate
     * @see <a href="https://processing.org/reference/mouseX.html">Processing's mouseX</a>
     */
    public int mouseX() {
        return screenY(adapter.getMouseY(), adapter.getMouseX());
    }

    public int screenY(int y, int x) {
        final MouseParameters last = mouseStack.getLast();
        return parseInt(cos((float) last.totalRotate) * (1 / last.totalScaleX) * (x - parseInt(last.totalOffsetX))) + parseInt(sin((float) last.totalRotate) * (1 / last.totalScaleY) * (y - parseInt(last.totalOffsetY)));
    }

    /**
     * Calculates the mouseTransformed Y-coordinate in current matrix (transformed or not).
     * <p>
     * Extension of mouseY keyword.
     *
     * @return Transformed cursor's Y-coordinate
     * @see <a href="https://processing.org/reference/mouseY.html">Processing's mouseY</a>
     */
    public int mouseY() {
        return screenX(adapter.getMouseX(), adapter.getMouseX());
    }

    public int screenX(int x, int y) {
        final MouseParameters last = mouseStack.getLast();
        return parseInt(cos((float) last.totalRotate) * (1 / last.totalScaleY) * (y - parseInt(last.totalOffsetY))) - parseInt(sin((float) last.totalRotate) * (1 / last.totalScaleX) * (x - parseInt(last.totalOffsetX)));
    }

    private static final class MouseParameters {
        final float initialOffsetX, initialOffsetY, initialScaleX, initialScaleY;
        final double initialRotate;
        private float totalOffsetX;
        private float totalOffsetY;
        private float totalScaleX;
        private float totalScaleY;
        private double totalRotate;

        MouseParameters(float initialOffsetX, float initialOffsetY, double initialRotate, float initialScaleX, float initialScaleY) {
            this.initialOffsetX = initialOffsetX;
            this.initialOffsetY = initialOffsetY;
            this.initialRotate = initialRotate;
            this.initialScaleX = initialScaleX;
            this.initialScaleY = initialScaleY;
            reset();
        }

        MouseParameters() {
            initialOffsetX = 0;
            initialOffsetY = 0;
            initialRotate = 0;
            initialScaleX = 1;
            initialScaleY = 1;
        }

        public void reset() {
            totalOffsetX = initialOffsetX;
            totalOffsetY = initialOffsetY;
            totalRotate = initialRotate;
            totalScaleX = initialScaleX;
            totalScaleY = initialScaleY;
        }
    }
}


