package View;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import Model.*;

public class BoardPanel extends JPanel implements Observer {

    public static final int boardWidth = 20;
    public static final int boardHeight = 20;
    public static final int tileSize = 25;
    private Boolean grid;

    private Model m;

    public BoardPanel(Model m, Boolean grid) {
        this.grid = grid;
        this.m = m;
        this.m.addObserver(this);
        init();
    }

    private void init() {

        this.setPreferredSize(new Dimension(LstoPx(boardWidth), LstoPx(boardHeight)));
        this.setBackground(new Color(255, 255, 255));
        this.setBorder(BorderFactory.createLineBorder(Color.BLACK));

    }

    public int getTileSize() {
        return tileSize;
    }

    public void addGrid() {
        grid = true;
    }

    public void removeGrid() {
        grid = false;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        if (grid) {
            for (int x = LstoPx(0); x <= LstoPx(boardWidth); x += LstoPx(1)) {
                for (int y = LstoPx(0); y <= LstoPx(boardHeight); y += LstoPx(1)) {
                    boolean occupied = false;
                    for (IGizmo gizmo : m.getGizmos()) {
                        for (List<Integer> list : gizmo.getOccupiedSpace()) {
                            if (PxtoLs(x) == list.get(0) && PxtoLs(y) == list.get(1)) {
                                occupied = true;
                            }
                        }
                    }
                    if (occupied) {
                        g2.setColor(new Color(255, 107, 106));
                    } else {
                        g2.setColor(new Color(157, 255, 158));
                    }
                    g2.fillRect(x, y, LstoPx(1), LstoPx(1));
                    g2.setColor(Color.black);
                    g2.drawRect(x, y, LstoPx(1), LstoPx(1));
                }
            }
        }


        // Draw all the vertical lines
        for (CircleGizmo c : m.getCircles()) {
            int x = (LstoPx(c.getX()));
            int y = (LstoPx(c.getY()));

            g2.setColor(c.getColour());
            g2.fillOval(x, y, LstoPx(1), LstoPx(1));
        }

        for (SquareGizmo s : m.getSquares()) {
            int x = (LstoPx(s.getX()));
            int y = (LstoPx(s.getY()));

            g2.setColor(s.getColour());
            g2.fillRect(x, y, LstoPx(1), LstoPx(1));
        }

        for (TriangleGizmo s : m.getTriangles()) {
            int x = (LstoPx(s.getXpos()));
            int y = (LstoPx(s.getYpos()));
            int x2 = LstoPx(s.getX2());
            int x3 = LstoPx(s.getX3());
            int y2 = LstoPx(s.getY2());
            int y3 = LstoPx(s.getY3());
            int xpoints[] = {x, x2, x3};
            int ypoints[] = {y, y2, y3};
            int npoints = 3;
            g2.setColor(s.getColour());
            g2.fillPolygon(xpoints, ypoints, npoints);
        }

        for (AbsorberGizmo a : m.getAbsorbers()) {
            int x = (LstoPx(a.getX()));
            int y = (LstoPx(a.getY()));
            int x2 = (LstoPx(a.getX2()));
            int y2 = (LstoPx(a.getY2()));
            if (x2 < x) {
                int temp = x;
                x = x2;
                x2 = temp;
            }

            if (y2 < y) {
                int temp = y;
                y = y2;
                y2 = temp;
            }
            g2.setColor(a.getColour());
            g2.fillRect(x, y, x2 - x, y2 - y);
        }

        double quarterTile = ((double) tileSize) / 4;
        int halfTile = (int) Math.round(((double) tileSize) / 2);
        for (FlipperGizmo f : m.getFlippers()) {
            int x = LstoPx(f.getX());
            int y = LstoPx(f.getY());
            double angle = Math.toRadians(f.getAngle());
            g2.setColor(f.getColour());

            if (f.isLeft()) {
                switch (f.getState()) {
                    case TOP_LEFT:
                        g2.rotate(-angle, x + quarterTile, y + quarterTile);
                        g2.fillRoundRect(x, y, halfTile, LstoPx(2), tileSize * 2, tileSize / 2);
                        g2.rotate(angle, x + quarterTile, y + quarterTile);
                        break;
                    case TOP_RIGHT:
                        g2.rotate(-angle, x + 2 * tileSize - quarterTile, y + quarterTile);
                        g2.fillRoundRect(x, y, LstoPx(2), halfTile, tileSize / 2, tileSize * 2);
                        g2.rotate(angle, x + 2 * tileSize - quarterTile, y + quarterTile);
                        break;
                    case BOTTOM_RIGHT:
                        g2.rotate(-angle, x + 2 * tileSize - quarterTile, y + 2 * tileSize - quarterTile);
                        g2.fillRoundRect(x + tileSize + halfTile, y, halfTile, LstoPx(2), tileSize * 2, tileSize / 2);
                        g2.rotate(angle, x + 2 * tileSize - quarterTile, y + 2 * tileSize - quarterTile);
                        break;
                    case BOTTOM_LEFT:
                        g2.rotate(-angle, x + quarterTile, y + 2 * tileSize - quarterTile);
                        g2.fillRoundRect(x, y + tileSize + halfTile, LstoPx(2), halfTile, tileSize / 2, tileSize * 2);
                        g2.rotate(angle, x + quarterTile, y + 2 * tileSize - quarterTile);
                        break;
                }
            } else {
                switch (f.getState()) {
                    case TOP_RIGHT:
                        g2.rotate(angle, x + 2 * tileSize - quarterTile, y + quarterTile);
                        g2.fillRoundRect(x + tileSize + halfTile, y, halfTile, LstoPx(2), tileSize * 2, tileSize / 2);
                        g2.rotate(-angle, x + 2 * tileSize - quarterTile, y + quarterTile);
                        break;
                    case BOTTOM_RIGHT:
                        g2.rotate(angle, x + 2 * tileSize - quarterTile, y + 2 * tileSize - quarterTile);
                        g2.fillRoundRect(x, y + tileSize + halfTile, LstoPx(2), halfTile, tileSize / 2, tileSize * 2);
                        g2.rotate(-angle, x + 2 * tileSize - quarterTile, y + 2 * tileSize - quarterTile);
                        break;
                    case BOTTOM_LEFT:
                        g2.rotate(angle, x + quarterTile, y + 2 * tileSize - quarterTile);
                        g2.fillRoundRect(x, y, halfTile, LstoPx(2), tileSize * 2, tileSize / 2);
                        g2.rotate(-angle, x + quarterTile, y + 2 * tileSize - quarterTile);
                        break;
                    case TOP_LEFT:
                        g2.rotate(angle, x + quarterTile, y + quarterTile);
                        g2.fillRoundRect(x, y, LstoPx(2), halfTile, tileSize / 2, tileSize * 2);
                        g2.rotate(-angle, x + quarterTile, y + quarterTile);
                        break;
                }
            }
        }

        for (Ball b : m.getBalls()) {
            if (b != null) {
                g2.setColor(b.getColour());

                double x = (LstoPxDouble(b.getX()) - LstoPxDouble(b.getRadius()));
                double y = (LstoPxDouble(b.getY()) - LstoPxDouble(b.getRadius()));
                double width = LstoPxDouble(2 * b.getRadius());

                Ellipse2D.Double shape = new Ellipse2D.Double(x, y, width, width);
                g2.fill(shape);
            }
        }

    }


    public int LstoPx(int a) {
        return a * tileSize;
    }

    public int LstoPx(double a) {
        return (int) Math.round(LstoPxDouble(a));
    }

    public double LstoPxDouble(double a) {
        return a * tileSize;
    }

    public int PxtoLs(double a) {
        return (int) Math.floor(a / tileSize);
    }

    public double PxtoLsDouble(double a) {
        return a / tileSize;
    }

    @Override
    public void update(Observable o, Object arg) {
        repaint();
    }
}















