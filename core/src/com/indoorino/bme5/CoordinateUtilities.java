package com.indoorino.bme5;


import java.lang.*;


public class CoordinateUtilities {

    static double a = 6378137.0;         // WGS-84 Earth semimajor axis (m)
    static double b = 6356752.314245;     // Derived Earth semiminor axis (m)
    static double f = (a - b) / a;           // Ellipsoid Flatness
    static double e_sq = f * (2 - f);    // Square of Eccentricity
    //private static final double e2 = 6.6943799901377997e-3;  //WGS-84 first eccentricity squared

    // Center Koordinates of calculated Area
    private static double centerLat = 49.448256;    // Degrees
    private static double centerLon = 11.095962;    // Degrees
    private static double centerAlt = 46.87;         // Meters

    CoordinateUtilities(){
    }

    // Combination of geo2ecef And ecef2enu
    // Input Arguments: current position latitude, current position longitude, current position altitude
    public static double[] geo2enu(double lat, double lon, double alt){
        double[] ecef = geo_to_ecef(lat, lon, alt);
        double[] enu = ecef2enu(ecef[0], ecef[1], ecef[2], centerLat, centerLon, centerAlt);
        return enu;
    }

    // Converts the Earth-Centered Earth-Fixed (ECEF) coordinates (x, y, z) to
    // East-North-Up coordinates in a Local Tangent Plane that is centered at the
    // (WGS-84) Geodetic point (lat0, lon0, h0).
    // https://gist.github.com/govert/1b373696c9a27ff4c72a
    public static double[] ecef2enu(double x, double y, double z, double lat0, double lon0, double h0){

        double lambda = Math.toRadians(lat0);
        double phi = Math.toRadians(lon0);
        double s = Math.sin(lambda);
        double N = a / Math.sqrt(1 - e_sq * s * s);

        double sin_lambda = Math.sin(lambda);
        double cos_lambda = Math.cos(lambda);
        double cos_phi = Math.cos(phi);
        double sin_phi = Math.sin(phi);

        double x0 = (h0 + N) * cos_lambda * cos_phi;
        double y0 = (h0 + N) * cos_lambda * sin_phi;
        double z0 = (h0 + (1 - e_sq) * N) * sin_lambda;

        double xd = x - x0;
        double yd = y - y0;
        double zd = z - z0;

        // This is the matrix multiplication
        double xEast = -sin_phi * xd + cos_phi * yd;
        double yNorth = -cos_phi * sin_lambda * xd - sin_lambda * sin_phi * yd + cos_lambda * zd;
        double zUp = cos_lambda * cos_phi * xd + cos_lambda * sin_phi * yd + sin_lambda * zd;

        double[] enu = {xEast, yNorth, zUp};
        return enu;
    }

    //Convert Lat, Lon, Altitude to Earth-Centered-Earth-Fixed (ECEF)
    //Input is a three element: lat, lon  and alt (m) // used to be lat, lon in (rads)
    //Returned array contains x, y, z in meters
    //http://danceswithcode.net/engineeringnotes/geodetic_to_ecef/geodetic_to_ecef.html
    public static double[] geo_to_ecef( double lat, double lon, double alt ) {
        double[] ecef = new double[3];  //Results go here (x, y, z)
        lat = Math.toRadians(lat); // changed here
        lon = Math.toRadians(lon); // changed here
        double n = a/Math.sqrt( 1 - e_sq*Math.sin( lat )*Math.sin( lat ) );
        ecef[0] = ( n + alt )*Math.cos( lat )*Math.cos( lon );    //ECEF x
        ecef[1] = ( n + alt )*Math.cos( lat )*Math.sin( lon );    //ECEF y
        ecef[2] = ( n*(1 - e_sq ) + alt )*Math.sin( lat );          //ECEF z
        return(ecef);     //Return x, y, z in ECEF
    }

}
