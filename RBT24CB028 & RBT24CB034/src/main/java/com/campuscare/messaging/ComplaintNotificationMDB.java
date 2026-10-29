package com.campuscare.messaging;

import com.campuscare.ejb.NotificationServiceBean;
import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.EJB;
import jakarta.ejb.MessageDriven;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.ObjectMessage;
import jakarta.jms.TextMessage;
import java.util.logging.Level;
import java.util.logging.Logger;

@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "jms/CampusCareQueue"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "jakarta.jms.Queue")
})
public class ComplaintNotificationMDB implements MessageListener {

    private static final Logger LOGGER = Logger.getLogger(ComplaintNotificationMDB.class.getName());

    @EJB
    private NotificationServiceBean notificationService;

    @Override
    public void onMessage(Message message) {
        LOGGER.log(Level.INFO, "===> JMS MDB Received Message: {0}", message);
        try {
            if (message instanceof ObjectMessage) {
                ObjectMessage objMsg = (ObjectMessage) message;
                Object obj = objMsg.getObject();
                if (obj instanceof ComplaintEvent) {
                    ComplaintEvent event = (ComplaintEvent) obj;
                    LOGGER.log(Level.INFO, "Processing ComplaintEvent via JMS MDB: Type={0}, ComplaintId={1}, User={2}",
                            new Object[]{event.getEventType(), event.getComplaintId(), event.getTargetUserId()});
                    if (event.getTargetUserId() != null) {
                        notificationService.createNotification(event.getTargetUserId(), event.getMessage());
                    }
                }
            } else if (message instanceof TextMessage) {
                TextMessage textMsg = (TextMessage) message;
                LOGGER.log(Level.INFO, "TextMessage received in MDB: {0}", textMsg.getText());
            }
        } catch (JMSException e) {
            LOGGER.log(Level.SEVERE, "Error processing JMS message in MDB", e);
        }
    }
}
