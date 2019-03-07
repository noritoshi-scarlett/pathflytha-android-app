package pl.noritoshi_scarlett.pathflytha.algorithm_utilities.geo_objects;

import android.util.Pair;

import com.google.maps.android.geometry.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.lang.Double.NaN;

final public class Circle {

    /**
     * Find points in normal line
     * more abbout: https://math.stackexchange.com/a/228855
     * @param center center of circle in obstacle
     * @param radius radius of circle
     * @param line line normal to colission line, cross with center
     * @return list of points
     */
    public static List<GraphPoint> findPointsAroundObstacleOnLine(GraphPoint center, double radius, LineDirectory line) {

        if (line.getA() == 0) {
            return new ArrayList<>();
        }

        double x1, x2, y1, y2;

        double A, B, C;
        A = line.getA() * line.getA() + 1;
        B = 2 * (line.getA() * line.getB() - line.getA() * center.y - center.x);
        C = center.y * center.y - radius * radius + center.x * center.x - 2 * (line.getB() * center.y) + line.getB() * line.getB();

        x1 = ( -B - Math.sqrt(B*B - 4*A*C) ) / 2*A;
        x2 = ( -B + Math.sqrt(B*B - 4*A*C) ) / 2*A;

        y1 = line.getA() * (x1) + line.getB();
        y2 = line.getA() * (x2) + line.getB();

        return Arrays.asList(new GraphPoint(x1, y1, 0), new GraphPoint(x2, y2, 0));
    }

    /**
     * Find tangets lines for circle and point
     * more abbout: https://stackoverflow.com/questions/1351746/find-a-tangent-point-on-circle
     * @param center center of circle in obstacle
     * @param radius radius of circle
     * @param start point for Lines
     * @return list of points
     */
    private static List<Point> findTangetLinesPointForObstacleCircle(GraphPoint center, double radius, GraphPoint start) {

        List<Point> points = new ArrayList<>();
        double x1, x2, y1, y2, t;

        radius = Math.round(radius);

        double dx = Math.round(center.x - start.x);
        double dy = Math.round(center.y - start.y);
        double dd = Math.round(Math.sqrt(dx * dx + dy * dy));
        double a = Math.asin(radius / dd);
        double b = Math.atan2(dy, dx);

        if (a == NaN) {
            return null;
        }

        t = (b - a);
        x1 = Math.round(center.x + radius * Math.sin(t));
        y1 = Math.round(center.y + radius * -Math.cos(t));

        t = (b + a);
        x2 = Math.round(center.x + radius * -Math.sin(t));
        y2 = Math.round(center.y + radius * Math.cos(t));

        points.add(new Point(x1, y1));
        points.add(new Point(x2, y2));

        return points;
    }

