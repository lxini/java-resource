package com.lagou.edu.feignclients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "lagou-service-email",path = "email")
public interface EmailFeignClient {

    @GetMapping("/{email}/{code}")
    public boolean sendEmail(@PathVariable("email") String email, @PathVariable("code") String code);
}
