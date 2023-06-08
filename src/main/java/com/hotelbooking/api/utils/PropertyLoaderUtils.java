package com.hotelbooking.api.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyLoaderUtils {

    public static String loadProperty(String propertyName) {

        InputStream config = PropertyLoaderUtils.class.getClassLoader().getResourceAsStream("application.properties");

        if (config == null) {
            throw new ExceptionInInitializerError("ERROR: Property file for not found!");
        }
        else {
            try {
                Properties appProps = new Properties();
                appProps.load(config);
                return appProps.getProperty(propertyName);
            } catch (IOException var5) {
                throw new ExceptionInInitializerError("ERROR: Property file loading failed!");
            }
        }

    }

}
