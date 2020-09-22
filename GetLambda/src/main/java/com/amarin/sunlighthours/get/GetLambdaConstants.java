package com.amarin.sunlighthours.get;

public interface GetLambdaConstants {
    // According to... https://docs.aws.amazon.com/codepipeline/latest/userguide/actions-invoke-lambda-function.html
    public static final String JSON_HEADER_KEY_CONTENT_TYPE = "Content-Type";
    public static final String JSON_HEADER_VALUE_APP_JSON = "application/json";
    public static final String BEGINS_ = "BEGINS_";
    public static final String ENDS_ = "ENDS_";
    public static final int FIRST_MOMENT_WITH_SUN_IN_SECONDS = 1;
    public static final int LAST_MOMENT_WITH_SUN_IN_SECONDS = 33061;

    public static final String DYNAMODB_TABLE_CITY_NAME = "badi-challenge-city";
    public static final String DYNAMODB_TABLE_CONFIG_NAME = "badi-challenge-config";

    public static final String RESPONSE_INPUT_VALUES_ERROR_MSG = "Invalid input data";
    public static final String RESPONSE_INPUT_VALUES_ERROR_CODE = "400";
    public static final String RESPONSE_ERROR_MSG = "Cannot get the data";
    public static final String RESPONSE_ERROR_CODE = "400";
    public static final String RESPONSE_APTO_NO_FOUND_ERROR_MSG = "No data found for that apartment";
    public static final String RESPONSE_APTO_NO_FOUND_ERROR_CODE = "400";
    public static final String RESPONSE_NEIGHBORHOOD_NO_FOUND_ERROR_MSG = "No data found for that neighborhood";
    public static final String RESPONSE_NEIGHBORHOOD_NO_FOUND_ERROR_CODE = "400";
    public static final String RESPONSE_PROCESSING_ERROR_MSG = "The data is still being processed";
    public static final String RESPONSE_PROCESSING_ERROR_CODE = "400";

    public static final String RESPONSE_INPUT_WRONG_NEIGHBORHOOD_ERROR_MSG = "Invalid neighborhood";
    public static final String RESPONSE_INPUT_WRONG_NEIGHBORHOOD_ERROR_CODE = "400";
    public static final String RESPONSE_INPUT_WRONG_BUILDING_ERROR_MSG = "Invalid building";
    public static final String RESPONSE_INPUT_WRONG_BUILDING_ERROR_CODE = "400";
    public static final String RESPONSE_INPUT_WRONG_APARTMENT_ERROR_MSG = "Invalid apartment";
    public static final String RESPONSE_INPUT_WRONG_APARTMENT_ERROR_CODE = "400";

    public static final String RESPONSE_SUCCESS = "success";
    public static final String RESPONSE_MESSAGE = "message";
    public static final String RESPONSE_IS_BASE_64_ENCODED = "isBase64Encoded";
    public static final String RESPONSE_STATUS_CODE = "statusCode";
    public static final String RESPONSE_HEADERS = "headers";
    public static final String RESPONSE_BODY = "body";
}
