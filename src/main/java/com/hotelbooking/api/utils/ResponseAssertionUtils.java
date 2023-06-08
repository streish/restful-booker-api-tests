package com.hotelbooking.api.utils;

import io.restassured.response.Response;

import static org.hamcrest.CoreMatchers.equalTo;

public class ResponseAssertionUtils {

    public static Response assertStatusCode(Response response, int expectedStatusCode) {
        return response.then()
                .assertThat()
                .statusCode(expectedStatusCode)
                .extract()
                .response();
    }

    public static Response assertBodyEquals(Response response, String expectedBody) {
        return response.then()
                .assertThat()
                .body(equalTo(expectedBody))
                .extract()
                .response();
    }

    // Assertion methods for specific status codes

    public static Response assertSuccess(Response response) {
        assertStatusCode(response, 201);
        assertBodyEquals(response, "Created");
        return response;
    }

    public static Response assertForbidden(Response response) {
        assertStatusCode(response, 403);
        assertBodyEquals(response, "Forbidden");
        return response;
    }

    public static Response assertMethodNotAllowed(Response response) {
        assertStatusCode(response, 405);
        assertBodyEquals(response, "Method Not Allowed");
        return response;
    }
}
