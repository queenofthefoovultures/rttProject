package com.indoorino.bme5;

import android.util.Log;

import java.util.Queue;

public class PositionCalculator {

    private double[] last;
    private float lastRotation = 0f;
    private float[] avgTwentyArray = new float[20];

    // Constructor receives first vector as movement and stores it locally
    public PositionCalculator(double[] gpsFirsInput){
        last = gpsFirsInput;
    }

    // Input: new positionvalue data on local map
    // Return:
    public double[] giveNewVec(double[] enuNewLocation){
        double[] movement = new double[3];
        movement[0] = enuNewLocation[0] - last[0]; // X coordinate
        Log.i("enuNewLocation","");
        movement[1] = enuNewLocation[1] - last[1]; // Y coordinate
        movement[2] = enuNewLocation[2] - last[2]; // Z coordinate
        last = enuNewLocation;
        return  movement;
    }

    public float rotateDiff(float input){
        float tmp = input - lastRotation;
        lastRotation = input;
     return tmp;
    }

    /* function shall return average of floats
    public float avgOfTwenty(){

        float avg = 0;
        for (int i = 0; i < avgTwentyArray.length; i++){
            avg = avg + avgTwentyArray[i];
        }

        return (avg/avgTwentyArray.length);
    }
    */
}
