





# Part 1 课程回顾

## 1.1 微服务架构图

![image-20200923212541551](D:\直播\06期-Spring Cloud part2 知识拓展.assets\image-20200923212541551.png)

**第一部分：**

- Eureka：服务注册与发现

- Ribbon：负载均衡

- Hystrix：服务熔断降级

- Hystrix Dashboard：断路监控仪表盘

- Hystrix Turbine：聚合监控

- Feign：服务远程调用

- GateWay：网关

- Spring Cloud Config：分布式配置中心

- Spring Cloud Config + Bus ：配置自动刷新

  

**第二部分：**

-  Sleuth + Zipkin ：分布式链路追踪技术
- Oauth2 + JWT ：微服务统一认证方案
- Nacos ：服务注册和配置中心
- Sentinel ：分布式系统的流量防卫兵





# Part 2 知识拓展

## 2.1 Hystrix线程池配置

```java
@HystrixCommand(
            // 线程池标识，要保持唯一，不唯一的话就共用了
            threadPoolKey = "findResumeOpenStateTimeout",
            // 线程池细节属性配置
            threadPoolProperties = {
                    @HystrixProperty(name="coreSize",value = "1"), // 线程数
                    @HystrixProperty(name="maxQueueSize",value="20") // 等待队列长度
            },
            // commandProperties熔断的一些细节属性配置
            commandProperties = {
                    // 每一个属性都是一个HystrixProperty
                    @HystrixProperty(name="execution.isolation.thread.timeoutInMilliseconds",value="2000")
            }
    )
```



### 2.1.1 配置误区

误区一：

```yaml
hystrix:
  threadpool:
    default:
      coreSize: 18 #并发执行的最大线程数，默认10
```

误区二：

```yaml
hystrix:
  threadpool:
    default:
      coreSize: 18 #并发执行的最大线程数，默认10
      maxQueueSize: 1000 #BlockingQueue的最大队列数，默认值-1
```



**coreSize：**

​	线程池核心线程数，默认为10



**maxQueueSize：**

​	最大排队长度，默认-1（如果maxQueueSize=-1的话，则该选项不起作用）



**queueSizeRejectionThreshold：**

​	 此属性设置队列大小拒绝阈值 - 即使未达到maxQueueSize也将发生拒绝的人为最大队列大小。 此属性存在，因为BlockingQueue的maxQueueSize不能动态更改，我们希望允许您**动态更改影响拒绝的队列大小**。

​	默认值为5，如果不指定值也就意味着，当线程队列中有五个请求时，之后的所有请求都会被拒绝。



**相对正确的设置：**

```yaml
hystrix:
  threadpool:
    default:
      coreSize: 10 #并发执行的最大线程数，默认10
      maxQueueSize: 1500 #BlockingQueue的最大队列数，默认值-1
      queueSizeRejectionThreshold: 1000 #拒绝阈值
```



### 2.1.2 推荐配置

关于Hystrix线程池配置没有通用答案，具体问题具体分析。

- 线程池默认大小为10
- spring cloud 官方文档对于hystrix线程池配置的建议是10-20个
- CPU核数 

注意：core默认为10，不代表每秒处理请求的能力为10。Hystrix线程池的配置取决于接口性能及设置超时时间等因素。

 

## 2.2 限流算法

保护高并发系统的三把利器：限流、缓存、降级。



以服务的调用方来看，可以分为两种类型服务

- 对外提供的服务（web服务）
  - 1.用户增长过快
  - 2.热点事件
  - 3.爬虫
  - 4.刷单
- 对内提供的服务（微服务之间调用）
  一个服务A的接口假如被B、C、D、E等多个服务进行调用，如果B服务发生突发流量，就会直接把A服务调用挂了，会导致C、D、E无法正常使用。
  解决方案：
  - 1.每个调用方采用线程池进行资源隔离（避免资源被耗尽无法分配资源）
  - 2.使用限流手段对每个调用方进行限流
  - 3.服务降级



