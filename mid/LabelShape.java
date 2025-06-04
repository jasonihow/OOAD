import java.awt.Graphics2D;
import java.awt.Rectangle;

interface LabelShape {
    void draw(Graphics2D g, int x, int y, int width, int height);

    Rectangle getBounds(int x, int y, int width, int height);
}
