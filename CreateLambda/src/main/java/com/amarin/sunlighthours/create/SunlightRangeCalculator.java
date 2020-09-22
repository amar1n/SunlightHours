package com.amarin.sunlighthours.create;

import com.amarin.sunlighthours.create.model.Neighborhood;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.*;
import org.decimal4j.util.DoubleRounder;

import java.util.Arrays;

public abstract class SunlightRangeCalculator implements Runnable {
    protected Neighborhood neighborhood;
    protected DynamoDB dynamoDB;
    protected double sunVelocity = 0.005444316738249;

    public SunlightRangeCalculator(Neighborhood neighborhood, DynamoDB dynamoDB) {
        this.neighborhood = neighborhood;
        this.dynamoDB = dynamoDB;
    }

    @Override
    public void run() {
        try {
            adjustRanges();

            Table table = createTable();

            populateTable(table);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Table createTable() throws Exception {
        Table table = dynamoDB.createTable(getTableName(),
                Arrays.asList(new KeySchemaElement("building_apto", KeyType.HASH)),
                Arrays.asList(new AttributeDefinition("building_apto", ScalarAttributeType.S)),
                new ProvisionedThroughput(5L, 5L));
        table.waitForActive();
        return table;
    }

    protected abstract String getTableName();

    protected abstract void populateTable(Table table);

    protected abstract void adjustRanges();

    protected double getShadowLength(double buildingHeight, int secondsElapsed) {
        double rads = Math.toRadians(getSunAngleInGrades(secondsElapsed));
        double cotangent = 1.0 / Math.tan(rads);
        return DoubleRounder.round(buildingHeight * cotangent, 2);
    }

    protected abstract double getSunAngleInGrades(int secondsElapsed);

    protected double getShadowHeightThere(double shadowLength, double distance, double sunAngleInGrades) {
        double sunAngleInRadians = Math.toRadians(sunAngleInGrades);
        return DoubleRounder.round((shadowLength - distance) * Math.tan(sunAngleInRadians), 2);
    }
}
