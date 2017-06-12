package pt.brene.adsb.client.message;

public class EsAirborneVelocity extends AdsbMessage {

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
