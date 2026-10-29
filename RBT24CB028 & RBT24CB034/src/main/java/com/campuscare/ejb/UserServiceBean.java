package com.campuscare.ejb;

import com.campuscare.dto.AuthResponse;
import com.campuscare.dto.LoginRequest;
import com.campuscare.dto.RegisterRequest;
import com.campuscare.dto.StaffDto;
import com.campuscare.dto.StudentDto;
import com.campuscare.entity.Department;
import com.campuscare.entity.Staff;
import com.campuscare.entity.Student;
import com.campuscare.entity.User;
import com.campuscare.exception.AppException;
import com.campuscare.security.PasswordUtil;
import com.campuscare.security.TokenUtil;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Stateless
public class UserServiceBean {

    @PersistenceContext(unitName = "CampusCarePU")
    private EntityManager em;

    public AuthResponse login(LoginRequest req) {
        if (req.getEmail() == null || req.getPassword() == null) {
            throw new AppException("Email and password are required", Response.Status.BAD_REQUEST);
        }

        TypedQuery<User> query = em.createQuery(
                "SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email)", User.class);
        query.setParameter("email", req.getEmail().trim());

        User user;
        try {
            user = query.getSingleResult();
        } catch (NoResultException e) {
            throw new AppException("Invalid email or password", Response.Status.UNAUTHORIZED);
        }

        if (!PasswordUtil.verifyPassword(req.getPassword(), user.getPassword())) {
            throw new AppException("Invalid email or password", Response.Status.UNAUTHORIZED);
        }

        String token = TokenUtil.generateToken(user.getId(), user.getRole());
        AuthResponse response = new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getPhone());

        if ("STUDENT".equalsIgnoreCase(user.getRole())) {
            Student student = getStudentEntityByUserId(user.getId());
            if (student != null) {
                response.setStudentId(student.getId());
            }
        } else if ("STAFF".equalsIgnoreCase(user.getRole())) {
            Staff staff = getStaffEntityByUserId(user.getId());
            if (staff != null) {
                response.setStaffId(staff.getId());
                if (staff.getDepartment() != null) {
                    response.setDepartmentId(staff.getDepartment().getId());
                    response.setDepartmentName(staff.getDepartment().getDepartmentName());
                }
            }
        }

