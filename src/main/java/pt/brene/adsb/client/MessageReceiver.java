package pt.brene.adsb.client;

import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;

import java.util.List;

@Slf4j
public class MessageReceiver {

    private final DB db = DBMaker.fileDB("messages.db")
            .closeOnJvmShutdown()
            .transactionEnable()
            .make();
    @SuppressWarnings("unchecked")
    private final HTreeMap<String, List<AdsbMessage>> messages = (HTreeMap<String, List<AdsbMessage>>) db.hashMap("messages").createOrOpen();

    @Subscribe
    public void receive(AdsbMessage msg) {
    }
}
