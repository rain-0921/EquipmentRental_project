package service;

import model.user.AuthenticatedUser;
import model.user.User;
import repository.UserRepository;
import security.PasswordHasher;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Verifies user credentials and returns the matching {@link User}.
 * Thin wrapper around {@link UserRepository} + {@link PasswordHasher}.
 */
public class AuthService {

    private final UserRepository userRepo = new UserRepository();

    public Optional<User> login(String email, String password) throws SQLException {
        if (email == null || email.isBlank() || password == null || password.isEmpty()) {
            return Optional.empty();
        }
        Optional<AuthenticatedUser> match = userRepo
            .findByEmail(email.trim().toLowerCase())
            .filter(u -> PasswordHasher.verify(password, u.passwordHash()));
        return match.map(u -> (User) u);
    }
}
