package domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlightEntry implements Serializable, Comparable<FlightEntry> {

    private Date timestamp;
    private String flightId;
    private double latitude;
    private double longitude;
    private double altitude;
    private double speed;

    @Override
    public int compareTo(@NotNull FlightEntry flightEntry) {
        return -timestamp.compareTo(flightEntry.getTimestamp());
    }
}
