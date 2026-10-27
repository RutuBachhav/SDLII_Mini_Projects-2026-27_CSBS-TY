package com.mycompany.hostelmanagementsystem;

import com.mycompany.hostelmanagementsystem.entity.Complaint;
import com.mycompany.hostelmanagementsystem.entity.FeePayment;
import jakarta.persistence.EntityManager;

public class DashboardDAO {

    public long countStudents() {
        return count("SELECT COUNT(s) FROM Student s");
    }

    public long countRooms() {
        return count("SELECT COUNT(r) FROM Room r");
    }

    public long countAvailableRooms() {
        return count("SELECT COUNT(r) FROM Room r WHERE r.currentOccupancy < r.capacity");
    }

    public long countPendingFees() {
        return count("SELECT COUNT(f) FROM FeePayment f WHERE f.status = 'PENDING'");
    }

    public long countOpenComplaints() {
        return count("SELECT COUNT(c) FROM Complaint c WHERE c.status = 'OPEN'");
    }

    private long count(String jpql) {
        EntityManager em = JPAUtil.createEntityManager();
        try {
            return em.createQuery(jpql, Long.class).getSingleResult();
        } finally {
            em.close();
        }
    }
}
