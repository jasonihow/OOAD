import java.awt.*;

class LineLink extends Link {
    public LineLink(Shape startShape, Point startPoint, Shape endShape, Point endPoint) {
        super(startShape, startPoint, endShape, endPoint);
    }

    @Override
    public void draw(Graphics2D g) {
        drawSelection(g);
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(2));
        g.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
    }
}
