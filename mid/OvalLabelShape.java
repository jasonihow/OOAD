import java.awt.Graphics2D;
import java.awt.Rectangle;

class OvalLabelShape implements LabelShape {
    @Override
    public void draw(Graphics2D g, int x, int y, int width, int height) {
        g.fillOval(x, y, width, height);
    }
}
