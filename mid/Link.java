
import java.awt.*;
import java.awt.geom.Line2D;

abstract class Link {
    Shape startShape, endShape;
    Point startPoint, endPoint;
    private boolean selected;

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

    public boolean contains(int x, int y) {
        return Line2D.ptSegDist(startPoint.x, startPoint.y, endPoint.x, endPoint.y, x, y) <= 6;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    protected void drawSelection(Graphics2D g) {
        if (selected) {
            g.setColor(new Color(0, 120, 215));
            g.setStroke(new BasicStroke(7));
            g.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
        }
    }

    public abstract void draw(Graphics2D g);
}
