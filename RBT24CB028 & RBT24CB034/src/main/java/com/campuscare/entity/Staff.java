package com.campuscare.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "staff")
public class Staff implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Associated user is required")
    @OneToOne(optional = false, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id")
    private Department department;

    @NotBlank(message = "Designation is required")
    @Column(nullable = false, length = 100)
    private String designation;

    @OneToMany(mappedBy = "assignedStaff", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private List<Complaint> assignedComplaints = new ArrayList<>();

    public Staff() {
    }

    public Staff(User user, Department department, String designation) {
        this.user = user;
        this.department = department;
        this.designation = designation;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public List<Complaint> getAssignedComplaints() {
        return assignedComplaints;
    }

    public void setAssignedComplaints(List<Complaint> assignedComplaints) {
        this.assignedComplaints = assignedComplaints;
    }
}
