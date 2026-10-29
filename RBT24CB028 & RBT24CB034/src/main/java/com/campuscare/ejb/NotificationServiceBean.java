package com.campuscare.ejb;

import com.campuscare.dto.NotificationDto;
import com.campuscare.entity.Notification;
import com.campuscare.entity.User;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Stateless
public class NotificationServiceBean {

    @PersistenceContext(unitName = "CampusCarePU")
    private EntityManager em;

    public Notification createNotification(Long userId, String message) {
        if (userId == null || message == null || message.trim().isEmpty()) {
            return null;
        }
        User user = em.find(User.class, userId);
        if (user == null) {
            return null;
        }
        Notification notification = new Notification(user, message);
        notification.setCreatedAt(new Date());
        em.persist(notification);
        return notification;
    }

    public List<NotificationDto> getUserNotifications(Long userId) {
        TypedQuery<Notification> query = em.createQuery(
                "SELECT n FROM Notification n WHERE n.user.id = :userId ORDER BY n.createdAt DESC",
                Notification.class
        );
        query.setParameter("userId", userId);
        query.setMaxResults(50);
        List<Notification> list = query.getResultList();

        List<NotificationDto> dtoList = new ArrayList<>();
        for (Notification n : list) {
            dtoList.add(new NotificationDto(
                    n.getId(),
                    n.getUser().getId(),
                    n.getMessage(),
                    n.isRead(),
                    n.getCreatedAt()
            ));
        }
        return dtoList;
    }

    public boolean markAsRead(Long notificationId, Long userId) {
        Notification notification = em.find(Notification.class, notificationId);
        if (notification != null && notification.getUser().getId().equals(userId)) {
            notification.setRead(true);
            em.merge(notification);
            return true;
        }
        return false;
    }

    public void markAllAsRead(Long userId) {
        em.createQuery("UPDATE Notification n SET n.read = true WHERE n.user.id = :userId")
                .setParameter("userId", userId)
                .executeUpdate();
    }
}
