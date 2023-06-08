package com.hotelbooking.api.contracts;

import com.hotelbooking.api.BaseTest;
import com.hotelbooking.api.model.Booking;
import com.hotelbooking.api.model.CreatedBooking;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

import static com.hotelbooking.api.utils.DateUtils.*;
import static com.hotelbooking.api.utils.ResponseAssertionUtils.assertForbidden;
import static com.hotelbooking.api.utils.ResponseAssertionUtils.assertMethodNotAllowed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PartialUpdateBookingTest extends BaseTest {
    private static final LocalDate NOW = LocalDate.now();

    @ParameterizedTest
    @MethodSource("generateSuccessData")
    public void testPartialUpdateBooking(Booking.BookingBuilder updatedBookingBuilder) {
        // Perform partial update
        Booking updatedBooking = updatedBookingBuilder.build();
        client.partialUpdateBookingJson(updatedBooking, createdBooking.getBookingid(), token)
                .then()
                .statusCode(200);

        // Retrieve the updated booking by id
        Booking retrievedBooking = client.getBookingById(createdBooking.getBookingid());

        // Assert that the values were updated
        assertEquals(updatedBooking.getFirstname(), retrievedBooking.getFirstname());
        assertEquals(updatedBooking.getLastname(), retrievedBooking.getLastname());
        assertEquals(updatedBooking.getTotalprice(), retrievedBooking.getTotalprice());
        assertEquals(updatedBooking.getAdditionalneeds(), retrievedBooking.getAdditionalneeds());

        // Assert that the values not updated remain the same
        assertEquals(createdBooking.getBooking().getDepositpaid(), retrievedBooking.getDepositpaid());
        assertEquals(createdBooking.getBooking().getBookingdates(), retrievedBooking.getBookingdates());
    }

    private static Stream<Booking.BookingBuilder> generateSuccessData() {
        Booking booking = Booking.builder()
                .firstname("John")
                .lastname("Smith")
                .totalprice(100)
                .additionalneeds("Updated")
                .build();

        return Stream.of(
                // Positive testing
                booking.toBuilder(),  // average case
                booking.toBuilder().totalprice(99999),  // average case with big price
                booking.toBuilder().additionalneeds(""),  // average case with empty additional needs

                // Negative testing. For me, it should be rejected, but the server successfully updates it.
                booking.toBuilder().firstname(""),  // first name empty
                booking.toBuilder().lastname(""),  // last name empty
                booking.toBuilder().totalprice(0),  // price is 0
                booking.toBuilder().totalprice(-100),  // price is negative

                // Boundary testing
                booking.toBuilder().totalprice(Integer.MAX_VALUE),  // maximum price
                booking.toBuilder().firstname("John".repeat(200)),  // long first name
                booking.toBuilder().lastname("Smith".repeat(200)),  // long last name
                booking.toBuilder().lastname("Breakfast".repeat(200)),  // long additional needs

                // Variety of data
                booking.toBuilder() // strings: spaces
                        .firstname("Anna Maria")
                        .lastname(" Santa Lucia")
                        .additionalneeds( "    All Inclusive    "),
                booking.toBuilder() // strings:  special characters
                        .firstname("Konstant's")
                        .lastname("Johnson&Johnson")
                        .additionalneeds( "bed&breakfast"),
                booking.toBuilder()  // strings: Unified Ideographs
                        .firstname("有錢")
                        .lastname("人")
                        .additionalneeds( "包羅萬象")
                // ... there are could be a lot of cases depends on requirements
        );
    }

    @ParameterizedTest
    @MethodSource("generateBookindDates")
    public void testPartialUpdateBookingDate(Booking.BookingDates.BookingDatesBuilder bookingDateBuilder) {
        // Perform partial update
        Booking.BookingDates bookingDate = bookingDateBuilder.build();
        Booking updatedBooking = new Booking();
        updatedBooking.setBookingdates(bookingDate);

        client.partialUpdateBookingJson(updatedBooking, createdBooking.getBookingid(), token)
                .then().statusCode(200);

        // Retrieve the updated booking by id
        Booking retrievedBooking = client.getBookingById(createdBooking.getBookingid());

        // Check the values of the saved dates
        if (isValidDate(bookingDate.getCheckin()) && isValidDate(bookingDate.getCheckout()))
            assertEquals(updatedBooking.getBookingdates(), retrievedBooking.getBookingdates());
        else // Check negative scenario
            assertTrue(retrievedBooking.getBookingdates().getCheckin().equals("0NaN-aN-aN") ||
                    retrievedBooking.getBookingdates().getCheckout().equals("0NaN-aN-aN"));

    }

    private static Stream<Booking.BookingDates.BookingDatesBuilder> generateBookindDates() {
        DateTimeFormatter formatter = bookingDateFormat();

        Booking.BookingDates bookingDate = Booking.BookingDates.builder()
                .checkin(TODAY)
                .checkout(TODAY)
                .build();

        return Stream.of(
                // Positive testing
                bookingDate.toBuilder(), // checkIn date = checkOut date
                bookingDate.toBuilder().checkin(formatter.format(NOW.minusMonths(1))),  // checkIn date is one month ago
                bookingDate.toBuilder().checkin(formatter.format(NOW.minusYears(1))),  // checkIn date is one year ago

                bookingDate.toBuilder().checkout(formatter.format(NOW.plusMonths(1))),  // checkOut date is one month after
                bookingDate.toBuilder().checkout(formatter.format(NOW.plusYears(1))),  // checkOut date is one year after

                // Negative testing.
                // For me, it should be rejected, but the server successfully updates it and save 0NaN-aN-aN.
                bookingDate.toBuilder().checkin(null),  // without checkIn date
                bookingDate.toBuilder().checkout(null),  // without checkOut date
                bookingDate.toBuilder().checkin(""),  // empty checkIn date
                bookingDate.toBuilder().checkout(""),  // empty checkOut date
                bookingDate.toBuilder().checkin("2018/01/01T12:12:12"),  // wrong format for checkIn date
                bookingDate.toBuilder().checkout("2018/01/01T12:12:12"),  // wrong format for checkOut date
                bookingDate.toBuilder().checkout(YESTERDAY)  // checkOut date is early than checkIn
                // ... there are could be a lot of cases depends on requirements

        );
    }

    @Test
    public void testPartialUpdateBooking_ResponseJson() {
        // Perform partial update
        Booking updatedBooking = new Booking();
        Booking.BookingDates bookingDate = Booking.BookingDates.builder()
                .checkin(bookingDateFormat().format(NOW.plusMonths(1)))
                .checkout(bookingDateFormat().format(NOW.plusMonths(2)))
                .build();

        updatedBooking.setFirstname("Updated");
        updatedBooking.setTotalprice(1);
        updatedBooking.setDepositpaid(false);
        updatedBooking.setBookingdates(bookingDate);

        // Get a Json response and check content type
        Response response = client
                .partialUpdateBookingJson(updatedBooking, createdBooking.getBookingid(), token)
                .then()
                .assertThat()
                .contentType(ContentType.JSON)// assert expected content type;
                .statusCode(200)
                .extract().response();

        Booking retrievedBooking = response.as(Booking.class);

        // Assert that the new values were returned and readable
        assertRetriedValues(createdBooking, updatedBooking, retrievedBooking);

    }

    private void assertRetriedValues(CreatedBooking createdBooking, Booking updatedBooking, Booking retrievedBooking) {
        // Assert that the new values were returned and readable
        assertEquals(updatedBooking.getFirstname(), retrievedBooking.getFirstname());
        assertEquals(updatedBooking.getTotalprice(), retrievedBooking.getTotalprice());
        assertEquals(updatedBooking.getDepositpaid(), retrievedBooking.getDepositpaid());
        assertEquals(updatedBooking.getBookingdates(), retrievedBooking.getBookingdates());

        // Assert that the values not updated remain the same
        assertEquals(createdBooking.getBooking().getAdditionalneeds(), retrievedBooking.getAdditionalneeds());
        assertEquals(createdBooking.getBooking().getLastname(), retrievedBooking.getLastname());
    }

    @Disabled
    @Test // Test failed due to content type mismatch. Instead of XML, as it should be, there is text/html.
    public void testPartialUpdateBooking_ResponseXml() {
        // Perform partial update
        Booking updatedBooking = new Booking();
        Booking.BookingDates bookingDate = Booking.BookingDates.builder()
                .checkin(YESTERDAY)
                .checkout(TODAY)
                .build();

        updatedBooking.setFirstname("Updated");
        updatedBooking.setTotalprice(1);
        updatedBooking.setDepositpaid(false);
        updatedBooking.setBookingdates(bookingDate);

        // Get XML response and check content type
        Response response = client
                .partialUpdateBookingXml(updatedBooking, createdBooking.getBookingid(), token)
                .then()
                .assertThat()
                .contentType(ContentType.XML)// assert expected content type;
                .statusCode(200)
                .extract().response();

        Booking retrievedBooking = response.as(Booking.class);

        // assert that the new values were returned and readable
        assertRetriedValues(createdBooking, updatedBooking, retrievedBooking);
    }

    @Test
    public void testPartialUpdateBooking_InvalidBookingId() {
        // Negative scenario: Non-existent booking id
        Booking updatedBooking = new Booking();
        updatedBooking.setTotalprice(111);

        assertMethodNotAllowed(client.partialUpdateBookingJson(updatedBooking, -1, token));
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    public void testPartialUpdateBooking_EmptyToken(String token) {
        // Negative scenario: Empty token
        Booking updatedBooking = new Booking();
        updatedBooking.setTotalprice(111);

        assertForbidden(client.partialUpdateBookingJson(updatedBooking, 1, token));
    }

    @Test
    public void testPartialUpdateBooking_InvalidToken() {
        // Negative scenario: Invalid token
        Booking updatedBooking = new Booking();
        updatedBooking.setTotalprice(111);

        assertForbidden(client.partialUpdateBookingJson(updatedBooking, 1, token.substring(0,8)));
    }
}
