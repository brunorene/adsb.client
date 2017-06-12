package pt.brene.adsb.client.message;

import java.util.Date;

public class EsIdentificationAndCategory extends AdsbMessage {

    public String getHexId() {
        return hexId;
    }

    public Date getDateTimeGenerated() {
        return dateTimeGenerated;
    }

    public String getCallSign() {
        return callSign;
    }
}
