package com.lagou.filter;

import com.lagou.controller.ValidateCodeController;
import com.lagou.exception.ValidateCodeException;
import com.lagou.service.impl.MyAuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 验证码过滤器 OncePerRequestFilter 一次请求只会经过一次过滤器
 */
@Component
public class ValidateCodeFilter extends OncePerRequestFilter {

    @Autowired
    MyAuthenticationService myAuthenticationService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 判断是否是登录请求
        if (request.getRequestURI().equals("/login") &&
                request.getMethod().equalsIgnoreCase("post")) {
            String imageCode = request.getParameter("imageCode");
            System.out.println(imageCode);
            // 具体的验证流程....
            try{
                validate(request, imageCode);
            }catch (ValidateCodeException e){
                myAuthenticationService.onAuthenticationFailure(request,
                        response,e);
                return;
            }

        }

        // 如果不是登录请求直接放行
        filterChain.doFilter(request, response);
    }

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    private void validate(HttpServletRequest request, String imageCode) {
        //从redis中获取验证码
        String redisKey = ValidateCodeController.REDIS_KEY_IMAGE_CODE + "-" + request.getRemoteAddr();
        String redisImageCode = stringRedisTemplate.boundValueOps(redisKey).get();
        // 验证码的判断
        if (!StringUtils.hasText(imageCode)) {
            throw new ValidateCodeException("验证码的值不能为空!");
        }
        if (redisImageCode == null) {
            throw new ValidateCodeException("验证码已过期!");
        }
        if (!redisImageCode.equals(imageCode)) {
            throw new ValidateCodeException("验证码不正确!");
        }
        //从redis中删除验证码
        stringRedisTemplate.delete(redisKey);
    }

}
