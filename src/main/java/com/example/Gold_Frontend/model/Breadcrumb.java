package com.example.Gold_Frontend.model;

public class Breadcrumb {
    private String name;
    private String url;

    public Breadcrumb(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}