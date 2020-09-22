package com.amarin.sunlighthours.create;

import com.amarin.sunlighthours.create.exception.MyException;
import com.amarin.sunlighthours.create.model.Building;
import com.amarin.sunlighthours.create.model.Neighborhood;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.amarin.sunlighthours.create.CreateLambdaConstants.*;

public class CreateLambdaHandler implements RequestStreamHandler {
    private JSONParser parser = new JSONParser();
    private AmazonDynamoDB dynamoDBClient;
    private DynamoDB dynamoDB;
    private ArrayList<Neighborhood> neighborhoodList; // Usado por init

    @Override
    public void handleRequest(InputStream is, OutputStream os, Context context) {
        try {
            processCreateSunlightHoursEvent(is, os);

            initAWSServices();

            createCity(os);

            initRangesCalculation(os);
        } catch (MyException ignored) {
            // Esto implica que he ejcutado el método answerWithErrorRequest, así que no hago nada!
        }
    }

    private void processCreateSunlightHoursEvent(InputStream is, OutputStream os) throws MyException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            JSONArray body = (JSONArray) parser.parse(reader);

            Gson gson = new Gson();
            Type neighborhoodListType = new TypeToken<ArrayList<Neighborhood>>() {
            }.getType();
            neighborhoodList = gson.fromJson(body.toJSONString(), neighborhoodListType);

            for (Neighborhood neighborhood : neighborhoodList) {
                neighborhood.init();
            }
        } catch (Exception e) {
            e.printStackTrace();
            answerWithErrorRequest(RESPONSE_INPUT_VALUES_ERROR_MSG, RESPONSE_INPUT_VALUES_ERROR_CODE, os);
        }
    }

    private void initAWSServices() {
        dynamoDBClient = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
        dynamoDB = new DynamoDB(dynamoDBClient);
    }

    private void createCity(OutputStream os) throws MyException {
        try {
            deleteCityTables();
            Table table = createCityTable();
            populateCityTable(table);
        } catch (Exception e) {
            e.printStackTrace();
            answerWithErrorRequest(RESPONSE_INIT_CREATE_ERROR_MSG, RESPONSE_INIT_CREATE_ERROR_CODE, os);
        }
    }

    private void deleteCityTables() throws Exception {
        boolean bFlag = true;
        Table cityTable = null;
        try {
            cityTable = dynamoDB.getTable(DYNAMODB_TABLE_CITY_NAME);
            cityTable.describe();
        } catch (ResourceNotFoundException rnfe) {
            bFlag = false;
            // Ocurre cuando la tabla no existe... así que no pasa nada
        }

        if (!bFlag) { return; }

        ScanRequest scanRequest = new ScanRequest().withTableName(DYNAMODB_TABLE_CITY_NAME);
        ScanResult result = dynamoDBClient.scan(scanRequest);

        List<Runnable> tasks = new ArrayList<>();

        for (Map<String, AttributeValue> item : result.getItems()) {
            tasks.add(() -> {
                try {
                    Table table = dynamoDB.getTable(BEGINS_ + item.get("id").getS());
                    table.describe();
                    table.delete();
                    table.waitForDelete();
                } catch (ResourceNotFoundException rnfe) {
                    // Ocurre cuando la tabla no existe... así que no pasa nada
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            tasks.add(() -> {
                try {
                    Table table = dynamoDB.getTable(ENDS_ + item.get("id").getS());
                    table.describe();
                    table.delete();
                    table.waitForDelete();
                } catch (ResourceNotFoundException rnfe) {
                    // Ocurre cuando la tabla no existe... así que no pasa nada
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        ExecutorService es = Executors.newCachedThreadPool();

        CompletableFuture<?>[] futures = tasks.stream().map(task -> CompletableFuture.runAsync(task, es)).toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(futures).join();
        es.shutdown();

        try {
            cityTable.delete();
            cityTable.waitForDelete();
        } catch (ResourceNotFoundException rnfe) {
            // Ocurre cuando la tabla no existe... así que no pasa nada
        }
    }

    private Table createCityTable() throws Exception {
        Table table = dynamoDB.createTable(DYNAMODB_TABLE_CITY_NAME,
                Arrays.asList(new KeySchemaElement("neighborhood", KeyType.HASH)),
                Arrays.asList(new AttributeDefinition("neighborhood", ScalarAttributeType.S)),
                new ProvisionedThroughput(5L, 5L));
        table.waitForActive();
        return table;
    }

    private void populateCityTable(Table table) {
        for (Neighborhood n : neighborhoodList) {
            List<Map<String, Object>> buildings = new ArrayList<>();
            for (Building b : n.getBuildings()) {
                Map<String, Object> edifice = new HashMap<>();
                edifice.put("id", b.getId());
                edifice.put("apartments_count", b.getApartments_count());
                edifice.put("distance", b.getDistance());
                edifice.put("name", b.getName());
                buildings.add(edifice);
            }

            Item item = new Item()
                    .withPrimaryKey("neighborhood", n.getNeighborhood())
                    .withString("id", n.getId())
                    .withNumber("apartments_height", n.getApartments_height())
                    .withList("buildings", buildings);
            table.putItem(item);
        }
    }

    private void initRangesCalculation(OutputStream os) throws MyException {
        try {
            List<Runnable> tasks = new ArrayList<>();
            for (Neighborhood neighborhood : neighborhoodList) {
                tasks.add(new Thread(new SunlightBeginningsRangeCalculator(neighborhood, dynamoDB)));
                tasks.add(new Thread(new SunlightEndingsRangeCalculator(neighborhood, dynamoDB)));
            }
            ExecutorService es = Executors.newCachedThreadPool();
            CompletableFuture<?>[] futures = tasks.stream().map(task -> CompletableFuture.runAsync(task, es)).toArray(CompletableFuture[]::new);
            CompletableFuture.allOf(futures).join();
            es.shutdown();

            Table configTable = dynamoDB.getTable(DYNAMODB_TABLE_CONFIG_NAME);
            configTable.deleteItem("action", "PROCESSING");
        } catch (Exception e) {
            e.printStackTrace();
            answerWithErrorRequest(RESPONSE_INPUT_VALUES_ERROR_MSG, RESPONSE_INPUT_VALUES_ERROR_CODE, os);
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
