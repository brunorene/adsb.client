package pt.brene.adsb.client;

import com.eaio.uuid.UUID;
import com.google.common.base.Charsets;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import pt.brene.adsb.consumer.FlightConsumer;
import pt.brene.adsb.consumer.MessageReceiver;
import pt.brene.adsb.domain.FlightEntry;
import lombok.extern.slf4j.Slf4j;
import org.jooq.lambda.Unchecked;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import pt.brene.adsb.client.message.AdsbMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static pt.brene.adsb.consumer.FlightConsumer.FLIGHT;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.mapdb.Serializer.ELSA;

@Slf4j
public class AdsbClient {

    public static final DB CONSUMERS_DB = DBMaker.fileDB("consumers.db")
            .transactionEnable()
            .closeOnJvmShutdown()
            .make();

    private final String host;
    private final Integer port;
    private final EventBus bus = new AsyncEventBus(Executors.newWorkStealingPool(4));

    public AdsbClient(String host, Integer port) {
        this.host = host;
        this.port = port;
        bus.register(new MessageReceiver(bus));
    }

    public void readFromSdr() throws IOException {
        new Thread(Unchecked.runnable(() -> {
            try (Socket socket = new Socket(host, port);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charsets.UTF_8))) {
                reader.lines()
                        .map(line -> AdsbMessage.newMessage(line))
                        .filter(msg -> msg != null)
                        .forEach(bus::post);
            }
        })).start();
    }

    public UUID getKey() {
        UUID uuid = new UUID();
        bus.register(new FlightConsumer(uuid));
        return uuid;
    }

    public List<UUID> getClients() {
        return StreamSupport.stream(CONSUMERS_DB.getAllNames().spliterator(), false)
                .filter(name -> name.startsWith(FLIGHT))
                .map(name -> new UUID(name.replace(FLIGHT, "")))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public Set<FlightEntry> pollState(UUID id) {
        NavigableSet<FlightEntry> set = (NavigableSet<FlightEntry>) CONSUMERS_DB.treeSet(FLIGHT + id, ELSA).counterEnable().createOrOpen();
        Set<FlightEntry> current = new TreeSet<>();
        FlightEntry entry;
        while ((entry = set.pollFirst()) != null) {
            current.add(entry);
        }
        CONSUMERS_DB.commit();
        return current;
    }

    // Used for testing
    public static void main(String[] args) {
        AdsbClient adsbClient = new AdsbClient("localhost", 30003);
        try {
            List<UUID> clients = Arrays.asList(adsbClient.getKey(), adsbClient.getKey(), adsbClient.getKey());
            Random rand = new Random();
            Executors.newScheduledThreadPool(3)
                    .scheduleAtFixedRate(() -> {
                        UUID currentClient = clients.get(rand.nextInt(clients.size()));
                        Set<FlightEntry> set = adsbClient.pollState(currentClient);
                        String entries = set.stream()
                                .map(entry -> entry.toString())
                                .collect(Collectors.joining("\n"));
                        log.info("state ({}) - {} - \n{}", currentClient, set.size(), entries);
                    }, 10, 20, SECONDS);
            adsbClient.readFromSdr();
        } catch (IOException e) {
            log.error(null, e);
        }
    }
}

