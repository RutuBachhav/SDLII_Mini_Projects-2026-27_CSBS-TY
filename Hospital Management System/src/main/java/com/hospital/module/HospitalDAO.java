package com.hospital.module;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

public class HospitalDAO {

    private static final EntityManagerFactory emf = buildEmf();

    /**
     * Honors JDBC_URL / JDBC_USER / JDBC_PASSWORD / JDBC_DRIVER environment
     * variables when set (hosted deployments); otherwise uses the
     * persistence.xml defaults (local in-memory H2).
     */
    private static EntityManagerFactory buildEmf() {
        Map<String, String> props = new HashMap<>();
        String url = System.getenv("JDBC_URL");
        if (url != null && !url.trim().isEmpty()) {
            props.put("javax.persistence.jdbc.url", url.trim());
            props.put("javax.persistence.jdbc.user", envOr("JDBC_USER", "sa"));
            props.put("javax.persistence.jdbc.password", envOr("JDBC_PASSWORD", ""));
            String driver = System.getenv("JDBC_DRIVER");
            if (driver != null && !driver.trim().isEmpty()) {
                props.put("javax.persistence.jdbc.driver", driver.trim());
            }
        }
        return Persistence.createEntityManagerFactory("HospitalPU", props);
    }

    private static String envOr(String key, String fallback) {
        String v = System.getenv(key);
        return v == null ? fallback : v;
    }

    /** Default seeded doctor login password (documented in README). */
    public static final String SEED_DOCTOR_PASSWORD = "doc123";

    /** How far ahead patients may book. */
    public static final int MAX_BOOK_AHEAD_DAYS = 30;

    static {
        seedDoctorsIfEmpty();
    }

    // ---------- legacy: doctors / patients ----------

    private static synchronized void seedDoctorsIfEmpty() {
        EntityManager em = emf.createEntityManager();
        try {
            Long count = em.createQuery("SELECT COUNT(d) FROM Doctor d", Long.class).getSingleResult();
            if (count == 0) {
                em.getTransaction().begin();
                em.persist(doctor("Dr. Sarah Rao", "Cardiology", "sarah@hospital.local",
                        "MD Cardiology", "Interventional cardiologist with 12 years of experience in preventive and emergency cardiac care.", 800));
                em.persist(doctor("Dr. Rajesh Sharma", "General Medicine", "rajesh@hospital.local",
                        "MBBS, MD Internal Medicine", "General physician focused on diabetes, hypertension and routine adult care.", 500));
                em.persist(doctor("Dr. Anita Desai", "Orthopedics", "anita@hospital.local",
                        "MS Orthopedics", "Orthopedic surgeon specialising in joint care, fractures and sports injuries.", 700));
                em.persist(doctor("Dr. Kevin Miller", "Pediatrics", "kevin@hospital.local",
                        "MD Pediatrics", "Pediatrician providing newborn care, immunisations and growth monitoring.", 600));
                em.getTransaction().commit();
            }
            em.getTransaction().begin();
            ensureDoctorLogins(em);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    private static Doctor doctor(String name, String dept, String email, String qual, String bio, int fee) {
        Doctor d = new Doctor(name, dept);
        d.setEmail(email.toLowerCase());
        d.setQualification(qual);
        d.setBio(bio);
        d.setConsultationFee(fee);
        d.setWorkStart("09:00");
        d.setWorkEnd("17:00");
        d.setSlotMinutes(30);
        d.setActive(true);
        return d;
    }

    /** Creates a login for every seeded doctor that lacks one. Runs inside a transaction. */
    private static void ensureDoctorLogins(EntityManager em) {
        List<Doctor> doctors = em.createQuery("SELECT d FROM Doctor d", Doctor.class).getResultList();
        for (Doctor d : doctors) {
            if (d.getEmail() == null || d.getEmail().trim().isEmpty()) {
                continue;
            }
            Long existing = em.createQuery("SELECT COUNT(u) FROM AppUser u WHERE u.email = :e", Long.class)
                    .setParameter("e", d.getEmail().toLowerCase()).getSingleResult();
            if (existing == 0) {
                PasswordUtil.Hash h = PasswordUtil.createHash(SEED_DOCTOR_PASSWORD);
                AppUser u = new AppUser();
                u.setFullName(d.getName());
                u.setEmail(d.getEmail().toLowerCase());
                u.setPasswordHash(h.hashHex);
                u.setPasswordSalt(h.saltHex);
                u.setRole(Role.DOCTOR);
                u.setDoctor(d);
                u.setCreatedAt(LocalDateTime.now());
                em.persist(u);
            }
        }
    }

    public static List<Doctor> getAllDoctors() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT d FROM Doctor d ORDER BY d.id ASC", Doctor.class).getResultList();
        } finally {
            em.close();
        }
    }

