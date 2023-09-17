package com.lagou.edu.service.impl;

import com.lagou.edu.dao.LagouUserDao;
import com.lagou.edu.pojo.LagouUser;
import com.lagou.edu.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private LagouUserDao lagouUserDao;

    @Override
    public void saveUser(LagouUser user) {
        lagouUserDao.save(user);
    }

    @Override
    public LagouUser findByEmail(String email) {
        return lagouUserDao.findByEmail(email);
    }

    @Override
    public LagouUser findByEmailAndPassword(String email, String password) {
        return lagouUserDao.findByEmailAndPassword(email,password);
    }
}
