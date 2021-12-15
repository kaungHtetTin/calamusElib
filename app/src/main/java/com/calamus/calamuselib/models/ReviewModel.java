package com.calamus.calamuselib.models;

public class ReviewModel {
    String name;
    String review;
    String  time;

    public ReviewModel(String name, String review, String time) {
        this.name = name;
        this.review = review;
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public String getReview() {
        return review;
    }

    public String getTime() {
        return time;
    }
}
