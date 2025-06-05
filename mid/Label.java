import java.awt.*;
import java.awt.geom.Rectangle2D;

class Label {
    private String text;
    private LabelShape shape;
    private Color backgroundColor;
    private int fontSize;

    public Label(String text) {
        this.text = text;
        this.shape = new RectangleLabelShape();
        this.backgroundColor = Color.WHITE;
        this.fontSize = 12;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public LabelShape getShape() {
        return shape;
    }

    public void setShape(LabelShape shape) {
        this.shape = shape;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public void draw(Graphics2D g, int x, int y, int width, int height) {
        g.setColor(backgroundColor);
        shape.draw(g, x, y, width, height);

        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.PLAIN, fontSize));
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getAscent();
        int textX = x + (width - textWidth) / 2;
        int textY = y + (height + textHeight) / 2 - fm.getDescent();
        g.drawString(text, textX, textY);
    }
}
