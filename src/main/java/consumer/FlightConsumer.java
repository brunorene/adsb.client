package consumer;

import com.eaio.uuid.UUID;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import domain.FlightEntry;
import lombok.extern.slf4j.Slf4j;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.util.Date;

import static org.mapdb.Serializer.DATE;
import static org.mapdb.Serializer.ELSA;

@Slf4j
public class FlightConsumer {

    private static final DB db = DBMaker.fileDB("consumers.db")
            .transactionEnable()
            .closeOnJvmShutdown()
            .make();
    private final UUID key;
    private final BTreeMap<Date, FlightEntry> map;

    @SuppressWarnings("unchecked")
    public FlightConsumer(UUID key) {
        this.key = key;
        map = db.treeMap("flight" + key, DATE, ELSA).createOrOpen();
    }

    @Subscribe
    @AllowConcurrentEvents
    public void newFlightEntry(FlightEntry entry) {
        map.put(new Date(), entry);
        db.commit();
        log.info("new flight entry for {}: {}", key, map.lastEntry());
    }

}
