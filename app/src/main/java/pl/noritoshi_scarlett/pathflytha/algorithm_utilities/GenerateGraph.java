package pl.noritoshi_scarlett.pathflytha.algorithm_utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Pair;

import com.google.android.gms.common.util.ArrayUtils;
import com.google.maps.android.geometry.Point;

import java.util.ArrayList;
import java.util.List;

import pl.noritoshi_scarlett.pathflytha.algorithm_utilities.geo_objects.ArcGraphEdge;
import pl.noritoshi_scarlett.pathflytha.algorithm_utilities.geo_objects.GraphConnector;
import pl.noritoshi_scarlett.pathflytha.algorithm_utilities.geo_objects.GraphEdge;
import pl.noritoshi_scarlett.pathflytha.algorithm_utilities.geo_objects.GraphPoint;
import pl.noritoshi_scarlett.pathflytha.algorithm_utilities.geo_objects.OverGraphEdge;
import pl.noritoshi_scarlett.pathflytha.algorithm_utilities.small_quests.CheckTerrain;
import pl.noritoshi_scarlett.pathflytha.algorithm_utilities.small_quests.CircleForObstacles;
import pl.noritoshi_scarlett.pathflytha.algorithm_utilities.geo_objects.Graph;
import pl.noritoshi_scarlett.pathflytha.pojos.PojoObstacle;
import pl.noritoshi_scarlett.pathflytha.algorithm_utilities.geo_objects.Circle;
import pl.noritoshi_scarlett.pathflytha.algorithm_utilities.geo_objects.LineDirectory;

import static java.lang.Math.pow;

public class GenerateGraph {

    // SETTINGS
    private final double MIN_DISTANCE_FROM_OBJECT;
    private final double MIN_DISTANCE_OVER_OBJECT;
    private final double MAX_DISTANCE_OVER;
    public final static double TOO_CLOSE_DISTANCE_FROM_OBJECT = 15800;
    private int pilotNormalFlyHeight;
    // CLIENTS AND ETC
    private Context context;
    private final SQLiteDatabase dataBase;
    // POINTS
    private GraphPoint startPoint;
    private GraphPoint startPointOut;
    private GraphPoint endPointTarget;
    private GraphPoint endPoint;
    // PATH
    private LineDirectory lineEquation;
    // OBSTACLES
    private int vObstCount;
    private int hObstCount;
    private double obstDensity;
    private ArrayList<PojoObstacle> obstaclesList;
    // GRAPH
    private List<GraphConnector> circlesToCalculate;
    private List<GraphConnector> selectedCircles;
    private Graph graph;
    private Graph.GraphBuilder graphBuilder;
    // AVOIDS
    private List<DoubleAvoidingSingleObstacle> obstaclesAvoids;


    public ArrayList<PojoObstacle> getObstaclesList() {
        return obstaclesList;
    }
    public Graph getGraph() {
        return graph;
    }
    public List<DoubleAvoidingSingleObstacle> getObstaclesAvoids() {
        return obstaclesAvoids;
    }

    GenerateGraph(
            Context context,
            GraphPoint startPoint, GraphPoint startPointOut, GraphPoint endPointTarget, GraphPoint endPoint,
            LineDirectory line,
            int pilotNormalFlyHeight,
            ArrayList<PojoObstacle> obstaclesList,
            SQLiteDatabase db) {

        this.context = context;
        this.startPoint =startPoint;
        this.startPointOut = startPointOut;
        this.endPointTarget = endPointTarget;
        this.endPoint = endPoint;
        this.pilotNormalFlyHeight = pilotNormalFlyHeight;
        this.obstaclesList = obstaclesList;
        this.obstaclesAvoids = new ArrayList<>();
        this.lineEquation = line;
        this.dataBase = db;

        circlesToCalculate = new ArrayList<>();
        selectedCircles = new ArrayList<>();

        graphBuilder = Graph.GraphBuilder.init();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.context);
        MIN_DISTANCE_FROM_OBJECT = Double.valueOf(prefs.getString("preference_minDistanceFromObstacle", "50"));
        MIN_DISTANCE_OVER_OBJECT = Double.valueOf(prefs.getString("preference_minDistanceOverObstacle", "100"));
        MAX_DISTANCE_OVER = Double.valueOf(prefs.getString("preference_maxDistanceOver", "10000"));

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

