package pl.noritoshi_scarlett.pathflytha.algorithm_utilities.geo_objects;

import android.support.annotation.Nullable;
import android.util.Pair;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.geometry.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LineDirectory {

    private double a;
    private double b;
    private boolean isOYPParallel = false;

    public String getFunction() {
        return String.valueOf(getA()) + "x + " + String.valueOf(getB());
    }

    /**
     * http://www.math.edu.pl/prosta-przechodzaca-przez-dwa-punkty
     * y = (y2 - y1) / (x2 - x1) * (x - x1) + y1
     * @param A     punkt A
     * @param B     punkt B
     */
    public LineDirectory(Point A, Point B) {
        if (A.x != B.x) {
            // stad: https://www.matemaks.pl/rownanie-prostej-przechodzacej-przez-dwa-punkty.html;
            this.a = (A.y - B.y) / (A.x - B.x);
            this.b = A.y - ( (A.y - B.y) / (A.x - B.x) ) * A.x;
        } else {
            //TODO
            isOYPParallel = true;
        }
    }
    public LineDirectory(GraphPoint A, GraphPoint B) {
        if (A.x != B.x) {
            // stad: https://www.matemaks.pl/rownanie-prostej-przechodzacej-przez-dwa-punkty.html;
            this.a = (A.y - B.y) / (A.x - B.x);
            this.b = A.y - ( (A.y - B.y) / (A.x - B.x) ) * A.x;
        } else {
            //TODO
            isOYPParallel = true;
        }
    }

    public LineDirectory(Pair<Point, Point> pair) {
        this(pair.first, pair.second);
    }

    public LineDirectory(LatLng A, LatLng B) {
        if (A.longitude != B.longitude) {
            this.a = (B.latitude - A.latitude) / (B.longitude - A.longitude) * A.longitude;
            this.b = a + A.latitude;
        } else {
            //TODO
            isOYPParallel = true;
        }
    }

    public LineDirectory(double a, double b) {
        this.isOYPParallel = (a == 0);
        this.a = a;
        this.b = b;
    }

    public void setA(double a) {
        this.isOYPParallel = (a == 0);
        this.a = a;
    }
    public double getA() {
        return a;
    }
    public double getB() {
        return b;
    }

    // WZOR NA PROSTA PRZECHODZACA PRZEZ PUNKT (X1,Y1)
    // y = ax + b
    // y1 - ax1 = b
    private double setBFromPoint(Point point, double new_a) {
        return point.y - new_a * point.x;
    }
    private double setBFromPoint(GraphPoint point, double new_a) { return point.y - new_a * point.x; }

    /**
     * linie rownolegle do obecnej i oddalone o odleglosc
     * @param distance      odleglosc
     * @return              lista linii
     */
    public List<LineDirectory> generateParallel(double distance) {
        // ZE WZORU NA ODLEGLOSC MIEDZY PROSTYMI ROWNOLEGLYMI
        // https://www.matematyka.pl/146337.htm
        // distance = ( |b - c| ) / ( sqrt( 1 + a^2 ) )
        // c = b -+ distance * sqrt( 1 + a^2 )
        // c to wyraz wolny
        if (distance == 0) {
            return Collections.singletonList(this);
        } else if (distance < 0) {
            distance *= -1;
        }
        List<LineDirectory> list = new ArrayList<>();
        double c = distance * Math.sqrt(1 + a*a);
        LineDirectory line;
        line = new LineDirectory( getA(), getB() + c );
        list.add(line);
        line = new LineDirectory( getA(), getB() - c );
        list.add(line);
        return list;
    }

    /*
        linia prostopadla do obecnej i przechddzaca przez punkt
     */
    public LineDirectory generateNormal(Point point) {
        return new LineDirectory(-1 / getA(), setBFromPoint(point, -1 / getA()) );
    }
    public LineDirectory generateNormal(GraphPoint point) {
        return new LineDirectory(-1 / getA(), setBFromPoint(point, -1 / getA()) );
    }

    /**
     * dystans pomiedzy punktem i linia
     * @param point     punkt
     * @param line      linia
     * @return          dystans
     */
    public static double distBetweenPointAndLine(Point point, LineDirectory line) {
        return distBetweenPointAndLine(point.x, point.y, line);
    }

    /**
     * dystans pomiedzy punktem i linia
     * @param x         wspolrzedna x
     * @param y         wsplorzedna y
     * @param line      linia
     * @return          dystans
     */
    public static double distBetweenPointAndLine(double x, double y, LineDirectory line) {

        // a2+b2=c2
        // sqrt(a2+b2)=c
        // h = ab / c
        // h = ab / sqrt(a2+b2)
        double distance;
        double lineY = line.getA() * x + line.getB();
        double lineX = (y - line.getB()) / line.getA();
        double YY = Math.abs(lineY - y);
        double XX = Math.abs(lineX - x);
        distance = (YY*XX) / Math.sqrt( YY*YY + XX*XX);
        return distance;
    }

    /**
     * sprawdza polozenie wektorow na okregu
     * @param edgeA     wezel przychodzacy
     * @param edgeB     wyezel wychodzacy
     * @return true     jesli oba wektory znajduja sie po tej samej stronie prostej
     */
    public static boolean checkLinksForCircle(GraphEdge edgeA, GraphEdge edgeB) {
        if (edgeA == null || edgeB == null) {
            return false;
        }

        LineDirectory line = new LineDirectory(edgeA.getTo(), edgeB.getFrom());
        boolean y_edgeA = line.valueOf(edgeA.getFrom().x) < edgeA.getFrom().x;
        boolean y_edgeB = line.valueOf(edgeB.getTo().x) < edgeB.getTo().x;

        return ((y_edgeA && y_edgeB) || (!y_edgeA && !y_edgeB));
    }

    /**
     * sprawdza polozenie punktow na okregu wzgledem wektorow, ktorych sa czescia (to co wyzej, ale inne dane wejsciowe)
     * @param A         wektor 1 - punkt zaczepny
     * @param B         wektor 2 - punkt zaczepny
     * @param C         wektor 1 - punkt dalszy
     * @param D         wektor 2 - punkt dalszy
     * @return          true jesli oba wektory znajduja sie po tej samej stronie prostej
     */
    public static boolean checkLinksForCircleAsPoints(GraphPoint A, GraphPoint B, GraphPoint C, GraphPoint D) {
        LineDirectory line = new LineDirectory(A, B);
        boolean y_edgeA = line.valueOf(C.x) < C.x;
        boolean y_edgeB = line.valueOf(D.x) < D.x;

        return ((y_edgeA && y_edgeB) || (!y_edgeA && !y_edgeB));
    }


    public static double distBetweenPointsAndLine(double x, int[][] y_array, LineDirectory line) {

        double closest = -1, temp, selected_y = -1;

        for (int[] aY_array : y_array) {
            temp = distBetweenPointAndLine(x, aY_array[0], line);
            if (selected_y < 0 || temp < closest) {
                closest = temp;
                selected_y = aY_array[1];
            }
            if (closest < 50 * Math.sqrt(2)) {
                break;
            }
        }
        return selected_y;
    }

    /**
     * dystans pomiedzy dwoma punktami w 2D
     * @param A         punkt A
     * @param B         punkt B
     * @return          odleglosc
     */
    public static double distBetweenPoints(Point A, Point B) {
        double XX = Math.abs(A.x - B.x);
        double YY = Math.abs(A.y - B.y);
        return Math.sqrt( YY*YY + XX*XX);
    }
    /**
     * dystans pomiedzy dwoma punktmai w 3D
     * @param A         punkt A
     * @param B         punkt B
     * @return          odleglosc
     */
    public static double distBetweenPoints(GraphPoint A, GraphPoint B) {
        double XX = Math.abs(A.x - B.x);
        double YY = Math.abs(A.y - B.y);
        double ZZ = Math.abs(A.z - B.z);
        return Math.sqrt( YY*YY + XX*XX + ZZ*ZZ);
    }

    /**
     * punkt srodkowy w dwuwymirowym ukladzie dla dwoch punktow
     * @param A         punkt A
     * @param B         punkt B
     * @return          punk srodkowy
     */
    public static Point centerFromTwoPoints(Point A, Point B) {
        double YY = Math.abs(A.y + B.y) / 2;
        double XX = Math.abs(A.x + B.x) / 2;
        return new Point(XX, YY);
    }
    /**
     * punkt srodkowy w trojwymiarowym ukladzie dla dwoch punkow
     * @param A         punkt A
     * @param B         punkt B
     * @return          punkt srodkowy
     */
    public static GraphPoint centerFromTwoPoints(GraphPoint A, GraphPoint B) {
        double XX = Math.abs(A.x + B.x) / 2;
        double YY = Math.abs(A.y + B.y) / 2;
        double ZZ = Math.abs(A.z + B.z) / 2;
        return new GraphPoint(XX, YY, ZZ);
    }

    /**
     * znajdowanie punktu przeciecia dwoch lnii
     * @param lineA     linia A
     * @param lineB     linia B
     * @return          zwracany punkt (moze byc nullem)
     */
    @Nullable
    public static Point findPointForLines(LineDirectory lineA, LineDirectory lineB) {
        if (lineA.a == lineB.a) {
            return null;
        } else {
            double x,y;
            // y = ax + b
            // y = a'x + c
            // ax + b = a'x + c
            // x(a - a') = c - b
            x = ( lineB.b - lineA.b ) / ( lineA.a - lineB.a );
            y = lineA.a * x + lineA.b;
            return new Point(x, y);
        }
    }

    /**
     * Poniewaz promienie sa rowne, to jest to srodek odcinka laczacego srodki okregow.
     * S == ( (x1+x2)/2 , (y1+y2)/2 )
     * @param centerA   start Point for line
     * @param centerB   end Point for line
     * @return          center Point in line
     */
    public static Point findCenterPointInLine(Point centerA, Point centerB) {
        return new Point(
                (centerA.x + centerB.x) / 2,
                (centerA.y + centerB.y) / 2
        );
    }

    /**
     * wartosc dla X
     * @param x         X
     * @return          Y dla podanego X
     */
    public double valueOf(double x) {
        return getA() * x + getB();
    }

}