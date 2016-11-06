package de.securitysquad.webifier.config.websocket;

import de.securitysquad.webifier.config.WebifierConstants;
import org.springframework.core.MethodParameter;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;

import javax.servlet.http.HttpSession;
import java.util.Map;

import static org.springframework.messaging.simp.SimpMessageHeaderAccessor.getSessionAttributes;

/**
 * Created by samuel on 05.11.16.
 */
public class SessionResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(HttpSession.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, Message<?> message) throws Exception {
        Map<String, Object> session = getSessionAttributes(message.getHeaders());
        return session.get(WebifierConstants.Session.HTTP_SESSION);
    }
}
