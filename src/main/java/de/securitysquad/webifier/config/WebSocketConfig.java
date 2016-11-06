package de.securitysquad.webifier.config;

import de.securitysquad.webifier.config.websocket.AnonymousHandshakeHandler;
import de.securitysquad.webifier.config.websocket.MessageValidateInterceptor;
import de.securitysquad.webifier.config.websocket.SessionInterceptor;
import de.securitysquad.webifier.config.websocket.SessionResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.ExpiringSession;
import org.springframework.session.web.socket.config.annotation.AbstractSessionWebSocketMessageBrokerConfigurer;
import org.springframework.util.Assert;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

import java.util.List;

@Configuration
@EnableScheduling
@EnableWebSocketMessageBroker
public class WebSocketConfig extends AbstractSessionWebSocketMessageBrokerConfigurer<ExpiringSession> {
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public WebSocketConfig(ApplicationEventPublisher eventPublisher) {
        Assert.notNull(eventPublisher, "eventPublisher must not be null!");
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/connect")
                .setHandshakeHandler(new AnonymousHandshakeHandler())
                .addInterceptors(new SessionInterceptor())
                .withSockJS();
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new SessionResolver());
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        super.configureClientInboundChannel(registration);
        registration.setInterceptors(new MessageValidateInterceptor(eventPublisher));
    }
}