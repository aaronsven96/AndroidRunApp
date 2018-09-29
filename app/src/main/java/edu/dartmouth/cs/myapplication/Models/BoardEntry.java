package edu.dartmouth.cs.myapplication.Models;

public class BoardEntry {
    private String email;
    private String activity_type;
    private String activity_date;
    private String input_type;
    private String duration;
    private String distance;

    public BoardEntry(String email,String activity_date,String activity_type,String input_type,String duration,String distance){
        this.email=email;
        this.activity_date=activity_date;
        this.activity_type=activity_type;
        this.input_type=input_type;
        this.duration=duration;
        this.distance=distance;
    }

    public String getEmail() {
        return email;
    }

    public String getActivity_type() {
        return activity_type;
    }

    public String getActivity_date() {
        return activity_date;
    }

    public String getInput_type() {
        return input_type;
    }

    public String getDuration() {
        return duration;
    }

    public String getDistance() {
        return distance;
    }
}
