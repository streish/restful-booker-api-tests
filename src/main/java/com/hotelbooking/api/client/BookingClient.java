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


public class BookingClient {

    private static final String BASE_URL = "https://restful-booker.herokuapp.com";

    private final RequestSpecification requestSpec;
    private String mediaType;


    public BookingClient() {
            requestSpec = new RequestSpecBuilder()
            .setBaseUri(BASE_URL)
            .log(LogDetail.URI)
            .log(LogDetail.BODY)
            .build();
    }

    public static Auth authenticateUser(Auth auth) {
        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(auth)
                .post(BASE_URL + "/auth");
        return response.as(Auth.class);
    }

    public CreatedBooking createBooking(Booking booking, String token) {
        Response response = RestAssured.given()
                .spec(requestSpec)
                .header("Authorization", token)
                .contentType(ContentType.JSON)
                .body(booking)
                .post(BASE_URL + "/booking")
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
                .get(BASE_URL + "/booking/" + id)
                .then()
                .log().body().log().status()
                .extract()
                .response();
        return response.as(Booking.class);
    }

    public Response partialUpdateBooking(Booking booking, int id, String token, String mediaType) {
        ContentType contentType = mediaType.contains("json") ? ContentType.JSON : ContentType.XML;

        return RestAssured.given()
                .spec(requestSpec)
                .header("Cookie", "token=" + token)
                .contentType(contentType)// set content type for request body
                .accept(mediaType)// set expected content type for response body
                .body(booking)
                .when()
                .patch(BASE_URL + "/booking/" + id)
                .then()
                .assertThat()
                .contentType(contentType)// assert expected content type
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
                .header("Authorization", token)
                .delete(BASE_URL + "/booking/" + id)
                .then()
                .log().body().log().status()
                .extract()
                .response();
    }

    public static void ping() {
        RestAssured.given()
                .get(BASE_URL + "/ping")
                .then()
                .assertThat().statusCode(201);
    }

}
