package com.amarin.sunlighthours.get;

import com.amarin.sunlighthours.get.exception.MyException;
import com.amarin.sunlighthours.get.model.Building;
import com.amarin.sunlighthours.get.model.Neighborhood;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.lang.reflect.Type;

import static com.amarin.sunlighthours.get.GetLambdaConstants.*;

public class GetLambdaHandler implements RequestStreamHandler {
    JSONObject event = null;
    Item beginning, ending = null;
    private JSONParser parser = new JSONParser();
    private AmazonDynamoDB dynamoDBClient;
    private DynamoDB dynamoDB;
    private String neighborhoodId, aptoId;
    private String neighborhoodName, buildingName, apartmentNumber;

    @Override
    public void handleRequest(InputStream is, OutputStream os, Context context) {
        try {
            initAWSServices();

            processGetSunlightHoursEvent(is, os);

            checkForProcessing(os);

            getNeighborhood(os);

            getSunlightHours(os);

            answerGetSunlightHoursRequest(os);
        } catch (MyException ignored) {
            // Esto implica que he ejcutado el método answerWithErrorRequest, así que no hago nada!
        }
    }

    private void initAWSServices() {
        dynamoDBClient = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
        dynamoDB = new DynamoDB(dynamoDBClient);
    }

    private void processGetSunlightHoursEvent(InputStream is, OutputStream os) throws MyException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            event = (JSONObject) parser.parse(reader);
            JSONObject qsp = (JSONObject) event.get("queryStringParameters");
            neighborhoodName = (String) qsp.get("neighborhood_name");
            buildingName = (String) qsp.get("building_name");
            apartmentNumber = (String) qsp.get("apartment_number");