在大型互联网应用中，为了应对巨大流量的瞬间提交,我们会做对应的限流处理，常见的限流算法：

​	1、计数器（固定窗口）算法
​	2、滑动窗口算法
​	3、漏桶算法
​	4、令牌桶算法

### 2.2.1 固定窗口算法（Fixed Window）



<img src="D:\直播\06期-Spring Cloud part2 知识拓展.assets\image-20200923225257631.png" alt="image-20200923225257631" style="zoom:80%;" />

实现原理：

​	实现方式也比较简单。就是通过维护一个单位时间内的计数值，每当一个请求通过时，就将计数值加1，当计数值超过预先设定的阈值时，就拒绝单位时间内的其他请求。如果单位时间已经结束，则将计数器清零，开启下一轮的计数。

```java
public class FixedWindow {
 
	private long time = new Date().getTime();
 
	private Integer count = 0; // 计数器
 
	private final Integer max = 100; // 请求阈值
 
	private final Integer interval = 1000; // 窗口大小
 
	public boolean trafficMonitoring() {
 
		long nowTime = new Date().getTime();
 
		if (nowTime < time + interval) {
			// 在时间窗口内
			count++;
 
			return max > count;
 
		} else {
			time = nowTime; // 开启新的窗口
 
			count = 1; // 初始化计数器,由于这个请求属于当前新开的窗口，所以记录这个请求
 
			return true;
		}
	}
 
}
```



临界值问题：

​	假设我们设定1秒内允许通过的请求阈值是100，如果有用户在时间窗口的最后几毫秒发送了100个请求，紧接着又在下一个时间窗口开始时发送了100个请求，那么这个用户其实在一秒内成功请求了200次，显然超过了阈值但并不会被限流。其实这就是临界值问题，那么临界值问题要怎么解决呢？



### 2.2.2 滑动窗口算法（Sliding Window）

​	计数器滑动窗口法就是为了解决上述固定窗口计数存在的问题而诞生，滑动窗口是基于时间来划分窗口的。

实现原理：

​	前面说了固定窗口存在临界值问题，要解决这种临界值问题，显然只用一个窗口是解决不了问题的。假设我们仍然设定1秒内允许通过的请求是200个，但是在这里我们需要把1秒的时间分成多格，假设分成5格（格数越多，流量过渡越平滑），每格窗口的时间大小是200毫秒，每过200毫秒，就将窗口向前移动一格。为了便于理解，可以看下图

​                                      	![image-20200924102756480](D:\直播\06期-Spring Cloud part2 知识拓展.assets\image-20200924102756480.png)

​	图中将窗口划为5份，每个小窗口中的数字表示在这个窗口中请求数，所以通过观察上图，可知在当前时间快（200毫秒）允许通过的请求数应该是20而不是200（只要超过20就会被限流），因为我们最终统计请求数时是需要把当前窗口的值进行累加，进而得到当前请求数来判断是不是需要进行限流。

​	**滑动窗口限流法其实就是计数器固定窗口算法的一个变种**。流量的过渡是否平滑依赖于我们设置的窗口格数也就是统计时间间隔，格数越多，统计越精确，但是具体要分多少格......



### 2.2.3 漏桶算法（Leaky Bucket）

![image-20200924102938647](D:\直播\06期-Spring Cloud part2 知识拓展.assets\image-20200924102938647.png)

为了消除"突刺现象"，可以采用漏桶算法实现限流，漏桶算法这个名字就很形象，算法内部有一个容器，类似生活用到的漏斗，当请求进来时，相当于水倒入漏斗，然后从下端小口慢慢匀速的流出。不管上面流量多大，下面流出的速度始终保持不变。

不管服务调用方多么不稳定，通过漏桶算法进行限流，每10毫秒处理一次请求。因为处理的速度是固定的，请求进来的速度是未知的，可能突然进来很多请求，没来得及处理的请求就先放在桶里，既然是个桶，肯定是有容量上限，如果桶满了，那么新进来的请求就丢弃。

