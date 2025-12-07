package util;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import model.Flight;

public class FlightGenerator {
    private static final String[] cities = {"Нур-Султан", "Алматы", "Лондон", "Париж", "Москва", "Токио"};
    private static final Random rand = new Random();

    public static List<Flight> generateFlights(int count) {
        List<Flight> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String from = cities[rand.nextInt(cities.length)];
            String to;
            do { to = cities[rand.nextInt(cities.length)]; } while (to.equals(from));
            LocalDateTime dep = LocalDateTime.now().plusHours(rand.nextInt(240));
            LocalDateTime arr = dep.plusHours(2 + rand.nextInt(8));
            String number = "KC" + (100 + rand.nextInt(900));
            double price = 40000 + rand.nextInt(120000);
            list.add(new Flight(number, from, to, dep, arr, price));
        }
        return list;
    }
}
