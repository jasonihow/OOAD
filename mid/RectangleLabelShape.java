import java.awt.*;
import java.awt.geom.Rectangle2D;

class RectangleLabelShape implements LabelShape {
    @Override
    public void draw(Graphics2D g, int x, int y, int width, int height) {
        g.fillRect(x, y, width, height);
    }

    @Override
    public Rectangle getBounds(int x, int y, int width, int height) {
        return new Rectangle(x, y, width, height);
    }
}
