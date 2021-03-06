package Model;

import physics.Circle;
import physics.LineSegment;

import java.awt.Color;
import java.util.List;
import java.util.ArrayList;

public class AbsorberGizmo implements IGizmo {

    private String id;
    private int x;
    private int y;
    private int x2;
    private int y2;
    private Color colour;
    private List<List<Integer>> occupiedSpace = new ArrayList<>();
    private List<Ball> balls;

    public AbsorberGizmo(String id, int x, int y, int x2, int y2) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.x2 = x2;
        this.y2 = y2;
        balls = new ArrayList<>();
        colour = Color.CYAN;
        setOccupiedSpace(this.x, this.y, this.x2, this.y2);
    }

    public List<Circle> getCircles() {
        List<Circle> l = new ArrayList<>();
        l.add(new Circle(x, y, 0));
        l.add(new Circle(x + (x2 - x + 1), y, 0));
        l.add(new Circle(x, y + (y2 - y + 1), 0));
        l.add(new Circle(x + (x2 - x + 1), y + (y2 - y + 1), 0));
        return l;
    }

    public List<LineSegment> getLines() {
        List<LineSegment> l = new ArrayList<>();
        l.add(new LineSegment(x,
                y,
                x2 + 1,
                y
        ));
        l.add(new LineSegment(x,
                y,
                x,
                y2 + 1
        ));
        l.add(new LineSegment(x2 + 1,
                y,
                x2 + 1,
                y2 + 1
        ));
        l.add(new LineSegment(x,
                y2 + 1,
                x2 + 1,
                y2 + 1
        ));
        return l;
    }

    @Override
    public void setPos(int x, int y) {
        this.x = x;
        this.y = y;
        setOccupiedSpace(this.x, this.y, x2, y2);
    }

    public void setPos2(int x, int y) {
        this.x2 = x;
        this.y2 = y;
        setOccupiedSpace(this.x, this.y, x2, y2);
    }

    @Override
    public void trigger(Ball ball) {
        if (ball != null) {
            balls.add(ball);

            ball.setX(x2 - .5);
            ball.setY(y2 - .5);
            ball.setVelocity(0, 0);

            ball.stop();
        }
    }

    @Override
    public void action() {
        for (int i = balls.size() - 1; i >= 0; i--) {
            balls.get(i).setVelocity(0, -50);
            balls.get(i).move();
            balls.remove(i);
        }
    }

    @Override
    public String getId() {
        return id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public void rotate() {
    }

    public int getX2() {
        return x2;
    }

    public int getY2() {
        return y2;
    }

    public boolean isInside(Ball ball) {
        return (ball.getX() >= x && ball.getY() >= y && ball.getX() <= x2 && ball.getY() <= y2);
    }

    public Color getColour() {
        return colour;
    }

    public List<List<Integer>> getOccupiedSpace() {
        return occupiedSpace;
    }

    public void setOccupiedSpace(int x, int y, int x2, int y2) {
        occupiedSpace = new ArrayList<>();
        if(x2<x){
            int temp = x2;
            x2 = x;
            x = temp;
        }
        if(y2<y){
            int temp = y2;
            y2 = y;
            y = temp;
        }
            for (int i = x; i < x2; i++) {
                for (int j = y; j < y2; j++) {
                    List<Integer> contain = new ArrayList<>();
                    contain.add(i);
                    contain.add(j);
                    occupiedSpace.add(contain);
                }
            }
    }

}
