package consumer;

import com.eaio.uuid.UUID;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import domain.FlightEntry;
import lombok.extern.slf4j.Slf4j;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.util.NavigableSet;
import java.util.TreeSet;

import static org.mapdb.Serializer.ELSA;

@Slf4j
public class FlightConsumer {

    private static final DB db = DBMaker.fileDB("consumers.db")
            .transactionEnable()
            .closeOnJvmShutdown()
            .make();
    private final UUID key;
    private final NavigableSet<FlightEntry> set;

    @SuppressWarnings("unchecked")
    public FlightConsumer(UUID key) {
        this.key = key;
        set = (NavigableSet<FlightEntry>) db.treeSet("flight" + key, ELSA).counterEnable().createOrOpen();
    }

    @Subscribe
    @AllowConcurrentEvents
    public void newFlightEntry(FlightEntry entry) {
        set.add(entry);
        db.commit();
        log.info("new flight entry for {}: {}", key, set.first());
    }

}
