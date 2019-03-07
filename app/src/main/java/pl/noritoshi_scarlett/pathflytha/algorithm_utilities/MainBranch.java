package pl.noritoshi_scarlett.pathflytha.algorithm_utilities;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.data.Point;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import pl.noritoshi_scarlett.pathflytha.R;
import pl.noritoshi_scarlett.pathflytha.algorithm_utilities.geo_objects.GraphConnector;
import pl.noritoshi_scarlett.pathflytha.algorithm_utilities.geo_objects.GraphEdge;
import pl.noritoshi_scarlett.pathflytha.algorithm_utilities.geo_objects.GraphPoint;
import pl.noritoshi_scarlett.pathflytha.algorithm_utilities.geo_objects.OverGraphEdge;
import pl.noritoshi_scarlett.pathflytha.algorithm_utilities.small_quests.DijkstraAlgorithm;
import pl.noritoshi_scarlett.pathflytha.algorithm_utilities.geo_objects.Graph;
import pl.noritoshi_scarlett.pathflytha.database_utilities.DatabaseHelper;
import pl.noritoshi_scarlett.pathflytha.pojos.PojoObstacle;
import pl.noritoshi_scarlett.pathflytha.algorithm_utilities.geo_objects.LineDirectory;

import static pl.noritoshi_scarlett.pathflytha.algorithm_utilities.small_quests.LatLongConverter.convertLatLongTo1992InMeters;

public class MainBranch {

    // SETTINGS
    private int pilotNormalFlyHeight;
    // DB, CONTEXT
    private DatabaseHelper mDBHelper;
    private SQLiteDatabase mDb;
    private Context context;
    // POINTS
    private GraphPoint startPoint;
    private GraphPoint startPointOut;
    private GraphPoint endPointTarget;
    private GraphPoint endPoint;
    // PATH
    private LineDirectory lineOfPath;
    // GRAPH
    private Graph graph;
    private LinkedList<GraphPoint> path;
    private List<GraphEdge> edges;
    private List<GraphEdge>  edgesAll;
    private List<GraphEdge>  edgesAllBad;
    private List<OverGraphEdge>  edgesOver;
    // TERRAIN
    private TerrainArea terrainAreas;
    // OBSTACLES
    private ArrayList<PojoObstacle> obstacles;
    // TERRAIN HEIGHT
    private List<LineGraphSeries<DataPoint>> terrainForPath;
    private List<PointsGraphSeries<DataPoint>> pointsForPath;
    private List<PointsGraphSeries<DataPoint>> pointsForObst;

    public GraphPoint getStartPoint() { return startPoint; }
    public GraphPoint getEndPoint() { return endPoint; }
    public GraphPoint getStartPointOut() { return startPointOut; }
    public GraphPoint getEndPointTarget() { return endPointTarget; }

    public Graph getGraph() { return graph; }
    public LinkedList<GraphPoint> getPath() { return path; }
    public List<GraphEdge> getEdges() { return edges; }
    public List<GraphEdge> getEdgesAll() { return edgesAll; }
    public List<GraphEdge> getEdgesAllBad() { return edgesAllBad; }
    public List<OverGraphEdge> getEdgesOver() { return edgesOver; }

    public TerrainArea getTerrainAreas() { return terrainAreas; }
    public ArrayList<PojoObstacle> getObstacles() { return obstacles; }
    public List<LineGraphSeries<DataPoint>> getTerrainForPath() {
        return terrainForPath;
    }
    public List<PointsGraphSeries<DataPoint>> getPointsForPath() { return pointsForPath; }
    public List<PointsGraphSeries<DataPoint>> getPointsForObst() { return pointsForObst; }

    public MainBranch(LatLng g_startPoint, LatLng g_startPointOut, LatLng g_endPointTarget, LatLng g_endPoint,
                      int pilotNormalFlyHeight, Context context) {

        this.startPoint =     new GraphPoint(convertLatLongTo1992InMeters(g_startPoint.latitude,     g_startPoint.longitude), 0);
        this.endPoint =       new GraphPoint(convertLatLongTo1992InMeters(g_endPoint.latitude,       g_endPoint.longitude), 0);
        this.startPointOut  = new GraphPoint(convertLatLongTo1992InMeters(g_startPointOut.latitude,  g_startPointOut.longitude), pilotNormalFlyHeight);
        this.endPointTarget = new GraphPoint(convertLatLongTo1992InMeters(g_endPointTarget.latitude, g_endPointTarget.longitude), pilotNormalFlyHeight);
        this.pilotNormalFlyHeight = pilotNormalFlyHeight;
        this.context = context;

        this.lineOfPath = new LineDirectory(startPointOut, endPointTarget);

        mDBHelper = new DatabaseHelper(this.context);
        // przygotowanie bazy danych
        try {
            mDBHelper.updateDataBase();
        } catch (IOException mIOException) {
            throw new Error("UnableToUpdateDatabase");
        }

        // pobranie danych
        mDb = mDBHelper.getReadableDatabase();

        init();
    }

