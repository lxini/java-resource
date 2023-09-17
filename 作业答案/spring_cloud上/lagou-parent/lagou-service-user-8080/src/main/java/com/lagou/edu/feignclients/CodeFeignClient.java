package com.lagou.edu.feignclients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "lagou-service-code",path = "code")
public interface CodeFeignClient {

    @GetMapping("/validate/{email}/{code}")
    public String validateCode(@PathVariable("email") String email,@PathVariable("code") String code);
}
