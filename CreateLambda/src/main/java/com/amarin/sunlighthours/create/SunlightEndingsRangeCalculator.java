package com.amarin.sunlighthours.create;

import com.amarin.sunlighthours.create.model.Appartment;
import com.amarin.sunlighthours.create.model.Building;
import com.amarin.sunlighthours.create.model.Neighborhood;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import org.decimal4j.util.DoubleRounder;

import static com.amarin.sunlighthours.create.CreateLambdaConstants.*;

public class SunlightEndingsRangeCalculator extends SunlightRangeCalculator {

    public SunlightEndingsRangeCalculator(Neighborhood neighborhood, DynamoDB dynamoDB) {
        super(neighborhood, dynamoDB);
    }

    @Override
    protected String getTableName() {
        return ENDS_ + neighborhood.getId();
    }

    @Override
    protected void populateTable(Table table) {
        for (Building building : neighborhood.getBuildings()) {
            for (Appartment apto : building.getApartments()) {
                Item item = new Item()
                        .withPrimaryKey("building_apto", apto.getId())
                        .withString("second_with_sun", String.valueOf(apto.getLastSecondWithSun()));
                table.putItem(item);
            }
        }
    }

    @Override
    protected double getSunAngleInGrades(int secondsElapsed) {
        return 180 - (sunVelocity * secondsElapsed);
    }

    @Override
    protected void adjustRanges() {
        Building[] buildingsArray = new Building[neighborhood.getBuildings().size()];
        buildingsArray = neighborhood.getBuildings().toArray(buildingsArray);

        for (int second = LAST_MOMENT_WITH_SUN_IN_SECONDS;
             second >= ZENITH_IN_SECONDS + 1;
             second--) {

            double sunAngleInGrades = getSunAngleInGrades(second);
            int buildingIndex = buildingsArray.length - 1;
            while (buildingIndex >= 0) {

                Building building = buildingsArray[buildingIndex];
                double buildingHeight = DoubleRounder.round(neighborhood.getApartments_height() * building.getApartments_count(), 2);
                double shadowLength = getShadowLength(buildingHeight, second);

                int i = 1;
                double distance = DoubleRounder.round(building.getDistance(), 2);
                boolean bFlag = shadowLength > distance;
                while (bFlag) {
                    int affectedBuildingIndex = buildingIndex - i;
                    if (affectedBuildingIndex < 0) { break; }

                    Building affectedBuilding = buildingsArray[affectedBuildingIndex];
                    double affectedBuildingHeight = DoubleRounder.round(neighborhood.getApartments_height() * affectedBuilding.getApartments_count(), 2);
                    double shadowHeightInAffectedBuilding = getShadowHeightThere(shadowLength, distance, sunAngleInGrades);

                    // Calcular las sombras de cada apartamento en affectedBuilding
                    for (int aptoIndex = 0;
                         aptoIndex < affectedBuilding.getApartments().size();
                         aptoIndex++) {
                        double aptoFloorHeight = DoubleRounder.round(neighborhood.getApartments_height() * (aptoIndex + 1), 2) - DoubleRounder.round(neighborhood.getApartments_height(), 2);

                        if (aptoFloorHeight < shadowHeightInAffectedBuilding) {
                            Appartment apto = affectedBuilding.getApartments().get(aptoIndex);
                            apto.setLastSecondWithSun(second - 1);
                        }
                    }

                    // Si la altura del edif. afectado es mayor que la sombra proyectada en él, debo salir
                    // ya que el resto de edificios no se ven afectados por dicha sombra.
                    if (affectedBuildingHeight >= shadowHeightInAffectedBuilding) { break; }

                    distance = distance + affectedBuilding.getDistance();
                    bFlag = shadowLength > distance;
                    i++;
                }

                buildingIndex = buildingIndex - i;
            }
        }
    }
}
