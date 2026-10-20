package com.hospital.module;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "doctors")
public class Doctor implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "doctor_name", nullable = false, columnDefinition = "VARCHAR(255)")
    private String name;

    @Column(name = "department", nullable = false, columnDefinition = "VARCHAR(255)")
    private String department;

    /** Login/contact email. Unique when present; null for legacy rows. */
    @Column(name = "email", unique = true, columnDefinition = "VARCHAR(255)")
    private String email;

    @Column(name = "phone", columnDefinition = "VARCHAR(32)")
    private String phone;

    @Column(name = "qualification", columnDefinition = "VARCHAR(255)")
    private String qualification;

    @Column(name = "bio", columnDefinition = "VARCHAR(2000)")
    private String bio;

    /** Consultation fee in whole rupees. Null means "not specified". */
    @Column(name = "consultation_fee")
    private Integer consultationFee;

    /** Working day start, 24h "HH:mm". */
    @Column(name = "work_start", columnDefinition = "VARCHAR(8)")
    private String workStart = "09:00";

    /** Working day end (exclusive), 24h "HH:mm". */
    @Column(name = "work_end", columnDefinition = "VARCHAR(8)")
    private String workEnd = "17:00";

    /** Slot length in minutes. */
    @Column(name = "slot_minutes")
    private Integer slotMinutes = 30;

    /** When false the doctor is hidden from booking and new bookings are blocked. */
    @Column(name = "active", nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "assignedDoctor", cascade = CascadeType.ALL)
    private List<Patient> patients = new ArrayList<>();

    public Doctor() {
    }

    public Doctor(String name, String department) {
        this.name = name;
        this.department = department;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public Integer getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(Integer consultationFee) {
        this.consultationFee = consultationFee;
    }

    public String getWorkStart() {
        return workStart;
    }

    public void setWorkStart(String workStart) {
        this.workStart = workStart;
    }

    public String getWorkEnd() {
        return workEnd;
    }

    public void setWorkEnd(String workEnd) {
        this.workEnd = workEnd;
    }

    public Integer getSlotMinutes() {
        return slotMinutes;
    }

    public void setSlotMinutes(Integer slotMinutes) {
        this.slotMinutes = slotMinutes;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<Patient> getPatients() {
        return patients;
    }

    public void setPatients(List<Patient> patients) {
        this.patients = patients;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Doctor)) {
            return false;
        }
        Doctor other = (Doctor) object;
        return (this.id != null || other.id == null) && (this.id == null || this.id.equals(other.id));
    }

    @Override
    public String toString() {
        return name + " (" + department + ")";
    }
}
