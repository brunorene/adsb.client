package pt.brene.adsb.client.event;

import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import pt.brene.adsb.client.message.EsAirbornePosition;
import pt.brene.adsb.client.message.EsAirborneVelocity;
import pt.brene.adsb.client.message.EsIdentificationAndCategory;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@SuppressWarnings("unchecked")
public class MessageReceiver {

    private final DB db = DBMaker.fileDB("messages.db")
            .closeOnJvmShutdown()
            .transactionEnable()
            .make();
    private final HTreeMap<String, List<EsAirbornePosition>> positions = (HTreeMap<String, List<EsAirbornePosition>>) db.hashMap("position").createOrOpen();
    private final HTreeMap<String, List<EsAirborneVelocity>> speeds = (HTreeMap<String, List<EsAirborneVelocity>>) db.hashMap("speed").createOrOpen();
    private final HTreeMap<String, List<EsIdentificationAndCategory>> identifiers = (HTreeMap<String, List<EsIdentificationAndCategory>>) db.hashMap("identifier").createOrOpen();

    @Subscribe
    public void receive(EsAirbornePosition position) {
        List<EsAirbornePosition> past = positions.computeIfAbsent(position.getHexId(), k -> new ArrayList<>());
        past.add(position);
        positions.put(position.getHexId(), past);
        db.commit();
    }

    @Subscribe
    public void receive(EsAirborneVelocity speed) {
        List<EsAirborneVelocity> past = speeds.computeIfAbsent(speed.getHexId(), k -> new ArrayList<>());
        past.add(speed);
        speeds.put(speed.getHexId(), past);
        db.commit();
    }

    @Subscribe
    public void receive(EsIdentificationAndCategory identifier) {
        List<EsIdentificationAndCategory> past = identifiers.computeIfAbsent(identifier.getHexId(), k -> new ArrayList<>());
        past.add(identifier);
        identifiers.put(identifier.getHexId(), past);
        db.commit();
    }

    private void logFlight(String hexId) {
        
    }
}
