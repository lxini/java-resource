package com.lagou.rmi.server;

import com.lagou.rmi.service.IUserService;
import com.lagou.rmi.service.UserServiceImpl;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * 服务端
 */
public class RMIServer {
    public static void main(String[] args) {
        try {
            //1.注册Registry实例. 绑定端口
            Registry registry = LocateRegistry.createRegistry(9998);
            //2.创建远程对象
            IUserService userService = new UserServiceImpl();
            //3.将远程对象注册到RMI服务器上即(服务端注册表上)
            registry.rebind("userService", userService);
            System.out.println("---RMI服务端启动成功----");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
