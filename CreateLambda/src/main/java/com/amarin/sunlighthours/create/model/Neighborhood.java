package com.amarin.sunlighthours.create.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;
import java.util.UUID;

public class Neighborhood {
    private String id;
    private String neighborhood;
    private double apartments_height;
    private List<Building> buildings;

    public Neighborhood(String json) {
        Gson gson = new Gson();
        Neighborhood request = gson.fromJson(json, Neighborhood.class);
        this.id = request.getId();
        this.neighborhood = request.getNeighborhood();
        this.apartments_height = request.getApartments_height();
        this.buildings = request.getBuildings();
    }

    public String toString() {
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public void setNeighborhood(String neighborhood) {
        this.neighborhood = neighborhood;
    }

    public double getApartments_height() {
        return apartments_height;
    }

    public void setApartments_height(double apartments_height) {
        this.apartments_height = apartments_height;
    }

    public List<Building> getBuildings() {
        return buildings;
    }

    public void setBuildings(List<Building> buildings) {
        this.buildings = buildings;
    }

    public void init() {
        setId(UUID.randomUUID().toString());

        for (Building building : buildings) {
            building.init();
        }
    }
}
