package ru.arbis29.passstorage.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class MainController {
    @GetMapping("/")
    public String rootApp(Model model, Principal principal){
        return "rootApp";
    }

}
