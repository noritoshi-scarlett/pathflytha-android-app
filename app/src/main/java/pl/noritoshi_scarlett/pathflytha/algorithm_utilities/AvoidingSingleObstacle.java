package pl.noritoshi_scarlett.pathflytha.algorithm_utilities;

import com.google.maps.android.geometry.Point;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import pl.noritoshi_scarlett.pathflytha.algorithm_utilities.geo_objects.LineDirectory;

import static java.lang.Double.NaN;

public class AvoidingSingleObstacle {

    private Point startPoint;
    private Point endPoint;
    private Point centerOfStartCircle;
    private Point centerOfEndCircle;
    private Point centerOfObstacleCircle;
    private Point pointBeetweenStartAndObstacleCircles;
    private Point pointBeetweenObstacleAndEndCircles;
    private double alfa;
    private double radius;
    private double fullLength;
    private LineDirectory line;

    AvoidingSingleObstacle(
            Point startPoint, Point endPoint,
            Point centerOfStartCircle, Point centerOfEndCircle,
            Point centerOfObstacleCircle,
            Point pointBeetweenStartAndObstacleCircles, Point pointBeetweenObstacleAndEndCircles,
            double alfa, double radius,
            LineDirectory line) {

        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.centerOfStartCircle = centerOfStartCircle;
        this.centerOfEndCircle = centerOfEndCircle;
        this.centerOfObstacleCircle = centerOfObstacleCircle;
        this.pointBeetweenStartAndObstacleCircles = pointBeetweenStartAndObstacleCircles;
        this.pointBeetweenObstacleAndEndCircles = pointBeetweenObstacleAndEndCircles;
        this.alfa = alfa;
        this.radius = radius;
        this.line = line;
        generateLength();
    }

    private void generateLength() {
        fullLength = (4 * alfa) / 360 * 2 * radius * Math.PI;
    }
    public double getFullLength() {
        return fullLength;
    }
    public Point getStartPoint() {
        return startPoint;
    }
    public Point getEndPoint() {
        return endPoint;
    }

    public List<List<LineGraphSeries<DataPoint>>> generateXYFunction() {

        List<List<LineGraphSeries<DataPoint>>> listOfAll = new ArrayList<>();
        boolean isUnder = (line.valueOf(centerOfObstacleCircle.x) > centerOfObstacleCircle.y);

         listOfAll.add(getPointsForCircleFunction(
                 startPoint, pointBeetweenStartAndObstacleCircles, centerOfStartCircle,
                 isUnder,"START_CIRCLE"));
         listOfAll.add(getPointsForCircleFunction(
                 pointBeetweenStartAndObstacleCircles, pointBeetweenObstacleAndEndCircles, centerOfObstacleCircle,
                 isUnder,"CENTER_CIRCLE"));
         listOfAll.add(getPointsForCircleFunction(
                 pointBeetweenObstacleAndEndCircles, endPoint, centerOfEndCircle,
                 isUnder, "END_CIRCLE"));

        return listOfAll; //series;

    }


