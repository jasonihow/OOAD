import java.awt.*;
import java.util.Collections;
import java.util.List;

class TriangleShape extends Shape {
    public TriangleShape(int x, int y, int width, int height) {
        super(x, y, width, height);
        updateConnectionPoints();
    }

    @Override
    protected void updateConnectionPoints() {
        connectionPoints.clear();
        connectionPoints.add(new Point(x + width / 2, y));
        connectionPoints.add(new Point(x + width / 4, y + height / 2));
        connectionPoints.add(new Point(x + 3 * width / 4, y + height / 2));
        connectionPoints.add(new Point(x, y + height));
        connectionPoints.add(new Point(x + width / 2, y + height));
        connectionPoints.add(new Point(x + width, y + height));
    }

    @Override
    void draw(Graphics2D g) {
        Polygon triangle = createTrianglePolygon();
        g.setColor(fillColor);
        g.fillPolygon(triangle);

        g.setColor(borderColor);
        g.drawPolygon(triangle);

        drawLabel(g);

        if (isSelected()) {
            drawAllConnectionPoints(g);
        } else if (isDragging()) {
            drawConnectionPoints(g);
        }
    }

    private Polygon createTrianglePolygon() {
        return new Polygon(
                new int[] { x + width / 2, x + width, x },
                new int[] { y, y + height, y + height },
                3);
    }

    @Override
    void group(Shape shape) {
    }

    @Override
    void ungroup(Shape shape) {
    }

    @Override
    List<Shape> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    @Override
    boolean contains(int x, int y) {
        return width > 0 && height > 0 && createTrianglePolygon().contains(x, y);
    }
}
