package com.hotelbooking.api.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyLoaderUtils {

    public static String loadProperty(String propertyName) {
        try (InputStream config = PropertyLoaderUtils.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (config == null)
                throw new ExceptionInInitializerError("ERROR: Property file not found!");

            Properties appProps = new Properties();
            appProps.load(config);
            return appProps.getProperty(propertyName);
            } catch (IOException e) {
                throw new ExceptionInInitializerError("ERROR: Property file loading failed!");
            }
    }

}
