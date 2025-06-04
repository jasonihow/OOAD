
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;

class CompositionLink extends Link {
    public CompositionLink(Shape startShape, Point startPoint, Shape endShape, Point endPoint) {
        super(startShape, startPoint, endShape, endPoint);
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(2));
        g.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
        drawCompositionArrow(g, startPoint, endPoint);
    }

    private void drawCompositionArrow(Graphics2D g2d, Point start, Point end) {
        double diamondSize = 10;
        double angle = Math.atan2(end.y - start.y, end.x - start.x);
        AffineTransform tx = new AffineTransform();

        double deltaX = 2 * diamondSize * Math.cos(angle);
        double deltaY = 2 * diamondSize * Math.sin(angle);
        tx.translate(end.x - deltaX, end.y - deltaY);
        tx.rotate(angle - Math.PI);

        Path2D diamondHead = new Path2D.Double();
        diamondHead.moveTo(0, 0);
        diamondHead.lineTo(-diamondSize, -diamondSize / 2);
        diamondHead.lineTo(-2 * diamondSize, 0);
        diamondHead.lineTo(-diamondSize, diamondSize / 2);
        diamondHead.closePath();

        g2d.setColor(Color.WHITE);
        g2d.fill(tx.createTransformedShape(diamondHead));
        g2d.setStroke(new BasicStroke(2));
        g2d.setColor(Color.GRAY);
        g2d.draw(tx.createTransformedShape(diamondHead));
    }
}
