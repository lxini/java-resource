package com.lagou.edu.controller;

import com.lagou.edu.feignclients.EmailFeignClient;
import com.lagou.edu.service.CodeService;
import com.netflix.discovery.converters.Auto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("code")
public class CodeController {

    @Autowired
    private CodeService codeService;

    @GetMapping("/create/{email}")
    public Boolean createCode(@PathVariable("email") String email) {
        return codeService.createCode(email);
    }


    @GetMapping("/validate/{email}/{code}")
    public String validateCode(@PathVariable("email") String email,@PathVariable("code") String code) {
        return codeService.validateCode(email,code);
    }
}
