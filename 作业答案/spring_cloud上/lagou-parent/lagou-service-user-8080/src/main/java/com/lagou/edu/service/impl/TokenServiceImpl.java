package com.lagou.edu.service.impl;

import com.lagou.edu.dao.LagouTokenDao;
import com.lagou.edu.pojo.LagouToken;
import com.lagou.edu.service.TokenService;
import com.netflix.discovery.converters.Auto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TokenServiceImpl implements TokenService {

    @Autowired
    private LagouTokenDao lagouTokenDao;

    @Override
    public String createToken() {
        return UUID.randomUUID().toString();
    }

    @Override
    public void saveToken(String email, String token) {
        LagouToken lagouToken = new LagouToken();
        lagouToken.setEmail(email);
        lagouToken.setToken(token);
        lagouTokenDao.save(lagouToken);
    }

    @Override
    public String getEmail(String token) {
        return lagouTokenDao.findByToken(token).getEmail();
    }
}
