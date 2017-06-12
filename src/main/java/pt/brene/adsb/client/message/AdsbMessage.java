package pt.brene.adsb.client.message;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Slf4j
@Setter
public abstract class AdsbMessage implements Comparable<AdsbMessage> {

    public static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS");

    protected MessageType messageType;
    protected String sessionId;
    protected String aircraftId;
    protected String hexId;
    protected String flightId;
    protected String dateGenerated;
    protected String timeGenerated;
    protected Date dateTimeGenerated;
    protected String dateLogged;
    protected String timeLogged;
    protected Date dateTimeLogged;
    protected String callSign;
    protected String altitude;
    protected String groundSpeed;
    protected String track;
    protected String latitude;
    protected String longitude;
    protected String verticalRate;
    protected String squawk;
    protected boolean squawkChangedAlert;
    protected boolean emergency;
    protected boolean spiId;
    protected boolean onGround;

    // MSG,1,,,40676B,,,,,,NLY1VC  ,,,,,,,,0,0,0,0
    // MSG,3,,,3C666A,,,,,,,1875,,,38.68789,-9.18597,,,0,0,0,

    public static AdsbMessage newMessage(String csvFormat) {
        String[] parts = StringUtils.splitPreserveAllTokens(csvFormat, ',');
        AdsbMessage message;
        MessageType messageType = MessageType.byIndex(new Integer(parts[1]));
        switch (messageType) {
            case ES_AIRBORNE_POSITION:
                message = new EsAirbornePosition();
                break;
            case ES_IDENTIFICATION_AND_CATEGORY:
                message = new EsIdentificationAndCategory();
                break;
            case ES_AIRBORNE_VELOCITY:
                message = new EsAirborneVelocity();
                break;
            default:
                return null;
        }
        message.messageType = messageType;
        message.sessionId = parts[2];
        message.aircraftId = parts[3];
        message.hexId = parts[4];
        message.flightId = parts[5];
        message.dateGenerated = parts[6];
        message.timeGenerated = parts[7];
        if (message.dateGenerated.isEmpty() || message.timeGenerated.isEmpty()) {
            message.dateTimeGenerated = Date.from(LocalDateTime.now().toInstant(ZoneOffset.UTC));
        } else {
            message.dateTimeGenerated = Date.from(Instant.from(formatter.parse(message.dateGenerated + " " + message.timeGenerated)));
        }
        message.dateLogged = parts[8];
        message.timeLogged = parts[9];
        if (message.dateLogged.isEmpty() || message.timeLogged.isEmpty()) {
            message.dateTimeLogged = Date.from(LocalDateTime.now().toInstant(ZoneOffset.UTC));
        } else {
            message.dateTimeLogged = Date.from(Instant.from(formatter.parse(message.dateLogged + " " + message.timeLogged)));
        }
        message.callSign = parts[10].trim();
        message.altitude = parts[11];
        message.groundSpeed = parts[12];
        message.track = parts[13];
        message.latitude = parts[14];
        message.longitude = parts[15];
        message.verticalRate = parts[16];
        message.squawk = parts[17];
        message.squawkChangedAlert = parts[18].equals("1");
        message.emergency = parts[19].equals("1");
        message.spiId = parts[20].equals("1");
        message.onGround = parts[21].equals("1");
        return message;
    }

    public String toString() {
        String str = getClass().getSimpleName() + "(" +
                (StringUtils.isBlank(sessionId) ? "" : ", sessionId=" + sessionId) +
                (StringUtils.isBlank(aircraftId) ? "" : ", aircraftId=" + aircraftId) +
                (StringUtils.isBlank(hexId) ? "" : ", hexId=" + hexId) +
                (StringUtils.isBlank(flightId) ? "" : ", flightId=" + flightId) +
                ", dateTimeGenerated=" + dateTimeGenerated +
                (dateTimeLogged == null || (dateTimeGenerated != null && dateTimeGenerated.equals(dateTimeLogged)) ? "" : ", dateTimeLogged=" + dateTimeLogged) +
                (StringUtils.isBlank(callSign) ? "" : ", callSign=" + callSign) +
                (StringUtils.isBlank(altitude) ? "" : ", altitude=" + altitude) +
                (StringUtils.isBlank(groundSpeed) ? "" : ", groundSpeed=" + groundSpeed) +
                (StringUtils.isBlank(track) ? "" : ", track=" + track) +
                (StringUtils.isBlank(latitude) ? "" : ", latitude=" + latitude) +
                (StringUtils.isBlank(longitude) ? "" : ", longitude=" + longitude) +
                (StringUtils.isBlank(verticalRate) ? "" : ", verticalRate=" + verticalRate) +
                (StringUtils.isBlank(squawk) ? "" : ", squawk=" + squawk) +
                (squawkChangedAlert ? ", squawkChangedAlert=" + squawkChangedAlert : "") +
                (emergency ? ", emergency=" + emergency : "") +
                (spiId ? ", spiId=" + spiId : "") +
                (onGround ? ", onGround=" + onGround : "") + ")";
        return str.replaceFirst("\\(, ", "(");
    }

    @Override
    public int compareTo(AdsbMessage adsbMessage) {
        return -ObjectUtils.compare(dateTimeGenerated, adsbMessage.dateTimeGenerated);
    }
}
