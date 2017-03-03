package de.securitysquad.webifier.web.controller;

import de.securitysquad.webifier.config.WebifierConstants;
import de.securitysquad.webifier.core.WebifierTesterLauncher;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by samuel on 02.03.17.
 */
@Controller
public class BatchController {
    private final WebifierTesterLauncher webifierTesterLauncher;

    @Autowired
    public BatchController(WebifierTesterLauncher webifierTesterLauncher) {
        Assert.notNull(webifierTesterLauncher, "webifierTesterLauncher must not be null!");
        this.webifierTesterLauncher = webifierTesterLauncher;
    }

    @RequestMapping("/batch")
    public ModelAndView returnBatchView(HttpSession session) {
        ModelAndView result = new ModelAndView("batch");
        Object error = session.getAttribute(WebifierConstants.Session.ERROR);
        if (error != null) {
            result.addObject("error", error);
            session.removeAttribute(WebifierConstants.Session.ERROR);
        }
        return result;
    }

    @RequestMapping(value = "/batch/check", method = RequestMethod.POST)
    public String redirectResultView(@RequestParam("urls") String urls, HttpSession session) {
        Map<String, Boolean> startedUrls = new HashMap<>();
        UrlValidator validator = new UrlValidator(new String[]{"http", "https"});
        for (String url : urls.split("\n")) {
            String trimmedUrl = withProtocol(url.trim());
            if (validator.isValid(trimmedUrl)) {
                String id = webifierTesterLauncher.launch(url);
                startedUrls.put(trimmedUrl, id != null);
            }
        }
        session.setAttribute(WebifierConstants.Session.STARTED, startedUrls);
        return "redirect:/batch/checked";
    }

    private String withProtocol(String url) {
        if (url.matches("^https?://.*")) {
            return url;
        }
        return "http://" + url;
    }

    @RequestMapping(value = "/batch/checked")
    public ModelAndView returnResultView(HttpSession session) {
        Object started = session.getAttribute(WebifierConstants.Session.STARTED);
        if (started == null) {
            return new ModelAndView("redirect:/batch");
        }
        ModelAndView result = new ModelAndView("batchresult");
        result.addObject("started", started);
        return result;
    }
}