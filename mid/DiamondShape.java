import java.awt.*;
import java.util.Collections;
import java.util.List;

class DiamondShape extends Shape {
    public DiamondShape(int x, int y, int width, int height) {
        super(x, y, width, height);
        updateConnectionPoints();
    }

    @Override
    protected void updateConnectionPoints() {
        connectionPoints.clear();
        connectionPoints.add(new Point(x + width / 2, y));
        connectionPoints.add(new Point(x + width, y + height / 2));
        connectionPoints.add(new Point(x + width / 2, y + height));
        connectionPoints.add(new Point(x, y + height / 2));
    }

    @Override
    void draw(Graphics2D g) {
        Polygon diamond = createDiamondPolygon();
        g.setColor(fillColor);
        g.fillPolygon(diamond);

        g.setColor(borderColor);
        g.drawPolygon(diamond);

        drawLabel(g);

        if (isSelected()) {
            drawAllConnectionPoints(g);
        } else if (isDragging()) {
            drawConnectionPoints(g);
        }
    }

    private Polygon createDiamondPolygon() {
        return new Polygon(
                new int[] { x + width / 2, x + width, x + width / 2, x },
                new int[] { y, y + height / 2, y + height, y + height / 2 },
                4);
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
        if (width <= 0 || height <= 0) {
            return false;
        }

        double normalizedX = Math.abs(x - (this.x + width / 2.0)) / (width / 2.0);
        double normalizedY = Math.abs(y - (this.y + height / 2.0)) / (height / 2.0);
        return normalizedX + normalizedY <= 1.0;
    }
}
