###  环境准备

####  Centos7安装mysql5.7

准备两台服务器（虚拟机），按步骤分别安装mysql5.7

- 下载并安装mysql官方的 Yum Repository

```shell
# 下载rpm文件
wget -i -c http://dev.mysql.com/get/mysql57-community-release-el7-10.noarch.rpm
yum -y install mysql57-community-release-el7-10.noarch.rpm
# 安装mysql服务器
yum -y install mysql-community-server
```

- 配置mysql

首先配置密码策略，允许弱密码

```shell
vi /etc/my.cnf
```

添加如下配置

```properties
# 添加validate_password_policy配置
validate_password_policy=0
# 关闭密码策略
validate_password = off
```

-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

```shell
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

```shell
systemctl stop firewalld.service
systemctl disable firewalld.service
```

###  配置主从复制

####  配置主库

编辑my.cnf文件

```shell
vi /etc/my.cnf
```

添加如下配置

```properties
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
```

查看主库信息

```sql
show master status;
binlog文件名|指针位置|需同步的库| 不需同步的库
```

![image-20210118220010692](D:\Typora\images\image-20210118220010692.png)

授权从库远程连接、同步

```sql
# 授权远程连接
grant all privileges on *.* to 'root'@'%' identified by 'password' with grant option;
# 授权从库同步
grant replication slave on *.* to 'root'@'%' identified by 'password';
# 刷新授权
flush privileges;
```

####  配置从库

编辑my.cnf文件

```shell
vi /etc/my.cnf
```

添加如下配置

```properties
# 设置serverid，主从库不同
server-id=2
# 设置relay_log日志
relay_log=mysql-relay-bin
# 设置只读
read_only=1
```

查看从库状态

```
show slave status;
```

![image-20210118220800869](D:\Typora\images\image-20210118220800869.png)

如果状态不为空，则说明之前有主从复制动作，需先停止

```
stop slave;
```

设置同步的主库信息

master_log_file与master_log_pos字段值取主库查看状态时的值

![image-20210118221050317](D:\Typora\images\image-20210118221050317.png)

```
 change master to master_host='主库ip',master_port=3306,master_user='root',master_password='password',master_log_file='mysql-bin.000001',master_log_pos=589;
```

启动主从复制

```
start slave;
```

查看从库状态

```
show slave status \G;
```

![image-20210118223357825](D:\Typora\images\image-20210118223357825.png)

### 设置半同步复制

#### 主库设置

查看是否动态加载

```
 select @@have_dynamic_loading;
```

![image-20210119110716257](D:\Typora\images\image-20210119110716257.png)

查看是否有半同步插件

```
show plugins;
```

![image-20210119110802857](D:\Typora\images\image-20210119110802857.png)

安装半同步插件

```
install plugin rpl_semi_sync_master soname 'semisync_master.so';
```

查看半同步复制参数

```
 show variables like '%semi%';
```

![image-20210119110922452](D:\Typora\images\image-20210119110922452.png)

设置参数

```
set global rpl_semi_sync_master_enabled=1;
set global rpl_semi_sync_master_timeout=1000;
```

#### 从库设置

安装半同步插件

```
install plugin rpl_semi_sync_slave soname 'semisync_slave.so';
```

查看半同步复制参数

```
 show variables like '%semi%';
```

![image-20210119111234541](D:\Typora\images\image-20210119111234541.png)

设置半同步复制参数

```
set global rpl_semi_sync_slave_enabled=1;
```

重启从库

```
stop slave;
start slave;
```

### 设置并行复制

####  设置主库

查看参数

```
show variables like '%binlog_group%';
```

![image-20210119112426731](D:\Typora\images\image-20210119112426731.png)

设置参数

```
set global  binlog_group_commit_sync_delay=1000;

set global binlog_group_commit_sync_no_delay_count=100;

```

####  设置从库

设置relay_log参数

```
vi /etc/my.cnf
```

添加以下配置 

```
slave_parallel_type='LOGICAL_CLOCK'
slave_parallel_workers=8
relay_log_recovery=1 
master_info_repository=TABLE
relay_log_info_repository=TABLE
```

重启mysql

```
systemctl restart mysqld;
```

###  搭建双主模式

#### 配置原主库

```
vi /etc/my.cnf
```

追加以下配置

```
#双主模式配置
relay_log=mysql-relay-bin
log_slave_updates=1
#主键自增步长开启，从1开始，步长为2
auto_increment_offset=1
auto_increment_increment=2
```

重启主库

```
systemctl restart mysqld;
```

#### 配置新主库

```
vi /etc/my.cnf
```

追加以下配置

```
log_bin=mysql-bin
server-id=3
sync-binlog=1
binlog-ignore-db=information_schema
binlog-ignore-db=performation_schema
binlog-ignore-db=sys
binlog-ignore-db=mysql

#双主模式配置
relay_log=mysql-relay-bin
log_slave_updates=1
#主键自增步长开启，从2开始，步长为2
auto_increment_offset=2
auto_increment_increment=2
```

重启主库

```
systemctl restart mysqld;
```

#### 分别在两个主库配置互为从库

```
 change master to master_host='主库ip',master_port=3306,master_user='root',master_password='password',master_log_file='mysql-bin.000001',master_log_pos=589;
```

```
start slave
```

