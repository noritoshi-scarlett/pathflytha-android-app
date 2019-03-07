package pl.noritoshi_scarlett.pathflytha;

import android.app.Application;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class Pathflytha extends Application {

    public static final String PATHFLYTHA_USER_SETTINGS = "PATHFLYTHA_User_Settings";
    static final String DEFAULT_FONT_ASSET_PATH = "fonts/Dosis-Light.ttf";

    public static final String POINT_START = "POINT_START";
    public static final String POINT_START_OUT = "POINT_START_OUT";
    public static final String POINT_END_TARGET = "POINT_END_TARGET";
    public static final String POINT_END = "POINT_END";
    public static final String START_MARKER_LONGITUDE = "START_MARKER_LONGITUDE";
    public static final String START_MARKER_LATITUDE = "START_MARKER_LATITUDE";
    public static final String START_MARKER_OUT_LONGITUDE = "START_MARKER_OUT_LONGITUDE";
    public static final String START_MARKER_OUT_LATITUDE = "START_MARKER_OUT_LATITUDE";
    public static final String END_MARKER_TARGET_LONGITUDE = "END_MARKER_TARGET_LONGITUDE";
    public static final String END_MARKER_TARGET_LATITUDE = "END_MARKER_TARGET_LATITUDE";
    public static final String END_MARKER_LONGITUDE = "END_MARKER_LONGITUDE";
    public static final String END_MARKER_LATITUDE = "END_MARKER_LATITUDE";

    public static final String FLY_NORMAL = "FLY_NORMAL";
    public static final String FLY_MINIMUM = "FLY_MINIMUM";

    public static final String SETUP_ID = "SETUP_ID";
    public static final int REQUEST_CODE_PICK_COORDINATIES = 2404;

    public static final String ARG_CHART_TERRAIN = "ARG_CHART_TERRAIN";
    public static final String ARG_CHART_OBSTACLES = "ARG_CHART_OBSTACLES";
    public static final String ARG_CHART_HEIGHT = "ARG_CHART_HEIGHT";
    public final static String ARG_PAGE_NUMBER = "ARG_PAGE_NUMBER";


    private static Pathflytha instance;

    @Override
    public void onCreate() {
        super.onCreate();

        // FONT -> Ustawienie domyślnej czcionki w aplikacji
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath(DEFAULT_FONT_ASSET_PATH)
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        instance = this;
    }

    public static Pathflytha getInstance() {
        if (instance == null) {instance = new Pathflytha();}
        return instance; }
}
