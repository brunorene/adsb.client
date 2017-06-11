package pt.brene.adsb.client;

import com.google.common.base.Charsets;
import com.google.common.eventbus.EventBus;
import lombok.extern.slf4j.Slf4j;
import pt.brene.adsb.client.event.MessageReceiver;
import pt.brene.adsb.client.message.AdsbMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

@Slf4j
public class AdsbClient {

    private final String host;
    private final Integer port;
    private final EventBus bus = new EventBus();

    private AdsbClient(String host, Integer port) {
        this.host = host;
        this.port = port;
        bus.register(new MessageReceiver());
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


    public static void main(String[] args) {
        AdsbClient client = new AdsbClient("localhost", 30003);
        try {
            client.read();
        } catch (IOException e) {
            log.error(null, e);
        }
    }
}

