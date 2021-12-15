package com.calamus.calamuselib.models;

public class CategoryModel {
    String id;
    String title;

    public CategoryModel(String id, String title) {
        this.id = id;
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
}
