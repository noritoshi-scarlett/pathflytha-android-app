package pl.noritoshi_scarlett.pathflytha.algorithm_utilities.geo_objects;

import android.util.Pair;

import com.google.maps.android.geometry.Point;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GraphConnector {

    public enum ObstacleType {
        SECURE,
        SECURE_BUT_TOO_LOW,
        SECURE_BUT_SO_CLOSE,
        UNSECURE_FOR_NORMAL_HIGHT,
        UNSECURE_FOR_MAX_HIGHT
    }

    final private GraphPoint centroid;
    final private double radius;
    private Circle obstacleCircle;
    final private int obstacleElevation;
    final private int obstacleHeight;
    final private ObstacleType type;

    private List<Pair<GraphConnector, GraphPoint>> connectFrom;
    private List<Pair<GraphConnector, GraphPoint>> connectTo;
    private List<Pair<GraphEdge, GraphPoint>> linktFrom;
    private List<Pair<GraphEdge, GraphPoint>> linkTo;

    public GraphConnector(double radius, GraphPoint centroid, int obsElevation, int obsHeight, ObstacleType type) {

        this.radius = radius;
        this.centroid = centroid;
        this.obstacleElevation = obsElevation;
        this.obstacleHeight = obsHeight;
        this.type = type;

        connectFrom = new ArrayList<>();
        connectTo = new ArrayList<>();
        linktFrom = new ArrayList<>();
        linkTo = new ArrayList<>();
    }


    public GraphPoint getCentroid() { return centroid; }
    public double getRadius() { return radius; }
    public Circle getObstacleCircle() { return obstacleCircle; }
    public int getObstacleElevation() { return obstacleElevation; }
    public int getObstacleHeight() { return obstacleHeight; }
    public ObstacleType getType() { return type; }

    public void addConnectFrom(Pair<GraphConnector, GraphPoint> connect) { this.connectFrom.add(connect); }
    public void addConnectTo(Pair<GraphConnector, GraphPoint> connect) { this.connectTo.add(connect); }
    public void addLinkFrom(Pair<GraphEdge, GraphPoint> link) { this.linktFrom.add(link); }
    public void addLinkTo(Pair<GraphEdge, GraphPoint> link) { this.linkTo.add(link); }
    public List<Pair<GraphEdge, GraphPoint>> getLinktFrom() { return linktFrom; }
    public List<Pair<GraphEdge, GraphPoint>> getLinkTo() { return linkTo; }

    public Pair<GraphConnector, GraphPoint> searchConnectFrom(GraphConnector circle) {
        for (int i = 0; i < connectFrom.size(); i++) {
            if (connectFrom.get(i).first.equals(circle)) {
                return connectFrom.get(i);
            }
        }
        return null;
    }

    public Pair<GraphConnector, GraphPoint> searchConnectTo(GraphConnector circle) {
        for (int i = 0; i < connectTo.size(); i++) {
            if (connectTo.get(i).first.equals(circle)) {
                return connectTo.get(i);
            }
        }
        return null;
    }

    /**
     * Funkcja zwracajaca gotowa funkcje do rysowania okregu
     * @return dwa polkola z okregu
     */
    public List<LineGraphSeries<DataPoint>> generateXYFunction() {

        return getPointsForCircleFunction(
                new Point(centroid.x - getRadius(), centroid.y),
                new Point(centroid.x + getRadius(), centroid.y),
                centroid,
                getRadius());
    }

    /**
     * funkcja rysujaca okrag
     * @param fromPoint lewy punkt z prostej rownoleglej do OX
     * @param toPoint prawy punkt z prostej rownoleglej do OX
     * @param center srodek okregu
     * @param radius promien okregu
     * @return dwie listy punktow
     */
    private static List<LineGraphSeries<DataPoint>> getPointsForCircleFunction(Point fromPoint, Point toPoint, GraphPoint center, double radius) {

        LineGraphSeries<DataPoint> entries1 = new LineGraphSeries<>();
        LineGraphSeries<DataPoint> entries2 = new LineGraphSeries<>();
        double fromX = fromPoint.x;
        double toX = toPoint.x;
        double pointsDistance = Math.abs(fromX - toX) / 30;
        double x = fromX, temp, y1, y2;

        for (int i = 0; i <= 30; i++, x+=pointsDistance) {

            temp = Math.sqrt(radius * radius - Math.pow(x - center.x, 2));
            y1 = temp + center.y;
            y2 = -temp + center.y;
            entries1.appendData(new DataPoint(x, y1), false, 30);
            entries2.appendData(new DataPoint(x, y2), false, 30);
        }
        return Arrays.asList(entries1, entries2);
    }
}
