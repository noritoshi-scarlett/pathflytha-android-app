package pl.noritoshi_scarlett.pathflytha.algorithm_utilities.geo_objects;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Collections;
import java.util.List;

public class GraphEdge {

    final GraphPoint from;
    final GraphPoint to;
    double weight;
    LineDirectory line;
    private boolean duplicate;

    GraphEdge(GraphPoint from, GraphPoint to) {
        this.from = from;
        this.to = to;
        this.duplicate = (from.x < to.x);
    }

    /**
     * dla prostych linii o dowolnej wysokosci lotu
     * @param from      zrodlo
     * @param to        cel
     * @param weight    odleglosc
     */
    public GraphEdge(GraphPoint from, GraphPoint to, double weight) {
        this.from = from;
        this.to = to;
        this.weight = weight;
        this.line = new LineDirectory(from, to);
    }

    public GraphPoint getFrom() { return from; }
    public GraphPoint getTo() { return to; }
    public double getWeight() { return weight; }
    public LineDirectory getLine() { return line; }
    public boolean isDuplicate() { return duplicate; }

    public List<LineGraphSeries<DataPoint>> getPointsForGraph() {

        LineGraphSeries<DataPoint> entries1 = new LineGraphSeries<>();
        if (this.getFrom().x < this.getTo().x) {
            entries1.appendData(new DataPoint(this.getFrom().x, this.getFrom().y), false, 2);
            entries1.appendData(new DataPoint(this.getTo().x, this.getTo().y), false, 2);
        } else {
            entries1.appendData(new DataPoint(this.getTo().x, this.getTo().y), false, 2);
            entries1.appendData(new DataPoint(this.getFrom().x, this.getFrom().y), false, 2);
        }
        return Collections.singletonList(entries1);
    }


    static public boolean isUniqueEgde(List<GraphEdge> edges, GraphPoint src, GraphPoint dest) {
        if (edges != null) {
            for (int i = 0; i < edges.size(); i++) {
                if (GraphPoint.isSamePoints(edges.get(i).getFrom(), src) && GraphPoint.isSamePoints(edges.get(i).getTo(), dest)) {
                    return false;
                }
            }
        }
        return true;
    }

}
