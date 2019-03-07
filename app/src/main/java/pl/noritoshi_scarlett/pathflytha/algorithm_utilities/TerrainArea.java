package pl.noritoshi_scarlett.pathflytha.algorithm_utilities;

import com.google.maps.android.geometry.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pl.noritoshi_scarlett.pathflytha.algorithm_utilities.geo_objects.GraphEdge;
import pl.noritoshi_scarlett.pathflytha.algorithm_utilities.geo_objects.Graph;
import pl.noritoshi_scarlett.pathflytha.algorithm_utilities.geo_objects.GraphPoint;
import pl.noritoshi_scarlett.pathflytha.pojos.PojoObstacle;
import pl.noritoshi_scarlett.pathflytha.algorithm_utilities.geo_objects.Circle;
import pl.noritoshi_scarlett.pathflytha.algorithm_utilities.geo_objects.LineDirectory;

import static java.lang.Math.pow;

public class TerrainArea {

    public final static float MIN_DISTANCE_FROM_OBJECT = 2000;
    public final static float TOO_CLOSE_DISTANCE_FROM_OBJECT = 4000;

    private GraphPoint startPoint;
    private GraphPoint endPoint;
    private int vObstCount;
    private int hObstCount;
    private double obstDensity;
    private ArrayList<PojoObstacle> obstaclesList;
    private LineDirectory lineEquation;

    private List<GraphEdge> edges;

    private List<DoubleAvoidingSingleObstacle> obstaclesAvoids;

    public List<DoubleAvoidingSingleObstacle> getObstaclesAvoids() {
        return obstaclesAvoids;
    }
    public ArrayList<PojoObstacle> getObstaclesList() {
        return obstaclesList;
    }
    public GraphPoint getStartPoint() {
        return startPoint;
    }
    public GraphPoint getEndPoint() {
        return endPoint;
    }

    public TerrainArea(GraphPoint startPoint, GraphPoint endPoint, LineDirectory line, ArrayList<PojoObstacle> obstaclesList) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.obstaclesList = obstaclesList;
        this.obstaclesAvoids = new ArrayList<>();
        this.lineEquation = line;

        this.edges = new ArrayList<>();

