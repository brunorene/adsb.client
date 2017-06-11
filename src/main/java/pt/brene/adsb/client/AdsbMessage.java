package pt.brene.adsb.client;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Data
public class AdsbMessage {

    public static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS");

    public MessageType messageType;
    public String sessionId;
    public String aircraftId;
    public String hexId;
    public String flightId;
    public String dateGenerated;
    public String timeGenerated;
    public LocalDateTime dateTimeGenerated;
    public String dateLogged;
    public String timeLogged;
    public LocalDateTime dateTimeLogged;
    public String callSign;
    public String altitude;
    public String groundSpeed;
    public String track;
    public String latitude;
    public String longitude;
    public String verticalRate;
    public String squawk;
    public boolean squawkChangedAlert;
    public boolean emergency;
    public boolean spiId;
    public boolean onGround;

    // MSG,1,,,40676B,,,,,,NLY1VC  ,,,,,,,,0,0,0,0
    // MSG,3,,,3C666A,,,,,,,1875,,,38.68789,-9.18597,,,0,0,0,


    public AdsbMessage(String csvFormat) {
        String[] parts = StringUtils.splitPreserveAllTokens(csvFormat, ',');
        messageType = MessageType.byIndex(new Integer(parts[1]));
        sessionId = parts[2];
        aircraftId = parts[3];
        hexId = parts[4];
        flightId = parts[5];
        dateGenerated = parts[6];
        timeGenerated = parts[7];
        if (dateGenerated.isEmpty() || timeGenerated.isEmpty()) {
            dateTimeGenerated = LocalDateTime.now();
        } else {
            dateTimeGenerated = LocalDateTime.from(formatter.parse(dateGenerated + " " + timeGenerated));
        }
        dateLogged = parts[8];
        timeLogged = parts[9];
        if (dateLogged.isEmpty() || timeLogged.isEmpty()) {
            dateTimeLogged = LocalDateTime.now();
        } else {
            dateTimeLogged = LocalDateTime.from(formatter.parse(dateLogged + " " + timeLogged));
        }
        callSign = parts[10];
        altitude = parts[11];
        groundSpeed = parts[12];
        track = parts[13];
        latitude = parts[14];
        longitude = parts[15];
        verticalRate = parts[16];
        squawk = parts[17];
        squawkChangedAlert = parts[18].equals("1");
        emergency = parts[19].equals("1");
        spiId = parts[20].equals("1");
        onGround = parts[21].equals("1");
        MDC.put("msgType", messageType.name());
        log.info(toString());
    }

    public String toString() {
        return "(messageType=" + getMessageType() +
                (getSessionId().isEmpty() ? "" : ", sessionId=" + getSessionId()) +
                (getAircraftId().isEmpty() ? "" : ", aircraftId=" + getAircraftId()) +
                (getHexId().isEmpty() ? "" : ", hexId=" + getHexId()) +
                (getFlightId().isEmpty() ? "" : ", flightId=" + getFlightId()) +
                ", dateTimeGenerated=" + getDateTimeGenerated() +
                ", dateTimeLogged=" + getDateTimeLogged() +
                (getCallSign().isEmpty() ? "" : ", callSign=" + getCallSign()) +
                (getAltitude().isEmpty() ? "" : ", altitude=" + getAltitude()) +
                (getGroundSpeed().isEmpty() ? "" : ", groundSpeed=" + getGroundSpeed()) +
                (getTrack().isEmpty() ? "" : ", track=" + getTrack()) +
                (getLatitude().isEmpty() ? "" : ", latitude=" + getLatitude()) +
                (getLongitude().isEmpty() ? "" : ", longitude=" + getLongitude()) +
                (getVerticalRate().isEmpty() ? "" : ", verticalRate=" + getVerticalRate()) +
                (getSquawk().isEmpty() ? "" : ", squawk=" + getSquawk()) +
                (isSquawkChangedAlert() ? ", squawkChangedAlert=" + isSquawkChangedAlert() : "") +
                (isEmergency() ? ", emergency=" + isEmergency() : "") +
                (isSpiId() ? ", spiId=" + isSpiId() : "") +
                (isOnGround() ? ", onGround=" + isOnGround() : "") + ")";
    }
}
