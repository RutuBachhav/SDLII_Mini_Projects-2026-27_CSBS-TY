package com.labloop.service;

import com.labloop.cdi.Audited;
import com.labloop.entity.LabUser;
import com.labloop.util.LabLoopException;
import com.labloop.util.PasswordUtil;
import jakarta.ejb.Stateless;
import java.util.List;

/** STATELESS SESSION BEAN - login and member management. */
@Stateless
@Audited
public class UserService extends ServiceSupport {

    /** Returns the user if email + password match, otherwise null. */
    public LabUser authenticate(String email, String password) {
        if (email == null || password == null) return null;
        List<LabUser> found = em.createQuery("SELECT u FROM LabUser u WHERE LOWER(u.email) = :e", LabUser.class)
                .setParameter("e", email.trim().toLowerCase()).getResultList();
        if (found.isEmpty() || !PasswordUtil.verify(password, found.get(0).getPasswordHash())) {
            return null;
        }
        LabUser u = found.get(0);
        record(u.getFullName(), "LOGIN", u.getEmail());
        return u;
    }

    public List<LabUser> findAll() {
        return em.createQuery("SELECT u FROM LabUser u ORDER BY u.role, u.fullName", LabUser.class).getResultList();
    }

    public LabUser find(Long id) {
        return requireUser(id);
    }

    public LabUser create(LabUser user, String rawPassword, Long adminId) {
        LabUser admin = requireStaff(adminId);
        if (rawPassword == null || rawPassword.length() < 6) {
            throw LabLoopException.validation("Password must be at least 6 characters.");
        }
        if (user.getEmail() != null) user.setEmail(user.getEmail().trim().toLowerCase());
        user.setPasswordHash(PasswordUtil.hash(rawPassword));
        validate(user);
        long same = em.createQuery("SELECT COUNT(u) FROM LabUser u WHERE u.email = :e", Long.class)
                .setParameter("e", user.getEmail()).getSingleResult();
        if (same > 0) {
            throw LabLoopException.conflict("A member with email " + user.getEmail() + " already exists.");
        }
        em.persist(user);
        record(admin.getFullName(), "MEMBER_ADDED", user.getFullName() + " (" + user.getRole().getLabel() + ")");
        return user;
    }
}
