package edu.dartmouth.cs.myapplication.Models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;


import com.google.android.gms.maps.model.LatLng;

//Aaron Svendsen
//Note not all methods are used as of yet since we havent done the whole app

public class NewExerciseEntry implements Serializable {

    private long id;
    private int onBoard;
    private int onCloud;
    private int mInputType;        // Manual, GPS or automatic
    private int mActivityType;     // Running, cycling etc.
    private String mDateTime;    // When does this entry happen
    private int mDuration;         // Exercise duration in seconds
    private double mDistance;      // Distance traveled. Either in meters or feet.
    private double mAvgPace;       // Average pace
    private double mAvgSpeed;      // Average speed
    private int mCalorie;          // Calories burnt
    private double mClimb;         // Climb. Either in meters or feet.
    private int mHeartRate;        // Heart rate
    private String mComment;       // Comments
    private ArrayList<MyLatLng> mLocationList; // Location list
    private String Email;

    public NewExerciseEntry(){}
    @Override
    public String toString() {
        return mInputType + " " + mActivityType;
    }

    public int getmInputType() {
        return mInputType;
    }

    public void setmInputType(int mInputType) {
        this.mInputType = mInputType;
    }

    public int getmActivityType() {
        return mActivityType;
    }

    public void setmActivityType(int mActivityType) {
        this.mActivityType = mActivityType;
    }

    public String getmDateTime() {
        return mDateTime;
    }

    public void setmDateTime(String mDateTime) {
        this.mDateTime = mDateTime;
    }

    public int getmDuration() {
        return mDuration;
    }

    public void setmDuration(int mDuration) {
        this.mDuration = mDuration;
    }

    public double getmDistance() {
        return mDistance;
    }

    public void setmDistance(double mDistance) {
        this.mDistance = mDistance;
    }

    public double getmAvgPace() {
        return mAvgPace;
    }

    public void setmAvgPace(double mAvgPace) {
        this.mAvgPace = mAvgPace;
    }

    public double getmAvgSpeed() {
        return mAvgSpeed;
    }

    public void setmAvgSpeed(double mAvgSpeed) {
        this.mAvgSpeed = mAvgSpeed;
    }

    public int getmCalorie() {
        return mCalorie;
    }

    public void setmCalorie(int mCalorie) {
        this.mCalorie = mCalorie;
    }

    public double getmClimb() {
        return mClimb;
    }

    public void setmClimb(double mClimb) {
        this.mClimb = mClimb;
    }

    public int getmHeartRate() {
        return mHeartRate;
    }

    public void setmHeartRate(int mHeartRate) {
        this.mHeartRate = mHeartRate;
    }

    public String getmComment() {
        return mComment;
    }

    public void setmComment(String mComment) {
        this.mComment = mComment;
    }

    public ArrayList<MyLatLng> getmLocationList() {
        return mLocationList;
    }

    public void setmLocationList(ArrayList<MyLatLng> mLocationList) {
        this.mLocationList = mLocationList;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getOnBoard() {
        return onBoard;
    }

    public void setOnBoard(int onBoard) {
        this.onBoard = onBoard;
    }

    public int getOnCloud() {
        return onCloud;
    }

    public void setOnCloud(int onCloud) {
        this.onCloud = onCloud;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }
}
