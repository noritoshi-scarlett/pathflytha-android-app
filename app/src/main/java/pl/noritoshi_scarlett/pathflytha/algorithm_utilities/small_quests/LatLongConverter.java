package pl.noritoshi_scarlett.pathflytha.algorithm_utilities.small_quests;

import android.content.Context;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.geometry.Point;

import pl.noritoshi_scarlett.pathflytha.R;

import static java.lang.Math.PI;
import static java.lang.Math.asin;
import static java.lang.Math.atan;
import static java.lang.Math.cos;
import static java.lang.Math.cosh;
import static java.lang.Math.exp;
import static java.lang.Math.log;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sinh;
import static java.lang.Math.sqrt;
import static java.lang.Math.tan;

public class LatLongConverter {

    // poludnik zerowy
    private static final int L0 = 19;
    // kwadrat mimosrodu
    private static final double e2 = 0.0066943800229;
    //usredniony promien elipsoidy
    private static final double r = 6367449.1458;
    // wspolczynniki i,k,l (poczatkowe 4 wyrazy: 2, 4, 6 i 8)
    private static final double i[] = new double[]
            { 8.377318247343e-4,  7.608527788824e-7,  1.197638019173e-9,   2.443376242509e-12};
    private static final double k[] = new double[]
            { 3.356551485596e-3,  6.571873148457e-6,  1.764656426454e-8,   5.400482187757e-11};
    private static final double l[] = new double[]
            {-8.377321681640e-4, -5.905869626081e-8, -1.673488904988e-10, -2.167737805596e-13};
    // półoś równikowa
    private static final double ae = 6378137;
    // skala podobieńswa poludnika poczatowego
    private static final double m0 = 0.999983;

    static public String convertLongitudeToDegrees(double longitude) {
        StringBuilder builder = new StringBuilder();

        String longitudeDegrees = Location.convert(Math.abs(longitude), Location.FORMAT_SECONDS);
        String[] longitudeSplit = longitudeDegrees.split(":");
        builder.append(longitudeSplit[0]).append("° ");
        builder.append(longitudeSplit[1]).append("' ");
        builder.append(longitudeSplit[2]).append("\" ");
        if (longitude < 0) {
            builder.append("W");
        } else {
            builder.append("E");
        }
        return builder.toString();
    }

    static public String convertLatitudeToDegrees(double latitude) {
        StringBuilder builder = new StringBuilder();

        String latitudeDegrees = Location.convert(Math.abs(latitude), Location.FORMAT_SECONDS);
        String[] latitudeSplit = latitudeDegrees.split(":");
        builder.append(latitudeSplit[0]).append("° ");
        builder.append(latitudeSplit[1]).append("' ");
        builder.append(latitudeSplit[2]).append("\" ");
        if (latitude < 0) {
            builder.append("S");
        } else {
            builder.append("N");
        }
        return builder.toString();
    }

    static public String[] writeAsString(Context context,
                                         LatLng startLatLng, LatLng startOutLatLng, LatLng endTargetLatLng, LatLng endLatLng) {
        String[] text = new String[4];
        text[0] = context.getResources().getString(R.string.main_pickCoordinatesPickedData1)
                + "\n" + LatLongConverter.convertLongitudeToDegrees(startLatLng.longitude)
                + ",   " + LatLongConverter.convertLatitudeToDegrees(startLatLng.latitude)
                + "\n";
        text[1] = context.getResources().getString(R.string.main_pickCoordinatesPickedData2)
                + "\n" + LatLongConverter.convertLongitudeToDegrees(startOutLatLng.longitude)
                + ",   " + LatLongConverter.convertLatitudeToDegrees(startOutLatLng.latitude)
                + "\n";
        text[2] = context.getResources().getString(R.string.main_pickCoordinatesPickedData3)
                + "\n" + LatLongConverter.convertLongitudeToDegrees(endTargetLatLng.longitude)
                + ",   " + LatLongConverter.convertLatitudeToDegrees(endTargetLatLng.latitude)
                + "\n";
        text[3] = context.getResources().getString(R.string.main_pickCoordinatesPickedData4)
                + "\n" + LatLongConverter.convertLongitudeToDegrees(endLatLng.longitude)
                + ",   " + LatLongConverter.convertLatitudeToDegrees(endLatLng.latitude);
        return text;
    }

