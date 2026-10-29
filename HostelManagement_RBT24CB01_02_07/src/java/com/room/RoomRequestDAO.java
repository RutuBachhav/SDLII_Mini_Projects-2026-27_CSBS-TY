package com.room;

import com.entity.AppUser;
import com.entity.Hostel;
import com.entity.Room;
import com.entity.RoomAllocation;
import com.entity.RoomRequest;
import com.entity.RoomRequestStatus;
import com.security.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class RoomRequestDAO {

    public List<Hostel> findHostels() {
        EntityManager em = JpaUtil.em();
        try {
            return em.createQuery(
                    "SELECT h FROM Hostel h ORDER BY h.hostelName, h.hostelId",
                    Hostel.class).getResultList();
        } finally {
            em.close();
        }
    }

    public List<AvailableRoom> search(Integer hostelId,
                                      Double minFee,
                                      Double maxFee,
                                      Integer minCapacity,
                                      String roomNumber,
                                      boolean availableOnly) {

        EntityManager em = JpaUtil.em();
        try {
            StringBuilder jpql = new StringBuilder(
                    "SELECT r FROM Room r LEFT JOIN FETCH r.hostel h WHERE 1 = 1");
            Map<String, Object> parameters = new LinkedHashMap<>();

            if (hostelId != null) {
                jpql.append(" AND h.hostelId = :hostelId");
                parameters.put("hostelId", hostelId);
            }
            if (minFee != null) {
                jpql.append(" AND r.monthlyFee >= :minFee");
                parameters.put("minFee", minFee);
            }
            if (maxFee != null) {
                jpql.append(" AND r.monthlyFee <= :maxFee");
                parameters.put("maxFee", maxFee);
            }
            if (minCapacity != null) {
                jpql.append(" AND r.capacity >= :minCapacity");
                parameters.put("minCapacity", minCapacity);
            }
            if (roomNumber != null && !roomNumber.trim().isEmpty()) {
                jpql.append(" AND LOWER(r.roomNumber) LIKE :roomNumber");
                parameters.put("roomNumber",
                        "%" + roomNumber.trim().toLowerCase(Locale.ROOT) + "%");
            }
            if (availableOnly) {
                jpql.append(" AND r.status = :availableStatus")
                        .append(" AND r.capacity > 0")
                        .append(" AND (SELECT COUNT(a) FROM RoomAllocation a")
                        .append(" WHERE a.room = r) < r.capacity");
                parameters.put("availableStatus", "AVAILABLE");
            }
            jpql.append(" ORDER BY r.monthlyFee, r.roomId");

            TypedQuery<Room> query = em.createQuery(jpql.toString(), Room.class);
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                query.setParameter(entry.getKey(), entry.getValue());
            }

            List<Room> rooms = query.getResultList();
            Map<Long, Long> counts = allocationCounts(em, rooms);
            List<AvailableRoom> result = new ArrayList<>(rooms.size());
            for (Room room : rooms) {
                Long count = counts.get(room.getRoomId());
                result.add(new AvailableRoom(room, count == null ? 0 : count));
            }
            return result;

        } finally {
            em.close();
        }
    }

    public AvailableRoom findAvailableRoom(long roomId) {
        EntityManager em = JpaUtil.em();
        try {
            List<Room> rooms = em.createQuery(
                    "SELECT r FROM Room r LEFT JOIN FETCH r.hostel "
                    + "WHERE r.roomId = :roomId", Room.class)
                    .setParameter("roomId", roomId)
                    .getResultList();
            if (rooms.isEmpty()) {
                return null;
            }
            Room room = rooms.get(0);
            return new AvailableRoom(room, countAllocations(em, roomId));
        } finally {
            em.close();
        }
    }

    public Set<Long> pendingRoomIds(long userId) {
        EntityManager em = JpaUtil.em();
        try {
            List<Long> ids = em.createQuery(
                    "SELECT rq.room.roomId FROM RoomRequest rq "
                    + "WHERE rq.user.userId = :userId AND rq.status = :status",
                    Long.class)
                    .setParameter("userId", userId)
                    .setParameter("status", RoomRequestStatus.PENDING)
                    .getResultList();
            return new HashSet<>(ids);
        } finally {
            em.close();
        }
    }

    public boolean hasActiveAllocation(long userId) {
        EntityManager em = JpaUtil.em();
        try {
            return countAllocationsForUser(em, userId) > 0;
        } finally {
            em.close();
        }
    }

    public long countPendingRequests() {
        EntityManager em = JpaUtil.em();
        try {
            return em.createQuery(
                    "SELECT COUNT(rq) FROM RoomRequest rq "
                    + "WHERE rq.status = :status", Long.class)
                    .setParameter("status", RoomRequestStatus.PENDING)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }

    public RoomRequest createRequest(long userId, long roomId) {
        EntityManager em = JpaUtil.em();
        try {
            em.getTransaction().begin();

            AppUser user = em.find(AppUser.class, userId);
            if (user == null || !user.isActive()) {
                throw new RoomRequestException(
                        RoomRequestException.Reason.FORBIDDEN,
                        "forbidden",
                        "Your account cannot submit a room request.");
            }

            Room room = em.find(Room.class, roomId,
                    LockModeType.PESSIMISTIC_WRITE);
            if (room == null) {
                throw new RoomRequestException(
                        RoomRequestException.Reason.NOT_FOUND,
                        "notfound",
                        "That room no longer exists.");
            }

            if (!"AVAILABLE".equalsIgnoreCase(room.getStatus())
                    || room.getCapacity() < 1
                    || countAllocationsForUpdate(em, roomId)
                    >= room.getCapacity()) {
                throw new RoomRequestException(
                        RoomRequestException.Reason.CONFLICT,
                        "unavailable",
                        "This room is no longer available.");
            }

            if (countAllocationsForUserForUpdate(em, userId) > 0) {
                throw new RoomRequestException(
                        RoomRequestException.Reason.CONFLICT,
                        "allocated",
                        "You already have an approved room allocation.");
            }

            Long duplicates = em.createQuery(
                    "SELECT COUNT(rq) FROM RoomRequest rq "
                    + "WHERE rq.user.userId = :userId "
                    + "AND rq.room.roomId = :roomId "
                    + "AND rq.status = :status", Long.class)
                    .setParameter("userId", userId)
                    .setParameter("roomId", roomId)
                    .setParameter("status", RoomRequestStatus.PENDING)
                    .getSingleResult();

            if (duplicates > 0) {
                throw new RoomRequestException(
                        RoomRequestException.Reason.CONFLICT,
                        "duplicate",
                        "You already have a pending request for this room.");
            }

            RoomRequest request = new RoomRequest();
            request.setUser(user);
            request.setRoom(room);
            request.setStatus(RoomRequestStatus.PENDING);
            request.setRequestedAt(new Date());
            em.persist(request);
            em.getTransaction().commit();
            return request;

        } catch (RoomRequestException ex) {
            rollback(em);
            throw ex;
        } catch (PersistenceException ex) {
            rollback(em);
            throw changedDuringOperation();
        } catch (RuntimeException ex) {
            rollback(em);
            throw ex;
        } finally {
            em.close();
        }
    }

    public List<RoomRequest> findForUser(long userId) {
        EntityManager em = JpaUtil.em();
        try {
            return em.createQuery(
                    "SELECT rq FROM RoomRequest rq "
                    + "JOIN FETCH rq.room r "
                    + "LEFT JOIN FETCH r.hostel "
                    + "LEFT JOIN FETCH rq.decidedBy "
                    + "WHERE rq.user.userId = :userId "
                    + "ORDER BY rq.requestedAt DESC, rq.requestId DESC",
                    RoomRequest.class)
                    .setParameter("userId", userId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<RoomRequest> findAll(RoomRequestStatus status) {
        EntityManager em = JpaUtil.em();
        try {
            StringBuilder jpql = new StringBuilder(
                    "SELECT rq FROM RoomRequest rq "
                    + "JOIN FETCH rq.user u "
                    + "JOIN FETCH rq.room r "
                    + "LEFT JOIN FETCH r.hostel "
                    + "LEFT JOIN FETCH rq.decidedBy");
            if (status != null) {
                jpql.append(" WHERE rq.status = :status");
            }
            jpql.append(" ORDER BY rq.requestedAt DESC, rq.requestId DESC");

            TypedQuery<RoomRequest> query = em.createQuery(
                    jpql.toString(), RoomRequest.class);
            if (status != null) {
                query.setParameter("status", status);
            }
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public void cancelOwn(long requestId, long userId) {
        EntityManager em = JpaUtil.em();
        try {
            em.getTransaction().begin();

            RoomRequest request = em.find(RoomRequest.class, requestId,
                    LockModeType.PESSIMISTIC_WRITE);
            if (request == null || request.getUser() == null
                    || request.getUser().getUserId() == null
                    || request.getUser().getUserId() != userId) {
                throw new RoomRequestException(
                        RoomRequestException.Reason.NOT_FOUND,
                        "notfound",
                        "That pending request was not found.");
            }
            if (request.getStatus() != RoomRequestStatus.PENDING) {
                throw new RoomRequestException(
                        RoomRequestException.Reason.CONFLICT,
                        "conflict",
                        "Only a pending request can be cancelled.");
            }

            request.setStatus(RoomRequestStatus.CANCELLED);
            request.setDecidedAt(new Date());
            request.setDecidedBy(null);
            em.getTransaction().commit();

        } catch (RoomRequestException ex) {
            rollback(em);
            throw ex;
        } catch (PersistenceException ex) {
            rollback(em);
            throw changedDuringOperation();
        } catch (RuntimeException ex) {
            rollback(em);
            throw ex;
        } finally {
            em.close();
        }
    }

    public void reject(long requestId, long actorId) {
        EntityManager em = JpaUtil.em();
        try {
            em.getTransaction().begin();

            RoomRequest request = em.find(RoomRequest.class, requestId,
                    LockModeType.PESSIMISTIC_WRITE);
            if (request == null) {
                throw new RoomRequestException(
                        RoomRequestException.Reason.NOT_FOUND,
                        "notfound",
                        "That room request was not found.");
            }
            if (request.getStatus() != RoomRequestStatus.PENDING) {
                throw new RoomRequestException(
                        RoomRequestException.Reason.CONFLICT,
                        "conflict",
                        "Only a pending request can be rejected.");
            }

            AppUser actor = activeActor(em, actorId);
            request.setStatus(RoomRequestStatus.REJECTED);
            request.setDecidedAt(new Date());
            request.setDecidedBy(actor);
            em.getTransaction().commit();

        } catch (RoomRequestException ex) {
            rollback(em);
            throw ex;
        } catch (PersistenceException ex) {
            rollback(em);
            throw changedDuringOperation();
        } catch (RuntimeException ex) {
            rollback(em);
            throw ex;
        } finally {
            em.close();
        }
    }

    public void approve(long requestId, long actorId) {
        EntityManager em = JpaUtil.em();
        try {
            em.getTransaction().begin();

            RoomRequest snapshot = em.find(RoomRequest.class, requestId);
            if (snapshot == null) {
                throw new RoomRequestException(
                        RoomRequestException.Reason.NOT_FOUND,
                        "notfound",
                        "That room request was not found.");
            }

            Long roomId = snapshot.getRoom().getRoomId();
            Room room = em.find(Room.class, roomId,
                    LockModeType.PESSIMISTIC_WRITE);
            if (room == null) {
                throw new RoomRequestException(
                        RoomRequestException.Reason.NOT_FOUND,
                        "notfound",
                        "The requested room no longer exists.");
            }
            em.refresh(room, LockModeType.PESSIMISTIC_WRITE);

            RoomRequest request = em.find(RoomRequest.class, requestId,
                    LockModeType.PESSIMISTIC_WRITE);
            if (request == null) {
                throw new RoomRequestException(
                        RoomRequestException.Reason.NOT_FOUND,
                        "notfound",
                        "That room request was not found.");
            }
            if (request.getStatus() != RoomRequestStatus.PENDING) {
                throw new RoomRequestException(
                        RoomRequestException.Reason.CONFLICT,
                        "conflict",
                        "This request has already been processed.");
            }

            AppUser actor = activeActor(em, actorId);
            Long requesterId = request.getUser().getUserId();
            if (requesterId != null && requesterId == actorId) {
                throw new RoomRequestException(
                        RoomRequestException.Reason.FORBIDDEN,
                        "self",
                        "You cannot approve your own room request.");
            }

            AppUser requester = requesterId == null
                    ? null : em.find(AppUser.class, requesterId);
            if (requester == null || !requester.isActive()) {
                throw new RoomRequestException(
                        RoomRequestException.Reason.CONFLICT,
                        "inactive",
                        "The requesting account is not active.");
            }

            if (!"AVAILABLE".equalsIgnoreCase(room.getStatus())
                    || room.getCapacity() < 1) {
                throw new RoomRequestException(
                        RoomRequestException.Reason.CONFLICT,
                        "unavailable",
                        "This room is no longer available.");
            }

            long roomAllocations = countAllocationsForUpdate(em, roomId);
            if (roomAllocations >= room.getCapacity()) {
                throw new RoomRequestException(
                        RoomRequestException.Reason.CONFLICT,
                        "unavailable",
                        "This room has reached its approved capacity.");
            }

            if (countAllocationsForUserForUpdate(em, requesterId) > 0) {
                throw new RoomRequestException(
                        RoomRequestException.Reason.CONFLICT,
                        "allocated",
                        "The requester already has an approved room allocation.");
            }

            RoomAllocation allocation = new RoomAllocation();
            allocation.setUser(requester);
            allocation.setRoom(room);
            allocation.setRequest(request);
            allocation.setAllocatedAt(new Date());
            allocation.setAllocatedBy(actor);
            em.persist(allocation);

            request.setStatus(RoomRequestStatus.APPROVED);
            request.setDecidedAt(new Date());
            request.setDecidedBy(actor);

            if (roomAllocations + 1 >= room.getCapacity()) {
                room.setStatus("OCCUPIED");
            }
            em.getTransaction().commit();

        } catch (RoomRequestException ex) {
            rollback(em);
            throw ex;
        } catch (PersistenceException ex) {
            rollback(em);
            throw changedDuringOperation();
        } catch (RuntimeException ex) {
            rollback(em);
            throw ex;
        } finally {
            em.close();
        }
    }

    public void release(long requestId, long actorId) {
        EntityManager em = JpaUtil.em();
        try {
            em.getTransaction().begin();

            RoomRequest snapshot = em.find(RoomRequest.class, requestId);
            if (snapshot == null) {
                throw new RoomRequestException(
                        RoomRequestException.Reason.NOT_FOUND,
                        "notfound",
                        "That room request was not found.");
            }

            Long roomId = snapshot.getRoom().getRoomId();
            Room room = em.find(Room.class, roomId,
                    LockModeType.PESSIMISTIC_WRITE);
            if (room == null) {
                throw new RoomRequestException(
                        RoomRequestException.Reason.NOT_FOUND,
                        "notfound",
                        "The allocated room no longer exists.");
            }
            em.refresh(room, LockModeType.PESSIMISTIC_WRITE);

            RoomRequest request = em.find(RoomRequest.class, requestId,
                    LockModeType.PESSIMISTIC_WRITE);
            if (request == null
                    || request.getStatus() != RoomRequestStatus.APPROVED) {
                throw new RoomRequestException(
                        RoomRequestException.Reason.CONFLICT,
                        "conflict",
                        "Only an approved request can be released.");
            }

            List<RoomAllocation> allocations = em.createQuery(
                    "SELECT a FROM RoomAllocation a "
                    + "WHERE a.request.requestId = :requestId",
                    RoomAllocation.class)
                    .setParameter("requestId", requestId)
                    .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                    .getResultList();
            if (allocations.isEmpty()) {
                throw new RoomRequestException(
                        RoomRequestException.Reason.CONFLICT,
                        "conflict",
                        "The active allocation could not be found.");
            }

            AppUser actor = activeActor(em, actorId);
            em.remove(allocations.get(0));
            em.flush();

            request.setStatus(RoomRequestStatus.CANCELLED);
            request.setDecidedAt(new Date());
            request.setDecidedBy(actor);

            long remaining = countAllocationsForUpdate(em, roomId);
            if ("OCCUPIED".equalsIgnoreCase(room.getStatus())
                    && remaining < room.getCapacity()) {
                room.setStatus("AVAILABLE");
            }
            em.getTransaction().commit();

        } catch (RoomRequestException ex) {
            rollback(em);
            throw ex;
        } catch (PersistenceException ex) {
            rollback(em);
            throw changedDuringOperation();
        } catch (RuntimeException ex) {
            rollback(em);
            throw ex;
        } finally {
            em.close();
        }
    }

    private AppUser activeActor(EntityManager em, long actorId) {
        AppUser actor = em.find(AppUser.class, actorId);
        if (actor == null || !actor.isActive()) {
            throw new RoomRequestException(
                    RoomRequestException.Reason.FORBIDDEN,
                    "forbidden",
                    "Your account cannot perform this operation.");
        }
        return actor;
    }

    private long countAllocations(EntityManager em, long roomId) {
        return em.createQuery(
                "SELECT COUNT(a) FROM RoomAllocation a "
                + "WHERE a.room.roomId = :roomId", Long.class)
                .setParameter("roomId", roomId)
                .getSingleResult();
    }

    private long countAllocationsForUser(EntityManager em, long userId) {
        return em.createQuery(
                "SELECT COUNT(a) FROM RoomAllocation a "
                + "WHERE a.user.userId = :userId", Long.class)
                .setParameter("userId", userId)
                .getSingleResult();
    }

    private long countAllocationsForUpdate(EntityManager em, long roomId) {
        return em.createQuery(
                "SELECT a FROM RoomAllocation a "
                + "WHERE a.room.roomId = :roomId", RoomAllocation.class)
                .setParameter("roomId", roomId)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getResultList().size();
    }

    private long countAllocationsForUserForUpdate(EntityManager em,
                                                  long userId) {
        return em.createQuery(
                "SELECT a FROM RoomAllocation a "
                + "WHERE a.user.userId = :userId", RoomAllocation.class)
                .setParameter("userId", userId)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getResultList().size();
    }

    private Map<Long, Long> allocationCounts(EntityManager em,
                                             List<Room> rooms) {
        Map<Long, Long> counts = new HashMap<>();
        if (rooms.isEmpty()) {
            return counts;
        }
        List<Long> ids = new ArrayList<>(rooms.size());
        for (Room room : rooms) {
            ids.add(room.getRoomId());
        }
        List<Object[]> rows = em.createQuery(
                "SELECT a.room.roomId, COUNT(a) FROM RoomAllocation a "
                + "WHERE a.room.roomId IN :ids "
                + "GROUP BY a.room.roomId", Object[].class)
                .setParameter("ids", ids)
                .getResultList();
        for (Object[] row : rows) {
            counts.put((Long) row[0], (Long) row[1]);
        }
        return counts;
    }

    private RoomRequestException changedDuringOperation() {
        return new RoomRequestException(
                RoomRequestException.Reason.CONFLICT,
                "conflict",
                "This request changed while it was being processed. "
                + "Please refresh and try again.");
    }

    private void rollback(EntityManager em) {
        try {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        } catch (RuntimeException ignored) {
        }
    }
}
