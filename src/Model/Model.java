package Model;

import org.junit.Ignore;
import physics.Circle;
import physics.LineSegment;
import physics.Vect;
import physics.Geometry.VectPair;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import static physics.Geometry.*;

public class Model extends Observable {

    private static final double moveTime = 0.05;
    private static final int gridSizeX = 20;
    private static final int gridSizeY = 20;
    private static final int ballMass = 1;
    private static final int maxGravity = 30;
    private static final int maxFriction = 1;
    private static double mu = 0.025;
    private static double mu2 = 0.025;
    private static double gravity = 25;


    private List<IGizmo> iGizmos;
    private List<LineSegment> walls;
    private List<Ball> balls;

    private List<Connection> connections;
    private List<KeyConnection> keyConnections;

    public Model() {
        iGizmos = new ArrayList<>();
        walls = new ArrayList<>();
        balls = new ArrayList<>();

        connections = new ArrayList<>();
        keyConnections = new ArrayList<>();

        walls.add(new LineSegment(0, 0, gridSizeX, 0));
        walls.add(new LineSegment(0, 0, 0, gridSizeY));
        walls.add(new LineSegment(gridSizeX, 0, gridSizeX, gridSizeY));
        walls.add(new LineSegment(0, gridSizeY, gridSizeX, gridSizeY));
    }

    public void moveBalls() {
        double moveTime = Model.moveTime;
        CollisionDetails cd = null;

        for (Ball ball : balls) {
            if (ball.isMoving()) {
                CollisionDetails cd_ = timeUntilCollision(ball);
                if (cd == null || cd.getTuc() > cd_.getTuc())
                    cd = cd_;
            }
        }

        double tuc = cd == null ? moveTime : cd.getTuc();

        moveFlippersForTime(tuc > moveTime ? moveTime : tuc);

        for (Ball ball : balls) {
            if (ball.isMoving()) {
                if (tuc > moveTime) {
                    moveBallForTime(ball, moveTime);

                    friction(ball, moveTime);
                    gravity(ball, moveTime);
                } else {
                    if (cd.hasCollision(ball)) {
                        if (cd.isBallToBallCollision()) {
                            if (cd.getBall_() == ball)
                                continue;
                            Ball ball_ = cd.getBall_();
                            moveBallForTime(ball, tuc);
                            moveBallForTime(ball_, tuc);

                            ball.setVelocity(cd.getVelocityPair().v1);
                            ball_.setVelocity(cd.getVelocityPair().v2);

                            friction(ball, tuc);
                            friction(ball_, tuc);
                            gravity(ball, tuc);
                            gravity(ball_, tuc);
                        } else {
                            moveBallForTime(ball, tuc);

                            ball.setVelocity(cd.getVelocity());

                            friction(ball, tuc);
                            gravity(ball, tuc);

                            if (cd.getGizmo() != null) {
                                cd.getGizmo().trigger(ball);
                                for (Connection connection : connections)
                                    if (connection.getTrigger() == cd.getGizmo())
                                        connection.triggered();
                            }
                        }
                    } else {
                        moveBallForTime(ball, tuc);

                        friction(ball, tuc);
                        gravity(ball, tuc);
                    }
                }
            }
        }
        this.setChanged();
        this.notifyObservers();
    }


