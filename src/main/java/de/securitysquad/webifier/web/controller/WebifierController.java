package de.securitysquad.webifier.web.controller;

import de.securitysquad.webifier.config.WebifierConstants;
import de.securitysquad.webifier.core.WebifierTestResultListener;
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
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by samuel on 25.10.16.
 */
@Controller
public class WebifierController {
    private final WebifierTesterLauncher webifierTesterLauncher;
    private final WebifierTestResultListener checkController;

    @Autowired
    public WebifierController(WebifierTesterLauncher webifierTesterLauncher, WebifierTestResultListener checkController) {
        Assert.notNull(webifierTesterLauncher, "webifierTesterLauncher must not be null!");
        Assert.notNull(checkController, "checkController must not be null!");
        this.webifierTesterLauncher = webifierTesterLauncher;
        this.checkController = checkController;
    }

    @RequestMapping("/")
    public ModelAndView returnIndexView(HttpSession session) {
        ModelAndView result = new ModelAndView("index");
        Object error = session.getAttribute("error");
        if (error != null) {
            result.addObject("error", error);
            session.removeAttribute("error");
        }
        return result;
    }

    @RequestMapping(value = "/check", method = RequestMethod.POST)
    public String redirectResultView(@RequestParam("url") String urlParameter, HttpSession session) {
        UrlValidator validator = new UrlValidator(new String[]{"http", "https"});
        String urlWithProtocol = withProtocol(urlParameter);
        if (validator.isValid(urlWithProtocol)) {
            try {
                URL url = new URL(urlWithProtocol);
                launchTester(url, session);
                return "redirect:/checked";
            } catch (MalformedURLException e) {
                // url is invalid
            }
        }
        session.setAttribute("error", "url_invalid");
        return "redirect:/";
    }

    private void launchTester(URL url, HttpSession session) {
        String id = webifierTesterLauncher.launch(url, session, checkController);
        session.setAttribute(WebifierConstants.Session.CHECK_URL, url.toString());
        session.setAttribute(WebifierConstants.Session.CHECK_ID, id);
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