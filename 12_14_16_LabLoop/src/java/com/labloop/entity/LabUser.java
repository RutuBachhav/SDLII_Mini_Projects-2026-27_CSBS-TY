package com.labloop.entity;

import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** Anyone who uses LabLoop: students, faculty, lab assistants, admins. ("USER" is a reserved SQL word.) */
@Entity
@Table(name = "LAB_USER")
public class LabUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Full name is required")
    @Size(min = 3, max = 60, message = "Name must be 3-60 characters")
    @Column(nullable = false, length = 60)
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Column(nullable = false, unique = true, length = 80)
    private String email;

    @NotBlank(message = "Roll number / staff ID is required")
    @Size(max = 20)
    @Column(nullable = false, length = 20)
    private String memberId;

    @NotBlank(message = "Department is required")
    @Size(max = 40)
    @Column(length = 40)
    private String department;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role = UserRole.STUDENT;

    /** Salted SHA-256 hash. Never sent out through the REST API. */
    @JsonbTransient
    @Column(nullable = false, length = 100)
    private String passwordHash;

    @OneToMany(mappedBy = "requestedBy")
    @JsonbTransient
    private List<Booking> bookings = new ArrayList<>();

    public LabUser() { }

    public LabUser(String fullName, String email, String memberId, String department, UserRole role) {
        this.fullName = fullName;
        this.email = email;
        this.memberId = memberId;
        this.department = department;
        this.role = role;
    }

    @JsonbTransient public boolean isStaff() { return role != null && role.isStaff(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    @JsonbTransient public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public List<Booking> getBookings() { return bookings; }

    @Override
    public int hashCode() { return id != null ? id.hashCode() : 0; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LabUser)) return false;
        LabUser other = (LabUser) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public String toString() { return "LabUser[" + email + "]"; }
}