    /**
     * Find point of tangets lines between circles (OUTER)
     */
    public static List<Pair<GraphPoint, GraphPoint>> findTangetLinesPointForTwoObstacleCircles(GraphConnector circle1, GraphConnector circle2) {
        List<Point> semiTangetPoints;
        List<LineDirectory> semiTangetLines = new ArrayList<>();
        List<LineDirectory> tangetLines = new ArrayList<>();
        List<Pair<Point, Point>> points = new ArrayList<>();
        List<Pair<GraphPoint, GraphPoint>> finalPoints = new ArrayList<>();

        Point point, point2;
        double radius;
        boolean distanceBetweenCircles = LineDirectory.distBetweenPoints(
                circle1.getCentroid(), circle2.getCentroid()) > circle1.getRadius() + circle2.getRadius();

        // GET OUTER
        semiTangetPoints = (findTangetLinesPointForObstacleCircle(
                circle1.getCentroid(),
                circle1.getRadius() - circle2.getRadius(),
                circle2.getCentroid()));
        point = new Point(circle2.getCentroid().x, circle2.getCentroid().y);
        radius = circle2.getRadius();
        point2 = new Point(circle1.getCentroid().x, circle1.getCentroid().y);

        if (semiTangetPoints != null) {
            for (Point semiTangetPoint : semiTangetPoints) {
                semiTangetLines.add(new LineDirectory(semiTangetPoint, point));
            }
            double temp1, temp2;
            for (int i = 0; i < semiTangetLines.size(); i++) {
                List<LineDirectory> semiTangetLinesAsParallelLines = semiTangetLines.get(i).generateParallel(radius);
                temp1 = LineDirectory.distBetweenPointAndLine(point2, semiTangetLinesAsParallelLines.get(0));
                temp2 = LineDirectory.distBetweenPointAndLine(point2, semiTangetLinesAsParallelLines.get(1));
                if (temp1 < temp2) {
                    tangetLines.add(i, semiTangetLinesAsParallelLines.get(0));
                } else {
                    tangetLines.add(i, semiTangetLinesAsParallelLines.get(1));
                }
            }
            for (int i = 0; i < tangetLines.size(); i++) {
                points.add(new Pair<>(
                        LineDirectory.findPointForLines(tangetLines.get(i).generateNormal(circle1.getCentroid()), tangetLines.get(i)),
                        LineDirectory.findPointForLines(tangetLines.get(i).generateNormal(circle2.getCentroid()), tangetLines.get(i))
                ));
            }
        }
        // GET INNER
        if (distanceBetweenCircles) {
            semiTangetPoints = (findTangetLinesPointForObstacleCircle(
                    circle1.getCentroid(),
                    circle1.getRadius() + circle2.getRadius(),
                    circle2.getCentroid()));
            point = new Point(circle2.getCentroid().x, circle2.getCentroid().y);
            radius = circle2.getRadius();
            point2 = new Point(circle1.getCentroid().x, circle1.getCentroid().y);

            if (semiTangetPoints != null) {
                for (Point semiTangetPoint : semiTangetPoints) {
                    semiTangetLines.add(new LineDirectory(semiTangetPoint, point));
                }
                for (int i = 0; i < semiTangetLines.size(); i++) {
                    List<LineDirectory> semiTangetLinesAsParallelLines = semiTangetLines.get(i).generateParallel(radius);
                    double temp1 = LineDirectory.distBetweenPointAndLine(point2, semiTangetLinesAsParallelLines.get(0));
                    double temp2 = LineDirectory.distBetweenPointAndLine(point2, semiTangetLinesAsParallelLines.get(1));
                    if (temp1 < temp2) {
                        tangetLines.add(i, semiTangetLinesAsParallelLines.get(0));
                    } else {
                        tangetLines.add(i, semiTangetLinesAsParallelLines.get(1));
                    }
                }
                for (int i = 0; i < tangetLines.size(); i++) {
                    points.add(new Pair<>(
                            LineDirectory.findPointForLines(tangetLines.get(i).generateNormal(circle1.getCentroid()), tangetLines.get(i)),
                            LineDirectory.findPointForLines(tangetLines.get(i).generateNormal(circle2.getCentroid()), tangetLines.get(i))
                    ));
                }
            }
        }
        for (int i = 0; i < points.size(); i++) {
            finalPoints.add(new Pair<>(
                    new GraphPoint(GraphPoint.roundPoint(points.get(i).first),  circle1.getObstacleElevation()),
                    new GraphPoint(GraphPoint.roundPoint(points.get(i).second), circle2.getObstacleElevation())));
        }
        return finalPoints;
    }

    /**
     * znajdownie pierwszego kolizyjnego okregu z linia styczna do dwoch innych okregow
     * @param allCircles    zbior wszystkich okregow
     * @param pointA        punkt poczatkowy
     * @param pointB        punkt docelowy
     * @param currentCircle okrag poczatkowy
     * @return              numer kolizyjnego okregu
     */
    public static int findFristColisionCircleForLine(
            List<GraphConnector> allCircles, GraphPoint pointA, GraphPoint pointB, GraphConnector currentCircle) {

        int numerOfCircle = -1;
        List<Pair<Integer, List<GraphPoint>>> listID = new ArrayList<>();
        List<GraphPoint> points;


        for(int i = 4; i < allCircles.size(); i++) {
            if (currentCircle == null) {
                continue;
            }
            if (allCircles.get(i).getCentroid().equals(currentCircle.getCentroid())) {
                continue;
            }

            points = Circle.isColisionCircleAndLine(allCircles.get(i), pointA, pointB);
            if (points.size() > 0) {
                listID.add(new Pair<>(i, points));
            }
        }
        if(listID.size() > 0) {

            double distance = 0, temp;
            Pair<Integer, List<GraphPoint>> pair;
            for (int i = 0; i < listID.size(); i++) {
                pair = listID.get(i);
                if (pair.second.size() > 1) {
                    temp = Math.min(LineDirectory.distBetweenPoints(pointA, pair.second.get(0)),
                            LineDirectory.distBetweenPoints(pointA, pair.second.get(1)));
                } else {
                    temp = LineDirectory.distBetweenPoints(pointA, pair.second.get(0));
                }
                if (temp < distance || distance == 0) {
                    distance = temp;
                    numerOfCircle = i;
                }
            }
        }
        return  numerOfCircle;
    }

