package pl.noritoshi_scarlett.pathflytha.fragments_calculate;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.geometry.Point;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.util.ArrayList;
import java.util.List;

import pl.noritoshi_scarlett.pathflytha.Pathflytha;
import pl.noritoshi_scarlett.pathflytha.R;
import pl.noritoshi_scarlett.pathflytha.algorithm_utilities.MainBranch;
import pl.noritoshi_scarlett.pathflytha.algorithm_utilities.geo_objects.GraphPoint;
import pl.noritoshi_scarlett.pathflytha.algorithm_utilities.small_quests.LatLongConverter;
import pl.noritoshi_scarlett.pathflytha.fragments_main.LegendRecyclerViewAdapter;
import pl.noritoshi_scarlett.pathflytha.pojos.PojoObstacle;
import pl.noritoshi_scarlett.pathflytha.pojos.PojoTerrain;


/**
 * A simple {@link Fragment} subclass.
 */
public class HeightChartFragment extends Fragment {

    private Context context;
    private GraphView graphView;
    private List<Pair<Integer,String>> legend;
    private RecyclerView legendView;

    private GraphPoint start;
    private GraphPoint end;

    public HeightChartFragment() {}

    public static HeightChartFragment init(int page) {
        Bundle args = new Bundle();
        args.putInt(Pathflytha.ARG_PAGE_NUMBER, page);

        HeightChartFragment fragment = new HeightChartFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_terrain_shape, container, false);

        context = getContext();
        graphView = view.findViewById(R.id.pathGraphView);
        legendView = view.findViewById(R.id.graphLegend);