    /**
     * funkcja obliczajaca wartosc wspolrzednych prostokatnych
     * Zaokragla wartosc do liczby calkowitej
     * @param latitude szerokosc geo
     * @param longitude długość geo
     * @return wspolrzedne prostokatne LatLng
     */
    static public Point convertLatLongTo1992InMeters(double latitude, double longitude) {

        // L = L - L0
        longitude = longitude - L0;

        double fi1, al, bet;
        double y = 0.0;
        double x = 0.0;

        fi1 = fi(latitude);
        al = alfa(fi1, longitude);
        bet = beta(fi1, longitude);

        for (int j = 1; j <= 4; j++) {
            y = y + i[j-1] * sin((2*j*al)) * cosh((2*j*bet));
            x = x + i[j-1] * cos((2*j*al)) * sinh((2*j*bet));
        }

        y = (m0*(r * (al  + y)) - 5300000);
        x = (m0*(r * (bet + x)) + 500000) ;

        return new Point(Math.round(x), Math.round(y));
    }

    /**
     * funkcja pomocnicza
     * @param latitude szerokosc geograficzna
     * @return wartosc fi
     */
    static private double fi(double latitude) {
        double e = sqrt(e2);
        double tg1, tg2, atg;
        tg1 = tan(( (PI/4) + (Math.toRadians(latitude)/2) ));
        tg2 =  pow(   ( (1 - e * sin(Math.toRadians(latitude))) /
                        (1 + e * sin(Math.toRadians(latitude)))    ),
                (e/2) );
        atg = atan((tg1 * tg2));
        return (2 * atg) - (PI/2);
    }

    /**
     * funkcja pomocnicza
     * @param fi wartosc fi
     * @param longitude dlugosc geograficzna
     * @return wartosc alfa
     */
    static private double alfa(double fi, double longitude) {
        return atan((
                (sin((fi)))
                        /
                (cos((fi)) * cos(Math.toRadians(longitude)))));
    }

    /**
     * funkcja pomocnicza
     * @param fi wartosc fi
     * @param longitude dlugosc geograficzna
     * @return wartosc beta
     */
    static private double beta(double fi, double longitude) {
        return 0.5 * log(
                (1 + cos((fi)) * sin(Math.toRadians(longitude)))
                        / (1 - cos((fi)) * sin(Math.toRadians(longitude))));
    }



    static public LatLng convert1992InMetersToLatLong(Point point) {

        double x0=-5300000.0;
        double y0= 500000.0;
        double x = (point.x-x0)/m0;
        double y =(point.y-y0)/m0;

        double al, bt, h, f, B, L;
        al = alfa2(x, y);
        bt = beta2(x, y);
        h = ha2(bt);
        f = fi2(h, al);
        B = (be(f))/Math.PI*180.0;
        L = L0 + (la(h, al))/Math.PI*180.0;

        return new LatLng(B, L);
    }



    static private double alfa2(double x, double y) {

        double xpr, ypr;
        double ap = 0.0;

        xpr = (x / r);
        ypr = (y / r);

        for (int j = 1; j <= 4; j++) {
            ap = ap + l[j - 1] * sin(2 * j * xpr) * cosh(2 * j * ypr);
        }

        return xpr + ap;
    }

    static private double beta2(double x, double y) {

        double xpr, ypr;
        double bp = 0.0;

        xpr = (x / r);
        ypr = ( y / r);

        for (int j = 1; j <= 4; j++) {
            bp = bp + l[j - 1] * cos(2 * j * xpr) * sinh(2 * j * ypr);
        }

        return ypr + bp;
    }

    static private double ha2(double bet) {

        return 2.0*atan(exp(bet))-Math.PI/2.0;
    }

    static private double fi2(double h, double al) {

        return  asin(cos(h)*sin(al));
    }

    static private double be(double f) {
        double bp = 0.0;

        for (int j = 1; j <= 4; j++) {
            bp = bp + k[j - 1] * sin(2 * j * f);
        }
        return  bp + f;
    }

    static private double la(double h, double al) {

        return  atan(tan(h)/cos(al));
    }
}
