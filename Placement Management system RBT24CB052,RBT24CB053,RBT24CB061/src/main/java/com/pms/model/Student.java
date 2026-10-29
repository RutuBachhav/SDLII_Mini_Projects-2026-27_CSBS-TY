package com.pms.model;

import java.math.BigDecimal;

/**
 * Read-only view of a student profile (students + users.email).
 *
 * <p>Plain JavaBean (not a record): GlassFish's EL implementation resolves
 * JSP properties such as {@code ${student.fullName}} only through
 * {@code getFullName()} getters.
 */
public final class Student {

    private final long studentId;
    private final long userId;
    private final String rollNumber;
    private final String fullName;
    private final String branch;
    private final BigDecimal cgpa;
    private final Integer graduationYear;
    private final String skills;
    private final String phone;
    private final String email;

    public Student(long studentId, long userId, String rollNumber, String fullName,
            String branch, BigDecimal cgpa, Integer graduationYear,
            String skills, String phone, String email) {
        this.studentId = studentId;
        this.userId = userId;
        this.rollNumber = rollNumber;
        this.fullName = fullName;
        this.branch = branch;
        this.cgpa = cgpa;
        this.graduationYear = graduationYear;
        this.skills = skills;
        this.phone = phone;
        this.email = email;
    }

    public long getStudentId() {
        return studentId;
    }

    public long getUserId() {
        return userId;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public String getBranch() {
        return branch;
    }

    public BigDecimal getCgpa() {
        return cgpa;
    }

    public Integer getGraduationYear() {
        return graduationYear;
    }

    public String getSkills() {
        return skills;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }
}
