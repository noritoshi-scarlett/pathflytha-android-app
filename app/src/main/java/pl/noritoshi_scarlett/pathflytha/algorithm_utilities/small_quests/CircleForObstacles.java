package pl.noritoshi_scarlett.pathflytha.algorithm_utilities.small_quests;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Pair;

import com.google.maps.android.geometry.Point;

import java.util.ArrayList;
import java.util.List;

import pl.noritoshi_scarlett.pathflytha.algorithm_utilities.geo_objects.GraphConnector;
import pl.noritoshi_scarlett.pathflytha.algorithm_utilities.geo_objects.LineDirectory;
import pl.noritoshi_scarlett.pathflytha.pojos.PojoObstacle;

import static pl.noritoshi_scarlett.pathflytha.algorithm_utilities.GenerateGraph.TOO_CLOSE_DISTANCE_FROM_OBJECT;

public class CircleForObstacles {

    private final Double MIN_DISTANCE_FROM_OBJECT;

    private final Context context;
    private List<PojoObstacle> obstaclesList;
    private Point centeroid;
    private double radius;
    private List<PojoObstacle> obstaclesInCircle;
    private List<PojoObstacle> convexHullPoints;
    private GraphConnector graphConnector;

    public CircleForObstacles(Context context, List<PojoObstacle> obstaclesList, double radius, PojoObstacle center) {
        this.context = context;
        this.obstaclesList = obstaclesList;
        this.radius = radius;
        this.centeroid = center.getPoint();
        obstaclesInCircle = new ArrayList<>();
        obstaclesInCircle.add(center);
        searchAroundObstacle();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.context);
        MIN_DISTANCE_FROM_OBJECT = Double.valueOf(prefs.getString("preference_minDistanceFromObstacle", "50"));

    }

    public Point getCentroid() {
        return centeroid;
    }

    public double getRadius() {
        return radius - MIN_DISTANCE_FROM_OBJECT;
    }

    private void searchAroundObstacle() {

        for (int i = 0; i < obstaclesList.size(); i++) {

            double distanceInner = Math.ceil(LineDirectory.distBetweenPoints(
                    centeroid,
                    obstaclesList.get(i).getPoint()));

            if (distanceInner == 0) {
                obstaclesList.get(i).setSelected(true);
                //allObstacles.add(obstaclesList.get(i));
                continue;
            }

            if ( ! obstaclesList.get(i).isSelected() && distanceInner <= (this.radius)) {
                obstaclesList.get(i).setSelected(true);
                if (obstaclesInCircle.size() < 2) {
                    this.radius = Math.max(obstaclesInCircle.get(0).getItem_obs_range() / 2, obstaclesList.get(i).getItem_obs_range() / 2)
                                + (distanceInner / 2)
                                + MIN_DISTANCE_FROM_OBJECT * 2;
                    centeroid = LineDirectory.centerFromTwoPoints(
                            obstaclesInCircle.get(0).getPoint(), obstaclesList.get(i).getPoint());
                    obstaclesInCircle.add(obstaclesList.get(i));
                } else {
                    if (! GrahamScan.areAllCollinear(obstaclesInCircle)) {
                        convexHullPoints = GrahamScan.getConvexHull(obstaclesInCircle);
                        Pair<List<PojoObstacle>, Double> maxPointsAndDist =
                                FarthestPointsFromPoints.getFarhestPoints(convexHullPoints);
                        centeroid = LineDirectory.centerFromTwoPoints(
                                maxPointsAndDist.first.get(0).getPoint(), maxPointsAndDist.first.get(1).getPoint());
                        this.radius = (maxPointsAndDist.second / 2) + MIN_DISTANCE_FROM_OBJECT * 2;
                    } else {
                        //TODO if colinear point
                        centeroid = LineDirectory.centerFromTwoPoints(
                                 LineDirectory.centerFromTwoPoints(obstaclesInCircle.get(0).getPoint(), obstaclesInCircle.get(1).getPoint()),
                                 LineDirectory.centerFromTwoPoints(obstaclesInCircle.get(1).getPoint(), centeroid)
                        );
                        this.radius += (obstaclesList.get(i).getItem_obs_range() / 2) + (distanceInner / 2);
                    }
                }

                searchAroundObstacle();
                break;
            }  else if (distanceInner <= this.radius + TOO_CLOSE_DISTANCE_FROM_OBJECT) {
                obstaclesList.get(i).setTooClose(true);
            }
        }
    }

    public List<PojoObstacle> getObstacles() {
        return obstaclesInCircle;
    }

   // public GraphCircle getGraphCircle() {
   //     graphCircle = new GraphCircle(obstaclesInCircle, radius, getCentroid(), null);
   //     return graphCircle;
   // }
}
