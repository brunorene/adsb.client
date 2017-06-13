package domain;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class FlightEntry {

    private final String flightId;
    private final double latitude;
    private final double longitude;
    private final double altitude;
    private final double speed;
}
