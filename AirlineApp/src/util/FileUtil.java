package util;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import model.Flight;
import model.User;

public class FileUtil {
    private static final String FLIGHTS_FILE = "flights.csv";
    private static final String USERS_FILE = "users.csv";
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    // ---------------- FLIGHTS ----------------
    public static List<Flight> loadFlights() {
        List<Flight> list = new ArrayList<>();
        File f = new File(FLIGHTS_FILE);
        if (!f.exists()) return list;

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isEmpty()) continue;
                String[] parts = line.split(",");
                Flight flight = new Flight(
                        parts[0],
                        parts[1],
                        parts[2],
                        LocalDateTime.parse(parts[3], dtf),
                        LocalDateTime.parse(parts[4], dtf),
                        Double.parseDouble(parts[5])
                );
                list.add(flight);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public static void saveFlights(List<Flight> flights) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FLIGHTS_FILE))) {
            for (Flight f : flights) {
                pw.println(String.join(",",
                        f.getFlightNumber(),
                        f.getDeparture(),
                        f.getArrival(),
                        f.getDepartureTime().format(dtf),
                        f.getArrivalTime().format(dtf),
                        String.valueOf((long)f.getPrice())
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ---------------- USERS ----------------
    public static List<User> loadUsers() {
        List<User> users = new ArrayList<>();
        File f = new File(USERS_FILE);
        if (!f.exists()) return users;

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isEmpty()) continue;
                String[] parts = line.split(",");
                String type = parts[2];
                if (type.equals("ADMIN")) users.add(new model.AdminUser(parts[0], parts[1]));
                else users.add(new model.RegularUser(parts[0], parts[1]));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return users;
    }

    public static void saveUsers(List<User> users) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(USERS_FILE))) {
            for (User u : users) {
                String type = u instanceof model.AdminUser ? "ADMIN" : "USER";
                pw.println(String.join(",", u.getLogin(), u.getPassword(), type));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}
