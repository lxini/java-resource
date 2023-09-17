package com.lagou.edu.service;

public interface EmailService {

    /**
     * 发送文本邮件
     * @param to 收件人
     * @param subject 主题
     * @param content 内容
     * @return
     */
    boolean sendSimpleMail(String to, String subject, String content);

}
