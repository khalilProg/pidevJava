package tn.esprit.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {

    private static final Properties props = new Properties();

    static {
        try (InputStream in = ConfigLoader.class
                .getResourceAsStream("/config.properties")) {
            if (in != null) {
                props.load(in);
            } else {
                throw new RuntimeException("config.properties not found!");
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot load config.properties", e);
        }
    }

    public static String get(String key) {
        return props.getProperty(key);
    }
}