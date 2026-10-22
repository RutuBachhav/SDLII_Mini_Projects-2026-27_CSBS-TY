package com.service;

import com.dao.UserDao;
import com.entity.User;
import com.util.AppException;
import com.util.Jpa;
import com.util.Passwords;
import com.util.ValidationException;
import jakarta.persistence.PersistenceException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Registration and sign-in.
 *
 * All validation happens here, on the server. The HTML form has {@code
 * required} and {@code type="email"} attributes too, but they are a
 * convenience; nothing in this class assumes they ran.
 */
public class AuthService {

    /**
     * Deliberately permissive: enough to catch a typo, not an attempt to
     * implement RFC 5322. The unique index is what guarantees uniqueness.
     */
    private static final Pattern EMAIL =
            Pattern.compile("^[^@\\s]+@[^@\\s.]+(\\.[^@\\s.]+)+$");

    private static final int NAME_MAX = 100;
    private static final int EMAIL_MAX = 190;
    private static final int PASSWORD_MIN = 8;
    private static final int PASSWORD_MAX = 200;

    private final UserDao users = new UserDao();

    /**
     * Creates an account and returns it.
     *
     * The duplicate email is reported twice over: once from a lookup, so the
     * message appears next to the field, and once by catching the unique
     * constraint, which is what actually holds when two registrations for the
     * same address arrive at the same moment.
     */
    public User register(String fullName, String email, String password, String confirm) {
        Map<String, String> errors = new LinkedHashMap<>();

        String name = trim(fullName);
        if (name.isEmpty()) {
            errors.put("fullName", "Your name is required.");
        } else if (name.length() < 2) {
            errors.put("fullName", "Please enter your full name.");
        } else if (name.length() > NAME_MAX) {
            errors.put("fullName", "Name cannot be longer than " + NAME_MAX + " characters.");
        }

        String mail = trim(email).toLowerCase();
        if (mail.isEmpty()) {
            errors.put("email", "Email is required.");
        } else if (mail.length() > EMAIL_MAX) {
            errors.put("email", "Email cannot be longer than " + EMAIL_MAX + " characters.");
        } else if (!EMAIL.matcher(mail).matches()) {
            errors.put("email", "That does not look like an email address.");
        }

        String pass = password == null ? "" : password;
        if (pass.isEmpty()) {
            errors.put("password", "Password is required.");
        } else if (pass.length() < PASSWORD_MIN) {
            errors.put("password", "Password must be at least " + PASSWORD_MIN + " characters.");
        } else if (pass.length() > PASSWORD_MAX) {
            errors.put("password", "Password is too long.");
        } else if (!pass.equals(confirm == null ? "" : confirm)) {
            errors.put("confirm", "The two passwords do not match.");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        return Jpa.write(em -> {
            if (users.emailExists(em, mail)) {
                throw new ValidationException("email", "That email is already registered.");
            }
            try {
                return users.insert(em, new User(name, mail, Passwords.hash(pass)));
            } catch (PersistenceException e) {
                // uq_users_email - two registrations raced.
                throw new ValidationException("email", "That email is already registered.");
            }
        });
    }

    /**
     * Checks credentials and returns the user.
     *
     * One message for both "no such email" and "wrong password", so the form
     * cannot be used to find out which addresses have accounts. The hash is
     * verified even when the email is unknown, against a throwaway hash, so the
     * two cases also take about the same time.
     */
    public User login(String email, String password) {
        String mail = trim(email).toLowerCase();
        String pass = password == null ? "" : password;
        if (mail.isEmpty() || pass.isEmpty()) {
            throw new AppException("Enter your email and password.");
        }

        Optional<User> found = Jpa.read(em -> users.findByEmail(em, mail));
        if (found.isEmpty()) {
            Passwords.verify(pass, "pbkdf2_sha256$1000$AAAAAAAAAAAAAAAAAAAAAA$AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
            throw new AppException("Email or password is incorrect.");
        }
        User user = found.get();
        if (!Passwords.verify(pass, user.getPasswordHash())) {
            throw new AppException("Email or password is incorrect.");
        }
        return user;
    }

    /** Reloads the signed-in user, e.g. to show their name in the header. */
    public Optional<User> byId(long userId) {
        return Jpa.read(em -> users.findById(em, userId));
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
