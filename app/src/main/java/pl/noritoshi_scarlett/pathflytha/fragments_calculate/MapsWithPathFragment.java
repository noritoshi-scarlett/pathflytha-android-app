package pl.noritoshi_scarlett.pathflytha.fragments_calculate;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.geometry.Point;

import java.util.ArrayList;
import java.util.List;

import pl.noritoshi_scarlett.pathflytha.Pathflytha;
import pl.noritoshi_scarlett.pathflytha.R;
import pl.noritoshi_scarlett.pathflytha.algorithm_utilities.geo_objects.GraphPoint;
import pl.noritoshi_scarlett.pathflytha.algorithm_utilities.small_quests.LatLongConverter;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapsWithPathFragment extends Fragment implements OnMapReadyCallback {

    private int currentPage;

    private MapView mapView;
    private GoogleMap mMap;


    public MapsWithPathFragment() {
        // Required empty public constructor
    }

    public static MapsWithPathFragment init(int page) {
        Bundle args = new Bundle();
        args.putInt(Pathflytha.ARG_PAGE_NUMBER, page);
        MapsWithPathFragment fragment = new MapsWithPathFragment();
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
        View view =   inflater.inflate(R.layout.fragment_maps_with_path, container, false);

        // MAP
        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.onResume(); // needed to get the map to display immediately
        mapView.getMapAsync(this);


        return view;
    }

    public void addPathToMaps(List<GraphPoint> points) {

        if (mMap != null) {
            if (points != null) {

                ArrayList<LatLng> latlangs = new ArrayList<>();

                for (GraphPoint point : points) {
                    mMap.addMarker(
                            new MarkerOptions()
                                    .position(LatLongConverter.convert1992InMetersToLatLong(new Point(point.x, point.y)))
                                    .title("x: " + point.x + ", y: " + point.y));
                    latlangs.add(LatLongConverter.convert1992InMetersToLatLong(new Point(point.x, point.y)));

                    Polyline line = mMap.addPolyline(new PolylineOptions()
                            .addAll(latlangs)
                            .width(5)
                            .color(Color.RED));
                    line.setVisible(true);

                }
            }
        }


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // warstwa terenu na mapie
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.getUiSettings().setTiltGesturesEnabled(false);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        // wysrodkowanie na Polske wraz ze zblizeniem
        float zoomLevel = 5.0f; //This goes up to 21
        LatLng polandCenter = new LatLng(51.8751298, 19.6466807);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(polandCenter, zoomLevel));

        LatLngBounds newarkBounds = new LatLngBounds(
                new LatLng(49.165953, 16.900756),       // South west corner
                new LatLng(51.310436, 21.887874));      // North east corner
        GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.terrain_map_alpha))
                .positionFromBounds(newarkBounds)
                .transparency(0.5f);
        mMap.addGroundOverlay(newarkMap);
    }
}
