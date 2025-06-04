import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.awt.geom.AffineTransform;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Path2D;
import javax.swing.border.EmptyBorder;
import java.util.*;

public class WorkflowEditor extends JFrame {
    private String mode = "";
    private DrawingCanvas canvas;
    private RoundedButton selectedButton = null;

    public WorkflowEditor() {
        setTitle("Workflow Design Editor");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");

        JButton fileButton = new JButton("File");
        fileButton.setFocusPainted(false);
        fileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        fileMenu.add(fileButton);
        menuBar.add(fileMenu);

        JMenu editMenu = new JMenu("Edit");

        JButton labelButton = new JButton("Label");
        labelButton.setFocusPainted(false);
        labelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Shape selectedShape = canvas.getSelectedShape();
                if (selectedShape != null) {
                    Label currentLabel = selectedShape.getLabel();
                    CustomLabelStyleDialog dialog = new CustomLabelStyleDialog(WorkflowEditor.this, selectedShape,
                            currentLabel);
                    dialog.setVisible(true);

                    if (!dialog.isCancelled()) {
                        String labelName = dialog.getLabelName();
                        String labelShape = dialog.getLabelShape();
                        Color backgroundColor = dialog.getBackgroundColor();
                        int fontSize = dialog.getFontSize();

                        Label newLabel = new Label(labelName);
                        if (labelShape.equals("Rectangle")) {
                            newLabel.setShape(new RectangleLabelShape());
                        } else if (labelShape.equals("Oval")) {
                            newLabel.setShape(new OvalLabelShape());
                        }
                        newLabel.setBackgroundColor(backgroundColor);
                        newLabel.setFontSize(fontSize);

                        selectedShape.setLabel(newLabel);
                        canvas.repaint();
                    }
                } else {
                    JOptionPane.showMessageDialog(WorkflowEditor.this, "請先選擇一個基本物件。", "提示",
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        JButton groupButton = new JButton("Group");
        groupButton.setFocusPainted(false);
        groupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                canvas.groupSelectedShapes();
            }
        });
        JButton unGroupButton = new JButton("UnGroup");
        unGroupButton.setFocusPainted(false);
        unGroupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                canvas.unGroupSelectedShape();
            }
        });
        editMenu.add(labelButton);
        editMenu.add(groupButton);
        editMenu.add(unGroupButton);
        menuBar.add(editMenu);

        setJMenuBar(menuBar);

        JPanel toolbar = new JPanel();
        toolbar.setLayout(new GridLayout(6, 1, 5, 1));
        toolbar.setBackground(new Color(240, 240, 240));

        toolbar.setBorder(new EmptyBorder(1, 2, 1, 2));

        canvas = new DrawingCanvas();

        String[] buttonNames = { "Select", "Association", "Generalization", "Composition", "Rect", "Oval" };

        for (String name : buttonNames) {
            RoundedButton button = new RoundedButton(name);
            button.setPreferredSize(new Dimension(100, 40));

            button.addActionListener(e -> {
                mode = name;
                updateButtonColors(button);
                if (!mode.equals("Select")) {
                    canvas.unselectAllShapes();
                    repaint();
                }
            });

            toolbar.add(button);
        }

        add(toolbar, BorderLayout.WEST);
        add(canvas, BorderLayout.CENTER);
        setVisible(true);
    }

    private void updateButtonColors(RoundedButton clickedButton) {
        if (selectedButton != null) {
            selectedButton.setSelected(false);
        }
        clickedButton.setSelected(true);
        selectedButton = clickedButton;
    }

    public class DrawingCanvas extends JPanel {
        private List<Shape> shapes = new ArrayList<>();
        private Shape startShape = null;
        private Shape selectedShapeForDrag = null;
        private Point startPoint = null;
        private Point startPort = null;
        private Point endPoint = null;
        private boolean isDragging = false;
        private List<Link> links = new ArrayList<>();
        private Point dragStartPoint = null;
        private Point dragEndPoint = null;
        private boolean isDraggingSelection = false;
        private boolean shapeClicked = false;
        private int dragOffsetX, dragOffsetY;

        public DrawingCanvas() {
            MouseAdapter mouseHandler = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (mode.equals("Select")) {
                        shapeClicked = false;

                        for (int i = shapes.size() - 1; i >= 0; i--) {
                            Shape shape = shapes.get(i);
                            if (shape.contains(e.getX(), e.getY())) {
                                unselectAllShapes();
                                shape.setSelected(true);
                                selectedShapeForDrag = shape;
                                dragOffsetX = e.getX() - shape.x;
                                dragOffsetY = e.getY() - shape.y;
                                shapeClicked = true;
                                // System.out.println("depth: " + shape.depth);
                                break;
                            } else {
                                shape.setSelected(false);
                            }
                        }
                        if (!shapeClicked) {
                            unselectAllShapes();
                            dragStartPoint = e.getPoint();
                            isDraggingSelection = true;
                        }
                        repaint();
                    } else if (mode.matches("Association|Generalization|Composition")) {
                        for (int i = shapes.size() - 1; i >= 0; i--) {
                            Shape shape = shapes.get(i);
                            Point clickedPort = shape.getClosestConnectionPoint(e.getX(), e.getY());
                            if (clickedPort != null) {
                                startShape = shape;
                                startPoint = clickedPort;
                                startPort = clickedPort;

                                startShape.addConnectedPort(startPort);
                                isDragging = true;
                                shape.setDragging(true);
                                repaint();
                                return;
                            }
                        }

                        for (Shape s : shapes) {
                            s.setDragging(false);
                            // s.removeAllConnectedPorts();
                        }
                        startShape = null;
                        startPoint = null;
                        startPort = null;

                    } else if (mode.matches("Rect|Oval")) {
                        Shape newShape = createShape(mode, e.getX(), e.getY());
                        if (newShape != null) {
                            newShape.setDepth(shapes.size());
                            shapes.add(newShape);
                            repaint();
                        }
                    }
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (mode.equals("Select") && isDraggingSelection) {
                        dragEndPoint = e.getPoint();
                        repaint();
                    } else if (mode.equals("Select") && shapeClicked) {
                        selectedShapeForDrag.translate(e.getX() - selectedShapeForDrag.x - dragOffsetX,
                                e.getY() - selectedShapeForDrag.y - dragOffsetY);
                        repaint();
                    } else if (isDragging) {
                        endPoint = new Point(e.getX(), e.getY());
                        repaint();
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (mode.equals("Select") && isDraggingSelection) {
                        isDraggingSelection = false;
                        if (dragStartPoint != null && dragEndPoint != null) {
                            Rectangle selectionRect = createSelectionRectangle(dragStartPoint, dragEndPoint);
                            boolean foundFullyContainedShape = false;
                            for (Shape shape : shapes) {
                                if (selectionRect.contains(shape.getBounds())) {
                                    shape.setSelected(true);
                                    foundFullyContainedShape = true;
                                }
                            }
                            if (!foundFullyContainedShape) {
                                unselectAllShapes();
                            }
                            dragStartPoint = null;
                            dragEndPoint = null;
                            repaint();
                        }
                        selectedShapeForDrag = null;
                    } else if (isDragging) {
                        isDragging = false;
                        Point releasedPort = null;

                        for (int i = shapes.size() - 1; i >= 0; i--) {
                            Shape shape = shapes.get(i);
                            if (shape.contains(e.getX(), e.getY()) && shape != startShape) {
                                releasedPort = shape.getClosestConnectionPoint(e.getX(), e.getY());
                                if (releasedPort != null) {
                                    Link link = null;
                                    switch (mode) {
                                        case "Association":
                                            link = new AssociationLink(startShape, startPoint, shape, releasedPort);
                                            break;
                                        case "Generalization":
                                            link = new GeneralizationLink(startShape, startPoint, shape, releasedPort);
                                            break;
                                        case "Composition":
                                            link = new CompositionLink(startShape, startPoint, shape, releasedPort);
                                            break;
                                        default:
                                            link = null;
                                            break;
                                    }
                                    links.add(link);
                                    repaint();
                                    break;
                                }

                            } else {
                                shape.deleteConnectedPort(startPort);
                                shape.setDragging(false);
                                repaint();
                            }
                        }
                        startShape = null;
                        startPoint = null;
                        endPoint = null;
                    }
                }
            };
            addMouseListener(mouseHandler);
            addMouseMotionListener(mouseHandler);
        }

        public Shape getSelectedShape() {
            for (Shape shape : shapes) {
                if (shape.isSelected() && !(shape instanceof CompositeShape)) {
                    return shape;
                }
            }
            return null;
        }

        private void unselectAllShapes() {
            for (Shape shape : shapes) {
                shape.setSelected(false);
            }
        }

        private Rectangle createSelectionRectangle(Point p1, Point p2) {
            int x = Math.min(p1.x, p2.x);
            int y = Math.min(p1.y, p2.y);
            int width = Math.abs(p1.x - p2.x);
            int height = Math.abs(p1.y - p2.y);
            return new Rectangle(x, y, width, height);
        }

        private Shape createShape(String mode, int x, int y) {
            switch (mode) {
                case "Rect":
                    return new RectangleShape(x, y, 80, 50);
                case "Oval":
                    return new OvalShape(x, y, 80, 50);
                default:
                    return null;
            }
        }

        public void groupSelectedShapes() {
            List<Shape> selectedShapes = new ArrayList<>();
            List<Shape> nonSelectedShapes = new ArrayList<>();
            int minDepth = Integer.MAX_VALUE;

            for (Shape shape : shapes) {
                if (shape.isSelected()) {
                    selectedShapes.add(shape);
                    minDepth = Math.min(minDepth, shape.depth);
                } else {
                    nonSelectedShapes.add(shape);
                }
            }

            if (selectedShapes.size() >= 2) {
                CompositeShape composite = new CompositeShape(selectedShapes);
                composite.setDepth(minDepth);
                shapes.removeAll(selectedShapes);
                for (Shape shape : selectedShapes) {
                    shape.setSelected(false);
                }
                shapes.add(composite);
                unselectAllShapes();
                composite.setSelected(true);
                repaint();
            } else {
                JOptionPane.showMessageDialog(this, "請至少選擇兩個物件進行群組。", "提示", JOptionPane.WARNING_MESSAGE);
            }
        }

        public void unGroupSelectedShape() {
            Shape selectedComposite = null;
            for (Shape shape : shapes) {
                if (shape.isSelected() && shape instanceof CompositeShape) {
                    selectedComposite = shape;
                    break;
                }
            }

            if (selectedComposite != null) {
                CompositeShape composite = (CompositeShape) selectedComposite;
                shapes.remove(selectedComposite);
                shapes.addAll(composite.getChildren());
                shapes.sort(Comparator.comparingInt(s -> s.depth));
                unselectAllShapes();
                for (Shape child : composite.getChildren()) {
                    child.setSelected(true);
                }
                repaint();
            } else {
                JOptionPane.showMessageDialog(this, "請選擇一個群組物件進行取消群組。", "提示", JOptionPane.WARNING_MESSAGE);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, getWidth(), getHeight());

            for (Shape shape : shapes) {
                shape.draw(g2d);
            }

            for (Link link : links) {
                link.draw(g2d);
            }
            if (isDragging && endPoint != null) {
                g2d.setColor(Color.BLACK);
                g2d.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
            }
            if (isDraggingSelection && dragStartPoint != null && dragEndPoint != null) {
                g2d.setColor(new Color(0, 0, 255, 50));
                Rectangle selectionRect = createSelectionRectangle(dragStartPoint, dragEndPoint);
                g2d.fillRect(selectionRect.x, selectionRect.y, selectionRect.width, selectionRect.height);
                g2d.setColor(Color.BLUE);
                g2d.drawRect(selectionRect.x, selectionRect.y, selectionRect.width, selectionRect.height);
            }
        }
    }

    static class RoundedButton extends JButton {
        private boolean isSelected = false;

        public RoundedButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorder(new RoundedBorder(15));
        }

        public void setSelected(boolean selected) {
            isSelected = selected;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (isSelected) {
                g2.setColor(Color.BLACK);
            } else {
                g2.setColor(Color.WHITE);
            }
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

            g2.setColor(isSelected ? Color.WHITE : Color.BLACK);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() + fm.getAscent()) / 2 - 2;
            g2.drawString(getText(), x, y);

            g2.dispose();
        }
    }

    static class RoundedBorder extends AbstractBorder {
        private int radius;

        public RoundedBorder(int radius) {
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.GRAY);
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(4, 4, 4, 4);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.right = insets.top = insets.bottom = 4;
            return insets;
        }
    }

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

    class RectangleShape extends Shape {
        public RectangleShape(int x, int y, int width, int height) {
            super(x, y, width, height);
            connectionPoints.add(new Point(x, y + height / 2));
            connectionPoints.add(new Point(x + width / 2, y));
            connectionPoints.add(new Point(x + width, y + height / 2));
            connectionPoints.add(new Point(x + width / 2, y + height));
            connectionPoints.add(new Point(x, y));
            connectionPoints.add(new Point(x + width, y));
            connectionPoints.add(new Point(x, y + height));
            connectionPoints.add(new Point(x + width, y + height));
        }

        @Override
        protected void updateConnectionPoints() {
            connectionPoints.clear();
            connectionPoints.add(new Point(x, y + height / 2));
            connectionPoints.add(new Point(x + width / 2, y));
            connectionPoints.add(new Point(x + width, y + height / 2));
            connectionPoints.add(new Point(x + width / 2, y + height));
            connectionPoints.add(new Point(x, y));
            connectionPoints.add(new Point(x + width, y));
            connectionPoints.add(new Point(x, y + height));
            connectionPoints.add(new Point(x + width, y + height));
        }

        @Override
        void draw(Graphics2D g) {
            g.setColor(fillColor);
            g.fillRect(x, y, width, height);

            g.setColor(borderColor);
            g.drawRect(x, y, width, height);

            drawLabel(g);

            if (isSelected()) {
                drawAllConnectionPoints(g);
            } else if (isDragging()) {
                drawConnectionPoints(g);
            }

        }

        @Override
        public Rectangle getBounds() {
            return new Rectangle(x, y, width, height);
        }

        @Override
        boolean contains(int x, int y) {
            return x >= this.x && x <= this.x + width && y >= this.y && y <= this.y + height;
        }
    }

    class OvalShape extends Shape {
        public OvalShape(int x, int y, int width, int height) {
            super(x, y, width, height);
            connectionPoints.add(new Point(x, y + height / 2));
            connectionPoints.add(new Point(x + width / 2, y));
            connectionPoints.add(new Point(x + width, y + height / 2));
            connectionPoints.add(new Point(x + width / 2, y + height));
        }

        @Override
        protected void updateConnectionPoints() {
            connectionPoints.clear();
            connectionPoints.add(new Point(x, y + height / 2));
            connectionPoints.add(new Point(x + width / 2, y));
            connectionPoints.add(new Point(x + width, y + height / 2));
            connectionPoints.add(new Point(x + width / 2, y + height));
        }

        @Override
        void draw(Graphics2D g) {
            g.setColor(fillColor);
            g.fillOval(x, y, width, height);

            drawLabel(g);

            g.setColor(borderColor);
            g.drawOval(x, y, width, height);

            if (isSelected()) {
                drawAllConnectionPoints(g);
            } else if (isDragging()) {
                drawConnectionPoints(g);
            }
        }

        @Override
        public Rectangle getBounds() {
            return new Rectangle(x, y, width, height);
        }

        @Override
        public boolean contains(int x, int y) {
            double centerX = this.x + width / 2.0;
            double centerY = this.y + height / 2.0;
            double normalizedX = (x - centerX) / (width / 2.0);
            double normalizedY = (y - centerY) / (height / 2.0);
            return normalizedX * normalizedX + normalizedY * normalizedY <= 1.0;
        }
    }

    abstract class Link {
        Shape startShape, endShape;
        Point startPoint, endPoint;

        public Link(Shape startShape, Point startPoint, Shape endShape, Point endPoint) {
            this.startShape = startShape;
            this.startPoint = new Point(startPoint);
            this.endShape = endShape;
            this.endPoint = new Point(endPoint);
            startShape.addConnectedLink(this);
            endShape.addConnectedLink(this);
        }

        public void updateEndpoints(Shape movedShape, int dx, int dy) {
            // System.out.println("UpdateEndpoints for " + (movedShape == startShape ?
            // "start" : "end") + " shape, dx = " + dx + ", dy = " + dy);
            if (movedShape == startShape) {
                startPoint.x += dx;
                startPoint.y += dy;
            } else if (movedShape == endShape) {
                endPoint.x += dx;
                endPoint.y += dy;
            }
        }

        public abstract void draw(Graphics2D g);
    }

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

    public class CompositeShape extends Shape {
        private List<Shape> children = new ArrayList<>();

        public CompositeShape(List<Shape> shapes) {
            super(0, 0, 0, 0);
            children.addAll(shapes);
            updateBounds();
        }

        public void add(Shape shape) {
            children.add(shape);
            updateBounds();
            updateConnectionPoints();
        }

        public void remove(Shape shape) {
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

        public Rectangle getBounds(Graphics2D g, int x, int y, int width, int height) {
            Font font = new Font("Arial", Font.PLAIN, fontSize);
            FontMetrics fm = g.getFontMetrics(font);
            int textWidth = fm.stringWidth(text);
            int textHeight = fm.getHeight();
            return shape.getBounds(x, y, Math.max(width, textWidth + 10), Math.max(height, textHeight + 5));
        }
    }

    interface LabelShape {
        void draw(Graphics2D g, int x, int y, int width, int height);

        Rectangle getBounds(int x, int y, int width, int height);
    }

    class RectangleLabelShape implements LabelShape {
        @Override
        public void draw(Graphics2D g, int x, int y, int width, int height) {
            g.fillRect(x, y, width, height);
        }

        @Override
        public Rectangle getBounds(int x, int y, int width, int height) {
            return new Rectangle(x, y, width, height);
        }
    }

    class OvalLabelShape implements LabelShape {
        @Override
        public void draw(Graphics2D g, int x, int y, int width, int height) {
            g.fillOval(x, y, width, height);
        }

        @Override
        public Rectangle getBounds(int x, int y, int width, int height) {
            return new Rectangle(x, y, width, height);
        }
    }

    public static class CustomLabelStyleDialog extends JDialog {

        private JTextField labelNameField;
        private JComboBox<String> labelShapeComboBox;
        private JComboBox<Color> backgroundColorComboBox;
        private JComboBox<Integer> fontSizeComboBox;
        private Shape selectedShape;
        private Label currentLabel;
        private boolean isCancelled = true;

        private static final Map<Color, String> colorNameMap = new HashMap<>();

        static {
            colorNameMap.put(Color.BLACK, "黑色");
            colorNameMap.put(Color.WHITE, "白色");
            colorNameMap.put(Color.GRAY, "灰色");
            colorNameMap.put(Color.YELLOW, "黃色");
            colorNameMap.put(Color.BLUE, "藍色");
            colorNameMap.put(Color.LIGHT_GRAY, "淺灰色");
            colorNameMap.put(Color.DARK_GRAY, "深灰色");
            colorNameMap.put(Color.RED, "紅色");
            colorNameMap.put(Color.GREEN, "綠色");
            colorNameMap.put(Color.CYAN, "青色");
        }

        public CustomLabelStyleDialog(JFrame parent, Shape shape, Label label) {
            super(parent, "Custom Label Style", true);
            this.selectedShape = shape;
            this.currentLabel = label;

            labelNameField = new JTextField(label != null ? label.getText() : "");
            labelShapeComboBox = new JComboBox<>(new String[] { "Rectangle", "Oval" });
            Color[] availableColors = { Color.WHITE, Color.GRAY, Color.LIGHT_GRAY, Color.DARK_GRAY, Color.YELLOW,
                    Color.RED, Color.GREEN, Color.CYAN };
            backgroundColorComboBox = new JComboBox<>(availableColors);
            fontSizeComboBox = new JComboBox<>(new Integer[] { 10, 12, 14, 16, 18, 20 });

            if (label != null) {
                labelShapeComboBox
                        .setSelectedItem(label.getShape().getClass().getSimpleName().replace("LabelShape", ""));
                backgroundColorComboBox.setSelectedItem(label.getBackgroundColor());
                fontSizeComboBox.setSelectedItem(label.getFontSize());
            }
            backgroundColorComboBox.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                        boolean isSelected, boolean cellHasFocus) {
                    JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
                            cellHasFocus);
                    if (value instanceof Color) {
                        Color color = (Color) value;
                        label.setText(colorNameMap.getOrDefault(color, getColorHexString(color)));
                        label.setIcon(new ColorIcon(color, 16, 16));
                    }
                    return label;
                }
            });

            JPanel mainPanel = new JPanel(new GridLayout(0, 2, 5, 5));
            mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            mainPanel.add(new JLabel("Label Name:"));
            mainPanel.add(labelNameField);

            mainPanel.add(new JLabel("Label Shape:"));
            mainPanel.add(labelShapeComboBox);

            mainPanel.add(new JLabel("Background Color:"));
            mainPanel.add(backgroundColorComboBox);

            mainPanel.add(new JLabel("Font Size:"));
            mainPanel.add(fontSizeComboBox);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton okButton = new JButton("OK");
            JButton cancelButton = new JButton("Cancel");

            okButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    isCancelled = false;
                    setVisible(false);
                }
            });

            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setVisible(false);
                }
            });

            buttonPanel.add(okButton);
            buttonPanel.add(cancelButton);

            add(mainPanel, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);

            pack();
            setLocationRelativeTo(parent);
        }

        public boolean isCancelled() {
            return isCancelled;
        }

        public String getLabelName() {
            return labelNameField.getText();
        }

        public String getLabelShape() {
            return (String) labelShapeComboBox.getSelectedItem();
        }

        public Color getBackgroundColor() {
            return (Color) backgroundColorComboBox.getSelectedItem();
        }

        public int getFontSize() {
            return (Integer) fontSizeComboBox.getSelectedItem();
        }

        private String getColorHexString(Color color) {
            return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
        }

        static class ColorIcon implements Icon {
            private Color color;
            private int width;
            private int height;

            public ColorIcon(Color color, int width, int height) {
                this.color = color;
                this.width = width;
                this.height = height;
            }

            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                g.setColor(color);
                g.fillRect(x, y, width, height);
                g.setColor(Color.BLACK);
                g.drawRect(x, y, width - 1, height - 1);
            }

            @Override
            public int getIconWidth() {
                return width;
            }

            @Override
            public int getIconHeight() {
                return height;
            }
        }
    }

    public static void main(String[] args) {
        new WorkflowEditor();
    }
}
