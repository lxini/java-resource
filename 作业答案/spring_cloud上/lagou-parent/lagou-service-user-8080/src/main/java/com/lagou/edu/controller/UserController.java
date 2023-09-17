package com.lagou.edu.controller;

import com.lagou.edu.feignclients.CodeFeignClient;
import com.lagou.edu.pojo.LagouUser;
import com.lagou.edu.service.TokenService;
import com.lagou.edu.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("user")
public class UserController {

    @Autowired
    private CodeFeignClient codeFeignClient;

    @Autowired
    private UserService userService;

    @Autowired
    private TokenService tokenService;


    /**
     * 注册接口，true成功，false失败
     * @param email
     * @param password
     * @param code
     * @return
     */
    @GetMapping("/register/{email}/{password}/{code}")
    public String register(@PathVariable("email") String email,@PathVariable("password") String password,
                            @PathVariable("code") String code,HttpServletResponse response) {
        if(isRegistered(email)) {
            return "-1"; // 已经注册过
        }
        // 校验验证码
        String validateCode = codeFeignClient.validateCode(email, code);
        if("1".equalsIgnoreCase(validateCode) || "2".equalsIgnoreCase(validateCode)) {
            return validateCode;
        }
        // 保存用户名+密码
        LagouUser lagouUser = new LagouUser();
        lagouUser.setEmail(email);
        lagouUser.setPassword(password);
        userService.saveUser(lagouUser);
        // 注册成功，颁发token令牌，存入数据库并写入cookie，重定向到欢迎页
        String token = tokenService.createToken();
        tokenService.saveToken(email,token);
        Cookie cookie = new Cookie("login_token",token);
        cookie.setPath("/");
        response.addCookie(cookie);
        Cookie emailCookie = new Cookie("login_email",email);
        emailCookie.setPath("/");
        response.addCookie(emailCookie);
        return "0";
    }

    /**
     * 登录接口，验证用户名密码合法性，根据用户名和密码生成token，token存入数据库，并写入cookie中，登录成功返回邮箱地址，重定向到欢迎页
     * @param email
     * @param password
     * @return
     */
    @GetMapping("/login/{email}/{password}")
    public Boolean login(@PathVariable("email") String email, @PathVariable("password") String password, HttpServletResponse response) throws IOException {
        LagouUser lagouUser = userService.findByEmailAndPassword(email, password);
        if(lagouUser != null) {
            // 注册成功，颁发token令牌，存入数据库并写入cookie，重定向到欢迎页
            String token = tokenService.createToken();
            tokenService.saveToken(email,token);
            Cookie cookie = new Cookie("login_token",token);
            cookie.setPath("/");
            response.addCookie(cookie);
            Cookie emailCookie = new Cookie("login_email",email);
            emailCookie.setPath("/");
            response.addCookie(emailCookie);
            return true;
        }
        return false;
    }

    /**
     * 是否已注册，根据邮箱判断,true代表已经注册过，false代表尚未注册
     * @param email
     * @return
     */
    @GetMapping("/isRegistered/{email}")
    public Boolean isRegistered(@PathVariable("email") String email) {
        LagouUser lagouUser = userService.findByEmail(email);
        if(lagouUser == null) {
            return false;
        }
        return true;
    }

    // 根据token查询用户登录邮箱接口
    @GetMapping("/info/{token}")
    public String getEmail(@PathVariable("token") String token) {
        return tokenService.getEmail(token);
    }
}
