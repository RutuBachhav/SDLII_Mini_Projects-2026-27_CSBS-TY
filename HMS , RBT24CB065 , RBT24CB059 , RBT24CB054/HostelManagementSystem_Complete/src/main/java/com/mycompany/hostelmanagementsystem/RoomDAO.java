package com.mycompany.hostelmanagementsystem;

import com.mycompany.hostelmanagementsystem.entity.Room;
import jakarta.persistence.EntityManager;
import java.util.List;

public class RoomDAO {

    public List<Room> getAllRooms() {
        EntityManager em = JPAUtil.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT r FROM Room r ORDER BY r.roomNo", Room.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public Room getRoomById(Integer id) {
        EntityManager em = JPAUtil.createEntityManager();
        try {
            return em.find(Room.class, id);
        } finally {
            em.close();
        }
    }

    public void addRoom(Room room) {
        validateRoom(room);
        EntityManager em = JPAUtil.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(room);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            rollback(em);
            throw e;
        } finally {
            em.close();
        }
    }

    public void updateRoom(Room room) {
        EntityManager em = JPAUtil.createEntityManager();
        try {
            em.getTransaction().begin();

            Room existing = em.find(Room.class, room.getId());
            if (existing == null) {
                throw new RuntimeException("Room not found.");
            }

            if (room.getCapacity() < existing.getCurrentOccupancy()) {
                throw new RuntimeException(
                        "Capacity cannot be less than current occupancy (" +
                        existing.getCurrentOccupancy() + ").");
            }

            existing.setRoomNo(room.getRoomNo().trim());
            existing.setRoomType(room.getRoomType());
            existing.setCapacity(room.getCapacity());
            existing.setStatus(
                    existing.getCurrentOccupancy() >= existing.getCapacity()
                    ? "FULL" : "AVAILABLE");

            em.getTransaction().commit();
        } catch (RuntimeException e) {
            rollback(em);
            throw e;
        } finally {
            em.close();
        }
    }

    public void deleteRoom(Integer id) {
        EntityManager em = JPAUtil.createEntityManager();
        try {
            em.getTransaction().begin();

            Room room = em.find(Room.class, id);
            if (room == null) {
                throw new RuntimeException("Room not found.");
            }

            Long students = em.createQuery(
                    "SELECT COUNT(s) FROM Student s WHERE s.room.id = :roomId",
                    Long.class)
                    .setParameter("roomId", id)
                    .getSingleResult();

            if (students > 0) {
                throw new RuntimeException(
                        "Cannot delete this room because students are assigned to it.");
            }

            em.remove(room);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            rollback(em);
            throw e;
        } finally {
            em.close();
        }
    }

    private void validateRoom(Room room) {
        if (room.getRoomNo() == null || room.getRoomNo().trim().isEmpty()) {
            throw new RuntimeException("Room number is required.");
        }
        if (room.getCapacity() == null || room.getCapacity() < 1) {
            throw new RuntimeException("Capacity must be at least 1.");
        }
    }

    private void rollback(EntityManager em) {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
    }
}
