package com.mycompany.hostelmanagementsystem;

import com.mycompany.hostelmanagementsystem.entity.Room;
import com.mycompany.hostelmanagementsystem.entity.Student;
import com.mycompany.hostelmanagementsystem.entity.Users;
import jakarta.persistence.EntityManager;
import java.util.List;

public class StudentDAO {

    public List<Student> getAllStudents() {
        EntityManager em = JPAUtil.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT s FROM Student s ORDER BY s.name", Student.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public Student getStudentByUsername(String username) {
        if (username == null) return null;

        EntityManager em = JPAUtil.createEntityManager();
        try {
            List<Student> results = em.createQuery(
                    "SELECT s FROM Student s WHERE s.user.username = :username",
                    Student.class)
                    .setParameter("username", username)
                    .getResultList();

            return results.isEmpty() ? null : results.get(0);
        } finally {
            em.close();
        }
    }

    public Student getStudentById(Integer id) {
        EntityManager em = JPAUtil.createEntityManager();
        try {
            return em.find(Student.class, id);
        } finally {
            em.close();
        }
    }

    public List<Room> getAvailableRooms() {
        EntityManager em = JPAUtil.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT r FROM Room r WHERE r.currentOccupancy < r.capacity " +
                    "ORDER BY r.roomNo", Room.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public void admitStudent(Student student, String username,
                             String password, Integer roomId) {

        if (username == null || username.trim().isEmpty()) {
            throw new RuntimeException("Student username is required.");
        }
        if (password == null || password.length() < 4) {
            throw new RuntimeException("Student password must contain at least 4 characters.");
        }

        EntityManager em = JPAUtil.createEntityManager();

        try {
            em.getTransaction().begin();

            Long usernameCount = em.createQuery(
                    "SELECT COUNT(u) FROM Users u WHERE u.username = :username",
                    Long.class)
                    .setParameter("username", username.trim())
                    .getSingleResult();

            if (usernameCount > 0) {
                throw new RuntimeException("Username already exists. Choose another username.");
            }

            Room room = em.find(Room.class, roomId);
            if (room == null) {
                throw new RuntimeException("Selected room not found.");
            }

            if (room.getCurrentOccupancy() >= room.getCapacity()) {
                throw new RuntimeException("Selected room is already full.");
            }

            Users user = new Users();
            user.setUsername(username.trim());
            user.setPassword(password);
            user.setRole("STUDENT");
            em.persist(user);

            student.setUser(user);
            student.setRoom(room);
            em.persist(student);

            int occupancy = room.getCurrentOccupancy() + 1;
            room.setCurrentOccupancy(occupancy);
            room.setStatus(occupancy >= room.getCapacity() ? "FULL" : "AVAILABLE");

            em.getTransaction().commit();

        } catch (RuntimeException e) {
            rollback(em);
            throw e;
        } finally {
            em.close();
        }
    }

    public void deleteStudent(Integer id) {
        EntityManager em = JPAUtil.createEntityManager();

        try {
            em.getTransaction().begin();

            Student student = em.find(Student.class, id);
            if (student == null) {
                throw new RuntimeException("Student not found.");
            }

            Room room = student.getRoom();
            Users user = student.getUser();

            em.createQuery("DELETE FROM FeePayment f WHERE f.student.id = :id")
                    .setParameter("id", id)
                    .executeUpdate();

            em.createQuery("DELETE FROM Complaint c WHERE c.student.id = :id")
                    .setParameter("id", id)
                    .executeUpdate();

            if (room != null) {
                room.setCurrentOccupancy(
                        Math.max(0, room.getCurrentOccupancy() - 1));
                room.setStatus("AVAILABLE");
                em.merge(room);
            }

            em.remove(student);

            if (user != null) {
                Users managedUser = em.find(Users.class, user.getId());
                if (managedUser != null) {
                    em.remove(managedUser);
                }
            }

            em.getTransaction().commit();

        } catch (RuntimeException e) {
            rollback(em);
            throw e;
        } finally {
            em.close();
        }
    }

    private void rollback(EntityManager em) {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
    }
}