        return view;
    }

    public void drawPath(MainBranch branch) {

        List<LineGraphSeries<DataPoint>> terrain = branch.getTerrainForPath();
        List<PointsGraphSeries<DataPoint>> points = branch.getPointsForPath();
        List<PointsGraphSeries<DataPoint>> bars = branch.getPointsForObst();
        start = branch.getStartPoint();
        end = branch.getEndPoint();

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

            // TERRAIN
            for (int i = 0; i < terrain.size(); i++) {
                if (terrain.get(i) != null) {
                    if ( ! terrain.get(i).isEmpty()) {
                        graphView.addSeries(terrain.get(i));
                    }
                }
            }
            for (int i = 0; i < points.size(); i++) {
                if (points.get(i) != null) {
                    if ( ! points.get(i).isEmpty()) {
                        graphView.addSeries(points.get(i));
                    }
                }
            }
            for (int i = 0; i < bars.size(); i++) {
                if (bars.get(i) != null) {
                    if ( ! bars.get(i).isEmpty()) {
                        graphView.addSeries(bars.get(i));
                    }
                }
            }

//            List<PojoObstacle> obstacles = branch.getObstacles();
//            // OBSTACLES
//            PointsGraphSeries<DataPoint> pointsOfObst = new PointsGraphSeries<>();
//            for (int i = 0; i < obstacles.size(); i++) {
//                pointsOfObst.appendData(
//                        new DataPoint(obstacles.get(i).getItem_obs_x(), obstacles.get(i).getItem_obs_y()),
//                        false, 10);
//                pointsOfObst.setColor(ContextCompat.getColor(
//                        context,
//                        (obstacles.get(i).isSelected()) ? R.color.graph_white : R.color.graph_grey));
//                graphView.addSeries(pointsOfObst);
//                pointsOfObst = new PointsGraphSeries<>();
//            }

        } else {
            if (getActivity() != null) {
                Toast toast = Toast.makeText(getActivity().getApplicationContext(), "nie ma wysokosci terenu!", Toast.LENGTH_SHORT);
                toast.show();
            }
        }


        addToLegend(R.color.graph_white,    getResources().getStringArray(R.array.calc_legend_height)[0]);
        addToLegend(R.color.graph_grey,     getResources().getStringArray(R.array.calc_legend_height)[1]);
        addToLegend(R.color.graph_green,    getResources().getStringArray(R.array.calc_legend_height)[2]);
        addToLegend(R.color.graph_red,      getResources().getStringArray(R.array.calc_legend_height)[3]);
        addToLegend(R.color.graph_dark_red, getResources().getStringArray(R.array.calc_legend_height)[4]);
        addToLegend(R.color.graph_yellow,   getResources().getStringArray(R.array.calc_legend_height)[5]);
        addLegend();
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


    public void draw(ArrayList<PojoTerrain> terrainBase, LatLng start, LatLng end) {

        double min_long, min_lat, max_long, max_lat;
        if (start.longitude < end.longitude) {
            min_long = start.longitude;
            max_long = end.longitude;
        } else {
            max_long = start.longitude;
            min_long = end.longitude;
        }
        if (start.latitude < end.latitude) {
            min_lat = start.latitude;
            max_lat = end.latitude;
        } else {
            max_lat = start.latitude;
            min_lat = end.latitude;
        }

        Point pointMin = LatLongConverter.convertLatLongTo1992InMeters(min_lat, min_long);
        Point pointMax = LatLongConverter.convertLatLongTo1992InMeters(max_lat, max_long);

        double height = pointMax.y - pointMin.y;
        double width = pointMax.x - pointMin.x;

        // imageViewTerrain.setImageDrawable(getResources().getDrawable(R.drawable.terrain_map_alpha));

        /*Bitmap bMap = Bitmap.createBitmap((int)Math.ceil(width), (int)Math.ceil(height), Bitmap.Config.ARGB_8888);

        int[] iImageArray = new int[bMap.getWidth()* bMap.getHeight()];


        //replace the red pixels with yellow ones
        int iWidth = bMap.getWidth();
        for (int i=0; i < bMap.getHeight(); i++)
        {
            for(int j=0; j<bMap.getWidth(); j++)
            {
                iImageArray[(i*iWidth)+j] = 0xFF00FFFF; // actual value of yellow


                if ( terrainBase.size() > i * (int)Math.ceil(width) + j) {
                    double currentZ = terrainBase.get(i * (int) Math.ceil(width) + j).getZ();
                    if (currentZ < 1000) {
                        iImageArray[(i*iWidth)+j] = 0xFF00FFFF;
                    } else if (currentZ < 200) {
                        iImageArray[(i*iWidth)+j] = 0xFF0099FF;
                    } else if (currentZ < 300) {
                        iImageArray[(i*iWidth)+j] = 0xFF0066FF;
                    } else if (currentZ < 400) {
                        iImageArray[(i*iWidth)+j] = 0xFF0033FF;
                    } else if (currentZ < 500) {
                        iImageArray[(i*iWidth)+j] = 0xFF00AAFF;
                    } else if (currentZ < 600) {
                        iImageArray[(i*iWidth)+j] = 0xFF00CCFF;
                    } else if (currentZ < 700) {
                        iImageArray[(i*iWidth)+j] = 0xFF00FFFF;
                    } else {
                        iImageArray[(i*iWidth)+j] = 0xFF00FFFF;
                    }
                }

            }
        }

        final Bitmap nbMap = Bitmap.createBitmap(iImageArray, bMap.getWidth(), bMap.getHeight(), Bitmap.Config.ARGB_8888);//Initialize the bitmap, with the replaced color

//        for (int i = 0; i < bMap.getWidth(); i++) {
//            for(int j = 0; j < bMap.getHeight(); j++) {
//                if ( terrainBase.size() > i * (int)Math.ceil(width) + j) {
//                    double currentZ = terrainBase.get(i * (int) Math.ceil(width) + j).getZ();
//                    if (currentZ < 100) {
//                        bMap.setPixel(i, j, Color.rgb(44, 55,  66));
//                    } else if (currentZ < 250) {
//                        bMap.setPixel(i, j, Color.rgb(66, 60,  66));
//                    } else if (currentZ < 280) {
//                        bMap.setPixel(i, j, Color.rgb(88, 65,  66));
//                    } else if (currentZ < 300) {
//                        bMap.setPixel(i, j, Color.rgb(110, 70,  66));
//                    } else if (currentZ < 320) {
//                        bMap.setPixel(i, j, Color.rgb(133, 75,  66));
//                    } else if (currentZ < 330) {
//                        bMap.setPixel(i, j, Color.rgb(155, 80,  66));
//                    } else if (currentZ < 350) {
//                        bMap.setPixel(i, j, Color.rgb(177, 85,  66));
//                    } else {
//                        bMap.setPixel(i, j, Color.rgb(200, 90,  66));
//                    }
//                }
//            }
//        }

        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    imageViewTerrain.setImageBitmap(nbMap);
                }
            });
        }
        */
    }
}