        //create circles
        createCirclesForAllObstacles();
        //select circles
        createConnectorsForAllCircles();
        //create edges between circles
        createConnectorsForSelectedCircles();
    }

    /**
     * Sprawdzenie przeszkod w okolicach punktu poczatkowego i koncowego
     * @return czy punkty są bezpieczne
     */
    private boolean checkMarkersForObstacles() {

        GraphPoint obstacleCenter;
        double distance, radius;
        for (int i = 0; i < obstaclesList.size(); i++) {

            obstacleCenter = new GraphPoint(
                    obstaclesList.get(i).getItem_obs_x(), obstaclesList.get(i).getItem_obs_y(),
                    obstaclesList.get(i).getItem_obs_elevation() + obstaclesList.get(i).getItem_obs_height());
            radius = Math.floor(MIN_DISTANCE_FROM_OBJECT + (obstaclesList.get(i).getItem_obs_range() / 2));
            distance = Math.ceil(LineDirectory.distBetweenPoints(
                    obstacleCenter, startPointOut));
            if (distance <= radius) {
                return false;
            }

            distance = Math.ceil(LineDirectory.distBetweenPoints(
                    obstacleCenter, endPointTarget));
            if (distance <= radius) {
                return false;
            }
        }
        return true;
    }

    /**
     * okregi dla punktow startu i końca oraz krawedzie je laczace
     * @return talica punktow w formie okregow je otaczajacych
     */
    private ArrayList<GraphConnector> createCirclesForMarkers() {

        LineDirectory lineForStart = new LineDirectory(startPoint, startPointOut);
        LineDirectory lineForEnd = new LineDirectory(endPointTarget, endPoint);
        LineDirectory lineNormalToStart = lineForStart.generateNormal(startPointOut);
        LineDirectory lineNormalToEnd = lineForEnd.generateNormal(endPointTarget);

        //TODO - promień zaraz po wylocie z lotniska (MIN_DISTANCE_FROM_OBJECT)
        List<LineDirectory> lineParallelsToStart = lineForStart.generateParallel(MIN_DISTANCE_FROM_OBJECT);
        List<LineDirectory> lineParallelsToEnd = lineForEnd.generateParallel(MIN_DISTANCE_FROM_OBJECT);

        Point point1 = LineDirectory.findPointForLines(lineNormalToStart, lineParallelsToStart.get(0));
        Point point2 = LineDirectory.findPointForLines(lineNormalToStart, lineParallelsToStart.get(1));
        Point point3 = LineDirectory.findPointForLines(lineNormalToEnd, lineParallelsToEnd.get(0));
        Point point4 = LineDirectory.findPointForLines(lineNormalToEnd, lineParallelsToEnd.get(1));

        // START CIRCLES AND EDGES
        ArrayList<GraphConnector> listOfCircles = new ArrayList<>();
        GraphEdge edge;
        listOfCircles.add(new GraphConnector(
                MIN_DISTANCE_FROM_OBJECT, new GraphPoint(point1, pilotNormalFlyHeight),
                0, 0, GraphConnector.ObstacleType.SECURE));
        listOfCircles.add(new GraphConnector(
                MIN_DISTANCE_FROM_OBJECT, new GraphPoint(point2, pilotNormalFlyHeight),
                0, 0, GraphConnector.ObstacleType.SECURE));
        edge = new GraphEdge(
                startPoint, startPointOut,
                LineDirectory.distBetweenPoints(startPoint, startPointOut));
        graphBuilder.addEdge(edge, Graph.EdgeType.EDGES_NORMAL);
        listOfCircles.get(0).addLinkFrom(new Pair<>(edge, startPointOut));
        listOfCircles.get(1).addLinkFrom(new Pair<>(edge, startPointOut));
        graphBuilder.addVertex(startPoint);
        graphBuilder.addVertex(startPointOut);

        // END CIRCLES AND EDGES
        listOfCircles.add(new GraphConnector(
                MIN_DISTANCE_FROM_OBJECT, new GraphPoint(point3, pilotNormalFlyHeight),
                0, 0, GraphConnector.ObstacleType.SECURE));
        listOfCircles.add(new GraphConnector(
                MIN_DISTANCE_FROM_OBJECT, new GraphPoint(point4, pilotNormalFlyHeight),
                0, 0, GraphConnector.ObstacleType.SECURE));
        edge = new GraphEdge(
                endPointTarget, endPoint,
                LineDirectory.distBetweenPoints(endPointTarget, endPoint));
        graphBuilder.addEdge(edge, Graph.EdgeType.EDGES_NORMAL);
        listOfCircles.get(2).addLinkTo(new Pair<>(edge, endPointTarget));
        listOfCircles.get(3).addLinkTo(new Pair<>(edge, endPointTarget));
        graphBuilder.addVertex(endPointTarget);
        graphBuilder.addVertex(endPoint);

        return listOfCircles;
    }

    /**
     * Wytyczenie okregow wokol przeszkod
     */
    private void createCirclesForAllObstacles() {

        double radius;
        boolean overMaxHeight;
        circlesToCalculate = createCirclesForMarkers();
        PojoObstacle obstacle;

        for (int i = 0; i < obstaclesList.size(); i++) {

            //TODO - zbyt blisko położone obiekty (długośc dcinka je łączacego?) łączyć w jeden
            obstacle = obstaclesList.get(i);
            if (obstacle.getItem_obs_elevation() + obstacle.getItem_obs_height() + MIN_DISTANCE_OVER_OBJECT > pilotNormalFlyHeight) {

                radius = Math.floor(MIN_DISTANCE_FROM_OBJECT) + obstacle.getItem_obs_range() / 2;
                // search for other obstacles around obstacle
                //CircleForObstacles searchOtherObstacles = new CircleForObstacles(
                //        obstaclesList, radius, obstaclesList.get(i));
                //circlesToCalculate.add(searchOtherObstacles.getGraphCircle());

                overMaxHeight = (obstacle.getItem_obs_elevation() + obstacle.getItem_obs_height()
                                + MIN_DISTANCE_OVER_OBJECT > MAX_DISTANCE_OVER);
                circlesToCalculate.add(
                        new GraphConnector(
                                radius, new GraphPoint(obstacle.getPoint(),
                                                obstacle.getItem_obs_elevation() + obstacle.getItem_obs_height()),
                                obstacle.getItem_obs_elevation(), obstacle.getItem_obs_height(),
                                overMaxHeight ? GraphConnector.ObstacleType.UNSECURE_FOR_MAX_HIGHT
                                              : GraphConnector.ObstacleType.UNSECURE_FOR_NORMAL_HIGHT
                        )
                );
            }
        }
    }

    /**
     * Wyszukiwanie okregow otaczajacych przeszkody, ktore znajduja sie na trasie lotu
     */
    private void createConnectorsForAllCircles() {

        List<Pair<GraphPoint, GraphPoint>> tangetPointsPairs;

        // UTWORZENIE OKREGOW DLA PUNKTOW POCZATKOWEGO I KONCOWEGO
        selectedCircles.addAll(circlesToCalculate.subList(0, 4));

        boolean getNewLoop = false;
        // DLA KAZDEGO OKREGU NOWEGO
        for (int i = 0; i < selectedCircles.size(); i++) {
            final List<Integer> listOfTo = new ArrayList<>();
            if (getNewLoop) {
                i = 0;
                getNewLoop = false;
            }
            // ITERUJ PO KAZDYM OKREGU
            for (int j = 0; j < selectedCircles.size(); j++) {
                // (Z WYJATKIEM SIEBIE)
                if (i == j) {
                    continue;
                }
                // POWIAZANIA MIEDZY PUNKTAMI STARTU I LADOWANIA
                if ((i == 0 && j == 1) || (i == 2 && j == 3)
                        || (i == 1 && j == 0) || (i == 3 && j == 2)) {
                    continue;
                }
                // ZNAJDZ MIEDZY OKREGAMI STYCZNE
                tangetPointsPairs = Circle.findTangetLinesPointForTwoObstacleCircles(
                        selectedCircles.get(i),
                        selectedCircles.get(j));
                // JESSLI JAKIES ZNALEZIONO
                if (tangetPointsPairs != null) {
                    // DLA KAZDEJ STYCZNEJ
                    for (int k = 0; k < tangetPointsPairs.size(); k++) {
                        // ZNAJDZ KOLIZYJNY OKRAG
                        int positionForConnect = (Circle.findFristColisionCircleForLine(
                                circlesToCalculate,
                                tangetPointsPairs.get(k).first,
                                tangetPointsPairs.get(k).second,
                                selectedCircles.get(i)
                        ));
                        if (positionForConnect != -1) {
                            // JESLI NIE ZAWIERA SIE TEN OKRAG W WYBRANYCH
                            if (! selectedCircles.contains(circlesToCalculate.get(positionForConnect))) {
                                selectedCircles.add(circlesToCalculate.get(positionForConnect));
                                getNewLoop = true;
                            }
                        }
                    }
                    if (!listOfTo.contains(j)) {
                        listOfTo.add(j);
                    }
                }
            }
        }
    }

    /**
     * Wyznaczenie powiazan pomiedzy okregami, budowa grafu
     */
    private void createConnectorsForSelectedCircles() {

        // SPRAWDZANIE TERENU
        final CheckTerrain checkTerrain = new CheckTerrain(context, pilotNormalFlyHeight);

        // WYLICZANIE POLACZEN MIEDZY OKREGAMI
        List<Pair<GraphPoint, GraphPoint>> tangetPointsPairs;
        GraphPoint graphPoint1, graphPoint2;

        int[] listForStartEnd = new int[]{0, 1, 2, 3};

        // DLA KAZDEGO OKREGU
        for (int i = 0; i < selectedCircles.size(); i++) {
            final List<Integer> listOfTo = new ArrayList<>();
            // ITERUJ PO KAZDYM OKREGU
            for (int j = 0; j < selectedCircles.size(); j++) {
                // (Z WYJATKIEM SIEBIE)
                if (i == j || listOfTo.contains(j)) {
                    continue;
                }
                // Z WYJATKIEM PUNKTU STARTOWEGO ZE STARTOWYM I KONCOWEGO Z KONCOWYM
                if ((i == 0 && j == 1) || (i == 2 && j == 3)
                        || (i == 1 && j == 0) || (i == 3 && j == 2)) {
                    continue;
                }
                // ZNAJDZ MIEDZY OKREGAMI STYCZNE
                tangetPointsPairs = Circle.findTangetLinesPointForTwoObstacleCircles(
                        selectedCircles.get(i),
                        selectedCircles.get(j));
                // JESSLI JAKIES ZNALEZIONO
                if (tangetPointsPairs != null) {
                    // DLA KAZDEGO ZNALEZIONEGO
                    for (int k = 0; k < tangetPointsPairs.size(); k++) {
                        // ZNAJDZ OKRAG KOLIZYJNY
                        int positionForConnect = (Circle.findFristColisionCircleForLine(
                                selectedCircles,
                                tangetPointsPairs.get(k).first,
                                tangetPointsPairs.get(k).second,
                                selectedCircles.get(i)
                        ));
                        Pair<GraphConnector, GraphPoint> pair;
                        // JESLI NEI MA KOLIZJI
                        if (positionForConnect == -1) {
                            // DODAJ PUNKTY
                            graphPoint1 = tangetPointsPairs.get(k).first;
                            if (GraphPoint.isUniquePoint(graphBuilder.getVertexes(), graphPoint1)) {
                                graphBuilder.addVertex(graphPoint1);
                            }
                            graphPoint2 = tangetPointsPairs.get(k).second;
                            if (GraphPoint.isUniquePoint(graphBuilder.getVertexes(), graphPoint2)) {
                                graphBuilder.addVertex(graphPoint2);
                            }
                            // DYSTANS
                            double distance = LineDirectory.distBetweenPoints(graphPoint1, graphPoint2);

                            // JESLI MOZNA POLACZYC DO CELU - LACZ POZIOMO, JSL NIE - PIONOWO (NAD)
                            if (checkTerrain.checkLine(tangetPointsPairs.get(k), dataBase)) {
                                //LACZNIE Z I DO J
                                pair = new Pair<>(
                                        selectedCircles.get(j),
                                        tangetPointsPairs.get(k).first);
                                selectedCircles.get(i).addConnectTo(pair);
                                if (GraphEdge.isUniqueEgde(graphBuilder.getEdges(), graphPoint1, graphPoint2)) {
                                    GraphEdge edge = new GraphEdge(graphPoint1, graphPoint2, distance);
                                    graphBuilder.addEdge(edge, Graph.EdgeType.EDGES_NORMAL);
                                    selectedCircles.get(i).addLinkTo(new Pair<>(edge, tangetPointsPairs.get(k).first));
                                    selectedCircles.get(j).addLinkFrom(new Pair<>(edge, tangetPointsPairs.get(k).second));
                                }
                                //LACZNIE Z J DO I
                                pair = new Pair<>(
                                        selectedCircles.get(i),
                                        tangetPointsPairs.get(k).second);
                                selectedCircles.get(j).addConnectFrom(pair);
                                if (GraphEdge.isUniqueEgde(graphBuilder.getEdges(), graphPoint2, graphPoint1)) {
                                    GraphEdge edge = new GraphEdge(graphPoint2, graphPoint1, distance);
                                    graphBuilder.addEdge(edge, Graph.EdgeType.EDGES_NORMAL);
                                    selectedCircles.get(j).addLinkTo(new Pair<>(edge, tangetPointsPairs.get(k).second));
                                    selectedCircles.get(i).addLinkFrom(new Pair<>(edge, tangetPointsPairs.get(k).first));
                                }
                            } else {
                                //LACZNIE Z I DO J
                                if (GraphEdge.isUniqueEgde(graphBuilder.getEdgesBad(), graphPoint1, graphPoint2)) {
                                    GraphEdge edge = new GraphEdge(graphPoint1, graphPoint2, distance);
                                    graphBuilder.addEdge(edge, Graph.EdgeType.EDGES_BAD);
                                }
                                //LACZNIE Z J DO I
                                if (GraphEdge.isUniqueEgde(graphBuilder.getEdgesBad(), graphPoint2, graphPoint1)) {
                                    GraphEdge edge = new GraphEdge(graphPoint2, graphPoint1, distance);
                                    graphBuilder.addEdge(edge, Graph.EdgeType.EDGES_BAD);
                                }
                            }
                        //JESLI NIE MA KOLIZJI I PUNKTY NIE NALEZA DO STARTU/LADOWANIA
                        } else if (! (ArrayUtils.contains(listForStartEnd, i) && ArrayUtils.contains(listForStartEnd, j))) {
                            tangetPointsPairs.get(k).first.z =
                                    selectedCircles.get(positionForConnect).getObstacleHeight()
                                            + selectedCircles.get(positionForConnect).getObstacleElevation()
                                            + MIN_DISTANCE_OVER_OBJECT;
                            tangetPointsPairs.get(k).second.z =
                                    selectedCircles.get(positionForConnect).getObstacleHeight()
                                            + selectedCircles.get(positionForConnect).getObstacleElevation()
                                            + MIN_DISTANCE_OVER_OBJECT;
                            OverGraphEdge edgeOver = new OverGraphEdge(
                                    tangetPointsPairs.get(k).first,
                                    tangetPointsPairs.get(k).second,
                                    selectedCircles.get(positionForConnect));
                            graphBuilder.addEdge(edgeOver, Graph.EdgeType.EDGES_OVER);
                            edgeOver = new OverGraphEdge(
                                    tangetPointsPairs.get(k).second,
                                    tangetPointsPairs.get(k).first,
                                    selectedCircles.get(positionForConnect));
                            graphBuilder.addEdge(edgeOver, Graph.EdgeType.EDGES_OVER);
                        }
                        listOfTo.add(j);
                    }
                }
            }
        }

        // BUDOWA KRAWEDZI W GRAFIE
        GraphEdge edge;
        GraphConnector circle;
        // DLA KAZDEGO OKREGU
        if (selectedCircles != null) {
            for (int i = 0; i < selectedCircles.size(); i++) {
                circle = selectedCircles.get(i);
                List<Pair<GraphEdge, GraphPoint>> listFrom = circle.getLinktFrom();
                List<Pair<GraphEdge, GraphPoint>> listTo = circle.getLinkTo();
                // OD KAZDEGO ZRODLA
                for (int j = 0; j < listFrom.size(); j++) {
                    // DO KAZDEGO CELU
                    for (int k = 0; k < listTo.size(); k++) {

                        if (LineDirectory.checkLinksForCircle(listFrom.get(j).first, listTo.get(k).first )) {
                            double distance = Circle.lengthOfCircleWithEdges(
                                   listFrom.get(j).first, listTo.get(k).first, circle.getRadius());
                            if (GraphEdge.isUniqueEgde(graphBuilder.getEdges(), listFrom.get(j).second, listTo.get(k).second)) {
                                edge = new ArcGraphEdge(
                                        listFrom.get(j).second, listTo.get(k).second,
                                        distance,
                                        circle.getCentroid(), circle.getRadius());
                                graphBuilder.addEdge(edge, Graph.EdgeType.EDGES_NORMAL);
                            }
                            if (GraphEdge.isUniqueEgde(graphBuilder.getEdges(), listTo.get(k).second, listFrom.get(j).second)) {
                                edge = new ArcGraphEdge(
                                        listTo.get(k).second, listFrom.get(j).second,
                                        distance,
                                        circle.getCentroid(), circle.getRadius());
                                graphBuilder.addEdge(edge, Graph.EdgeType.EDGES_NORMAL);
                            }
                        }

                        // JESLI TO POCZATKOWE
                        if (i < 2) {
                            if (LineDirectory.checkLinksForCircleAsPoints(
                                        startPointOut, listFrom.get(j).second, listFrom.get(j).first.getFrom(), startPoint )) {
                                double distance = Circle.lengthOfCircle(
                                        startPointOut, listFrom.get(j).second, listFrom.get(j).first.getFrom(), startPoint, circle.getRadius());
                                if (GraphEdge.isUniqueEgde(graphBuilder.getEdges(), startPointOut, listFrom.get(j).second)) {
                                    edge = new ArcGraphEdge(
                                            startPointOut, listFrom.get(j).second,
                                            distance,
                                            circle.getCentroid(), circle.getRadius());
                                    graphBuilder.addEdge(edge, Graph.EdgeType.EDGES_NORMAL);
                                }
                                if (GraphEdge.isUniqueEgde(graphBuilder.getEdges(), listFrom.get(j).second, startPointOut)) {
                                    edge = new ArcGraphEdge(
                                            listFrom.get(j).second, startPointOut,
                                            distance,
                                            circle.getCentroid(), circle.getRadius());
                                    graphBuilder.addEdge(edge, Graph.EdgeType.EDGES_NORMAL);
                                }
                            }
                            if (LineDirectory.checkLinksForCircleAsPoints(
                                        startPointOut, listTo.get(k).second, listTo.get(k).first.getTo(), startPoint )) {
                                double distance = Circle.lengthOfCircle(
                                        startPointOut, listTo.get(k).second, listTo.get(k).first.getTo(), startPoint, circle.getRadius());
                                if (GraphEdge.isUniqueEgde(graphBuilder.getEdges(), startPointOut, listTo.get(k).second)) {
                                    edge = new ArcGraphEdge(
                                            startPointOut, listTo.get(k).second,
                                            distance,
                                            circle.getCentroid(), circle.getRadius());
                                    graphBuilder.addEdge(edge, Graph.EdgeType.EDGES_NORMAL);
                                }
                                if (GraphEdge.isUniqueEgde(graphBuilder.getEdges(), listTo.get(k).second, startPointOut)) {
                                    edge = new ArcGraphEdge(
                                            listTo.get(k).second, startPointOut,
                                            distance,
                                            circle.getCentroid(), circle.getRadius());
                                    graphBuilder.addEdge(edge, Graph.EdgeType.EDGES_NORMAL);
                                }
                            }
                        // JESLI TO KONCOWE
                        } else if (i < 4) {
                            if (LineDirectory.checkLinksForCircleAsPoints(
                                        endPointTarget, listFrom.get(j).second, listFrom.get(j).first.getFrom(), endPoint )) {
                                double distance = Circle.lengthOfCircle(
                                        endPointTarget, listFrom.get(j).second, listFrom.get(j).first.getFrom(), endPoint, circle.getRadius());
                                if (GraphEdge.isUniqueEgde(graphBuilder.getEdges(), endPointTarget, listFrom.get(j).second)) {
                                    edge = new ArcGraphEdge(
                                            endPointTarget, listFrom.get(j).second,
                                            distance,
                                            circle.getCentroid(), circle.getRadius());
                                    graphBuilder.addEdge(edge, Graph.EdgeType.EDGES_NORMAL);
                                }
                                if (GraphEdge.isUniqueEgde(graphBuilder.getEdges(), listFrom.get(j).second, endPointTarget)) {
                                    edge = new ArcGraphEdge(
                                            listFrom.get(j).second, endPointTarget,
                                            distance,
                                            circle.getCentroid(), circle.getRadius());
                                    graphBuilder.addEdge(edge, Graph.EdgeType.EDGES_NORMAL);
                                }
                            }
                            if (LineDirectory.checkLinksForCircleAsPoints(
                                        endPointTarget, listTo.get(k).second, listTo.get(k).first.getTo(), endPoint )) {
                                double distance = Circle.lengthOfCircle(
                                        endPointTarget, listTo.get(k).second, listTo.get(k).first.getTo(), endPoint, circle.getRadius());
                                if (GraphEdge.isUniqueEgde(graphBuilder.getEdges(), endPointTarget, listTo.get(k).second)) {
                                    edge = new ArcGraphEdge(
                                            endPointTarget, listTo.get(k).second,
                                            distance,
                                            circle.getCentroid(), circle.getRadius());
                                    graphBuilder.addEdge(edge, Graph.EdgeType.EDGES_NORMAL);
                                }
                                if (GraphEdge.isUniqueEgde(graphBuilder.getEdges(), listTo.get(k).second, endPointTarget)) {
                                    edge = new ArcGraphEdge(
                                            listTo.get(k).second, endPointTarget,
                                            distance,
                                            circle.getCentroid(), circle.getRadius());
                                    graphBuilder.addEdge(edge, Graph.EdgeType.EDGES_NORMAL);
                                }
                            }
                        }
                    }
                }
            }

            checkedOtherCircles();

            this.graph = graphBuilder.build(selectedCircles, circlesToCalculate);
        }
    }


    /**
     * Oznaczenie pozostalych przeszkod
     */
    private void checkedOtherCircles() {

        double radius;
        PojoObstacle obstacle;

        for (int i = 0; i < obstaclesList.size(); i++) {

            obstacle = obstaclesList.get(i);
            if (obstacle.getItem_obs_elevation() + obstacle.getItem_obs_height() + MIN_DISTANCE_OVER_OBJECT <= pilotNormalFlyHeight) {

                radius = Math.floor(MIN_DISTANCE_FROM_OBJECT) + obstacle.getItem_obs_range() / 2;

                circlesToCalculate.add(
                        new GraphConnector(
                                radius, new GraphPoint(obstacle.getPoint(),
                                obstacle.getItem_obs_elevation() + obstacle.getItem_obs_height()),
                                obstacle.getItem_obs_elevation(), obstacle.getItem_obs_height(),
                                GraphConnector.ObstacleType.SECURE_BUT_TOO_LOW
                        )
                );
            }
        }
    }

    private void findConflicts_newObstacle() {
        if (obstaclesList.isEmpty()) {
            //none
        } else {
            double distance, radius;
            Point lastPointInPath = new Point(startPointOut.x, startPointOut.y);
            Point centerOfCurrentObstacle;

            for (int i = 0; i < obstaclesList.size(); i++) {

                centerOfCurrentObstacle = obstaclesList.get(i).getPoint();
                distance = Math.ceil(LineDirectory.distBetweenPointAndLine(centerOfCurrentObstacle, lineEquation));
                radius = Math.floor(MIN_DISTANCE_FROM_OBJECT + (obstaclesList.get(i).getItem_obs_range() / 2));

                if (distance <= radius && obstaclesList.get(i).getItem_obs_x() > lastPointInPath.x) {
                    obstaclesList.get(i).setSelected(true);

                    CircleForObstacles searchOtherObstacles = new CircleForObstacles(this.context, obstaclesList, radius, obstaclesList.get(i));
                    radius = searchOtherObstacles.getRadius();

                    obstaclesAvoids.add(getPathsForCollision(
                            lastPointInPath, lineEquation,
                            searchOtherObstacles.getCentroid(),
                            radius, distance));

                    lastPointInPath = obstaclesAvoids.get(obstaclesAvoids.size() - 1).getShortPath().getEndPoint();

                } else if (distance <= TOO_CLOSE_DISTANCE_FROM_OBJECT) {
                    obstaclesList.get(i).setTooClose(true);
                }
            }
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

            // 5. Wyznaczenie punktów stycznosci okregów
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
