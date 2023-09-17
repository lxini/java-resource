package com.lagou.edu.service;

public interface TokenService {

    // 生成令牌,uuid模拟
    String createToken();

    // 保存token
    void saveToken(String email,String token);

    // 根据令牌获取邮箱
    String getEmail(String token);
}
