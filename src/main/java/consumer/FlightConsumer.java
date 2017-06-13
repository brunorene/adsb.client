package consumer;

import com.eaio.uuid.UUID;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import domain.FlightEntry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class FlightConsumer {

    private final UUID key;

    @Subscribe
    @AllowConcurrentEvents
    public void newFlightEntry(FlightEntry entry) {
        log.info("new flight entry for {}: {}", key, entry);
    }

}
