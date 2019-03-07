package pl.noritoshi_scarlett.pathflytha.fragments_calculate;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;

import java.util.ArrayList;
import java.util.List;

import pl.noritoshi_scarlett.pathflytha.Pathflytha;
import pl.noritoshi_scarlett.pathflytha.pojos.PojoObstacle;
import pl.noritoshi_scarlett.pathflytha.pojos.PojoTerrain;
import pl.noritoshi_scarlett.pathflytha.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class PageChartFragment extends Fragment implements PageChartsFragmentAdapter.InterfaceChartListener {

    private ScatterChart chart;

    private int currentPage;
    private View.OnKeyListener mOnKeyListener;

    public PageChartFragment() {}

    public static PageChartFragment init(int page) {
        Bundle args = new Bundle();
        args.putInt(Pathflytha.ARG_PAGE_NUMBER, page);
        PageChartFragment fragment = new PageChartFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentPage = getArguments().getInt(Pathflytha.ARG_PAGE_NUMBER);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_page_chart, container, false);

        chart = view.findViewById(R.id.chart);

        return view;
    }

    /**
     * Zaladowanie nowych danych
     * @param data dane do wykresu
     */
    @Override
    public void changeValues(ArrayList<?> data, String type) {

        List<Entry> entries = new ArrayList<>();

        if (data != null && data.size() > 0) {
            switch (type) {
                case Pathflytha.ARG_CHART_TERRAIN: {
                    PojoTerrain item;
                    for (Object object : data) {
                        item = (PojoTerrain) object;
                        entries.add(new Entry(item.getX(), item.getY()));
                        Entry terraitn = new Entry(item.getX(), item.getY());
                    }
                    break;
                }
                case Pathflytha.ARG_CHART_OBSTACLES: {
                    PojoObstacle item;
                    for (Object object : data) {
                        item = (PojoObstacle) object;
                        entries.add(new Entry(item.getItem_obs_x(), item.getItem_obs_y()));
                    }
                    break;
                }
                case Pathflytha.ARG_CHART_HEIGHT: {
                    PojoTerrain item;
                    for (Object object : data) {
                        item = (PojoTerrain) object;
                        entries.add(new Entry(item.getX(), item.getY()));
                    }
                    break;
                }
            }
            ScatterDataSet dataSet = new ScatterDataSet(entries, type); // add entries to dataset
            dataSet.setColor(R.color.colorStatusBar);
            dataSet.setValueTextColor(R.color.textPrimary);
            dataSet.setDrawHighlightIndicators(false);
            ScatterData scatterData = new ScatterData(dataSet);
            chart.setData(scatterData);

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    chart.invalidate(); // refresh
                }
            });
        }
    }

    /**
     * usuniecie wartosci z wykresu
     */
    @Override
    public void removeValues() {

        // TODO -> remove chart
    }

}
