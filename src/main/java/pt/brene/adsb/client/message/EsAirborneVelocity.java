package pt.brene.adsb.client.message;

import java.io.Serializable;
import java.time.LocalDateTime;

public class EsAirborneVelocity extends AdsbMessage implements Serializable {

    public String getHexId() {
        return hexId;
    }

    public LocalDateTime getDateTimeGenerated() {
        return dateTimeGenerated;
    }

    public String getGroundSpeed() {
        return groundSpeed;
    }

    public String getTrack() {
        return track;
    }

    public String getVerticalRate() {
        return verticalRate;
    }

}
