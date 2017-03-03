package de.securitysquad.webifier.web.controller;

import de.securitysquad.webifier.core.WebifierTesterLauncher;
import de.securitysquad.webifier.web.domain.WebifierQueueResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by samuel on 02.03.17.
 */
@RestController
public class QueueController {
    private final WebifierTesterLauncher webifierTesterLauncher;

    @Autowired
    public QueueController(WebifierTesterLauncher webifierTesterLauncher) {
        Assert.notNull(webifierTesterLauncher, "webifierTesterLauncher must not be null!");
        this.webifierTesterLauncher = webifierTesterLauncher;
    }

    @RequestMapping("/queue")
    @ResponseBody
    public WebifierQueueResponse getQueue() {
        int size = webifierTesterLauncher.getQueueSize();
        return new WebifierQueueResponse(size);
    }
}