package pt.brene.adsb.client;

import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.opensky.libadsb.Decoder;
import org.opensky.libadsb.Position;
import org.opensky.libadsb.PositionDecoder;
import org.opensky.libadsb.exceptions.BadFormatException;
import org.opensky.libadsb.exceptions.MissingInformationException;
import org.opensky.libadsb.exceptions.UnspecifiedFormatError;
import org.opensky.libadsb.msgs.AirbornePositionMsg;
import org.opensky.libadsb.msgs.AirspeedHeadingMsg;
import org.opensky.libadsb.msgs.ModeSReply;
import org.opensky.libadsb.msgs.SurfacePositionMsg;
import org.opensky.libadsb.tools;
import org.slf4j.MDC;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class MessageReceiver {

    private final DB db = DBMaker.fileDB("positions.db")
            .closeOnJvmShutdown()
            .transactionEnable()
            .make();
    private final Map<String, PositionDecoder> decoders = new HashMap<>();
    @SuppressWarnings("unchecked")
    private final HTreeMap<String, List<Position>> positions = (HTreeMap<String, List<Position>>) db.hashMap("positions").createOrOpen();

    @Subscribe
    public void receive(String msg) throws MissingInformationException {
        ModeSReply reply;
        try {
            reply = Decoder.genericDecoder(msg);
        } catch (BadFormatException | UnspecifiedFormatError e) {
            log.error(e.getLocalizedMessage());
            return;
        }

        String icao24 = tools.toHexString(reply.getIcao24());

        if (tools.isZero(reply.getParity()) || reply.checkParity()) {
            Set<String> toRemove = decoders.entrySet()
                    .stream()
                    .filter(f -> f.getValue().getLastUsedTime() < System.currentTimeMillis() * 1000 - 3600)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());
            toRemove.forEach(decoders::remove);
            List<Position> pastPositions;
            PositionDecoder decoder;
            Position current;
            MDC.put("msgType", reply.getType().toString());
            switch (reply.getType()) {
                case ADSB_AIRBORN_POSITION:
                    AirbornePositionMsg airpos = (AirbornePositionMsg) reply;
                    decoder = decoders.computeIfAbsent(icao24, k -> new PositionDecoder());
                    pastPositions = positions.computeIfAbsent(icao24, k -> new ArrayList<>());
                    current = pastPositions.isEmpty() ?
                            decoder.decodePosition(System.currentTimeMillis(), airpos) :
                            decoder.decodePosition(System.currentTimeMillis(),
                                    pastPositions.get(pastPositions.size() - 1), airpos);
                    if (current == null) {
                        log.info("No position yet");
                    } else {
                        pastPositions.add(current);
                        positions.put(icao24, pastPositions);
                        log.info("current Lat: {} Lon: {} Alt: {}{}",
                                current.getLatitude(), current.getLongitude(), current.getAltitude(),
                                current.isReasonable() ? " (reasonable)" : "");
                    }
                    break;
                case ADSB_SURFACE_POSITION:
                    SurfacePositionMsg surfpos = (SurfacePositionMsg) reply;
                    if (decoders.containsKey(icao24)) {
                        pastPositions = positions.computeIfAbsent(icao24, k -> new ArrayList<>());
                        decoder = decoders.get(icao24);
                        current = pastPositions.isEmpty() ?
                                decoder.decodePosition(System.currentTimeMillis(), surfpos) :
                                decoder.decodePosition(System.currentTimeMillis(), surfpos,
                                        pastPositions.get(pastPositions.size() - 1));
                        if (current == null) {
                            log.info("No position yet");
                        } else {
                            pastPositions.add(current);
                            positions.put(icao24, pastPositions);
                            log.info("current Lat: {} Lon: {} Alt: {}{}",
                                    current.getLatitude(), current.getLongitude(), current.getAltitude(),
                                    current.isReasonable() ? " (reasonable)" : "");
                        }
                    } else {
                        log.info("Surface cannot be the first position");
                    }
                    log.info("Horizontal containment radius is " + surfpos.getHorizontalContainmentRadiusLimit() + " m");
                    if (surfpos.hasValidHeading()) {
                        log.info("Heading is " + surfpos.getHeading() + " m");
                    }
                    log.info("Airplane is on the ground.");
                    break;
                case ADSB_AIRSPEED:
                    AirspeedHeadingMsg airspeed = (AirspeedHeadingMsg) reply;
                    log.info("[" + icao24 + "]: Airspeed is " +
                            (airspeed.hasAirspeedInfo() ? airspeed.getAirspeed() + " m/s" : "unknown"));
                    if (airspeed.hasHeadingInfo()) {
                        log.info("Heading is " + (airspeed.hasHeadingInfo() ? airspeed.getHeading() + "°" : "unknown"));
                    }
                    if (airspeed.hasVerticalRateInfo()) {
                        log.info("Vertical rate is " + (airspeed.hasVerticalRateInfo() ? airspeed.getVerticalRate() + " m/s" : "unknown"));
                    }
                    break;
            }
        }
    }
}
