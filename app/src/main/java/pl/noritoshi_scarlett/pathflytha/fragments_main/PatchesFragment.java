package pl.noritoshi_scarlett.pathflytha.fragments_main;


import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import pl.noritoshi_scarlett.pathflytha.pojos.PojoPatches;
import pl.noritoshi_scarlett.pathflytha.R;


public class PatchesFragment extends Fragment implements
        SwipeRefreshLayout.OnRefreshListener {


    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeLayout;
    private FloatingActionButton floatBtnApply;
    private FloatingActionButton floatBtnDelete;

    private Context context;
    private ArrayList<PojoPatches> patchesList;

    public PatchesFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_patches, container, false);
        context = getActivity().getApplicationContext();

        // USUWANIE SCIEZKI
        floatBtnDelete =  view.findViewById(R.id.floatBtnDelete);
        floatBtnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO -> show dialog
            }
        });
        // POKAZANIE TRASY
        floatBtnApply =  view.findViewById(R.id.floatBtnApply);
        floatBtnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO -> show dialog
            }
        });
        // ODŚWIEŻANIE LISTY
        swipeLayout =  view.findViewById(R.id.swipeRefreshLayout);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(context, R.color.colorAccent));
        swipeLayout.setColorSchemeColors(ContextCompat.getColor(context, android.R.color.holo_blue_bright),
                ContextCompat.getColor(context, android.R.color.holo_green_light),
                ContextCompat.getColor(context, android.R.color.holo_orange_light),
                ContextCompat.getColor(context, android.R.color.holo_red_light));
        // lISTA TRAS
        recyclerView = view.findViewById(R.id.recyclerView);

        getPatchesData();

        return view;
    }

    private void getPatchesData() {
        // TODO -> get data from db
        //patchesList = ?????????????????
        if (patchesList != null) {
            updateRecyclerView(context, patchesList);
        } else {
            if (swipeLayout.isRefreshing()) {
                swipeLayout.setRefreshing(false);
            }
            // TODO -> brak danych, pokaz komunikat
        }
    }

    @Override
    public void onRefresh() {
        getPatchesData();
    }

    private void updateRecyclerView(Context context, ArrayList<PojoPatches> patchesList) {

        PatchesRecyclerViewAdapter adapter = new PatchesRecyclerViewAdapter(context, this.patchesList);
        RecyclerView.LayoutManager mGridManager = new GridLayoutManager(context, 2);
        recyclerView.setLayoutManager(mGridManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                GridLayoutManager gridManager = ((GridLayoutManager) recyclerView.getLayoutManager());
                swipeLayout.setEnabled(gridManager.findFirstCompletelyVisibleItemPosition() == 0); // 0 is for first item position
            }
        });
        recyclerView.setAdapter(adapter);

        if (swipeLayout.isRefreshing()) {
            // TODO - załadowano dane - toast
            swipeLayout.setRefreshing(false);
        }
    }


}