    private CollisionDetails timeUntilCollision(Ball ball) {
        Circle ballCircle = ball.getCircle();
        Vect ballVelocity = ball.getVelocity();
        Vect newVelo = new Vect(0.0D, 0.0D);

        double shortestTime = Double.MAX_VALUE;
        double time;
        IGizmo gizmo = null;

        for (IGizmo iIGizmo : iGizmos) {
            // Ignore collisions if ball is inside Absorber
            if (iIGizmo instanceof AbsorberGizmo && ((AbsorberGizmo) iIGizmo).isInside(ball))
                continue;

            if (iIGizmo instanceof FlipperGizmo && ((FlipperGizmo) iIGizmo).isMoving()) {
                // Moving Flipper physics
                double angularVelocity = Math.toRadians(((FlipperGizmo) iIGizmo).getAngularVelocity());

                Vect center = ((FlipperGizmo) iIGizmo).getRotationCenter();
                for (Circle circle : iIGizmo.getCircles()) {
                    time = timeUntilRotatingCircleCollision(circle, center, angularVelocity, ballCircle, ballVelocity);
                    if (time < shortestTime) {
                        gizmo = iIGizmo;
                        shortestTime = time;
                        newVelo = reflectRotatingCircle(circle, center, angularVelocity, ballCircle, ballVelocity, .95D);
                    }
                }

                for (LineSegment line : iIGizmo.getLines()) {
                    time = timeUntilRotatingWallCollision(line, center, angularVelocity, ballCircle, ballVelocity);
                    if (time < shortestTime) {
                        gizmo = iIGizmo;
                        shortestTime = time;
                        newVelo = reflectRotatingWall(line, center, angularVelocity, ballCircle, ballVelocity, .95D);
                    }
                }
            } else {
                // Static Physics
                for (Circle circle : iIGizmo.getCircles()) {
                    time = timeUntilCircleCollision(circle, ballCircle, ballVelocity);
                    if (time < shortestTime) {
                        gizmo = iIGizmo;
                        shortestTime = time;
                        newVelo = reflectCircle(circle.getCenter(), ballCircle.getCenter(), ballVelocity);
                    }
                }

                for (LineSegment line : iIGizmo.getLines()) {
                    time = timeUntilWallCollision(line, ballCircle, ballVelocity);
                    if (time < shortestTime) {
                        gizmo = iIGizmo;
                        shortestTime = time;
                        newVelo = reflectWall(line, ballVelocity, 1.0D);
                    }
                }
            }
        }

        for (LineSegment line : walls) {
            time = timeUntilWallCollision(line, ballCircle, ballVelocity);
            if (time < shortestTime) {
                shortestTime = time;
                newVelo = reflectWall(line, ballVelocity, 1.0D);
            }
        }

        Ball ball_ = null;
        VectPair newVelo_ = null;
        for (Ball b : balls) {
            if (b != ball) {
                time = timeUntilBallBallCollision(ballCircle, ballVelocity, b.getCircle(), b.getVelocity());
                if (time < shortestTime) {
                    ball_ = b;
                    shortestTime = time;
                    newVelo_ = reflectBalls(ballCircle.getCenter(), ballMass, ballVelocity, b.getCircle().getCenter(), ballMass, b.getVelocity());
                }
            }
        }

        return ball_ != null ? new CollisionDetails(ball, shortestTime, newVelo_, ball_) : new CollisionDetails(ball, shortestTime, newVelo, gizmo);
    }

    private Ball moveBallForTime(Ball ball, double time) {
        double xVel = ball.getVelocity().x();
        double yVel = ball.getVelocity().y();
        double newX = ball.getX() + (xVel * time);
        double newY = ball.getY() + (yVel * time);
        ball.setX(newX);
        ball.setY(newY);
        return ball;
    }

    private Ball friction(Ball ball, double delta_t) {
        double xVel = ball.getVelocity().x();
        double yVel = ball.getVelocity().y();
        xVel *= (1 - mu * delta_t - mu2 * Math.abs(xVel) * delta_t);
        yVel *= (1 - mu * delta_t - mu2 * Math.abs(yVel) * delta_t);
        ball.setVelocity(xVel, yVel);
        return ball;
    }

    private Ball gravity(Ball ball, double delta_t) {
        double xVel = ball.getVelocity().x();
        double yVel = ball.getVelocity().y();
        yVel += gravity * delta_t;
        ball.setVelocity(xVel, yVel);
        return ball;
    }

    private void moveFlippersForTime(double delta_t) {
        for (FlipperGizmo f : this.getFlippers())
            f.moveFlipperForTime(delta_t);
    }

    public void setGravity(double newGravity) {
        gravity = newGravity < -maxGravity ? -maxGravity : newGravity > maxGravity ? maxGravity : newGravity;
    }

    public void setFriction(double newFriction) {
        mu = newFriction < -maxFriction ? -maxFriction : newFriction > maxFriction ? maxFriction : newFriction;
        mu2 = mu;
    }

    public void setFrictionXY(double x, double y) {
        mu = x < -maxFriction ? -maxFriction : x > maxFriction ? maxFriction : x;
        mu2 = y < -maxFriction ? -maxFriction : y > maxFriction ? maxFriction : y;
    }

    public List<IGizmo> getGizmos() {
        return iGizmos;
    }

    public IGizmo getGizmoByName(String name) {
        for (IGizmo gizmo : iGizmos) {
            if (gizmo.getId().equals(name)) {
                return gizmo;
            }
        }
        return null;
    }

    public List<CircleGizmo> getCircles() {
        List<CircleGizmo> l = new ArrayList<>();
        for (IGizmo g : iGizmos)
            if (g instanceof CircleGizmo)
                l.add((CircleGizmo) g);
        return l;
    }

    public List<SquareGizmo> getSquares() {
        List<SquareGizmo> l = new ArrayList<>();
        for (IGizmo g : iGizmos)
            if (g instanceof SquareGizmo)
                l.add((SquareGizmo) g);
        return l;
    }

    public List<TriangleGizmo> getTriangles() {
        List<TriangleGizmo> l = new ArrayList<>();
        for (IGizmo g : iGizmos)
            if (g instanceof TriangleGizmo)
                l.add((TriangleGizmo) g);
        return l;
    }

