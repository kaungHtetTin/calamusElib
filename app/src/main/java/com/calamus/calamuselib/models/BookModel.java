package com.calamus.calamuselib.models;


public class BookModel {
    String id;
    String title;
    String author;
    String description;
    String downloadCount;
    String votes;
    String thumbnail;
    String Url;
    String sell;

    public BookModel(String id,String title, String author, String description, String downloadCount, String votes, String thumbnail, String url,String sell) {
        this.title = title;
        this.author = author;
        this.description = description;
        this.downloadCount = downloadCount;
        this.votes = votes;
        this.thumbnail = thumbnail;
        this.id=id;
        Url = url;
        this.sell=sell;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getDescription() {
        return description;
    }

    public String getDownloadCount() {
        return downloadCount;
    }

    public String getVotes() {
        return votes;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getUrl() {
        return Url;
    }

    public void setVotes(String votes) {
        this.votes = votes;
    }

    public String getSell() {
        return sell;
    }
}
