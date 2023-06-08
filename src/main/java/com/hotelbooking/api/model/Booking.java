package com.hotelbooking.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlRootElement;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlRootElement(name = "booking")
public class Booking {
    // First name for the guest who made the booking
    private String firstname;

    // Last name for the guest who made the booking
    private String lastname;

    // The total price for the booking
    private Integer totalprice;

    // Whether the deposit has been paid or not
    private Boolean depositpaid;

    // Date the guest reserved
    private BookingDates bookingdates;

    // Any other needs the guest has
    private String additionalneeds;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class BookingDates{
        // Date the guest is checking in
        private String checkin;

        // Date the guest is checking out
        private String checkout;
    }


}