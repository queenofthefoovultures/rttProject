package com.indoorino.bme5;

public class PositionCalculator {

    private float[] last;

    // Constructor receives first vector as movement and stores it locally
    public PositionCalculator(float[] gpsFirsInput){
        last = gpsFirsInput;
    }

    // Input: new positionvalue data on local map
    // Return:
    public float[] giveNewVec(float[] gpsNewLocation){
        float[] movement = new float[3];
        movement[0] = gpsNewLocation[0] + last[0]; // X coordinate
        movement[1] = gpsNewLocation[1] + last[1]; // Y coordinate
        movement[2] = gpsNewLocation[2] + last[2]; // Z coordinate
        last = gpsNewLocation;
        return  movement;
    }
}
