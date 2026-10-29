package com.campuscare.ejb;

import com.campuscare.dto.DepartmentDto;
import com.campuscare.entity.Department;
import com.campuscare.exception.AppException;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Stateless
public class DepartmentServiceBean {

    @PersistenceContext(unitName = "CampusCarePU")
    private EntityManager em;

    public List<DepartmentDto> getAllDepartments() {
        TypedQuery<Department> query = em.createQuery(
                "SELECT d FROM Department d ORDER BY d.departmentName ASC",
                Department.class
        );
        List<Department> list = query.getResultList();
        List<DepartmentDto> dtos = new ArrayList<>();

        for (Department d : list) {
            DepartmentDto dto = new DepartmentDto(d.getId(), d.getDepartmentName(), d.getDescription());
            Long staffCount = em.createQuery(
                    "SELECT COUNT(s) FROM Staff s WHERE s.department.id = :deptId", Long.class)
                    .setParameter("deptId", d.getId())
                    .getSingleResult();
            Long complaintCount = em.createQuery(
                    "SELECT COUNT(c) FROM Complaint c WHERE c.department.id = :deptId", Long.class)
                    .setParameter("deptId", d.getId())
                    .getSingleResult();
            dto.setStaffCount(staffCount != null ? staffCount : 0L);
            dto.setComplaintCount(complaintCount != null ? complaintCount : 0L);
            dtos.add(dto);
        }
        return dtos;
    }

    public Department getDepartmentEntity(Long id) {
        if (id == null) {
            return null;
        }
        return em.find(Department.class, id);
    }

    public DepartmentDto getDepartmentById(Long id) {
        Department d = em.find(Department.class, id);
        if (d == null) {
            throw new AppException("Department not found with ID: " + id, Response.Status.NOT_FOUND);
        }
        DepartmentDto dto = new DepartmentDto(d.getId(), d.getDepartmentName(), d.getDescription());
        Long staffCount = em.createQuery(
                "SELECT COUNT(s) FROM Staff s WHERE s.department.id = :deptId", Long.class)
                .setParameter("deptId", d.getId())
                .getSingleResult();
        Long complaintCount = em.createQuery(
                "SELECT COUNT(c) FROM Complaint c WHERE c.department.id = :deptId", Long.class)
                .setParameter("deptId", d.getId())
                .getSingleResult();
        dto.setStaffCount(staffCount != null ? staffCount : 0L);
        dto.setComplaintCount(complaintCount != null ? complaintCount : 0L);
        return dto;
    }

    public DepartmentDto createDepartment(DepartmentDto dto) {
        if (dto.getDepartmentName() == null || dto.getDepartmentName().trim().isEmpty()) {
            throw new AppException("Department name is required");
        }
        // Check uniqueness
        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(d) FROM Department d WHERE LOWER(d.departmentName) = LOWER(:name)", Long.class)
                .setParameter("name", dto.getDepartmentName().trim());
        if (query.getSingleResult() > 0) {
            throw new AppException("A department with this name already exists");
        }

        Department department = new Department(dto.getDepartmentName().trim(), dto.getDescription());
        em.persist(department);
        em.flush();
        return new DepartmentDto(department.getId(), department.getDepartmentName(), department.getDescription());
    }

    public DepartmentDto updateDepartment(Long id, DepartmentDto dto) {
        Department d = em.find(Department.class, id);
        if (d == null) {
            throw new AppException("Department not found with ID: " + id, Response.Status.NOT_FOUND);
        }
        if (dto.getDepartmentName() != null && !dto.getDepartmentName().trim().isEmpty()) {
            // Check uniqueness if name changed
            if (!d.getDepartmentName().equalsIgnoreCase(dto.getDepartmentName().trim())) {
                TypedQuery<Long> query = em.createQuery(
                        "SELECT COUNT(x) FROM Department x WHERE LOWER(x.departmentName) = LOWER(:name) AND x.id <> :id", Long.class)
                        .setParameter("name", dto.getDepartmentName().trim())
                        .setParameter("id", id);
                if (query.getSingleResult() > 0) {
                    throw new AppException("A department with this name already exists");
                }
            }
            d.setDepartmentName(dto.getDepartmentName().trim());
        }
        if (dto.getDescription() != null) {
            d.setDescription(dto.getDescription());
        }
        em.merge(d);
        return new DepartmentDto(d.getId(), d.getDepartmentName(), d.getDescription());
    }
}
