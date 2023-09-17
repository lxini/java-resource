package com.lagou.service.impl;

import com.lagou.domain.Permission;
import com.lagou.domain.User;
import com.lagou.service.PermissionService;
import com.lagou.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 基于数据库完成认证
 */
@Service
public class MyUserDetailsService implements UserDetailsService {


    @Autowired
    UserService userService;

    @Autowired
    PermissionService permissionService;

    /**
     * 根据用户名查询用户
     *
     * @param username 前端传入的用户名
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户没有找到," + username);
        }
        // 权限的集合
        Collection<GrantedAuthority> authorities = new ArrayList<>();

        // 基于数据库查询用户对应的权限
        List<Permission> permissionList = permissionService.findByUserId(user.getId());
        for (Permission permission:permissionList) {
            authorities.add(new SimpleGrantedAuthority(permission.getPermissionTag()));
        }

        UserDetails userDetails = new org.springframework.security.core.userdetails.User
                (username, "{bcrypt}" + user.getPassword(),//noop不使用密码加密 , bcrypt使用加密算法
                        true,// 用户是否启用
                        true,// 用户是否过期
                        true,// 用户凭证是否过期
                        true,// 用户是否锁定
                        authorities);
        return userDetails;
    }

    public static void main(String[] args) {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String encode = bCryptPasswordEncoder.encode("123456");
        System.out.println(encode);
        String encode1 = bCryptPasswordEncoder.encode("123456");
        System.out.println(encode1);
    }
}
