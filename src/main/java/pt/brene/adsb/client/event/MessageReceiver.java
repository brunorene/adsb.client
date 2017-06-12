package pt.brene.adsb.client.event;

import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.jooq.lambda.Seq;
import pt.brene.adsb.client.message.EsAirbornePosition;
import pt.brene.adsb.client.message.EsAirborneVelocity;
import pt.brene.adsb.client.message.EsIdentificationAndCategory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@SuppressWarnings("unchecked")
public class MessageReceiver {

    private final TreeMap<String, TreeSet<EsAirbornePosition>> positions = new TreeMap<>();
    private final TreeMap<String, TreeSet<EsAirborneVelocity>> speeds = new TreeMap<>();
    private final TreeMap<String, TreeSet<EsIdentificationAndCategory>> identifiers = new TreeMap<>();

    public MessageReceiver() {
        Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(() -> {
            LocalDateTime now = LocalDateTime.now();
            List<String> keysToRemove = Seq.concat(
                    positions.entrySet()
                            .stream()
                            .filter(entry -> now.minusMinutes(1).isAfter(entry.getValue().first().getDateTimeGenerated()))
                            .map(entry -> entry.getKey()),
                    speeds.entrySet()
                            .stream()
                            .filter(entry -> now.minusMinutes(1).isAfter(entry.getValue().first().getDateTimeGenerated()))
                            .map(entry -> entry.getKey()),
                    identifiers.entrySet()
                            .stream()
                            .filter(entry -> now.minusMinutes(1).isAfter(entry.getValue().first().getDateTimeGenerated()))
                            .map(entry -> entry.getKey()))
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                    .entrySet()
                    .stream()
                    .filter(entry -> entry.getValue() == 3)
                    .map(entry -> entry.getKey())
                    .collect(Collectors.toList());
            keysToRemove.stream()
                    .forEach(k -> {
                        positions.remove(k);
                        speeds.remove(k);
                        identifiers.remove(k);
                        log.info("removing old info from {}", k);
                    });
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
