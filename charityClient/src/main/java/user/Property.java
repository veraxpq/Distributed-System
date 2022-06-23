package user;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * This property class records the user's default property
 */
public class Property {
    private final Properties mainProperty = new Properties();
    private static final Logger LOGGER = Logger.getLogger(server.Property.class.getName());

    public Property() {
        String path = "./user.properties";
        try {
            FileInputStream file = new FileInputStream(path);
            mainProperty.load(file);
            file.close();
        } catch (IOException e) {
            LOGGER.warning(e.getMessage());
        }
    }

    /**
     * get the user's property
     * @param property user's property field
     * @return user's property value
     */
    public String getProperty(String property) {
        return mainProperty.getProperty(property);
    }
}