        init();
    }

    private void init() {

        //wyliczenie gestosci zabudowy
        vObstCount = obstaclesList.size();
        double hRange = Math.abs(startPoint.x - endPoint.x);
        double vRange = Math.abs(startPoint.y - endPoint.y);
        if (obstaclesList.size() > 0) {

            float obsRange;
            double obsAllSize = 0;
            for (int i = 0; i < obstaclesList.size(); i++) {
                obsRange = obstaclesList.get(i).getItem_obs_range();
                obsAllSize += obsRange * obsRange;
            }
            obstDensity = (hRange * vRange) / obsAllSize;

        } else {
            obstDensity = 0;
        }

        findConflicts_newObstacle();
        findConflicts_staticObtacles();
    }

    public double getObstDensity() {
        return obstDensity;
    }

    public String getName() {
        return this.startPoint.x + ", " + this.endPoint.x + "\n" + this.startPoint.y + ", " + this.endPoint.y;
    }

    private void findConflicts_staticObtacles() {

        if (! obstaclesList.isEmpty()) {
            double distance, radius;
            Point lastPointInPath = new Point(startPoint.x, startPoint.y);

            for (int i = 0; i < obstaclesList.size(); i++) {

                distance = Math.ceil(LineDirectory.distBetweenPointAndLine(
                        obstaclesList.get(i).getItem_obs_x(), obstaclesList.get(i).getItem_obs_y(),
                        lineEquation));
                radius = Math.floor(MIN_DISTANCE_FROM_OBJECT + (obstaclesList.get(i).getItem_obs_range() / 2));

                if (distance <= radius && obstaclesList.get(i).getItem_obs_x() > lastPointInPath.x) {
                    obstaclesList.get(i).setSelected(true);

                    radius = searchForOtherObstacles(radius,
                            new GraphPoint(
                                    obstaclesList.get(i).getItem_obs_x(),
                                    obstaclesList.get(i).getItem_obs_y(),
                                    obstaclesList.get(i).getItem_obs_elevation()));

                    List<GraphPoint> newPoints = Circle.findPointsAroundObstacleOnLine(
                            new GraphPoint(
                                    obstaclesList.get(i).getItem_obs_x(),
                                    obstaclesList.get(i).getItem_obs_y(),
                                    obstaclesList.get(i).getItem_obs_elevation()),
                            radius,
                            lineEquation
                    );

                    if (newPoints != null) {

                        List<LineDirectory> newLines = Arrays.asList(
                                new LineDirectory(startPoint, newPoints.get(0)),
                                new LineDirectory(newPoints.get(1), endPoint)
                        );

                        edges.remove(endPoint);

                        //edges.add(startPoint, newPoints.get(0), LineDirectory.distBetweenPoints(startPoint, newPoints.get(0)));
                        //edges.add(startPoint, newPoints.get(1), LineDirectory.distBetweenPoints(startPoint, newPoints.get(1)));

                       // edges.add(newPoints.get(0), endPoint, LineDirectory.distBetweenPoints(newPoints.get(0), endPoint));
                        //edges.add(newPoints.get(1), endPoint, LineDirectory.distBetweenPoints(newPoints.get(1), endPoint));

                    }

                } else if (distance <= TOO_CLOSE_DISTANCE_FROM_OBJECT) {
                    obstaclesList.get(i).setTooClose(true);
                }
            }
        }
    }

    private void findConflicts_newObstacle() {
        if (obstaclesList.isEmpty()) {
            //none
        } else {
            double distance, radius;
            Point lastPointInPath = new Point(startPoint.x, startPoint.y);

            for (int i = 0; i < obstaclesList.size(); i++) {

                distance = Math.ceil(LineDirectory.distBetweenPointAndLine(obstaclesList.get(i).getPoint(), lineEquation));
                radius = Math.floor(MIN_DISTANCE_FROM_OBJECT + (obstaclesList.get(i).getItem_obs_range() / 2));

                if (distance <= radius && obstaclesList.get(i).getItem_obs_x() > lastPointInPath.x) {
                    obstaclesList.get(i).setSelected(true);

                    radius = searchForOtherObstacles(radius,
                            new GraphPoint(obstaclesList.get(i).getItem_obs_x(), obstaclesList.get(i).getItem_obs_y(), 0));

                    obstaclesAvoids.add(getPathsForCollision(
                            lastPointInPath, lineEquation,
                            new Point( Math.round(obstaclesList.get(i).getItem_obs_x()), Math.round(obstaclesList.get(i).getItem_obs_y()) ),
                            radius, distance));
                    lastPointInPath = obstaclesAvoids.get(obstaclesAvoids.size() - 1).getShortPath().getEndPoint();


                } else if (distance <= TOO_CLOSE_DISTANCE_FROM_OBJECT) {
                    obstaclesList.get(i).setTooClose(true);
                }
            }
        }
    }

    private double searchForOtherObstacles(double radius, GraphPoint currentPont) {
        double radiusInner = radius;
        for (int i = 0; i < obstaclesList.size(); i++) {

            double distanceInner = Math.ceil(LineDirectory.distBetweenPoints(
                    currentPont,
                    new GraphPoint(obstaclesList.get(i).getItem_obs_x(), obstaclesList.get(i).getItem_obs_y(), 0)));

            if ( ! obstaclesList.get(i).isSelected() && distanceInner <= radius * 3) {
                obstaclesList.get(i).setSelected(true);
                radiusInner =  searchForOtherObstacles(
                        radiusInner + (distanceInner / 2),
                        LineDirectory.centerFromTwoPoints(
                                currentPont,
                                new GraphPoint(obstaclesList.get(i).getItem_obs_x(), obstaclesList.get(i).getItem_obs_y(), 0)
                        )
                );
                break;
            }  else if (distanceInner <= radiusInner + TOO_CLOSE_DISTANCE_FROM_OBJECT) {
                obstaclesList.get(i).setTooClose(true);
            }
        }
        return radiusInner;
    }

    private double getWeightForLines(LineDirectory line1, LineDirectory line2) {
        if (line2.getA() != 0) {
            if (line1.getA() == -1 / line2.getA()) {
                return 1;
            } else {
                return Math.tan(Math.abs((line1.getA() - line2.getA()) / (1 + line1.getA() * line2.getA())));
            }
        } else if (line1.getA() != 0) {
            if (line2.getA() == -1 / line1.getA()) {
                return 1;
            } else {
                return Math.tan(Math.abs((line1.getA() - line2.getA()) / (1 + line1.getA() * line2.getA())));
            }
        } else {
            return -1;
        }
    }

    private static DoubleAvoidingSingleObstacle getPathsForCollision(Point lastPointInPath, LineDirectory lineFromPath, Point center, double radius, double h) {

        DoubleAvoidingSingleObstacle obstacleAvoids = new DoubleAvoidingSingleObstacle();

        // 1. Tworzymy rownolegle do trasy, odlegla o promien
        //   Pirwsz lezy nad, a druga pod (bo pierwsza ma c dodatnie, a druga ujemne, a c wychodzi dodatnie)
        List<LineDirectory> parallelLForPath = lineFromPath.generateParallel(radius);
        // 2. Tworzymy prostopadla do trasy, przehcodzaca przez przeszkode
        LineDirectory normalBase = lineFromPath.generateNormal(center);

        // 3. Wyliczamy prostopadłe do utworzonej w punkcie 2.
        // distance
        // distance = sqrt( (r-h)^2 + (2r)^2 )
        // distance = sqrt( (r+h)^2 - (2r)^2 )
        double distance;
        List<Double> angles = new ArrayList<>();
        // dla okregow ze srodkami po stronie przeszkody
        distance = Math.sqrt( Math.abs(pow(2*radius, 2) - pow((radius-h), 2) ) );
        angles.add(Math.abs( Math.cos(Math.toRadians( (radius - h) / 2*radius ))) );
        List<LineDirectory> parallelForNormalBaseA = normalBase.generateParallel(distance);
        // dla okregow ze srodkami po przeciwnej stronie przeszkody
        distance = Math.sqrt( Math.abs(pow(2*radius, 2) - pow((radius+h), 2) ) );
        angles.add(Math.abs( Math.cos(Math.toRadians( (radius + h) / 2*radius ))) );
        List<LineDirectory> parallelForNormalBaseB = normalBase.generateParallel(distance);


        // 4. Znajdujemy punty wspole
        // 4.A wspolne z trasa lotu
        List<Point> listOfPointsInPath = new ArrayList<>();
        for (LineDirectory lineTemp : parallelForNormalBaseA) {
            listOfPointsInPath.add(LineDirectory.findPointForLines(lineFromPath, lineTemp));
        }
        for (LineDirectory lineTemp : parallelForNormalBaseB) {
            listOfPointsInPath.add(LineDirectory.findPointForLines(lineFromPath, lineTemp));
        }

        // 4.B wspolne ze srodkami okregow
        boolean isUp = (lineFromPath.valueOf(center.x) < center.y);
        if (parallelLForPath.size() >= 2) {
            List<Point> listOfPointsInCircles = new ArrayList<>();
            for (LineDirectory lineTemp : parallelForNormalBaseA) {
                listOfPointsInCircles.add(LineDirectory.findPointForLines(parallelLForPath.get(isUp ? 0 : 1), lineTemp));
            }
            for (LineDirectory lineTemp : parallelForNormalBaseB) {
                listOfPointsInCircles.add(LineDirectory.findPointForLines(parallelLForPath.get(isUp ? 1 : 0), lineTemp));
            }

            // 5. Wyznaczenie punktów stycznosci o  kregów
            List<Point> listOfPointsBeetweenCircles = new ArrayList<>();
            for (Point pointTemp : listOfPointsInCircles) {
                listOfPointsBeetweenCircles.add(LineDirectory.findCenterPointInLine(center, pointTemp));
            }

            for (int k = 0; k < 3; k+=2) {

                // utworzenie obiektowej reprezentacji dla omijanej przeszkody
                AvoidingSingleObstacle avoid = new AvoidingSingleObstacle(
                        listOfPointsInPath.get(k), listOfPointsInPath.get(k + 1),
                        listOfPointsInCircles.get(k ), listOfPointsInCircles.get(k + 1),
                        center,
                        listOfPointsBeetweenCircles.get(k), listOfPointsBeetweenCircles.get(k + 1),
                        angles.get( (k == 0) ? 0 : 1 ), radius,
                        lineFromPath
                );

                obstacleAvoids.add(avoid);
            }

        }

        // 6. Wyznaczenie wzorow dla omijania przeszkody

        // 7. Prezentacja graficzna.
        return obstacleAvoids;
    }

}
