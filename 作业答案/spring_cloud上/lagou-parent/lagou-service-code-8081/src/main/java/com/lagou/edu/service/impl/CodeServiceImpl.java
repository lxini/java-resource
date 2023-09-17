package com.lagou.edu.service.impl;

import com.lagou.edu.dao.LagouAuthCodeDao;
import com.lagou.edu.feignclients.EmailFeignClient;
import com.lagou.edu.pojo.LagouAuthCode;
import com.lagou.edu.service.CodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CodeServiceImpl implements CodeService {

    @Autowired
    private EmailFeignClient emailFeignClient;

    @Autowired
    private LagouAuthCodeDao lagouAuthCodeDao;

    /**
     * 生成验证码，随机六位数
     * @param email
     * @return
     */
    @Override
    public boolean createCode(String email) {
        try{
            // 生成验证码，存储到数据库，并发送到邮箱
            int code = (int) ((Math.random() * 9 + 1) * 100000);
            String codeStr = String.valueOf(code);
            // TODO 存入数据库
            LagouAuthCode lagouAuthCode = new LagouAuthCode();
            lagouAuthCode.setCode(codeStr);
            lagouAuthCode.setEmail(email);
            long currentTime = System.currentTimeMillis();
            lagouAuthCode.setCreatetime(String.valueOf(currentTime));
            lagouAuthCode.setExpiretime(String.valueOf(currentTime + 600*1000));
            lagouAuthCodeDao.save(lagouAuthCode);
            // 调用邮件微服务发送邮件
            emailFeignClient.sendEmail(email,codeStr);
            System.out.println("===============>>>>>验证码微服务，处理正常.....");
            return true;
        }catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * 查询最近一次为该邮箱生成的验证码，校验验证码是否正确，0正确1错误2超时
     * @param email
     * @param code
     * @return
     */
    @Override
    public String validateCode(String email, String code) {
        LagouAuthCode latestCode = lagouAuthCodeDao.findLatestCode(email);
        if(latestCode == null) {
            return "1";
        }
        long expireTime = Long.parseLong(latestCode.getExpiretime());
        long nowTime = System.currentTimeMillis();
        if(nowTime > expireTime) {
            return "2";
        }
        return "0";
    }
}
