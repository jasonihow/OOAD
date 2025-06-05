
import java.awt.*;

abstract class Link {
    Shape startShape, endShape;
    Point startPoint, endPoint;

    public Link(Shape startShape, Point startPoint, Shape endShape, Point endPoint) {
        this.startShape = startShape;
        this.startPoint = new Point(startPoint);
        this.endShape = endShape;
        this.endPoint = new Point(endPoint);
        startShape.addConnectedLink(this);
        endShape.addConnectedLink(this);
    }

    public void updateEndpoints(Shape movedShape, int dx, int dy) {
        if (movedShape == startShape) {
            startPoint.x += dx;
            startPoint.y += dy;
        } else if (movedShape == endShape) {
            endPoint.x += dx;
            endPoint.y += dy;
        }
    }

    public abstract void draw(Graphics2D g);
}
