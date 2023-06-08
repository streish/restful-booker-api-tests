package com.hotelbooking.api.contracts;

import com.hotelbooking.api.BaseTest;
import com.hotelbooking.api.model.CreatedBooking;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;

import static com.hotelbooking.api.utils.ResponseAssertionUtils.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DeleteBookingTest extends BaseTest {

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
    void testDeleteBooking_Success() {
        assertSuccess(client.deleteBooking(createdBooking.getBookingid(), token));
        // try to get the deleted booking by id
        assertThrows(IllegalStateException.class, () -> client.getBookingById(createdBooking.getBookingid()));
    }

    @Test
    void testDeleteBooking_NotExist() {
        assertMethodNotAllowed(client.deleteBooking(-1, token));
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    void testDeleteBooking_NoToken(String token) {
        assertForbidden(client.deleteBooking(createdBooking.getBookingid(), token));
    }

    @Test
    void testDeleteBooking_InvalidToken() {
        assertForbidden(client.deleteBooking(createdBooking.getBookingid(),  token.substring(0,8)));
    }
}
