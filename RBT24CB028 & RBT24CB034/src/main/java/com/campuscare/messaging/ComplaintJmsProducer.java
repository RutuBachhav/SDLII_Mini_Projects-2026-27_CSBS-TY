package com.campuscare.messaging;

import com.campuscare.ejb.NotificationServiceBean;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.MessageProducer;
import jakarta.jms.ObjectMessage;
import jakarta.jms.Queue;
import jakarta.jms.Session;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class ComplaintJmsProducer {

    private static final Logger LOGGER = Logger.getLogger(ComplaintJmsProducer.class.getName());

    @Resource(lookup = "jms/CampusCareConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Resource(lookup = "jms/CampusCareQueue")
    private Queue queue;

    @Inject
    private NotificationServiceBean notificationService;

    public void sendEvent(ComplaintEvent event) {
        if (event == null) {
            return;
        }

        boolean sentViaJms = false;
        if (connectionFactory != null && queue != null) {
            Connection connection = null;
            Session session = null;
            try {
                connection = connectionFactory.createConnection();
                session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                MessageProducer producer = session.createProducer(queue);
                ObjectMessage message = session.createObjectMessage(event);
                producer.send(message);
                sentViaJms = true;
                LOGGER.log(Level.INFO, "===> JMS Event sent to queue successfully: {0}", event.getEventType());
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to send JMS message via ConnectionFactory: " + e.getMessage(), e);
            } finally {
                if (session != null) {
                    try {
                        session.close();
                    } catch (JMSException ignored) {
                    }
                }
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (JMSException ignored) {
                    }
                }
            }
        }

        // If JMS could not deliver to MDB directly, persist notification as fallback
        if (!sentViaJms && event.getTargetUserId() != null) {
            LOGGER.log(Level.INFO, "Fallback: directly creating notification for userId {0}", event.getTargetUserId());
            notificationService.createNotification(event.getTargetUserId(), event.getMessage());
        }
    }
}
