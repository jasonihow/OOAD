import java.awt.*;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;

class Label {
    private String text;
    private LabelShape shape;
    private Color backgroundColor;
    private int fontSize;
    private boolean underlined;

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

    public boolean isUnderlined() {
        return underlined;
    }

    public void setUnderlined(boolean underlined) {
        this.underlined = underlined;
    }

    void drawText(Graphics2D g, int x, int baseline) {
        Font font = new Font("Arial", Font.PLAIN, fontSize);
        g.setFont(font);
        g.drawString(text, x, baseline);

        if (underlined && !text.isEmpty()) {
            FontMetrics metrics = g.getFontMetrics(font);
            LineMetrics lineMetrics = font.getLineMetrics(text, g.getFontRenderContext());
            int underlineY = Math.round(baseline + lineMetrics.getUnderlineOffset());
            int textWidth = metrics.stringWidth(text);
            int underlineThickness = Math.max(1, Math.round(lineMetrics.getUnderlineThickness()));
            for (int i = 0; i < underlineThickness; i++) {
                g.drawLine(x, underlineY + i, x + textWidth, underlineY + i);
            }
        }
    }

    public void draw(Graphics2D g, int x, int y, int width, int height) {
        g.setColor(backgroundColor);
        shape.draw(g, x, y, width, height);

        g.setColor(Color.BLACK);
        Font font = new Font("Arial", Font.PLAIN, fontSize);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics(font);
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getAscent();
        int textX = x + (width - textWidth) / 2;
        int textY = y + (height + textHeight) / 2 - fm.getDescent();
        drawText(g, textX, textY);
    }
}
