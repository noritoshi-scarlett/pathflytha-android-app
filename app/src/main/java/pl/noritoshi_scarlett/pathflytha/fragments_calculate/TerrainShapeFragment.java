package pl.noritoshi_scarlett.pathflytha.fragments_calculate;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.maps.android.geometry.Point;
import com.jjoe64.graphview.GraphView;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.util.ArrayList;
import java.util.List;

import pl.noritoshi_scarlett.pathflytha.Pathflytha;
import pl.noritoshi_scarlett.pathflytha.algorithm_utilities.AvoidingSingleObstacle;
import pl.noritoshi_scarlett.pathflytha.algorithm_utilities.DoubleAvoidingSingleObstacle;
import pl.noritoshi_scarlett.pathflytha.algorithm_utilities.MainBranch;
import pl.noritoshi_scarlett.pathflytha.algorithm_utilities.TerrainArea;
import pl.noritoshi_scarlett.pathflytha.algorithm_utilities.geo_objects.ArcGraphEdge;
import pl.noritoshi_scarlett.pathflytha.algorithm_utilities.geo_objects.GraphConnector;
import pl.noritoshi_scarlett.pathflytha.algorithm_utilities.geo_objects.GraphEdge;
import pl.noritoshi_scarlett.pathflytha.algorithm_utilities.geo_objects.Graph;
import pl.noritoshi_scarlett.pathflytha.algorithm_utilities.geo_objects.GraphPoint;
import pl.noritoshi_scarlett.pathflytha.algorithm_utilities.geo_objects.OverGraphEdge;
import pl.noritoshi_scarlett.pathflytha.fragments_main.LegendRecyclerViewAdapter;
import pl.noritoshi_scarlett.pathflytha.pojos.PojoObstacle;
import pl.noritoshi_scarlett.pathflytha.R;
import pl.noritoshi_scarlett.pathflytha.activities.CalculateActivity;


/**
 * A simple {@link Fragment} subclass.
 */
public class TerrainShapeFragment extends Fragment implements CalculateActivity.OnPathDataReceivedListener {

    private Context context;
    private GraphView graphView;
    private List<Pair<Integer,String>> legend;
    private RecyclerView legendView;

    private Point start;
    private Point end;
    private double flyHeightNormal;

    public TerrainShapeFragment() {
    }

    public static TerrainShapeFragment init(int page) {
        Bundle args = new Bundle();
        args.putInt(Pathflytha.ARG_PAGE_NUMBER, page);

        TerrainShapeFragment fragment = new TerrainShapeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_terrain_shape, container, false);

        context = getContext();
        graphView = view.findViewById(R.id.pathGraphView);
        legendView = view.findViewById(R.id.graphLegend);

