package de.securitysquad.webifier.web.controller;

import de.securitysquad.webifier.core.TesterResult;
import de.securitysquad.webifier.core.WebifierTestResultListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.session.SessionRepository;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.security.Principal;

/**
 * Created by samuel on 02.11.16.
 */
@RestController
public class CheckController implements WebifierTestResultListener {
    private final SessionRepository sessionRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public CheckController(SessionRepository sessionRepository, SimpMessagingTemplate messagingTemplate) {
        Assert.notNull(sessionRepository, "sessionRepository must not be null!");
        Assert.notNull(messagingTemplate, "messagingTemplate must not be null!");
        this.sessionRepository = sessionRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @SubscribeMapping("/user/check")
    public void check(Principal p) throws InterruptedException {
        messagingTemplate.convertAndSendToUser(p.getName(), "/check", Boolean.toString(true));
    }

    @Override
    public void onTestResult(HttpSession session, String launchId, TesterResult result) {
        if (result.getTyp().equals("TesterFinished")) {
            session.invalidate();
        }
        messagingTemplate.convertAndSendToUser(launchId, "/check", result.getContent());
    }

    @Override
    public void onError(HttpSession session, String line) {
        System.err.println(line);
    }
}