package rental.service;

import rental.repo.UserRepository;
import rental.repo.RentalRepository;
import rental.model.user.User;
import rental.model.user.UserRole;
import rental.model.user.UserStatus;
import rental.model.user.UserFactory;

import java.util.List;

public class UserService {
    private static UserService instance;
    private final UserRepository userRepository;
    private final RentalRepository rentalRepository;
    private final UserFactory userFactory;

    private UserService() {
        this.userRepository = UserRepository.getInstance();
        this.rentalRepository = RentalRepository.getInstance();
        this.userFactory = UserFactory.getInstance();
    }

    public static UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }

    public String createUser(String userId, String name, String password, UserRole role) {
        if (userRepository.getUser(userId) != null) {
            return "User ID already exists";
        }

        User user = userFactory.createUser(userId, name, password, role);
        userRepository.addUser(user);
        return "SUCCESS";
    }

    public User getUser(String userId) {
        return userRepository.getUser(userId);
    }

    public List<User> getAllUsers() {
        return userRepository.getAllUsers();
    }

    public String updateUser(String userId, String name, String password) {
        User user = userRepository.getUser(userId);
        if (user == null) {
            return "User not found";
        }

        if ((name != null && !name.isEmpty()) || (password != null && !password.isEmpty())) {
            String newName = (name != null && !name.isEmpty()) ? name : user.getName();
            String newPassword = (password != null && !password.isEmpty()) ? password : user.getPassword();
            user = userFactory.createUser(userId, newName, newPassword, user.getRole());
            user.setStatus(user.getStatus());
        }

        userRepository.updateUser(user);
        return "SUCCESS";
    }

    public String inactivateUser(String userId) {
        User user = userRepository.getUser(userId);
        if (user == null) {
            return "User not found";
        }

        if (user.getStatus() == UserStatus.INACTIVE) {
            return "User is already inactive";
        }

        if (rentalRepository.hasActiveRentalsForUser(userId)) {
            return "Cannot inactivate user with active rentals. Please return all equipment first.";
        }

        user.setStatus(UserStatus.INACTIVE);
        userRepository.updateUser(user);
        return "SUCCESS";
    }

    public String activateUser(String userId) {
        User user = userRepository.getUser(userId);
        if (user == null) {
            return "User not found";
        }

        if (user.getStatus() != UserStatus.INACTIVE) {
            return "User is already active";
        }

        user.setStatus(UserStatus.ACTIVE);
        userRepository.updateUser(user);
        return "SUCCESS";
    }
}
