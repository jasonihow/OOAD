import java.awt.*;
import java.util.Collections;
import java.util.List;

class RectangleShape extends Shape {
    public RectangleShape(int x, int y, int width, int height) {
        super(x, y, width, height);
        connectionPoints.add(new Point(x, y + height / 2));
        connectionPoints.add(new Point(x + width / 2, y));
        connectionPoints.add(new Point(x + width, y + height / 2));
        connectionPoints.add(new Point(x + width / 2, y + height));
        connectionPoints.add(new Point(x, y));
        connectionPoints.add(new Point(x + width, y));
        connectionPoints.add(new Point(x, y + height));
        connectionPoints.add(new Point(x + width, y + height));
    }

    @Override
    protected void updateConnectionPoints() {
        connectionPoints.clear();
        connectionPoints.add(new Point(x, y + height / 2));
        connectionPoints.add(new Point(x + width / 2, y));
        connectionPoints.add(new Point(x + width, y + height / 2));
        connectionPoints.add(new Point(x + width / 2, y + height));
        connectionPoints.add(new Point(x, y));
        connectionPoints.add(new Point(x + width, y));
        connectionPoints.add(new Point(x, y + height));
        connectionPoints.add(new Point(x + width, y + height));
    }

    @Override
    void draw(Graphics2D g) {
        g.setColor(fillColor);
        g.fillRect(x, y, width, height);

        g.setColor(borderColor);
        g.drawRect(x, y, width, height);

        drawLabel(g);

        if (isSelected()) {
            drawAllConnectionPoints(g);
        } else if (isDragging()) {
            drawConnectionPoints(g);
        }

    }

    @Override
    void group(Shape shape) {
        // No specific grouping logic for OvalShape
    }

    @Override
    void ungroup(Shape shape) {
        // No specific ungrouping logic for OvalShape
    }

    @Override
    List<Shape> getChildren() {
        return Collections.emptyList(); // RectangleShape does not have children
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    @Override
    boolean contains(int x, int y) {
        return x >= this.x && x <= this.x + width && y >= this.y && y <= this.y + height;
    }
}
