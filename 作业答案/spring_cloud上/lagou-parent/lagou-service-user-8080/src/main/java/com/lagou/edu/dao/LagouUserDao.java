package com.lagou.edu.dao;


import com.lagou.edu.pojo.LagouToken;
import com.lagou.edu.pojo.LagouUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LagouUserDao extends JpaRepository<LagouUser,Long> {
    LagouUser findByEmail(String email);

    LagouUser findByEmailAndPassword(String email,String password);
}
