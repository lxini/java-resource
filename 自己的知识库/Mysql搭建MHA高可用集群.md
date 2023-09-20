##  MHA搭建Mysql高可用集群

###  一、环境&软件版本

|     环境&软件      |             版本             |
| :----------------: | :--------------------------: |
|   虚拟机&VMware    |    15.5.0 build-14665864     |
|   服务器&Centos    | CentOS-7-x86_64-Minimal-1708 |
|    数据库&Mysql    |            5.7.33            |
| 远程连接&MobaXterm |            v 20.3            |

###  二、环境架构

| 集群角色 |       IP        |     机器名称      |    权限    |
| :------: | :-------------: | :---------------: | :--------: |
|  master  | 192.168.230.128 | Centos7_01 (root) |    读写    |
|  slave1  | 192.168.230.129 | Centos7_02 (root) |    只读    |
|  slave2  | 192.168.230.130 | Centos7_03 (root) |    只读    |
|   mha    | 192.168.230.132 | Centos7_04 (root) | 高可用监控 |

###  三、Mysql主从配置

####  3.1 Mysql安装

根据步骤分别在master、slave1、slave2机器上安装mysql 5.7.33

- 下载并安装mysql官方的 Yum Repository

  ```properties
  # 下载rpm文件
  wget -i -c http://dev.mysql.com/get/mysql57-community-release-el7-10.noarch.rpm
  yum -y install mysql57-community-release-el7-10.noarch.rpm
  # 安装mysql服务器
  yum -y install mysql-community-server
  ```

- 配置mysql

  ```properties
  #首先配置密码策略，允许弱密码
  vi /etc/my.cnf
  # 添加validate_password_policy配置
  validate_password_policy=0
  # 关闭密码策略
  validate_password = off
  ```

  ```properties
  # 启动mysql服务
  systemctl start  mysqld.service
  # 查看默认密码
  grep "password" /var/log/mysqld.log
  # 登录mysql服务
  mysql -uroot -p
  # 修改密码
  ALTER USER 'root'@'localhost' IDENTIFIED BY 'new password';
  ```

- 关闭防火墙

  ```properties
  systemctl stop firewalld.service
  systemctl disable firewalld.service
  ```

####  3.2 Mysql主从配置

#####  3.2.1 配置主库

- 编辑my.cnf文件

  ```properties
  vi /etc/my.cnf
  # 添加如下配置
  # 设置binlog文件名
  log_bin=mysql-bin
  # 设置serverId，主库和从库不重复
  server-id=1
  sync-binlog=1
  #设置不同步的表
  binlog-ignore-db=performance_schema
  binlog-ignore-db=mysql
  binlog-ignore-db=sys
  binlog-ignore-db=information_schema
  #binlog-do-db=lagou 指定同步的数据库，不配置就是全同步
  #relay_log配置 
  relay_log=mysql-relay-bin 
  log_slave_updates=1 
  #禁止relay_log在线程执行完后被自动删除
  relay_log_purge=0 
  ```

- 查看主库信息

  ```properties
  show master status;
  #表头
  binlog文件名|指针位置|需同步的库| 不需同步的库
  ```

  ![image-20210118220010692](D:\Typora\images\image-20210118220010692.png)

- 授权从库远程连接、同步

  ```properties
  # 授权远程连接
  grant all privileges on *.* to 'root'@'%' identified by 'password' with grant option;
  # 授权从库同步
  grant replication slave on *.* to 'root'@'%' identified by 'password';
  # 刷新授权
  flush privileges;
  ```

#####  3.2.2 配置从库

- 编辑my.cnf文件

  ```properties
  vi /etc/my.cnf
  # 添加如下配置
  # 设置serverid，主从库不同
  server-id=2
  # 设置relay_log日志
  relay_log=mysql-relay-bin
  log_slave_updates=1
  # 设置只读
  read_only=1
  #禁止relay_log在线程执行完后被自动删除
  relay_log_purge=0 
  
  log_bin=mysql-bin
  sync-binlog=1
  #设置不同步的表
  binlog-ignore-db=performance_schema
  binlog-ignore-db=mysql
  binlog-ignore-db=sys
  binlog-ignore-db=information_schema
  ```

