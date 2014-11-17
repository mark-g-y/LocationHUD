package com.locationhud.compassdirection;

import android.util.Log;

import com.locationhud.storage.FileStorage;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by Mark on 16/11/2014.
 */
public class Converter {

    static int l_value = 65341;
    static int _361 = 361;

    static int nmax;
    static double currentHeight;
    static double[] cc = new double[l_value+ 1];
    static double[] cs = new double[l_value+ 1];
    static double[] hc = new double[l_value+ 1];
    static double[] hs = new double[l_value+ 1];
    static double[] p = new double[l_value+ 1];
    static double[] sinml = new double[_361+ 1];
    static double[] cosml = new double [_361+ 1];
    static double[] rleg = new double[_361+ 1];

    static Converter converter = null;

    private static int lastIndex = 0;

    public static Converter getInstance () {
        if (converter == null) {
            converter = new Converter();
            initArrays();
            currentHeight = -9999;
        }
        return converter;
    }


    double getHeightFromLatitudeAndLongitude(double lat, double lon) {
        updatePositionWithLatitudeAndLongitude(lat, lon);
        return getCurrentHeightOffset();
    }

    public double getCurrentHeightOffset() {
        return currentHeight;
    }

    public void updatePositionWithLatitudeAndLongitude(double lat, double lon) {
        double rad = 180 / Math.PI;
        double flat, flon, u;
        flat = lat; flon = lon;

        u = undulation(flat / rad, flon / rad, nmax, nmax + 1);

    /*u is the geoid undulation from the egm96 potential coefficient model
       including the height anomaly to geoid undulation correction term
       and a correction term to have the undulations refer to the
       wgs84 ellipsoid. the geoid undulation unit is meters.*/
        currentHeight = u;
    }


    double hundu(int nmax, double[] p,
                 double[] hc, double[] hs,
                 double[] sinml, double[] cosml, double gr, double re,
                 double[] cc, double[] cs) {/*constants for wgs84(g873);gm in units of m**3/s**2*/
        double gm = .3986004418e15, ae = 6378137.;
        double arn, ar, ac, a, b, sum, sumc, sum2, tempc, temp;
        int k, n, m;
        ar = ae / re;
        arn = ar;
        ac = a = b = 0;
        k = 3;
        for (n = 2; n <= nmax; n++) {
            arn *= ar;
            k++;
            sum = p[k] * hc[k];
            sumc = p[k] * cc[k];
            sum2 = 0;
            for (m = 1; m <= n; m++) {
                k++;
                tempc = cc[k] * cosml[m] + cs[k] * sinml[m];
                temp = hc[k] * cosml[m] + hs[k] * sinml[m];
                sumc += p[k] * tempc;
                sum += p[k] * temp;
            }
            ac += sumc;
            a += sum * arn;
        }
        ac += cc[1] + p[2] * cc[2] + p[3] * (cc[3] * cosml[1] + cs[3] * sinml[1]);
/*add haco=ac/100 to convert height anomaly on the ellipsoid to the undulation
add -0.53m to make undulation refer to the wgs84 ellipsoid.*/
        return a * gm / (gr * re) + ac / 100 - .53;
    }

    void dscml(double rlon, int nmax, double[] sinml, double[] cosml) {
        double a, b;
        int m;
        a = Math.sin(rlon);
        b = Math.cos(rlon);
        sinml[1] = a;
        cosml[1] = b;
        sinml[2] = 2 * b * a;
        cosml[2] = 2 * b * b - 1;
        for (m = 3; m <= nmax; m++) {
            sinml[m] = 2 * b * sinml[m - 1] - sinml[m - 2];
            cosml[m] = 2 * b * cosml[m - 1] - cosml[m - 2];
        }
    }

