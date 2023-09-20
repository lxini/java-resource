# 方法1
如果是Oracle 10以后的版本，先运行安装程序进行卸载，你找找应该是有的，必须一个目录一个目录的选择，
删除后，把安装目录给删除掉，再运行注册清理工具，清理下注册表，重新解压缩安装包，重装即可

#  方法2，转载自网络（已经是最终解决版本），注册表清理从第三步开始即可。

1.关闭Oracle所有的服务,按【win+R】运行【services.msc】找到所有Oracle开头的服务,点击停止。
2.使用Oracle自带软件卸载Oracle程序。 点击【开始】->【程序】->【Oracle - OraDb11g_home1】->【Oracle 安装产品】->【Universal Installer】,点击【卸载产品】按钮后,勾选【Oracle主目录】-【OraDb11g_home1】-【Oracle Database 11g 11.2.0.1.0】最后一项后点击【删除】,删除完之后再勾选【Oracle主目录】-【OraDb11g_home1】项后点击【删除】。卸载完成 后点击关闭。
3.打开注册表程序,按【win+R】->输入【regedit】->【回车】,删除下面的路径/项/值:
3.1：HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Services\ 删除该路径下的所有以oracle开始的服务名称 (Oracle11+OracleDBConsoleorcl+OracleJobSchedulerORCL+OracleOraDb11g_home1TNSListener+OracleRemExecService+OracleServiceORCL+OracleVssWriterORCL)。

3.2：HKEY_LOCAL_MACHINE\SOFTWARE\ORACLE 删除该oracle目录,该目录下注册着Oracle数据库的软件安装信息。

3.3：HKEY_USERS\S-1-5-21-514346280-2349712288-2123507266-500\Software \Microsoft\Windows\CurrentVersion\Explorer\StartPage\NewShortcuts删除所有包含Oracle的快捷方式。

3.4:HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Services\Eventlog\Application 删除注册表的以oracle开头的所有项目(Oracle Services for MTS+Oracle.orcl+Oracle.VSSWriter.ORCL+OracleDBConsoleorcl)。

1. 删除环境变量有关oracle相关设置: 在桌面上用鼠标右键单击"我的电脑-->属性-->高级-->环境变量:删除下面的环境变量或者值

4.1删除NLS_LANG环境变量,默认值为SIMPLIFIED CHINESE_CHINA.ZHS16GBK
4.2删除ORACLE_HOME环境变量,默认值为C:\app\Administrator\product\11.2.0\dbhome_1
4.3删除ORACLE_SID环境变量,默认值为ORCL
4.4删除Path环境变量中的C:\app\Administrator\product\11.2.0\dbhome_1\bin;字符串
4.5删除TNS_ADMIN环境变量,默认值为C:\app\Administrator\product\11.2.0\dbhome_1\NETWORK\ADMIN
\5. 重新启动操作系统。
\6. 重启后删除Oracle程序相关目录。包括安装目录(C:\app)+系统配置目录(C:\Program Files\oracle) 。
\7. 重启后删除开始菜单下的Oracle项(C:\Users\Administrator\Oracle) 。