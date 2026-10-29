package com.splitease.service;

import com.splitease.dao.UserDAO;
import com.splitease.exception.ApiException;
import com.splitease.model.User;
import java.util.List;
import java.util.regex.Pattern;

/** Business rules for people. */
public class UserService {

    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final Pattern PHONE = Pattern.compile("^[6-9]\\d{9}$");   // Indian mobile number

    private final UserDAO userDAO = new UserDAO();

    public List<User> listUsers() {
        return userDAO.findAll();
    }

    public User getUser(int id) {
        User u = userDAO.findById(id);
        if (u == null) {
            throw ApiException.notFound("User " + id + " not found");
        }
        return u;
    }

    public User createUser(User u) {
        if (u == null) {
            throw ApiException.badRequest("Request body is missing");
        }
        String name = trim(u.getName());
        String email = trim(u.getEmail());
        String phone = trim(u.getPhone());

        if (name.length() < 2 || name.length() > 80) {
            throw ApiException.badRequest("Name must be 2 to 80 characters");
        }
        if (!EMAIL.matcher(email).matches()) {
            throw ApiException.badRequest("Enter a valid e-mail address");
        }
        if (!phone.isEmpty() && !PHONE.matcher(phone).matches()) {
            throw ApiException.badRequest("Phone must be a 10-digit mobile number");
        }
        if (userDAO.emailExists(email)) {
            throw ApiException.conflict("A user with this e-mail already exists");
        }
        u.setName(name);
        u.setEmail(email.toLowerCase());
        u.setPhone(phone.isEmpty() ? null : phone);
        return userDAO.insert(u);
    }

    static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
