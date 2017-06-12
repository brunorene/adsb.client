package pt.brene.adsb.client.message;

import java.io.Serializable;
import java.util.Date;

public class EsAirbornePosition extends AdsbMessage implements Serializable {

    public String getHexId() {
        return hexId;
    }

    public Date getDateTimeGenerated() {
        return dateTimeGenerated;
    }

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
