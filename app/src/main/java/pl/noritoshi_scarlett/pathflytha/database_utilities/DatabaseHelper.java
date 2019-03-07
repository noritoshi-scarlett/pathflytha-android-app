package pl.noritoshi_scarlett.pathflytha.database_utilities;


import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Pair;

import com.google.maps.android.geometry.Point;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import pl.noritoshi_scarlett.pathflytha.algorithm_utilities.geo_objects.GraphPoint;
import pl.noritoshi_scarlett.pathflytha.pojos.PojoObstacle;
import pl.noritoshi_scarlett.pathflytha.pojos.PojoObstacleNames;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_PATH = "/data/data/";
    private static String DB_NAME = "db_pathflytha.db";
    private static String DB_PATH = "";
    private static final int DB_VERSION = 1;

    private SQLiteDatabase mDataBase;
    private final Context mContext;
    private boolean mNeedUpdate = false;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        DB_PATH = DATABASE_PATH + context.getPackageName() + "/databases/";
        this.mContext = context;

        copyDataBase();

        this.getReadableDatabase();
    }

    public void updateDataBase() throws IOException {
        if (mNeedUpdate) {
            File dbFile = new File(DB_PATH + DB_NAME);
            if (dbFile.exists())
                dbFile.delete();

            copyDataBase();

            mNeedUpdate = false;
        }
    }

    private boolean checkDataBase() {
        File dbFile = new File(DB_PATH + DB_NAME);
        return dbFile.exists();
    }

    private void copyDataBase() {
        if (!checkDataBase()) {
            this.getReadableDatabase();
            this.close();
            try {
                copyDBFile();
            } catch (IOException mIOException) {
                throw new Error("ErrorCopyingDataBase");
            }
        }
    }

    private void copyDBFile() throws IOException {
        InputStream mInput = mContext.getAssets().open(DB_NAME);
        //InputStream mInput = mContext.getResources().openRawResource(R.raw.info);
        OutputStream mOutput = new FileOutputStream(DB_PATH + DB_NAME);
        byte[] mBuffer = new byte[1024];
        int mLength;
        while ((mLength = mInput.read(mBuffer)) > 0)
            mOutput.write(mBuffer, 0, mLength);
        mOutput.flush();
        mOutput.close();
        mInput.close();
    }

    public boolean openDataBase() throws SQLException {
        mDataBase = SQLiteDatabase.openDatabase(DB_PATH + DB_NAME, null, SQLiteDatabase.CREATE_IF_NECESSARY);
        return mDataBase != null;
    }

    @Override
    public synchronized void close() {
        if (mDataBase != null)
            mDataBase.close();
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion)
            mNeedUpdate = true;
    }


    private static final String TABLE_OBSTACLE_NAMES = "PHFLTA_obstacle_names";
    private static final String TABLE_OBSTACLES_LIST = "PHFLTA_obstacles_list";
    private static final String TABLE_TERRAIN_SILESIA = "PHFLTA_terrain_silesia";

    private static final String COLUMN_OBSTACLES_LIST_ID = "obslist_id";
    private static final String COLUMN_OBSTACLES_LIST_LATITUDE = "obslist_latitude";
    private static final String COLUMN_OBSTACLES_LIST_LONGITUDE = "obslist_longitude";
    private static final String COLUMN_OBSTACLES_LIST_X = "obslist_x";
    private static final String COLUMN_OBSTACLES_LIST_Y = "obslist_y";
    private static final String COLUMN_OBSTACLES_LIST_HEIGHT = "obslist_height";
    private static final String COLUMN_OBSTACLES_LIST_ELEVATION = "obslist_elevation";
    private static final String COLUMN_OBSTACLES_LIST_LIGHTED = "obslist_lighted";
    private static final String COLUMN_OBSTACLES_LIST_NAME_ID = "obslist_name_id";
    private static final String COLUMN_OBSTACLES_LIST_RANGE = "obslist_range";

    private static final String QUERY_COLUMNS_OBSTACLES_LIST[] = {
            COLUMN_OBSTACLES_LIST_ID,
            COLUMN_OBSTACLES_LIST_LATITUDE, COLUMN_OBSTACLES_LIST_LONGITUDE,
            COLUMN_OBSTACLES_LIST_HEIGHT, COLUMN_OBSTACLES_LIST_ELEVATION, COLUMN_OBSTACLES_LIST_LIGHTED,
            COLUMN_OBSTACLES_LIST_NAME_ID, COLUMN_OBSTACLES_LIST_RANGE };

    private static final String COLUMN_OBSTACLE_NAMES_ID = "obsnames_id";
    private static final String COLUMN_OBSTACLE_NAMES_NAME = "obsnames_name";
    private static final String QUERY_COLUMNS_OBSTACLE_NAMES[] = {
            COLUMN_OBSTACLE_NAMES_ID,
            COLUMN_OBSTACLE_NAMES_NAME };

    private static final String COLUMN_TERRAIN_X = "terrain_x";
    private static final String COLUMN_TERRAIN_Y = "terrain_y";
    private static final String COLUMN_TERRAIN_Z = "terrain_z";
    private static final String QUERY_COLUMNS_TERRAIN_SILESIA[] = {
            COLUMN_TERRAIN_X,
            COLUMN_TERRAIN_Y,
            COLUMN_TERRAIN_Z };
    private static final String QUERY_SMALL_COLUMNS_TERRAIN_SILESIA[] = {
            COLUMN_TERRAIN_Y,
            COLUMN_TERRAIN_Z };


    static public ArrayList<PojoObstacleNames> getAllObstacleNames(SQLiteDatabase db) {
        ArrayList<PojoObstacleNames> obstacleNames = new ArrayList<>();
        Cursor cursor = null;

        try {
            cursor = db.query(TABLE_OBSTACLE_NAMES, QUERY_COLUMNS_OBSTACLE_NAMES, null, null, null, null, null);
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    PojoObstacleNames pojo = new PojoObstacleNames();
                    pojo.setItem_obs_id(cursor.getInt(cursor.getColumnIndex(COLUMN_OBSTACLE_NAMES_ID)));
                    pojo.setItem_obs_name(cursor.getString(cursor.getColumnIndex(COLUMN_OBSTACLE_NAMES_NAME)));
                    obstacleNames.add(pojo);
                }
            }
        } catch (Exception e) {
            Log.d("baza danych", "Zgloszono wyjatek: " + e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return obstacleNames;
    }

    static public int[][] getTerrainForPath(SQLiteDatabase db, double current_x, Point start, Point end) {

        int[][] terrainArea;
        Cursor cursor = null;

        double min_y, max_y;

        min_y = Math.floor(Math.min(start.y, end.y));
        max_y = Math.ceil(Math.max(start.y, end.y));

        String selector =
                "(" +
                        COLUMN_TERRAIN_X + " = " + current_x +
                        ") AND ( " +
                        COLUMN_TERRAIN_Y + " BETWEEN " + min_y + " AND " + max_y +
                ")";

        long numberOfRows = DatabaseUtils.queryNumEntries(db, TABLE_TERRAIN_SILESIA, selector, null);
        terrainArea = new int[ (int) ( Math.abs(max_y - min_y) / 100 + 2) ][2];
        int i = 0;
        String limit_string;

        try {
            int limit = 0;
            while (limit + 500 < numberOfRows) {
                limit_string = limit + "," + limit + "" + 500;
                cursor = db.query(TABLE_TERRAIN_SILESIA, QUERY_SMALL_COLUMNS_TERRAIN_SILESIA,
                        selector, null, null, null, COLUMN_TERRAIN_X, limit_string);
                if (cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        terrainArea[i]  [0] = cursor.getInt(cursor.getColumnIndex(COLUMN_TERRAIN_Y));
                        terrainArea[i++][1] = cursor.getInt(cursor.getColumnIndex(COLUMN_TERRAIN_Z));
                    }
                }
                cursor.close();
                limit += 500;
            }

            limit_string = limit + "," + numberOfRows;
            cursor = db.query(TABLE_TERRAIN_SILESIA, QUERY_SMALL_COLUMNS_TERRAIN_SILESIA,
                    selector, null, null, null, COLUMN_TERRAIN_X, limit_string);
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    terrainArea[i]  [0] = cursor.getInt(cursor.getColumnIndex(COLUMN_TERRAIN_Y));
                    terrainArea[i++][1] = cursor.getInt(cursor.getColumnIndex(COLUMN_TERRAIN_Z));
                }
            }
            cursor.close();


        } catch (Exception e) {
            Log.d("baza danych", "Zgloszono wyjatek: " + e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return terrainArea;
    }

    static public List<GraphPoint> getTerrainForPoints(SQLiteDatabase db, List<GraphPoint> points) {

        Cursor cursor = null;
        List<GraphPoint> pointsFromDB = new ArrayList<>();

        Point[] pointsXY;
        double minX, minY;
        StringBuilder selector = new StringBuilder();
        for (int i = 0; i < points.size(); i++) {
            minX = Math.floor(points.get(i).x / 100) * 100;
            minY = Math.floor(points.get(i).y / 100) * 100;
            pointsXY = new Point[] { new Point(minX, minY)         , new Point(minX + 100, minY),
                                     new Point(minX, minY + 100), new Point(minX + 100, minY + 100) };
            for(Point point2 : pointsXY) {
                if (! selector.toString().equals("") ) {
                    selector.append(" OR ");
                }
                selector.append("(" + COLUMN_TERRAIN_X + " = ").append(point2.x).append(" AND ")
                        .append(      COLUMN_TERRAIN_Y + " = ").append(point2.y).append(")");
            }
        }

        try {
           GraphPoint point;
            cursor = db.query(TABLE_TERRAIN_SILESIA, QUERY_COLUMNS_TERRAIN_SILESIA,
                    selector.toString(), null, null, null, COLUMN_TERRAIN_X, null);
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    point = new GraphPoint(
                            cursor.getInt(cursor.getColumnIndex(COLUMN_TERRAIN_X)),
                            cursor.getInt(cursor.getColumnIndex(COLUMN_TERRAIN_Y)),
                            cursor.getInt(cursor.getColumnIndex(COLUMN_TERRAIN_Z)));
                    pointsFromDB.add(point);
                }
            }
            cursor.close();
        } catch (Exception e) {
            Log.d("baza danych", "Zgloszono wyjatek: " + e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return pointsFromDB;
    }

    static public ArrayList<PojoObstacle> getAllObstacles(SQLiteDatabase db) {
        ArrayList<PojoObstacle> obstaclesList = new ArrayList<>();
        Cursor cursor = null;

        try {

            String where_condition = COLUMN_OBSTACLES_LIST_NAME_ID + " = " +  COLUMN_OBSTACLE_NAMES_ID;
            cursor = db.rawQuery(
                    "SELECT * FROM " + TABLE_OBSTACLES_LIST + ", " + TABLE_OBSTACLE_NAMES +
                    " WHERE " + where_condition, null);

            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    PojoObstacle pojo = new PojoObstacle();
                    pojo.setItem_obs_id(cursor.getInt(cursor.getColumnIndex(COLUMN_OBSTACLES_LIST_ID)));
                    pojo.setItem_obs_latitude(cursor.getFloat(cursor.getColumnIndex(COLUMN_OBSTACLES_LIST_LATITUDE)));
                    pojo.setItem_obs_longitude(cursor.getFloat(cursor.getColumnIndex(COLUMN_OBSTACLES_LIST_LONGITUDE)));
                    pojo.setItem_obs_height(cursor.getInt(cursor.getColumnIndex(COLUMN_OBSTACLES_LIST_HEIGHT)));
                    pojo.setItem_obs_elevation(cursor.getInt(cursor.getColumnIndex(COLUMN_OBSTACLES_LIST_ELEVATION)));
                    pojo.setItem_obs_name_id(cursor.getInt(cursor.getColumnIndex(COLUMN_OBSTACLES_LIST_NAME_ID)));
                    pojo.setItem_obs_name(cursor.getString(cursor.getColumnIndex(COLUMN_OBSTACLE_NAMES_NAME)));
                    obstaclesList.add(pojo);
                }
            }
        } catch (Exception e) {
            Log.d("baza danych", "Zgloszono wyjatek: " + e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return obstaclesList;
    }

    static public ArrayList<PojoObstacle> getAreaObstacles(Context context, SQLiteDatabase db, GraphPoint start, GraphPoint end) {
        ArrayList<PojoObstacle> obstaclesList = new ArrayList<>();
        Cursor cursor = null;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        double MIN_DISTANCE_FROM_OBJECT = Double.valueOf(prefs.getString("preference_minDistanceFromObstacle", "50"));
        MIN_DISTANCE_FROM_OBJECT *= 2;

        double min_x, min_y, max_x, max_y;
        if (start.y < end.y) {
            min_y = start.y - MIN_DISTANCE_FROM_OBJECT;
            max_y = end.y + MIN_DISTANCE_FROM_OBJECT;
        } else {
            max_y = start.y + MIN_DISTANCE_FROM_OBJECT;
            min_y = end.y - MIN_DISTANCE_FROM_OBJECT;
        }
        if (start.x < end.x) {
            min_x = start.x - MIN_DISTANCE_FROM_OBJECT;
            max_x = end.x + MIN_DISTANCE_FROM_OBJECT;
        } else {
            max_x = start.x + MIN_DISTANCE_FROM_OBJECT;
            min_x = end.x - MIN_DISTANCE_FROM_OBJECT;
        }

        try {
            String selector =
                    "(" +
                            COLUMN_OBSTACLES_LIST_X + " BETWEEN " + min_x + " AND " + max_x +
                            ") AND ( " +
                            COLUMN_OBSTACLES_LIST_Y + " BETWEEN " + min_y + " AND " + max_y +
                            ")";
            String where_condition = COLUMN_OBSTACLES_LIST_NAME_ID + " = " +  COLUMN_OBSTACLE_NAMES_ID;
            cursor = db.rawQuery(
                    "SELECT * FROM " + TABLE_OBSTACLES_LIST + ", " + TABLE_OBSTACLE_NAMES +
                            " WHERE " + where_condition + " AND " + selector + " ORDER BY " + COLUMN_OBSTACLES_LIST_X,
                    null);

            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    PojoObstacle pojo = new PojoObstacle();
                    pojo.setItem_obs_id(cursor.getInt(cursor.getColumnIndex(COLUMN_OBSTACLES_LIST_ID)));
                    pojo.setItem_obs_latitude(cursor.getFloat(cursor.getColumnIndex(COLUMN_OBSTACLES_LIST_LATITUDE)));
                    pojo.setItem_obs_longitude(cursor.getFloat(cursor.getColumnIndex(COLUMN_OBSTACLES_LIST_LONGITUDE)));
                    pojo.setItem_obs_x(cursor.getInt(cursor.getColumnIndex(COLUMN_OBSTACLES_LIST_X)));
                    pojo.setItem_obs_y(cursor.getInt(cursor.getColumnIndex(COLUMN_OBSTACLES_LIST_Y)));
                    pojo.setItem_obs_height(cursor.getInt(cursor.getColumnIndex(COLUMN_OBSTACLES_LIST_HEIGHT)));
                    pojo.setItem_obs_elevation(cursor.getInt(cursor.getColumnIndex(COLUMN_OBSTACLES_LIST_ELEVATION)));
                    pojo.setItem_obs_name_id(cursor.getInt(cursor.getColumnIndex(COLUMN_OBSTACLES_LIST_NAME_ID)));
                    pojo.setItem_obs_name(cursor.getString(cursor.getColumnIndex(COLUMN_OBSTACLE_NAMES_NAME)));
                    pojo.setItem_obs_range(cursor.getFloat(cursor.getColumnIndex(COLUMN_OBSTACLES_LIST_RANGE)));
                    obstaclesList.add(pojo);
                }
            }
        } catch (Exception e) {
            Log.d("baza danych", "Zgloszono wyjatek: " + e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return obstaclesList;
    }
}
