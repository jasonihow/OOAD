import java.awt.*;
import java.util.*;
import java.util.List;

public class CompositeShape extends Shape {
    private List<Shape> children = new ArrayList<>();

    public CompositeShape(List<Shape> shapes) {
        super(0, 0, 0, 0);
        children.addAll(shapes);
        updateBounds();
    }

    @Override
    public void group(Shape shape) {
        children.add(shape);
        updateBounds();
        updateConnectionPoints();
    }

    @Override
    public void ungroup(Shape shape) {
        children.remove(shape);
        updateBounds();
        updateConnectionPoints();
    }

    public List<Shape> getChildren() {
        return children;
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        children.forEach(child -> child.setSelected(selected));
    }

    @Override
    void draw(Graphics2D g) {
        List<Shape> sortedChildren = new ArrayList<>(children);
        sortedChildren.sort(Comparator.comparingInt(s -> s.depth));
        for (Shape child : sortedChildren) {
            child.draw(g);
        }
        if (isSelected()) {
            g.setColor(Color.BLUE);
            g.setStroke(new BasicStroke(2));
            g.drawRect(x, y, width, height);
        }
    }

    @Override
    Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    private void updateBounds() {
        if (children.isEmpty()) {
            this.x = 0;
            this.y = 0;
            this.width = 0;
            this.height = 0;
            return;
        }

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (Shape child : children) {
            Rectangle bounds = child.getBounds();
            minX = Math.min(minX, bounds.x);
            minY = Math.min(minY, bounds.y);
            maxX = Math.max(maxX, bounds.x + bounds.width);
            maxY = Math.max(maxY, bounds.y + bounds.height);
        }

        this.x = minX;
        this.y = minY;
        this.width = maxX - minX;
        this.height = maxY - minY;
        updateConnectionPoints();
    }

    @Override
    protected void updateConnectionPoints() {
        connectionPoints.clear();
        if (children.isEmpty())
            return;

        connectionPoints.add(new Point(x, y + height / 2));
        connectionPoints.add(new Point(x + width / 2, y));
        connectionPoints.add(new Point(x + width, y + height / 2));
        connectionPoints.add(new Point(x + width / 2, y + height));
    }

    @Override
    boolean contains(int px, int py) {
        for (Shape child : children) {
            if (child.contains(px, py)) {
                return true;
            }
        }
        return getBounds().contains(px, py);
    }

    @Override
    public void translate(int dx, int dy) {
        for (Shape child : children) {
            child.translate(dx, dy);
        }
        super.translate(dx, dy);
        updateConnectionPoints();
    }
}
