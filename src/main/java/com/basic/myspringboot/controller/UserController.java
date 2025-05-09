package com.basic.myspringboot.controller;

import com.basic.myspringboot.entity.User;
import com.basic.myspringboot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/index")
    public String index(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "index";
    }

    @GetMapping("/signup")
    public String showSignUpForm(@ModelAttribute("user") User user) {
        return "add-user";
    }

    @GetMapping("/thymeleaf")
    public String leaf(Model model) {
        model.addAttribute("name", "스프링부트!");
        return "leaf";
    }
}