        return response;
    }

    public AuthResponse register(RegisterRequest req) {
        if (req.getEmail() == null || req.getPassword() == null || req.getName() == null || req.getRole() == null) {
            throw new AppException("Name, email, password, and role are required", Response.Status.BAD_REQUEST);
        }

        String role = req.getRole().trim().toUpperCase();
        if (!"STUDENT".equals(role) && !"STAFF".equals(role) && !"ADMIN".equals(role)) {
            throw new AppException("Invalid role. Role must be STUDENT, STAFF, or ADMIN", Response.Status.BAD_REQUEST);
        }

        // Check if email already registered
        TypedQuery<Long> countQuery = em.createQuery(
                "SELECT COUNT(u) FROM User u WHERE LOWER(u.email) = LOWER(:email)", Long.class);
        countQuery.setParameter("email", req.getEmail().trim());
        if (countQuery.getSingleResult() > 0) {
            throw new AppException("Email already registered: " + req.getEmail(), Response.Status.BAD_REQUEST);
        }

        String hashedPassword = PasswordUtil.hashPassword(req.getPassword());
        User user = new User(req.getName().trim(), req.getEmail().trim(), hashedPassword, role, req.getPhone());
        user.setCreatedAt(new Date());
        em.persist(user);
        em.flush();

        Long studentId = null;
        Long staffId = null;
        Long departmentId = null;
        String departmentName = null;

        if ("STUDENT".equals(role)) {
            if (req.getRollNumber() == null || req.getRollNumber().trim().isEmpty()) {
                throw new AppException("Roll number is required for students", Response.Status.BAD_REQUEST);
            }
            // Check roll number uniqueness
            TypedQuery<Long> rollCheck = em.createQuery(
                    "SELECT COUNT(s) FROM Student s WHERE LOWER(s.rollNumber) = LOWER(:roll)", Long.class);
            rollCheck.setParameter("roll", req.getRollNumber().trim());
            if (rollCheck.getSingleResult() > 0) {
                throw new AppException("Roll number already registered", Response.Status.BAD_REQUEST);
            }

            Student student = new Student(
                    user,
                    req.getRollNumber().trim(),
                    req.getBranch() != null ? req.getBranch().trim() : "General",
                    req.getYear() != null ? req.getYear().trim() : "1st Year",
                    req.getDivision() != null ? req.getDivision().trim() : "A"
            );
            em.persist(student);
            em.flush();
            studentId = student.getId();
        } else if ("STAFF".equals(role)) {
            Department dept = null;
            if (req.getDepartmentId() != null) {
                dept = em.find(Department.class, req.getDepartmentId());
                if (dept == null) {
                    throw new AppException("Invalid Department ID", Response.Status.BAD_REQUEST);
                }
                departmentId = dept.getId();
                departmentName = dept.getDepartmentName();
            }

            Staff staff = new Staff(
                    user,
                    dept,
                    req.getDesignation() != null ? req.getDesignation().trim() : "Support Staff"
            );
            em.persist(staff);
            em.flush();
            staffId = staff.getId();
        }

        String token = TokenUtil.generateToken(user.getId(), user.getRole());
        AuthResponse response = new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getPhone());
        response.setStudentId(studentId);
        response.setStaffId(staffId);
        response.setDepartmentId(departmentId);
        response.setDepartmentName(departmentName);

        return response;
    }

    public User getUserById(Long id) {
        if (id == null) {
            return null;
        }
        return em.find(User.class, id);
    }

    public Student getStudentEntityByUserId(Long userId) {
        try {
            return em.createQuery("SELECT s FROM Student s WHERE s.user.id = :uid", Student.class)
                    .setParameter("uid", userId)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public Staff getStaffEntityByUserId(Long userId) {
        try {
            return em.createQuery("SELECT s FROM Staff s WHERE s.user.id = :uid", Staff.class)
                    .setParameter("uid", userId)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<StudentDto> getAllStudents() {
        List<Student> students = em.createQuery("SELECT s FROM Student s ORDER BY s.id DESC", Student.class).getResultList();
        List<StudentDto> list = new ArrayList<>();
        for (Student s : students) {
            StudentDto dto = new StudentDto();
            dto.setId(s.getId());
            dto.setUserId(s.getUser().getId());
            dto.setName(s.getUser().getName());
            dto.setEmail(s.getUser().getEmail());
            dto.setPhone(s.getUser().getPhone());
            dto.setRollNumber(s.getRollNumber());
            dto.setBranch(s.getBranch());
            dto.setYear(s.getYear());
            dto.setDivision(s.getDivision());
            dto.setCreatedAt(s.getUser().getCreatedAt());

            Long complaintsCount = em.createQuery(
                    "SELECT COUNT(c) FROM Complaint c WHERE c.student.id = :sid", Long.class)
                    .setParameter("sid", s.getId())
                    .getSingleResult();
            dto.setTotalComplaints(complaintsCount != null ? complaintsCount : 0L);
            list.add(dto);
        }
        return list;
    }

    public List<StaffDto> getAllStaff() {
        List<Staff> staffList = em.createQuery("SELECT s FROM Staff s ORDER BY s.id DESC", Staff.class).getResultList();
        List<StaffDto> list = new ArrayList<>();
        for (Staff s : staffList) {
            StaffDto dto = new StaffDto();
            dto.setId(s.getId());
            dto.setUserId(s.getUser().getId());
            dto.setName(s.getUser().getName());
            dto.setEmail(s.getUser().getEmail());
            dto.setPhone(s.getUser().getPhone());
            if (s.getDepartment() != null) {
                dto.setDepartmentId(s.getDepartment().getId());
                dto.setDepartmentName(s.getDepartment().getDepartmentName());
            }
            dto.setDesignation(s.getDesignation());
            dto.setCreatedAt(s.getUser().getCreatedAt());

            Long assignedCount = em.createQuery(
                    "SELECT COUNT(c) FROM Complaint c WHERE c.assignedStaff.id = :sid", Long.class)
                    .setParameter("sid", s.getId())
                    .getSingleResult();
            Long resolvedCount = em.createQuery(
                    "SELECT COUNT(c) FROM Complaint c WHERE c.assignedStaff.id = :sid AND c.status = 'RESOLVED'", Long.class)
                    .setParameter("sid", s.getId())
                    .getSingleResult();
            dto.setAssignedCount(assignedCount != null ? assignedCount : 0L);
            dto.setResolvedCount(resolvedCount != null ? resolvedCount : 0L);
            list.add(dto);
        }
        return list;
    }
}