    public List<AbsorberGizmo> getAbsorbers() {
        List<AbsorberGizmo> l = new ArrayList<>();
        for (IGizmo g : iGizmos)
            if (g instanceof AbsorberGizmo)
                l.add((AbsorberGizmo) g);
        return l;
    }

    public List<FlipperGizmo> getFlippers() {
        List<FlipperGizmo> l = new ArrayList<>();
        for (IGizmo g : iGizmos)
            if (g instanceof FlipperGizmo)
                l.add((FlipperGizmo) g);
        return l;
    }

    public List<Ball> getBalls() {
        return balls;
    }

    public Ball getBallByName(String name) {
        for (Ball ball : balls) {
            if (ball.getId().equals(name)) {
                return ball;
            }
        }
        return null;
    }

    public void removeBall(Ball ball) {
        balls.remove(ball);
    }

    public TriangleGizmo getTriangleByName(String name) {
        for (TriangleGizmo t : getTriangles())
            if (name.equals(t.getId()))
                return t;
        return null;
    }

    public FlipperGizmo getFlipperByName(String name) {
        for (FlipperGizmo f : getFlippers())
            if (name.equals(f.getId()))
                return f;
        return null;
    }

    public List<KeyConnection> getKeyConnections() {
        return keyConnections;
    }

    public List<KeyConnection> getSpecificKeyConnections(IGizmo gizmo) {
        ArrayList<KeyConnection> specificConn = new ArrayList<>();
        for (KeyConnection connection : getKeyConnections()) {
            if (connection.getAction().equals(gizmo)) {
                specificConn.add(connection);
            }
        }
        return specificConn;
    }

    public void addGizmo(IGizmo gizmo) throws InvalidLocationException {
        if (isOccupied(gizmo, null)) {
            throw new InvalidLocationException();
        }
        iGizmos.add(gizmo);
        this.setChanged();
        this.notifyObservers();
    }

    public void moveGizmo(IGizmo gizmo, int x, int y) throws InvalidLocationException {
        if (isOccupied(x, y)) {
            throw new InvalidLocationException();
        }
        gizmo.setPos(x, y);
    }

    public void moveAbsorber(AbsorberGizmo absorberGizmo, int x, int y) throws InvalidLocationException {
        int x1 = absorberGizmo.getX();
        int y1 = absorberGizmo.getY();
        int x2 = absorberGizmo.getX2();
        int y2 = absorberGizmo.getY2();

        if (x2 < x1) {
            int temp = x2;
            x2 = x1;
            x1 = temp;
        }
        if (y2 < y1) {
            int temp = y2;
            y2 = y1;
            y1 = temp;
        }
        int xLong = x2 - x1;
        int yLong = y2 - y1;

        if (isOccupied(x, y, xLong, yLong) || x + xLong > gridSizeX || y + yLong > gridSizeY) {
            throw new InvalidLocationException();
        }

        absorberGizmo.setPos(x, y);
        absorberGizmo.setPos2(x + xLong, y + yLong);
    }

    public boolean isOccupied(int xDes, int yDes, int xLong, int yLong) {
        ArrayList<ArrayList<Integer>> potentialSpaces = new ArrayList<>();
        for (int i = xDes; i < xDes + xLong; i++) {
            for (int j = yDes; j < yDes + yLong; j++) {
                ArrayList<Integer> space = new ArrayList<>();
                space.add(i);
                space.add(j);
                potentialSpaces.add(space);
            }
        }

        for (List<Integer> list : potentialSpaces) {
            if (isOccupied(list.get(0), list.get(1))) {
                return true;
            }
        }
        return false;
    }

    public boolean isOccupied(IGizmo giz, int xDes, int yDes, int xLong, int yLong) {
        ArrayList<ArrayList<Integer>> potentialSpaces = new ArrayList<>();
        for (int i = xDes; i < xDes + xLong; i++) {
            for (int j = yDes; j < yDes + yLong; j++) {
                ArrayList<Integer> space = new ArrayList<>();
                space.add(i);
                space.add(j);
                potentialSpaces.add(space);
            }
        }

        for (List<Integer> list : potentialSpaces) {
            if (isOccupied(giz, list.get(0), list.get(1))) {
                return true;
            }
        }
        return false;
    }

    public void moveBall(Ball ball, double x, double y) throws InvalidLocationException {
        if (isOccupied((int) Math.floor(x), (int) Math.floor(y))) {
            throw new InvalidLocationException();
        }
        ball.setX(x);
        ball.setY(y);
    }

