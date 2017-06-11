package pt.brene.adsb.client.message;

import java.io.Serializable;
import java.time.LocalDateTime;

public class EsIdentificationAndCategory extends AdsbMessage implements Serializable {

    public String getHexId() {
        return hexId;
    }

    public LocalDateTime getDateTimeGenerated() {
        return dateTimeGenerated;
    }

    public String getCallSign() {
        return callSign;
    }

}
