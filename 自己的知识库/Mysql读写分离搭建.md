###  使用MySql Proxy搭建读写分离

- 下载安装包

  ```
  wget https://cdn.mysql.com/archives/mysql-proxy/mysql-proxy-0.8.5-linux-glibc2.3-x86-64bit.tar.gz
  ```

- 解压

  ```
  tar -zxvf mysql-proxy-0.8.5-linux-glibc2.3-x86-64bit.tar.gz
  ```

- 创建配置文件

  ```
   vim /opt/conf/mysql-proxy.cnf
  ```

- 配置项配置

  ```properties
  [mysql-proxy]
  #当前代理服务器用户
  user=root
  #代理mysql服务器用户
  admin-username=root
  #代理mysql服务器密码
  admin-password=tlr857
  #当前代理服务器ip地址
  proxy-address=127.0.0.1:4040
  #主库地址，多个主库用逗号隔开
  proxy-backend-addresses=192.168.230.128:3306
  #从库地址，多个从库用逗号隔开
  proxy-read-only-backend-addresses=192.168.230.129:3306
  #路由lua脚本
  proxy-lua-script=/opt/mysql-proxy-0.8.5-linux-glibc2.3-x86-64bit/share/doc/mysql-proxy/rw-splitting.lua
  #日志文件
  log-file=/var/log/mysql-proxy.log
  #日志级别
  log-level=debug
  #是否以守护进程方式运行
  daemon=false
  #高可用
  keepalive=true
  ```

- 设置配置文件权限

  ```
  chmod 660 /opt/conf/mysql-proxy
  ```

- 修改lua脚本，设置1个线程启用读写分离

  ```
  vim /opt/mysql-proxy-0.8.5-linux-glibc2.3-x86-64bit/share/doc/mysql-proxy/rw-splitting.lua
  ```

  ![image-20210122145812461](D:\Typora\images\image-20210122145812461.png)

- 启动代理服务器

  ```
  ./mysql-proxy --defaults-file=/opt/conf/mysql-proxy.cnf
  ```

  