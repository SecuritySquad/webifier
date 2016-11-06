package de.securitysquad.webifier.web.controller;

import de.securitysquad.webifier.config.WebifierConstants;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

/**
 * Created by samuel on 25.10.16.
 */
@Controller
public class WebifierController {
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
                session.setAttribute(WebifierConstants.Session.CHECK_URL, url.toString());
                session.setAttribute(WebifierConstants.Session.CHECK_ID, UUID.randomUUID().toString());
                // TODO trigger check
                return "redirect:/checked";
            } catch (MalformedURLException e) {
                // url is invalid
            }
        }
        session.setAttribute("error", "url_invalid");
        return "redirect:/";
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