package pt.brene.adsb.consumer;

import com.eaio.uuid.UUID;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import pt.brene.adsb.domain.FlightEntry;
import lombok.extern.slf4j.Slf4j;

import java.util.NavigableSet;

import static org.mapdb.Serializer.ELSA;
import static pt.brene.adsb.client.AdsbClient.CONSUMERS_DB;

@Slf4j
public class FlightConsumer {

    public static final String FLIGHT = "flight";

    private final UUID key;
    private final NavigableSet<FlightEntry> set;

    @SuppressWarnings("unchecked")
    public FlightConsumer(UUID key) {
        this.key = key;
        set = (NavigableSet<FlightEntry>) CONSUMERS_DB.treeSet(FLIGHT + key, ELSA).counterEnable().createOrOpen();
    }

    @Subscribe
    @AllowConcurrentEvents
    public void newFlightEntry(FlightEntry entry) {
        set.add(entry);
        CONSUMERS_DB.commit();
    }

}
