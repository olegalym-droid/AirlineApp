package service;

import java.util.List;
import model.User;
import util.FileUtil;

public class UserService {
    private List<User> users;

    public UserService() {
        users = FileUtil.loadUsers();
    }

    public boolean isLoginExists(String login) {
        return users.stream().anyMatch(u -> u.getLogin().equalsIgnoreCase(login));
    }

    public void register(String login, String password) {
        User u = new User(login, password);
        users.add(u);
        FileUtil.saveUsers(users);
    }

    public User login(String login, String password) {
        return users.stream()
                .filter(u -> u.getLogin().equalsIgnoreCase(login) && u.getPassword().equals(password))
                .findFirst().orElse(null);
    }
}
