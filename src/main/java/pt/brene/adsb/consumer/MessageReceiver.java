package pt.brene.adsb.consumer;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import pt.brene.adsb.domain.FlightEntry;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Seq;
import pt.brene.adsb.client.message.EsAirbornePosition;
import pt.brene.adsb.client.message.EsAirborneVelocity;
import pt.brene.adsb.client.message.EsIdentificationAndCategory;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
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

    private final EventBus bus;

    public MessageReceiver(EventBus bus) {
        this.bus = bus;
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
        if (identifiers.containsKey(hexId)
                && positions.containsKey(hexId)
                && speeds.containsKey(hexId)
                && StringUtils.isNoneBlank(identifiers.get(hexId).first().getCallSign()
                , positions.get(hexId).first().getLatitude()
                , positions.get(hexId).first().getLongitude()
                , positions.get(hexId).first().getAltitude()
                , speeds.get(hexId).first().getGroundSpeed())) {
            bus.post(new FlightEntry(new Date(), identifiers.get(hexId).pollFirst().getCallSign()
                    , Double.parseDouble(positions.get(hexId).first().getLatitude())
                    , Double.parseDouble(positions.get(hexId).first().getLongitude())
                    , Double.parseDouble(positions.get(hexId).pollFirst().getAltitude())
                    , Double.parseDouble(speeds.get(hexId).pollFirst().getGroundSpeed())));
            if (identifiers.get(hexId).isEmpty()) {
                identifiers.remove(hexId);
            }
            if (positions.get(hexId).isEmpty()) {
                positions.remove(hexId);
            }
            if (speeds.get(hexId).isEmpty()) {
                speeds.remove(hexId);
            }
        }
    }

}
