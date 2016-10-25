package de.securitysquad.webifier.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

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
    public String returnResultView(@RequestParam String url) {
        return "result";
    }
}