package com.campuscare.ejb;

import com.campuscare.dto.DashboardStatsDto;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Stateless
public class DashboardServiceBean {

    @PersistenceContext(unitName = "CampusCarePU")
    private EntityManager em;

    public DashboardStatsDto getAdminDashboardStats() {
        DashboardStatsDto stats = new DashboardStatsDto();

        Long total = em.createQuery("SELECT COUNT(c) FROM Complaint c", Long.class).getSingleResult();
        stats.setTotalComplaints(total != null ? total : 0);

        stats.setPendingComplaints(getCountByStatus("PENDING"));
        stats.setAssignedComplaints(getCountByStatus("ASSIGNED"));
        stats.setInProgressComplaints(getCountByStatus("IN_PROGRESS"));
        stats.setResolvedComplaints(getCountByStatus("RESOLVED"));
        stats.setClosedComplaints(getCountByStatus("CLOSED"));

        Long students = em.createQuery("SELECT COUNT(s) FROM Student s", Long.class).getSingleResult();
        stats.setTotalStudents(students != null ? students : 0);

        Long staff = em.createQuery("SELECT COUNT(st) FROM Staff st", Long.class).getSingleResult();
        stats.setTotalStaff(staff != null ? staff : 0);

        Long depts = em.createQuery("SELECT COUNT(d) FROM Department d", Long.class).getSingleResult();
        stats.setTotalDepartments(depts != null ? depts : 0);

        // Group by category
        List<Object[]> catList = em.createQuery(
                "SELECT c.category, COUNT(c) FROM Complaint c GROUP BY c.category", Object[].class).getResultList();
        Map<String, Long> catMap = new HashMap<>();
        for (Object[] row : catList) {
            catMap.put((String) row[0], (Long) row[1]);
        }
        stats.setComplaintsByCategory(catMap);

        // Group by department
        List<Object[]> deptList = em.createQuery(
                "SELECT COALESCE(c.department.departmentName, 'Unassigned'), COUNT(c) FROM Complaint c GROUP BY c.department.departmentName", Object[].class).getResultList();
        Map<String, Long> deptMap = new HashMap<>();
        for (Object[] row : deptList) {
            deptMap.put((String) row[0], (Long) row[1]);
        }
        stats.setComplaintsByDepartment(deptMap);

        // Group by priority
        List<Object[]> prioList = em.createQuery(
                "SELECT c.priority, COUNT(c) FROM Complaint c GROUP BY c.priority", Object[].class).getResultList();
        Map<String, Long> prioMap = new HashMap<>();
        for (Object[] row : prioList) {
            prioMap.put((String) row[0], (Long) row[1]);
        }
        stats.setComplaintsByPriority(prioMap);

        return stats;
    }

    public DashboardStatsDto getStaffDashboardStats(Long staffId) {
        DashboardStatsDto stats = new DashboardStatsDto();

        Long total = em.createQuery(
                "SELECT COUNT(c) FROM Complaint c WHERE c.assignedStaff.id = :sid", Long.class)
                .setParameter("sid", staffId)
                .getSingleResult();
        stats.setTotalComplaints(total != null ? total : 0);

        stats.setAssignedComplaints(getCountByStaffAndStatus(staffId, "ASSIGNED"));
        stats.setInProgressComplaints(getCountByStaffAndStatus(staffId, "IN_PROGRESS"));
        stats.setResolvedComplaints(getCountByStaffAndStatus(staffId, "RESOLVED"));
        stats.setClosedComplaints(getCountByStaffAndStatus(staffId, "CLOSED"));

        return stats;
    }

    public DashboardStatsDto getStudentDashboardStats(Long studentId) {
        DashboardStatsDto stats = new DashboardStatsDto();

        Long total = em.createQuery(
                "SELECT COUNT(c) FROM Complaint c WHERE c.student.id = :sid", Long.class)
                .setParameter("sid", studentId)
                .getSingleResult();
        stats.setTotalComplaints(total != null ? total : 0);

        stats.setPendingComplaints(getCountByStudentAndStatus(studentId, "PENDING"));
        stats.setAssignedComplaints(getCountByStudentAndStatus(studentId, "ASSIGNED"));
        stats.setInProgressComplaints(getCountByStudentAndStatus(studentId, "IN_PROGRESS"));
        stats.setResolvedComplaints(getCountByStudentAndStatus(studentId, "RESOLVED"));
        stats.setClosedComplaints(getCountByStudentAndStatus(studentId, "CLOSED"));

        return stats;
    }

    private long getCountByStatus(String status) {
        Long val = em.createQuery("SELECT COUNT(c) FROM Complaint c WHERE c.status = :status", Long.class)
                .setParameter("status", status)
                .getSingleResult();
        return val != null ? val : 0;
    }

    private long getCountByStaffAndStatus(Long staffId, String status) {
        Long val = em.createQuery("SELECT COUNT(c) FROM Complaint c WHERE c.assignedStaff.id = :sid AND c.status = :status", Long.class)
                .setParameter("sid", staffId)
                .setParameter("status", status)
                .getSingleResult();
        return val != null ? val : 0;
    }

    private long getCountByStudentAndStatus(Long studentId, String status) {
        Long val = em.createQuery("SELECT COUNT(c) FROM Complaint c WHERE c.student.id = :sid AND c.status = :status", Long.class)
                .setParameter("sid", studentId)
                .setParameter("status", status)
                .getSingleResult();
        return val != null ? val : 0;
    }
}
