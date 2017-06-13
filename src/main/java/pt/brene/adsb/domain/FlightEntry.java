package pt.brene.adsb.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = "timestamp")
public class FlightEntry implements Serializable, Comparable<FlightEntry> {

    private Date timestamp;
    private String flightId;
    private double latitude;
    private double longitude;
    private double altitude;
    private double speed;

    @Override
    public int compareTo(@NotNull FlightEntry flightEntry) {
        if (equals(flightEntry)) {
            return 0;
        }
        return -timestamp.compareTo(flightEntry.getTimestamp());
    }
}
