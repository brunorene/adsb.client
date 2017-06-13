package domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlightEntry implements Serializable {

    private String flightId;
    private double latitude;
    private double longitude;
    private double altitude;
    private double speed;
}
