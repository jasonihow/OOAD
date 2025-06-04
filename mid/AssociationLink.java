
import java.awt.*;

class AssociationLink extends Link {
    public AssociationLink(Shape startShape, Point startPoint, Shape endShape, Point endPoint) {
        super(startShape, startPoint, endShape, endPoint);
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(2));
        g.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
        drawAssociationArrow(g, startPoint, endPoint);
    }

    private void drawAssociationArrow(Graphics2D g2d, Point start, Point end) {
        double arrowSize = 10;
        double angle = Math.atan2(end.y - start.y, end.x - start.x);

        double angle1 = angle - Math.PI / 6;
        double angle2 = angle + Math.PI / 6;

        int x1 = (int) (end.x - arrowSize * Math.cos(angle1));
        int y1 = (int) (end.y - arrowSize * Math.sin(angle1));
        int x2 = (int) (end.x - arrowSize * Math.cos(angle2));
        int y2 = (int) (end.y - arrowSize * Math.sin(angle2));

        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));

        g2d.drawLine(end.x, end.y, x1, y1);
        g2d.drawLine(end.x, end.y, x2, y2);
    }
}