        return view;
    }

    public void drawPath(MainBranch branch, int setup_ID, double flyHeightNormal) {

        this.flyHeightNormal = flyHeightNormal;

        if (setup_ID == 0) {
            drawPointsForGraph(branch.getGraph(), branch.getEdges(), branch.getEdgesAll(), branch.getEdgesAllBad(),
                    branch.getEdgesOver(), branch.getObstacles(),
                    branch.getStartPoint(), branch.getEndPoint(), branch.getStartPointOut(), branch.getEndPointTarget());
        } else if (setup_ID == 1) {
            drawPointsForGraph(branch.getTerrainAreas(),
                    branch.getStartPoint(), branch.getEndPoint(), branch.getStartPointOut(), branch.getEndPointTarget());
        }
    }

    public void drawPointsForGraph(Graph graph, List<GraphEdge> edges, List<GraphEdge> edgesAll, List<GraphEdge> edgesAllBad,
                                   List<OverGraphEdge> edgesOver,
                                   List<PojoObstacle> obstacles, GraphPoint start, GraphPoint end, GraphPoint out, GraphPoint target) {

        // INICJALIZACJA WYKRESU
        if (graphView != null) {
            graphView.getViewport().setScalable(true);
            graphView.getViewport().setScrollable(true);
            graphView.getViewport().setScalableY(true);
            graphView.getViewport().setScrollableY(true);

            graphView.setVerticalScrollBarEnabled(true);
            graphView.setHorizontalScrollBarEnabled(true);

            graphView.getGridLabelRenderer().setHumanRounding(true);
            // enable scaling and scrolling
            graphView.refreshDrawableState();

            // POJEDYNCZY WYKRES
            LineGraphSeries<DataPoint> lineOfCalcPathes = new LineGraphSeries<>();
            addToLegend(R.color.graph_green, getResources().getStringArray(R.array.calc_legend_graph)[5]);
            // ITERACJA PO OBIEKTACH GRAFU
            for (OverGraphEdge edge : edgesOver) {
                if (edge.isDuplicate()) {
                    continue;
                }
                if (edge.getColisionOverPoints().size() > 0) {
                    // JESLI SA KOLIZYJNE Z JAKIMS OKREGIEM
                    if (edge.getFrom().x < edge.getColisionOverPoints().get(0).x) {
                        lineOfCalcPathes.appendData(
                                new DataPoint(edge.getFrom().x, edge.getFrom().y), false, 10);
                        lineOfCalcPathes.appendData(
                                new DataPoint(edge.getColisionOverPoints().get(0).x, edge.getColisionOverPoints().get(0).y), false, 10);
                    } else {
                        lineOfCalcPathes.appendData(
                                new DataPoint(edge.getColisionOverPoints().get(0).x, edge.getColisionOverPoints().get(0).y), false, 10);
                        lineOfCalcPathes.appendData(
                                new DataPoint(edge.getFrom().x, edge.getFrom().y), false, 10);
                    }
                    lineOfCalcPathes.setColor(ContextCompat.getColor(context, R.color.graph_green));
                    graphView.addSeries(lineOfCalcPathes);
                    lineOfCalcPathes = new LineGraphSeries<>();
                    GraphPoint next = edge.getColisionOverPoints().get(0);
                    // JESLI KOLIZYJNE PUNKT SA PRZYNAJMNIEJ DWA
                    if (edge.getColisionOverPoints().size() > 1) {
                        next = edge.getColisionOverPoints().get(1);
                        if (edge.getColisionOverPoints().get(0).x < next.x) {
                            lineOfCalcPathes.appendData(
                                    new DataPoint(edge.getColisionOverPoints().get(0).x, edge.getColisionOverPoints().get(0).y), false, 10);
                            lineOfCalcPathes.appendData(
                                    new DataPoint(next.x, next.y), false, 10);
                        } else {
                            lineOfCalcPathes.appendData(
                                    new DataPoint(next.x, next.y), false, 10);
                            lineOfCalcPathes.appendData(
                                    new DataPoint(edge.getColisionOverPoints().get(0).x, edge.getColisionOverPoints().get(0).y), false, 10);
                        }
                        lineOfCalcPathes.setColor(ContextCompat.getColor(context, R.color.graph_green));
                        graphView.addSeries(lineOfCalcPathes);
                        lineOfCalcPathes = new LineGraphSeries<>();
                    }
                    // OSATNI LACZNIK
                    if (next.x < edge.getTo().x) {
                        lineOfCalcPathes.appendData(
                                new DataPoint(next.x, next.y), false, 10);
                        lineOfCalcPathes.appendData(
                                new DataPoint(edge.getTo().x, edge.getTo().y), false, 10);
                    } else {
                        lineOfCalcPathes.appendData(
                                new DataPoint(edge.getTo().x, edge.getTo().y), false, 10);
                        lineOfCalcPathes.appendData(
                                new DataPoint(next.x, next.y), false, 10);
                    }
                    lineOfCalcPathes.setColor(ContextCompat.getColor(context, R.color.graph_green));
                    graphView.addSeries(lineOfCalcPathes);
                    lineOfCalcPathes = new LineGraphSeries<>();
                    // NIE MA PUNKTOW KOLIZYJNYCH WEC POLACZ POZATEK Z KONCEM TYLKO
                } else {
                    if (edge.getFrom().x < edge.getTo().x) {
                        lineOfCalcPathes.appendData(
                                new DataPoint(edge.getFrom().x, edge.getFrom().y), false, 10);
                        lineOfCalcPathes.appendData(
                                new DataPoint(edge.getTo().x, edge.getTo().y), false, 10);
                    } else {
                        lineOfCalcPathes.appendData(
                                new DataPoint(edge.getTo().x, edge.getTo().y), false, 10);
                        lineOfCalcPathes.appendData(
                                new DataPoint(edge.getFrom().x, edge.getFrom().y), false, 10);
                    }
                    lineOfCalcPathes.setColor(ContextCompat.getColor(context, R.color.graph_green));
                    graphView.addSeries(lineOfCalcPathes);
                    lineOfCalcPathes = new LineGraphSeries<>();
                }
            }

            // POJEDYNCZY WYKRES
            lineOfCalcPathes = new LineGraphSeries<>();
            addToLegend(R.color.graph_grey, getResources().getStringArray(R.array.calc_legend_graph)[6]);
            // ITERACJA PO OBIEKTACH GRAFU
            for (GraphEdge edge : edgesAllBad) {
                if (edge.isDuplicate()) {
                    continue;
                }
                List<LineGraphSeries<DataPoint>> lines;
                if (edge instanceof ArcGraphEdge) {
                    lines = ((ArcGraphEdge) edge).getPointsForGraph();
                } else {
                    lines = edge.getPointsForGraph();
                }
                if (lines != null) {
                    for (LineGraphSeries<DataPoint> line : lines) {
                        line.setColor(ContextCompat.getColor(context, R.color.graph_blue));
                        graphView.addSeries(line);
                    }
                }
            }

            // POJEDYNCZY WYKRES
            addToLegend(R.color.graph_blue, getResources().getStringArray(R.array.calc_legend_graph)[4]);
            // ITERACJA PO OBIEKTACH GRAFU
            for (GraphEdge edge : edgesAll) {
                if (edge.isDuplicate()) {
                    continue;
                }
                List<LineGraphSeries<DataPoint>> lines;
                if (edge instanceof ArcGraphEdge) {
                    lines = ((ArcGraphEdge) edge).getPointsForGraph();
                } else {
                    lines = edge.getPointsForGraph();
                }
                if (lines != null) {
                    for (LineGraphSeries<DataPoint> line : lines) {
                        line.setColor(ContextCompat.getColor(context, R.color.graph_blue));
                        graphView.addSeries(line);
                    }
                }
            }

            //OKREGI
            boolean fristAvoid = true;
            boolean fristStartEnd = true;
            if (graph.getCirclesChecked() != null) {
                for (int i = 0; i < graph.getCirclesChecked().size(); i++) {
                    for (LineGraphSeries<DataPoint> line : graph.getCirclesChecked().get(i).generateXYFunction()) {
                        if (line != null) {
                            if (i < 4) {
                                line.setColor(ContextCompat.getColor(context, R.color.graph_yellow));
                                if (fristStartEnd) {
                                    addToLegend(R.color.graph_yellow, getResources().getStringArray(R.array.calc_legend_graph)[3]);
                                    fristStartEnd = false;
                                }
                            } else {
                                line.setColor(ContextCompat.getColor(context, R.color.graph_red));
                                if (fristAvoid) {
                                    addToLegend(R.color.graph_red, getResources().getStringArray(R.array.calc_legend_graph)[2]);
                                    fristAvoid = false;
                                }
                            }
                            graphView.addSeries(line);
                        }
                    }
                }
            }

            // ITERACJA PO SCIEZCE GRAFU
            boolean fristCalculatedPath = true;
            if (edges != null) {
                for (GraphEdge edge : edges) {
                    List<LineGraphSeries<DataPoint>> lines;
                    if (edge instanceof ArcGraphEdge) {
                        lines = ((ArcGraphEdge) edge).getPointsForGraph();
                    } else {
                       lines = edge.getPointsForGraph();
                    }
                    if (lines != null) {
                        for (LineGraphSeries<DataPoint> line : lines) {
                            if (fristCalculatedPath) {
                                addToLegend(R.color.graph_white, getResources().getStringArray(R.array.calc_legend_graph)[8]);
                                fristCalculatedPath = false;
                            }
                            line.setColor(ContextCompat.getColor(context, R.color.graph_white));
                            graphView.addSeries(line);
                        }
                    }
                }
            }

        }
        // START AND END POINTS
        PointsGraphSeries<DataPoint> pointsOfPath = new PointsGraphSeries<>();
        if (start.x < end.x) {
            pointsOfPath.appendData(
                    new DataPoint(start.x, start.y), false, 2);
            pointsOfPath.appendData(
                    new DataPoint(end.x, end.y), false, 2);
        } else {
            pointsOfPath.appendData(
                    new DataPoint(end.x, end.y), false, 2);
            pointsOfPath.appendData(
                    new DataPoint(start.x, start.y), false, 2);
        }
        pointsOfPath.setColor(ContextCompat.getColor(context, R.color.graph_yellow));
        graphView.addSeries(pointsOfPath);

        // OUT TARGET POINTS AND LINE
        pointsOfPath = new PointsGraphSeries<>();
        LineGraphSeries<DataPoint> lineOfPath = new LineGraphSeries<>();
        if (out.x < target.x) {
            pointsOfPath.appendData(
                    new DataPoint(out.x, out.y), false, 2);
            pointsOfPath.appendData(
                    new DataPoint(target.x, target.y), false, 2);
            lineOfPath.appendData(
                    new DataPoint(out.x, out.y), false, 2);
            lineOfPath.appendData(
                    new DataPoint(target.x, target.y), false, 2);
        } else {
            pointsOfPath.appendData(
                    new DataPoint(target.x, target.y), false, 2);
            pointsOfPath.appendData(
                    new DataPoint(out.x, out.y), false, 2);
            lineOfPath.appendData(
                    new DataPoint(target.x, target.y), false, 2);
            lineOfPath.appendData(
                    new DataPoint(out.x, out.y), false, 2);
        }
        lineOfPath.setColor(ContextCompat.getColor(context, R.color.graph_dark_red));
        graphView.addSeries(lineOfPath);
        pointsOfPath.setColor(ContextCompat.getColor(context, R.color.graph_red));
        graphView.addSeries(pointsOfPath);

        //OKREGI - SZARE (JASNE I CIEMNE)
        PointsGraphSeries<DataPoint> pointsOfObst = new PointsGraphSeries<>();
        if (graph.getCirclesAll() != null) {
            for (int i = 4; i < graph.getCirclesAll().size(); i++) {
                pointsOfObst.appendData(
                        new DataPoint(graph.getCirclesAll().get(i).getCentroid().x, graph.getCirclesAll().get(i).getCentroid().y),
                        false, 10);
                if (graph.getCirclesAll().get(i).getType().equals(GraphConnector.ObstacleType.SECURE_BUT_TOO_LOW)) {
                    pointsOfObst.setColor(ContextCompat.getColor(context, R.color.graph_dark_grey));
                } else {
                    pointsOfObst.setColor(ContextCompat.getColor(context, R.color.graph_black));
                }
                graphView.addSeries(pointsOfObst);
                pointsOfObst = new PointsGraphSeries<>();
            }
        }

        //OBSTACLES-OKREGI-BIALE
        if (graph.getCirclesChecked() != null) {
            for (int i = 4; i < graph.getCirclesChecked().size(); i++) {
                pointsOfObst.appendData(
                        new DataPoint(graph.getCirclesChecked().get(i).getCentroid().x, graph.getCirclesChecked().get(i).getCentroid().y),
                        false, 10);
                pointsOfObst.setColor(ContextCompat.getColor(context, R.color.graph_white));
                graphView.addSeries(pointsOfObst);
                pointsOfObst = new PointsGraphSeries<>();
            }
        }

        //LEGENDA
        addToLegend(R.color.graph_white,      getResources().getStringArray(R.array.calc_legend_graph)[0]);
        addToLegend(R.color.graph_black,      getResources().getStringArray(R.array.calc_legend_graph)[11]);
        addToLegend(R.color.graph_dark_red,   getResources().getStringArray(R.array.calc_legend_graph)[7]);
        addToLegend(R.color.graph_yellow,     getResources().getStringArray(R.array.calc_legend_graph)[9]);
        addToLegend(R.color.graph_red,        getResources().getStringArray(R.array.calc_legend_graph)[10]);
        addLegend();
        // KONCOWE USTAWIENIA WYKRESU
        //graphView.getGridLabelRenderer().setHumanRounding(true);
        graphView.refreshDrawableState();


    }

    private void drawPointsForGraph(TerrainArea areas, GraphPoint start, GraphPoint end, GraphPoint out, GraphPoint target) {

        if (graphView != null) {
            graphView.getViewport().setScalable(true);
            graphView.getViewport().setScrollable(true);
            graphView.getViewport().setScalableY(true);
            graphView.getViewport().setScrollableY(true);

            graphView.setVerticalScrollBarEnabled(true);
            graphView.setHorizontalScrollBarEnabled(true);

            graphView.getGridLabelRenderer().setHumanRounding(true);
            // enable scaling and scrolling
            graphView.refreshDrawableState();

            List<DoubleAvoidingSingleObstacle> obstacles = areas.getObstaclesAvoids();
            LineGraphSeries<DataPoint> lineOfCalcPathes = new LineGraphSeries<>();

            if (obstacles.size() > 0) {

                for (int i = 0; i < obstacles.size(); i++) {
                    DoubleAvoidingSingleObstacle obsAv = obstacles.get(i);
                    AvoidingSingleObstacle avoid = obsAv.getShortPath();

                    List<List<LineGraphSeries<DataPoint>>> series = avoid.generateXYFunction();
                    for (List<LineGraphSeries<DataPoint>> lines : series) {
                        for (LineGraphSeries<DataPoint> line : lines) {
                            line.setColor(ContextCompat.getColor(context, R.color.colorAccent));
                            graphView.addSeries(line);
                        }
                    }

                    if (lineOfCalcPathes.getHighestValueX() < avoid.getStartPoint().x) {
                        lineOfCalcPathes.appendData(new DataPoint(avoid.getStartPoint().x, avoid.getStartPoint().y),
                                false, 2);
                    }

                    if (lineOfCalcPathes.getLowestValueX() < lineOfCalcPathes.getHighestValueX()) {
                        graphView.addSeries(lineOfCalcPathes);
                    }
                    lineOfCalcPathes = new LineGraphSeries<>();

                    if (lineOfCalcPathes.getHighestValueX() < avoid.getEndPoint().x) {
                        lineOfCalcPathes.appendData(new DataPoint(avoid.getEndPoint().x, avoid.getEndPoint().y),
                                false, 2);
                    }


                }
                //lineOfPath.appendData(new DataPoint(areas.getEndPoint().x, areas.getEndPoint().y),
                //        false, 2);
                //graphView.addSeries(lineOfPath);
            } else {
                LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
                series.appendData(new DataPoint(areas.getStartPoint().x, areas.getStartPoint().y), false, 2);
                series.appendData(new DataPoint(areas.getEndPoint().x, areas.getEndPoint().y), false, 2);
                series.setColor(ContextCompat.getColor(context, R.color.colorAccent));
                graphView.addSeries(series);
            }

            PointsGraphSeries<DataPoint> pointSeriesAvoids = new PointsGraphSeries<>();
            PointsGraphSeries<DataPoint> pointSeriesClose = new PointsGraphSeries<>();
            List<PojoObstacle> obstaclesList = areas.getObstaclesList();

            for (int i = 0; i < obstaclesList.size(); i++) {
                if (obstaclesList.get(i).isTooClose()) {
                    pointSeriesClose.appendData(
                            new DataPoint(obstaclesList.get(i).getItem_obs_x(), obstaclesList.get(i).getItem_obs_y()),
                            false, obstaclesList.size());
                }
                if (obstaclesList.get(i).isSelected()) {
                    pointSeriesAvoids.appendData(
                            new DataPoint(obstaclesList.get(i).getItem_obs_x(), obstaclesList.get(i).getItem_obs_y()),
                            false, obstaclesList.size());
                }
            }
            if (pointSeriesClose.getSize() > 0) {
                pointSeriesClose.setColor(ContextCompat.getColor(context, R.color.textGray));
                graphView.addSeries(pointSeriesClose);
            }
            if (pointSeriesAvoids.getSize() > 0) {
                pointSeriesAvoids.setColor(ContextCompat.getColor(context, R.color.textPrimary));
                graphView.addSeries(pointSeriesAvoids);
            }
        } else {
            if (getActivity() != null) {
                Toast toast = Toast.makeText(getActivity().getApplicationContext(), "nie ma grafu!", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

    public void addToLegend(Integer color, String title) {

        if (legend == null) {
            legend = new ArrayList<>();
        }
        legend.add(new Pair<>(color, title));
    }

    public void addLegend() {
        LegendRecyclerViewAdapter adapter = new LegendRecyclerViewAdapter(context, legend);
        legendView.setAdapter(adapter);
        legendView.setLayoutManager(new LinearLayoutManager(context));
    }

    @Override
    public void onDataReceived(Point start, Point end) {
        this.start = start;
        this.end = end;
    }
}
