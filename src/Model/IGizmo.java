package Model;

import physics.Circle;
import physics.LineSegment;

import java.awt.Color;
import java.util.List;

public interface IGizmo {
    List<Circle> getCircles();
    List<LineSegment> getLines();
    void setPos(int x, int y);
    void trigger(Ball ball);
    void action();
    String getId();
    List<List<Integer>> getOccupiedSpace();
    int getX();
    int getY();
    Color getColour();
    void rotate();
}
