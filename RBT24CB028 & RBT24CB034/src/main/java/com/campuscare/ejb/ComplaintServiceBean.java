package com.campuscare.ejb;

import com.campuscare.dto.ComplaintRequest;
import com.campuscare.dto.ComplaintResponse;
import com.campuscare.dto.ComplaintUpdateRequest;
import com.campuscare.entity.*;
import com.campuscare.exception.AppException;
import com.campuscare.messaging.ComplaintEvent;
import com.campuscare.messaging.ComplaintJmsProducer;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Stateless
public class ComplaintServiceBean {

    @PersistenceContext(unitName = "CampusCarePU")
    private EntityManager em;

    @Inject
    private ComplaintJmsProducer jmsProducer;

    public ComplaintResponse createComplaint(Long studentUserId, ComplaintRequest req) {
        if (studentUserId == null) {
            throw new AppException("User not authenticated", Response.Status.UNAUTHORIZED);
        }

        Student student;
        try {
            student = em.createQuery("SELECT s FROM Student s WHERE s.user.id = :uid", Student.class)
                    .setParameter("uid", studentUserId)
                    .getSingleResult();
        } catch (Exception e) {
            throw new AppException("Student profile not found for user", Response.Status.FORBIDDEN);
        }

        Department dept = null;
        if (req.getDepartmentId() != null) {
            dept = em.find(Department.class, req.getDepartmentId());
        }

        Complaint complaint = new Complaint(
                student,
                dept,
                req.getTitle().trim(),
                req.getDescription().trim(),
                req.getCategory().trim(),
                req.getPriority() != null ? req.getPriority().trim().toUpperCase() : "MEDIUM"
        );
        complaint.setStatus("PENDING");
        Date now = new Date();
        complaint.setCreatedAt(now);
        complaint.setUpdatedAt(now);

        em.persist(complaint);
        em.flush();

        // Add initial lifecycle history entry
        ComplaintUpdate update = new ComplaintUpdate(
                complaint,
                null,
                "Complaint lodged by student (" + student.getUser().getName() + ")",
                "PENDING"
        );
        update.setCreatedAt(now);
        em.persist(update);
        em.flush();

        // Send JMS Asynchronous Message
        ComplaintEvent event = new ComplaintEvent(
                "CREATED",
                complaint.getId(),
                complaint.getTitle(),
                student.getUser().getId(),
                "Your complaint #" + complaint.getId() + " ('" + complaint.getTitle() + "') was submitted and is PENDING review."
        );
        jmsProducer.sendEvent(event);

        return mapToResponse(complaint);
    }

    public ComplaintResponse getComplaintById(Long id) {
        Complaint complaint = em.find(Complaint.class, id);
        if (complaint == null) {
            throw new AppException("Complaint not found with ID: " + id, Response.Status.NOT_FOUND);
        }
        return mapToResponse(complaint);
    }

