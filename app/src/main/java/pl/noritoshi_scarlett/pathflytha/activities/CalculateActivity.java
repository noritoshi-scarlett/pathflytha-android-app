package pl.noritoshi_scarlett.pathflytha.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.geometry.Point;

import java.util.ArrayList;

import pl.noritoshi_scarlett.pathflytha.Pathflytha;
import pl.noritoshi_scarlett.pathflytha.database_utilities.DatabaseHelper;
import pl.noritoshi_scarlett.pathflytha.fragments_calculate.HeightChartFragment;
import pl.noritoshi_scarlett.pathflytha.fragments_calculate.MapsWithPathFragment;
import pl.noritoshi_scarlett.pathflytha.fragments_calculate.PageChartsFragmentAdapter;
import pl.noritoshi_scarlett.pathflytha.pojos.PojoObstacle;
import pl.noritoshi_scarlett.pathflytha.pojos.PojoTerrain;
import pl.noritoshi_scarlett.pathflytha.R;
import pl.noritoshi_scarlett.pathflytha.fragments_calculate.TerrainShapeFragment;
import pl.noritoshi_scarlett.pathflytha.algorithm_utilities.MainBranch;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class CalculateActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private TabLayout tabLayout;
    private LinearLayout layoutViewPager;
    private ViewPager viewPager;
    private PageChartsFragmentAdapter viewPagerAdapter;

    private LatLng startPoint;
    private LatLng startPointOut;
    private LatLng endPointTarget;
    private LatLng endPoint;
    private int pilotNormalFlyHeight;
    private int setup_ID;

    private ArrayList<PojoTerrain> terrainBase;
    private ArrayList<PojoObstacle> obstacleBase;

    @Override
    protected void attachBaseContext(Context newBase) {
        // FONT -> Ustawienie domyślnej czcionki w aplikacji
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private OnPathDataReceivedListener mPathDataListener;

    public interface OnPathDataReceivedListener {
        void onDataReceived(Point start, Point end);
    }

    public void setPathDataListener(OnPathDataReceivedListener listener) {
        this.mPathDataListener = listener;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculate);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        terrainBase = new ArrayList<>();
        obstacleBase = new ArrayList<>();

        startPoint = new LatLng(
                getIntent().getDoubleExtra(Pathflytha.START_MARKER_LATITUDE, 0),
                getIntent().getDoubleExtra(Pathflytha.START_MARKER_LONGITUDE, 0));
        startPointOut = new LatLng(
                getIntent().getDoubleExtra(Pathflytha.START_MARKER_OUT_LATITUDE, 0),
                getIntent().getDoubleExtra(Pathflytha.START_MARKER_OUT_LONGITUDE, 0));
        endPointTarget = new LatLng(
                getIntent().getDoubleExtra(Pathflytha.END_MARKER_TARGET_LATITUDE, 0),
                getIntent().getDoubleExtra(Pathflytha.END_MARKER_TARGET_LONGITUDE, 0));
        endPoint = new LatLng(
                getIntent().getDoubleExtra(Pathflytha.END_MARKER_LATITUDE, 0),
                getIntent().getDoubleExtra(Pathflytha.END_MARKER_LONGITUDE, 0));
        pilotNormalFlyHeight = Integer.valueOf(getIntent().getStringExtra(Pathflytha.FLY_NORMAL));

        setup_ID = getIntent().getIntExtra(Pathflytha.SETUP_ID, 0);

        // TABLAYOUT & VIEWPAGER
        // ustawienie Adaptera dla ViewPagera
        layoutViewPager = findViewById(R.id.layoutViewPager);
        viewPager = findViewById(R.id.viewPager);
        viewPager.setOffscreenPageLimit(3);
        viewPagerAdapter = new PageChartsFragmentAdapter(getSupportFragmentManager(), this);
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
            }
        });

        new GetMainBranchTask(this).execute();
    }

    public class GetMainBranchTask extends AsyncTask<Void, Void, MainBranch> {

        private CalculateActivity activity;

        GetMainBranchTask(CalculateActivity activity) {
            this.activity = activity;
        }

        @Override
        protected MainBranch doInBackground(Void... params) {

            MainBranch calcuateBranch = new MainBranch(
                    startPoint, startPointOut, endPointTarget, endPoint, pilotNormalFlyHeight, getApplicationContext());

            return calcuateBranch;//graph.getAllVertices();
        }

        @Override
        protected void onPostExecute(MainBranch branch) {
            super.onPostExecute(branch);


            Toast toast = Toast.makeText(activity, R.string.main_obstacles_is_loaded, Toast.LENGTH_SHORT);
            toast.show();

            if (branch != null) {
                // zaladowanie danych
                if (viewPagerAdapter.getCount() > 2) {
                    if ((viewPagerAdapter.getItem(0)) != null) {
                        TerrainShapeFragment fragment = ((TerrainShapeFragment) viewPagerAdapter.getItem(0));
                        fragment.drawPath(branch, setup_ID, pilotNormalFlyHeight);
                    }
                    if ((viewPagerAdapter.getItem(1)) != null) {
                        MapsWithPathFragment fragment = ((MapsWithPathFragment) viewPagerAdapter.getItem(1));
                        fragment.addPathToMaps(branch.getPath());
                    }
                    if ((viewPagerAdapter.getItem(2)) != null) {
                        HeightChartFragment fragment = ((HeightChartFragment) viewPagerAdapter.getItem(2));
                        fragment.drawPath(branch);
                    }
                }
            }

        }
    }
}
