package server;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * This property class records the server's default property
 */
public class Property {
    private final Properties mainProperty = new Properties();
    private static final Logger LOGGER = Logger.getLogger(Property.class.getName());

    public Property() {
        String path = "./server.properties";
        try {
            FileInputStream file = new FileInputStream(path);
            mainProperty.load(file);
            file.close();
        } catch (IOException e) {
            LOGGER.warning(e.getMessage());
        }
    }

    /**
     * Get the server's property
     * @param property server's property field
     * @return server's property value
     */
    public String getProperty(String property) {
        return mainProperty.getProperty(property);
    }
}
