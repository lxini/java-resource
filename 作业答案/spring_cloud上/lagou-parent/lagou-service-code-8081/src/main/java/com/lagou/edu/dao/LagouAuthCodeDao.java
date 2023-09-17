package com.lagou.edu.dao;


import com.lagou.edu.pojo.LagouAuthCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LagouAuthCodeDao extends JpaRepository<LagouAuthCode,Long> {

    // 根据邮箱查询最近一个验证码
    @Query(value = "select * from lagou_auth_code where email = ?1 order by createtime desc limit 1",nativeQuery = true)
    LagouAuthCode findLatestCode(String email);
}
