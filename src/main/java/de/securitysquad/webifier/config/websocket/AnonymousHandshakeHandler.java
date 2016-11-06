package de.securitysquad.webifier.config.websocket;

import de.securitysquad.webifier.config.WebifierConstants;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.Map;

/**
 * Created by samuel on 05.11.16.
 */
public class AnonymousHandshakeHandler extends DefaultHandshakeHandler {
    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        return () -> {
            HttpSession session = (HttpSession) attributes.get(WebifierConstants.Session.HTTP_SESSION);
            return (String) session.getAttribute(WebifierConstants.Session.CHECK_ID);
        };
    }


}