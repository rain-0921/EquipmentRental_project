package rental.service;

import rental.repo.UserRepository;
import rental.repo.RentalRepository;
import rental.model.user.User;
import rental.model.user.UserRole;
import rental.model.user.UserStatus;
import rental.model.user.StudentUser;
import rental.model.user.FinalYearStudentUser;
import rental.model.user.StaffUser;

import java.util.List;

public class UserService {
    private static UserService instance;
    private final UserRepository userRepository;
    private final RentalRepository rentalRepository;

    private UserService() {
        this.userRepository = UserRepository.getInstance();
        this.rentalRepository = RentalRepository.getInstance();
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
        
        User user;
        switch (role) {
            case STUDENT:
                user = new StudentUser(userId, name, password);
                break;
            case FINAL_YEAR_STUDENT:
                user = new FinalYearStudentUser(userId, name, password);
                break;
            case STAFF:
                user = new StaffUser(userId, name, password);
                break;
            default:
                return "Invalid role";
        }
        
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
        
        if (name != null && !name.isEmpty()) {
            user = createUpdatedUser(user, name, password, user.getRole());
        } else if (password != null && !password.isEmpty()) {
            user = createUpdatedUser(user, user.getName(), password, user.getRole());
        }
        
        userRepository.updateUser(user);
        return "SUCCESS";
    }

    private User createUpdatedUser(User oldUser, String name, String password, UserRole role) {
        switch (role) {
            case STUDENT:
                return new StudentUser(oldUser.getUserId(), name, password);
            case FINAL_YEAR_STUDENT:
                return new FinalYearStudentUser(oldUser.getUserId(), name, password);
            case STAFF:
                return new StaffUser(oldUser.getUserId(), name, password);
            default:
                return oldUser;
        }
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
