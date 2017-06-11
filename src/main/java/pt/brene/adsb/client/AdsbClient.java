package pt.brene.adsb.client;

import com.google.common.eventbus.EventBus;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.stream.Stream;

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
             BufferedInputStream inputStream = new BufferedInputStream(socket.getInputStream())) {
            int read;
            StringBuilder msg = new StringBuilder();
            while ((read = inputStream.read()) >= 0) {
                if (read != ';') {
                    msg.append((char) read);
                } else {
                    if (msg.toString().trim().startsWith("*")) {
                        bus.post(msg.substring(2));
                    }
                    msg = new StringBuilder();
                }
            }
        }
    }


    public static void main(String[] args) {
        AdsbClient client = new AdsbClient("localhost", 30002);
        try {
            client.read();
        } catch (IOException e) {
            log.error(null, e);
        }
    }
}

