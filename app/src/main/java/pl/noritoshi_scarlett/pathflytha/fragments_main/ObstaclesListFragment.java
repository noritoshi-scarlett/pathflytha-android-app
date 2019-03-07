package pl.noritoshi_scarlett.pathflytha.fragments_main;


import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;

import java.io.IOException;
import java.util.ArrayList;

import pl.noritoshi_scarlett.pathflytha.database_utilities.DatabaseHelper;
import pl.noritoshi_scarlett.pathflytha.maps_utilities.MapClusterItem;
import pl.noritoshi_scarlett.pathflytha.pojos.PojoObstacle;
import pl.noritoshi_scarlett.pathflytha.R;


public class ObstaclesListFragment extends Fragment implements
        OnMapReadyCallback, ObstaclesListRecyclerViewAdapter.OnItemClickListener {

    private Context context;
    private DatabaseHelper mDBHelper;
    private SQLiteDatabase mDb;

    private RecyclerView recyclerView;
    private ObstaclesListRecyclerViewAdapter adapter;
    private ArrayList<PojoObstacle> obstacleList;
    private PojoObstacle currentChecked;
    private SparseArray<Marker> markersList;
    private MapView mMapView;
    private GoogleMap mMap;

    // Declare a variable for the cluster manager.
    private ClusterManager<MapClusterItem> mClusterManager;

    public ObstaclesListFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_obstacles_list, container, false);

        context = getActivity().getApplicationContext();
        markersList = new SparseArray<>();

        // FIX SCROLL BEHAVIOR ON MAP
        AppBarLayout mAppBarLayout = view.findViewById(R.id.obsListMapBar);
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mAppBarLayout.getLayoutParams();
        AppBarLayout.Behavior behavior = new AppBarLayout.Behavior();
        behavior.setDragCallback(new AppBarLayout.Behavior.DragCallback() {
            @Override
            public boolean canDrag(@NonNull AppBarLayout appBarLayout) {
                return false;
            }
        });
        params.setBehavior(behavior);
        final FloatingActionButton fabBtnListObs = view.findViewById(R.id.fabBtnListObs);
        // RECYCLER
        recyclerView = view.findViewById(R.id.recyclerView);
        // MAP
        mMapView = view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mMapView.getMapAsync(this);

        // MAX WYSKOSC MAPY
//        mMapView.post(new Runnable() {
//            @Override
//            public void run() {
//                if (fabBtnListObs.getHeight() != 0) {
//                    int width = view.getWidth();
//                    int height = view.getHeight() - fabBtnListObs.getHeight() / 2;
//                    CollapsingToolbarLayout.LayoutParams mapParams = new CollapsingToolbarLayout.LayoutParams(width, height);
//                    mMapView.setLayoutParams(mapParams);
//                }
//            }
//        });

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // wysrodkowanie na Polske wraz ze zblizeniem
        float zoomLevel = 5.0f; //This goes up to 21
        LatLng polandCenter = new LatLng(51.8751298, 19.6466807);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(polandCenter, zoomLevel));

        // pobranie danych
        new GetObstacleDBTask(this).execute();
    }

    @Override
    public void setOnItemClickListener(View view, int position) {
        // dodawanie do mapy markerow i usuwanie przez zaznaczanie
        if (markersList.get(position) == null) {
            currentChecked = obstacleList.get(position);
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(
                            currentChecked.getItem_obs_latitude(),
                            currentChecked.getItem_obs_longitude()))
                    .title(currentChecked.getItem_obs_name()));
            mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
            marker.showInfoWindow();
            markersList.put(position, marker);
        } else {
            markersList.get(position).setVisible(false);
            markersList.get(position).remove();
            markersList.remove(position);
        }
    }

    public class GetObstacleDBTask extends AsyncTask<Void, Void, ArrayList<PojoObstacle>> {

        private ObstaclesListFragment mFragment;

        GetObstacleDBTask(ObstaclesListFragment mFragment) {
            this.mFragment = mFragment;
        }

        @Override
        protected ArrayList<PojoObstacle> doInBackground(Void... params) {

            mDBHelper = new DatabaseHelper(context);
            // przygotowanie bazy danych
            try {
                mDBHelper.updateDataBase();
            } catch (IOException mIOException) {
                throw new Error("UnableToUpdateDatabase");
            }

            // pobranie danych
            try {
                mDb = mDBHelper.getWritableDatabase();
            } catch (SQLException mSQLException) {
                throw mSQLException;
            }
            obstacleList = mDBHelper.getAllObstacles(mDb);
            return obstacleList;
        }

        @Override
        protected void onPostExecute(ArrayList<PojoObstacle> obstacle) {
            super.onPostExecute(obstacle);

            if (recyclerView != null) {
                adapter = new ObstaclesListRecyclerViewAdapter(context, obstacleList);
                recyclerView.setLayoutManager(new GridLayoutManager(context, 2));
                recyclerView.setAdapter(adapter);
                adapter.setOnItemClickListener(mFragment); // Bind the listener
            }
            if (mDBHelper != null) {
                mDBHelper.close();
            }
            Toast toast = Toast.makeText(context, R.string.main_obstacles_is_loaded, Toast.LENGTH_SHORT);
            toast.show();

            setUpClusterer();
            mClusterManager.setAnimation(true);

            if (mMap != null) {
                PojoObstacle obj;
                for (int i = 0; i < obstacleList.size(); i++ ) {
                    obj = obstacleList.get(i);
                    // Create a cluster item for the marker and set the title and snippet using the constructor.
                    MapClusterItem infoWindowItem = new MapClusterItem(
                            obj.getItem_obs_latitude(),
                            obj.getItem_obs_longitude(),
                            obj.getItem_obs_name(),
                            "");
                    // Add the cluster item (marker) to the cluster manager.
                    mClusterManager.addItem(infoWindowItem);
                }
            }
        }
    }

    private void setUpClusterer() {
        // Position the map.
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(50.091282, 19.546003), 6));

        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        mClusterManager = new ClusterManager<MapClusterItem>(context, mMap);

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);
    }

}
