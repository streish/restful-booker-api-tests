package com.hotelbooking.api.contracts;

import com.hotelbooking.api.BaseTest;
import com.hotelbooking.api.model.Booking;
import com.hotelbooking.api.model.CreatedBooking;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.hotelbooking.api.utils.DateUtils.YESTERDAY;
import static org.junit.jupiter.api.Assertions.*;

public class GetBookingIdsTest extends BaseTest {

    private CreatedBooking createdBooking;

    @BeforeEach
    public void setupData() {
        // create a new booking
        createdBooking = createBooking();
    }

    @AfterEach
    void cleanup() {
        if (createdBooking != null) {
            client.deleteBooking(createdBooking.getBookingid(), token);
        }
    }

    @Test
    void testGetBookingIdsWithoutFilter() {
        // Positive scenario: without filters
        Response response = client.getBookingIds(new HashMap<>());
        response.then().statusCode(200);

        CreatedBooking[] bookings = response.as(CreatedBooking[].class);
        assertNotNull(bookings);
        // Additional assertions based on the expected response
        assertTrue(bookings.length > 1); // Assuming the response contains more then 1 bookings
        // Check if all booking IDs are unique
        Set<Integer> bookingIds = new HashSet<>();
        for (CreatedBooking booking : bookings) {
            assertTrue(bookingIds.add(booking.getBookingid()));
        }
    }

    @Test
    void testGetBookingIdsWithFilters() {
        // Positive scenario: with filters
        Map<String, Object> filters = new HashMap<>();
        filters.put("firstname", createdBooking.getBooking().getFirstname());
        filters.put("lastname", createdBooking.getBooking().getLastname());

        Response response = client.getBookingIds(filters);
        response.then().statusCode(200);

        CreatedBooking[] bookings = response.as(CreatedBooking[].class);
        assertNotNull(bookings);
        // Assuming the response contains only our booking
        assertEquals(1, bookings.length);
        // Check if all bookings have the expected firstname and lastname
        for (CreatedBooking booking : bookings) {
            Booking retrievedBooking = client.getBookingById(booking.getBookingid());
            assertEquals(createdBooking.getBooking().getFirstname(), retrievedBooking.getFirstname());
            assertEquals(createdBooking.getBooking().getLastname(), retrievedBooking.getLastname());
        }
    }

    @Test
    void testGetBookingIdsWithInvalidFilter() {
        // Negative scenario: with filters
        // Get all bookings without filters
        CreatedBooking[] bookingsExpedted = client
                .getBookingIds(new HashMap<>())
                .as(CreatedBooking[].class);
        int expectedSize = bookingsExpedted.length;

        // Create unknown filter
        Map<String, Object> filters = new HashMap<>();
        filters.put("unknownFilter", "someValue");

        Response response = client.getBookingIds(filters);
        response.then().statusCode(200);

        CreatedBooking[] bookings = response.as(CreatedBooking[].class);
        assertNotNull(bookings);

        // Assert that the result is more or equal to the full list from a moment ago
        assertTrue(bookings.length >= expectedSize);
        //assertEquals(0, bookings.length); // Assuming no bookings match the invalid filter, but it works vice versa
    }

    // Positive scenario for a check-in date filter.
    // In documentation filter checkin return bookings that have a checkin date greater than or equal to the set checkin date.
    // That's not true, it works only with a greater dates, not include the same date.
    @Test
    void testGetBookingIdsWitFilterByMultipleData() {
        Map<String, Object> filters = new HashMap<>();
        // Set filter = yesterday to find everything starting from today
        filters.put("checkin", YESTERDAY);

        Response response = client.getBookingIds(filters);
        response.then().statusCode(200);

        CreatedBooking[] bookings = response.as(CreatedBooking[].class);
        assertNotNull(bookings);
        // Assuming there should be at least one paid booking that we created before tests
        assertTrue(bookings.length > 0);

        // Keep track of booking IDs for uniqueness check
        Set<Integer> bookingIds = new HashSet<>();

        // Check if all bookings have correct Check In date and each ID is unique
        for (CreatedBooking booking : bookings) {
            Booking retrievedBooking = client.getBookingById(booking.getBookingid());

            LocalDate actualCkeckin = LocalDate.parse(retrievedBooking.getBookingdates().getCheckin());
            assertTrue(LocalDate.parse(YESTERDAY).compareTo(actualCkeckin) < 0);
            assertTrue(bookingIds.add(booking.getBookingid()));
        }
    }
}
