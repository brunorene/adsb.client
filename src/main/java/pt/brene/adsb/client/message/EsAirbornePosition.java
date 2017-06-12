package pt.brene.adsb.client.message;

import java.io.Serializable;

public class EsAirbornePosition extends AdsbMessage implements Serializable {

    public String getAltitude() {
        return altitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }
}
