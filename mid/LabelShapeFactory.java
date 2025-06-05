import java.awt.Color;

public class LabelShapeFactory {
    public static Label create(String labelName, String labelShape, Color backgroundColor, int fontSize) {
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
        return newLabel;
    }
}
