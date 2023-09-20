###  构建环境

> jdk 1.8
>
> idea 2020.1.1
>
> gradle 5.6.3
>
> springframework 5.1.x

###  安装gradle

下载地址:https://gradle.org/releases/

> 下载安装包后解压并配置环境变量

### springframework 5.1.x源码构建

- 修改spring-framework-5.1.x\gradle\wrapper目录下的gradle-wrapper.properties文件distributionUrl属性值，指定gradle目录

```properties
#distributionUrl=https\://services.gradle.org/distributions/gradle-4.10.3-bin.zip
distributionUrl=file:///D:/environment/gradle-5.6.3-all.zip
```

- 查看Groovy、Kotlin的版本

> 打开命令控制窗口，输入gradle -v查看版本

![image-20201217134625552](D:\Typora\images\image-20201217134625552.png)

- 修改源码根目录下build.gradle文件

1. 第一行修改后

   ```groovy
   repositories {
   		maven { url "https://maven.aliyun.com/repository/spring-plugin" }
   		maven{ url "https://maven.aliyun.com/nexus/content/repositories/spring-plugin"}
   		maven { url "https://repo.spring.io/plugins-release" }
   }
   ```

   

2. 第151行修改后

   ```groovy
   repositories {
   		maven { url "https://maven.aliyun.com/repository/central" }
   		maven { url "https://repo.spring.io/libs-release" }
   		maven { url "https://repo.spring.io/snapshot" }
   		mavenLocal()
   	}
   ```

3. 修改33行Groovy、Kotlin的版本

   ![image-20201217135032035](D:\Typora\images\image-20201217135032035.png)

   

- 导入idea，配置jdk，gradle，编码等，等待jar包下载
- 编译croe -> oxm -> context -> beans -> aspects-> aop  compileTestJava



