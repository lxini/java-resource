package com.lagou.edu.service;

public interface CodeService {


    boolean createCode(String email);

    String validateCode(String email, String code);
}
