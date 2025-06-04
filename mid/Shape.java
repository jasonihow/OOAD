import java.awt.*;
import java.util.*;
import java.util.List;

public abstract class Shape {
    Color defaultFillColor = Color.LIGHT_GRAY;
    Color defaultBorderColor = Color.DARK_GRAY;

    int x, y, width, height, depth;
    private Label label;
    private boolean isSelected = false;
    private boolean isDragging = false;
    Set<Point> connectedPorts = new HashSet<>();
    Color fillColor, borderColor;
    List<Point> connectionPoints = new ArrayList<>();

    public Shape(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.fillColor = defaultFillColor;
        this.borderColor = defaultBorderColor;
    }

    abstract void draw(Graphics2D g);

    abstract Rectangle getBounds();

    public Label getLabel() {
        return label;
    }

    public void setLabel(Label label) {
        this.label = label;
    }

    protected void drawLabel(Graphics2D g) {
        if (label != null) {
            Rectangle shapeBounds = getBounds();
            Font font = new Font("Arial", Font.PLAIN, label.getFontSize());
            FontMetrics fm = g.getFontMetrics(font);
            int textWidth = fm.stringWidth(label.getText());
            int textHeight = fm.getHeight();

            int labelWidth = Math.max(textWidth + 10, 30);
            int labelHeight = Math.max(textHeight + 5, 20);

            int labelCenterX = shapeBounds.x + shapeBounds.width / 2;
            int labelCenterY = shapeBounds.y + shapeBounds.height / 2;

            int labelX = labelCenterX - labelWidth / 2;
            int labelY = labelCenterY - labelHeight / 2;

            g.setColor(label.getBackgroundColor());
            label.getShape().draw(g, labelX, labelY, labelWidth, labelHeight);

            g.setColor(Color.BLACK);
            g.setFont(font);
            int textX = labelX + (labelWidth - textWidth) / 2;
            int textY = labelY + (labelHeight + fm.getAscent()) / 2 - fm.getDescent();
            g.drawString(label.getText(), textX, textY);
        }
    }

    protected abstract void updateConnectionPoints();

    void drawAllConnectionPoints(Graphics2D g) {
        g.setColor(Color.BLACK);
        for (Point point : connectionPoints) {
            g.fillRect(point.x - 3, point.y - 3, 6, 6);
        }
    }

    public void addConnectedPort(Point port) {
        connectedPorts.add(port);
    }

    public void deleteConnectedPort(Point port) {
        connectedPorts.remove(port);
    }

    void drawConnectionPoints(Graphics2D g) {
        g.setColor(Color.BLACK);
        for (Point port : connectedPorts) {
            g.fillRect(port.x - 3, port.y - 3, 6, 6);
        }
    }

    abstract boolean contains(int x, int y);

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isDragging() {
        return isDragging;
    }

    public void setDragging(boolean Dragging) {
        isDragging = Dragging;
    }

    public void removeAllConnectedPorts() {
        connectedPorts.clear();
    }

    public void translate(int dx, int dy) {
        // System.out.println("Translate: dx = " + dx + ", dy = " + dy + " for shape at
        // (" + x + ", " + y + ")");
        this.x += dx;
        this.y += dy;
        updateConnectionPoints();
        for (Link link : getConnectedLinks()) {
            link.updateEndpoints(this, dx, dy);
        }
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public Point getClosestConnectionPoint(int x, int y) {
        Point closest = null;
        double minDistSq = Double.MAX_VALUE;
        for (Point port : connectionPoints) {
            double distSq = (x - port.x) * (x - port.x) + (y - port.y) * (y - port.y);
            if (distSq < minDistSq) {
                minDistSq = distSq;
                closest = port;
            }
        }
        if (minDistSq <= 6 * 6) {
            return closest;
        }
        return null;
    }

    private List<Link> connectedLinks = new ArrayList<>();

    public void addConnectedLink(Link link) {
        connectedLinks.add(link);
    }

    public void removeConnectedLink(Link link) {
        connectedLinks.remove(link);
    }

    public List<Link> getConnectedLinks() {
        return connectedLinks;
    }
}
