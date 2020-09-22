package com.amarin.sunlighthours.init;

import com.amarin.sunlighthours.init.exception.MyException;
import com.amarin.sunlighthours.init.model.Neighborhood;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONTokener;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvocationType;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;

import static com.amarin.sunlighthours.init.InitLambdaConstants.*;

public class InitLambdaHandler implements RequestStreamHandler {
    JSONObject event = null;
    private LambdaClient lambdaClient;
    private JSONParser parser = new JSONParser();
    private AmazonDynamoDB dynamoDBClient;
    private DynamoDB dynamoDB;
    private ArrayList<Neighborhood> neighborhoodList;

    @Override
    public void handleRequest(InputStream is, OutputStream os, Context context) {
        try {
            processInitEvent(is, os);

            initAWSServices();

            checkForOtherInit(os);

            invokeCreateSunlightHoursLambda(os);

            answerInitRequest(os);
        } catch (MyException ignored) {
            // Esto implica que he ejcutado el método answerWithErrorRequest, así que no hago nada!
        }
    }

    private void processInitEvent(InputStream is, OutputStream os) throws MyException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            event = (JSONObject) parser.parse(reader);

            JSONArray body = (JSONArray) parser.parse((String) event.get(JSON_BODY));

            org.json.JSONObject jsonSchema = new org.json.JSONObject(
                    new JSONTokener(InitLambdaHandler.class.getResourceAsStream("/citySchema.json")));
            Schema schema = SchemaLoader.load(jsonSchema);

            org.json.JSONArray b = new org.json.JSONArray(body.toArray());
            schema.validate(b);

            Gson gson = new Gson();
            Type neighborhoodListType = new TypeToken<ArrayList<Neighborhood>>() {
            }.getType();
            neighborhoodList = gson.fromJson(body.toJSONString(), neighborhoodListType);
        } catch (Exception e) {
            e.printStackTrace();
            answerWithErrorRequest(RESPONSE_INPUT_VALUES_ERROR_MSG, RESPONSE_INPUT_VALUES_ERROR_CODE, os);
        }
    }

    private void initAWSServices() {
        dynamoDBClient = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
        dynamoDB = new DynamoDB(dynamoDBClient);
        lambdaClient = LambdaClient.builder().region(Region.US_EAST_1).build();
    }

    private void checkForOtherInit(OutputStream os) throws MyException {
        try {
            ScanRequest scanRequest = new ScanRequest().withTableName(DYNAMODB_TABLE_CONFIG_NAME);
            ScanResult result = dynamoDBClient.scan(scanRequest);
            if (result.getItems().size() > 0) {
                // Hay otro init en proceso...
                answerWithErrorRequest(RESPONSE_INIT_OTHER_ERROR_MSG, RESPONSE_INIT_OTHER_ERROR_CODE, os);
            }

            Table configTable = dynamoDB.getTable(DYNAMODB_TABLE_CONFIG_NAME);
            Item item = new Item().withPrimaryKey("action", "PROCESSING");
            configTable.putItem(item);
        } catch (Exception e) {
            e.printStackTrace();
            answerWithErrorRequest(RESPONSE_INIT_ERROR_MSG, RESPONSE_INIT_ERROR_CODE, os);
        }
    }

    private void invokeCreateSunlightHoursLambda(OutputStream os) throws MyException {
        try {
            SdkBytes payload = SdkBytes.fromUtf8String((String) event.get(JSON_BODY));
            InvokeRequest invokeRequest = InvokeRequest.builder()
                    .functionName("badi_calculateSunlightHours_Java")
                    .invocationType(InvocationType.EVENT)
                    .payload(payload)
                    .build();
            InvokeResponse invokeResponse = lambdaClient.invoke(invokeRequest);
//        System.out.println("Response: " + invokeResponse.toString());
//        System.out.println("Response Payload: " + invokeResponse.payload().asUtf8String());
        } catch (Exception e) {
            e.printStackTrace();
            answerWithErrorRequest(RESPONSE_INVOKE_LAMBDA_ERROR_MSG, RESPONSE_INVOKE_LAMBDA__ERROR_CODE, os);
        }
    }

    private void answerInitRequest(OutputStream os) throws MyException {
        JSONObject responseJson = new JSONObject();

        if (neighborhoodList.size() > 0) {
            responseJson.put(RESPONSE_IS_BASE_64_ENCODED, false);
            responseJson.put(RESPONSE_STATUS_CODE, 200);

            JSONObject responseHeaders = new JSONObject();
            responseHeaders.put(JSON_HEADER_KEY_CONTENT_TYPE, JSON_HEADER_VALUE_APP_JSON);
            responseJson.put(RESPONSE_HEADERS, responseHeaders);

            JSONObject responseBody = new JSONObject();
            responseBody.put(RESPONSE_MESSAGE, RESPONSE_OK_MSG);
            responseBody.put(RESPONSE_SUCCESS, true);
            responseJson.put(RESPONSE_BODY, responseBody.toString());

            try (OutputStreamWriter writer = new OutputStreamWriter(os, "UTF-8")) {
                writer.write(responseJson.toJSONString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
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
