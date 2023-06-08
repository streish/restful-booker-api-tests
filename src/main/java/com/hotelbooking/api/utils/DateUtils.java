package com.hotelbooking.api.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateUtils {

    public static final String TODAY = bookingDateFormat().format(LocalDate.now());
    public static final String YESTERDAY = bookingDateFormat().format(LocalDate.now().minusDays(1));

    public static DateTimeFormatter bookingDateFormat(){
        return DateTimeFormatter.ofPattern("yyyy-MM-dd");
    }

    public static boolean isValidDate(String date) {
        if (date == null) return false;
        try {
            LocalDate.parse(date);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
