package de.securitysquad.webifier.web.controller;

import de.securitysquad.webifier.core.WebifierTesterResult;
import de.securitysquad.webifier.core.WebifierTesterResultListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by samuel on 02.11.16.
 */
@RestController
public class CheckController implements WebifierTesterResultListener {
    private final SimpMessagingTemplate messagingTemplate;
    private final Map<WebifierEndpoint, List<String>> subscribed;
    private final Map<WebifierEndpoint, Map<String, List<String>>> cache;

    @Autowired
    public CheckController(SimpMessagingTemplate messagingTemplate) {
        Assert.notNull(messagingTemplate, "messagingTemplate must not be null!");
        this.messagingTemplate = messagingTemplate;
        this.subscribed = new HashMap<>();
        this.subscribed.put(WebifierEndpoint.WAITING, new ArrayList<>());
        this.subscribed.put(WebifierEndpoint.STARTED, new ArrayList<>());
        this.subscribed.put(WebifierEndpoint.CHECK, new ArrayList<>());
        this.cache = new HashMap<>();
        this.cache.put(WebifierEndpoint.WAITING, new HashMap<>());
        this.cache.put(WebifierEndpoint.STARTED, new HashMap<>());
        this.cache.put(WebifierEndpoint.CHECK, new HashMap<>());
    }

    @MessageMapping("/connect")
    public void connect(Principal p) {
        subscribed.get(WebifierEndpoint.WAITING).add(p.getName());
        cache.get(WebifierEndpoint.WAITING).getOrDefault(p.getName(), new ArrayList<>()).forEach(message -> sendWaitingEvent(p.getName(), message));
        subscribed.get(WebifierEndpoint.STARTED).add(p.getName());
        cache.get(WebifierEndpoint.STARTED).getOrDefault(p.getName(), new ArrayList<>()).forEach(message -> sendStartedEvent(p.getName()));
        subscribed.get(WebifierEndpoint.CHECK).add(p.getName());
        messagingTemplate.convertAndSendToUser(p.getName(), "/check", Boolean.toString(true));
        cache.get(WebifierEndpoint.CHECK).getOrDefault(p.getName(), new ArrayList<>()).forEach(message -> sendCheckEvent(p.getName(), message));
    }

    @Override
    public void onWaitingPositionChanged(HttpSession session, String id, int position) {
        sendWaitingEvent(id, Integer.toString(position));
    }

    @Override
    public void onStarted(HttpSession session, String id) {
        sendStartedEvent(id);
    }

    @Override
    public void onTestResult(HttpSession session, String launchId, WebifierTesterResult result) {
        sendCheckEvent(launchId, result.getContent());
    }

    @Override
    public void onError(HttpSession session, String line) {
        System.err.println(line);
    }

    @Override
    public void onFinished(HttpSession session, String id) {
        subscribed.forEach((endpoint, ids) -> ids.remove(id));
        cache.forEach((endpoint, cache) -> cache.remove(id));
        try {
            session.invalidate();
        } catch (IllegalStateException e) {
            // session is already expired
        }
    }

    private void sendStartedEvent(String id) {
        List<String> cache = getOrCreateCache(WebifierEndpoint.STARTED, id);
        String message = "started";
        if (!cache.contains(message)) {
            cache.add(message);
        }
        if (subscribed.get(WebifierEndpoint.STARTED).contains(id)) {
            messagingTemplate.convertAndSendToUser(id, "/started", message);
        }
    }

    private void sendWaitingEvent(String id, String message) {
        List<String> cache = getOrCreateCache(WebifierEndpoint.WAITING, id);
        if (!cache.contains(message)) {
            cache.add(message);
        }
        if (subscribed.get(WebifierEndpoint.WAITING).contains(id)) {
            messagingTemplate.convertAndSendToUser(id, "/waiting", message);
        }
    }

    private void sendCheckEvent(String id, String message) {
        List<String> cache = getOrCreateCache(WebifierEndpoint.CHECK, id);
        if (!cache.contains(message)) {
            cache.add(message);
        }
        if (subscribed.get(WebifierEndpoint.CHECK).contains(id)) {
            messagingTemplate.convertAndSendToUser(id, "/check", message);
        }
    }

    private List<String> getOrCreateCache(WebifierEndpoint endpoint, String id) {
        if (!cache.get(endpoint).containsKey(id)) {
            cache.get(endpoint).put(id, new ArrayList<>());
        }
        return cache.get(endpoint).get(id);
    }

    private enum WebifierEndpoint {
        WAITING, STARTED, CHECK
    }
}