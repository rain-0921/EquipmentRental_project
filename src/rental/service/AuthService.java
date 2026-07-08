package rental.service;

import rental.repo.UserRepository;
import rental.model.user.User;
import rental.model.user.UserStatus;

public class AuthService {
    private static AuthService instance;
    private final UserRepository userRepository;
    private User currentUser;

    private AuthService() {
        this.userRepository = UserRepository.getInstance();
    }

    public static AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    public User authenticate(String userId, String password) {
        User user = userRepository.findByUserIdAndPassword(userId, password);
        if (user != null) {
            if (user.getStatus() == UserStatus.INACTIVE) {
                return null;
            }
            this.currentUser = user;
            return user;
        }
        return null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void logout() {
        this.currentUser = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean isStaff() {
        return currentUser != null && 
               currentUser.getRole() == rental.model.user.UserRole.STAFF;
    }
}
