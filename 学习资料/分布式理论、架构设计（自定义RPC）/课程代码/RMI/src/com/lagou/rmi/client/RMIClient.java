package com.lagou.rmi.client;

import com.lagou.rmi.pojo.User;
import com.lagou.rmi.service.IUserService;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * 客户端
 */
public class RMIClient {
    public static void main(String[] args) throws RemoteException, NotBoundException {
        //1.获取Registry实例
        Registry registry = LocateRegistry.getRegistry("127.0.0.1", 9998);
        //2.通过Registry实例查找远程对象
        IUserService userService = (IUserService) registry.lookup("userService");
        User user = userService.getByUserId(2);
        System.out.println(user.getId() + "----" + user.getName());
    }
}
