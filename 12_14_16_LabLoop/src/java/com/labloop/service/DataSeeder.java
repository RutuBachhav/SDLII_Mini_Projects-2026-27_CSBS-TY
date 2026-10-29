package com.labloop.service;

import com.labloop.entity.*;
import com.labloop.util.PasswordUtil;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Loads realistic demo data the first time the app starts on an empty database.
 * Every demo account uses the password  labloop123
 */
@Stateless
public class DataSeeder {

    private static final Logger LOG = Logger.getLogger(DataSeeder.class.getName());
    public static final String DEMO_PASSWORD = "labloop123";

    @PersistenceContext(unitName = "LabLoopPU")
    private EntityManager em;

    private Map<String, Equipment> eq;

    public void seedIfEmpty() {
        eq = new HashMap<>();
        long labs = em.createQuery("SELECT COUNT(l) FROM Lab l", Long.class).getSingleResult();
        if (labs > 0) {
            LOG.info("[LabLoop] Database already has data - seeding skipped");
            return;
        }
        LOG.info("[LabLoop] Empty database - loading demo data");

        // ---------- people ----------
        LabUser admin = user("Kavita Rao", "admin@labloop.edu", "EMP-1001", "Central Lab Services", UserRole.ADMIN);
        LabUser assistant = user("Mahesh Patil", "assistant@labloop.edu", "EMP-2045", "Electronics & TC", UserRole.LAB_ASSISTANT);
        LabUser faculty = user("Dr. Anjali Kulkarni", "faculty@labloop.edu", "EMP-3120", "Computer Engineering", UserRole.FACULTY);
        LabUser s1 = user("Aarav Deshmukh", "student@labloop.edu", "22CS1042", "Computer Engineering", UserRole.STUDENT);
        LabUser s2 = user("Sneha Joshi", "sneha.joshi@labloop.edu", "22EC1017", "Electronics & TC", UserRole.STUDENT);
        user("Rohan Shinde", "rohan.shinde@labloop.edu", "22AI1088", "AI & Data Science", UserRole.STUDENT);

        // ---------- labs ----------
        Lab ai = lab("CSL-101", "AI & Data Science Lab", "Block A, Room 101", 40, "Dr. Anjali Kulkarni");
        Lab iot = lab("ECL-204", "Embedded Systems & IoT Lab", "Block B, Room 204", 30, "Prof. Suresh Nair");
        Lab net = lab("NWL-110", "Computer Networks Lab", "Block A, Room 110", 36, "Prof. Meera Iyer");
        Lab rob = lab("RBL-301", "Robotics & Automation Lab", "Block C, Room 301", 24, "Dr. Vikram Sethi");

        // ---------- equipment ----------
        LocalDate today = LocalDate.now();
        item("LL-CMP-0001", "GPU Workstation", "Dell Precision / RTX 4090", EquipmentCategory.COMPUTING, ai, 385000, today.minusMonths(14), 180, today.minusDays(40));
        item("LL-CMP-0002", "GPU Workstation", "Dell Precision / RTX 4090", EquipmentCategory.COMPUTING, ai, 385000, today.minusMonths(14), 180, today.minusDays(40));
        item("LL-IOT-0003", "Jetson Orin Nano Dev Kit", "NVIDIA 8GB", EquipmentCategory.IOT, ai, 52000, today.minusMonths(8), 365, null);
        item("LL-ELC-0004", "Digital Storage Oscilloscope", "Rigol DS1054Z 50MHz", EquipmentCategory.ELECTRONICS, iot, 38500, today.minusMonths(30), 365, today.minusDays(400));
        item("LL-ELC-0005", "Function Generator", "Siglent SDG1032X", EquipmentCategory.ELECTRONICS, iot, 29900, today.minusMonths(30), 365, today.minusDays(120));
        item("LL-ELC-0006", "Logic Analyzer", "Saleae Logic 8", EquipmentCategory.ELECTRONICS, iot, 41000, today.minusMonths(20), 365, null);
        item("LL-IOT-0007", "Raspberry Pi 5 Kit", "8GB + sensor pack", EquipmentCategory.IOT, iot, 14500, today.minusMonths(6), 365, null);
        item("LL-IOT-0008", "Raspberry Pi 5 Kit", "8GB + sensor pack", EquipmentCategory.IOT, iot, 14500, today.minusMonths(6), 365, null);
        item("LL-ELC-0009", "Soldering Station", "Weller WE1010", EquipmentCategory.ELECTRONICS, iot, 11800, today.minusMonths(18), 90, today.minusDays(100));
        item("LL-NET-0010", "Managed Switch", "Cisco Catalyst 2960-X 24P", EquipmentCategory.NETWORKING, net, 68000, today.minusMonths(40), 365, today.minusDays(200));
        item("LL-NET-0011", "Router", "Cisco ISR 1941", EquipmentCategory.NETWORKING, net, 54000, today.minusMonths(40), 365, today.minusDays(200));
        item("LL-NET-0012", "Wireless Access Point", "Ubiquiti UniFi U6+", EquipmentCategory.NETWORKING, net, 13900, today.minusMonths(10), 365, null);
        item("LL-ROB-0013", "6-DOF Robotic Arm", "uFactory xArm Lite 6", EquipmentCategory.ROBOTICS, rob, 410000, today.minusMonths(12), 180, today.minusDays(60));
        item("LL-ROB-0014", "2D LiDAR Sensor", "Slamtec RPLIDAR A2", EquipmentCategory.ROBOTICS, rob, 27500, today.minusMonths(9), 365, null);
        item("LL-ROB-0015", "Programmable Drone", "DJI Tello EDU", EquipmentCategory.ROBOTICS, rob, 16900, today.minusMonths(16), 180, today.minusDays(150));
        item("LL-FAB-0016", "3D Printer", "Creality K1 Max", EquipmentCategory.FABRICATION, rob, 72000, today.minusMonths(11), 90, today.minusDays(70));

        // ---------- bookings in different states ----------
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS);
        LocalDateTime tomorrow10 = today.plusDays(1).atTime(10, 0);
        if (tomorrow10.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) tomorrow10 = tomorrow10.plusDays(1);