    public List<ComplaintResponse> getAllComplaints(String status, String category, String priority, Long deptId) {
        StringBuilder sb = new StringBuilder("SELECT c FROM Complaint c WHERE 1=1");
        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status)) {
            sb.append(" AND c.status = :status");
        }
        if (category != null && !category.trim().isEmpty() && !"ALL".equalsIgnoreCase(category)) {
            sb.append(" AND c.category = :category");
        }
        if (priority != null && !priority.trim().isEmpty() && !"ALL".equalsIgnoreCase(priority)) {
            sb.append(" AND c.priority = :priority");
        }
        if (deptId != null && deptId > 0) {
            sb.append(" AND c.department.id = :deptId");
        }
        sb.append(" ORDER BY c.id DESC");

        TypedQuery<Complaint> query = em.createQuery(sb.toString(), Complaint.class);
        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status)) {
            query.setParameter("status", status.trim().toUpperCase());
        }
        if (category != null && !category.trim().isEmpty() && !"ALL".equalsIgnoreCase(category)) {
            query.setParameter("category", category.trim());
        }
        if (priority != null && !priority.trim().isEmpty() && !"ALL".equalsIgnoreCase(priority)) {
            query.setParameter("priority", priority.trim().toUpperCase());
        }
        if (deptId != null && deptId > 0) {
            query.setParameter("deptId", deptId);
        }

        List<Complaint> list = query.getResultList();
        List<ComplaintResponse> responses = new ArrayList<>();
        for (Complaint c : list) {
            responses.add(mapToResponse(c));
        }
        return responses;
    }

    public List<ComplaintResponse> getComplaintsByStudent(Long studentId) {
        TypedQuery<Complaint> query = em.createQuery(
                "SELECT c FROM Complaint c WHERE c.student.id = :sid ORDER BY c.id DESC", Complaint.class);
        query.setParameter("sid", studentId);
        List<Complaint> list = query.getResultList();
        List<ComplaintResponse> responses = new ArrayList<>();
        for (Complaint c : list) {
            responses.add(mapToResponse(c));
        }
        return responses;
    }

    public List<ComplaintResponse> getComplaintsByStaff(Long staffId) {
        TypedQuery<Complaint> query = em.createQuery(
                "SELECT c FROM Complaint c WHERE c.assignedStaff.id = :sid ORDER BY c.id DESC", Complaint.class);
        query.setParameter("sid", staffId);
        List<Complaint> list = query.getResultList();
        List<ComplaintResponse> responses = new ArrayList<>();
        for (Complaint c : list) {
            responses.add(mapToResponse(c));
        }
        return responses;
    }

    public ComplaintResponse assignComplaint(Long complaintId, Long staffId, String priority, Long adminUserId) {
        Complaint complaint = em.find(Complaint.class, complaintId);
        if (complaint == null) {
            throw new AppException("Complaint not found with ID: " + complaintId, Response.Status.NOT_FOUND);
        }

        Staff staff = em.find(Staff.class, staffId);
        if (staff == null) {
            throw new AppException("Staff member not found with ID: " + staffId, Response.Status.NOT_FOUND);
        }

        complaint.setAssignedStaff(staff);
        complaint.setStatus("ASSIGNED");
        Date now = new Date();
        complaint.setUpdatedAt(now);
        if (priority != null && !priority.trim().isEmpty()) {
            complaint.setPriority(priority.trim().toUpperCase());
        }
        if (staff.getDepartment() != null) {
            complaint.setDepartment(staff.getDepartment());
        }
        em.merge(complaint);

        // Record update entry
        ComplaintUpdate update = new ComplaintUpdate(
                complaint,
                staff,
                "Assigned to staff: " + staff.getUser().getName() + " (" + staff.getDesignation() + ")",
                "ASSIGNED"
        );
        update.setCreatedAt(now);
        em.persist(update);

        // Send JMS notification to assigned staff
        ComplaintEvent staffEvent = new ComplaintEvent(
                "ASSIGNED",
                complaint.getId(),
                complaint.getTitle(),
                staff.getUser().getId(),
                "New complaint #" + complaint.getId() + " ('" + complaint.getTitle() + "') has been assigned to you."
        );
        jmsProducer.sendEvent(staffEvent);

        // Send JMS notification to student
        ComplaintEvent studentEvent = new ComplaintEvent(
                "ASSIGNED",
                complaint.getId(),
                complaint.getTitle(),
                complaint.getStudent().getUser().getId(),
                "Your complaint #" + complaint.getId() + " has been assigned to " + staff.getUser().getName() + "."
        );
        jmsProducer.sendEvent(studentEvent);

        return mapToResponse(complaint);
    }

    public ComplaintResponse updateComplaintStatus(Long complaintId, String newStatus, String remarks, Long userId, String userRole) {
        Complaint complaint = em.find(Complaint.class, complaintId);
        if (complaint == null) {
            throw new AppException("Complaint not found with ID: " + complaintId, Response.Status.NOT_FOUND);
        }

        String targetStatus = newStatus.trim().toUpperCase();
        validateStatusTransition(complaint.getStatus(), targetStatus, userRole);

        Date now = new Date();
        complaint.setStatus(targetStatus);
        complaint.setUpdatedAt(now);
        if ("RESOLVED".equals(targetStatus)) {
            complaint.setResolvedAt(now);
        }
        em.merge(complaint);

        Staff updatingStaff = null;
        if ("STAFF".equalsIgnoreCase(userRole)) {
            try {
                updatingStaff = em.createQuery("SELECT s FROM Staff s WHERE s.user.id = :uid", Staff.class)
                        .setParameter("uid", userId)
                        .getSingleResult();
            } catch (Exception ignored) {
            }
        }

        String message = (remarks != null && !remarks.trim().isEmpty())
                ? "Status updated to " + targetStatus + ": " + remarks.trim()
                : "Status updated to " + targetStatus;

        ComplaintUpdate update = new ComplaintUpdate(complaint, updatingStaff, message, targetStatus);
        update.setCreatedAt(now);
        em.persist(update);

        // Notify student of progress
        ComplaintEvent studentEvent = new ComplaintEvent(
                targetStatus,
                complaint.getId(),
                complaint.getTitle(),
                complaint.getStudent().getUser().getId(),
                "Your complaint #" + complaint.getId() + " status is now: " + targetStatus + (remarks != null ? " (" + remarks + ")" : "")
        );
        jmsProducer.sendEvent(studentEvent);

        return mapToResponse(complaint);
    }

    public ComplaintResponse addComplaintUpdate(Long complaintId, ComplaintUpdateRequest req, Long userId, String userRole) {
        Complaint complaint = em.find(Complaint.class, complaintId);
        if (complaint == null) {
            throw new AppException("Complaint not found with ID: " + complaintId, Response.Status.NOT_FOUND);
        }

        Staff updatingStaff = null;
        if ("STAFF".equalsIgnoreCase(userRole)) {
            try {
                updatingStaff = em.createQuery("SELECT s FROM Staff s WHERE s.user.id = :uid", Staff.class)
                        .setParameter("uid", userId)
                        .getSingleResult();
            } catch (Exception ignored) {
            }
        }

        Date now = new Date();
        if (req.getNewStatus() != null && !req.getNewStatus().trim().isEmpty()) {
            String targetStatus = req.getNewStatus().trim().toUpperCase();
            validateStatusTransition(complaint.getStatus(), targetStatus, userRole);
            complaint.setStatus(targetStatus);
            complaint.setUpdatedAt(now);
            if ("RESOLVED".equals(targetStatus)) {
                complaint.setResolvedAt(now);
            }
            em.merge(complaint);
        }

        ComplaintUpdate update = new ComplaintUpdate(
                complaint,
                updatingStaff,
                req.getMessage().trim(),
                complaint.getStatus()
        );
        update.setCreatedAt(now);
        em.persist(update);

        // Notify student
        ComplaintEvent event = new ComplaintEvent(
                "UPDATE",
                complaint.getId(),
                complaint.getTitle(),
                complaint.getStudent().getUser().getId(),
                "New update on complaint #" + complaint.getId() + ": " + req.getMessage().trim()
        );
        jmsProducer.sendEvent(event);

        return mapToResponse(complaint);
    }

    public void deleteComplaint(Long id, Long userId, String userRole) {
        Complaint complaint = em.find(Complaint.class, id);
        if (complaint == null) {
            throw new AppException("Complaint not found with ID: " + id, Response.Status.NOT_FOUND);
        }

        if ("STUDENT".equalsIgnoreCase(userRole)) {
            if (!complaint.getStudent().getUser().getId().equals(userId)) {
                throw new AppException("You can only delete your own complaints", Response.Status.FORBIDDEN);
            }
            if (!"PENDING".equalsIgnoreCase(complaint.getStatus())) {
                throw new AppException("Cannot delete a complaint that is already in progress or resolved", Response.Status.BAD_REQUEST);
            }
        } else if (!"ADMIN".equalsIgnoreCase(userRole)) {
            throw new AppException("Unauthorized to delete complaints", Response.Status.FORBIDDEN);
        }

        em.remove(complaint);
    }

    private void validateStatusTransition(String current, String next, String role) {
        if (current.equalsIgnoreCase(next)) {
            return;
        }

        // Student can close their resolved complaint
        if ("STUDENT".equalsIgnoreCase(role)) {
            if ("CLOSED".equalsIgnoreCase(next) && "RESOLVED".equalsIgnoreCase(current)) {
                return;
            }
            throw new AppException("Students can only close RESOLVED complaints", Response.Status.FORBIDDEN);
        }

        // Staff lifecycle: ASSIGNED -> IN_PROGRESS -> RESOLVED
        if ("STAFF".equalsIgnoreCase(role)) {
            if ("ASSIGNED".equalsIgnoreCase(current) && "IN_PROGRESS".equalsIgnoreCase(next)) {
                return;
            }
            if ("IN_PROGRESS".equalsIgnoreCase(current) && "RESOLVED".equalsIgnoreCase(next)) {
                return;
            }
            if ("ASSIGNED".equalsIgnoreCase(current) && "RESOLVED".equalsIgnoreCase(next)) {
                return;
            }
            throw new AppException("Invalid status transition for staff: " + current + " -> " + next, Response.Status.BAD_REQUEST);
        }

        // Admin can perform transitions: PENDING -> ASSIGNED, etc.
        if ("ADMIN".equalsIgnoreCase(role)) {
            return; // Admin can manage transitions
        }
    }

    public ComplaintResponse mapToResponse(Complaint c) {
        ComplaintResponse dto = new ComplaintResponse();
        dto.setId(c.getId());
        dto.setTitle(c.getTitle());
        dto.setDescription(c.getDescription());
        dto.setCategory(c.getCategory());
        dto.setPriority(c.getPriority());
        dto.setStatus(c.getStatus());
        dto.setCreatedAt(c.getCreatedAt());
        dto.setUpdatedAt(c.getUpdatedAt());
        dto.setResolvedAt(c.getResolvedAt());

        if (c.getStudent() != null) {
            dto.setStudentId(c.getStudent().getId());
            if (c.getStudent().getUser() != null) {
                dto.setStudentName(c.getStudent().getUser().getName());
            }
            dto.setRollNumber(c.getStudent().getRollNumber());
            dto.setBranch(c.getStudent().getBranch());
            dto.setYear(c.getStudent().getYear());
        }

        if (c.getDepartment() != null) {
            dto.setDepartmentId(c.getDepartment().getId());
            dto.setDepartmentName(c.getDepartment().getDepartmentName());
        }

        if (c.getAssignedStaff() != null) {
            dto.setAssignedStaffId(c.getAssignedStaff().getId());
            if (c.getAssignedStaff().getUser() != null) {
                dto.setAssignedStaffName(c.getAssignedStaff().getUser().getName());
            }
            dto.setAssignedStaffDesignation(c.getAssignedStaff().getDesignation());
        }

        // Map updates
        TypedQuery<ComplaintUpdate> uq = em.createQuery(
                "SELECT u FROM ComplaintUpdate u WHERE u.complaint.id = :cid ORDER BY u.createdAt ASC",
                ComplaintUpdate.class);
        uq.setParameter("cid", c.getId());
        List<ComplaintUpdate> updates = uq.getResultList();

        for (ComplaintUpdate u : updates) {
            String staffName = (u.getStaff() != null && u.getStaff().getUser() != null) ? u.getStaff().getUser().getName() : "System / Student";
            Long staffId = (u.getStaff() != null) ? u.getStaff().getId() : null;
            dto.getUpdates().add(new ComplaintResponse.ComplaintUpdateDto(
                    u.getId(),
                    staffId,
                    staffName,
                    u.getMessage(),
                    u.getStatus(),
                    u.getCreatedAt()
            ));
        }

        return dto;
    }
}
