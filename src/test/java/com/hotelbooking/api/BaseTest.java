package com.hotelbooking.api;

import com.github.javafaker.Faker;
import com.hotelbooking.api.client.BookingClient;
import com.hotelbooking.api.model.Auth;
import com.hotelbooking.api.model.Booking;
import com.hotelbooking.api.model.CreatedBooking;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDate;

import static com.hotelbooking.api.utils.DateUtils.bookingDateFormat;
import static com.hotelbooking.api.utils.PropertyLoaderUtils.loadProperty;


public abstract class BaseTest {
    protected BookingClient client;
    protected String token;
    protected CreatedBooking createdBooking;

    @BeforeAll
    public static void pingClient() {
        BookingClient.ping();
    }

    @BeforeEach
    public void setupClient() {
        client = new BookingClient();
        // Authorization by admin
        String admin = loadProperty("admin.user");
        String password = loadProperty("admin.password");
        Auth session = new Auth(admin, password, null);
        token = client.authenticateUser(session).getToken();
    }

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

    public CreatedBooking createBooking() {
        Booking booking = generateBooking();

        CreatedBooking createdBooking = client.createBooking(booking, token);
        return createdBooking;
    }

    public Booking generateBooking() {
        Booking.BookingDates bookingDate = Booking.BookingDates.builder()
                .checkin(bookingDateFormat().format(LocalDate.now()))
                .checkout(bookingDateFormat().format(LocalDate.now().plusDays(1)))
                .build();

        Faker faker =  new Faker();
        return Booking.builder()
                .firstname(faker.name().firstName())
                .lastname(faker.name().lastName())
                .totalprice(faker.number().numberBetween(1, 1000))
                .depositpaid(true)
                .bookingdates(bookingDate)
                .additionalneeds("Breakfast")
                .build();
    }

}
