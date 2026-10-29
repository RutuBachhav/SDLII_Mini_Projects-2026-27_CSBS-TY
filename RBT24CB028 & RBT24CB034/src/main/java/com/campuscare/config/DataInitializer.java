package com.campuscare.config;

import com.campuscare.entity.*;
import com.campuscare.security.PasswordUtil;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
@Startup
public class DataInitializer {

    private static final Logger LOGGER = Logger.getLogger(DataInitializer.class.getName());

    @PersistenceContext(unitName = "CampusCarePU")
    private EntityManager em;

    @PostConstruct
    public void init() {
        try {
            java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("Asia/Kolkata"));
            Long userCount = em.createQuery("SELECT COUNT(u) FROM User u", Long.class).getSingleResult();
            if (userCount != null && userCount > 0) {
                LOGGER.info("CampusCare database already contains data. Skipping initial seeding.");
                return;
            }

            LOGGER.info("Seeding initial CampusCare demo data...");

            // 1. Departments
            Department csDept = new Department("Computer & IT Labs", "Handles computers, network equipment, and software lab facilities");
            Department elecDept = new Department("Electrical Maintenance", "Handles campus power supply, lighting, switchboards, and air conditioning");
            Department infraDept = new Department("Civil & Infrastructure", "Handles building infrastructure, furniture, washrooms, and painting");
            Department waterDept = new Department("Water Supply & Sanitation", "Handles drinking water coolers, RO filters, plumbing, and washrooms");
            Department libDept = new Department("Library Services", "Handles library books, reading hall amenities, and digital subscriptions");

            em.persist(csDept);
            em.persist(elecDept);
            em.persist(infraDept);
            em.persist(waterDept);
            em.persist(libDept);
            em.flush();

            // 2. Admin User
            User adminUser = new User(
                    "System Administrator",
                    "admin@campuscare.com",
                    PasswordUtil.hashPassword("admin123"),
                    "ADMIN",
                    "+91 9876543210"
            );
            em.persist(adminUser);
            em.flush();

            // 3. Staff Users
            User staffUser1 = new User(
                    "Prof. Rajesh Sharma",
                    "staff@campuscare.com",
                    PasswordUtil.hashPassword("staff123"),
                    "STAFF",
                    "+91 9823456781"
            );
            em.persist(staffUser1);
            em.flush();

            Staff staff1 = new Staff(staffUser1, csDept, "Senior Lab Incharge");
            em.persist(staff1);

            User staffUser2 = new User(
                    "Sunil Verma",
                    "electrical.staff@campuscare.com",
                    PasswordUtil.hashPassword("staff123"),
                    "STAFF",
                    "+91 9834567892"
            );
            em.persist(staffUser2);
            em.flush();

            Staff staff2 = new Staff(staffUser2, elecDept, "Maintenance Supervisor");
            em.persist(staff2);
            em.flush();

            // 4. Student Users
            User studentUser1 = new User(
                    "Aarav Patel",
                    "student@campuscare.com",
                    PasswordUtil.hashPassword("student123"),
                    "STUDENT",
                    "+91 9912345678"
            );
            em.persist(studentUser1);
            em.flush();

            Student student1 = new Student(studentUser1, "CS-2024-001", "Computer Engineering", "3rd Year", "A");
            em.persist(student1);

            User studentUser2 = new User(
                    "Neha Kulkarni",
                    "neha.student@campuscare.com",
                    PasswordUtil.hashPassword("student123"),
                    "STUDENT",
                    "+91 9923456789"
            );
            em.persist(studentUser2);
            em.flush();

            Student student2 = new Student(studentUser2, "IT-2024-042", "Information Technology", "2nd Year", "B");
            em.persist(student2);
            em.flush();

            // 5. Initial Complaints with Lifecycles
            // Complaint 1: IN_PROGRESS
            Complaint c1 = new Complaint(
                    student1,
                    csDept,
                    "Lab 3 Projector not displaying HDMI signal",
                    "The overhead projector in Lab 3 keeps losing HDMI signal during lectures. It flickers off every 5 minutes.",
                    "Computer/Lab",
                    "HIGH"
            );
            c1.setStatus("IN_PROGRESS");
            c1.setAssignedStaff(staff1);
            em.persist(c1);
            em.flush();

            ComplaintUpdate u1_1 = new ComplaintUpdate(c1, null, "Complaint submitted by student", "PENDING");
            ComplaintUpdate u1_2 = new ComplaintUpdate(c1, staff1, "Assigned to Prof. Rajesh Sharma by Admin", "ASSIGNED");
            ComplaintUpdate u1_3 = new ComplaintUpdate(c1, staff1, "Inspected projector; replacing faulty HDMI cable this afternoon", "IN_PROGRESS");
            em.persist(u1_1);
            em.persist(u1_2);
            em.persist(u1_3);

            // Complaint 2: ASSIGNED
            Complaint c2 = new Complaint(
                    student2,
                    elecDept,
                    "Flickering tube light in Classroom 204",
                    "Two fluorescent lights in the front row of Classroom 204 are buzzing and flickering constantly.",
                    "Electricity",
                    "MEDIUM"
            );
            c2.setStatus("ASSIGNED");
            c2.setAssignedStaff(staff2);
            em.persist(c2);
            em.flush();

            ComplaintUpdate u2_1 = new ComplaintUpdate(c2, null, "Complaint submitted by student", "PENDING");
            ComplaintUpdate u2_2 = new ComplaintUpdate(c2, staff2, "Assigned to Sunil Verma by Admin", "ASSIGNED");
            em.persist(u2_1);
            em.persist(u2_2);

            // Complaint 3: PENDING
            Complaint c3 = new Complaint(
                    student1,
                    csDept,
                    "Wi-Fi disconnection issues in Central Library",
                    "Campus Wi-Fi AP-Library-East disconnects after every 10 minutes when students attempt online research.",
                    "Wi-Fi/Internet",
                    "URGENT"
            );
            c3.setStatus("PENDING");
            em.persist(c3);
            em.flush();

            ComplaintUpdate u3_1 = new ComplaintUpdate(c3, null, "Complaint lodged by student and pending administrative assignment", "PENDING");
            em.persist(u3_1);

            // Complaint 4: RESOLVED
            Complaint c4 = new Complaint(
                    student2,
                    waterDept,
                    "Water cooler filter replacement on 2nd floor",
                    "The water flow from the water cooler near staircase 2 was very low and filter indicator was blinking.",
                    "Water",
                    "LOW"
            );
            c4.setStatus("RESOLVED");
            c4.setAssignedStaff(staff2);
            c4.setResolvedAt(new Date());
            em.persist(c4);
            em.flush();

            ComplaintUpdate u4_1 = new ComplaintUpdate(c4, null, "Complaint submitted by student", "PENDING");
            ComplaintUpdate u4_2 = new ComplaintUpdate(c4, staff2, "Assigned to Sunil Verma", "ASSIGNED");
            ComplaintUpdate u4_3 = new ComplaintUpdate(c4, staff2, "Work initiated: filter replaced and tested", "IN_PROGRESS");
            ComplaintUpdate u4_4 = new ComplaintUpdate(c4, staff2, "Filter cartridge replaced and water purity tested. Normal flow restored.", "RESOLVED");
            em.persist(u4_1);
            em.persist(u4_2);
            em.persist(u4_3);
            em.persist(u4_4);

            // 6. Initial Notifications
            em.persist(new Notification(student1.getUser(), "Your complaint #1 ('Lab 3 Projector') is currently IN_PROGRESS."));
            em.persist(new Notification(staff1.getUser(), "New complaint #1 ('Lab 3 Projector') has been assigned to you."));
            em.persist(new Notification(student2.getUser(), "Your complaint #4 ('Water cooler filter replacement') has been RESOLVED."));
            em.persist(new Notification(adminUser, "Welcome to CampusCare Administration Portal!"));

            LOGGER.info("CampusCare demo data initialized successfully!");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing demo data", e);
        }
    }
}
