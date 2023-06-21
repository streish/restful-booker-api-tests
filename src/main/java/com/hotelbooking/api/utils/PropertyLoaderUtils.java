package com.hotelbooking.api.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyLoaderUtils {

    public static String loadProperty(String propertyName) {

        InputStream config = PropertyLoaderUtils.class.getClassLoader().getResourceAsStream("application.properties");
        if (config == null) {
            throw new ExceptionInInitializerError("Property file for not found!");
        }
        else {
            Properties appProps = new Properties();
            try {
                appProps.load(config);
                return appProps.getProperty(propertyName);
            } catch (IOException var5) {
                throw new ExceptionInInitializerError(new IOException("ERROR: Property file loading failed!"));
            }
        }

    }

}
