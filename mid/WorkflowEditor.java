import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WorkflowEditor extends JFrame {
    private String mode = "";
    private DrawingCanvas canvas;
    private RoundedButton selectedButton = null; // 記錄當前選中的按鈕

    public WorkflowEditor() {
        setTitle("Workflow Design Editor");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 建立工具列（左側按鈕區）
        JPanel toolbar = new JPanel();
        toolbar.setLayout(new GridLayout(6, 1, 5, 5));
        toolbar.setBackground(new Color(240, 240, 240));

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
        private Point startPoint = null;
        private Point endPoint = null;
        private boolean isDragging = false;
        private List<Link> links = new ArrayList<>();

        public DrawingCanvas() {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (mode.equals("Select")) {
                        boolean shapeClicked = false;
                        // 從最上層的物件開始檢查
                        for (int i = shapes.size() - 1; i >= 0; i--) {
                            Shape shape = shapes.get(i);
                            if (shape.contains(e.getX(), e.getY())) {
                                for (Shape s : shapes) {
                                    if (s != shape) {
                                        s.setSelected(false);
                                    }
                                }
                                shape.setSelected(true);
                                shapeClicked = true;
                                System.out.println("Clicked on shape at depth: " + shape.getDepth());
                                break; // 停止檢查其他物件
                            } else {
                                shape.setSelected(false);
                            }
                        }
                        if (!shapeClicked) {
                            for (Shape shape : shapes) {
                                shape.setSelected(false);
                            }
                        }
                        repaint();
                    } else if (mode.matches("Association|Ge neralization|Composition")) {
                        // 從最上層的物件開始檢查
                        for (int i = shapes.size() - 1; i >= 0; i--) {
                            Shape shape = shapes.get(i);
                            if (shape.contains(e.getX(), e.getY())) {
                                // System.out.println("Clicked on shape at depth: " + shape.getDepth());
                                startShape = shape;
                                startPoint = new Point(e.getX(), e.getY());
                                isDragging = true;
                                return; // 停止檢查其他物件
                            }
                        }
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
                    if (isDragging) {
                        endPoint = new Point(e.getX(), e.getY());
                        repaint();
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (isDragging) {
                        isDragging = false;
                        // 從最上層的物件開始檢查
                        for (int i = shapes.size() - 1; i >= 0; i--) {
                            Shape shape = shapes.get(i);
                            if (shape.contains(e.getX(), e.getY()) && shape != startShape) {
                                Link link = new Link(startShape, startPoint, shape, new Point(e.getX(), e.getY()), mode);
                                links.add(link);
                                repaint();
                                break; // 停止檢查其他物件
                            }
                        }
                        startShape = null;
                        startPoint = null;
                        endPoint = null;
                    }
                }

                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    // ... (其他繪製程式碼)
                    for (Link link : links) {
                        link.draw(g);
                    }
                    if (isDragging && endPoint != null) {
                        g.setColor(Color.BLACK);
                        g.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
                    }
                }

            });
        }

        // 🔹 工廠方法：根據 mode 建立不同形狀
        private Shape createShape(String mode, int x, int y) {

            return switch (mode) {
                case "Rect" -> new RectangleShape(x, y, 80, 50);
                case "Oval" -> new OvalShape(x, y, 80, 50);
                default -> null;
            };
        }


        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, getWidth(), getHeight());

            for (Shape shape : shapes) {
                shape.draw(g);
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

        abstract void draw(Graphics g);

        void drawConnectionPoints(Graphics g) {
            g.setColor(Color.BLUE); // 連接點顏色
            for (Point point : connectionPoints) {
                g.fillRect(point.x - 3, point.y - 3, 6, 6); // 繪製連接點
            }
        }

        abstract boolean contains(int x, int y);

        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(boolean selected) {
            isSelected = selected;
        }

        public int getDepth() {
            return depth;
        }

        public void setDepth(int depth) {
            this.depth = depth;
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
        void draw(Graphics g) {
            // 填滿矩形
            g.setColor(fillColor);
            g.fillRect(x, y, width, height);

            // 畫邊框
            g.setColor(borderColor);
            g.drawRect(x, y, width, height);

            if (isSelected()){
                // 繪製連接點
                drawConnectionPoints(g);
            }
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
        void draw(Graphics g) {
            // 填滿橢圓
            g.setColor(fillColor);
            g.fillOval(x, y, width, height);

            // 畫邊框
            g.setColor(borderColor);
            g.drawOval(x, y, width, height);

            if (isSelected()){
                // 繪製連接點
                drawConnectionPoints(g);
            }
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

    class Link {
        Shape startShape, endShape;
        Point startPoint, endPoint;

        public Link(Shape startShape, Point startPoint, Shape endShape, Point endPoint, String mode) {
            this.startShape = startShape;
            this.startPoint = startPoint;
            this.endShape = endShape;
            this.endPoint = endPoint;
            this.mode = mode;
        }

        public void draw(Graphics g) {
            g.setColor(Color.BLACK);
            g.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
        }
    }


    public static void main(String[] args) {
        new WorkflowEditor();
    }
}
