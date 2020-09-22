package com.amarin.sunlighthours.init.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Building {
    private String id;
    private String name;
    private int apartments_count;
    private double distance;
    private List<Appartment> apartments;

    public Building(String json) {
        Gson gson = new Gson();
        Building request = gson.fromJson(json, Building.class);
        this.id = request.getId();
        this.name = request.getName();
        this.apartments_count = request.getApartments_count();
        this.distance = request.getDistance();
        this.apartments = new ArrayList<Appartment>();
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getApartments_count() {
        return apartments_count;
    }

    public void setApartments_count(int apartments_count) {
        this.apartments_count = apartments_count;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public List<Appartment> getApartments() {
        return apartments;
    }

    public void init() {
        setId(UUID.randomUUID().toString());

        this.apartments = new ArrayList<Appartment>();
        for (int i = 0;
             i < apartments_count;
             i++) {
            Appartment apto = new Appartment(getId() + "_##_" + i, i);
            apto.init();
            apartments.add(apto);
        }
    }
}
