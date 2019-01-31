package com.indoorino.bme5;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public class CoordinateConverter {

    static double a = 6378137.0;         // WGS-84 Earth semimajor axis (m)
    static double b = 6356752.314245;     // Derived Earth semiminor axis (m)
    static double f = (a - b) / a;           // Ellipsoid Flatness
    static double e_sq = f * (2 - f);    // Square of Eccentricity

    private Vector3 gpsBaseCoordinate; // 0 = Latitude, 1 = Longitude, 2 = Altitude
    private Vector3 ecefBaseCoordinate;
    private Matrix4 converterMatrix;

    // Constructor gets float-array of GPS Basis Coordinate
    CoordinateConverter(float[] gpsBasePoint){
        gpsBaseCoordinate = new Vector3(gpsBasePoint[0], gpsBasePoint[1], gpsBasePoint[2]);
        ecefBaseCoordinate = new Vector3(gps2ecef(gpsBaseCoordinate));
        createConverterMatrix(ecefBaseCoordinate);
    }

    public void setNewBasePoint(float[] gps){
        gpsBaseCoordinate = new Vector3(gps[0], gps[1], gps[2]);
        ecefBaseCoordinate = new Vector3(gps2ecef(gpsBaseCoordinate));
        createConverterMatrix(ecefBaseCoordinate);
    }

    // Input = GPS float Array //  0 = Latitude, 1 = Longitude, 2 = Altitude
    public Vector3 gps2LocalEnu(float[] gps){
        Vector3 gpsInput =  new Vector3(gps[0], gps[1], gps[2]);
        Vector3 ecef = new Vector3(gps2ecef(gpsInput));
        Vector3 enu = new Vector3(ecef2LocalEnu(ecef));
        return enu;
    }

    // tmp.x = lat; tmp.y = lon; tmp.z = alt
    public Vector3 gps2ecef(Vector3 gps){
        float lat = (float) Math.toRadians(gps.x);
        float lon = (float) Math.toRadians(gps.y);
        float[] ecef = new float[3];  //Results go here (x, y, z)
        float n = (float) (a/Math.sqrt( 1 - e_sq*Math.sin( lat )*Math.sin(lat) ));
        ecef[0] = (float) (( n + gps.z )*Math.cos( lat )*Math.cos( lon ));    //ECEF x
        ecef[1] = (float) (( n + gps.z )*Math.cos( lat )*Math.sin( lon ));    //ECEF y
        ecef[2] = (float) (( n*(1 - e_sq ) + gps.z )*Math.sin( lat ));        //ECEF z
        return (new Vector3(ecef[0],ecef[1],ecef[2]));
    }

    public Vector3 ecef2LocalEnu(Vector3 ecef){

        ecef.mul(converterMatrix);
        return (ecef);
    }

    // Ausgangspunkt ECEF Koordinaten des Ursprungs = tmp
    private void createConverterMatrix(Vector3 ecefCoordinates){
        Vector3 z = new Vector3(ecefCoordinates.nor());
        Vector3 x = new Vector3(0,0,1).crs(z).nor();
        Vector3 xcpy = x.cpy();
        Vector3 y = new Vector3(xcpy.crs(z)).nor();
        float[] matrixArray4x4 =  { x.x,x.y,x.z,-ecefCoordinates.x,
                                    y.x,y.y,y.z,-ecefCoordinates.y,
                                    z.x,z.y,z.z,-ecefCoordinates.z,
                                    0,  0,  0,  1       };

        converterMatrix = new Matrix4(matrixArray4x4);
    }
}
