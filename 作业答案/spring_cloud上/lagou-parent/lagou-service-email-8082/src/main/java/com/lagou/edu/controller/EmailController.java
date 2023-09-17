package com.lagou.edu.controller;

import com.lagou.edu.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("email")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @GetMapping("/{email}/{code}")
    public boolean sendEmail(@PathVariable("email") String email,@PathVariable("code") String code) {
        System.out.println("========================>>>>>>邮件服务被调用，发送邮件中..........");
        return emailService.sendSimpleMail(email,"AuthCode",code);
    }
}