    public static List<Doctor> getActiveDoctors() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT d FROM Doctor d WHERE d.active = true ORDER BY d.department, d.name", Doctor.class).getResultList();
        } finally {
            em.close();
        }
    }

    public static List<Patient> getAllPatients() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT p FROM Patient p LEFT JOIN FETCH p.assignedDoctor ORDER BY p.id DESC", Patient.class).getResultList();
        } finally {
            em.close();
        }
    }

    public static Doctor findDoctorById(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Doctor.class, id);
        } finally {
            em.close();
        }
    }

    public static void savePatient(Patient patient) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            if (patient.getCreatedAt() == null) {
                patient.setCreatedAt(LocalDateTime.now());
            }
            em.persist(patient);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    // ---------- users / auth ----------

    public static AppUser findUserByEmail(String email) {
        if (email == null) {
            return null;
        }
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<AppUser> q = em.createQuery("SELECT u FROM AppUser u WHERE u.email = :e", AppUser.class);
            q.setParameter("e", email.trim().toLowerCase());
            try {
                AppUser u = q.getSingleResult();
                // Touch links while the session is open to avoid lazy-loading issues.
                if (u.getPatient() != null) {
                    u.getPatient().getName();
                }
                if (u.getDoctor() != null) {
                    u.getDoctor().getName();
                }
                return u;
            } catch (NoResultException e) {
                return null;
            }
        } finally {
            em.close();
        }
    }

    public static AppUser findUserById(Long id) {
        if (id == null) {
            return null;
        }
        EntityManager em = emf.createEntityManager();
        try {
            AppUser u = em.find(AppUser.class, id);
            if (u != null) {
                if (u.getPatient() != null) {
                    u.getPatient().getName();
                }
                if (u.getDoctor() != null) {
                    u.getDoctor().getName();
                }
            }
            return u;
        } finally {
            em.close();
        }
    }

    public static boolean emailTaken(String email) {
        return findUserByEmail(email) != null;
    }

    /** Registers a patient: creates Patient + PATIENT AppUser. Role is always PATIENT. */
    public static synchronized AppUser registerPatient(String fullName, String email, String password) {
        String norm = email.trim().toLowerCase();
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Long existing = em.createQuery("SELECT COUNT(u) FROM AppUser u WHERE u.email = :e", Long.class)
                    .setParameter("e", norm).getSingleResult();
            if (existing > 0) {
                throw new IllegalStateException("An account with this email already exists.");
            }
            Patient p = new Patient();
            p.setName(fullName.trim());
            p.setEmail(norm);
            p.setCreatedAt(LocalDateTime.now());
            em.persist(p);

            PasswordUtil.Hash h = PasswordUtil.createHash(password);
            AppUser u = new AppUser();
            u.setFullName(fullName.trim());
            u.setEmail(norm);
            u.setPasswordHash(h.hashHex);
            u.setPasswordSalt(h.saltHex);
            u.setRole(Role.PATIENT);
            u.setPatient(p);
            u.setCreatedAt(LocalDateTime.now());
            em.persist(u);
            em.getTransaction().commit();
            return u;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public static Patient findPatientById(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Patient.class, id);
        } finally {
            em.close();
        }
    }

    public static void updatePatient(Patient detached) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(detached);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public static void updateDoctor(Doctor detached) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(detached);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public static void updateUserName(Long userId, String fullName) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            AppUser u = em.find(AppUser.class, userId);
            if (u != null) {
                u.setFullName(fullName);
            }
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    // ---------- appointments ----------

    /** Stores a doctor's note after verifying the appointment belongs to that doctor. */
    public static synchronized void saveDoctorNote(Long appointmentId, Long doctorId, String notes) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Appointment a = em.find(Appointment.class, appointmentId);
            if (a == null) {
                throw new IllegalArgumentException("Appointment not found.");
            }
            if (!a.getDoctor().getId().equals(doctorId)) {
                throw new SecurityException("You can only manage your own schedule.");
            }
            if (a.getStatus().isTerminal()) {
                throw new IllegalStateException("Notes cannot be changed once the appointment is closed.");
            }
            String n = notes == null ? "" : notes.trim();
            a.setNotes(n.length() > 2000 ? n.substring(0, 2000) : n);
            a.setUpdatedAt(LocalDateTime.now());
            em.merge(a);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Books an appointment. Synchronized + transactional overlap check so two
     * concurrent requests for the same doctor slot cannot both succeed.
     */
    public static synchronized Appointment bookAppointment(Long patientId, Long doctorId,
            LocalDate date, LocalTime start, String reason) {
        if (patientId == null || doctorId == null || date == null || start == null) {
            throw new IllegalArgumentException("Doctor, date and time are required.");
        }
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Appointments cannot be booked in the past.");
        }
        if (date.isAfter(LocalDate.now().plusDays(MAX_BOOK_AHEAD_DAYS))) {
            throw new IllegalArgumentException("Appointments can be booked up to " + MAX_BOOK_AHEAD_DAYS + " days ahead.");
        }
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Doctor doctor = em.find(Doctor.class, doctorId);
            Patient patient = em.find(Patient.class, patientId);
            if (doctor == null || !doctor.isActive()) {
                throw new IllegalArgumentException("This doctor is not available for booking.");
            }
            if (patient == null) {
                throw new IllegalArgumentException("Patient record not found.");
            }
            int slotMinutes = doctor.getSlotMinutes() != null ? doctor.getSlotMinutes() : 30;
            LocalTime workStart = ValidationUtil.parseTime(doctor.getWorkStart());
            LocalTime workEnd = ValidationUtil.parseTime(doctor.getWorkEnd());
            if (workStart == null || workEnd == null) {
                throw new IllegalStateException("Doctor working hours are not configured.");
            }
            LocalTime end = start.plusMinutes(slotMinutes);
            if (start.isBefore(workStart) || end.isAfter(workEnd)) {
                throw new IllegalArgumentException("That time is outside this doctor's working hours ("
                        + doctor.getWorkStart() + "–" + doctor.getWorkEnd() + ").");
            }
            // Must align to slot grid measured from work start.
            long minutesFromStart = java.time.Duration.between(workStart, start).toMinutes();
            if (minutesFromStart < 0 || minutesFromStart % slotMinutes != 0) {
                throw new IllegalArgumentException("Appointments start every " + slotMinutes
                        + " minutes from " + doctor.getWorkStart() + ".");
            }
            if (date.equals(LocalDate.now()) && !start.isAfter(LocalTime.now())) {
                throw new IllegalArgumentException("That slot has already passed today. Please pick a later time.");
            }
            Long clashes = em.createQuery(
                    "SELECT COUNT(a) FROM Appointment a WHERE a.doctor.id = :doc AND a.date = :d "
                            + "AND a.status IN :active AND a.startTime < :end AND a.endTime > :start", Long.class)
                    .setParameter("doc", doctorId)
                    .setParameter("d", date)
                    .setParameter("active", java.util.Arrays.asList(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED))
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .getSingleResult();
            if (clashes > 0) {
                throw new IllegalStateException("That slot was just taken. Please choose another time.");
            }
            Appointment a = new Appointment();
            a.setPatient(patient);
            a.setDoctor(doctor);
            a.setDate(date);
            a.setStartTime(start);
            a.setEndTime(end);
            a.setStatus(AppointmentStatus.PENDING);
            a.setReason(reason != null && reason.trim().length() > 500
                    ? reason.trim().substring(0, 500) : (reason == null ? null : reason.trim()));
            a.setCreatedAt(LocalDateTime.now());
            a.setUpdatedAt(LocalDateTime.now());
            em.persist(a);
            em.getTransaction().commit();
            return a;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public static Appointment findAppointmentById(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            Appointment a = em.find(Appointment.class, id);
            if (a != null) {
                a.getDoctor().getName();
                a.getPatient().getName();
            }
            return a;
        } finally {
            em.close();
        }
    }

    public static List<Appointment> findAppointmentsByPatient(Long patientId) {
        EntityManager em = emf.createEntityManager();
        try {
            List<Appointment> list = em.createQuery(
                    "SELECT a FROM Appointment a JOIN FETCH a.doctor JOIN FETCH a.patient "
                            + "WHERE a.patient.id = :p ORDER BY a.date DESC, a.startTime DESC", Appointment.class)
                    .setParameter("p", patientId).getResultList();
            return list;
        } finally {
            em.close();
        }
    }

    public static List<Appointment> findAppointmentsByDoctor(Long doctorId) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT a FROM Appointment a JOIN FETCH a.doctor JOIN FETCH a.patient "
                            + "WHERE a.doctor.id = :d ORDER BY a.date ASC, a.startTime ASC", Appointment.class)
                    .setParameter("d", doctorId).getResultList();
        } finally {
            em.close();
        }
    }

    public static List<Appointment> findUpcomingByDoctor(Long doctorId, int limit) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT a FROM Appointment a JOIN FETCH a.doctor JOIN FETCH a.patient "
                            + "WHERE a.doctor.id = :d AND a.status IN :active AND a.date >= :today "
                            + "ORDER BY a.date ASC, a.startTime ASC", Appointment.class)
                    .setParameter("d", doctorId)
                    .setParameter("active", java.util.Arrays.asList(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED))
                    .setParameter("today", LocalDate.now())
                    .setMaxResults(limit).getResultList();
        } finally {
            em.close();
        }
    }

    public static List<Appointment> findUpcomingByPatient(Long patientId, int limit) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT a FROM Appointment a JOIN FETCH a.doctor JOIN FETCH a.patient "
                            + "WHERE a.patient.id = :p AND a.status IN :active AND a.date >= :today "
                            + "ORDER BY a.date ASC, a.startTime ASC", Appointment.class)
                    .setParameter("p", patientId)
                    .setParameter("active", java.util.Arrays.asList(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED))
                    .setParameter("today", LocalDate.now())
                    .setMaxResults(limit).getResultList();
        } finally {
            em.close();
        }
    }

    /** Counts by status for a doctor's dashboard. Keys: PENDING, CONFIRMED, TODAY. */
    public static Map<String, Long> doctorCounts(Long doctorId) {
        EntityManager em = emf.createEntityManager();
        try {
            Map<String, Long> m = new LinkedHashMap<>();
            m.put("PENDING", countDoctor(em, doctorId, AppointmentStatus.PENDING, null));
            m.put("CONFIRMED", countDoctor(em, doctorId, AppointmentStatus.CONFIRMED, null));
            m.put("TODAY", countDay(em, doctorId, LocalDate.now()));
            m.put("COMPLETED", countDoctor(em, doctorId, AppointmentStatus.COMPLETED, null));
            return m;
        } finally {
            em.close();
        }
    }

    private static Long countDoctor(EntityManager em, Long doctorId, AppointmentStatus status, LocalDate day) {
        String jpql = "SELECT COUNT(a) FROM Appointment a WHERE a.doctor.id = :d AND a.status = :s";
        if (day != null) {
            jpql += " AND a.date = :day";
        }
        TypedQuery<Long> q = em.createQuery(jpql, Long.class)
                .setParameter("d", doctorId).setParameter("s", status);
        if (day != null) {
            q.setParameter("day", day);
        }
        return q.getSingleResult();
    }

    private static Long countDay(EntityManager em, Long doctorId, LocalDate day) {
        return em.createQuery(
                "SELECT COUNT(a) FROM Appointment a WHERE a.doctor.id = :d AND a.date = :day "
                        + "AND a.status IN :active", Long.class)
                .setParameter("d", doctorId).setParameter("day", day)
                .setParameter("active", java.util.Arrays.asList(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED))
                .getSingleResult();
    }

    /**
     * Applies a status transition with ownership + transition checks.
     *
     * @param actorRole    role of the caller (from the server session, never the client)
     * @param actorPatientId patient id when actor is a patient, else null
     * @param actorDoctorId  doctor id when actor is a doctor, else null
     * @param notes        optional doctor note (stored only when the owning doctor supplies it)
     */
    public static synchronized Appointment transitionAppointment(Long appointmentId, AppointmentStatus target,
            Role actorRole, Long actorPatientId, Long actorDoctorId, String notes) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Appointment a = em.find(Appointment.class, appointmentId);
            if (a == null) {
                throw new IllegalArgumentException("Appointment not found.");
            }
            AppointmentStatus from = a.getStatus();
            if (actorRole == Role.PATIENT) {
                if (actorPatientId == null || !actorPatientId.equals(a.getPatient().getId())) {
                    throw new SecurityException("You can only manage your own appointments.");
                }
                if (target != AppointmentStatus.CANCELLED
                        || !(from == AppointmentStatus.PENDING || from == AppointmentStatus.CONFIRMED)) {
                    throw new IllegalStateException("This appointment can no longer be cancelled.");
                }
            } else if (actorRole == Role.DOCTOR) {
                if (actorDoctorId == null || !actorDoctorId.equals(a.getDoctor().getId())) {
                    throw new SecurityException("You can only manage your own schedule.");
                }
                boolean ok = (from == AppointmentStatus.PENDING
                        && (target == AppointmentStatus.CONFIRMED || target == AppointmentStatus.REJECTED
                            || target == AppointmentStatus.CANCELLED))
                        || (from == AppointmentStatus.CONFIRMED
                        && (target == AppointmentStatus.COMPLETED || target == AppointmentStatus.CANCELLED));
                if (!ok) {
                    throw new IllegalStateException("Cannot move this appointment from " + from + " to " + target + ".");
                }
                if (notes != null && !notes.trim().isEmpty()) {
                    String n = notes.trim();
                    a.setNotes(n.length() > 2000 ? n.substring(0, 2000) : n);
                }
            } else {
                throw new SecurityException("Not authorized.");
            }
            a.setStatus(target);
            a.setUpdatedAt(LocalDateTime.now());
            em.merge(a);
            em.getTransaction().commit();
            return a;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    /** All bookable slot starts for a doctor on a date (working-hours grid minus taken). */
    public static List<LocalTime> availableSlots(Long doctorId, LocalDate date) {
        List<LocalTime> out = new ArrayList<>();
        if (doctorId == null || date == null || date.isBefore(LocalDate.now())
                || date.isAfter(LocalDate.now().plusDays(MAX_BOOK_AHEAD_DAYS))) {
            return out;
        }
        Doctor doctor = findDoctorById(doctorId);
        if (doctor == null || !doctor.isActive()) {
            return out;
        }
        int slotMinutes = doctor.getSlotMinutes() != null ? doctor.getSlotMinutes() : 30;
        LocalTime start = ValidationUtil.parseTime(doctor.getWorkStart());
        LocalTime end = ValidationUtil.parseTime(doctor.getWorkEnd());
        if (start == null || end == null || !start.isBefore(end) || slotMinutes < 10 || slotMinutes > 240) {
            return out;
        }
        EntityManager em = emf.createEntityManager();
        try {
            List<Appointment> taken = em.createQuery(
                    "SELECT a FROM Appointment a WHERE a.doctor.id = :d AND a.date = :day "
                            + "AND a.status IN :active", Appointment.class)
                    .setParameter("d", doctorId).setParameter("day", date)
                    .setParameter("active", java.util.Arrays.asList(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED))
                    .getResultList();
            for (LocalTime t = start; !t.plusMinutes(slotMinutes).isAfter(end); t = t.plusMinutes(slotMinutes)) {
                LocalTime s = t;
                LocalTime e = t.plusMinutes(slotMinutes);
                if (date.equals(LocalDate.now()) && !s.isAfter(LocalTime.now())) {
                    continue;
                }
                boolean clash = false;
                for (Appointment a : taken) {
                    if (a.getStartTime().isBefore(e) && a.getEndTime().isAfter(s)) {
                        clash = true;
                        break;
                    }
                }
                if (!clash) {
                    out.add(s);
                }
            }
            return out;
        } finally {
            em.close();
        }
    }
}
