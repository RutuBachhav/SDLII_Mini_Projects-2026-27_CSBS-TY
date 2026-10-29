package com.labloop.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.sql.DataSourceDefinition;
import jakarta.ejb.EJB;
import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import java.util.logging.Logger;

/**
 * SINGLETON SESSION BEAN - exactly ONE instance for the whole application, shared by all users
 * (same idea as EmployeeSingletonBean / companyName in Assignment 2B).
 *
 *  - @Startup      : created when the app is deployed, not on first use.
 *  - @PostConstruct: loads demo data into an empty database.
 *  - @Lock(READ)   : many users may read settings at the same time.
 *  - @Lock(WRITE)  : an admin changing settings gets exclusive access.
 *
 * @DataSourceDefinition registers the MySQL connection pool from code, so you do NOT have to
 * create a JDBC pool in the GlassFish admin console. Change user/password below to match your MySQL.
 */
@DataSourceDefinition(
        name = "java:app/jdbc/LabLoopDS",
        className = "com.mysql.cj.jdbc.MysqlDataSource",
        url = "jdbc:mysql://localhost:3306/labloop?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata",
        user = "root",
        password = "YOUR_MYSQL_PASSWORD",
        minPoolSize = 0,
        maxPoolSize = 20
)
@Singleton
@Startup
public class LabLoopSettingsBean {

    private static final Logger LOG = Logger.getLogger(LabLoopSettingsBean.class.getName());

    @EJB
    private DataSeeder seeder;

    private String institutionName = "Institute of Engineering & Technology";
    private int studentMaxHours = 4;
    private int facultyMaxHours = 72;
    private int maxAdvanceDays = 30;

    @PostConstruct
    void init() {
        LOG.info("[LabLoop] Singleton settings bean started - checking demo data");
        seeder.seedIfEmpty();
    }

    @Lock(LockType.READ)
    public String getInstitutionName() { return institutionName; }

    @Lock(LockType.READ)
    public int getStudentMaxHours() { return studentMaxHours; }

    @Lock(LockType.READ)
    public int getFacultyMaxHours() { return facultyMaxHours; }

    @Lock(LockType.READ)
    public int getMaxAdvanceDays() { return maxAdvanceDays; }

    /** Changes take effect for every user immediately, because there is only one instance. */
    @Lock(LockType.WRITE)
    public void update(String institutionName, int studentMaxHours, int facultyMaxHours, int maxAdvanceDays) {
        if (institutionName != null && !institutionName.isBlank()) {
            this.institutionName = institutionName.trim();
        }
        this.studentMaxHours = clamp(studentMaxHours, 1, 12);
        this.facultyMaxHours = clamp(facultyMaxHours, 1, 168);
        this.maxAdvanceDays = clamp(maxAdvanceDays, 1, 120);
    }

    private static int clamp(int v, int lo, int hi) { return Math.max(lo, Math.min(hi, v)); }
}
