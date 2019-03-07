package pl.noritoshi_scarlett.pathflytha.algorithm_utilities;

import java.util.ArrayList;
import java.util.List;

public class DoubleAvoidingSingleObstacle {

    private List<AvoidingSingleObstacle> avoidList;

    public DoubleAvoidingSingleObstacle(List<AvoidingSingleObstacle> avoidList) {
        this.avoidList = avoidList;
    }

    DoubleAvoidingSingleObstacle() {
        this.avoidList = new ArrayList<>();
    }

    public AvoidingSingleObstacle getShortPath() {
        if (avoidList.size() >= 2) {
            if (avoidList.get(0).getFullLength() > avoidList.get(1).getFullLength()) {
                return avoidList.get(1);
            }
            if (avoidList.get(0).getFullLength() < avoidList.get(1).getFullLength()) {
                return avoidList.get(0);
            }
            //its same
            return avoidList.get(1);
        }
        return null;
    }

    public void add(AvoidingSingleObstacle avoid) {
        avoidList.add(avoid);
    }

    public AvoidingSingleObstacle getLongPath() {
        if (avoidList.size() >= 2) {
            if (avoidList.get(0).getFullLength() > avoidList.get(1).getFullLength()) {
                return avoidList.get(0);
            }
            if (avoidList.get(0).getFullLength() < avoidList.get(1).getFullLength()) {
                return avoidList.get(1);
            }
            //its same
            return avoidList.get(0);
        }
        return null;
    }
}