    private void init() {
        // wygeneruj graph
        this.graph = generateGraph();

        // znajdz sciezke
        DijkstraAlgorithm dijkstraAlgorithm = new DijkstraAlgorithm(graph);
         edges = new ArrayList<>();
        edgesAll = graph.getEdges();
        edgesAllBad = graph.getEdgesBad();
        edgesOver = graph.getEdgesOver();
        dijkstraAlgorithm.execute((startPoint));
        path = dijkstraAlgorithm.getPath(endPoint);
        if (path != null) {
            for (int it = 0; it < path.size() - 1; it++) {
                edges.add(dijkstraAlgorithm.findEdgeWithPoint(path.get(it), path.get(it+1)));
                edgesAll.remove(dijkstraAlgorithm.findEdgeWithPoint(path.get(it), path.get(it+1)));
            }
        }

        generateTerrainAreas();
        generateTerrainForPath();

        if (mDBHelper != null) {
            mDBHelper.close();
        }
    }

    private void generateTerrainForPath() {

        GraphPoint min_x, max_x, lastPoint = startPoint, newPoint;
        LineGraphSeries<DataPoint> linePath, lineTerrain;
        PointsGraphSeries<DataPoint> pointPath = new PointsGraphSeries<>();
        terrainForPath = new ArrayList<>();
        pointsForPath = new ArrayList<>();
        pointsForObst = new ArrayList<>();
        double distanceFromStartForTerrain = 0;
        double distanceFromStartForPath = 0;
        double lastHeight = this.pilotNormalFlyHeight;

        // DLA KAZDEJ KRAWEDZI
        for (GraphEdge edge : edges) {
            // START I KONIEC KRAWEDZI
            if (LineDirectory.distBetweenPoints(lastPoint, edge.getFrom())
                    < LineDirectory.distBetweenPoints(lastPoint, edge.getTo())) {
                min_x = edge.getFrom();
                max_x = edge.getTo();
            } else {
                max_x = edge.getFrom();
                min_x = edge.getTo();
            }

            List<GraphPoint> pointsInEdge = Arrays.asList(min_x, max_x);
            linePath = new LineGraphSeries<>();
            lineTerrain = new LineGraphSeries<>();
            List<GraphPoint> terrain;
            // POBIERZ PUNKTY Z BAZY
            terrain = DatabaseHelper.getTerrainForPoints(mDb, pointsInEdge);
            // DLA KAZDEEGO PUNKTU
            // KONSTRUOWANIE KRZYWEJ TERENU
            for (GraphPoint pointInEdge : pointsInEdge) {
                for (int i = 0; i < terrain.size(); i += 2) {
                    // DYSTANS OD STARTU DO PUNKTU
//                    newPoint = new GraphPoint(
//                            pointInEdge.x,
//                            pointInEdge.y,
//                            GraphPoint.getHeightForPoint(
//                                    terrain.get(i), terrain.get(i+1), terrain.get(i+2), terrain.get(i+3), pointInEdge)
//                    );
                    newPoint = terrain.get(i);
                    distanceFromStartForTerrain += LineDirectory.distBetweenPoints(lastPoint, newPoint);
                    if (distanceFromStartForTerrain > lineTerrain.getHighestValueX()) {
                        lineTerrain.appendData(
                                new DataPoint(distanceFromStartForTerrain, newPoint.z), false, 100);
                    }
                    lastPoint = newPoint;
                }
            }
            distanceFromStartForTerrain = LineDirectory.distBetweenPoints(startPoint, max_x);
            lastPoint = max_x;

            // WPISANIE KRZYWEJ TERENU
            lineTerrain.setColor(ContextCompat.getColor(context, R.color.graph_green));
            terrainForPath.add(lineTerrain);
            //distanceFromStartForTerrain += edge.getWeight();
            if (edge.getTo().equals(endPoint) || edge.getFrom().equals(endPoint)) {

            } else if (edge.getFrom().equals(startPoint) || edge.getTo().equals(startPoint)) {
                linePath.appendData(new DataPoint(distanceFromStartForPath,
                        0), false, 2);
                distanceFromStartForPath += edge.getWeight();
                linePath.appendData(new DataPoint(distanceFromStartForPath,
                        (max_x.z > this.pilotNormalFlyHeight) ? max_x.z : this.pilotNormalFlyHeight), false, 2);
                linePath.setColor(ContextCompat.getColor(context, R.color.graph_red));
                terrainForPath.add(linePath);
            } else {
                // KONSTRUOWANIE I WPISANIE KRYWEJ LOTU
                linePath.appendData(
                        new DataPoint(distanceFromStartForPath,
                                (min_x.z > this.pilotNormalFlyHeight) ? min_x.z : this.pilotNormalFlyHeight), false, 2);
                distanceFromStartForPath += edge.getWeight();
                if (min_x.z != 0) {
                    lastHeight = min_x.z;
                }
                linePath.appendData(
                        new DataPoint(distanceFromStartForPath,
                                (max_x.z > this.pilotNormalFlyHeight) ? max_x.z : this.pilotNormalFlyHeight), false, 2);
                linePath.setColor(ContextCompat.getColor(context, R.color.graph_red));
                terrainForPath.add(linePath);
                if (max_x.z != 0) {
                    lastHeight = max_x.z;
                }
            }
        }
        linePath = new LineGraphSeries<>();
        linePath.appendData(new DataPoint(distanceFromStartForPath - LineDirectory.distBetweenPoints(endPoint, endPointTarget),
                this.pilotNormalFlyHeight), false, 2);
        linePath.appendData(new DataPoint(distanceFromStartForPath,
                0), false, 2);
        linePath.setColor(ContextCompat.getColor(context, R.color.graph_red));
        terrainForPath.add(linePath);

        pointPath.appendData(new DataPoint(0, 0), false, 2);
        pointPath.appendData(new DataPoint(distanceFromStartForPath,
                0), false, 2);
        pointPath.setColor(ContextCompat.getColor(context, R.color.graph_yellow));
        pointsForPath.add(pointPath);
        pointPath = new PointsGraphSeries<>();
        pointPath.appendData(new DataPoint(LineDirectory.distBetweenPoints(startPoint, startPointOut),
                this.pilotNormalFlyHeight), false, 2);
        pointPath.setColor(ContextCompat.getColor(context, R.color.graph_red));
        pointsForPath.add(pointPath);
        pointPath = new PointsGraphSeries<>();
        pointPath.appendData(new DataPoint(distanceFromStartForPath - LineDirectory.distBetweenPoints(endPoint, endPointTarget),
                this.pilotNormalFlyHeight), false, 2);
        pointPath.setColor(ContextCompat.getColor(context, R.color.graph_red));
        pointsForPath.add(pointPath);


        for (GraphConnector obst : graph.getCirclesAll()) {
            PointsGraphSeries<DataPoint> barObst = new PointsGraphSeries<>();
            barObst.appendData(new DataPoint(LineDirectory.distBetweenPoints(startPoint, obst.getCentroid()),
                    obst.getObstacleHeight() + obst.getObstacleElevation()), false, 2);
            if (obst.getType() == GraphConnector.ObstacleType.SECURE_BUT_TOO_LOW) {
                barObst.setColor(ContextCompat.getColor(context, R.color.graph_black));
            } else if (obst.getType() == GraphConnector.ObstacleType.SECURE_BUT_SO_CLOSE) {
                barObst.setColor(ContextCompat.getColor(context, R.color.graph_dark_grey));
            } else if (obst.getType() == GraphConnector.ObstacleType.UNSECURE_FOR_NORMAL_HIGHT) {
                barObst.setColor(ContextCompat.getColor(context, R.color.graph_white));
            } else if (obst.getType() == GraphConnector.ObstacleType.UNSECURE_FOR_MAX_HIGHT) {
                barObst.setColor(ContextCompat.getColor(context, R.color.graph_white));
            } else {
                barObst.setColor(ContextCompat.getColor(context, R.color.graph_dark_grey));
            }
            //barObst.setDrawValuesOnTop(true);
            //barObst.setValuesOnTopColor(R.color.textPrimary);
            pointsForObst.add(barObst);
        }
    }

    private Graph generateGraph() {
        ArrayList<PojoObstacle> obstList = DatabaseHelper.getAreaObstacles(context, this.mDb, startPoint, endPoint);
        GenerateGraph generator = new GenerateGraph(context, startPoint, startPointOut, endPointTarget, endPoint,
                lineOfPath, pilotNormalFlyHeight, obstList, mDb);
        this.obstacles = generator.getObstaclesList();
        return generator.getGraph();
    }

    private void generateTerrainAreas() {
        ArrayList<PojoObstacle> obstList = DatabaseHelper.getAreaObstacles(context, mDb, startPoint, endPoint);
        terrainAreas = new TerrainArea(startPoint, endPoint, lineOfPath, obstList);
    }
}