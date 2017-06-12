package pt.brene.adsb.client.event;

import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import pt.brene.adsb.client.message.EsAirbornePosition;
import pt.brene.adsb.client.message.EsAirborneVelocity;
import pt.brene.adsb.client.message.EsIdentificationAndCategory;

import java.util.Comparator;
import java.util.Date;
import java.util.Optional;
import java.util.TreeMap;

@Slf4j
@SuppressWarnings("unchecked")
public class MessageReceiver {

    private final TreeMap<String, TreeMap<Date, EsAirbornePosition>> positions = new TreeMap<>();
    private final TreeMap<String, TreeMap<Date, EsAirborneVelocity>> speeds = new TreeMap<>();
    private final TreeMap<String, TreeMap<Date, EsIdentificationAndCategory>> identifiers = new TreeMap<>();

    @Subscribe
    public void receive(EsAirbornePosition position) {
        positions.computeIfAbsent(position.getHexId(), k -> new TreeMap<>(Comparator.reverseOrder())).put(position.getDateTimeGenerated(), position);
        logFlight(position.getHexId());
    }

    @Subscribe
    public void receive(EsAirborneVelocity speed) {
        speeds.computeIfAbsent(speed.getHexId(), k -> new TreeMap<>(Comparator.reverseOrder())).put(speed.getDateTimeGenerated(), speed);
        logFlight(speed.getHexId());
    }

    @Subscribe
    public void receive(EsIdentificationAndCategory identifier) {
        identifiers.computeIfAbsent(identifier.getHexId(), k -> new TreeMap<>(Comparator.reverseOrder())).put(identifier.getDateTimeGenerated(), identifier);
        logFlight(identifier.getHexId());
    }

    private void logFlight(String hexId) {
        log.info("id: {} coord: {},{} alt: {} speed: {}",
                Optional.ofNullable(identifiers.get(hexId).firstEntry()).map(entry -> entry.getValue().getCallSign()).orElse("-"),
                Optional.ofNullable(positions.get(hexId).firstEntry()).map(entry -> entry.getValue().getLatitude()).orElse("-"),
                Optional.ofNullable(positions.get(hexId).firstEntry()).map(entry -> entry.getValue().getLongitude()).orElse("-"),
                Optional.ofNullable(positions.get(hexId).firstEntry()).map(entry -> entry.getValue().getAltitude()).orElse("-"),
                Optional.ofNullable(speeds.get(hexId).firstEntry()).map(entry -> entry.getValue().getGroundSpeed()).orElse("-"));
    }

}
