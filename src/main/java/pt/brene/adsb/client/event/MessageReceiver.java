package pt.brene.adsb.client.event;

import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import pt.brene.adsb.client.message.EsAirbornePosition;
import pt.brene.adsb.client.message.EsAirborneVelocity;
import pt.brene.adsb.client.message.EsIdentificationAndCategory;

import java.util.Optional;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@SuppressWarnings("unchecked")
public class MessageReceiver {

    private final TreeMap<String, TreeSet<EsAirbornePosition>> positions = new TreeMap<>();
    private final TreeMap<String, TreeSet<EsAirborneVelocity>> speeds = new TreeMap<>();
    private final TreeMap<String, TreeSet<EsIdentificationAndCategory>> identifiers = new TreeMap<>();

    public MessageReceiver() {
        Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(() -> {
        }, 1, 1, TimeUnit.MINUTES);
    }

    @Subscribe
    public void receive(EsAirbornePosition position) {
        int before = positions.getOrDefault(position.getHexId(), new TreeSet<>()).size();
        positions.computeIfAbsent(position.getHexId(), k -> new TreeSet<>()).add(position);
        if (positions.get(position.getHexId()).size() > before) {
            logFlight(position.getHexId());
        }
    }

    @Subscribe
    public void receive(EsAirborneVelocity speed) {
        int before = speeds.getOrDefault(speed.getHexId(), new TreeSet<>()).size();
        speeds.computeIfAbsent(speed.getHexId(), k -> new TreeSet<>()).add(speed);
        if (speeds.get(speed.getHexId()).size() > before) {
            logFlight(speed.getHexId());
        }
    }

    @Subscribe
    public void receive(EsIdentificationAndCategory identifier) {
        int before = identifiers.getOrDefault(identifier.getHexId(), new TreeSet<>()).size();
        identifiers.computeIfAbsent(identifier.getHexId(), k -> new TreeSet<>()).add(identifier);
        if (identifiers.get(identifier.getHexId()).size() > before) {
            logFlight(identifier.getHexId());
        }
    }

    private void logFlight(String hexId) {
        log.info("id: {} coord: {},{} alt: {} speed: {}",
                Optional.ofNullable(identifiers.get(hexId)).map(item -> item.first().getCallSign()).orElse("-"),
                Optional.ofNullable(positions.get(hexId)).map(item -> item.first().getLatitude()).orElse("-"),
                Optional.ofNullable(positions.get(hexId)).map(item -> item.first().getLongitude()).orElse("-"),
                Optional.ofNullable(positions.get(hexId)).map(item -> item.first().getAltitude()).orElse("-"),
                Optional.ofNullable(speeds.get(hexId)).map(item -> item.first().getGroundSpeed()).orElse("-"));
    }

}