- 查看从库状态

  ```
  show slave status;
  ```

  ![image-20210118220800869](D:\Typora\images\image-20210118220800869.png)

  如果状态不为空，则说明之前有主从复制动作，先停止

  ```
  stop slave;
  ```

  设置同步的主库信息，master_log_file与master_log_pos字段值取主库查看状态时的值

  ![image-20210118221050317](D:\Typora\images\image-20210118221050317.png)

  ```
  change master to master_host='主库ip',master_port=3306,master_user='root',master_password='password',master_log_file='mysql-bin.000001',master_log_pos=589;
  ```

- 启动从库

  ```
  start slave;
  ```

- 查看从库状态

  ```
  show slave status \G;
  ```

  ![image-20210118223357825](D:\Typora\images\image-20210118223357825.png)

###  四、半同步复制设置

####  4.1 配置主库

- 查看是否有动态加载

  ```
  select @@have_dynamic_loading;
  ```

  ![image-20210119110716257](D:\Typora\images\image-20210119110716257.png)

- 安装半同步插件

  ```
  install plugin rpl_semi_sync_master soname 'semisync_master.so';
  ```

- 设置参数

  ```
  set global rpl_semi_sync_master_enabled=1;
  set global rpl_semi_sync_master_timeout=1000;
  ```

#### 4.2 配置从库

- 安装半同步插件

  ```
  install plugin rpl_semi_sync_slave soname 'semisync_slave.so';
  ```

- 设置参数

  ```
  set global rpl_semi_sync_slave_enabled=1;
  ```

- 重启从库

  ```
  stop slave;
  start slave;
  ```

###  五、MHA高可用搭建

####  5.1 集群内ssh互通

- 在四台服务器分别执行以下命令

  ```
  ssh-keygen -t rsa
  sh-copy-id -i ~/.ssh/id_rsa.pub root@192.168.230.129
  sh-copy-id -i ~/.ssh/id_rsa.pub root@192.168.230.130
  sh-copy-id -i ~/.ssh/id_rsa.pub root@192.168.230.132
  ```

####  5.2 MHA安装、配置

将课件中的MHA的Manager和Node软件包下载，分别上传到对应服务器。

三台Mysql服务器需要安装Node

MHA Manager服务器需要安装Manager和Node

- MHA Node安装

  在四台服务器上安装mha4mysql-node。

  ```
  #安装依赖
  yum -y install perl-DBD-MySQL 
  #安装node
  rpm -ivh mha4mysql-node-0.58-0.el7.centos.noarch.rpm
  ```

- MHA Manager安装

  ```
  #安装依赖
  yum install  perl-ExtUtils-CBuilder perl-ExtUtils-MakeMaker perl-CPAN perl-DBD-MySQL perl-Config-Tiny perl-Log-Dispatch perl-Parallel-ForkManager perl-Time-HiRes perl-DBI mysql-libs perl-Email-Date-Format perl-File-Remove perl-Mail-Sender perl-Mail-Sendmail perl-MIME-Lite perl-MIME-Types perl-Module-Install perl-Module-ScanDeps perl-YAML -y
  #安装manager
  rpm -ivh mha4mysql-manager-0.58-0.el7.centos.noarch.rpm
  ```

