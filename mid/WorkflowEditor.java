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
    private RoundedButton selectedButton = null; // 記錄當前選中的按鈕

    public WorkflowEditor() {
        setTitle("Workflow Design Editor");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 建立菜單欄
        JMenuBar menuBar = new JMenuBar();

        // 建立 "File" 菜單
        JMenu fileMenu = new JMenu("File");
        // 這裡我們直接添加按鈕，但通常 File 菜單會包含菜單項 (JMenuItem)
        JButton fileButton = new JButton("File");
        fileButton.setFocusPainted(false); // 可選：去除按鈕獲得焦點時的虛線框
        fileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 在這裡添加 File 按鈕的邏輯
            }
        });
        fileMenu.add(fileButton);
        menuBar.add(fileMenu);

        // 建立 "Edit" 菜單
        JMenu editMenu = new JMenu("Edit");
        // 同樣，這裡直接添加按鈕
        JButton labelButton = new JButton("Label");
        labelButton.setFocusPainted(false); // 可選
        labelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        JButton groupButton = new JButton("Group");
        groupButton.setFocusPainted(false); // 可選
        groupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                canvas.groupSelectedShapes();
            }
        });
        JButton unGroupButton = new JButton("UnGroup");
        unGroupButton.setFocusPainted(false); // 可選
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

        // 將菜單欄設置到 JFrame
        setJMenuBar(menuBar);

        // 建立工具列（左側按鈕區）
        JPanel toolbar = new JPanel();
        toolbar.setLayout(new GridLayout(6, 1, 5, 1));
        toolbar.setBackground(new Color(240, 240, 240));

        toolbar.setBorder(new EmptyBorder(1, 2, 1, 2));

        // 建立畫布
        canvas = new DrawingCanvas();

        // 按鈕名稱
        String[] buttonNames = {"Select", "Association", "Generalization", "Composition", "Rect", "Oval"};

        for (String name : buttonNames) {
            RoundedButton button = new RoundedButton(name);
            button.setPreferredSize(new Dimension(100, 40));

            // 設定按鈕點擊事件，切換模式並改變顏色
            button.addActionListener(e -> {
                mode = name;
                updateButtonColors(button);
                if (!mode.equals("Select")) {
                    canvas.unselectAllShapes(); // 取消所有選取
                    repaint();
                }
            });

            toolbar.add(button);
        }

        add(toolbar, BorderLayout.WEST);
        add(canvas, BorderLayout.CENTER);
        setVisible(true);
    }

    // 更新按鈕顏色（選中的變黑，其他變回白色）
    private void updateButtonColors(RoundedButton clickedButton) {
        if (selectedButton != null) {
            selectedButton.setSelected(false); // 取消舊的選中狀態
        }
        clickedButton.setSelected(true);
        selectedButton = clickedButton; // 更新選取的按鈕
    }

    // 畫布類別
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
                        // 從最上層的物件開始檢查
                        for (int i = shapes.size() - 1; i >= 0; i--) {
                            Shape shape = shapes.get(i);
                            if (shape.contains(e.getX(), e.getY())) {
                                unselectAllShapes();
                                shape.setSelected(true);
                                selectedShapeForDrag = shape;
                                dragOffsetX = e.getX() - shape.x; // 計算點擊位置與形狀左上角的 X 偏移量
                                dragOffsetY = e.getY() - shape.y;
                                shapeClicked = true;
                                //System.out.println("depth: " + shape.depth);
                                break; // 停止檢查其他物件
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
                        // 從最上層的物件開始檢查
                        for (int i = shapes.size() - 1; i >= 0; i--) {
                            Shape shape = shapes.get(i);
                            Point clickedPort = shape.getClosestConnectionPoint(e.getX(), e.getY());
                            if (clickedPort != null) {
                                startShape = shape;
                                startPoint = clickedPort; // 記錄起始連接點
                                startPort = clickedPort;

                                startShape.addConnectedPort(startPort);
                                isDragging = true;
                                shape.setDragging(true);
                                repaint();
                                return; // 停止檢查其他物件
                            }
                        }
                        // 清除拖曳起始標記和點擊的連接埠
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
                            newShape.setDepth(shapes.size()); // 設定深度
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
                        selectedShapeForDrag.translate(e.getX() - selectedShapeForDrag.x - dragOffsetX, e.getY() - selectedShapeForDrag.y - dragOffsetY);
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
                        selectedShapeForDrag = null; // 釋放選中的形狀
                    }
                    else if (isDragging) {
                        isDragging = false;
                        Point releasedPort = null;
                        // 從最上層的物件開始檢查
                        for (int i = shapes.size() - 1; i >= 0; i--) {
                            Shape shape = shapes.get(i);
                            if (shape.contains(e.getX(), e.getY()) && shape != startShape) {
                                releasedPort = shape.getClosestConnectionPoint(e.getX(), e.getY());
                                if (releasedPort != null) {
                                    Link link = switch (mode) {
                                        case "Association" ->
                                                new AssociationLink(startShape, startPoint, shape, releasedPort);
                                        case "Generalization" ->
                                                new GeneralizationLink(startShape, startPoint, shape, releasedPort);
                                        case "Composition" ->
                                                new CompositionLink(startShape, startPoint, shape, releasedPort);
                                        default -> null;
                                    };
                                    links.add(link);
                                    repaint();
                                    break;
                                }

                            }
                            else {
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

        // 🔹 工廠方法：根據 mode 建立不同形狀
        private Shape createShape(String mode, int x, int y) {

            return switch (mode) {
                case "Rect" -> new RectangleShape(x, y, 80, 50);
                case "Oval" -> new OvalShape(x, y, 80, 50);
                default -> null;
            };
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
                // 移除原先選取的物件
                shapes.removeAll(selectedShapes);
                // 確保子物件不再被選取
                for (Shape shape : selectedShapes) {
                    shape.setSelected(false);
                }
                // 加入新的 Composite 物件
                shapes.add(composite);
                // 取消所有物件的選取狀態，並選取新的 Composite 物件
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
                // 移除 Composite 物件
                shapes.remove(selectedComposite);
                // 將 Composite 物件中的子物件加入到 shapes 列表
                shapes.addAll(composite.getChildren());
                // 排序 shapes 列表，根據 depth 從小到大
                shapes.sort(Comparator.comparingInt(s -> s.depth));
                // 取消所有物件的選取狀態，並選取剛解散的子物件
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
            // 繪製選框
            if (isDraggingSelection && dragStartPoint != null && dragEndPoint != null) {
                g2d.setColor(new Color(0, 0, 255, 50)); // 半透明藍色
                Rectangle selectionRect = createSelectionRectangle(dragStartPoint, dragEndPoint);
                g2d.fillRect(selectionRect.x, selectionRect.y, selectionRect.width, selectionRect.height);
                g2d.setColor(Color.BLUE);
                g2d.drawRect(selectionRect.x, selectionRect.y, selectionRect.width, selectionRect.height);
            }
        }
    }

    // **自訂圓角按鈕**
    static class RoundedButton extends JButton {
        private boolean isSelected = false;

        public RoundedButton(String text) {
            super(text);
            setContentAreaFilled(false); // 讓背景透明，自己畫
            setFocusPainted(false);
            setBorder(new RoundedBorder(15));
        }

        public void setSelected(boolean selected) {
            isSelected = selected;
            repaint(); // 重新繪製按鈕
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (isSelected) {
                g2.setColor(Color.BLACK); // 選取時黑色
            } else {
                g2.setColor(Color.WHITE); // 預設白色
            }
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

            g2.setColor(isSelected ? Color.WHITE : Color.BLACK); // 文字顏色
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() + fm.getAscent()) / 2 - 2;
            g2.drawString(getText(), x, y);

            g2.dispose();
        }
    }

    // 自訂圓角邊框類別
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

    // 🟢 抽象類別，讓所有形狀都能共用 x, y, width, height
    // 🟢 抽象類別，加入 fillColor 和 borderColor
    abstract class Shape {
        // 🟢 所有 Shape 共享這些顏色
        static Color defaultFillColor = Color.LIGHT_GRAY;
        static Color defaultBorderColor = Color.DARK_GRAY;

        int x, y, width, height, depth;
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
            this.fillColor = defaultFillColor;  // 預設填充顏色
            this.borderColor = defaultBorderColor;  // 預設邊框顏色
        }

        abstract void draw(Graphics2D g);

        abstract Rectangle getBounds();

        protected abstract void updateConnectionPoints();

        void drawAllConnectionPoints(Graphics2D g) {
            g.setColor(Color.BLACK); // 連接點顏色
            for (Point point : connectionPoints) {
                g.fillRect(point.x - 3, point.y - 3, 6, 6); // 繪製連接點
            }
        }

        public void addConnectedPort(Point port) {
            connectedPorts.add(port);
        }

        public void deleteConnectedPort(Point port) {
            connectedPorts.remove(port);
        }

        void drawConnectionPoints(Graphics2D g) {
            g.setColor(Color.BLACK); // 連接點顏色
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
            System.out.println("Translate: dx = " + dx + ", dy = " + dy + " for shape at (" + x + ", " + y + ")");
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
            // 可以設定一個點擊連接點的容忍範圍 (例如半徑 5 個像素)
            if (minDistSq <= 6 * 6) {
                return closest;
            }
            return null;
        }
        // 新增方法來管理連接的 Link
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


    // 🟢 矩形
    class RectangleShape extends Shape {
        public RectangleShape(int x, int y, int width, int height) {
            super(x, y, width, height);
            // 新增矩形的連接點
            connectionPoints.add(new Point(x, y + height / 2)); // 左邊中點
            connectionPoints.add(new Point(x + width / 2, y)); // 上邊中點
            connectionPoints.add(new Point(x + width, y + height / 2)); // 右邊中點
            connectionPoints.add(new Point(x + width / 2, y + height)); // 下邊中點
            connectionPoints.add(new Point(x, y)); // 左上角
            connectionPoints.add(new Point(x + width, y)); // 右上角
            connectionPoints.add(new Point(x, y + height)); // 左下角
            connectionPoints.add(new Point(x + width, y + height)); // 右下角
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
            // 填滿矩形
            g.setColor(fillColor);
            g.fillRect(x, y, width, height);

            // 畫邊框
            g.setColor(borderColor);
            g.drawRect(x, y, width, height);

            if (isSelected()){
                // 繪製連接點
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

    // 🟢 橢圓
    class OvalShape extends Shape {
        public OvalShape(int x, int y, int width, int height) {
            super(x, y, width, height);
            // 新增橢圓的連接點
            connectionPoints.add(new Point(x, y + height / 2)); // 左邊中點
            connectionPoints.add(new Point(x + width / 2, y)); // 上邊中點
            connectionPoints.add(new Point(x + width, y + height / 2)); // 右邊中點
            connectionPoints.add(new Point(x + width / 2, y + height)); // 下邊中點
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
            // 填滿橢圓
            g.setColor(fillColor);
            g.fillOval(x, y, width, height);

            // 畫邊框
            g.setColor(borderColor);
            g.drawOval(x, y, width, height);

            if (isSelected()){
                // 繪製連接點
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
            // 使用橢圓方程式進行精確包含檢查
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
            this.startPoint = new Point(startPoint); // 使用傳入的 Point 創建新的 Point 物件
            this.endShape = endShape;
            this.endPoint = new Point(endPoint);   // 使用傳入的 Point 創建新的 Point 物件
            startShape.addConnectedLink(this);
            endShape.addConnectedLink(this);
        }
        public void updateEndpoints(Shape movedShape, int dx, int dy) {
            System.out.println("UpdateEndpoints for " + (movedShape == startShape ? "start" : "end") + " shape, dx = " + dx + ", dy = " + dy);
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

            // 計算箭頭兩個端點的角度 (相對於連接線的角度 +/- 45 度)
            double angle1 = angle - Math.PI / 6; // -45 度
            double angle2 = angle + Math.PI / 6; // +45 度

            // 計算箭頭兩個端點的座標
            int x1 = (int) (end.x - arrowSize * Math.cos(angle1));
            int y1 = (int) (end.y - arrowSize * Math.sin(angle1));
            int x2 = (int) (end.x - arrowSize * Math.cos(angle2));
            int y2 = (int) (end.y - arrowSize * Math.sin(angle2));

            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2)); // 設定線條粗細

            // 繪製兩條箭頭線
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

            g2d.setColor(Color.WHITE); // 空心箭頭，所以填充白色
            g2d.fill(tx.createTransformedShape(arrowHead));

            g2d.setStroke(new BasicStroke(2)); // 設定邊框粗細為 2
            g2d.setColor(Color.GRAY); // 畫出箭頭的邊框
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

            // 計算菱形前端應該在的位置
            double deltaX = 2 * diamondSize * Math.cos(angle);
            double deltaY = 2 * diamondSize * Math.sin(angle);
            tx.translate(end.x - deltaX, end.y - deltaY); // 將變換平移到菱形前端
            tx.rotate(angle - Math.PI);

            Path2D diamondHead = new Path2D.Double();
            diamondHead.moveTo(0, 0);
            diamondHead.lineTo(-diamondSize, -diamondSize / 2);
            diamondHead.lineTo(-2 * diamondSize, 0);
            diamondHead.lineTo(-diamondSize, diamondSize / 2);
            diamondHead.closePath();

            g2d.setColor(Color.WHITE); // 實心菱形箭頭，填充白色
            g2d.fill(tx.createTransformedShape(diamondHead));
            g2d.setStroke(new BasicStroke(2)); // 設定邊框粗細為 2
            g2d.setColor(Color.GRAY);
            g2d.draw(tx.createTransformedShape(diamondHead));
        }
    }

    public class CompositeShape extends Shape {
        private List<Shape> children = new ArrayList<>();

        public CompositeShape(List<Shape> shapes) {
            // 計算 CompositeShape 的邊界，這裡先設定為 0, 0, 0, 0，稍後會更新
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
            // 對 children 列表根據 depth 進行排序
            List<Shape> sortedChildren = new ArrayList<>(children);
            sortedChildren.sort(Comparator.comparingInt(s -> s.depth));
            // 繪製排序後的子物件
            for (Shape child : sortedChildren) {
                child.draw(g);
            }
            // 如果 CompositeShape 被選中，繪製其邊框
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
            updateConnectionPoints(); // 邊界更新後需要更新連接點
        }

        @Override
        protected void updateConnectionPoints() {
            connectionPoints.clear();
            if (children.isEmpty()) return;

            // 這裡可以定義 CompositeShape 的連接點邏輯，例如取其邊界的中點
            connectionPoints.add(new Point(x, y + height / 2)); // 左邊中點
            connectionPoints.add(new Point(x + width / 2, y)); // 上邊中點
            connectionPoints.add(new Point(x + width, y + height / 2)); // 右邊中點
            connectionPoints.add(new Point(x + width / 2, y + height)); // 下邊中點
        }

        @Override
        boolean contains(int px, int py) {
            for (Shape child : children) {
                if (child.contains(px, py)) {
                    return true;
                }
            }
            return getBounds().contains(px, py); // 也檢查是否包含 CompositeShape 的邊界
        }

        @Override
        public void translate(int dx, int dy) {
            for (Shape child : children) {
                child.translate(dx, dy);
            }
            super.translate(dx, dy); // 同時平移 CompositeShape 的邊界
            updateConnectionPoints();
        }
    }


    public static void main(String[] args) {
        new WorkflowEditor();
    }
}
