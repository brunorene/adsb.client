package pt.brene.adsb.api;

import com.eaio.uuid.UUID;
import com.google.common.net.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.http.HttpHeader;
import pt.brene.adsb.client.AdsbClient;

import java.io.IOException;

import static spark.Spark.get;
import static spark.Spark.port;

@Slf4j
public class RestApi {

    public static AppConfig appConfig;

    static {
        try {
            appConfig = new AppConfig();
        } catch (IOException e) {
            log.error(null, e);
        }
    }

    public static void main(String... args) throws IOException {
        AdsbClient adsbClient = new AdsbClient(appConfig.getString("adsb.host"), appConfig.getInt("adsb.port"));
        port(appConfig.getInt("rest.port"));
        adsbClient.readFromSdr();
        get("/key", (req, res) -> adsbClient.getKey());
        get("/clients", (req, res) -> {
            res.header(HttpHeader.CONTENT_TYPE.asString(), MediaType.JSON_UTF_8.toString());
            return adsbClient.getClients();
        }, new JsonTransformer());
        get("/poll/:uuid", (req, res) -> {
            res.header(HttpHeader.CONTENT_TYPE.asString(), MediaType.JSON_UTF_8.toString());
            return adsbClient.pollState(new UUID(req.params(":uuid")));
        }, new JsonTransformer());
    }
}
