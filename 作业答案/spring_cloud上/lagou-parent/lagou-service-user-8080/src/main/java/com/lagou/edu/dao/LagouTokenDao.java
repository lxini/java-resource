package com.lagou.edu.dao;


import com.lagou.edu.pojo.LagouToken;
import com.lagou.edu.pojo.LagouUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LagouTokenDao extends JpaRepository<LagouToken,Long> {

    LagouToken findByToken(String token);
}
