package de.securitysquad.webifier.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by samuel on 25.10.16.
 */
@Controller
public class WebifierController {
    @RequestMapping("/")
    public String returnIndexView() {
        return "index";
    }

    @RequestMapping(value = "/check", method = RequestMethod.POST)
    public ModelAndView returnResultView(@RequestParam String url) {
        // TODO validate url
        ModelAndView result = new ModelAndView("result");
        result.addObject("url", url);
        return result;
    }
}