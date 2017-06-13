package pt.brene.adsb.api;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by brsantos on 13-06-2017.
 */
public class AppConfig extends Properties {

    public AppConfig() throws IOException {
        load(getClass().getResourceAsStream("/config.properties"));
    }

    public Integer getInt(String key) {
        return new Integer(getProperty(key));
    }

    public String getString(String key) {
        return getProperty(key);
    }
}
