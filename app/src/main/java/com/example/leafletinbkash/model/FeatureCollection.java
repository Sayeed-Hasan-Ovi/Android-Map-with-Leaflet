package com.example.leafletinbkash.model;

import java.util.List;

public class FeatureCollection {
    private String type;
    private List<Feature> features;

    // Getters and setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Feature> getFeatures() {
        return features;
    }

    public void setFeatures(List<Feature> features) {
        this.features = features;
    }
}