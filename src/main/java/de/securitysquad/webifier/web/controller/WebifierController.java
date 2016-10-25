package de.securitysquad.webifier.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by samuel on 25.10.16.
 */
@Controller
public class WebifierController {
    @RequestMapping("/")
    public String returnIndexView() {
        return "index";
    }

    @RequestMapping("/check")
    public String returnResultView() {
        return "result";
    }
}