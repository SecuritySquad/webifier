package de.securitysquad.webifier.config.websocket;

import de.securitysquad.webifier.config.WebifierConstants;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptorAdapter;
import org.springframework.session.events.SessionDestroyedEvent;

import javax.servlet.http.HttpSession;

import static java.util.Arrays.asList;
import static org.springframework.messaging.simp.SimpMessageType.*;

/**
 * Created by samuel on 05.11.16.
 */
public class MessageValidateInterceptor extends ChannelInterceptorAdapter {
    private ApplicationEventPublisher eventPublisher;

    public MessageValidateInterceptor(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        HttpSession session = (HttpSession) accessor.getSessionAttributes().get(WebifierConstants.Session.HTTP_SESSION);

        if (asList(CONNECT_ACK, SUBSCRIBE, MESSAGE).contains(accessor.getMessageType())) {
            if (!validateMessageHeader(session, accessor)) {
                eventPublisher.publishEvent(new SessionDestroyedEvent(this, session.getId()));
            }
        }
        accessor.setLeaveMutable(true);
        return message;
    }

    private boolean validateMessageHeader(HttpSession session, StompHeaderAccessor accessor) {
        String url = accessor.getFirstNativeHeader(WebifierConstants.Parameter.URL);
        String id = accessor.getFirstNativeHeader(WebifierConstants.Parameter.ID);
        return (url != null && id != null) && url.equals(session.getAttribute(WebifierConstants.Session.CHECK_URL)) && id.equals(session.getAttribute(WebifierConstants.Session.CHECK_ID));
    }
}