    public static void dhcsin(int nmax, double[] hc, double[] hs) {

        Integer n, m;
        Double j2, j4, j6, j8, j10, c, s, ec, es;
/*the even degree zonal coefficients given below were computed for the
 wgs84(g873) system of constants and are identical to those values
 used in the NIMA gridding procedure. computed using subroutine
 grs written by N.K. PAVLIS*/
        j2 = 0.108262982131e-2;
        j4 = -.237091120053e-05;
        j6 = 0.608346498882e-8;
        j8 = -0.142681087920e-10;
        j10 = 0.121439275882e-13;
        m = ((nmax + 1) * (nmax + 2)) / 2;
        for (n = 1; n <= m; n++) {
            hc[n] = hs[n] = 0;
        }

        String path1 = "res/raw/egm96";
        InputStream in = FileStorage.class.getClassLoader().getResourceAsStream(path1);
        long startTime = System.currentTimeMillis();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] lines = line.split(" ");
                n = Integer.parseInt(getNextNoneEmptyString(lines, 0));
                m = Integer.parseInt(getNextNoneEmptyString(lines, lastIndex));
                c = Double.parseDouble(getNextNoneEmptyString(lines, lastIndex));
                s = Double.parseDouble(getNextNoneEmptyString(lines, lastIndex));
                ec = Double.parseDouble(getNextNoneEmptyString(lines, lastIndex));
                es = Double.parseDouble(getNextNoneEmptyString(lines, lastIndex));
                if (n > nmax) {
                } else {
                    n = (n * (n + 1)) / 2 + m + 1;
                    hc[n] = c;
                    hs[n] = s;
                }
            }
            reader.close();
            in.close();
        } catch(Exception e) {
            e.printStackTrace();
        }

        Log.i("TIMINGTIMING", "foo2" + (System.currentTimeMillis() - startTime));

        hc[4] += j2 / Math.sqrt(5);
        hc[11] += j4 / 3;
        hc[22] += j6 / Math.sqrt(13);
        hc[37] += j8 / Math.sqrt(17);
        hc[56] += j10 / Math.sqrt(21);
    }

    void legfdn(int m, double theta, double[] rleg, int nmx)
/*this subroutine computes  all normalized legendre function
in "rleg". order is always
m, and colatitude is always theta  (radians). maximum deg
is  nmx. all calculations in double precision.
ir  must be set to zero before the first call to this sub.
the dimensions of arrays  rleg must be at least equal to  nmx+1.
Original programmer :Oscar L. Colombo, Dept. of Geodetic Science
the Ohio State University, August 1980
ineiev: I removed the derivatives, for they are never computed here*/
    {
        double[] drts = new double[1301];
        double[] dirt = new double [1301];
        double cothet;
        double sithet;
        double[] rlnn = new double[_361+ 1];
        int ir = 0;
        int nmx1 = nmx + 1, nmx2p = 2 * nmx + 1, m1 = m + 1, m2 = m + 2, m3 = m + 3, n, n1, n2;
        if (ir != 0) {
            ir = 1;
            for (n = 1; n <= nmx2p; n++) {
                drts[n] = Math.sqrt(n);
                dirt[n] = 1 / drts[n];
            }
        }
        cothet = Math.cos(theta);
        sithet = Math.sin(theta);
    /*compute the legendre functions*/
        rlnn[1] = 1;
        rlnn[2] = sithet * drts[3];
        for (n1 = 3; n1 <= m1; n1++) {
            n = n1 - 1;
            n2 = 2 * n;
            rlnn[n1] = drts[n2 + 1] * dirt[n2] * sithet * rlnn[n];
        }
        switch (m) {
            case 1:
                rleg[2] = rlnn[2];
                rleg[3] = drts[5] * cothet * rleg[2];
                break;
            case 0:
                rleg[1] = 1;
                rleg[2] = cothet * drts[3];
                break;
        }
        rleg[m1] = rlnn[m1];
        if (m2 <= nmx1) {
            rleg[m2] = drts[m1 * 2 + 1] * cothet * rleg[m1];
            if (m3 <= nmx1)
                for (n1 = m3; n1 <= nmx1; n1++) {
                    n = n1 - 1;
                    if ((m != 0 && n < 2) || (m == 1 && n < 3))continue;
                    n2 = 2 * n;
                    rleg[n1] = drts[n2 + 1] * dirt[n + m] * dirt[n - m] *
                            (drts[n2 - 1] * cothet * rleg[n1 - 1] - drts[n + m - 1] * drts[n - m - 1] * dirt[n2 - 3] * rleg[n1 - 2]);
                }
        }
    }

    ArrayList<Double> radgra(double lat, double lon, Double rlat, Double gr, Double re)
