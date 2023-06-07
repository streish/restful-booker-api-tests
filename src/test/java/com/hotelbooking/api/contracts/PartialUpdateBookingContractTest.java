package com.hotelbooking.api.contracts;

import com.hotelbooking.api.BaseTest;
import com.hotelbooking.api.model.Booking;
import com.hotelbooking.api.model.CreatedBooking;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PartialUpdateBookingContractTest extends BaseTest {

    @ParameterizedTest
    @MethodSource("generateSuccessData")
    public void testPartialUpdateBooking(Booking.BookingBuilder updatedBookingBuilder) {
        // create a new booking
        CreatedBooking createdBooking = createBooking();

        // perform partial update
        Booking updatedBooking = updatedBookingBuilder.build();
        client.partialUpdateBookingJson(updatedBooking, createdBooking.getBookingid(), token)
                .then()
                .statusCode(200);

        // retrieve the updated booking by id
        Booking retrievedBooking = client.getBookingById(createdBooking.getBookingid());

        // assert that the values were updated
        assertEquals(updatedBooking.getFirstname(), retrievedBooking.getFirstname());
        assertEquals(updatedBooking.getLastname(), retrievedBooking.getLastname());
        assertEquals(updatedBooking.getTotalprice(), retrievedBooking.getTotalprice());
        assertEquals(updatedBooking.getAdditionalneeds(), retrievedBooking.getAdditionalneeds());

        // assert that the values not updated remain the same
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
                        .lastname("Santa Lucia")
                        .additionalneeds( "All Inclusive"),
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
        // Negative scenario:
        // create a new booking
        CreatedBooking createdBooking = createBooking();

        // perform partial update
        Booking.BookingDates bookingDate = bookingDateBuilder.build();
        Booking updatedBooking = new Booking();
        updatedBooking.setBookingdates(bookingDate);

        client.partialUpdateBookingJson(updatedBooking, createdBooking.getBookingid(), token)
                .then().statusCode(200);
    }

    private static Stream<Booking.BookingDates.BookingDatesBuilder> generateBookindDates() {
        DateTimeFormatter formatter = Booking.BookingDates.dateTimeFormatter;
        String today = formatter.format(LocalDate.now());

        Booking.BookingDates bookingDate = Booking.BookingDates.builder()
                .checkin(today)
                .checkout(today)
                .build();

        return Stream.of(
                // Positive testing
                bookingDate.toBuilder(), // checkIn date = checkOut date
                bookingDate.toBuilder().checkin(formatter.format(LocalDate.now().minusMonths(1))),  // checkIn date is one month ago
                bookingDate.toBuilder().checkin(formatter.format(LocalDate.now().minusYears(1))),  // checkIn date is one year ago

                bookingDate.toBuilder().checkout(formatter.format(LocalDate.now().plusMonths(1))),  // checkOut date is one month after
                bookingDate.toBuilder().checkout(formatter.format(LocalDate.now().plusYears(1))),  // checkOut date is one year after

                // Negative testing. For me, it should be rejected, but the server successfully updates it.
                bookingDate.toBuilder().checkin(""),  // empty checkIn date
                bookingDate.toBuilder().checkout(""),  // empty checkOut date
                bookingDate.toBuilder().checkin("2018-01-01T12:12:12"),  // wrong format for checkIn date
                bookingDate.toBuilder().checkout("2018-01-01T12:12:12"),  // wrong format for checkOut date
                bookingDate.toBuilder().checkout(formatter.format(LocalDate.now().minusDays(1)))  // checkOut date is early than checkIn

        );
    }



    @Test
    public void testPartialUpdateBookingResponseFormatJson() {
        // create a new booking
        CreatedBooking createdBooking = createBooking();

        // perform partial update
        Booking updatedBooking = new Booking();
        Booking.BookingDates bookingDate = Booking.BookingDates.builder()
                .checkin(Booking.BookingDates.dateTimeFormatter.format(LocalDate.now().plusMonths(1)))
                .checkout(Booking.BookingDates.dateTimeFormatter.format(LocalDate.now().plusMonths(2)))
                .build();

        updatedBooking.setFirstname("Updated");
        updatedBooking.setTotalprice(1);
        updatedBooking.setDepositpaid(false);
        updatedBooking.setBookingdates(bookingDate);

        Booking retrievedBooking = client
                .partialUpdateBookingJson(updatedBooking, createdBooking.getBookingid(), token)
                .as(Booking.class);

        // assert that the new values were returned and readable
        assertEquals(updatedBooking.getFirstname(), retrievedBooking.getFirstname());
        assertEquals(updatedBooking.getTotalprice(), retrievedBooking.getTotalprice());
        assertEquals(updatedBooking.getDepositpaid(), retrievedBooking.getDepositpaid());
        assertEquals(updatedBooking.getBookingdates(), retrievedBooking.getBookingdates());

        // assert that the values not updated remain the same
        assertEquals(createdBooking.getBooking().getAdditionalneeds(), retrievedBooking.getAdditionalneeds());
        assertEquals(createdBooking.getBooking().getLastname(), retrievedBooking.getLastname());
    }

    @Test
    public void testPartialUpdateBookingResponseFormatXml() {
        // create a new booking
        CreatedBooking createdBooking = createBooking();

        // perform partial update
        Booking updatedBooking = new Booking();
        Booking.BookingDates bookingDate = Booking.BookingDates.builder()
                .checkin(Booking.BookingDates.dateTimeFormatter.format(LocalDate.now().plusMonths(1)))
                .checkout(Booking.BookingDates.dateTimeFormatter.format(LocalDate.now().plusMonths(2)))
                .build();

        updatedBooking.setFirstname("Updated");
        updatedBooking.setTotalprice(1);
        updatedBooking.setDepositpaid(false);
        updatedBooking.setBookingdates(bookingDate);

        Booking retrievedBooking = client
                .partialUpdateBookingXml(updatedBooking, createdBooking.getBookingid(), token)
                .as(Booking.class);

        // assert that the new values were returned and readable
        assertEquals(updatedBooking.getFirstname(), retrievedBooking.getFirstname());
        assertEquals(updatedBooking.getTotalprice(), retrievedBooking.getTotalprice());
        assertEquals(updatedBooking.getDepositpaid(), retrievedBooking.getDepositpaid());
        assertEquals(updatedBooking.getBookingdates(), retrievedBooking.getBookingdates());

        // assert that the values not updated remain the same
        assertEquals(createdBooking.getBooking().getAdditionalneeds(), retrievedBooking.getAdditionalneeds());
        assertEquals(createdBooking.getBooking().getLastname(), retrievedBooking.getLastname());
    }

    @Test
    public void testPartialUpdateBookingWithInvalidBookingId() {
        // Negative scenario: Non-existent booking id
        Booking updatedBooking = new Booking();
        updatedBooking.setTotalprice(111);

        client.partialUpdateBookingJson(updatedBooking, 0, token)
                .then().statusCode(405);
    }

    @Test
    public void testPartialUpdateBookingWithoutToken() {
        // Negative scenario: Non-existent booking id
        Booking updatedBooking = new Booking();
        updatedBooking.setTotalprice(111);

        client.partialUpdateBookingJson(updatedBooking, 0, "")
                .then().statusCode(403);
    }
}