    private boolean isOccupied(IGizmo newGizmo, Ball ball) {
        for (IGizmo gizmo : getGizmos()) {
            for (List<Integer> list : gizmo.getOccupiedSpace()) {
                if (ball == null && newGizmo != null) {
                    for (List<Integer> newOcc : newGizmo.getOccupiedSpace()) {
                        if ((newOcc.get(0).equals(list.get(0)) && newOcc.get(1).equals(list.get(1)))) {
                            return true;
                        }
                        if (newOcc.get(0) > gridSizeX || newOcc.get(0) < 0 || newOcc.get(1) < 0 || newOcc.get(1) > gridSizeY) {
                            return true;
                        }
                    }
                } else if (ball != null && newGizmo == null) {
                    for (List<Double> ballList : ball.getOccupiedSpace()) {
                        if ((Math.floor(ballList.get(0)) == (list.get(0)) && Math.floor(ballList.get(1)) == (list.get(1)))) {
                            return true;
                        }
                        if (ballList.get(0) > gridSizeX || ballList.get(0) < 0 || ballList.get(1) < 0 || ballList.get(1) > gridSizeY) {
                            return true;
                        }
                    }
                }
            }
        }
        for (Ball b : getBalls()) {
            for (List<Double> list : b.getOccupiedSpace()) {
                if (ball == null && newGizmo != null) {
                    for (List<Integer> newOcc : newGizmo.getOccupiedSpace()) {
                        if ((newOcc.get(0) == Math.floor(list.get(0))) && newOcc.get(1) == Math.floor(list.get(1))) {
                            return true;
                        }
                    }
                } else if (ball != null && newGizmo == null) {
                    for (List<Double> list1 : ball.getOccupiedSpace()) {
                        if (list1.get(0) == (list.get(0)) && list1.get(1) == (list.get(1))) {
                            return true;
                        }
                    }
                }
            }
        }
        if (newGizmo != null) {
            if (newGizmo instanceof FlipperGizmo) {
                FlipperGizmo fg = (FlipperGizmo) newGizmo;
                if (fg.getX() == gridSizeX - 1 || fg.getY() == gridSizeY - 1)
                    return true;
            }
        }
        return false;
    }

    private boolean isOccupied(int xpos, int ypos) {
        for (IGizmo gizmo : getGizmos()) {
            for (List<Integer> list : gizmo.getOccupiedSpace()) {
                if ((xpos == list.get(0) && ypos == list.get(1))) {
                    return true;
                }
            }
        }
        for (Ball b : getBalls()) {
            for (List<Double> list : b.getOccupiedSpace()) {
                if (Math.floor(list.get(0)) == xpos && Math.floor(list.get(1)) == ypos) {
                    return true;
                }
            }
            if (xpos > gridSizeX || xpos < 0 || ypos > gridSizeY || ypos < 0) {
                return true;
            }
        }
        return false;
    }

    private boolean isOccupied(IGizmo g, int xpos, int ypos) {
        for (IGizmo gizmo : getGizmos()) {
            if (g == gizmo) {
                continue;
            }
            for (List<Integer> list : gizmo.getOccupiedSpace()) {
                if ((xpos == list.get(0) && ypos == list.get(1))) {
                    return true;
                }
            }
        }
        for (Ball b : getBalls()) {
            for (List<Double> list : b.getOccupiedSpace()) {
                if (Math.floor(list.get(0)) == xpos && Math.floor(list.get(1)) == ypos) {
                    return true;
                }
            }
            if (xpos > gridSizeX || xpos < 0 || ypos > gridSizeY || ypos < 0) {
                return true;
            }
        }
        return false;
    }

    public void addBall(Ball b) throws InvalidLocationException {
        if (isOccupied(null, b)) {
            throw new InvalidLocationException();
        }
        balls.add(b);
        this.setChanged();
        this.notifyObservers();
    }

    public void addConnection(Connection c) {
        connections.add(c);
    }

    public List<Connection> getConnections() {
        return connections;
    }

    public List<Connection> getSpecificConnections(IGizmo gizmo) {
        ArrayList<Connection> specificConn = new ArrayList<>();
        for (Connection connection : getConnections()) {
            if (connection.getAction().equals(gizmo)) {
                specificConn.add(connection);
            }
        }
        return specificConn;
    }

    public void removeConnection(Connection remove) {
        connections.remove(remove);
    }

    public void addKeyConnection(KeyConnection kc) {
        keyConnections.add(kc);
    }

    public void removeGizmo(IGizmo gizmo) {
        iGizmos.remove(gizmo);
    }

    public void clear() {
        iGizmos = new ArrayList<>();
        balls = new ArrayList<>();
        connections = new ArrayList<>();
        keyConnections = new ArrayList<>();
    }

    public void removeKeyConnection(KeyConnection keyConnection) {
        keyConnections.remove(keyConnection);
    }
}