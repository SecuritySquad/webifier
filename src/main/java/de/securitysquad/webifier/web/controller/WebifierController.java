package de.securitysquad.webifier.web.controller;

import de.securitysquad.webifier.config.WebifierConstants;
import de.securitysquad.webifier.core.WebifierTesterLauncher;
import de.securitysquad.webifier.core.WebifierTesterResultListener;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;

/**
 * Created by samuel on 25.10.16.
 */
@Controller
public class WebifierController {
    private final WebifierTesterLauncher webifierTesterLauncher;
    private final WebifierTesterResultListener checkController;

    @Autowired
    public WebifierController(WebifierTesterLauncher webifierTesterLauncher, WebifierTesterResultListener checkController) {
        Assert.notNull(webifierTesterLauncher, "webifierTesterLauncher must not be null!");
        Assert.notNull(checkController, "checkController must not be null!");
        this.webifierTesterLauncher = webifierTesterLauncher;
        this.checkController = checkController;
    }

    @RequestMapping("/")
    public ModelAndView returnIndexView(HttpSession session) {
        ModelAndView result = new ModelAndView("index");
        Object error = session.getAttribute(WebifierConstants.Session.ERROR);
        if (error != null) {
            result.addObject("error", error);
            session.removeAttribute(WebifierConstants.Session.ERROR);
        }
        return result;
    }

    @RequestMapping(value = "/check", method = RequestMethod.POST)
    public String redirectResultView(@RequestParam("url") String url, HttpSession session) {
        String trimmedUrl = url.trim();
        UrlValidator validator = new UrlValidator(new String[]{"http", "https"});
        if (validator.isValid(withProtocol(trimmedUrl))) {
            if (!launchTester(trimmedUrl, session)) {
                session.setAttribute(WebifierConstants.Session.ERROR, "queue_full");
                return "redirect:/";
            }
            return "redirect:/checked";
        }
        session.setAttribute(WebifierConstants.Session.ERROR, "url_invalid");
        return "redirect:/";
    }

    private boolean launchTester(String url, HttpSession session) {
        String id = webifierTesterLauncher.launch(url, session, checkController);
        if (id == null)
            return false;
        session.setAttribute(WebifierConstants.Session.CHECK_URL, url);
        session.setAttribute(WebifierConstants.Session.CHECK_ID, id);
        return true;
    }

    private String withProtocol(String url) {
        if (url.matches("^https?://.*")) {
            return url;
        }
        return "http://" + url;
    }

    @RequestMapping(value = "/checked")
    public ModelAndView returnResultView(HttpSession session) {
        Object id = session.getAttribute(WebifierConstants.Session.CHECK_ID);
        Object url = session.getAttribute(WebifierConstants.Session.CHECK_URL);
        if (id == null || url == null) {
            return new ModelAndView("redirect:/");
        }
        ModelAndView result = new ModelAndView("result");
        result.addObject("check_url", url);
        result.addObject("check_id", id);
        return result;
    }
}