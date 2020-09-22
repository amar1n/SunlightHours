package com.amarin.sunlighthours.create;

public interface CreateLambdaConstants {
    // According to... https://docs.aws.amazon.com/codepipeline/latest/userguide/actions-invoke-lambda-function.html
    public static final String JSON_HEADER_KEY_CONTENT_TYPE = "Content-Type";
    public static final String JSON_HEADER_VALUE_APP_JSON = "application/json";
    public static final String BEGINS_ = "BEGINS_";
    public static final String ENDS_ = "ENDS_";
    public static final int FIRST_MOMENT_WITH_SUN_IN_SECONDS = 1;
    public static final int LAST_MOMENT_WITH_SUN_IN_SECONDS = 33061;
    public static final int ZENITH_IN_SECONDS = 16531;

    public static final String DYNAMODB_TABLE_CITY_NAME = "badi-challenge-city";
    public static final String DYNAMODB_TABLE_CONFIG_NAME = "badi-challenge-config";

    public static final String RESPONSE_INPUT_VALUES_ERROR_MSG = "Invalid input data";
    public static final String RESPONSE_INPUT_VALUES_ERROR_CODE = "400";
    public static final String RESPONSE_INIT_CREATE_ERROR_MSG = "Cannot init, error creating city table";
    public static final String RESPONSE_INIT_CREATE_ERROR_CODE = "400";

    public static final String RESPONSE_SUCCESS = "success";
    public static final String RESPONSE_MESSAGE = "message";
    public static final String RESPONSE_IS_BASE_64_ENCODED = "isBase64Encoded";
    public static final String RESPONSE_STATUS_CODE = "statusCode";
    public static final String RESPONSE_HEADERS = "headers";
    public static final String RESPONSE_BODY = "body";
}
