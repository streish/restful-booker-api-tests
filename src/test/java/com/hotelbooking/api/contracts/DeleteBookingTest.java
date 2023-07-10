package com.hotelbooking.api.contracts;

import com.hotelbooking.api.BaseTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;

import static com.hotelbooking.api.utils.ResponseAssertionUtils.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DeleteBookingTest extends BaseTest {

    @Test
    void testDeleteBooking_Success() {
        assertSuccess(client.deleteBooking(createdBooking.getBookingid(), token));
        // try to get the deleted booking by id
        assertThrows(IllegalStateException.class, () -> client.getBookingById(createdBooking.getBookingid()));
    }

    @Test
    void testDeleteBooking_NotExist() {
        // The server response code is 405, but in this case it's better to return 404.
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
