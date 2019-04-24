package br.com.mvgc.reactivekanban.clientnotifier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Sends notification to clients
 */
@Component
public class ClientNotifier {

    @Autowired
    SimpMessagingTemplate template;

    public void notifyClients(String destination, Object message) {
        template.convertAndSend(destination, message);
    }

}
