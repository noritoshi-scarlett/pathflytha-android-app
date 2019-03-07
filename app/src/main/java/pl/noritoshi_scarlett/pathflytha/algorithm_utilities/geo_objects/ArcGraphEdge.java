package pl.noritoshi_scarlett.pathflytha.algorithm_utilities.geo_objects;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ArcGraphEdge extends GraphEdge {

    final private GraphPoint center;
    final private double radius;

    /**
     * dla fragmentów okregow o krzywym torze lotu na dowolnej wyskosci
     * @param from      zrodlo
     * @param to        cel
     * @param weight    dystans
     * @param center    srodek okregu
     * @param radius    prmien okregu
     */
    public ArcGraphEdge(GraphPoint from, GraphPoint to, double weight, GraphPoint center, double radius) {
        super(from, to);

        this.weight = weight;
        this.center = center;
        this.radius = radius;
        this.line = new LineDirectory(from, to);
    }

    public List<LineGraphSeries<DataPoint>> getPointsForGraph() {

        LineGraphSeries<DataPoint> entries1 = new LineGraphSeries<>();
        //if (LineDirectory.distBetweenPoints(from, to) < 2) {
        //    return super.getPointsForGraph();
        //}

        LineGraphSeries<DataPoint> entries2 = new LineGraphSeries<>();
        double minX = Math.min(from.x, to.x);
        double maxX = Math.max(from.x, to.x);
        double fromX = Math.max(center.x - radius, minX);
        double toX = Math.min(center.x + radius, maxX);
        double pointsDistance = Math.abs(fromX - toX) / 30;
        double x = fromX, temp, y1, y2;

        GraphPoint centerOfVertex = LineDirectory.centerFromTwoPoints(from, to);
        GraphPoint point1, point2;
        boolean get1 = true;
        for (int i = 0; i <= 30; i++, x+=pointsDistance) {

            temp = Math.sqrt(radius * radius - Math.pow(x - center.x, 2));
            y1 = temp + center.y;
            y2 = -temp + center.y;
            point1 = new GraphPoint(x, y1, 0);
            point2 = new GraphPoint(x, y2, 0);
            if (x <= centerOfVertex.x) {
                if (LineDirectory.distBetweenPoints(from, point1) > LineDirectory.distBetweenPoints(from, point2)) {
                    get1 = false;
                }
            } else {
                if (LineDirectory.distBetweenPoints(to, point1) > LineDirectory.distBetweenPoints(to, point2)) {
                    get1 = false;
                }
            }
            if (get1) {
                entries1.appendData(new DataPoint(x, y1), false, 30);
            } else{
                entries2.appendData(new DataPoint(x, y2), false, 30);
            }
        }
        if (! entries1.isEmpty() && ! entries2.isEmpty()) {
            return Arrays.asList(entries1, entries2);
        } else if (! entries1.isEmpty()) {
            return Collections.singletonList(entries1);
        } else if (! entries2.isEmpty()) {
            return Collections.singletonList(entries2);
        } else {
            return Collections.emptyList();
        }
    }
}
