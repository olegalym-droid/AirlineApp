package ui;

import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import model.AdminUser;
import model.Flight;
import model.User;
import service.FlightService;
import service.UserService;
import util.FileUtil;
import util.FlightGenerator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MainAppSwing extends JFrame {

    private static final Logger logger = LogManager.getLogger(MainAppSwing.class);

    private final UserService userService = new UserService();
    private final FlightService flightService = new FlightService();

    private JTable flightTable;
    private JTextField fromField, toField, dateField;

    private final DateTimeFormatter dtfDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final DateTimeFormatter dtfDate = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final Random rand = new Random();

    private User currentUser; // текущий пользователь

    public MainAppSwing() {
        setTitle("Airline App — Регистрация и поиск авиабилетов");
        setSize(1000, 640);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        logger.info("Приложение запущено");

        showStartMenu();
    }

    // ---------------- START MENU ----------------
    private void showStartMenu() {
        currentUser = null; // сброс текущего пользователя
        getContentPane().removeAll();
        setLayout(new GridLayout(3, 1, 10, 10));

        add(new JLabel("Добро пожаловать в Airline App", SwingConstants.CENTER));

        JButton loginButton = new JButton("Войти");
        JButton registerButton = new JButton("Регистрация");

        loginButton.addActionListener(e -> showLogin());
        registerButton.addActionListener(e -> showRegister());

        JPanel p = new JPanel();
        p.add(loginButton);
        p.add(registerButton);

        add(new JPanel()); // spacer
        add(p);

        revalidate();
        repaint();
    }

    // ---------------- LOGIN ----------------
    private void showLogin() {
        getContentPane().removeAll();
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Вход", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        add(new JLabel("Логин:"), gbc);
        JTextField loginField = new JTextField();
        gbc.gridx = 1; add(loginField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        add(new JLabel("Пароль:"), gbc);
        JPasswordField passField = new JPasswordField();
        gbc.gridx = 1; add(passField, gbc);

        JButton loginBtn = new JButton("Войти");
        JButton backBtn = new JButton("Назад");
        gbc.gridy = 3; gbc.gridx = 0; add(backBtn, gbc);
        gbc.gridx = 1; add(loginBtn, gbc);

        backBtn.addActionListener(e -> showStartMenu());

        loginBtn.addActionListener(e -> {
            String u = loginField.getText().trim();
            String p = new String(passField.getPassword()).trim();
            if (u.isEmpty() || p.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Введите логин и пароль!");
                logger.warn("Попытка входа с пустыми полями");
                return;
            }

            User user;
            if (u.equals("admin") && p.equals("admin")) {
                user = new AdminUser("admin", "admin");
            } else {
                user = userService.login(u, p);
            }

            if (user != null) {
                currentUser = user;
                logger.info("Пользователь вошел: " + u);
                showFlightSearch();
            } else {
                JOptionPane.showMessageDialog(this, "Неверный логин или пароль!");
                logger.warn("Неудачная попытка входа: " + u);
            }
        });

        revalidate();
        repaint();
    }

    // ---------------- REGISTER ----------------
    private void showRegister() {
        getContentPane().removeAll();
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Регистрация", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        add(new JLabel("Логин:"), gbc);
        JTextField loginField = new JTextField();
        gbc.gridx = 1; add(loginField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        add(new JLabel("Пароль:"), gbc);
        JPasswordField passField = new JPasswordField();
        gbc.gridx = 1; add(passField, gbc);

        JButton regBtn = new JButton("Создать аккаунт");
        JButton backBtn = new JButton("Назад");
        gbc.gridy = 3; gbc.gridx = 0; add(backBtn, gbc);
        gbc.gridx = 1; add(regBtn, gbc);

        backBtn.addActionListener(e -> showStartMenu());

        regBtn.addActionListener(e -> {
            String u = loginField.getText().trim();
            String p = new String(passField.getPassword()).trim();
            if (u.isEmpty() || p.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Заполните все поля!");
                logger.warn("Попытка регистрации с пустыми полями");
                return;
            }
            if (userService.login(u, p) != null || u.equals("admin")) {
                JOptionPane.showMessageDialog(this, "Такой логин уже зарегистрирован!");
                logger.warn("Попытка регистрации с уже существующим логином: " + u);
                return;
            }
            userService.register(u, p);
            JOptionPane.showMessageDialog(this, "Регистрация выполнена. Войдите в систему.");
            logger.info("Пользователь зарегистрирован: " + u);
            showLogin();
        });

        revalidate();
        repaint();
    }

    // ---------------- FLIGHT SEARCH / TABLE ----------------
    private void showFlightSearch() {
        getContentPane().removeAll();
        setLayout(new BorderLayout(10, 10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        top.add(new JLabel("Откуда:"));
        fromField = new JTextField(14);
        top.add(fromField);

        top.add(new JLabel("Куда:"));
        toField = new JTextField(14);
        top.add(toField);

        top.add(new JLabel("Дата (yyyy-MM-dd):"));
        dateField = new JTextField(10);
        top.add(dateField);

        JButton searchBtn = new JButton("Поиск");
        JButton allBtn = new JButton("Показать все");
        JButton refreshBtn = new JButton("Обновить рейсы");
        JButton logoutBtn = new JButton("Выйти");

        top.add(searchBtn);
        top.add(allBtn);
        top.add(refreshBtn);
        top.add(logoutBtn);

        add(top, BorderLayout.NORTH);

        flightTable = new JTable();
        JScrollPane scroll = new JScrollPane(flightTable);
        add(scroll, BorderLayout.CENTER);

        installContextMenu();

        searchBtn.addActionListener(this::onSearch);
        allBtn.addActionListener(e -> loadFlightsToTable(flightService.getAllFlights()));
        logoutBtn.addActionListener(e -> {
            logger.info("Пользователь вышел: " + (currentUser != null ? currentUser.getLogin() : "неизвестный"));
            showStartMenu();
        });
        refreshBtn.addActionListener(e -> onRefreshFlights());

        loadFlightsToTable(flightService.getAllFlights());

        revalidate();
        repaint();
    }

    // ---------------- CONTEXT MENU ----------------
    private void installContextMenu() {
        if (!(currentUser instanceof AdminUser)) return; // контекст меню только для админа

        JPopupMenu menu = new JPopupMenu();
        JMenuItem editItem = new JMenuItem("Изменить рейс");
        JMenuItem deleteItem = new JMenuItem("Удалить рейс");
        menu.add(editItem);
        menu.add(deleteItem);

        flightTable.addMouseListener(new MouseAdapter() {
            private void tryShow(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int row = flightTable.rowAtPoint(e.getPoint());
                    if (row >= 0 && row < flightTable.getRowCount()) {
                        flightTable.setRowSelectionInterval(row, row);
                        menu.show(flightTable, e.getX(), e.getY());
                    }
                }
            }
            @Override public void mousePressed(MouseEvent e) { tryShow(e); }
            @Override public void mouseReleased(MouseEvent e) { tryShow(e); }
        });

        editItem.addActionListener(e -> editSelectedFlight());
        deleteItem.addActionListener(e -> deleteSelectedFlight());
    }

    // ---------------- SEARCH ----------------
    private void onSearch(ActionEvent ev) {
        String from = fromField.getText().trim().toLowerCase();
        String to = toField.getText().trim().toLowerCase();
        String date = dateField.getText().trim();

        List<Flight> list = flightService.getAllFlights().stream()
                .filter(f -> from.isEmpty() || f.getDeparture().toLowerCase().contains(from))
                .filter(f -> to.isEmpty() || f.getArrival().toLowerCase().contains(to))
                .filter(f -> date.isEmpty() || f.getDepartureTime().toLocalDate().toString().equals(date))
                .sorted(Comparator.comparing(Flight::getDepartureTime))
                .collect(Collectors.toList());

        loadFlightsToTable(list);
        logger.info("Поиск рейсов выполнен: from='" + from + "' to='" + to + "' date='" + date + "'");
    }

    // ---------------- LOAD TABLE ----------------
    private void loadFlightsToTable(List<Flight> flights) {
        String[] cols = {"№ рейса", "Откуда", "Куда", "Вылет", "Прилет", "Цена"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        flights.sort(Comparator.comparing(Flight::getDepartureTime));

        for (Flight f : flights) {
            model.addRow(new Object[]{
                    f.getFlightNumber(),
                    f.getDeparture(),
                    f.getArrival(),
                    f.getDepartureTime().format(dtfDateTime),
                    f.getArrivalTime().format(dtfDateTime),
                    String.format("%.0f", f.getPrice())
            });
        }

        flightTable.setModel(model);
    }

    // ---------------- EDIT ----------------
    private void editSelectedFlight() {
        int row = flightTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Выберите рейс."); return; }

        String flightNumber = (String) flightTable.getValueAt(row, 0);
        Flight flight = flightService.getAllFlights().stream()
                .filter(f -> f.getFlightNumber().equals(flightNumber))
                .findFirst().orElse(null);

        if (flight == null) { JOptionPane.showMessageDialog(this, "Рейс не найден."); return; }

        JTextField depField = new JTextField(flight.getDeparture());
        JTextField arrField = new JTextField(flight.getArrival());
        JTextField depTimeField = new JTextField(flight.getDepartureTime().format(dtfDateTime));
        JTextField arrTimeField = new JTextField(flight.getArrivalTime().format(dtfDateTime));
        JTextField priceField = new JTextField(String.valueOf((long) flight.getPrice()));

        JPanel p = new JPanel(new GridLayout(0,2,6,6));
        p.add(new JLabel("Откуда:")); p.add(depField);
        p.add(new JLabel("Куда:")); p.add(arrField);
        p.add(new JLabel("Вылет (yyyy-MM-dd HH:mm):")); p.add(depTimeField);
        p.add(new JLabel("Прилет (yyyy-MM-dd HH:mm):")); p.add(arrTimeField);
        p.add(new JLabel("Цена:")); p.add(priceField);

        int res = JOptionPane.showConfirmDialog(this, p, "Редактирование рейса " + flightNumber,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (res == JOptionPane.OK_OPTION) {
            try {
                flight.setDeparture(depField.getText().trim());
                flight.setArrival(arrField.getText().trim());
                flight.setDepartureTime(LocalDateTime.parse(depTimeField.getText().trim(), dtfDateTime));
                flight.setArrivalTime(LocalDateTime.parse(arrTimeField.getText().trim(), dtfDateTime));
                flight.setPrice(Double.parseDouble(priceField.getText().trim()));
                flightService.updateFlight(flight);
                FileUtil.saveFlights(flightService.getAllFlights());
                loadFlightsToTable(flightService.getAllFlights());
                JOptionPane.showMessageDialog(this, "Рейс обновлён.");
                logger.info("Рейс обновлен: " + flightNumber);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Ошибка: проверьте формат полей.");
                logger.error("Ошибка при редактировании рейса: " + flightNumber, ex);
            }
        }
    }

    // ---------------- DELETE ----------------
    private void deleteSelectedFlight() {
        int row = flightTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Выберите рейс."); return; }

        String flightNumber = (String) flightTable.getValueAt(row, 0);
        Flight flight = flightService.getAllFlights().stream()
                .filter(f -> f.getFlightNumber().equals(flightNumber))
                .findFirst().orElse(null);

        if (flight == null) { JOptionPane.showMessageDialog(this, "Рейс не найден."); return; }

        int confirm = JOptionPane.showConfirmDialog(this, "Удалить рейс " + flightNumber + "?", "Подтверждение",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            flightService.deleteFlight(flight);
            FileUtil.saveFlights(flightService.getAllFlights());
            loadFlightsToTable(flightService.getAllFlights());
            JOptionPane.showMessageDialog(this, "Рейс удалён.");
            logger.info("Рейс удален: " + flightNumber);
        }
    }

    // ---------------- REFRESH ----------------
    private void onRefreshFlights() {
        List<Flight> flights = flightService.getAllFlights();

        // Генерируем 5 новых рейсов
        List<Flight> generated = FlightGenerator.generateFlights(5);

        // Добавляем новые рейсы к последнему времени вылета
        LocalDateTime last = flights.isEmpty() ? LocalDateTime.now() : flights.get(flights.size() - 1).getDepartureTime();
        for (int i = 0; i < generated.size(); i++) {
            Flight nf = generated.get(i);
            LocalDateTime newDep = last.plusHours(2L * (i + 1)).plusHours(rand.nextInt(3));
            nf.setDepartureTime(newDep);
            nf.setArrivalTime(newDep.plusHours(2 + rand.nextInt(8)));
            flights.add(nf);
        }

        flights.sort(Comparator.comparing(Flight::getDepartureTime));
        FileUtil.saveFlights(flights);
        loadFlightsToTable(flights);

        logger.info("Добавлено 5 новых рейсов");
    }

    // ---------------- MAIN ----------------
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainAppSwing app = new MainAppSwing();
            app.setVisible(true);
            logger.info("Главное окно приложения отображено");
        });
    }
}