    private List<LineGraphSeries<DataPoint>> getPointsForCircleFunction(Point fromPoint, Point toPoint, Point center,
                                                                  boolean under, String type) {

        LineGraphSeries<DataPoint> entries1 = new LineGraphSeries<>();
        LineGraphSeries<DataPoint> entries2 = new LineGraphSeries<>();
        double fromX = center.x - radius - 10;
        double toX = center.x + radius + 10;
        double pointsDistance = Math.abs(fromX - toX) / 30;
        double x = fromX, temp, y1, y2, yf1, yf2, yl1, yl2;
        LineDirectory lineBeetweenCircles
                = new LineDirectory(pointBeetweenStartAndObstacleCircles, pointBeetweenObstacleAndEndCircles);

        yl1 = 0;
        yl2 = 0;

        double CURRENT_1_distBeetweenPointAndCircleCenter, CURRENT_2_distBeetweenPointAndCircleCenter;
        double CURRENT_1_distBeetweenPointAndPath, CURRENT_2_distBeetweenPointAndPath;
        double distBeetweenPathAndObstacleCenter = 0, distBeetweenPathFromAndPointBeetweenCircles = 0;
        Point PointInPath = (type.equals("START_CIRCLE")) ? fromPoint : toPoint;
        Point pointInLoop_1, pointInLoop_2;

        switch (type) {
            case "START_CIRCLE":
                distBeetweenPathAndObstacleCenter = LineDirectory.distBetweenPoints(fromPoint, centerOfObstacleCircle);
                distBeetweenPathFromAndPointBeetweenCircles
                        = LineDirectory.distBetweenPoints(pointBeetweenStartAndObstacleCircles, fromPoint);
                break;
            case "CENTER_CIRCLE":
                distBeetweenPathAndObstacleCenter = 0;
                distBeetweenPathFromAndPointBeetweenCircles = 0;
                break;
            case "END_CIRCLE":
                distBeetweenPathAndObstacleCenter = LineDirectory.distBetweenPoints(toPoint, centerOfObstacleCircle);
                distBeetweenPathFromAndPointBeetweenCircles
                        = LineDirectory.distBetweenPoints(pointBeetweenObstacleAndEndCircles, toPoint);
                break;
        }

        for (int i = 0; i < 30; i++) {
            x = (fromX < toX) ? x + pointsDistance : x - pointsDistance;
            temp = Math.sqrt(radius * radius - Math.pow(x - center.x, 2));
            y1 = temp + center.y;
            y2 = -temp + center.y;
            yf1 = 0; yf2 = 0;

            if (distBeetweenPathAndObstacleCenter > 0 && distBeetweenPathFromAndPointBeetweenCircles > 0) {
                pointInLoop_1 = new Point(x, y1);
                pointInLoop_2 = new Point(x, y2);
                CURRENT_1_distBeetweenPointAndCircleCenter =
                        LineDirectory.distBetweenPoints(pointInLoop_1, centerOfObstacleCircle);
                CURRENT_2_distBeetweenPointAndCircleCenter =
                        LineDirectory.distBetweenPoints(pointInLoop_2, centerOfObstacleCircle);
                CURRENT_1_distBeetweenPointAndPath =
                        LineDirectory.distBetweenPoints(PointInPath, pointInLoop_1);
                CURRENT_2_distBeetweenPointAndPath =
                        LineDirectory.distBetweenPoints(PointInPath, pointInLoop_2);
                if (CURRENT_1_distBeetweenPointAndCircleCenter <= distBeetweenPathAndObstacleCenter
                        && CURRENT_1_distBeetweenPointAndPath <= distBeetweenPathFromAndPointBeetweenCircles) {
                    yf1 = y1;
                }
                if (CURRENT_2_distBeetweenPointAndCircleCenter <= distBeetweenPathAndObstacleCenter
                        && CURRENT_2_distBeetweenPointAndPath <= distBeetweenPathFromAndPointBeetweenCircles) {
                    yf2 = y2;
                }
            } else {
                if (under) {
                    if (y1 >= lineBeetweenCircles.valueOf(x)) {
                        yf1 = y1;
                    }
                    if (y2 >= lineBeetweenCircles.valueOf(x)) {
                        yf2 = y2;
                    }
                } else {
                    if (y1 <= lineBeetweenCircles.valueOf(x)) {
                        yf1 = y1;
                    }
                    if (y2 <= lineBeetweenCircles.valueOf(x)) {
                        yf2 = y2;
                    }
                }
            }

            if (x > entries1.getHighestValueX() && yf1 != yl1 && yf1 != 0 && yf1 != NaN) {
                entries1.appendData(new DataPoint(x, yf1), false, 30);
            }
            if (x > entries2.getHighestValueX() && yf2 != yl2 && yf2 != 0 && yf2 != NaN) {
                entries2.appendData(new DataPoint(x, yf2), false, 30);
            }

            yl1 = yf1;
            yl2 = yf2;
        }
        return Arrays.asList(entries1, entries2);
    }
}
