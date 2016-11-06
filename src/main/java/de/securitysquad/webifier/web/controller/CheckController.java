package de.securitysquad.webifier.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 * Created by samuel on 02.11.16.
 */
@RestController
public class CheckController {
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    public CheckController(SimpMessagingTemplate messagingTemplate) {
        Assert.notNull(messagingTemplate, "messagingTemplate must not be null!");
        this.messagingTemplate = messagingTemplate;
    }

    @SubscribeMapping("/user/checked")
    public void check(Principal p) throws InterruptedException {
        messagingTemplate.convertAndSendToUser(p.getName(), "/checked", Boolean.toString(true));
    }
}