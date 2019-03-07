package pl.noritoshi_scarlett.pathflytha.algorithm_utilities.small_quests;

import android.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import pl.noritoshi_scarlett.pathflytha.algorithm_utilities.geo_objects.LineDirectory;
import pl.noritoshi_scarlett.pathflytha.pojos.PojoObstacle;

public final class FarthestPointsFromPoints {

    public static Pair<List<PojoObstacle>, Double> getFarhestPoints(List<PojoObstacle> points) {

        if (points == null || points.size() < 2) {
            return null;
        }

        PojoObstacle selectedPoint, farhestPoint = null, tempPoint;
        int posOfMax = 0;
        List<Double> distances = new ArrayList<>();

        double temp = 0;
        for (int i = 0; i < points.size(); i++){
            selectedPoint = points.get(i);
            distances.add(0.0);
            for (int j = 0; j < points.size(); j++) {
                if (points.get(j).equals(selectedPoint)) {
                    continue;
                }
                tempPoint = points.get(j);
                temp = LineDirectory.distBetweenPoints(selectedPoint.getPoint(), tempPoint.getPoint());
                temp += (tempPoint.getItem_obs_range() / 2) + (selectedPoint.getItem_obs_range() / 2);
                if (temp > Collections.max(distances)) {
                    distances.add(i, temp);
                    farhestPoint = points.get(j);
                    posOfMax = i;
                }
            }
        }
        return new Pair<>(Arrays.asList(points.get(posOfMax), farhestPoint), distances.get(posOfMax));
    }

}
