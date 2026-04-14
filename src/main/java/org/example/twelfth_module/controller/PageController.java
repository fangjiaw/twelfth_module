package org.example.twelfth_module.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String index() {
        return "redirect:/statistics";
    }

    @GetMapping("/statistics")
    public String statisticsPage() {
        return "statistics/index";
    }
}
