import java.awt.*;

class RectangleLabelShape implements LabelShape {
    @Override
    public void draw(Graphics2D g, int x, int y, int width, int height) {
        g.fillRect(x, y, width, height);
    }
}
