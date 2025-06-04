import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;

class GeneralizationLink extends Link {
    public GeneralizationLink(Shape startShape, Point startPoint, Shape endShape, Point endPoint) {
        super(startShape, startPoint, endShape, endPoint);
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(2));
        g.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
        drawGeneralizationArrow(g, startPoint, endPoint);
    }

    private void drawGeneralizationArrow(Graphics2D g2d, Point start, Point end) {
        double generalizationArrowSize = 10;
        double angle = Math.atan2(end.y - start.y, end.x - start.x);
        AffineTransform tx = new AffineTransform();
        tx.translate(end.x, end.y);
        tx.rotate(angle);

        Path2D arrowHead = new Path2D.Double();
        arrowHead.moveTo(0, 0);
        arrowHead.lineTo(-generalizationArrowSize, -generalizationArrowSize / 2);
        arrowHead.lineTo(-generalizationArrowSize, generalizationArrowSize / 2);
        arrowHead.closePath();

        g2d.setColor(Color.WHITE);
        g2d.fill(tx.createTransformedShape(arrowHead));

        g2d.setStroke(new BasicStroke(2));
        g2d.setColor(Color.GRAY);
        g2d.draw(tx.createTransformedShape(arrowHead));

    }
}
