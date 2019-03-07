package pl.noritoshi_scarlett.pathflytha.algorithm_utilities.small_quests;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

import pl.noritoshi_scarlett.pathflytha.algorithm_utilities.geo_objects.GraphPoint;
import pl.noritoshi_scarlett.pathflytha.algorithm_utilities.geo_objects.LineDirectory;
import pl.noritoshi_scarlett.pathflytha.database_utilities.DatabaseHelper;

public class CheckTerrain {

    static public double MIN_DISTANCE_UNDER_FLY_HEIGHT = 100;

    private double pilotNormalFlyHeight;
    private double flyHeightMinimum;
    private double flyHeightMaximum = 500;
    private boolean canFlyNormal;
    private boolean canFly;
    private Context context;

    public CheckTerrain(Context context, int pilotNormalFlyHeight) {
        this.context = context;
        this.pilotNormalFlyHeight = pilotNormalFlyHeight;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.context);
        flyHeightMinimum =  Integer.valueOf(preferences.getString("preference_minDistanceFromTerrain", "100"));

        //TODO - load other settings from preferences

    }

    public boolean isCanFlyNormal() {
        return canFlyNormal;
    }

    public boolean isCanFlyUpper() {
        return canFly;
    }


    public boolean checkLine(Pair<GraphPoint, GraphPoint> pairOfPoints, SQLiteDatabase db) {
        return checkLine(pairOfPoints.first, pairOfPoints.second, new LineDirectory(pairOfPoints.first, pairOfPoints.second), db);
    }

    public boolean checkLine(GraphPoint pointA, GraphPoint pointB, LineDirectory line, SQLiteDatabase db) {
        canFlyNormal = true;
        canFly = true;
        double xx = Math.abs(pointA.x - pointB.x);
        double yy = Math.abs(pointA.y - pointB.y);

        int minX = ((int) (Math.floor(Math.min(pointA.x, pointB.x) / 100) - 1)) * 100 + 50;

        int terrainSize = (int) Math.ceil((xx / 100) / 5) * 100;

        List<GraphPoint> terrain = new ArrayList<>();
        List<GraphPoint> terrainFromDB = new ArrayList<>();

        db.beginTransaction();

        for (int i = minX, j = 0; j < 6; i+=terrainSize, j++) {
            terrain.add(new GraphPoint(i, line.valueOf(i), 0));
        }
        for (int i = 0; i < terrain.size(); i+=12 ) {
            if (i+12 < terrain.size()) {
                terrainFromDB.addAll(DatabaseHelper.getTerrainForPoints(db, terrain.subList(i, i + 12)));
            } else {
                terrainFromDB.addAll(DatabaseHelper.getTerrainForPoints(db, terrain.subList(i, terrain.size())));
            }
        }

        db.endTransaction();

        double currentHeight;
        for(int k = 0, l = 0;  (k < terrainFromDB.size() - 3) && (l < terrain.size()); k+=4, l++) {
            if (k + 3 < terrainFromDB.size()) {
                currentHeight = GraphPoint.getHeightForPoint(terrainFromDB.get(k), terrainFromDB.get(k + 1),
                        terrainFromDB.get(k + 2), terrainFromDB.get(k + 3), terrain.get(l));

                if (currentHeight >= pilotNormalFlyHeight - MIN_DISTANCE_UNDER_FLY_HEIGHT) {
                    return false;
                }
                if (currentHeight + flyHeightMinimum >  flyHeightMaximum) {
                    return false;
                }
            }
        }
        return true;
    }

/*
    public boolean checkLine(Point pointA, Point pointB, LineDirectory line) {
        canFlyNormal = true;
        cantFly = false;
        double xx = Math.abs(pointA.x - pointB.x);
        double yy = Math.abs(pointA.y - pointB.y);

        int minX = (int) (Math.floor(Math.min(pointA.x, pointB.x) / 100) - 1) * 100 + 50;

        int terrainSize = (int) Math.ceil(xx / 100) + 1;

        List<Point> terrain = new ArrayList<>();
        List<Pair<Point, Integer>> terrainFromDB = new ArrayList<>();
        DatabaseHelper db = new DatabaseHelper(context);

        db.getReadableDatabase().beginTransaction();

        for (int i = minX, j = 0; j < terrainSize; i+=100, j++) {
            terrain.add(new Point(i, line.valueOf(i)));
        }
        for (int i = 0; i < terrain.size(); i+=12 ) {
            if (i+12 < terrain.size()) {
                terrainFromDB.addAll(db.getTerrainForPoint(db.getReadableDatabase(), terrain.subList(i, i + 12)));
            } else {
                terrainFromDB.addAll(db.getTerrainForPoint(db.getReadableDatabase(), terrain.subList(i, terrain.size())));
            }
        }

        db.getReadableDatabase().endTransaction();

        double currentHeight;
        for(int k = 0, l = 0;  k < terrainFromDB.size() - 3; k+=3, l++) {
            if (k + 3 < terrainFromDB.size()) {
                currentHeight = LineDirectory.getHeightForPoint(terrainFromDB.get(k), terrainFromDB.get(k + 1),
                        terrainFromDB.get(k + 2), terrainFromDB.get(k + 3), terrain.get(l));

                if (currentHeight > flyHeightNormal - MIN_DISTANCE_UNDER_FLY_HEIGHT) {
                    canFlyNormal = false;
                }
                if (currentHeight + flyHeightMinimum >  flyHeightMaximum) {
                    cantFly = true;
                }
                if (!canFlyNormal || cantFly) {
                    break;
                }
            }
        }
        return (canFlyNormal || ! cantFly);
    }
    */
}
