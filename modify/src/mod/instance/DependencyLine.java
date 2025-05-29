package mod.instance;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;

import javax.swing.JPanel;

import Define.AreaDefine;
import Pack.DragPack;
import bgWork.handler.CanvasPanelHandler;
import mod.IFuncComponent;
import mod.ILinePainter;
import java.lang.Math;

public class DependencyLine extends JPanel
        implements IFuncComponent, ILinePainter {
    JPanel from;
    int fromSide;
    Point fp = new Point(0, 0);
    JPanel to;
    int toSide;
    Point tp = new Point(0, 0);
    int arrowSize = 6;
    int panelExtendSize = 10;
    boolean isSelect = false;
    int selectBoxSize = 5;
    CanvasPanelHandler cph;

    public DependencyLine(CanvasPanelHandler cph) {
        this.setOpaque(false);
        this.setVisible(true);
        this.setMinimumSize(new Dimension(1, 1));
        this.cph = cph;
    }

    @Override
    public void paintComponent(Graphics g) {
        Point fpPrime;
        Point tpPrime;
        renewConnect();
        fpPrime = new Point(fp.x - this.getLocation().x,
                fp.y - this.getLocation().y);
        tpPrime = new Point(tp.x - this.getLocation().x,
                tp.y - this.getLocation().y);
        g.setColor(Color.BLACK);

        java.awt.Graphics2D g2 = (java.awt.Graphics2D) g;
        float[] dash = { 8.0f, 8.0f }; // 虛線樣式：8像素線、8像素空白
        java.awt.BasicStroke dashed = new java.awt.BasicStroke(
                isSelect ? 2.0f : 1.0f,
                java.awt.BasicStroke.CAP_BUTT,
                java.awt.BasicStroke.JOIN_MITER,
                10.0f, dash, 0.0f);
        java.awt.Stroke oldStroke = g2.getStroke();
        g2.setStroke(dashed);

        if (isSelect) {
            g2.setColor(Color.BLUE);
        } else {
            g2.setColor(Color.BLACK);
        }
        g2.drawLine(fpPrime.x, fpPrime.y, tpPrime.x, tpPrime.y);

        g2.setStroke(oldStroke); // 還原線條樣式
        paintArrow(g, tpPrime);
        if (isSelect == true) {
            System.out.println("paintSelect");
            paintSelect(g);
        }
    }

    @Override
    public void reSize() {
        Dimension size = new Dimension(
                Math.abs(fp.x - tp.x) + panelExtendSize * 2,
                Math.abs(fp.y - tp.y) + panelExtendSize * 2);
        this.setSize(size);
        this.setLocation(Math.min(fp.x, tp.x) - panelExtendSize,
                Math.min(fp.y, tp.y) - panelExtendSize);
    }

    @Override
    public void paintArrow(Graphics g, Point point) {
        int arrowWingLength = arrowSize * 2;

        double angle = Math.PI / 6; // 30 度

        g.setColor(Color.BLACK);

        int x1, y1, x2, y2;

        switch (toSide) {
            case 0: // TOP (箭頭朝上)
                x1 = point.x - (int) (arrowWingLength * Math.sin(angle));
                y1 = point.y + (int) (arrowWingLength * Math.cos(angle));
                g.drawLine(x1, y1, point.x, point.y);

                x2 = point.x + (int) (arrowWingLength * Math.sin(angle));
                y2 = point.y + (int) (arrowWingLength * Math.cos(angle));
                g.drawLine(x2, y2, point.x, point.y);
                break;

            case 1: // RIGHT (箭頭朝右)
                x1 = point.x - (int) (arrowWingLength * Math.cos(angle));
                y1 = point.y - (int) (arrowWingLength * Math.sin(angle));
                g.drawLine(x1, y1, point.x, point.y);

                x2 = point.x - (int) (arrowWingLength * Math.cos(angle));
                y2 = point.y + (int) (arrowWingLength * Math.sin(angle));
                g.drawLine(x2, y2, point.x, point.y);
                break;

            case 2: // LEFT (箭頭朝左)
                x1 = point.x + (int) (arrowWingLength * Math.cos(angle));
                y1 = point.y - (int) (arrowWingLength * Math.sin(angle));
                g.drawLine(x1, y1, point.x, point.y);

                x2 = point.x + (int) (arrowWingLength * Math.cos(angle));
                y2 = point.y + (int) (arrowWingLength * Math.sin(angle));
                g.drawLine(x2, y2, point.x, point.y);
                break;

            case 3: // BOTTOM (箭頭朝下)
                x1 = point.x - (int) (arrowWingLength * Math.sin(angle));
                y1 = point.y - (int) (arrowWingLength * Math.cos(angle));
                g.drawLine(x1, y1, point.x, point.y);

                x2 = point.x + (int) (arrowWingLength * Math.sin(angle));
                y2 = point.y - (int) (arrowWingLength * Math.cos(angle));
                g.drawLine(x2, y2, point.x, point.y);
                break;

            default:
                break;
        }
    }

    @Override
    public void setConnect(DragPack dPack) {
        Point mfp = dPack.getFrom();
        Point mtp = dPack.getTo();
        from = (JPanel) dPack.getFromObj();
        to = (JPanel) dPack.getToObj();
        fromSide = new AreaDefine().getArea(from.getLocation(), from.getSize(),
                mfp);
        toSide = new AreaDefine().getArea(to.getLocation(), to.getSize(), mtp);
        renewConnect();
        System.out.println("from side " + fromSide);
        System.out.println("to side " + toSide);
        ;
    }

    void renewConnect() {
        try {
            fp = getConnectPoint(from, fromSide);
            tp = getConnectPoint(to, toSide);
            this.reSize();
        } catch (NullPointerException e) {
            this.setVisible(false);
            cph.removeComponent(this);
        }
    }

    Point getConnectPoint(JPanel jp, int side) {
        Point temp = new Point(0, 0);
        Point jpLocation = cph.getAbsLocation(jp);
        if (side == new AreaDefine().TOP) {
            temp.x = (int) (jpLocation.x + jp.getSize().getWidth() / 2);
            temp.y = jpLocation.y;
        } else if (side == new AreaDefine().RIGHT) {
            temp.x = (int) (jpLocation.x + jp.getSize().getWidth());
            temp.y = (int) (jpLocation.y + jp.getSize().getHeight() / 2);
        } else if (side == new AreaDefine().LEFT) {
            temp.x = jpLocation.x;
            temp.y = (int) (jpLocation.y + jp.getSize().getHeight() / 2);
        } else if (side == new AreaDefine().BOTTOM) {
            temp.x = (int) (jpLocation.x + jp.getSize().getWidth() / 2);
            temp.y = (int) (jpLocation.y + jp.getSize().getHeight());
        } else {
            temp = null;
            System.err.println("getConnectPoint fail:" + side);
        }
        return temp;
    }

    int[] removeAt(int arr[], int index) {
        int temp[] = new int[arr.length - 1];
        for (int i = 0; i < temp.length; i++) {
            if (i < index) {
                temp[i] = arr[i];
            } else if (i >= index) {
                temp[i] = arr[i + 1];
            }
        }
        return temp;
    }

    @Override
    public void paintSelect(Graphics gra) {
        gra.setColor(Color.BLACK);
        gra.fillRect(fp.x, fp.y, selectBoxSize, selectBoxSize);
        gra.fillRect(tp.x, tp.y, selectBoxSize, selectBoxSize);
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean isSelect) {
        this.isSelect = isSelect;
    }

    public JPanel getFrom() {
        return from;
    }

    public int getFromSide() {
        return fromSide;
    }

    public JPanel getTo() {
        return to;
    }

    public int getToSide() {
        return toSide;
    }
}
