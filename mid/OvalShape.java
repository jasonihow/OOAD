import java.awt.*;
import java.util.Collections;
import java.util.List;

class OvalShape extends Shape {
    public OvalShape(int x, int y, int width, int height) {
        super(x, y, width, height);
        connectionPoints.add(new Point(x, y + height / 2));
        connectionPoints.add(new Point(x + width / 2, y));
        connectionPoints.add(new Point(x + width, y + height / 2));
        connectionPoints.add(new Point(x + width / 2, y + height));
    }

    @Override
    protected void updateConnectionPoints() {
        connectionPoints.clear();
        connectionPoints.add(new Point(x, y + height / 2));
        connectionPoints.add(new Point(x + width / 2, y));
        connectionPoints.add(new Point(x + width, y + height / 2));
        connectionPoints.add(new Point(x + width / 2, y + height));
    }

    @Override
    void draw(Graphics2D g) {
        g.setColor(fillColor);
        g.fillOval(x, y, width, height);

        drawLabel(g);

        g.setColor(borderColor);
        g.drawOval(x, y, width, height);

        if (isSelected()) {
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
        return Collections.emptyList(); // OvalShape does not have children
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    @Override
    public boolean contains(int x, int y) {
        double centerX = this.x + width / 2.0;
        double centerY = this.y + height / 2.0;
        double normalizedX = (x - centerX) / (width / 2.0);
        double normalizedY = (y - centerY) / (height / 2.0);
        return normalizedX * normalizedX + normalizedY * normalizedY <= 1.0;
    }
}
