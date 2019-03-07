package pl.noritoshi_scarlett.pathflytha.algorithm_utilities.geo_objects;

import java.util.List;

public class OverGraphEdge extends GraphEdge {

    final private List<GraphPoint> colisionOverPoints;

    /**
     * dla pokonywania przeszkod gora, o okreslonej w danym punkcie minimalnej wysokosci lotu
     * @param from      zrodlo
     * @param to        cel
     * @param circle    okrag
     */
    public OverGraphEdge(GraphPoint from, GraphPoint to, GraphConnector circle) {
        super(from, to);

        this.colisionOverPoints = Circle.isColisionCircleAndLine(circle, this.from, this.to);
        if (this.colisionOverPoints.size() > 1) {
            this.weight = LineDirectory.distBetweenPoints(this.from, colisionOverPoints.get(0))
                    +  LineDirectory.distBetweenPoints(colisionOverPoints.get(0), colisionOverPoints.get(1))
                    +  LineDirectory.distBetweenPoints(colisionOverPoints.get(1), this.to);
        } else if (this.colisionOverPoints.size() > 0) {
            this.weight = LineDirectory.distBetweenPoints(this.from, colisionOverPoints.get(0))
                    +  LineDirectory.distBetweenPoints(colisionOverPoints.get(0), this.to);
        } else {
            this.weight = LineDirectory.distBetweenPoints(this.from, this.to);
        }
        this.line = new LineDirectory(from, to);
    }

    public List<GraphPoint> getColisionOverPoints() { return colisionOverPoints; }
}
