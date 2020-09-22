package com.amarin.sunlighthours.init;

import com.amarin.sunlighthours.init.model.Neighborhood;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class InitLambdaHandlerTest {

    @Test
    public void givenValidCity_whenValidating_thenValid() throws ValidationException {
        JSONObject jsonSchema = new JSONObject(
                new JSONTokener(InitLambdaHandlerTest.class.getResourceAsStream("/citySchema.json")));
        Schema schema = SchemaLoader.load(jsonSchema);

        JSONArray jsonArray = new JSONArray(
                new JSONTokener(InitLambdaHandlerTest.class.getResourceAsStream("/city_valid.json")));
        schema.validate(jsonArray);
    }

    @Test(expected = ValidationException.class)
    public void givenInvalidCity_whenValidating_thenInvalid() throws ValidationException {
        JSONObject jsonSchema = new JSONObject(
                new JSONTokener(InitLambdaHandlerTest.class.getResourceAsStream("/citySchema.json")));
        Schema schema = SchemaLoader.load(jsonSchema);

        JSONArray jsonArray = new JSONArray(
                new JSONTokener(InitLambdaHandlerTest.class.getResourceAsStream("/city_invalid.json")));
        schema.validate(jsonArray);
    }

    @Test
    public void givenValidNeighborhood_whenValidating_thenValid() {
        JSONObject neighborhoodJson = new JSONObject(
                new JSONTokener(InitLambdaHandlerTest.class.getResourceAsStream("/neighborhood_valid.json")));

        Gson gson = new Gson();
        Neighborhood neighborhood = gson.fromJson(neighborhoodJson.toString(), Neighborhood.class);
        assertNotNull(neighborhood);
    }

    @Test(expected = JsonSyntaxException.class)
    public void givenInvalidNeighborhood_whenValidating_thenInvalid() {
        JSONObject neighborhoodJson = new JSONObject(
                new JSONTokener(InitLambdaHandlerTest.class.getResourceAsStream("/neighborhood_invalid.json")));

        Gson gson = new Gson();
        gson.fromJson(neighborhoodJson.toString(), Neighborhood.class);
    }
}