​	在算法实现方面，可以准备一个队列，用来保存请求，另外通过一个线程池定期从队列中获取请求并执行，可以一次性获取多个并发执行。这种算法，在使用过后也存在弊端：无法应对短时间的突发流量。



### 2.2.4 令牌桶算法（Token Bucket）

从某种意义上讲，令牌桶算法是对漏桶算法的一种改进，桶算法能够限制请求调用的速率，而令牌桶算法能够在限制调用的平均速率的同时还允许一定程度的突发调用。

​	![image-20200924103022384](D:\直播\06期-Spring Cloud part2 知识拓展.assets\image-20200924103022384.png)

​	令牌产生的速度是固定的	

​	在令牌桶算法中，存在一个桶，用来存放固定数量的令牌。算法中存在一种机制，以一定的速率往桶中放令牌。每次请求调用需要先获取令牌，只有拿到令牌，才有机会继续执行，否则选择选择等待可用的令牌、或者直接拒绝。

​	放令牌这个动作是持续不断的进行，如果桶中令牌数达到上限，就丢弃令牌，所以就存在这种情况，桶中一直有大量的可用令牌，这时进来的请求就可以直接拿到令牌执行，比如设置QPS为100，那么限流器初始化完成一秒后，桶中就已经有100个令牌了，这时服务还没完全启动好，等启动完成对外提供服务时，该限流器可以抵挡瞬时的100个请求。所以，只有桶中没有令牌时，请求才会进行等待，最后相当于以一定的速率执行。

​	令牌桶算法算是漏斗算法的改进版,为了处理短时间的突发流量而做了优化,令牌桶算法主要由三部分组成`令牌流`、`数据流`、`令牌桶`.



名词释义:

- 令牌流：流通令牌的管道,用于生成的令牌的流通,放入令牌桶中
- 数据流：进入系统的数据流量
- 令牌桶：保存令牌的区域，可以理解为一个缓存区：令牌保存在这里用于使用



算法原理：

​	令牌桶算法会按照一定的速率生成令牌放入令牌桶，访问要进入系统时，需要从令牌桶获取令牌，有令牌的可以进入，没有的被抛弃。由于令牌桶的令牌是源源不断生成的，当访问量小时，可以留存令牌达到令牌桶的上限，这样当短时间的突发访问量来时，积累的令牌数可以处理这个问题。当访问量持续大量流入时，由于生成令牌的速率是固定的，最后也就变成了类似漏斗算法的固定流量处理。



### 2.2.5 令牌桶和漏桶对比：

令牌桶是按照固定速率往桶中添加令牌，请求是否被处理需要看桶中令牌是否足够，当令牌数减为零时则拒绝新的请求；漏桶则是按照常量固定速率流出请求，流入请求速率任意，当流入的请求数累积到漏桶容量时，则新流入的请求被拒绝；

令牌桶限制的是平均流入速率，允许突发请求，只要有令牌就可以处理；漏桶限制的是常量流出速率，即流出速率是一个固定常量值，比如都是1的速率流出，而不能一次是1，下次又是2，从而平滑突发流入速率；

令牌桶允许一定程度的突发，而漏桶主要目的是平滑流出速率；



### 2.2.6 Guava

​	Google Guava工程包含了若干被Google的 Java项目广泛依赖 的核心库，例如：集合 [collections] 、缓存 [caching] 、原生类型支持 [primitives support] 、并发库 [concurrency libraries] 、通用注解 [common annotations] 、字符串处理 [string processing] 、I/O 等等。 

​	Guava官方文档-RateLimiter类

​	**RateLimiter使用的是一种叫令牌桶的流控算法，RateLimiter会按照一定的频率往桶里扔令牌，线程拿到令牌才能执行，比如你希望自己的应用程序QPS不要超过1000，那么RateLimiter设置1000的速率后，就会每秒往桶里扔1000个令牌,RateLimiter经常用于限制对一些物理资源或者逻辑资源的访问速率。。**