            if (neighborhoodName == null || buildingName == null || apartmentNumber == null) {
                answerWithErrorRequest(RESPONSE_INPUT_VALUES_ERROR_MSG, RESPONSE_INPUT_VALUES_ERROR_CODE, os);
            }
        } catch (Exception e) {
            e.printStackTrace();
            answerWithErrorRequest(RESPONSE_INPUT_VALUES_ERROR_MSG, RESPONSE_INPUT_VALUES_ERROR_CODE, os);
        }
    }

    private void checkForProcessing(OutputStream os) throws MyException {
        try {
            ScanRequest scanRequest = new ScanRequest().withTableName(DYNAMODB_TABLE_CONFIG_NAME);
            ScanResult result = dynamoDBClient.scan(scanRequest);
            if (result.getItems().size() > 0) {
                // Aún se están calculado los datos...
                answerWithErrorRequest(RESPONSE_PROCESSING_ERROR_MSG, RESPONSE_PROCESSING_ERROR_CODE, os);
            }
        } catch (Exception e) {
            e.printStackTrace();
            answerWithErrorRequest(RESPONSE_ERROR_MSG, RESPONSE_ERROR_CODE, os);
        }
    }

    private void getNeighborhood(OutputStream os) throws MyException {
        Table cityTable = getTable(DYNAMODB_TABLE_CITY_NAME);
        if (cityTable == null) {
            answerWithErrorRequest(RESPONSE_ERROR_MSG, RESPONSE_ERROR_CODE, os);
        }

        Item neighborhoodItem = cityTable.getItem("neighborhood", neighborhoodName);
        if (neighborhoodItem == null) {
            answerWithErrorRequest(RESPONSE_INPUT_WRONG_NEIGHBORHOOD_ERROR_MSG, RESPONSE_INPUT_WRONG_NEIGHBORHOOD_ERROR_CODE, os);
        }

        Gson gson = new Gson();
        Type neighborhoodType = new TypeToken<Neighborhood>() {
        }.getType();
        Neighborhood neighborhood = gson.fromJson(neighborhoodItem.toJSON(), neighborhoodType);
        if (neighborhood == null) {
            answerWithErrorRequest(RESPONSE_INPUT_WRONG_NEIGHBORHOOD_ERROR_MSG, RESPONSE_INPUT_WRONG_NEIGHBORHOOD_ERROR_CODE, os);
        }

        Building building = neighborhood.getBuildings().stream()
                .filter(b -> buildingName.equals(b.getName()))
                .findAny()
                .orElse(null);
        if (building == null) {
            answerWithErrorRequest(RESPONSE_INPUT_WRONG_BUILDING_ERROR_MSG, RESPONSE_INPUT_WRONG_BUILDING_ERROR_CODE, os);
        }

        int aptoNum = Integer.parseInt(apartmentNumber);
        if (aptoNum >= building.getApartments_count()) {
            answerWithErrorRequest(RESPONSE_INPUT_WRONG_APARTMENT_ERROR_MSG, RESPONSE_INPUT_WRONG_APARTMENT_ERROR_CODE, os);
        }

        neighborhoodId = neighborhood.getId();
        aptoId = building.getId() + "_##_" + aptoNum;
    }

    private void getSunlightHours(OutputStream os) throws MyException {
        beginning = null;
        ending = null;

        Table table_BEGINNINGS = getTable(BEGINS_ + neighborhoodId);
        if (table_BEGINNINGS != null) {
            Table table_ENDINGS = getTable(ENDS_ + neighborhoodId);
            if (table_ENDINGS != null) {
                beginning = table_BEGINNINGS.getItem("building_apto", aptoId);
                ending = table_ENDINGS.getItem("building_apto", aptoId);
            } else {
                answerWithErrorRequest(RESPONSE_NEIGHBORHOOD_NO_FOUND_ERROR_MSG, RESPONSE_NEIGHBORHOOD_NO_FOUND_ERROR_CODE, os);
            }
        } else {
            answerWithErrorRequest(RESPONSE_NEIGHBORHOOD_NO_FOUND_ERROR_MSG, RESPONSE_NEIGHBORHOOD_NO_FOUND_ERROR_CODE, os);
        }

        if (beginning == null || ending == null) {
            answerWithErrorRequest(RESPONSE_APTO_NO_FOUND_ERROR_MSG, RESPONSE_APTO_NO_FOUND_ERROR_CODE, os);
        }
    }

    private Table getTable(String tableName) {
        try {
            Table table = dynamoDB.getTable(tableName);
            table.describe();
            return table;
        } catch (ResourceNotFoundException rnfe) {
            // Ocurre cuando la tabla no existe... así que no pasa nada
        }
        return null;
    }

    private void answerGetSunlightHoursRequest(OutputStream os) {
        JSONObject responseJson = new JSONObject();

        responseJson.put(RESPONSE_IS_BASE_64_ENCODED, false);
        responseJson.put(RESPONSE_STATUS_CODE, 200);

        JSONObject responseHeaders = new JSONObject();
        responseHeaders.put(JSON_HEADER_KEY_CONTENT_TYPE, JSON_HEADER_VALUE_APP_JSON);
        responseJson.put(RESPONSE_HEADERS, responseHeaders);

        JSONObject responseBody = new JSONObject();

        DateTime base = new DateTime(2020, 12, 22, 8, 13, 59);
        DateTime start = base.plusSeconds(Integer.parseInt(beginning.get("second_with_sun").toString()));
        DateTime end = base.plusSeconds(Integer.parseInt(ending.get("second_with_sun").toString()));
        DateTimeFormatter dtfOut = DateTimeFormat.forPattern("HH:mm:ss");
        responseBody.put(RESPONSE_MESSAGE, dtfOut.print(start) + " - " + dtfOut.print(end));
        responseBody.put(RESPONSE_SUCCESS, true);
        responseJson.put(RESPONSE_BODY, responseBody.toString());

        try (OutputStreamWriter writer = new OutputStreamWriter(os, "UTF-8")) {
            writer.write(responseJson.toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void answerWithErrorRequest(String error, String code, OutputStream os) throws MyException {
        JSONObject responseJson = new JSONObject();

        responseJson.put(RESPONSE_IS_BASE_64_ENCODED, false);
        responseJson.put(RESPONSE_STATUS_CODE, code);

        JSONObject responseHeaders = new JSONObject();
        responseHeaders.put(JSON_HEADER_KEY_CONTENT_TYPE, JSON_HEADER_VALUE_APP_JSON);
        responseJson.put(RESPONSE_HEADERS, responseHeaders);

        JSONObject responseBody = new JSONObject();
        responseBody.put(RESPONSE_MESSAGE, error);
        responseBody.put(RESPONSE_SUCCESS, false);
        responseJson.put(RESPONSE_BODY, responseBody.toString());

        try (OutputStreamWriter writer = new OutputStreamWriter(os, "UTF-8")) {
            writer.write(responseJson.toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        throw new MyException("");
    }
}
