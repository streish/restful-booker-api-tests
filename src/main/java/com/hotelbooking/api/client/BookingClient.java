package com.hotelbooking.api.client;

import com.hotelbooking.api.model.Auth;
import com.hotelbooking.api.model.Booking;
import com.hotelbooking.api.model.CreatedBooking;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

import static com.hotelbooking.api.utils.PropertyLoaderUtils.loadProperty;
import static org.hamcrest.Matchers.notNullValue;


public class BookingClient {

    private static String BASE_URL;

    private final RequestSpecification requestSpec;


    public BookingClient() {
        loadBaseUrl();

        requestSpec = new RequestSpecBuilder()
                .setBaseUri(BASE_URL)
                .log(LogDetail.URI)
                .log(LogDetail.METHOD)
                .log(LogDetail.BODY)
                .build();
    }

    private static void loadBaseUrl() {
        // read base URL from resources
        BASE_URL = loadProperty("booker.host");
    }

    public Auth authenticateUser(Auth auth) {
        Response response = RestAssured.given()
                .spec(requestSpec)
                .contentType(ContentType.JSON)
                .body(auth)
                .post("/auth")
                .then()
                .assertThat()
                .body("token", notNullValue())
                .extract()
                .response();
        return response.as(Auth.class);
    }

    public CreatedBooking createBooking(Booking booking, String token) {
        Response response = RestAssured.given()
                .spec(requestSpec)
                .header("Authorization", token)
                .contentType(ContentType.JSON)
                .body(booking)
                .post("/booking")
                .then()
                .log().body().log().status()
                .extract()
                .response();
        return response.as(CreatedBooking.class);
    }

    public Booking getBookingById(int id) {
        Response response = RestAssured.given()
                .spec(requestSpec)
                .accept("application/json")
                .get("/booking/" + id)
                .then()
                .log().body().log().status()
                .extract()
                .response();
        return response.as(Booking.class);
    }

    public Response getBookingIds(Map<String, Object> filters) {
        RequestSpecification request = RestAssured.given()
                .spec(requestSpec)
                .accept("application/json");
        // collect filters and their values
        for (Map.Entry<String, Object> entry : filters.entrySet()) {
            request.queryParam(entry.getKey(), entry.getValue());
        }
        Response response = request.get("/booking")
                .then()
                //.log().body() // too many logs so probably need it only for debug
                .log().status()
                .extract()
                .response();
        // here we can also measure a time for the response, such as response.time() for testing purposes.
        return response;
    }

    private Response partialUpdateBooking(Booking booking, int id, String token, String mediaType) {
        ContentType contentType = mediaType.contains("json") ? ContentType.JSON : ContentType.XML;

        return RestAssured.given()
                .spec(requestSpec)
                .header("Cookie", "token=" + token)
                .contentType(contentType)// set content type for request body
                .accept(mediaType)// set expected content type for response body
                .body(booking)
                .when()
                .patch("/booking/" + id)
                .then()
                .log().all()
                .extract()
                .response();
    }

    public Response partialUpdateBookingJson(Booking booking, int id, String token){
        return partialUpdateBooking(booking, id, token, "application/json");
    }

    public Response partialUpdateBookingXml(Booking booking, int id, String token){
        return partialUpdateBooking(booking, id, token, "application/xml");
    }

    public Response deleteBooking(int id, String token) {
        return RestAssured.given()
                .spec(requestSpec)
                .header("Cookie", "token=" + token)
                .delete("/booking/" + id)
                .then()
                .log().body().log().status()
                .extract()
                .response();
    }

    public static void ping() {
        if (BASE_URL == null) loadBaseUrl();

        RestAssured.given()
                .get(BASE_URL + "/ping")
                .then()
                .assertThat().statusCode(201);
    }

}
