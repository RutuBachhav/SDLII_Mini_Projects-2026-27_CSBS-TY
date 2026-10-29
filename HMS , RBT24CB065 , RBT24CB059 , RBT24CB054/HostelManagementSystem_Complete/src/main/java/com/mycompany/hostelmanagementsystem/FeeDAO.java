package com.mycompany.hostelmanagementsystem;

import com.mycompany.hostelmanagementsystem.entity.FeePayment;
import com.mycompany.hostelmanagementsystem.entity.Student;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;

public class FeeDAO {

    public List<FeePayment> getAllFees() {
        EntityManager em = JPAUtil.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT f FROM FeePayment f ORDER BY f.id DESC",
                    FeePayment.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Student> getAllStudents() {
        EntityManager em = JPAUtil.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT s FROM Student s ORDER BY s.name",
                    Student.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public void addFeeRecord(Integer studentId, String month,
                             java.math.BigDecimal amount) {

        if (month == null || month.trim().isEmpty()) {
            throw new RuntimeException("Month is required.");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new RuntimeException("Amount must be greater than zero.");
        }

        EntityManager em = JPAUtil.createEntityManager();

        try {
            em.getTransaction().begin();

            Student student = em.find(Student.class, studentId);
            if (student == null) {
                throw new RuntimeException("Student not found.");
            }

            Long duplicate = em.createQuery(
                    "SELECT COUNT(f) FROM FeePayment f " +
                    "WHERE f.student.id = :studentId AND LOWER(f.month) = LOWER(:month)",
                    Long.class)
                    .setParameter("studentId", studentId)
                    .setParameter("month", month.trim())
                    .getSingleResult();

            if (duplicate > 0) {
                throw new RuntimeException("Fee record for this student and month already exists.");
            }

            FeePayment fee = new FeePayment();
            fee.setStudent(student);
            fee.setMonth(month.trim());
            fee.setAmount(amount);
            fee.setStatus("PENDING");

            em.persist(fee);
            em.getTransaction().commit();

        } catch (RuntimeException e) {
            rollback(em);
            throw e;
        } finally {
            em.close();
        }
    }

    public void markAsPaid(Integer id) {
        EntityManager em = JPAUtil.createEntityManager();
        try {
            em.getTransaction().begin();

            FeePayment fee = em.find(FeePayment.class, id);
            if (fee != null) {
                fee.setStatus("PAID");
                fee.setPaymentDate(LocalDate.now());
            }

            em.getTransaction().commit();
        } catch (RuntimeException e) {
            rollback(em);
            throw e;
        } finally {
            em.close();
        }
    }

    public void deleteFeeRecord(Integer id) {
        EntityManager em = JPAUtil.createEntityManager();
        try {
            em.getTransaction().begin();

            FeePayment fee = em.find(FeePayment.class, id);
            if (fee != null) {
                em.remove(fee);
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