```xml
<dependency>
  <groupId>com.google.guava</groupId>
  <artifactId>guava</artifactId>
  <version>29.0-jre</version>
  <!-- or, for Android: -->
  <version>29.0-android</version>
</dependency>
```



![image-20200924120300383](D:\直播\06期-Spring Cloud part2 知识拓展.assets\image-20200924120300383.png)

![image-20200924120328553](D:\直播\06期-Spring Cloud part2 知识拓展.assets\image-20200924120328553.png)



```java
/**
 * 模拟RateLimiter限流
 */
public class TestRateLimiter {
    public static void main(String[] args) {
        //0.5代表一秒最多多少个
        RateLimiter rateLimiter = RateLimiter.create(0.5);
        List<Runnable> tasks = new ArrayList<Runnable>();
        for (int i = 0; i < 10; i++) {
            tasks.add(new UserRequest(i));
        }
        ExecutorService threadPool = Executors.newCachedThreadPool();
        for (Runnable runnable : tasks) {
            System.out.println("等待时间：" + rateLimiter.acquire());
            threadPool.execute(runnable);
        }
    }

    private static class UserRequest implements Runnable {
        private int id;

        public UserRequest(int id) {
            this.id = id;
        }

        public void run() {
            System.out.println("userQuestID:"+id);
        }
    }

}

```



## 微服务获取真实请求的IP



1、Nginx

```
http {
    include       mime.types;
    default_type  application/octet-stream;

  upstream gwadds {
  server 127.0.0.1:81;
}

server {
  listen 80;
  server_name  gw.com;
  location / {
    proxy_pass http://gwadds/;
    proxy_set_header   Host             $host;
    proxy_set_header   X-Real-IP        $remote_addr;                        
    proxy_set_header   X-Forwarded-For  $proxy_add_x_forwarded_for;

  }
}
```



```java
@RestController
@RequestMapping("/member")
public class MemberController {

    @PostMapping("/login")
    BaseResponse<JSONObject> login(@RequestBody UserLoginDto userLoginDto, @RequestHeader("X-Real-IP")
            String sourceIp, @RequestHeader("channel") String channel, @RequestHeader("deviceInfor") String deviceInfor);
}
```





压力测试





# Part 2 作业指导

根据如下描述，改造Spring Cloud（上）的作业，完成练习
1)Eureka注册中心  替换为  Nacos注册中心
2)Config+Bus配置中心 替换为 Nacos配置中心
3)Feign调用 替换为 Dubbo RPC调用
4)使用Sentinel对GateWay网关的入口资源进行限流（限流参数自定义并完成测试即可）   
注意：
1）所有替换组件使用单节点即可
2）提交作业时只需要提交代码部分即可



# Part 3 问题答疑

### 1 dubbo rpc和feign用哪个好?

​	RPC和HTTP，RPC的中文意思是远程过程调用，HTTP是一种应用层传输协议。二者不是一个层面的东西，所以我们一般会称呼RPC为框架，HTTP为协议，在RPC框架中可以选择HTTP作为传输协议。

​	RPC说的简单一点，远程过程调用其实描述的是一件事儿，一件客户端如何调用服务端的事儿。在这件事儿里，包括了很多内容，例如：序列化和反序列化协议怎么弄，传输协议选什么等等。说到这里，**把RPC称做调用调用远程过程是不是更合适一点。**常见的RPC框架：



