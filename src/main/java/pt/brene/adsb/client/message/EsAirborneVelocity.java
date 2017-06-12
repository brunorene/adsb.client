package pt.brene.adsb.client.message;

import java.util.Date;

public class EsAirborneVelocity extends AdsbMessage {

    public String getHexId() {
        return hexId;
    }

    public Date getDateTimeGenerated() {
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
