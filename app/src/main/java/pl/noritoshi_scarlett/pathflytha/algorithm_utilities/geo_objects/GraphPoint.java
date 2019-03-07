package pl.noritoshi_scarlett.pathflytha.algorithm_utilities.geo_objects;


import com.google.maps.android.geometry.Point;

import java.util.List;

public class GraphPoint {

    final public double x;
    final public double y;
    public double z;

    public GraphPoint(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public GraphPoint(Point point, double z) {
        this.x = point.x;
        this.y = point.y;
        this.z = z;
    }


    static public boolean isSamePoints(GraphPoint test, GraphPoint point) {
        return test.x == point.x && test.y == point.y && test.z == point.z;
    }

    static public boolean isUniquePoint(List<GraphPoint> points, GraphPoint point) {
        if (points != null) {
            for (GraphPoint test : points) {
                if (isSamePoints(test, point)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * zaokraglenie wartosci dlapunktu
     * @param point     punkt wejsciowy
     * @return          punktz zaokraglonymi wartosciami
     */
    static public Point roundPoint(Point point) {
        return new Point(Math.round(point.x), Math.round(point.y));
    }

    /**
     * Funkcja obliczajaca wysokosc dla punktu na podstawie czterech sasiadujacych punktow.
     * Wykorzystuje metode aproksymacji powierzchnii terenowej za pomoca paraboli hiperbolicznej
     * Wykorzystywany jest wzor interpolacyjny w postaci ogolnej sredniej arytmetycznej
     * wiecej informacji: Waldemar Izdebski - Wykłady z przedmiotu SIT / Mapa zasadnicza
     * @param p_i_j   para punkt i wysokosc dla i, j
     * @param p_i1_j  para punkt i wysokosc dla i+1, j
     * @param p_i_j1  para punkt i wysokosc dla i, j+1
     * @param p_i1_j1 para punkt i wysokosc dla i+1, j+1
     * @param point   punkt srodkowy
     * @return        wysokosc dla punktu srodkowego
     */
    static public double getHeightForPoint(GraphPoint p_i_j, GraphPoint p_i1_j,
                                           GraphPoint p_i_j1, GraphPoint p_i1_j1,
                                           GraphPoint point) {
        double W_1, W_2, W_3, W_4;

        W_1 = Math.abs(p_i_j.x   - point.x) * Math.abs(p_i_j.y   - point.y);
        W_2 = Math.abs(p_i1_j.x  - point.x) * Math.abs(p_i1_j.y  - point.y);
        W_3 = Math.abs(p_i_j1.x  - point.x) * Math.abs(p_i_j1.y  - point.y);
        W_4 = Math.abs(p_i1_j1.x - point.x) * Math.abs(p_i1_j1.y - point.y);

        return ( (W_1 * p_i1_j1.z) + (W_2 * p_i_j1.z) + (W_3 * p_i1_j.z) + (W_4 * p_i_j.z) ) / 10000;
    }
}
