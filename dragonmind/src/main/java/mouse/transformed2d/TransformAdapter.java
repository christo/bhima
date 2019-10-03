package mouse.transformed2d;

public interface TransformAdapter {
    void pushMatrix();

    void popMatrix();

    void translate(float offsetX, float offsetY);

    void scale(float scale);

    void scale(float scaleX, float scaleY);

    void rotate(float angle);

    int getMouseX();

    int getMouseY();
}
