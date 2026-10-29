package com.mycompany.hostelmanagementsystem;

import com.mycompany.hostelmanagementsystem.entity.Complaint;
import com.mycompany.hostelmanagementsystem.entity.Student;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;

public class ComplaintDAO {

    public List<Complaint> getAllComplaints() {
        EntityManager em = JPAUtil.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT c FROM Complaint c ORDER BY c.id DESC",
                    Complaint.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Complaint> getComplaintsByStudent(Integer studentId) {
        EntityManager em = JPAUtil.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT c FROM Complaint c WHERE c.student.id = :studentId ORDER BY c.id DESC",
                    Complaint.class)
                    .setParameter("studentId", studentId)
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

    public void addComplaint(Integer studentId, String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new RuntimeException("Complaint description is required.");
        }

        EntityManager em = JPAUtil.createEntityManager();

        try {
            em.getTransaction().begin();

            Student student = em.find(Student.class, studentId);
            if (student == null) {
                throw new RuntimeException("Student not found.");
            }

            Complaint complaint = new Complaint();
            complaint.setStudent(student);
            complaint.setDescription(description.trim());
            complaint.setStatus("OPEN");
            complaint.setDateRaised(LocalDate.now());

            em.persist(complaint);
            em.getTransaction().commit();

        } catch (RuntimeException e) {
            rollback(em);
            throw e;
        } finally {
            em.close();
        }
    }

    public void resolveComplaint(Integer complaintId) {
        EntityManager em = JPAUtil.createEntityManager();
        try {
            em.getTransaction().begin();

            Complaint complaint = em.find(Complaint.class, complaintId);
            if (complaint != null) {
                complaint.setStatus("RESOLVED");
                complaint.setDateResolved(LocalDate.now());
            }

            em.getTransaction().commit();
        } catch (RuntimeException e) {
            rollback(em);
            throw e;
        } finally {
            em.close();
        }
    }

    public void deleteComplaint(Integer complaintId) {
        EntityManager em = JPAUtil.createEntityManager();
        try {
            em.getTransaction().begin();

            Complaint complaint = em.find(Complaint.class, complaintId);
            if (complaint != null) {
                em.remove(complaint);
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
