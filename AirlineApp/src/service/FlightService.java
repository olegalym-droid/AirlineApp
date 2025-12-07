package service;

import java.util.List;
import model.Flight;
import util.FileUtil;

public class FlightService {
    private List<Flight> flights;

    public FlightService() {
        flights = FileUtil.loadFlights();
    }

    public List<Flight> getAllFlights() { return flights; }

    public void addFlight(Flight flight) {
        flights.add(flight);
        FileUtil.saveFlights(flights);
    }

    public void updateFlight(Flight flight) {
        // просто перезаписываем CSV
        FileUtil.saveFlights(flights);
    }

    public void deleteFlight(Flight flight) {
        flights.remove(flight);
        FileUtil.saveFlights(flights);
    }
}
