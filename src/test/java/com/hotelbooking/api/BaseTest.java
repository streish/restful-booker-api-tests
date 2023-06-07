package com.hotelbooking.api;

import com.github.javafaker.Faker;
import com.hotelbooking.api.client.BookingClient;
import com.hotelbooking.api.model.Auth;
import com.hotelbooking.api.model.Booking;
import com.hotelbooking.api.model.CreatedBooking;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDate;


public class BaseTest {
    protected BookingClient client;
    protected String token;

    @BeforeAll
    public static void pingClient() {
        BookingClient.ping();
    }

    @BeforeEach
    public void setup() {
        Auth admin = new Auth("admin", "password123", null);
        token = BookingClient.authenticateUser(admin).getToken();
        client = new BookingClient();
    }

    public CreatedBooking createBooking() {
        Booking.BookingDates bookingDate = Booking.BookingDates.builder()
                .checkin(Booking.BookingDates.dateTimeFormatter.format(LocalDate.now()))
                .checkout(Booking.BookingDates.dateTimeFormatter.format(LocalDate.now().plusDays(1)))
                .build();

        Faker faker =  new Faker();
        Booking booking = Booking.builder()
                .firstname(faker.name().firstName())
                .lastname(faker.name().lastName())
                .totalprice(faker.number().numberBetween(100, 500))
                .depositpaid(true)
                .bookingdates(bookingDate)
                .additionalneeds("Breakfast")
                .build();

        CreatedBooking createdBooking = client.createBooking(booking, token);
        return createdBooking;
    }
}
