package com.example.artik.studyt;

public class User {
    private String uid;
    private String name;
    private String picture;
    private String thumb_pic;
    private String subject1;
    private String subject2;
    private int score;

    public User(String uid, String name, String picture, String thumb_pic, String subject1, String subject2, int score) {
        this.uid = uid;
        this.name = name;
        this.picture = picture;
        this.thumb_pic = thumb_pic;
        this.subject1 = subject1;
        this.subject2 = subject2;
        this.score = score;
    }

    public User() {
    }

    public String getSubject1() {
        return subject1;
    }

    public void setSubject1(String subject1) {
        this.subject1 = subject1;
    }

    public String getSubject2() {
        return subject2;
    }

    public void setSubject2(String subject2) {
        this.subject2 = subject2;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getThumb_pic() {
        return thumb_pic;
    }

    public void setThumb_pic(String thumb_pic) {
        this.thumb_pic = thumb_pic;
    }

    public String getName() {
        return name;
    }

    public String getPicture() {
        return picture;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }
}

