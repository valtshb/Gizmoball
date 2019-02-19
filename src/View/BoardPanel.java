package View;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.Observable;
import java.util.Observer;

import Model.Model;
import Model.CircleGizmo;
import Model.SquareGizmo;
import Model.FlipperGizmo;
import Model.AbsorberGizmo;
import Model.TriangleGizmo;
import Model.Ball;

public class BoardPanel extends JPanel implements Observer {

    private static final int boardWidth = 20;
    private static final int boardHeight = 20;
    private static final int tileSize = 25;

    //private JPanel board;
    protected Model m;

    public BoardPanel(Model m) {
        this.m = m;
        this.m.addObserver(this);
        init();
    }

    public void init() {

        this.setPreferredSize(new Dimension(LstoPx(boardWidth) + 1, LstoPx(boardHeight) + 1));
        this.setBackground(new Color(255, 255, 255));
        this.setBorder(BorderFactory.createLineBorder(Color.BLACK));

    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        // Draw all the vertical lines
        for (CircleGizmo c : m.getCircles()) {
            int x = (LstoPx(c.getXpos()));
            int y = (LstoPx(c.getYpos()));

            g2.setColor(Color.ORANGE);
            g2.fillOval(x, y, LstoPx(1), LstoPx(1));
        }

        for (SquareGizmo s : m.getSquare()) {
            int x = (LstoPx(s.getXpos()));
            int y = (LstoPx(s.getYpos()));

            g2.setColor(Color.RED);
            g2.fillRect(x, y, LstoPx(1), LstoPx(1));
        }

        for (TriangleGizmo s : m.getTriangles()) {
            int x = (LstoPx(s.getXpos()));
            int y = (LstoPx(s.getYpos()));
            int x2 = LstoPx(s.getX2());
            int x3 = LstoPx(s.getX3());
            int y2 = LstoPx(s.gety2());
            int y3 = LstoPx(s.gety3());
            int xpoints[] = {x, x2, x3};
            int ypoints[] = {y, y2, y3};
            int npoints = 3;
            g2.setColor(Color.blue);
            g2.fillPolygon(xpoints, ypoints, npoints);
        }

        for (AbsorberGizmo a : m.getAbsorber()) {
            int x = (LstoPx(a.getXpos()));
            int y = (LstoPx(a.getYpos()));
            int x2 = (LstoPx(a.getXpos2()));
            int y2 = (LstoPx(a.getYpos2()));
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
            g2.setColor(Color.CYAN);
            g2.fillRect(x, y, x2 - x, y2 - y);
        }

        double quarterTile = ((double) tileSize) / 4;
        int halfTile = (int) Math.round(((double) tileSize) / 2);
        for (FlipperGizmo f : m.getFlippers()) {
            int x;

            int y = LstoPx(f.getY());
            double angle = Math.toRadians(f.getAngle());
            g2.setColor(Color.green);

            if (f.isLeft()) {
                x = LstoPx(f.getX());

                g2.rotate(-angle, x + quarterTile, y + quarterTile);
                g2.fillRoundRect(x, y, halfTile, LstoPx(2), 50, 15);
                g2.rotate(angle, x + quarterTile, y + quarterTile);
            } else {
                x = LstoPx(f.getX() + 1 + 0.5);

                g2.rotate(angle, x + quarterTile, y + quarterTile);
                g2.fillRoundRect(x, y, halfTile, LstoPx(2), 50, 15);
                g2.rotate(-angle, x + quarterTile, y + quarterTile);
            }
        }

        for (Ball b : m.getBalls()) {
            if (b != null) {
                g2.setColor(Color.black);

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

    public int PxtoLs(int a) {
        return a / tileSize;
    }


    @Override
    public void update(Observable o, Object arg) {
        repaint();
    }
}















