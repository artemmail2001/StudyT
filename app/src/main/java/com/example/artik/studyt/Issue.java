package com.example.artik.studyt;

public class Issue {
    private String date;
    private String name;
    private String text;
    private String thumb;
    private double latitude;
    private double longitude;
    public int number_people;
    public int number_people_left;
    private int score;
    private String title;
    private String uid;
    private String key;

    public Issue(String date, String name, String text, String thumb, double latitude, double longitude, int number_people, int number_people_left, int score, String title, String uid, String key) {
        this.date = date;
        this.name = name;
        this.text = text;
        this.thumb = thumb;
        this.latitude = latitude;
        this.longitude = longitude;
        this.number_people = number_people;
        this.number_people_left = number_people_left;
        this.score = score;
        this.title = title;
        this.uid = uid;
        this.key = key;
    }
    public Issue() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getNumber_people() {
        return number_people;
    }

    public void setNumber_people(int number_people) {
        this.number_people = number_people;
    }

    public int getNumber_people_left() {
        return number_people_left;
    }

    public void setNumber_people_left(int number_people_left) {
        this.number_people_left = number_people_left;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
