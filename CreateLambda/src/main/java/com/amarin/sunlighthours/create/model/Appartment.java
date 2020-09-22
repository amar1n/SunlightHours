package com.amarin.sunlighthours.create.model;

import static com.amarin.sunlighthours.create.CreateLambdaConstants.FIRST_MOMENT_WITH_SUN_IN_SECONDS;
import static com.amarin.sunlighthours.create.CreateLambdaConstants.LAST_MOMENT_WITH_SUN_IN_SECONDS;

public class Appartment {
    private String id;
    private int nro;
    private int firstSecondWithSun;
    private int lastSecondWithSun;

    public Appartment(String id, int nro) {
        this.id = id;
        this.nro = nro;
    }

    public String getId() {
        return id;
    }

    public int getNro() {
        return nro;
    }

    public int getFirstSecondWithSun() {
        return firstSecondWithSun;
    }

    public void setFirstSecondWithSun(int firstSecondWithSun) {
        this.firstSecondWithSun = firstSecondWithSun;
    }

    public int getLastSecondWithSun() {
        return lastSecondWithSun;
    }

    public void setLastSecondWithSun(int lastSecondWithSun) {
        this.lastSecondWithSun = lastSecondWithSun;
    }

    public void init() {
        setFirstSecondWithSun(FIRST_MOMENT_WITH_SUN_IN_SECONDS);
        setLastSecondWithSun(LAST_MOMENT_WITH_SUN_IN_SECONDS);
    }
}
