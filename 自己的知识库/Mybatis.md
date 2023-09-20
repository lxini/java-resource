##  Mybatis 知识库

###  一、Mybatis 配置文件头

####  1.1 sqlMapConfig.xml配置文件头

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE configuration PUBLIC "-//mybatis.org//DTD Config 3.0//EN" "http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>
    <environments default="development">
        <environment id="development">
            <transactionManager type="JDBC"/>
            <dataSource type="POOLED">
                <property name="driver" value="com.mysql.jdbc.Driver"/>
                <property name="url" value="jdbc:mysql:///test"/>
                <property name="username" value="root"/>
                <property name="password" value="root"/>
            </dataSource>
        </environment>
    </environments>
    <mappers>
        <mapper resource="com/lagou/mapper/UserMapper.xml"/>
    </mappers>
</configuration>

```

####  1.2 Mapper.xml配置文件头

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
"http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="mapper">

</mapper>

```

###  二、自定义Mybatis

####  2.1 JDBC存在的问题

```java
public static void main(String[] args) {
    Connection connection = null;
    PreparedStatement preparedStatement = null;
    ResultSet resultSet = null;
    try {
        // 加载数据库驱动
        Class.forName("com.mysql.jdbc.Driver");
        // 通过驱动管理类获取数据库链接
        connection =
        DriverManager.getConnection("jdbc:mysql://localhost:3306/mybatis?
        characterEncoding=utf-8", "root", "root");
        // 定义sql语句？表示占位符
        String sql = "select * from user where username = ?";
        // 获取预处理statement
        preparedStatement = connection.prepareStatement(sql);
        // 设置参数，第一个参数为sql语句中参数的序号(从1开始)，第二个参数为设置的参数值
        preparedStatement.setString(1, "tom");
        // 向数据库发出sql执行查询，查询出结果集
        resultSet = preparedStatement.executeQuery();
        // 遍历查询结果集
        while (resultSet.next()) {
        int id = resultSet.getInt("id");
        String username = resultSet.getString("username");
        // 封装User
        user.setId(id);
        user.setUsername(username);
        }
        System.out.println(user);
        }
    } catch (Exception e) {
    	e.printStackTrace();
    } finally {
        // 释放资源
        if (resultSet != null) {
            try {
            	resultSet.close();
            } catch (SQLException e) {
            	e.printStackTrace();
            }
        }
        if (preparedStatement != null) {
            try {
            	preparedStatement.close();
            } catch (SQLException e) {
            	e.printStackTrace();
            }
        }
        if (connection != null) {
            try {
            	connection.close();
            } catch (SQLException e) {
            	e.printStackTrace();
        }
    }
}
```

**jdbc问题总结**

- 频繁创建、释放数据库连接，造成资源浪费，影响程序性能。
  - 解决方案：使用数据库连接池管理数据库连接。
- sql语句、预处理statement设置占位符硬编码，实际使用维护不易，更改sql需要修改java代码。
  - 解决方案：使用配置文件。
- 设置返回结果集硬编码

