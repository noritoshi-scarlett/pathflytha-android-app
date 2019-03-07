package pl.noritoshi_scarlett.pathflytha.activities;


import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.nio.file.Path;

import pl.noritoshi_scarlett.pathflytha.Pathflytha;
import pl.noritoshi_scarlett.pathflytha.fragments_maps.PageMapsFragment;
import pl.noritoshi_scarlett.pathflytha.fragments_maps.PageMapsFragmentAdapter;
import pl.noritoshi_scarlett.pathflytha.R;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MapsActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        PageMapsFragmentAdapter.InterfaceEditLongLatListener {

    private Toolbar mToolbar;
    private Menu menuInToolbar;
    int rotationAngleForShowHideIcon = 0;
    private BottomNavigationView bottomNavigationView;

    private MapView mMapView;
    private GoogleMap mMap;

    private TabLayout tabLayout;
    private LinearLayout layoutViewPager;
    private ViewPager viewPager;
    private PageMapsFragmentAdapter viewPagerAdapter;

    private Marker startMarker;
    private Marker startMarkerOut;
    private Marker endMarker;
    private Marker endMarkerTarget;
    private int flagNumberOfPoint = 0;

    @Override
    protected void attachBaseContext(Context newBase) {
        // FONT -> Ustawienie domyślnej czcionki w aplikacji
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // TABLAYOUT & VIEWPAGER
        // ustawienie Adaptera dla ViewPagera
        layoutViewPager = findViewById(R.id.layoutViewPager);
        viewPager = findViewById(R.id.viewPager);
        viewPagerAdapter = new PageMapsFragmentAdapter(getSupportFragmentManager(), this);
        // powiązanie Taba z ViewPagerem i ustalenie zakladek
        tabLayout = findViewById(R.id.tabsLayout);
        // ustalenie wskaznika na podstawie uzywanego tabu
        tabLayout.post(new Runnable() {
            @Override
            public void run() {
                viewPager.setAdapter(viewPagerAdapter);
                tabLayout.setupWithViewPager(viewPager);
                // ustalenie ikon i tekstu
                int length = tabLayout.getTabCount();
                for (int i = 0; i < length; i++) {
                    // customizacja layoutu
                    if (tabLayout.getTabAt(i) != null) {
                        tabLayout.getTabAt(i).setCustomView(viewPagerAdapter.getTabIconView(i, false));
                    }
                }
                // listener dla:
                // zmiany wskaznika na aktywnego taba
                // centrowania kamery na punkcie jesli taki istnieje
                tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        MenuItem item = bottomNavigationView.getMenu().findItem(R.id.maps_action_delete);
                        if        (tab.getPosition() == 0) {
                            flagNumberOfPoint = 0;
                            changeMarkersStatus(startMarker, item);
                        } else if (tab.getPosition() == 1) {
                            flagNumberOfPoint = 1;
                            changeMarkersStatus(startMarkerOut, item);
                        } else if (tab.getPosition() == 2) {
                            flagNumberOfPoint = 2;
                            changeMarkersStatus(endMarkerTarget, item);
                        } else if (tab.getPosition() == 3) {
                            flagNumberOfPoint = 3;
                            changeMarkersStatus(endMarker, item);
                        }
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {
                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {
                    }

                });
            }
        });

        // BOTTOM NAVIGATION
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.maps_action_apply:
                        if (       startMarker.isVisible()     && startMarkerOut.isVisible()
                                && endMarkerTarget.isVisible() && endMarker.isVisible())     {
                            Intent intent = new Intent();
                            intent.putExtra(Pathflytha.START_MARKER_LONGITUDE,      startMarker.getPosition().longitude);
                            intent.putExtra(Pathflytha.START_MARKER_LATITUDE,       startMarker.getPosition().latitude);
                            intent.putExtra(Pathflytha.START_MARKER_OUT_LONGITUDE,  startMarkerOut.getPosition().longitude);
                            intent.putExtra(Pathflytha.START_MARKER_OUT_LATITUDE,   startMarkerOut.getPosition().latitude);
                            intent.putExtra(Pathflytha.END_MARKER_TARGET_LONGITUDE, endMarkerTarget.getPosition().longitude);
                            intent.putExtra(Pathflytha.END_MARKER_TARGET_LATITUDE,  endMarkerTarget.getPosition().latitude);
                            intent.putExtra(Pathflytha.END_MARKER_LONGITUDE,        endMarker.getPosition().longitude);
                            intent.putExtra(Pathflytha.END_MARKER_LATITUDE,         endMarker.getPosition().latitude);
                            setResult(RESULT_OK, intent);
                            finish();
                        } else {
                            startMarker.setVisible(false);
                            startMarker.remove();
                            startMarkerOut.setVisible(false);
                            startMarkerOut.remove();
                            endMarkerTarget.setVisible(false);
                            endMarkerTarget.remove();
                            endMarker.setVisible(false);
                            endMarker.remove();
                            checkingPoints();
                        }
                        break;

                    case R.id.maps_action_delete:
                        // usuniecie danych z aktualnie aktywnego taba
                        ((PageMapsFragment) viewPagerAdapter.getItem(tabLayout.getSelectedTabPosition())).removeValues();
                        if (tabLayout.getSelectedTabPosition() == 0) {
                            if (startMarker != null) {
                                startMarker.setVisible(false);
                                startMarker.remove();
                            }
                        } else if (tabLayout.getSelectedTabPosition() == 1) {
                            if (startMarkerOut != null) {
                                startMarkerOut.setVisible(false);
                                startMarkerOut.remove();
                            }
                        } else if (tabLayout.getSelectedTabPosition() == 2) {
                            if (endMarkerTarget != null) {
                                endMarkerTarget.setVisible(false);
                                endMarkerTarget.remove();
                            }
                        } else {
                            if (endMarker != null) {
                                endMarker.setVisible(false);
                                endMarker.remove();
                            }
                        }
                        // aktualizacja przycisku usuwania i przycisku akceptacji
                        item.setEnabled(false);
                        bottomNavigationView.getMenu().findItem(R.id.maps_action_apply).setEnabled(false);
                        break;

                    case R.id.maps_action_info:
                        showInfoDialog();
                        break;
                }
                return true;
            }
        });

        // MAP
        mMapView = findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume(); // needed to get the map to display immediately
        mMapView.getMapAsync(this);


        // UKRYCIE STATUSBARA I VIEV
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (menuInToolbar.findItem(R.id.maps_action_showHideTab) != null) {
                    changeScreenSize(menuInToolbar.findItem(R.id.maps_action_showHideTab));
                }
            }
        }, 1000);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu_main; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_maps_bar, menu);
        menuInToolbar = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.maps_action_showHideTab:
                changeScreenSize(item);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
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

        // listener dla zbierania markerow
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                markedClickedPoint(latLng);
            }
        });
        // listener dla zmieniania aktywnego tabu
        mMap.setOnMarkerClickListener( this);

        LatLngBounds newarkBounds = new LatLngBounds(
                new LatLng(49.165953, 16.900756),       // South west corner
                new LatLng(51.310436, 21.887874));      // North east corner
        GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.terrain_map_alpha))
                .positionFromBounds(newarkBounds)
                .transparency(0.5f);
        mMap.addGroundOverlay(newarkMap);
    }

    /**
     * zmiana tabu w zaleznosci od kliknietego markera
     * @param marker musi byc jednym z dwoch (start/end), jesli inny to usuwany jest jakby go nie bylo
     * @return true jesli kliknieto wlasciwy marker
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        final int position;
        if (marker.equals(startMarker)) {
            position = 0;
        } else if (marker.equals(startMarkerOut)) {
            position = 1;
        } else if (marker.equals(endMarkerTarget)) {
            position = 2;
        } else if (marker.equals(endMarker)) {
            position = 3;
        } else {
            marker.setVisible(false);
            marker.remove();
            return false;
        }
        // jesli klikasz ten sam, to po prostu tylko wyswietl etykiete
        if (viewPager.getCurrentItem() == position) {
            marker.showInfoWindow();
        } else {
            changeTabSelected(position);
        }
        return false;
    }

    @Override
    public void markNewPoint(Double longitude, Double latitude) {
        markedClickedPoint(new LatLng(longitude, latitude));
    }

    /**
     * Zaznaczenie na mapie kliknietego punktu
     * @param point wspolrzedne geograficzne punktu
     */
    public void markedClickedPoint(LatLng point) {
        if        (flagNumberOfPoint == 0) {
            startMarker = markedNewMarkerAtPoint(point, startMarker, Pathflytha.POINT_START,
                    getResources().getString(R.string.maps_startCoordinatesTitle));
            startMarker.showInfoWindow();
        } else if (flagNumberOfPoint == 1) {
            startMarkerOut = markedNewMarkerAtPoint(point, startMarkerOut, Pathflytha.POINT_START_OUT,
                    getResources().getString(R.string.maps_start_out_CoordinatesTitle));
            startMarkerOut.showInfoWindow();
        } else if (flagNumberOfPoint == 2) {
            endMarkerTarget = markedNewMarkerAtPoint(point, endMarkerTarget, Pathflytha.POINT_END_TARGET,
                    getResources().getString(R.string.maps_end_target_CoordinatesTitle));
            endMarkerTarget.showInfoWindow();
        } else {
            endMarker = markedNewMarkerAtPoint(point, endMarker, Pathflytha.POINT_END,
                    getResources().getString(R.string.maps_endCoordinatesTitle));
            endMarker.showInfoWindow();
        }
        checkingPoints();
    }

    private Marker markedNewMarkerAtPoint(LatLng point, Marker marker, String pointType, String title) {
        if (marker != null) {
            marker.setVisible(false);
            marker.remove();
        }
        changeTextView(point.longitude, point.latitude, pointType);
        MenuItem item = bottomNavigationView.getMenu().findItem(R.id.maps_action_delete);
        if (item != null) {
            item.setEnabled(true);
        }
        return  mMap.addMarker(new MarkerOptions().position(point).title(title));
    }

    private void changeMarkersStatus(Marker currentMarker, MenuItem item) {
        if (currentMarker != null && currentMarker.isVisible()) {
            if ( ! currentMarker.equals(startMarker)     && startMarker != null     && startMarker.isVisible()) {
                startMarker.hideInfoWindow();
            }
            if ( ! currentMarker.equals(startMarkerOut)  && startMarkerOut != null  && startMarkerOut.isVisible()) {
                startMarkerOut.hideInfoWindow();
            }
            if ( ! currentMarker.equals(endMarkerTarget) && endMarkerTarget != null && endMarkerTarget.isVisible()) {
                endMarkerTarget.hideInfoWindow();
            }
            if ( ! currentMarker.equals(endMarker)       && endMarker != null       && endMarker.isVisible()) {
                endMarker.hideInfoWindow();
            }
            currentMarker.showInfoWindow();
            mMap.animateCamera(CameraUpdateFactory.newLatLng(currentMarker.getPosition()));
            if (item != null) {
                item.setEnabled(true);
            }
        } else {
            if (item != null) {
                item.setEnabled(false);
            }
        }
    }

    /**
     * Zmien wyswietlane dane o punkcie wybranym z mapy
     * @param longitude Długosc geograficzna
     * @param latitude Szerokosc geograficzna
     * @param point rodzaj punktu (star czy end)
     */
    private void changeTextView(final Double longitude, final Double latitude, final String point) {

        MapsActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                if (viewPagerAdapter.getCount() > 1) {
                    int number;
                    switch (point) {
                        case Pathflytha.POINT_START:
                            number = 0;
                            break;
                        case Pathflytha.POINT_START_OUT:
                            number = 1;
                            break;
                        case Pathflytha.POINT_END_TARGET:
                            number = 2;
                            break;
                        default:
                            number = 3;
                            break;
                    }
                    if ((viewPagerAdapter.getItem(number)) != null) {
                        ((PageMapsFragment) viewPagerAdapter.getItem(number)).changeValues(longitude, latitude);
                    }
                }
            }
        });
    }

    /**
     * Zmienia aktywny Tab na wskazany
     * Zmienia przycisk do usuwania
     * @param position nr tabu do wyswietlenia
     */
    private void changeTabSelected(final int position) {
        MapsActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                if (tabLayout != null && tabLayout.getTabAt(position) != null) {
                    tabLayout.getTabAt(position).select();
                    viewPager.setCurrentItem(position);
                    viewPager.refreshDrawableState();
                    tabLayout.refreshDrawableState();

                    MenuItem item = bottomNavigationView.getMenu().findItem(R.id.maps_action_delete);
                    if (item != null) {
                        if (position == 0) {
                            item.setEnabled(startMarker != null  && startMarker.isVisible());
                        } else if (position == 1) {
                            item.setEnabled(startMarkerOut != null  && startMarkerOut.isVisible());
                        } else if (position == 2) {
                            item.setEnabled(endMarkerTarget != null  && endMarkerTarget.isVisible());
                        } else if (position == 3) {
                            item.setEnabled(endMarker != null  && endMarker.isVisible());
                        }
                    }
                }
            }
        });
    }

    /**
     * Sprawdza, czy oba punkty mają uzupelnione wspolrzedne i jesli tak, to pozwala przejsc dalej
     */
    private void checkingPoints() {
        final boolean flag;
        flag = (startMarker != null && startMarker.isVisible() && endMarker != null && endMarker.isVisible()
            && startMarkerOut != null && startMarkerOut.isVisible() && endMarkerTarget != null && endMarkerTarget.isVisible());
        MapsActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                MenuItem item = bottomNavigationView.getMenu().findItem(R.id.maps_action_apply);
                if (item != null) {
                    item.setEnabled(flag);
                }
            }
        });
    }

    /**
     * zmiana wielkosci ekranu ze zwyklego na fullscreen lub odwrotnie
     * @param item klikniety przycisk
     */
    private void changeScreenSize(MenuItem item) {
        // animated icon
        View v = mToolbar.findViewById(item.getItemId());
        ObjectAnimator anim = ObjectAnimator.ofFloat(v, "rotation",
                rotationAngleForShowHideIcon, rotationAngleForShowHideIcon + 180);
        anim.setDuration(500).start();
        rotationAngleForShowHideIcon += 180;
        rotationAngleForShowHideIcon = rotationAngleForShowHideIcon % 360;
        // fullscreen and visible viewPager
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        if (layoutViewPager.getVisibility() == View.VISIBLE) {
            layoutViewPager.setVisibility(View.GONE);
            item.setTitle(R.string.maps_menuShowTab);
            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        } else {
            layoutViewPager.setVisibility(View.VISIBLE);
            item.setTitle(R.string.maps_menuHideTab);
            attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
        }
        getWindow().setAttributes(attrs);
    }

    /**
     * wyswietlenie komunikatu informacynego
     */
    private void showInfoDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);

        builder.setMessage(R.string.maps_infoDesc)
                .setTitle(R.string.maps_infoTitle);

        builder.setPositiveButton(R.string.maps_infoClose, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
