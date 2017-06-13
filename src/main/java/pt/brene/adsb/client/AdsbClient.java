package pt.brene.adsb.client;

import com.eaio.uuid.UUID;
import com.eaio.uuid.UUIDHelper;
import com.google.common.base.Charsets;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import consumer.FlightConsumer;
import lombok.extern.slf4j.Slf4j;
import consumer.MessageReceiver;
import pt.brene.adsb.client.message.AdsbMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.Executors;

@Slf4j
public class AdsbClient {

    private final String host;
    private final Integer port;
    private final EventBus bus = new AsyncEventBus(Executors.newWorkStealingPool(4));

    private AdsbClient(String host, Integer port) {
        this.host = host;
        this.port = port;
        bus.register(new MessageReceiver(bus));
    }

    private void read() throws IOException {
        try (Socket socket = new Socket(host, port);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charsets.UTF_8))) {
            reader.lines()
                    .map(line -> AdsbMessage.newMessage(line))
                    .filter(msg -> msg != null)
                    .forEach(bus::post);
        }
    }

    public UUID getKey() {
        UUID uuid = new UUID();
        bus.register(new FlightConsumer(uuid));
        return uuid;
    }

    public static void main(String[] args) {
        AdsbClient client = new AdsbClient("localhost", 30003);
        try {
            log.info("new key: {}", client.getKey());
            log.info("new key: {}", client.getKey());
            log.info("new key: {}", client.getKey());
            client.read();
        } catch (IOException e) {
            log.error(null, e);
        }
    }
}