- 初始化MHA配置文件

  MHA Manager服务器需要为每个监控的 Master/Slave 集群提供一个专用的配置文件，而所有的Master/Slave 集群也可共享全局配置。

  - 初始化配置目录

    ```
    #目录说明 
    #/var/log (CentOS目录) 
    # /mha (MHA监控根目录) 
    # /app1 (MHA监控实例根目录) 
    # /manager.log (MHA监控实例日志文件) 
    mkdir -p /var/log/mha/app1 
    touch /var/log/mha/app1/manager.log
    ```

  - 配置监控全局配置文件

    ```
    vim /etc/masterha_default.cnf
    ```

    ```properties
    [server default] 
    #用户名 
    user=root 
    #密码 
    password=root 
    #ssh登录账号 
    ssh_user=root 
    #主从复制账号 
    repl_user=root 
    #主从复制密码 
    repl_password=root 
    #ping次数 
    ping_interval=1 
    #二次检查的主机 
    secondary_check_script=masterha_secondary_check -s 192.168.230.128 -s 192.168.230.129 -s 192.168.230.130
    ```

  - 配置监控实例配置文件

    ```
    mkdir /etc/mha
    vim /etc/mha/app1.cnf
    ```

    ```
    [server default] 
    #MHA监控实例根目录 
    manager_workdir=/var/log/mha/app1 
    #MHA监控实例日志文件 
    manager_log=/var/log/mha/app1/manager.log 
    #[serverx] 服务器编号 
    #hostname 主机名 
    #candidate_master 可以做主库 
    #master_binlog_dir binlog日志文件目录 
    [server1] 
    hostname=192.168.230.128
    candidate_master=1 
    master_binlog_dir="/var/lib/mysql" 
    [server2]
    hostname=192.168.230.129
    candidate_master=1 
    master_binlog_dir="/var/lib/mysql" 
    [server3] 
    hostname=192.168.230.130
    candidate_master=1 master_binlog_dir="/var/lib/mysql"5.3 MHA 配置检测
    ```

####  5.3 MHA配置检测

- 执行ssh通信检测

  在MHA Manager服务器上执行

  ```
  masterha_check_ssh --conf=/etc/mha/app1.cnf
  ```

- 检测Mysql主从复制

  在MHA Manager服务器上执行

  ```
  masterha_check_repl --conf=/etc/mha/app1.cnf
  ```

  出现“MySQL Replication Health is OK.”证明MySQL复制集群没有问题。

  ![image-20210123172747658](D:\Typora\images\image-20210123172747658.png)

####  5.4 MHA Manager启动

- 在MHA Manager服务器上执行

  ```
  nohup masterha_manager --conf=/etc/mha/app1.cnf --remove_dead_master_conf -- ignore_last_failover < /dev/null > /var/log/mha/app1/manager.log 2>&1 &
  ```

- 查看监控状态命令如下

  ```
  masterha_check_status --conf=/etc/mha/app1.cnf
  ```

- 查看监控日志命令如下

  ```
  tail -100f /var/log/mha/app1/manager.log
  ```

####  5.5 测试MHA高可用

- 模拟主节点崩溃

  - 关闭Master Mysql服务器，模拟主节点崩溃

    ```
    systemctl stop mysqld
    ```

  - 查看监控日志，观察主库切换情况

    ```
    tail -100f /var/log/mha/app1/manager.log
    ```

    ![image-20210123173338540](D:\Typora\images\image-20210123173338540.png)

- 将原Master启动，并切回原主库

  - 启动Mysql服务器

    ```
    systemctl start mysqld
    ```

  - 挂到新Master做从库

    ```
    change master to master_host='192.168.230.129',master_port=3306,master_user='root',master_password ='root',master_log_file='mysql-bin.000003 ',master_log_pos=426; 
    
    start slave; // 开启同步
    ```

    **注意：*master_log_file和master_log_pos两个参数需要去新主库查看，show master status \G;***

  - 将原主库信息添加回实例配置文件

    ```
    vim /etc/masterha_default.cnf
    ```

    ```
    [server1] 
    hostname=192.168.230.128
    candidate_master=1 
    master_binlog_dir="/var/lib/mysql" 
    ```

    

  - 使用MHA在线切换命令将原Master库切回

    ```
    masterha_master_switch --conf=/etc/mha/app1.cnf --master_state=alive --new_master_host=192.168.230.128 --new_master_port=3306 --orig_master_is_new_slave --running_updates_limit=10000
    ```

    

