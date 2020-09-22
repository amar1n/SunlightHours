package com.amarin.sunlighthours.init;

public interface InitLambdaConstants {
    // According to... https://docs.aws.amazon.com/codepipeline/latest/userguide/actions-invoke-lambda-function.html
    public static final String JSON_HEADER_KEY_CONTENT_TYPE = "Content-Type";
    public static final String JSON_HEADER_VALUE_APP_JSON = "application/json";
    public static final String JSON_BODY = "body";
    public static final int FIRST_MOMENT_WITH_SUN_IN_SECONDS = 1;
    public static final int LAST_MOMENT_WITH_SUN_IN_SECONDS = 33061;

    public static final String DYNAMODB_TABLE_CONFIG_NAME = "badi-challenge-config";

    public static final String RESPONSE_OK_MSG = "Ok!";
    public static final String RESPONSE_INPUT_VALUES_ERROR_MSG = "Invalid input data";
    public static final String RESPONSE_INPUT_VALUES_ERROR_CODE = "400";
    public static final String RESPONSE_INIT_ERROR_MSG = "Cannot init";
    public static final String RESPONSE_INIT_ERROR_CODE = "400";
    public static final String RESPONSE_INIT_OTHER_ERROR_MSG = "Cannot init, other process in action";
    public static final String RESPONSE_INIT_OTHER_ERROR_CODE = "400";
    public static final String RESPONSE_INVOKE_LAMBDA_ERROR_MSG = "Error invoking lambda for calculation of the ranges";
    public static final String RESPONSE_INVOKE_LAMBDA__ERROR_CODE = "400";

    public static final String RESPONSE_SUCCESS = "success";
    public static final String RESPONSE_MESSAGE = "message";
    public static final String RESPONSE_IS_BASE_64_ENCODED = "isBase64Encoded";
    public static final String RESPONSE_STATUS_CODE = "statusCode";
    public static final String RESPONSE_HEADERS = "headers";
    public static final String RESPONSE_BODY = "body";
}