/*this subroutine computes geocentric distance to the point,
the geocentric latitude,and
an approximate value of normal gravity at the point based
the constants of the wgs84(g873) system are used*/
    {
        double a = 6378137., e2 = .00669437999013, geqt = 9.7803253359, k = .00193185265246;
        double n, t1 = Math.sin(lat) * Math.sin(lat), t2, x, y, z;
        n = a / Math.sqrt(1 - e2 * t1);
        t2 = n * Math.cos(lat);
        x = t2 * Math.cos(lon);
        y = t2 * Math.sin(lon);
        z = (n * (1 - e2)) * Math.sin(lat);
        re = Math.sqrt(x * x + y * y + z * z);/*compute the geocentric radius*/
        rlat = Math.atan(z / Math.sqrt(x * x + y * y));/*compute the geocentric latitude*/
        gr = geqt * (1 + k * t1) / Math.sqrt(1 - e2 * t1);/*compute normal gravity:units are m/sec**2*/
        ArrayList<Double> toReturn = new ArrayList<Double>();
        toReturn.add(re);
        toReturn.add(rlat);
        toReturn.add(gr);
        return toReturn;
    }


    double undulation(double lat, double lon, int nmax, int k) {
        Double rlat = 0.0, gr = 0.0, re = 0.0;
        int i, j, m;
        ArrayList<Double> results = radgra(lat, lon, rlat, gr, re);
        rlat = results.get(0);
        gr = results.get(1);
        re = results.get(2);
        rlat = Math.PI / 2 - rlat;
        for (j = 1; j <= k; j++) {
            m = j - 1;
            legfdn(m, rlat, rleg, nmax);
            for (i = j; i <= k; i++)p[(i - 1) * i / 2 + m + 1] = rleg[i];
        }
        dscml(lon, nmax, sinml, cosml);
        return hundu(nmax, p, hc, hs, sinml, cosml, gr, re, cc, cs);
    }

    static void initArrays() {
        int ig, i, n, m;
        double t1, t2;

        nmax = 360;
        for (i = 1; i <= l_value; i++) {
            cc[i] = cs[i] = 0;
        }

        String path1 = "res/raw/corcoef";
        InputStream in = FileStorage.class.getClassLoader().getResourceAsStream(path1);
        long startTime = System.currentTimeMillis();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] lines = line.split(" ");
                n = Integer.parseInt(getNextNoneEmptyString(lines, 0));
                m = Integer.parseInt(getNextNoneEmptyString(lines, lastIndex));
                t1 = Double.parseDouble(getNextNoneEmptyString(lines, lastIndex));
                t2 = Double.parseDouble(getNextNoneEmptyString(lines, lastIndex));
                ig = (n * (n + 1)) / 2 + m + 1;
                cc[ig] = t1;
                cs[ig] = t2;
            }
//            sc.close();
            reader.close();
            in.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
        Log.d("TIMINGTIMING", "foo" + (System.currentTimeMillis() - startTime));
/*the correction coefficients are now read in*/
/*the potential coefficients are now read in and the reference
 even degree zonal harmonic coefficients removed to degree 6*/
        dhcsin(nmax, hc, hs);
    }

    private static String getNextNoneEmptyString(String[] line, int startIndex) {
        for (int ind = startIndex; ind < line.length; ind++) {
            if (!"".equals(line[ind])) {
                lastIndex = ind + 1;
                return line[ind];
            }
        }
        return null;
    }
}