    /**
     * sprawdza, czy wskazany GraphCircle przecina linie AB
     * @param circle okrag
     * @param pointA punkt A
     * @param pointB punkt B
     * @return lista punktow kolizyjnych
     */
    public static List<GraphPoint> isColisionCircleAndLine(GraphConnector circle, GraphPoint pointA, GraphPoint pointB) {
        double baX = pointB.x - pointA.x;
        double baY = pointB.y - pointA.y;
        double caX = circle.getCentroid().x - pointA.x;
        double caY = circle.getCentroid().y - pointA.y;

        double a = baX * baX + baY * baY;
        double bBy2 = baX * caX + baY * caY;
        double c = caX * caX + caY * caY - circle.getRadius() * circle.getRadius();

        double pBy2 = bBy2 / a;
        double q = c / a;

        double disc = pBy2 * pBy2 - q;
        if (disc < 0) {
            return Collections.emptyList();
        }
        // if disc == 0 ... dealt with later
        double tmpSqrt = Math.sqrt(disc);
        double abScalingFactor1 = -pBy2 + tmpSqrt;
        double abScalingFactor2 = -pBy2 - tmpSqrt;

        GraphPoint p1 = new GraphPoint(pointA.x - baX * abScalingFactor1, pointA.y
                - baY * abScalingFactor1, circle.getObstacleElevation());
        if (disc == 0) { // abScalingFactor1 == abScalingFactor2
            return prepareResultForCollision(Collections.singletonList(p1), pointA, pointB);
        }
        GraphPoint p2 = new GraphPoint(pointA.x - baX * abScalingFactor2, pointA.y
                - baY * abScalingFactor2, circle.getObstacleElevation());
        return prepareResultForCollision(Arrays.asList(p1, p2), pointA, pointB);
    }

    private static List<GraphPoint> prepareResultForCollision(List<GraphPoint> points, GraphPoint pointA, GraphPoint pointB) {
        double distA_0, distA_1, distB_0, distB_1, distBaseCircles;

        if (points.size() > 1) {
            distA_0 = LineDirectory.distBetweenPoints(pointA, points.get(0));
            distA_1 = LineDirectory.distBetweenPoints(pointA, points.get(1));
            distB_0 = LineDirectory.distBetweenPoints(pointB, points.get(0));
            distB_1 = LineDirectory.distBetweenPoints(pointB, points.get(1));
            distBaseCircles = LineDirectory.distBetweenPoints(pointA, pointB);
            double closestA = Math.min(distA_0, distA_1);
            double closestB = Math.min(distB_0, distB_1);
            double biggerClosest = Math.max(closestA, closestB);
            if (biggerClosest + 2 <= distBaseCircles) {
                return points;
            }
        } else {
            distA_0 = LineDirectory.distBetweenPoints(pointA, points.get(0));
            distB_0 = LineDirectory.distBetweenPoints(pointB, points.get(0));
            distBaseCircles = LineDirectory.distBetweenPoints(pointA, pointB);
            double biggerClosest = Math.max(distA_0, distB_0);
            if (biggerClosest + 2 <= distBaseCircles) {
                return points;
            }
        }
        return Collections.emptyList();
    }

    /**
     * dlugosc luku pomiedzy punktami na okregu
     * @param A         punkt A
     * @param B         punkt B
     * @param C         przychodzacego punkt
     * @param D         wychodzacego punkt
     * @param radius    promien
     * @return          dlugosc krotszego luku
     */
    public static double lengthOfCircle(GraphPoint A, GraphPoint B, GraphPoint C, GraphPoint D, double radius) {
        double length;
        double dist = LineDirectory.distBetweenPoints(A, B);
        double acos = (Math.acos(1 -  ( (dist*dist)/(2*(radius*radius)) ) ));
        length = radius * acos;
        if (dist > LineDirectory.distBetweenPoints(C, D)) {
            length = 2 * Math.PI * radius - length;
        }
        return length;
    }

    /**
     * dlugosc czesci okregu
     * @param edgeA przychodzaca krawedz
     * @param edgeB wychodzaca krawedz
     * @param radius promien
     * @return dlugosc
     */
    public static double lengthOfCircleWithEdges(GraphEdge edgeA, GraphEdge edgeB, double radius) {
        double length;
        double dist = LineDirectory.distBetweenPoints(edgeA.getTo(), edgeB.getFrom());
        double acos = (Math.acos(1 -  ( (dist*dist)/(2*(radius*radius)) ) ));
        length =  radius * acos;
        if (dist > LineDirectory.distBetweenPoints(edgeA.getFrom(), edgeB.getTo())) {
            length = 2 * Math.PI * radius - length;
        }
        if (length == NaN) {
            return  0.0;
        }
        return length;

    }
}
