package com.mycompany.hostelmanagementsystem.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "complaint")
public class Complaint implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "OPEN";

    @Column(name = "date_raised")
    private LocalDate dateRaised;

    @Column(name = "date_resolved")
    private LocalDate dateResolved;

    public Complaint() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getDateRaised() { return dateRaised; }
    public void setDateRaised(LocalDate dateRaised) { this.dateRaised = dateRaised; }

    public LocalDate getDateResolved() { return dateResolved; }
    public void setDateResolved(LocalDate dateResolved) { this.dateResolved = dateResolved; }
}
