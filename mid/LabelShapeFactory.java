import java.awt.Color;

public class LabelShapeFactory {
    public static Label create(String labelName, String labelShape, Color backgroundColor, int fontSize,
            boolean underlined) {
        Label newLabel = new Label(labelName);
        switch (labelShape) {
            case "Rectangle":
                newLabel.setShape(new RectangleLabelShape());
                break;
            case "Oval":
                newLabel.setShape(new OvalLabelShape());
                break;
            default:
                throw new IllegalArgumentException("Unknown label shape: " + labelShape);
        }
        newLabel.setBackgroundColor(backgroundColor);
        newLabel.setFontSize(fontSize);
        newLabel.setUnderlined(underlined);
        return newLabel;
    }
}