|      实现框架       | 使用特点                                                     |
| :-----------------: | ------------------------------------------------------------ |
|      Dubbo RPC      | 微服务是一种架构思想，其中涉及到的远远不止RPC这么一件事儿，而Dubbo除了RPC和简单的服务治理外，并没有更多的可以服务于微服务的东西；而Spring Cloud被大家成为全家桶，那是因为在Spring Cloud为微服务提供了近乎完整组件（虽然Spring Cloud把自己称为搬运工，但是大家还是喜欢用全家桶来称呼他）。Dubbo还是很厉害的，在序列化、传输上都提供了丰富的选择。我个人认为Dubbo做的比其他RPC框架好的一点是，引入了注册中心的概念，从此开发小哥就摆脱了疯狂配置的工作；此外，在大厂影响力的加持下，在过去的某一段时间里，Dubbo被炒的沸沸扬扬，在国内的软件行业中占有相当大的比重。**在Dubbo默认的配置中，传输层并没有使用HTTP协议，而是使用了传统的TCP协议。** |
| String Http Invoker | 优势就是在Spring体系中使用非常简单，缺点是效率不行。从Http Invoker的名字，我们都可以发现，在Http Invoker的传输层，也是使用了HTTP协议。 |
|        Feign        | Feign是Spring Cloud全家桶中推荐使用的RPC框架，但是Feign也是使用了HTTP作为传输层协议的。 |



### 2 用sentinel对网关gateway进行限流

**说明： sentinel可以作为各微服务的限流，也可以作为gateway网关的限流组件。 spring cloud gateway有限流功能，但此处用sentinel来作为替待。**

**说明：sentinel流控可以放在gateway网关端，也可以放在各微服务端。**



url: http://lagou-service-gateway/page/create/



**1)Gateway限流方案：**

​	RequestRateLimiterGatewayFilterFactory：适用Redis和lua脚本实现了令牌桶的方式

```xml

 <dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifatId>spring-boot-starter-data-redis-reactive</artifactId>
</dependency>
```



```yaml

server:
  port: 8081
spring:
  cloud:
    gateway:
      routes:
      - id: limit_route
        uri: http://httpbin.org:80/get
        predicates:
        - After=2017-01-20T17:42:47.789-07:00[America/Denver]
        filters:
        - name: RequestRateLimiter
          args:
            # #自动以用于限流的键的解析器的 Bean 对象的名字。它使用 SpEL 表达式根据#{@beanName}从 Spring 容器中获取 Bean 对象。
            key-resolver: '#{@hostAddrKeyResolver}'
            redis-rate-limiter.replenishRate: 10     # 令牌桶每秒填充平均速率。
            redis-rate-limiter.burstCapacity: 10000  #令牌桶总容量。
  application:
    name: gateway-limiter
  redis:
    host: localhost
    port: 6379
    database: 0

```

```java
//据Hostname进行限流
public class HostAddrKeyResolver implements KeyResolver {

    @Override
    public Mono<String> resolve(ServerWebExchange exchange) {
        return Mono.just(exchange.getRequest().getRemoteAddress().getAddress().getHostAddress());
    }

}
//注册到容器
 @Bean
    public HostAddrKeyResolver hostAddrKeyResolver() {
        return new HostAddrKeyResolver();
    }


public class UriKeyResolver  implements KeyResolver {

    @Override
    public Mono<String> resolve(ServerWebExchange exchange) {
        return Mono.just(exchange.getRequest().getURI().getPath());
    }

}
 @Bean
    public UriKeyResolver uriKeyResolver() {
        return new UriKeyResolver();
    }

```



**2)sentinel作为gateway**

```xml
　　　　 <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-gateway</artifactId>
        </dependency>
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
        </dependency>
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-alibaba-sentinel-gateway</artifactId>
        </dependency>
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
        </dependency>
```



**Sentinel 1.6.0 引入了 Sentinel API Gateway Adapter Common 模块，此模块中包含网关限流的规则和自定义 API 的实体和管理逻辑：**

```xml
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>
<dependency>
	<groupId>com.alibaba.csp</groupId>
	<artifactId>sentinel-spring-cloud-gateway-adapter</artifactId>
	<version>1.6.0</version>
</dependency>
```



