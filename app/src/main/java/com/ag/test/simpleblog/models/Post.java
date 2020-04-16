package com.ag.test.simpleblog.models;

public class Post {
    String title;
    String description;
    String image;
    String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Post() {
    }

    public Post(String title, String description, String image,String name) {
        this.title = title;
        this.description = description;
        this.image = image;
        this.name = name;
    }
}
