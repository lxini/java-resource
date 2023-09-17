package com.lagou.edu.service;

import com.lagou.edu.pojo.LagouUser;

public interface UserService {


    void saveUser(LagouUser user);

    LagouUser findByEmail(String email);

    LagouUser findByEmailAndPassword(String email,String password);
}