```yaml
server:
  port: 9092
spring:
  cloud:
    nacos:
      discovery:
        register-enabled: false
        server-addr: localhost:8848
        namespace: c22e5019-0bee-43b1-b80b-fc0b9d847501
    sentinel:
      transport:
        dashboard: localhost:8080
        port: 8719
      scg:
        fallback:
          mode: response
          response-status: 455
          response-body: error!
    gateway:
      routes:
        - id: demo_route
          uri: lb://demo
          predicates:
            - Path=/demo/**
        - id: demo2_test
          uri: lb://demo2
          predicates:
            - Path=/user/**
  application:
    name: gateway-sentinel
```

启动后，在sentinel控制台可以看到 gateway-sentinel 应用，可以通过控制台设置流控规则。



**3) Sentinel 1.6.3 正式发布，引入网关流控控制台的支持，同时带来一些 bug 修复和功能改进，欢迎使用！**

参考：https://github.com/alibaba/Sentinel/wiki/网关限流

- 在Sentinel仪表板中添加对管理网关流规则和自定义API组的支持
- 添加`Ordered`Spring Cloud Gateway过滤器的界面支持

在 API Gateway 端，用户只需要在[原有启动参数](https://github.com/alibaba/Sentinel/wiki/控制台#32-配置启动参数)的基础上添加如下启动参数即可标记应用为 API Gateway 类型：

```shell
-Dcsp.sentinel.app.type=1
```



### 3 nacos 的集群leader 节点的配置信息是否也会同步到fllower节点，同步的原理是什么，以及怎么选举leader？选举的时候也会对外停止服务？

Nacos集群采用 **raft** 算法来实现，包括数据的处理和数据同步。

![image-20200924152127783](D:\直播\06期-Spring Cloud part2 知识拓展.assets\image-20200924152127783.png)

可以看出所有节点启动时都是follower状态；在一段时间内如果没有收到来自leader的心跳，从follower切换到candidate，发起选举；如果收到majority的造成票（含自己的一票）则切换到leader状态；如果发现其他节点比自己更新，则主动切换到follower。

   总之，系统中最多只有一个leader，如果在一段时间里发现没有leader，则大家通过选举-投票选出leader。leader会不停的给follower发心跳消息，表明自己的存活状态。如果leader故障，那么follower会转换成candidate，重新选出leader。

​		

所有节点启动时都是follower状态；

在一段时间内如果没有收到来自leader的心跳，从follower切换到candidate，发起选举；

如果收到majority的造成票（含自己的一票）则切换到leader状态；如果发现其他节点比自己更新，则主动切换到follower。

总之，系统中最多只有一个leader，如果在一段时间里发现没有leader，则大家通过选举-投票选出leader。leader会不停的给follower发心跳消息，表明自己的存活状态。如果leader故障，那么follower会转换成candidate，重新选出leader。



**nacos数据同步：**

- ### 复制过程

  1.客户端的请求包含了被复制状态机执行的指令，并转发给leader；

  2.leader把指令作为新的日志，并发送给其他server，让他们复制；

  3.假如日志被安全的复制（收到超过majority的ack），leader会将日志添加到状态机中，并返回给客户端；

  4.如果follower丢失，leader会不断重试，直到全部follower都最终存储了所有日志条目。

Raft 是分布式一致性算法，保证的实际上是多台机器上数据的一致性，前面说的 leader 选举是为了保证日志复制的一致性。

![image-20200924161257645](D:\直播\06期-Spring Cloud part2 知识拓展.assets\image-20200924161257645.png)



### 4 sentinel dashboard实际生产中环境中能使用么

开源的和内部版本是一样的，最核心的代码和能力都开源出来了。可以生产级应用，但并非 “开箱即用”，需要你做一些二次开发和调整，接下来我会对这些问题仔细展开。当然，我更推荐你直接使用阿里云上的AHASSentinel 控制台和ASM配置中心，那是最佳实践的输出，你可以节省很多时间、人力、运维成本等。

![image-20200924153921659](D:\直播\06期-Spring Cloud part2 知识拓展.assets\image-20200924153921659.png)

开源版不足：

1. 限流、降级等规则保存在应用节点的内存中，应用发布重启后就会失效，这在生产环境中显然是无法接受的

2. metrics信息由Dashboard拉取上来后保存到内存中，仅仅保留5分钟，错过后可能无法还原 “案发现场”，而且无法看到流量趋势；

3. 如果接入限流的应用有500+个，每个应用平均部署4个节点，那么总共2000个节点，那么Dashboard肯定会成为瓶颈，单机的线程池根本处理不过来；



**扩展原则**
1、不影响原有核心功能
2、快速简单



### 5、这么多微服务是搞一个maven父子工程放到一个git仓库，还是每个服务一个仓库，如果都放在一个仓库打包发布时间是不是很长，如果每个都放一个仓库那服务直接的依赖怎么处理

可以考虑把多个服务集成到一个仓库里面，按服务分子文件夹，通过一个统一的Pom文件维护版本号。



### 6、feign接口的一个超时问题。如果上游服务调用下游服务的多个接口，是否要设置超时时间为响应时间最长的接口时间为超时时间？feign接口超时问题。如果下游服务链路很长，链路错综复杂，超时时间如何设置？



### 7、nacos在实际使用中会产生大量日志，占用资源，定时清理还是怎么搞

![image-20200924163135432](D:\直播\06期-Spring Cloud part2 知识拓展.assets\image-20200924163135432.png)

access日志大量打印，这个日志是Spring Boot提供的tomcat访问日志打印，Spring Boot在关于该日志的选项中，修改：application.properties

服务端业务日志，此种日志修改服务端日志配置文件，修改：nacos-logback.xml

控制台心跳日志，Yml配置日志级别即可：

```yaml
# application.yml添加
logging:
  level:
    com:
      alibaba:
        nacos: warn
```

### 8、spring  oauth2怎么实现用户权限角色检验，比如用户只赋予某个微服务接口的读权限，没有写权限，老师能不能这个例子参考下

spring security 



### 9、定时回调任务采用什么机制来实现比较好，使用jdk自带的延迟队列无法满足分布式环境，使用redis的队列需要一直的去扫描队列，竞争锁去获取任务，感觉有点浪费资源  。有没有那种在分布式环境中 能够定时通知的中间件 比如给了一条数据  数据上待着一个剩余存活时间 当存活时间=0的时候     发出一个通知

使用消息队列：RabbitMQ RocketMQ  kafka 

**死信队列：**

 	场景介绍：我们都经常在淘宝上买东西，当我们提交订单后，如果某个时间段之内我们没有支付，淘宝肯定不会帮我们一直保留那个订单，如果超过半个小时我们未支付的话，淘宝会自动帮我们取消订单。

- RabbitMQ 可以针对 Queue 和 Message 设置 x-message-tt，来控制消息的生存时间，如果超时，则消息变为dead letter；
- RabbitMQ 的 Queue 可以配置 x-dead-letter-exchange 和 x-dead-letter-routing-key（可选）两个参数，用来控制队列内出现了 dead letter，则按照这两个参数重新路由。

![image-20200924173135767](D:\直播\06期-Spring Cloud part2 知识拓展.assets\image-20200924173135767.png)



![image-20200924211207322](D:\直播\06期-Spring Cloud part2 知识拓展.assets\image-20200924211207322.png)

来自一个队列的消息可以被当做‘死信’，即被重新发布到另外一个“exchange”去，这样的情况有：

- 消息被拒绝 不重新入队参数或达到的retry重新入队的上限次数
- 消息的TTL(Time To Live)-存活时间已经过期
- 队列长度限制被超越（队列满，queue的"**x-max-length**"参数）



### 10 分布式下如何实现接口幂等性



**接口幂等性就是用户对于同一操作发起的一次请求或者多次请求的结果是一致的，不会因为多次点击而产生了副作用。**



举个最简单的例子，那就是支付，用户购买商品后支付，支付扣款成功，但是返回结果的时候网络异常，此时钱已经扣了，用户再次点击按钮，此时会进行第二次扣款，返回结果成功，用户查询余额返发现多扣钱了，流水记录也变成了两条．．．,这就没有保证接口的幂等性



假如你有个服务提供一个接口，结果这服务部署在了 5 台机器上，接着有个接口就是**付款接口**。然后人家用户在前端上操作的时候，不知道为啥，总之就是一个订单**不小心发起了两次支付请求**，然后这俩请求分散在了这个服务部署的不同的机器上，好了，结果一个订单扣款扣两次。

或者是订单系统调用支付系统进行支付，结果不小心因为**网络超时**了，然后订单系统走了前面我们看到的那个重试机制，咔嚓给你重试了一把，好，支付系统收到一个支付请求两次，而且因为负载均衡算法落在了不同的机器上，尴尬了。。。



**这个不是技术问题，这个没有通用的一个方法，这个应该结合业务来保证幂等性。**



其实保证幂等性主要是三点： 

- 对于每个请求必须有一个唯一的标识，举个栗子：订单支付请求，肯定得包含订单 id，一个订单 id 最多支付一次，对吧。 
- 每次处理完请求之后，必须有一个记录标识这个请求处理过了。常见的方案是在 mysql 中记录个状态，比如支付之前记录一条这个订单的支付流水。
- 每次接收请求需要进行判断，判断之前是否处理过。比如说，如果有一个订单已经支付了，就已经有了一条支付流水，那么如果重复发送这个请求，则此时先插入支付流水，orderId 已经存在了，唯一键约束生效，报错插入不进去的。然后你就不用再扣款了。

实际运作过程中，你要结合自己的业务来，比如说利用 redis，用 orderId 作为唯一键。只有成功插入这个支付流水，才可以执行实际的支付扣款。

要求是支付一个订单，必须插入一条支付流水，order_id 建一个唯一键 `unique key`。你在支付一个订单之前，先插入一条支付流水，order_id 就已经进去了。你就可以写一个标识到 redis 里面去，`set order_id payed`，下一次重复请求过来了，先查 redis 的 order_id 对应的 value，如果是 `payed` 就说明已经支付过了，你就别重复支付了。



### "在分布式环境下 服务拆分粒度如果偏小，将会产生大量的请求调用，并且数据都分布在不同的服务下的数据库中  如果这个时候需要做分页查询的数据分布在多个服务下，如果直接查询mysql没办法实现，目前的想法是只能将需要分页查询的数据都统一存在一个地方，比如elasticsearch或则mongodb中，然后在去这些统一的地方查询。这种方法有点麻烦 有没有什么好的解决方案"



### 11、使用 Sentinel 进行流量保护，但是默认的 web servlet filter 是拦截全部 http 请求。在传统的项目中问题不大。但是如果项目中用了 Spring MVC，并且用了@PathVariable 就尴尬了。比如 uri pattern 是 `/foo/{id}` ,而从 Sentinel 监控看 `/foo/1` 和 `/foo/2` 就是两个资源了，并且 Sentinel 最大支持 6000 个资源，再多就不生效了。

**解决办法：**
官方给的方案是:     UrlCleaner       通配符：  /autodeliver/checkstate/*

```java
@SpringBootApplication
@EnableDiscoveryClient  // 开启服务发现
@EnableFeignClients   // 开启Feign
public class PageApplication9101 {

    public static void main(String[] args) {
        SpringApplication.run(PageApplication9101.class,args);
		
        
        WebCallbackManager.setUrlCleaner(new UrlCleaner() {
            @Override
            public String clean(String originUrl) {
                if(!StringUtils.isEmpty(originUrl) && originUrl.startsWith("/autodeliver/checkState/"))
                    return "/autodeliver/checkState/*";
                return null;
            }
        });
    }

}

```



# Part 4 互动环节