        booking("LL-ELC-0004", s2, tomorrow10, tomorrow10.plusHours(2), "DSP practical - observing FIR filter output", BookingStatus.PENDING, null, null);
        booking("LL-ROB-0013", s1, tomorrow10.plusHours(3), tomorrow10.plusHours(5), "Final-year project: pick-and-place trial", BookingStatus.PENDING, null, null);
        booking("LL-CMP-0001", faculty, now.plusHours(2), now.plusHours(26), "Fine-tuning a vision model for research paper", BookingStatus.APPROVED, faculty, "Auto-approved (Faculty)");
        booking("LL-IOT-0007", s1, now.minusHours(1), now.plusHours(2), "IoT mini-project: MQTT weather node", BookingStatus.CHECKED_OUT, assistant, null);
        // already overdue - the dashboard and timer will flag it
        booking("LL-ROB-0015", s2, now.minusHours(6), now.minusHours(3), "Drone navigation lab assignment", BookingStatus.CHECKED_OUT, assistant, null);
        booking("LL-NET-0010", s1, now.minusDays(2), now.minusDays(2).plusHours(2), "VLAN configuration practical", BookingStatus.RETURNED, assistant, "Returned in good condition");

        // ---------- an open fault ----------
        Equipment printer = eq.get("LL-FAB-0016");
        printer.setStatus(EquipmentStatus.UNDER_MAINTENANCE);
        MaintenanceTicket t = new MaintenanceTicket();
        t.setEquipment(printer);
        t.setReportedBy(assistant);
        t.setIssue("Extruder clogging after 20 minutes; first layer not adhering");
        t.setSeverity(TicketSeverity.HIGH);
        em.persist(t);

        // Written directly (not via a CDI event): this runs while the Singleton beans are still starting up.
        em.persist(new ActivityLog("SYSTEM", "DEMO_DATA_LOADED", "4 labs, 16 equipment items, 6 members loaded"));
        LOG.info("[LabLoop] Demo data loaded");
    }

    private LabUser user(String name, String email, String memberId, String dept, UserRole role) {
        LabUser u = new LabUser(name, email, memberId, dept, role);
        u.setPasswordHash(PasswordUtil.hash(DEMO_PASSWORD));
        em.persist(u);
        return u;
    }

    private Lab lab(String code, String name, String location, int capacity, String inCharge) {
        Lab l = new Lab(code, name, location, capacity, inCharge);
        em.persist(l);
        return l;
    }

    private void item(String tag, String name, String model, EquipmentCategory cat, Lab lab, double cost,
                      LocalDate bought, int intervalDays, LocalDate lastService) {
        Equipment e = new Equipment();
        e.setAssetTag(tag);
        e.setName(name);
        e.setModel(model);
        e.setCategory(cat);
        e.setLab(lab);
        e.setPurchaseCost(cost);
        e.setPurchaseDate(bought);
        e.setServiceIntervalDays(intervalDays);
        e.setLastServicedOn(lastService);
        em.persist(e);
        eq.put(tag, e);
    }

    private void booking(String tag, LabUser who, LocalDateTime start, LocalDateTime end, String purpose,
                         BookingStatus status, LabUser decidedBy, String remark) {
        Booking b = new Booking();
        Equipment e = eq.get(tag);
        b.setEquipment(e);
        b.setRequestedBy(who);
        b.setStartTime(start);
        b.setEndTime(end);
        b.setPurpose(purpose);
        b.setStatus(status);
        b.setDecidedBy(decidedBy);
        b.setStaffRemark(remark);
        b.setCreatedAt(start.minusDays(1));
        if (status == BookingStatus.CHECKED_OUT || status == BookingStatus.RETURNED) {
            b.setCheckedOutAt(start);
        }
        if (status == BookingStatus.CHECKED_OUT) {
            e.setStatus(EquipmentStatus.IN_USE);
        }
        if (status == BookingStatus.RETURNED) {
            b.setReturnedAt(end);
        }
        em.persist(b);
    }
}
