#  Redis

##  第一部分 缓存原理&设计

###  一、缓存基本思想

####  1.1 缓存的使用场景

- **DB缓存，减轻DB服务器压力**
- **提高系统响应**
- **做Session分离**
- **做分布式锁（Redis）**
- **做乐观锁（Redis）**

####  1.2 大型网站中缓存的使用

![](/Users/lixin/Documents/知识库/图片\redis\Snipaste_2021-12-06_15-35-45.png)

####  1.3 常见缓存分类

- **客户端缓存**

  - 传统互联网：页面缓存和浏览器缓存 

    - 页面缓存

      页面自身对某些元素或全部元素进行存储，并保存成文件。

      html5：Cookie、WebStorage（SessionStorage和LocalStorage）、WebSql、indexDB、 Application Cache等

    - 浏览器缓存

      当客户端向服务器请求资源时，会先抵达浏览器缓存，如果浏览器有“要请求资源”的副本，就可以直接从浏览器缓存中提取而不是从原始服务器中提取这个资源。

  - 移动互联网：APP缓存

    - APP缓存

      原生APP中把数据缓存在内存、文件或本地数据库（SQLite）中。比如图片文件。

- **网络端缓存**

  通过代理的方式响应客户端请求，对重复的请求返回缓存中的数据资源。

  - Web代理缓存

    可以缓存原生服务器的静态资源，比如样式、图片等，如nginx。

  - 边缘缓存

    边缘缓存中典型的商业化服务就是CDN了。 CDN的全称是Content Delivery Network，即内容分发网络。

    CDN通过部署在各地的边缘服务器，使用户就近获取所需内容，降低网络拥塞，提高用户访问响应速度 和命中率。

    CDN的关键技术主要有内容存储和分发技术。现在一般的公有云服务商都提供CDN服务。

- **服务端缓存**

  服务器端缓存是整个缓存体系的核心。包括数据库级缓存、平台级缓存和应用级缓存。

  - 数据库级缓存

    数据库是用来存储和管理数据的。 

    MySQL在Server层使用查询缓存机制。将查询后的数据缓存起来。 

    K-V结构，Key：select语句的hash值，Value：查询结果 。

    InnoDB存储引擎中的buffer-pool用于缓存InnoDB索引及数据块。

  - 平台级缓存

    平台级缓存指的是带有缓存特性的应用框架。 

    比如：GuavaCache 、EhCache（二级缓存，硬盘）、OSCache（页面缓存）等。 部署在应用服务器上，也称为服务器本地缓存。

  - 应用级缓存（重点）

    具有缓存功能的中间件：Redis、Memcached、EVCache（AWS）、Tair（阿里 、美团）等。 

    采用K-V形式存储。 利用集群支持高可用、高性能、高并发、高扩展。 

    分布式缓存

###  二、缓存的优势与代价

#### 2.1 使用缓存的优势

- 提升用户体验
- 减轻服务器压力
- 提升系统性能

####  2.2 使用缓存的代价

- 额外的硬件支出
- 高并发缓存失效（缓存穿透、缓存雪崩、缓存击穿）
- 缓存与数据库数据同步
- 缓存并发竞争

###  三、缓存的读写模式

#### 3.1 Cache Aside Pattern 旁路缓存（常用）

最经典的缓存+数据库读写模式。

读的时候，先读缓存，缓存没有的话，就读数据库，然后取出数据后放入缓存，同时返回响应。

![](/Users/lixin/Documents/知识库/图片\redis\Snipaste_2021-12-06_15-48-39.png)

更新的时候，先更新数据库，然后再删除缓存。

![](/Users/lixin/Documents/知识库/图片\redis\Snipaste_2021-12-06_15-49-10.png)



- 为什么是删除缓存而不是更新缓存

  - 缓存的值是一个结构，更新需要遍历后修改，比较耗时
  - 缓存采用懒加载，使用的时候才更新缓存

- 高并发**脏读**的三种情况

  - 先更新数据库，再更新缓存

    update与commit之间，更新缓存，commit失败，则DB与缓存数据不一致

  - 先删除缓存，再更新数据库

    update与commit之间，有新的读，缓存空，读DB数据到缓存 数据是旧的数据 commit后，DB为新数据，则DB与缓存数据不一致

  - 先更新数据库，再删除缓存（推荐）

    update与commit之间，有新的读，缓存空，读DB数据到缓存数据是旧的数据 commit后，DB为新数据 则DB与缓存数据不一致。

    解决方法：采用延时双删策略

####  3.2 Read/Write Through Pattern

​	应用程序只操作缓存，缓存操作数据库。 

​	Read-Through（穿透读模式/直读模式）：应用程序读缓存，缓存没有，由缓存回源到数据库，并写入缓存。

（guavacache） Write-Through（穿透写模式/直写模式）：应用程序写缓存，缓存写数据库。 该种模式需要提供数据库的handler，开发较为复杂。

####  3.3 Write Behind Caching Pattern

​		应用程序只更新缓存。 缓存通过异步的方式将数据批量或合并后更新到DB中不能时时同步，甚至会丢数据

###  四、缓存架构的设计思路

#### 4.1 多层次

![](/Users/lixin/Documents/知识库/图片\redis\Snipaste_2021-12-06_16-00-08.png)

​	分布式缓存宕机，本地缓存还可以使用

####  4.2 数据类型

- 简单数据类型

  ​	Value是字符串或整数或二进制 Value的值比较大（大于100K） 只进行setter和getter 可采用Memcached Memcached纯内存缓存，多线程 K-V

- 复杂数据类型

  ​	Value是hash、set、list、zset，需要存储关系，聚合，计算，可采用Redis

#### 4.3 集群

​	分布式缓存集群方案（Redis） ：

- codis 
- 哨兵+主从 
- RedisCluster

####  4.4 缓存的数据结构设计

- 与数据库表一致

  ​	数据库表和缓存是一一对应的，缓存的字段会比数据库表少一些，缓存的数据是经常访问的用户表，商品表

- 与数据库表不一致

  ​	需要存储关系，聚合，计算等，比如某个用户的帖子、用户的评论。 以用户评论为例，DB结构如下：

  | ID   | UID  | PostTime   | Content    |
  | ---- | ---- | ---------- | ---------- |
  | 1    | 1000 | 1547342000 | xxxxxxxxxx |
  | 2    | 1000 | 1547342000 | xxxxxxxxxx |
  | 3    | 1002 | 1547342000 | xxxxxxxxxx |

  ​	如果要取出UID为1000的用户的评论，原始的表的数据结构显然是不行的。 我们应做如下设计： 

  key：UID+时间戳(精确到天) 评论一般以天为计算单位 

  value：Redis的Hash类型。field为id和content 

  expire：设置为一天

##  第二部分 Redis的数据类型与底层数据结构

###  一、Redis数据类型和应用场景

####  1.1 Redis简介

​	Redis是一个Key-Value的存储系统，使用ANSI C语言编写。 

- key的类型是字符串。 

- value的数据类型有： 
  - 常用的：string字符串类型、list列表类型、set集合类型、sortedset（zset）有序集合类型、hash类型。 
  - 不常见的：bitmap位图类型、geo地理位置类型。

   Redis5.0新增一种：stream类型 

**注意**：Redis中命令是忽略大小写，（set SET），key是不忽略大小写的 （NAME name）。

####  1.2 Redis的Key的设计

- 用":"分割。

- 把表名转换为key前缀, 比如: user: 

- 第二段放置主键值 

- 第三段放置列名

  比如：用户表user, 转换为redis的key-value存储

| userId | username | password | email         |
| ------ | -------- | -------- | ------------- |
| 9      | lx       | lx59     | lixin@163.com |

​	username 的key：user:9:username

​	email的key：user:9:email

​	表示明确，不易被覆盖

####  1.3 String字符串类型

​	Redis的String能表达3种值的类型：字符串、整数、浮点数，100.01是个六位的串

- 常见命令：

| 命令名称 |                      | 命令描述                                                     |
| -------- | -------------------- | ------------------------------------------------------------ |
| set      | set key value        | 赋值                                                         |
| get      | get key              | 取值                                                         |
| getset   | getset key value     | 取值并赋值                                                   |
| setnx    | setnx key value      | 当value不存在时采用赋值 set key value NX PX 3000 原子操作，px 设置毫秒数 |
| append   | append key value     | 向尾部追加值                                                 |
| strlen   | strlen key           | 获取字符串长度                                               |
| incr     | incr key             | 递增数字                                                     |
| incrby   | incrby key increment | 增加指定的整数                                               |
| decr     | decr key             | 递减数字                                                     |
| decrby   | decrby key decrement | 减少指定的整数                                               |

- 应用场景
  - key和命令是字符串 
  - 普通的赋值 
  - incr用于乐观锁，递增数字，可用于实现乐观锁watch(事务) 
  - setnx用于分布式锁，当value不存在时采用赋值，可用于实现分布式锁

####  1.4 List列表类型

​	list列表类型可以存储有序、可重复的元素，获取头部或尾部附近的记录是极快的，list的元素个数最多为2^32-1个（40亿）

- 常见命令：

  | 命令名称   | 命令格式                             | 描述                                                         |
  | ---------- | ------------------------------------ | ------------------------------------------------------------ |
  | lpush      | lpush key v1 v2 v3 ...               | 从左侧插入列表                                               |
  | lpop       | lpop key                             | 从列表左侧取出                                               |
  | rpush      | rpush key v1 v2 v3 ...               | 从右侧插入列表                                               |
  | rpop       | rpop key                             | 从列表右侧取出                                               |
  | lpushx     | lpushx key value                     | 将值插入到列表头部                                           |
  | rpushx     | rpushx key value                     | 将值插入到列表尾部                                           |
  | blpop      | blpop key timeout                    | 从列表左侧取出，当列表为空时阻塞，可以设置最大阻塞时间，单位为秒 |
  | brpop      | brpop key timeout                    | 从列表右侧取出，当列表为空时阻塞，可以设置最大阻塞时间，单位为秒 |
  | llen       | llen key                             | 获得列表中元素个数                                           |
  | lindex     | lindex key index                     | 获得列表中下标为index的元素 index从0开始                     |
  | lrange     | lrange key start end                 | 返回列表中指定区间的元素，区间通过start和end指定             |
  | lrem       | lrem key count value                 | 删除列表中与value相等的元素当count>0时，lrem会从列表左边开始删除;当count<0时，lrem会从列表后边开始删除;当count=0时，lrem删除所有值为value的元素 |
  | lset       | lset key index value                 | 将列表index位置的元素设置成value的值                         |
  | ltrim      | ltrim key start end                  | 对列表进行修剪，只保留start到end区间                         |
  | rpoplpush  | rpoplpush key1 key2                  | 从key1列表右侧弹出并插入到key2列表左侧                       |
  | brpoplpush | brpoplpush key1 key2                 | 从key1列表右侧弹出并插入到key2列表左侧，会阻塞               |
  | linsert    | linsert key BEFORE/AFTER pivot value | 将value插入到列表，且位于值pivot之前或之后                   |

- 应用场景

  - 作为栈或队列使用
  - 可用于各种列表，比如用户列表、商品列表、评论列表等。

####  1.5 Set集合类型

​	无序、唯一元素，集合中最大的成员数为 2^32 - 1

- 常用命令

  | 命令名称    | 命令格式                | 描述                                   |
  | ----------- | ----------------------- | -------------------------------------- |
  | sadd        | sadd key mem1 mem2 .... | 为集合添加新成员                       |
  | srem        | srem key mem1 mem2 .... | 删除集合中指定成员                     |
  | smembers    | smembers key            | 获得集合中所有元素                     |
  | spop        | spop key                | 返回集合中一个随机元素，并将该元素删除 |
  | srandmember | srandmember key         | 返回集合中一个随机元素，不会删除该元素 |
  | scard       | scard key               | 获得集合中元素的数量                   |
  | sismember   | sismember key member    | 判断元素是否在集合内                   |
  | sinter      | sinter key1 key2 key3   | 求多集合的交集                         |
  | sdiff       | sdiff key1 key2 key3    | 求多集合的差集                         |
  | sunion      | sunion key1 key2 key3   | 求多集合的并集                         |

- 应用场景

  ​	适用于不能重复的且不需要顺序的数据结构，比如：关注的用户，还可以通过spop进行随机抽奖

####  1.6 Sortedset有序集合类型

​	SortedSet(ZSet) 有序集合： 元素本身是无序不重复的

​	每个元素关联一个分数(score) 可按分数排序，分数可重复

- 常用命令

  | 命令名称  | 命令格式                                   | 描述                                       |
  | --------- | ------------------------------------------ | ------------------------------------------ |
  | zadd      | zadd key score1 member1 score2 member2 ... | 为有序集合添加新成员                       |
  | zrem      | zrem key mem1 mem2 ....                    | 删除有序集合中指定成员                     |
  | zcard     | zcard key                                  | 获得有序集合中的元素数量                   |
  | zcount    | zcount key min max                         | 返回集合中score值在[min,max]区间的元素数量 |
  | zincrby   | zincrby key increment member               | 在集合的member分值上加increment            |
  | zscore    | zscore key member                          | 获得集合中member的分值                     |
  | zrank     | zrank key member                           | 获得集合中member的排名（按分值从 小到大）  |
  | zrevrank  | zrevrank key member                        | 获得集合中member的排名（按分值从 大到小）  |
  | zrange    | zrange key start end                       | 获得集合中指定区间成员，按分数递增 排序    |
  | zrevrange | zrevrange key start end                    | 获得集合中指定区间成员，按分数递减 排序    |

- 应用场景

  ​	由于可以按照分值排序，所以适用于各种排行榜。比如：点击排行榜、销量排行榜、关注排行榜等。

####  1.7 Hash类型（散列表）

​	Redis hash 是一个 string 类型的 field 和 value 的映射表，它提供了字段和字段值的映射。 每个 hash 可以存储 2^32 - 1 键值对（40多亿）。

![](/Users/lixin/Documents/知识库/图片\redis\Snipaste_2021-12-06_16-35-04.png)

- 常用命令

  | 命令名称 | 命令格式                              | 描述                        |
  | -------- | ------------------------------------- | --------------------------- |
  | hset     | hset key field value                  | 赋值，不区别新增或修改      |
  | hmset    | hmset key field1 value1 field2 value2 | 批量赋值                    |
  | hsetnx   | hsetnx key field value                | 赋值，如果filed存在则不操作 |
  | hexists  | hexists key filed                     | 查看某个field是否存在       |
  | hget     | hget key field                        | 获取一个字段值              |
  | hmget    | hmget key field1 field2 ...           | 获取多个字段值              |
  | hgetall  | hgetall key                           | 获取所有字段                |
  | hdel     | hdel key field1 field2...             | 删除指定字段                |
  | hincrby  | hincrby key field increment           | 指定字段自增increment       |
  | hlen     | hlen key                              | 获得字段数量                |

- 应用场景

  ​	对象的存储 ，表数据的映射

####  1.8 Bitmap位图类型

​	bitmap是进行位操作的,通过一个bit位来表示某个元素对应的值或者状态,其中的key就是对应元素本身。 bitmap本身会极大的节省储存空间。

- 常用命令

  | 命令名称 | 命令格式                                  | 描述                                    |
  | -------- | ----------------------------------------- | --------------------------------------- |
  | setbit   | setbit key offset value                   | 设置key在offset处的bit值(只能是0或者 1) |
  | getbit   | getbit key offset                         | 获得key在offset处的bit值                |
  | bitcount | bitcount key                              | 获得key的bit位为1的个数                 |
  | bitpos   | bitpos key value                          | 返回第一个被设置为bit值的索引值         |
  | bitop    | bitop and[or/xor/not] destkey key [key …] | 对多个key 进行逻辑运算后存入destkey 中  |

- 应用场景

  - 用户每月签到，用户id为key ， 日期作为偏移量 1表示签到 
  - 统计活跃用户, 日期为key，用户id为偏移量 1表示活跃 
  - 查询用户在线状态， 日期为key，用户id为偏移量 1表示在线

####  1.9 Geo地理位置类型

​	geo是Redis用来处理位置信息的。在Redis3.2中正式使用。主要是利用了Z阶曲线、Base32编码和 geohash算法

- Z阶曲线

  ​	在x轴和y轴上将十进制数转化为二进制数，采用x轴和y轴对应的二进制数依次交叉后得到一个六位数编 码。把数字从小到大依次连起来的曲线称为Z阶曲线，Z阶曲线是把多维转换成一维的一种方法。

  ![](/Users/lixin/Documents/知识库/图片\redis\Snipaste_2021-12-06_17-14-03.png)

- Base32编码

  ​	Base32这种数据编码机制，主要用来把二进制数据编码成可见的字符串，其编码规则是：任意给定一个二进制数据，以5个位(bit)为一组进行切分(base64以6个位(bit)为一组)，对切分而成的每个组进行编码得到1个可见字符。Base32编码表字符集中的字符总数为32个（0-9、b-z去掉a、i、l、o），这也是 Base32名字的由来。

  ![](/Users/lixin/Documents/知识库/图片\redis\Snipaste_2021-12-06_17-16-22.png)

- geohash算法

  ​	Gustavo在2008年2月上线了geohash.org网站。Geohash是一种地理位置信息编码方法。 经过 geohash映射后，地球上任意位置的经纬度坐标可以表示成一个较短的字符串。可以方便的存储在数据库中，附在邮件上，以及方便的使用在其他服务中。以北京的坐标举例，[39.928167,116.389550]可以 转换成 wx4g0s8q3jf9 。

  ​	Redis中经纬度使用52位的整数进行编码，放进zset中，zset的value元素是key，score是GeoHash的52位整数值。在使用Redis进行Geo查询时，其内部对应的操作其实只是zset(skiplist)的操作。通过zset的score进行排序就可以得到坐标附近的其它元素，通过将score还原成坐标值就可以得到元素的原始坐标。

- 常用命令

  | 命令名称          | 命令格式                                                     | 描述                   |
  | ----------------- | ------------------------------------------------------------ | ---------------------- |
  | geoadd            | geoadd key 经度 纬度 成员名称1 经度1 纬度1 成 员名称2 经度2 纬度 2 ... | 添加地理坐标           |
  | geohash           | geohash key 成员名称1 成员名称2...                           | 返回标准的geohash串    |
  | geopos            | geopos key 成员名称1 成员名称2...                            | 返回成员经纬度         |
  | geodist           | geodist key 成员1 成员2 单位                                 | 计算成员间距离         |
  | georadiusbymember | georadiusbymember key 成员值 单位 count数 asc[desc]          | 根据成员查找附近的成员 |

- 应用场景

  - 记录地理位置 
  - 计算距离 
  - 查找"附近的人"

####  1.10 Stream数据流类型

​	stream是Redis5.0后新增的数据结构，用于可持久化的消息队列。 

​	几乎满足了消息队列具备的全部内容，包括： 

1. 消息ID的序列化生成 
2. 消息遍历 
3. 消息的阻塞和非阻塞读取 
4. 消息的分组消费 
5. 未完成消息的处理 
6. 消息队列监控 

   每个Stream都有唯一的名称，它就是Redis的key，首次使用 xadd 指令追加消息时自动创建。

- 常用命令

  | 命令名称   | 命令格式                                                     | 描述                                                         |
  | ---------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
  | xadd       | xadd key id <*> field1 value1....                            | 将指定消息数据追加到指定队列(key)中，* 表示最新生成的id（当前时间+序列号） |
  | xread      | xread [COUNT count] [BLOCK milliseconds] STREAMS key [key ...] ID [ID ...] | 从消息队列中读取，COUNT：读取条数， BLOCK：阻塞读（默认不阻塞）key：队列 名称 id：消息id |
  | xrange     | xrange key start end [COUNT]                                 | 读取队列中给定ID范围的消息 COUNT：返 回消息条数（消息id从小到大） |
  | xrevrange  | xrevrange key start end [COUNT]                              | 读取队列中给定ID范围的消息 COUNT：返 回消息条数（消息id从大到小） |
  | xdel       | xdel key id                                                  | 删除队列的消息                                               |
  | xgroup     | xgroup create key groupname id                               | 创建一个新的消费组                                           |
  | xgroup     | xgroup destory key groupname                                 | 删除指定消费组                                               |
  | xgroup     | xgroup delconsumer key groupname cname                       | 删除指定消费组中的某个消费者                                 |
  | xgroup     | xgroup setid key id                                          | 修改指定消息的最大id                                         |
  | xreadgroup | xreadgroup group groupname consumer COUNT streams key        | 从队列中的消费组中创建消费者并消费数据 （consumer不存在则创建） |

- 应用场景

  消息队列

###  二、底层数据结构

​	Redis作为Key-Value存储系统，数据结构如下：

![](/Users/lixin/Documents/知识库/图片\redis\Snipaste_2021-12-06_17-31-46.png)

​	Redis没有表的概念，Redis实例所对应的db以编号区分，db本身就是key的命名空间。 比如：user:1000作为key值，表示在user这个命名空间下id为1000的元素，类似于user表的id=1000的行。

####  2.1 RedisDB结构

​	Redis中存在“数据库”的概念，该结构由redis.h中的redisDb定义。 

​	当redis 服务器初始化时，会预先分配 16 个数据库所有数据库保存到结构 redisServer 的一个成员，redisServer.db 数组中 redisClient中存在一个名叫db的指针指向当前使用的数据库 

​	RedisDB结构体源码：

```c
typedef struct redisDb {
    int id; //id是数据库序号，为0-15（默认Redis有16个数据库）
    long avg_ttl; //存储的数据库对象的平均ttl（time to live），用于统计
    dict *dict; //存储数据库所有的key-value
    dict *expires; //存储key的过期时间
    dict *blocking_keys;//blpop 存储阻塞key和客户端对象
    dict *ready_keys;//阻塞后push 响应阻塞客户端 存储阻塞后push的key和客户端对象
    dict *watched_keys;//存储watch监控的的key和客户端对象
} redisDb;
```

####  2.2 RedisObject结构

​	Value是一个对象，包含字符串对象，列表对象，哈希对象，集合对象和有序集合对象。

#####  2.2.1 **结构信息概览**

```c
typedef struct redisObject {
    /**
     * 类型 五种对象类型
     * REDIS_STRING(字符串)、REDIS_LIST (列表)、REDIS_HASH(哈希)、REDIS_SET(集合)、REDIS_ZSET(有序集合)。
     */
    unsigned type:4;
    
    /**
     * 对象的内部编码，占 4 位
     * 每个对象有不同的实现编码Redis 
     * 可以根据不同的使用场景来为对象设置不同的编码，大大提高了 Redis 的灵活性和效率。
     * 通过 object encoding 命令，可以查看对象采用的编码方式
     */
    unsigned encoding:4;
    
    /**
     * ptr 指针指向具体的数据，比如：set hello world，ptr 指向包含字符串 world 的 SDS。
     */
    void *ptr;
    //...
    
    /**
     * refcount 记录的是该对象被引用的次数，类型为整型。
     * refcount 的作用，主要在于对象的引用计数和内存回收。
     * 当对象的refcount>1时，称为共享对象
     * Redis 为了节省内存，当有一些对象重复出现时，新的程序不会创建新的对象，而是仍然使用原来的对象。
     */
    int refcount;//引用计数
    //...
    
    /**
     * lru 记录的是对象最后一次被命令程序访问的时间，（ 4.0 版本占 24 位，2.6 版本占 22 位）。
     * 高16位存储一个分钟数级别的时间戳，低8位存储访问计数（lfu ： 最近访问次数）
     * lru----> 高16位: 最后被访问的时间
     * lfu----->低8位：最近访问次数
     */
    unsigned lru:LRU_BITS; //LRU_BITS为24bit 记录最后一次被命令程序访问的时间
    //...
}robj;

```

##### **2.2.2 存储数据结构**

###### 2.2.2.1 字符串对象

Redis 使用了 SDS(Simple Dynamic String)。用于存储字符串和整型数据。

![](/Users/lixin/Documents/知识库/图片\redis\Snipaste_2021-12-07_09-50-48.png)

```c
struct sdshdr{
   uint8_t len; // 已经使用的字节数
   uint8_t alloc; // 实际可以存储的字节最大长度，不包括SDS头部和结尾的空字符
   unsigned char flags; // flags中的低3个bit决定使用哪种结构存储字符串，高5bit未使用
   char buf[]; // 柔性数组，用来保存实际的字符串
}
```

buf[] 的长度=len+alloc+1

SDS的优势： 

1. SDS 在 C 字符串的基础上加入了 free 和 len 字段，获取字符串长度：SDS 是 O(1)，C 字符串是 O(n)。 buf数组的长度=free+len+1 
2. SDS 由于记录了长度，在可能造成缓冲区溢出时会自动重新分配内存，杜绝了缓冲区溢出。 
3. 可以存取二进制数据，以字符串长度len来作为结束标识 

**使用场景**： SDS的主要应用在存储字符串和整型数据、存储key、AOF缓冲区和用户输入缓冲。

###### 2.2.2.2 跳跃表（重点）

​	跳跃表是有序集合（sorted-set）的底层实现，效率高，实现简单。 

​	跳跃表的基本思想： 将有序链表中的部分节点分层，每一层都是一个有序链表。

- 查找

  在查找时优先从最高层开始向后查找，当到达某个节点时，如果next节点值大于要查找的值或next指针 指向null，则从当前节点下降一层继续向后查找。

  例如：

  ![](/Users/lixin/Documents/知识库/图片\redis\Snipaste_2021-12-07_10-07-19.png)

  查找元素9，按道理我们需要从头结点开始遍历，一共遍历8个结点才能找到元素9。

  第一次分层： 遍历5次找到元素9（红色的线为查找路径）

  ![](/Users/lixin/Documents/知识库/图片\redis\Snipaste_2021-12-07_10-07-51.png)

  第二次分层： 遍历4次找到元素9

  ![](/Users/lixin/Documents/知识库/图片\redis\Snipaste_2021-12-07_10-08-13.png)

  第三层分层: 遍历4次找到元素9

  ![](/Users/lixin/Documents/知识库/图片\redis\Snipaste_2021-12-07_10-08-38.png)

  这种数据结构，就是跳跃表，它具有二分查找的功能。

- 插入

  上面例子中，9个结点，一共4层，是理想的跳跃表。

  通过抛硬币（概率1/2）的方式来决定新插入结点跨越的层数： 

  正面:插入上层 

  背面：不插入 达到1/2概率（计算次数）

- 删除

  找到指定元素并删除每层的该元素即可

- Redis跳跃表的实现

  ```c
  //跳跃表节点
  typedef struct zskiplistNode {
      /* 
       * 存储字符串类型数据 redis3.0版本中使用robj类型表示，
       * 但是在redis4.0.1中直接使用sds类型表示 
       */
      sds ele; 
      double score;//存储排序的分值
      struct zskiplistNode *backward;//后退指针，指向当前节点最底层的前一个节点
      /*
       * 层，柔性数组，随机生成1-64的值
       */
      struct zskiplistLevel {
          struct zskiplistNode *forward; //指向本层下一个节点
          unsigned int span;//本层下个节点到本节点的元素个数
      } level[];
  } zskiplistNode;
  
  //链表
  typedef struct zskiplist{
      //表头节点和表尾节点
      structz skiplistNode *header, *tail;
      //表中节点的数量
      unsigned long length;
      //表中层数最大的节点的层数
      int level;
  }zskiplist;
  
  ```

  完整的跳跃表结构体：

  ![](/Users/lixin/Documents/知识库/图片\redis\Snipaste_2021-12-07_10-16-24.png)

  跳跃表的优势： 

  1. 可以快速查找到需要的节点 
  2. 可以在O(1)的时间复杂度下，快速获得跳跃表的头节点、尾结点、长度和高度。 

  应用场景：有序集合的实现

###### 2.2.2.3 字典（重点、难点）

字典dict又称散列表（hash），是用来存储键值对的一种数据结构。 

Redis整个数据库是用字典来存储的。

(K-V结构)对Redis进行CURD操作其实就是对字典中的数据进行CURD操作。

- 数组

  用来存储数据的容器，采用头指针+偏移量的方式能够以O(1)的时间复杂度定位到数据所在的内存地址。

- Hash函数

  作用是把任意长度的输入通过散列算法转换成固定类型、固定长度的散列值。 hash函数可以把Redis里的key：包括字符串、整数、浮点数统一转换成整数。

  数组下标=hash(key)%数组容量(hash值%数组容量得到的余数)

  Redis-cli hash算法:times 33 

  Redis-Server hash算法: siphash

- Hash冲突

  不同的key经过计算后出现数组下标一致，称为Hash冲突。 采用单链表在相同的下标位置处存储原始key和value（拉链法），当根据key找Value时，找到数组下标，遍历单链表可以找出key相同的value。

- Redis字典的实现

  Redis字典实现包括：字典(dict)、Hash表(dictht)、Hash表节点(dictEntry)。

  ![](/Users/lixin/Documents/知识库/图片\redis\Snipaste_2021-12-07_10-23-51.png)

  1. dict字典

     ```c
     typedef struct dict {
         dictType *type; // 该字典对应的特定操作函数
         void *privdata; // 上述类型函数对应的可选参数
         dictht ht[2]; //两张哈希表，存储键值对数据，ht[0]为原生哈希表，ht[1]为 rehash 哈希表 
         /*
          * rehash标识 
          * 当等于-1时表示没有在rehash，否则表示正在进行rehash操作，存储的值表示hash表 
          * ht[0]的rehash进行到哪个索引值(数组下标)
          */
         long rehashidx; 
         int iterators; // 当前运行的迭代器数量
     } dict;
     
     ```

     type字段，指向dictType结构体，里边包括了对该字典操作的函数指针

     ```java
     typedef struct dictType {
         // 计算哈希值的函数
         unsigned int (*hashFunction)(const void *key);
         // 复制键的函数
         void *(*keyDup)(void *privdata, const void *key);
         // 复制值的函数
         void *(*valDup)(void *privdata, const void *obj);
         // 比较键的函数
         int (*keyCompare)(void *privdata, const void *key1, const void *key2);
         // 销毁键的函数
         void (*keyDestructor)(void *privdata, void *key);
         // 销毁值的函数
         void (*valDestructor)(void *privdata, void *obj);
     } dictType;
     ```

  2. Hash表

     ```c
     /**
      * 1、hash表的数组初始容量为4，随着k-v存储量的增加需要对hash表数组进行扩容，新扩容量为当前量的一倍，4,8,16,32
      * 2、索引值=Hash值&掩码值（Hash值与Hash表容量取余）
      */
     typedef struct dictht {
         dictEntry **table; // 哈希表数组
         unsigned long size; // 哈希表数组的大小
         unsigned long sizemask; // 用于映射位置的掩码，值永远等于(size-1)
         unsigned long used; // 哈希表已有节点的数量,包含next单链表数据
     } dictht;
     ```

  3. Hash表节点

     ```c
     typedef struct dictEntry {
         void *key; // 键
         union { // 值v的类型可以是以下4种类型
             void *val;
             uint64_t u64;
             int64_t s64;
             double d;
         } v;
         struct dictEntry *next; // 指向下一个哈希表节点，形成单向链表 解决hash冲突
     } dictEntry;
     ```

     

  ![](/Users/lixin/Documents/知识库/图片\redis\Snipaste_2021-12-07_10-28-05.png)

  Redis字典除了主数据库的K-V数据存储以外，还可以用于：散列表对象、哨兵模式中的主从节点管理等 在不同的应用中，字典的形态都可能不同，dictType是为了实现各种形态的字典而抽象出来的操作函数 （多态）。

  完整的Redis字典数据结构：

  ![](/Users/lixin/Documents/知识库/图片\redis\Snipaste_2021-12-07_10-32-35.png)

- 字典扩容

  字典达到存储上限，需要rehash（扩容）

  扩容流程

  ![](/Users/lixin/Documents/知识库/图片\redis\Snipaste_2021-12-07_10-36-29.png)

  说明： 

  1. 初次申请默认容量为4个dictEntry，非初次申请为当前hash表容量的一倍。 
  2. rehashidx=0表示要进行rehash操作。 
  3. 新增加的数据在新的hash表h[1] 
  4. 修改、删除、查询在老hash表h[0]、新hash表h[1]中（rehash中） 
  5. 将老的hash表h[0]的数据重新计算索引值后全部迁移到新的hash表h[1]中，这个过程称为 rehash。

- 渐进式rehash

  当数据量巨大时rehash的过程是非常缓慢的，所以需要进行优化。 服务器忙，则只对一个节点进行rehash 服务器闲，可批量rehash(100节点) 

应用场景： 

1、主数据库的K-V数据存储 

2、散列表对象（hash） 

3、哨兵模式中的主从节点管理

###### 2.2.2.4 压缩列表

压缩列表（ziplist）是由一系列特殊编码的连续内存块组成的顺序型数据结构，是一个字节数组，可以包含多个节点（entry）。每个节点可以保存一个字节数组或一个整数。 压缩列表的数据结构如下：

![](/Users/lixin/Documents/知识库/图片\redis\Snipaste_2021-12-07_10-48-37.png)

zlbytes：压缩列表的字节长度 

zltail：压缩列表尾元素相对于压缩列表起始地址的偏移量 

zllen：压缩列表的元素个数 entry1..entryX : 压缩列表的各个节点 

zlend：压缩列表的结尾，占一个字节，恒为0xFF（255） 

entryX元素的编码结构：

![](/Users/lixin/Documents/知识库/图片\redis\Snipaste_2021-12-07_10-49-24.png)

```c
typedef struct zlentry {
    unsigned int prevrawlensize; //previous_entry_length字段的长度
    unsigned int prevrawlen; //previous_entry_length字段存储的内容
    unsigned int lensize; //encoding字段的长度
    unsigned int len; //数据内容长度
    unsigned int headersize; //当前元素的首部长度，即previous_entry_length字段长度与encoding字段长度之和。
    unsigned char encoding; //数据类型
    unsigned char *p; //当前元素首地址
} zlentry;
```

**应用场景：** 

sorted-set和hash元素个数少且是小整数或短字符串（直接使用） 

list用快速链表(quicklist)数据结构存储，而快速链表是双向列表与压缩列表的组合。（间接使用）

###### 2.2.2.5 整数集合

整数集合(intset)是一个有序的（整数升序）、存储整数的连续存储结构。

当Redis集合类型的元素都是整数并且都处在64位有符号整数范围内（2^64），使用该结构体存储。

```shell
127.0.0.1:6379> sadd set:001 1 3 5 6 2
(integer) 5
127.0.0.1:6379> object encoding set:001
"intset"
127.0.0.1:6379> sadd set:004 1 100000000000000000000000000 9999999999
(integer) 3
127.0.0.1:6379> object encoding set:004
"hashtable"
```

intset的结构图如下：

![](/Users/lixin/Documents/知识库/图片\redis\Snipaste_2021-12-07_10-58-30.png)

```c
typedef struct intset{
    //编码方式
    uint32_t encoding;
    //集合包含的元素数量
    uint32_t length;
    //保存元素的数组
    int8_t contents[];
}intset;
```

应用场景： 可以保存类型为int16_t、int32_t 或者int64_t 的整数值，并且保证集合中不会出现重复元素。

###### 2.2.2.6 快速列表（重要）

快速列表（quicklist）是Redis底层重要的数据结构。是列表的底层实现。（在Redis3.2之前，Redis采 用双向链表（adlist）和压缩列表（ziplist）实现。）在Redis3.2以后结合adlist和ziplist的优势Redis设 计出了quicklist。

```shell
127.0.0.1:6379> lpush list:001 1 2 5 4 3
(integer) 5
127.0.0.1:6379> object encoding list:001
"quicklist"
```

- **双向列表（adlist）**

  ![](/Users/lixin/Documents/知识库/图片\redis\Snipaste_2021-12-07_11-00-55.png)

  双向链表优势： 

  1. 双向：链表具有前置节点和后置节点的引用，获取这两个节点时间复杂度都为O(1)。 

  2. 普通链表（单链表）：节点类保留下一节点的引用。链表类只保留头节点的引用，只能从头节点插 入删除

  3. 无环：表头节点的 prev 指针和表尾节点的 next 指针都指向 NULL,对链表的访问都是以 NULL 结 束。 

     环状：头的前一个节点指向尾节点 

  4. 带链表长度计数器：通过 len 属性获取链表长度的时间复杂度为 O(1)。 
  5. 多态：链表节点使用 void* 指针来保存节点值，可以保存各种不同类型的值。

- **快速列表**

  quicklist是一个双向链表，链表中的每个节点是一个ziplist结构。quicklist中的每个节点ziplist都能够存储多个数据元素。

  <img src="/Users/lixin/Documents/知识库/图片\redis\Snipaste_2021-12-07_11-03-15.png" style="zoom:150%;" />

  quicklist的结构定义如下：

  ```c
  typedef struct quicklist {
      quicklistNode *head; // 指向quicklist的头部
      quicklistNode *tail; // 指向quicklist的尾部
      unsigned long count; // 列表中所有数据项的个数总和
      unsigned int len; // quicklist节点的个数，即ziplist的个数
      int fill : 16; // ziplist大小限定，由list-max-ziplist-size给定(Redis设定)
      unsigned int compress : 16; // 节点压缩深度设置，由list-compress-depth给定(Redis设定)
  } quicklist;
  ```

  quicklistNode的结构定义如下：

  ```c
  typedef struct quicklistNode {
      struct quicklistNode *prev; // 指向上一个ziplist节点
      struct quicklistNode *next; // 指向下一个ziplist节点
      unsigned char *zl; // 数据指针，如果没有被压缩，就指向ziplist结构，反之指向 quicklistLZF结构
      unsigned int sz; // 表示指向ziplist结构的总长度(内存占用长度)
      unsigned int count : 16; // 表示ziplist中的数据项个数
      unsigned int encoding : 2; // 编码方式，1--ziplist，2--quicklistLZF
      unsigned int container : 2; // 预留字段，存放数据的方式，1--NONE，2--ziplist
      unsigned int recompress : 1; // 解压标记，当查看一个被压缩的数据时，需要暂时解压，标记此参数为 1，之后再重新进行压缩
      unsigned int attempted_compress : 1; // 测试相关
      unsigned int extra : 10; // 扩展字段，暂时没用
  } quicklistNode;
  ```

  - 数据压缩

    ​	quicklist每个节点的实际数据存储结构为ziplist，这种结构的优势在于节省存储空间。为了进一步降低 ziplist的存储空间，还可以对ziplist进行压缩。Redis采用的压缩算法是LZF。其基本思想是：数据与前面重复的记录重复位置及长度，不重复的记录原始数据。 压缩过后的数据可以分成多个片段，每个片段有两个部分：解释字段和数据字段。quicklistLZF的结构 体如下：c

    ```c
    typedef struct quicklistLZF {
        unsigned int sz; // LZF压缩后占用的字节数
        char compressed[]; // 柔性数组，指向数据部分
    } quicklistLZF;
    ```

  - 应用场景 

    列表(List)的底层实现、发布与订阅、慢查询、监视器等功能。

###### 2.2.2.7 流对象

stream主要由：消息、生产者、消费者和消费组构成。

![](/Users/lixin/Documents/知识库/图片\redis\Snipaste_2021-12-07_11-13-33.png)

Redis Stream的底层主要使用了listpack（紧凑列表）和Rax树（基数树）。

- listpack

  listpack表示一个字符串列表的序列化，listpack可用于存储字符串或整数。用于存储stream的消息内 容。 结构如下图：

  ![](/Users/lixin/Documents/知识库/图片\redis\Snipaste_2021-12-07_11-17-59.png)

- Rax树

  Rax 是一个有序字典树 (基数树 Radix Tree)，按照 key 的字典序排列，支持快速地定位、插入和删除操作。

  ![](/Users/lixin/Documents/知识库/图片\redis\Snipaste_2021-12-07_11-18-42.png)

  Rax 被用在 Redis Stream 结构里面用于存储消息队列，在 Stream 里面消息 ID 的前缀是时间戳 + 序号，这样的消息可以理解为时间序列消息。使用 Rax 结构 进行存储就可以快速地根据消息 ID 定位到具体的消息，然后继续遍历指定消息之后的所有消息。

  ![](/Users/lixin/Documents/知识库/图片\redis\Snipaste_2021-12-07_11-19-10.png)

  应用场景： stream的底层实现

#####  2.2.3 对象内部编码

encoding 表示对象的内部编码，占 4 位。 Redis通过 encoding 属性为对象设置不同的编码，对于少的和小的数据，Redis采用小的和压缩的存储方式，体现Redis的灵活性，大大提高了 Redis 的存储量和执行效率。

###### 2.2.3.1 String编码

- REDIS_ENCODING_INT：int类型的整数，RedisObject中的指针就直接赋值为整数数据了，这样就不用额外的指针再指向整数了，节省了指针的空间开销。
- REDIS_ENCODING_EMBSTR：小字符串长度小于44个字节时，RedisObject中的元数据、指针和 SDS 是一块连续的内存区域，这样就可以避免内存碎片。
- REDIS_ENCODING_RAW：大字符串长度大于44个字节，SDS 的数据量就开始变多了，Redis 就不再把 SDS 和 RedisObject 布局在一起了，而是会给 SDS 分配独立的空间，并用指针指向 SDS 结构。

###### 2.2.3.2 List编码

- REDIS_ENCODING_QUICKLIST（quicklist快速列表）

###### 2.2.3.3 Hash编码

- REDIS_ENCODING_HT（dict字典）：当散列表元素的个数比较多或元素不是小整数或短字符串时。
- REDIS_ENCODING_ZIPLIST（ziplist压缩列表）：当散列表元素的个数比较少，且元素都是小整数或短字符串时。

###### 2.2.3.4 Set编码

- REDIS_ENCODING_INTSET（intset整数集合）：当Redis集合类型的元素都是整数并且都处在64位有符号整数范围内
- REDIS_ENCODING_HT（dict字典）：当Redis集合类型的元素都是非整数或都处在64位有符号整数范围外

###### 2.2.3.5 zSet编码

- REDIS_ENCODING_ZIPLIST（ziplist压缩列表）：当元素的个数比较少，且元素都是小整数或短字符串时。
- REDIS_ENCODING_SKIPLIST（跳跃表+字典）：当元素的个数比较多或元素不是小整数或短字符串时。

###  三、缓存过期和淘汰策略

​	Redis性能很高，官方数据Redis支持110000次/s的读和81000次/s的写，不过长期使用，key会不断增加，Redis作为缓存使用，物理内存也会满，这会导致内存与硬盘交换虚拟内存，频繁IO导致性能急剧下降。

####  3.1 maxmemory最大内存参数

- 不设置的场景
  - Redis的key是固定的，不会增加 
  - Redis作为DB使用，保证数据的完整性，不能淘汰 ，可以做集群，横向扩展 
  - 缓存淘汰策略 maxmemory-policy：禁止驱逐 （默认）
- 设置的场景
  - Redis作为缓存，不断增加key
  - maxmemory默认为0，不限制，与业务相关，一般设置物理内存的3/4
  - 需要配合 maxmemory-policy参数设置

####  3.2 expire数据结构

​		在Redis中可以使用expire命令设置一个键的存活时间(ttl: time to live)，过了这段时间，该键就会自动 被删除。

- expire的使用：expire key ttl(单位秒)

  ```shell
  127.0.0.1:6379> expire name 2 #2秒失效
  (integer) 1
  127.0.0.1:6379> get name
  (nil)
  127.0.0.1:6379> set name zhangfei
  OK
  127.0.0.1:6379> ttl name #永久有效
  (integer) -1
  127.0.0.1:6379> expire name 30 #30秒失效
  (integer) 1
  127.0.0.1:6379> ttl name #还有24秒失效
  (integer) 24
  127.0.0.1:6379> ttl name #失效
  (integer) -2
  ```

- expire原理

  ```c
  typedef struct redisDb {
  dict *dict; -- key Value
  dict *expires; -- key ttl
  dict *blocking_keys;
  dict *ready_keys;
  dict *watched_keys;
  int id;
  } redisDb;
  ```

  ​	上面的代码是Redis 中关于数据库的结构体定义，这个结构体定义中除了 id 以外都是指向字典的指针， 其中我们只看 dict 和 expires。 

  ​	dict 用来维护一个 Redis 数据库中包含的所有 Key-Value 键值对，expires则用于维护一个 Redis 数据 库中设置了失效时间的键(即key与失效时间的映射)。 

  ​	当我们使用 expire命令设置一个key的失效时间时，Redis 首先到 dict 这个字典表中查找要设置的key 是否存在，如果存在就将这个key和失效时间添加到 expires 这个字典表。 

  ​	当我们使用 setex命令向系统插入数据时，Redis 首先将 Key 和 Value 添加到 dict 这个字典表中，然后 将 Key 和失效时间添加到 expires 这个字典表中。 

  ​	简单地总结来说就是，设置了失效时间的key和具体的失效时间全部都维护在 expires 这个字典表中。

- 删除策略

  Redis的数据删除有定时删除、惰性删除和主动删除三种方式。 Redis目前采用惰性删除+主动删除的方式。

  - 定时删除

    在设置键的过期时间的同时，创建一个定时器，让定时器在键的过期时间来临时，立即执行对键的删除操作。 需要创建定时器，而且消耗CPU，一般不推荐使用。

  - 惰性删除

    在key被访问时如果发现它已经失效，那么就删除它。 调用expireIfNeeded函数，该函数的意义是：读取数据之前先检查一下它有没有失效，如果失效了就删 除它。

    ```c
    int expireIfNeeded(redisDb *db, robj *key) {
        //获取主键的失效时间 get当前时间-创建时间>ttl
        long when = getExpire(db,key);
        //假如失效时间为负数，说明该主键未设置失效时间（失效时间默认为-1），直接返回0
        if (when < 0) return 0;
        //假如Redis服务器正在从RDB文件中加载数据，暂时不进行失效主键的删除，直接返回0
        if (server.loading) return 0;
        ...
        //如果以上条件都不满足，就将主键的失效时间与当前时间进行对比，如果发现指定的主键
        //还未失效就直接返回0
        if (mstime() <= when) return 0;
        //如果发现主键确实已经失效了，那么首先更新关于失效主键的统计个数，然后将该主键失
        //效的信息进行广播，最后将该主键从数据库中删除
        server.stat_expiredkeys++;
        propagateExpire(db,key);
        return dbDelete(db,key);
    }
    ```

  - 主动删除

    在redis.conf文件中可以配置主动删除策略,默认是no-enviction（不删除）

    - LRU最近最少使用

      ​	算法根据数据的历史访问记录来进行淘汰数据，其核心思想 是“如果数据最近被访问过，那么将来被访问的几率也更高”。

       最常见的实现是使用一个链表保存缓存数据，详细算法实现如下： 

      1. 新数据插入到链表头部； 
      2. 每当缓存命中（即缓存数据被访问），则将数据移到链表头部； 
      3. 当链表满的时候，将链表尾部的数据丢弃。 
      4. 在Java中可以使用LinkHashMap（哈希链表）去实现LRU

      **Redis的LRU数据淘汰机制：**

      ​	在服务器配置中保存了 lru 计数器 server.lrulock，会定时（redis 定时程序 serverCorn()）更新， server.lrulock 的值是根据 server.unixtime 计算出来的。 另外，从 struct redisObject 中可以发现，每一个 redis对象都会设置相应的lru。可以想象的是，每一次访问数据的时候，会更新redisObject.lru。 

      ​	LRU数据淘汰机制是这样的：在数据集中随机挑选几个键值对，取出其中 lru最大的键值对淘汰。用当前时间-最近访问时间，越大说明访问间隔时间越长。

      1. volatile-lru 从已设置过期时间的数据集（server.db[i].expires）中挑选最近最少使用的数据淘汰 
      2. allkeys-lru 从数据集（server.db[i].dict）中挑选最近最少使用的数据淘汰

    - LFU最不经常使用

      ​	如果一个数据在最近一段时间内使用次数很少，那么在将 来一段时间内被使用的可能性也很小。

      1. volatile-lfu 
      2. allkeys-lfu

    - Random随机

      1. volatile-random 从已设置过期时间的数据集（server.db[i].expires）中任意选择数据淘汰 
      2. allkeys-random 从数据集（server.db[i].dict）中任意选择数据淘汰

    - TTL过期淘汰

      - volatile-ttl：从已设置过期时间的数据集（server.db[i].expires）中挑选将要过期的数据淘汰

        redis 数据集数据结构中保存了键值对过期时间的表，即 redisDb.expires。 

        TTL 数据淘汰机制：从过期时间的表中随机挑选几个键值对，取出其中 ttl 最小的键值对淘汰。

    - noenviction（默认）

      ​	禁止驱逐数据，不删除

    **缓存淘汰策略的选择**

    1. allkeys-lru ： 在不确定时一般采用策略。 冷热数据交换 
    2. volatile-lru ： 比allkeys-lru性能差，需要存 : 过期时间 
    3. allkeys-random ： 希望请求符合平均分布(每个元素以相同的概率被访问) 
    4. 自己控制：volatile-ttl 缓存穿透

##  第三部分 Redis通讯协议及事件处理机制

### 一、通信协议 

​	Redis是单线程的。

​	应用系统和Redis通过Redis协议（RESP）进行交互。

####  1.1 请求相应模式

​	Redis协议位于TCP层之上，即客户端和Redis实例保持双工的连接。

- **串行的请求相应模式（ping-pong）**

​	串行化是最简单模式，客户端与服务器端建立长连接，连接通过心跳机制检测（ping-pong）ack应答。 

​	客户端发送请求，服务端响应，客户端收到响应后，再发起第二个请求，服务器端再响应，telnet和redis-cli发出的命令都属于该种模式。

**特点**：有问有答、耗时在网络传输命令、性能较低

![](/Users/lixin/Documents/知识库/图片\redis\Snipaste_2021-12-10_16-06-38.png)

- **双工的请求响应模式(pipeline)**

  批量请求，批量响应，请求响应交叉进行，不会混淆(TCP双工)

  pipeline的作用是将一批命令进行打包，然后发送给服务器，服务器执行完按顺序打包返回。

  通过pipeline，一次pipeline（n条命令）=一次网络时间 + n次命令时间

  通过jedis可以很方便的使用pipeline

  ```java
  Jedis redis = new Jedis("192.168.1.111", 6379);
  redis.auth("12345678");//授权密码 对应redis.conf的requirepass密码
  Pipeline pipe = jedis.pipelined();
  for (int i = 0; i <50000; i++) {
  	pipe.set("key_"+String.valueOf(i),String.valueOf(i));
  }
  //将封装后的PIPE一次性发给redis
  pipe.sync();
  ```

- **原子化的批量请求响应模式（事务）**

  Redis可以利用事务机制批量执行命令。

- **发布订阅模式(pub/sub)**

  发布订阅模式是：一个客户端触发，多个客户端被动接收，通过服务器中转

- **脚本化的批量执行（lua）**

  客户端向服务器端提交一个lua脚本，服务器端执行该脚本。

####  1.2 请求数据格式

​	Redis客户端与服务器交互采用**序列化协议**（RESP）。 

​	请求以**字符串数组**的形式来表示要执行命令的参数 。

​	Redis使用命令特有（command-specific）数据类型作为回复。 

​	Redis通信协议的主要特点有： 

1. 客户端和服务器通过 TCP 连接来进行数据交互， 服务器默认的端口号为 6379 。 
2. 客户端和服务器发送的命令或数据一律以 \r\n （CRLF）结尾。 
3. 在这个协议中， 所有发送至 Redis 服务器的参数都是二进制安全（binary safe）的。 
4. 简单，高效，易读。

- 内联格式（telnet）

  可以使用telnet给Redis发送命令，首字符为Redis命令名的字符，格式为 str1 str2 str3...

  ```shell
  [root@localhost bin]# telnet 127.0.0.1 6379
  Trying 127.0.0.1...
  Connected to 127.0.0.1.
  Escape character is '^]'.
  ping
  +PONG
  exists name
  :1
  ```

- 规范格式（redis-cli）

  1. 间隔符号，在Linux下是\r\n，在Windows下是\n 
  2. 简单字符串 Simple Strings, 以 "+"加号开头 
  3. 错误 Errors, 以"-"减号 开头 
  4. 整数型 Integer， 以 ":" 冒号开头 
  5. 大字符串类型 Bulk Strings, 以 "$"美元符号开头，长度限制512M 
  6. 数组类型 Arrays，以 "*"星号开头 

  用SET命令来举例说明RESP协议的格式。

  ```shell
  redis> SET mykey Hello
  "OK"
  ```

  实际发送的请求数据

  ```shell
  *3\r\n$3\r\nSET\r\n$5\r\nmykey\r\n$5\r\nHello\r\n
  *3 #长度为3的数组
  $3 #长度为3的字符串
  SET
  $5 #长度为5的字符串
  mykey
  $5 #长度为5的字符串
  Hello
  ```

  实际收到的响应数据

  ```shell
  +OK\r\n
  ```

####  1.3 命令处理流程

整个流程包括：服务器启动监听、接收命令请求并解析、执行命令请求、返回命令回复等。

![](/Users/lixin/Documents/知识库/图片\redis\Snipaste_2021-12-10_16-32-27.png)

- Server启动时监听socket

  启动调用 initServer方法： 

  - 创建eventLoop（事件机制） 
  - 注册时间事件处理器 
  - 注册文件事件（socket）处理器 
  - 监听 socket 建立连接

- 建立Client

  - redis-cli建立socket 
  - redis-server为每个连接（socket）创建一个 Client 对象 
  - 创建文件事件监听socket 
  - 指定事件处理函数

- 读取socket数据到输入缓冲区

- 解析获取命令

  - 将输入缓冲区中的数据解析成对应的命令 

  - 判断是单条命令还是多条命令并调用相应的解析器解析

- 执行命令

  解析成功后调用processCommand方法执行命令，如下图：

  <img src="/Users/lixin/Documents/知识库/图片\redis\Snipaste_2021-12-10_16-39-23.png" style="zoom:150%;" />

  ​	大致分三个部分： 

  1. 调用 lookupCommand 方法获得对应的 redisCommand 
  2. 检测当前 Redis 是否可以执行该命令 
  3. 调用 call 方法真正执行命令

####  1.4 协议响应格式

- 状态回复

  对于状态，回复的第一个字节是“+”

  ```shell
  “+ok”
  ```

- 错误回复

  对于错误，回复的第一个字节是“ - ”

  ```
  1. -ERR unknown command 'foobar'
  2. -WRONGTYPE Operation against a key holding the wrong kind of value
  ```

- 整数回复

  对于整数，回复的第一个字节是":"

  ```
  ":6"
  ```

- 批量回复

  对于批量字符串，回复的第一个字节是“$”

  ```
  "$6 foobar"
  ```

- 多条批量回复

  对于多条批量回复（数组），回复的第一个字节是“*”

  ```
  “*3”
  ```

####  1.5 协议解析及处理

包括协议解析、调用命令、返回结果。

- 协议解析

  用户在Redis客户端键入命令后，Redis-cli会把命令转化为RESP协议格式，然后发送给服务器。服务器再对协议进行解析，分为三个步骤

  - 解析命令请求参数数量

    命令请求参数数量的协议格式为"*N\r\n" ,其中N就是数量，比如

    ```shell
    127.0.0.1:6379> set name:10 zhaoyun
    ```

    我们打开aof文件可以看到协议内容

    ```shell
    *3(/r/n)
    $3(/r/n)
    set(/r/n)
    $7(/r/n)
    name:10(/r/n)
    $7(/r/n)
    zhaoyun(/r/n)
    ```

    首字符必须是“*”，使用"\r"定位到行尾，之间的数就是参数数量了。

  - 循环解析请求参数

    首字符必须是"$"，使用"/r"定位到行尾，之间的数是参数的长度，从/n后到下一个"$"之间就是参数的值，循环解析知道没有"$".

- 协议执行

  协议的执行包括命令的调用和返回结果。

  判断参数个数和取出的参数是否一致。

  RedisServer解析完命令后,会调用函数processCommand处理该命令请求。

  - quit校验，如果是“quit”命令，直接返回并关闭客户端
  - 命令语法校验，执行lookupCommand，查找命令(set)，如果不存在则返回：“unknown command”错误。 
  - 参数数目校验，参数数目和解析出来的参数个数要匹配，如果不匹配则返回：“wrong number of arguments”错误。 
  - 此外还有权限校验，最大内存校验，集群校验，持久化校验等等。

  校验成功后，会调用call函数执行命令，并记录命令执行时间和调用次数 如果执行命令时间过长还要记录慢查询日志，执行命令后返回结果的类型不同则协议格式也不同，分为5类：状态回复、错误回复、整数回复、批量回复、多条批量回复。

###  二、事件处理机制

Redis服务器是典型的事件驱动系统。

Redis将事件分为两大类：文件事件和时间事件。

####  2.1 文件事件

文件事件即Socket的读写事件，也就是IO事件。如：客户端的连接、命令请求、数据回复、连接断开。

#####  2.1.1 Reactor

Redis事件处理机制采用单线程的Reactor模式，属于I/O多路复用的一种常见模式。

IO多路复用( I/O multiplexing ）指的通过单个线程管理多个Socket。 Reactor pattern(反应器设计模式)是一种为处理并发服务请求，并将请求提交到一个或者多个服务处理程序的事件设计模式。 Reactor模式是事件驱动的。

#####  2.1.2 4种IO多路复用模型与选择

select、poll、epoll、kqueue都是IO多路复用的机制。c

I/O多路复用就是通过一种机制，一个进程可以监视多个描述符（socket），一旦某个描述符就绪（一 般是读就绪或者写就绪），能够通知程序进行相应的读写操作。

- select

  ```c
  int select (int n, fd_set *readfds, fd_set *writefds, fd_set *exceptfds, struct timeval *timeout);
  ```

  select 函数监视的文件描述符分3类，分别是:

  - writefds：写事件
  - readfds：读事件
  - exceptfds：异常事件

  调用后select函数会阻塞，直到有描述符就绪（有数据可读、可写、或者有except），或者超时 （timeout指定等待时间，如果立即返回设为null即可），函数返回。当select函数返回后，可以通过遍历fd列表，来找到就绪的描述符。

  **优点：**select目前几乎在所有的平台上支持，其良好跨平台支持也是它的一个优点。

  **缺点：**单个进程打开的文件描述是有一定限制的，它由FD_SETSIZE设置，默认值是1024，采用数组存储。另外在检查数组中是否有文件描述需要读写时，采用的是线性扫描的方法，即不管这些socket是不是活 跃的，都轮询一遍，所以效率比较低。

- poll

  ```c
  int poll (struct pollfd *fds, unsigned int nfds, int timeout);
  struct pollfd {
      int fd; //文件描述符
      short events; //要监视的事件c
      short revents; //实际发生的事件
  };
  ```

  poll使用一个 pollfd的指针实现，pollfd结构包含了要监视的event和发生的event，不再使用select“参 数-值”传递的方式。

  **优点：** 采用链表的形式存储，它监听的描述符数量没有限制，可以超过select默认限制的1024大小。

  **缺点**：在检查链表中是否有文件描述需要读写时，采用的是线性扫描的方法，即不管这些socket是不是活 跃的，都轮询一遍，所以效率比较低。

- epoll

  epoll是在linux2.6内核中提出的，是之前的select和poll的增强版本。相对于select和poll来说，epoll更加灵活，没有描述符限制。epoll使用一个文件描述符管理多个描述符，将用户关心的文件描述符的事件存放到内核的一个事件表中，这样在用户空间和内核空间的copy只需一次。

  ```c
  /*
          创建一个epoll的句柄。自从linux2.6.8之后，size参数是被忽略的。需要注意的是，当创建好epoll
      句柄后，它就是会占用一个fd值，在linux下如果查看/proc/进程id/fd/，是能够看到这个fd的，所
      以在使用完epoll后，必须调用close()关闭，否则可能导致fd被耗尽。
  */
  int epoll_create(int size)
  ```

  ```c
  /*
      poll的事件注册函数，它不同于select()是在监听事件时告诉内核要监听什么类型的事件，而是在这里先注册要监听的事件类型。
      第一个参数是epoll_create()的返回值。
      第二个参数表示动作，用三个宏来表示：
      	EPOLL_CTL_ADD：注册新的fd到epfd中；
      	EPOLL_CTL_MOD：修改已经注册的fd的监听事件；
      	EPOLL_CTL_DEL：从epfd中删除一个fd；
      第三个参数是需要监听的fd。
      第四个参数是告诉内核需要监听什么事
  */
  int epoll_ctl(int epfd, int op, int fd, struct epoll_event *event)
  ```

  ```c
  /*
  	等待内核返回的可读写事件，最多返回maxevents个事件。
  */
  int epoll_wait(int epfd, struct epoll_event * events, int maxevents, int timeout);
  ```

  **优点：**

  1. epoll 没有最大并发连接的限制，上限是最大可以打开文件的数目，举个例子,在1GB内存的机器上大约是10万左右。
  2. 效率提升， epoll 最大的优点就在于它只管你“活跃”的连接 ，而跟连接总数无关，因此在实际的网络环境中， epoll 的效率就会远远高于 select 和 poll 。 
  3. epoll使用了共享内存，不用做内存拷贝。

- kqueue（少见）

  kqueue 是 unix 下的一个IO多路复用库。最初是2000年Jonathan Lemon在FreeBSD系统上开发的一个高性能的事件通知接口。注册一批socket描述符到 kqueue 以后，当其中的描述符状态发生变化时， kqueue 将一次性通知应用程序哪些描述符可读、可写或出错了。

  ```c
  struct kevent {
      uintptr_t ident; //是事件唯一的 key，在 socket() 使用中，它是 socket 的 fd
      句柄
      int16_t filter; //是事件的类型(EVFILT_READ socket 可读事件
      EVFILT_WRITE socket 可 写事件)c
      uint16_t flags; //操作方式
      uint32_t fflags; //
      intptr_t data; //数据长度
      void *udata; //数据
  };
  ```

  **优点：**能处理大量数据，性能较高

#####  2.1.3 文件事件分派器

在redis中，对于文件事件的处理采用了Reactor模型。采用的是epoll的实现方式。

![](/Users/lixin/Documents/知识库/图片\redis\Snipaste_2021-12-13_09-15-06.png)

Redis在主循环中统一处理文件事件和时间事件，信号事件则由专门的handler来处理。

```c
void aeMain(aeEventLoop *eventLoop) {
    eventLoop->stop = 0;
    //循环监听事件
    while (!eventLoop->stop) { 
        // 阻塞之前的处理
        if (eventLoop->beforesleep != NULL)
        eventLoop->beforesleep(eventLoop);
        // 事件处理，第二个参数决定处理哪类事件
        aeProcessEvents(eventLoop, AE_ALL_EVENTS|AE_CALL_AFTER_SLEEP);
    }
}
```

#####  2.1.4 事件处理器

- 连接处理函数acceptTCPHandler

  ​	当客户端向 Redis 建立 socket时，aeEventLoop 会调用 acceptTcpHandler 处理函数，服务器会为每个链接创建一个 Client 对象，并创建相应文件事件来监听socket的可读事件，并指定事件处理函数。

  ```c
  // 当客户端建立链接时进行的eventloop处理函数 networking.c
  void acceptTcpHandler(aeEventLoop *el, int fd, void *privdata, int mask) {
      ....
      // 层层调用，最后在anet.c 中 anetGenericAccept 方法中调用 socket 的 accept 方法
      cfd = anetTcpAccept(server.neterr, fd, cip, sizeof(cip), &cport);
      if (cfd == ANET_ERR) {
      if (errno != EWOULDBLOCK)
          serverLog(LL_WARNING,
          "Accepting client connection: %s", server.neterr);
          return;
      }
      serverLog(LL_VERBOSE,"Accepted %s:%d", cip, cport);
      /**
      * 进行socket 建立连接后的处理
      */
      acceptCommonHandler(cfd,0,cip);
  }
  ```

- 请求处理函数readQueryFromClient

  ​	当客户端通过 socket 发送来数据后，Redis 会调用 readQueryFromClient 方法,readQueryFromClient 方法会调用 read 方法从 socket 中读取数据到输入缓冲区中，然后判断其大小是否大于系统设置的 client_max_querybuf_len，如果大于，则向 Redis返回错误信息，并关闭 client。

  ```c
  // 处理从client中读取客户端的输入缓冲区内容。
  void readQueryFromClient(aeEventLoop *el, int fd, void *privdata, int mask) {
      client *c = (client*) privdata;
      ....
      if (c->querybuf_peak < qblen) c->querybuf_peak = qblen;
      c->querybuf = sdsMakeRoomFor(c->querybuf, readlen);
      // 从 fd 对应的socket中读取到 client 中的 querybuf 输入缓冲区
      nread = read(fd, c->querybuf+qblen, readlen);
      ....
      // 如果大于系统配置的最大客户端缓存区大小，也就是配置文件中的client-query-buffer-limit
      if (sdslen(c->querybuf) > server.client_max_querybuf_len) {
          sds ci = catClientInfoString(sdsempty(),c), bytes = sdsempty();
          // 返回错误信息，并且关闭client
          bytes = sdscatrepr(bytes,c->querybuf,64);
          serverLog(LL_WARNING,"Closing client that reached max query buffer
          length: %s (qbuf initial bytes: %s)", ci, bytes);
          sdsfree(ci);
          sdsfree(bytes);
          freeClient(c);
          return;
      }
      if (!(c->flags & CLIENT_MASTER)) {
          // processInputBuffer 处理输入缓冲区
          processInputBuffer(c);
      } else {
          // 如果client是master的连接
          size_t prev_offset = c->reploff;
          processInputBuffer(c);
          // 判断是否同步偏移量发生变化，则通知到后续的slave
          size_t applied = c->reploff - prev_offset;
          if (applied) {
              replicationFeedSlavesFromMasterStream(server.slaves,
              c->pending_querybuf, applied);
              sdsrange(c->pending_querybuf,applied,-1);
      	}
      }
  }
  ```

- 命令回复处理器 sendReplyToClient

  sendReplyToClient函数是Redis的命令回复处理器，这个处理器负责将服务器执行命令后得到的命令回复通过套接字返回给客户端。 

  1. 将outbuf内容写入到套接字描述符并传输到客户端 
  2. aeDeleteFileEvent 用于删除文件写事件

####  2.2 时间事件

时间事件分为定时事件与周期事件，一个时间事件主要由以下三个属性组成：

- id(全局唯一id) 
- when (毫秒时间戳，记录了时间事件的到达时间)
- timeProc（时间事件处理器，当时间到达时，Redis就会调用相应的处理器来处理事件）

```c
/* Time event structure
*
* 时间事件结构
*/
typedef struct aeTimeEvent {
    // 时间事件的唯一标识符
    long long id; /* time event identifier. */
    // 事件的到达时间，存贮的是UNIX的时间戳
    long when_sec; /* seconds */
    long when_ms; /* milliseconds */
    // 事件处理函数，当到达指定时间后调用该函数处理对应的问题
    aeTimeProc *timeProc;
    // 事件释放函数
    aeEventFinalizerProc *finalizerProc;
    // 多路复用库的私有数据
    void *clientData;
    // 指向下个时间事件结构，形成链表
    struct aeTimeEvent *next;
} aeTimeEvent;
```

#####  2.2.1 serverCron

​	时间事件的最主要的应用是在redis服务器需要对自身的资源与配置进行定期的调整，从而确保服务器的 长久运行，这些操作由redis.c中的serverCron函数实现。该时间事件主要进行以下操作：

1）更新redis服务器各类统计信息，包括时间、内存占用、数据库占用等情况。 

2）清理数据库中的过期键值对。 

3）关闭和清理连接失败的客户端。 

4）尝试进行aof和rdb持久化操作。 

5）如果服务器是主服务器，会定期将数据向从服务器做同步操作。 

6）如果处于集群模式，对集群定期进行同步与连接测试操作。

​	redis服务器开启后，就会周期性执行此函数，直到redis服务器关闭为止。默认每秒执行10次，平均100毫秒执行一次，可以在redis配置文件的hz选项，调整该函数每秒执行的次数。

- server.hz

  ```shell
  #serverCron在一秒内执行的次数,在redis/conf中可以配,server.hz是100，也就是servreCron的执行间隔是10ms
  hz 100
  ```

- run_with_period

  ```c
  #define run_with_period(_ms_) \
  if ((_ms_ <= 1000/server.hz) || !(server.cronloops%((_ms_)/(1000/server.hz))))
  ```

  ​	定时任务执行都是在10毫秒的基础上定时处理自己的任务(run_with_period(ms))，即调用 run_with_period(ms)[ms是指多长时间执行一次，单位是毫秒]来确定自己是否需要执行。

  ​	返回1表示执行。 假如有一些任务需要每500ms执行一次，就可以在serverCron中用run_with_period(500)把每500ms需要执行一次的工作控制起来。

#####  2.2.2 定时事件

​	让一段程序在指定的时间之后执行一次 aeTimeProc（时间处理器）的返回值是AE_NOMORE，该事件在达到后删除，之后不会再重复。

#####  2.2.3 周期性事件

​	让一段程序每隔指定时间就执行一次 aeTimeProc（时间处理器）的返回值不是AE_NOMORE，当一个时间事件到达后，服务器会根据时间处理器的返回值，对时间事件的 when 属性进行更新，让这 个事件在一段时间后再次达到。 serverCron就是一个典型的周期性事件。

####  2.3 aeEventLoop

aeEventLoop是整个事件驱动的核心，Redis自己的事件处理机制，它管理着文件事件表和时间事件列表， 不断地循环处理着就绪的文件事件和到期的时间事件。

![](/Users/lixin/Documents/知识库/图片\redis\Snipaste_2021-12-13_09-41-27.png)

```c
typedef struct aeEventLoop {
    //最大文件描述符的值
    int maxfd; /* highest file descriptor currently registered */
    //文件描述符的最大监听数
    int setsize; /* max number of file descriptors tracked */
    //用于生成时间事件的唯一标识id
    long long timeEventNextId;
    //用于检测系统时间是否变更（判断标准 now<lastTime）
    time_t lastTime; /* Used to detect system clock skew */
    //注册的文件事件
    aeFileEvent *events; /* Registered events */
    //已就绪的事件
    aeFiredEvent *fired; /* Fired events */
    //注册要使用的时间事件
    aeTimeEvent *timeEventHead;
    //停止标志，1表示停止
    int stop;
    //这个是处理底层特定API的数据，对于epoll来说，该结构体包含了epoll fd和epoll_event
    void *apidata; /* This is used for polling API specific data */
    //在调用processEvent前（即如果没有事件则睡眠），调用该处理函数
    aeBeforeSleepProc *beforesleep;
    //在调用aeApiPoll后，调用该函数
    aeBeforeSleepProc *aftersleep;
} aeEventLoop;
```

#####  2.3.1 初始化

​	Redis 服务端在其初始化函数 initServer 中，会创建事件管理器 aeEventLoop 对象。 

​	函数 aeCreateEventLoop 将创建一个事件管理器，主要是初始化 aeEventLoop 的各个属性值，比如 events 、fired 、timeEventHead 和 apidata ：

- 首先创建 aeEventLoop 对象。
- 初始化注册的文件事件表、就绪文件事件表。 events 指针指向注册的文件事件表、 fired 指针指 向就绪文件事件表。表的内容在后面添加具体事件时进行初变更。 
- 初始化时间事件列表，设置 timeEventHead 和 timeEventNextId 属性。 调用 aeApiCreate 函数创建 epoll 实例，并初始化 apidata 

#####  2.3.2 FileEvents,FiredEvent,apidata

- aeFileEvent:已经注册并需要监听的事件的结构体。

  ```c
  typedef struct aeFileEvent {
      // 监听事件类型掩码，
      // 值可以是 AE_READABLE 或 AE_WRITABLE ，
      // 或者 AE_READABLE | AE_WRITABLE
      int mask; /* one of AE_(READABLE|WRITABLE) */
      // 读事件处理器
      aeFileProc *rfileProc;
      // 写事件处理器
      aeFileProc *wfileProc;
      // 多路复用库的私有数据
      void *clientData;
  } aeFileEvent;
  ```

- aeFiredEvent:已就绪的文件事件

  ```c
  typedef struct aeFiredEvent {
      // 已就绪文件描述符
      int fd;
      // 事件类型掩码，
      // 值可以是 AE_READABLE 或 AE_WRITABLE
      // 或者是两者的或
      int mask;
  } aeFiredEvent
  ```

- void *apidata:在ae创建的时候,会被赋值为aeApiState结构体,结构体的定义如下：

  ```c
  typedef struct aeApiState {
      // epoll_event 实例描述符
      int epfd;
      // 事件槽
      struct epoll_event *events;
  } aeApiState;
  ```

  这个结构体是为了epoll所准备的数据结构。redis可以选择不同的io多路复用方法。因此 apidata 是个 void类型，根据不同的io多路复用库来选择不同的实现 

  ae.c里面使用如下的方式来决定系统使用的机制:

  ```shell
  #ifdef HAVE_EVPORT
  #include "ae_evport.c"
  #else
      #ifdef HAVE_EPOLL
      #include "ae_epoll.c"
      #else
          #ifdef HAVE_KQUEUE
          #include "ae_kqueue.c"
          #else
          #include "ae_select.c"
          #endif
      #endif
  #endif
  ```

#####  2.3.3 timeEvent,beforesleep,aftersleep

​	aeTimeEvent结构体为时间事件，Redis 将所有时间事件都放在一个无序链表中，每次 Redis 会遍历整 个链表，查找所有已经到达的时间事件，并且调用相应的事件处理器。

```c
typedef struct aeTimeEvent {
    /* 全局唯一ID */
    long long id; /* time event identifier. */
    /* 秒精确的UNIX时间戳，记录时间事件到达的时间*/
    long when_sec; /* seconds */
    /* 毫秒精确的UNIX时间戳，记录时间事件到达的时间*/
    long when_ms; /* milliseconds */
    /* 时间处理器 */
    aeTimeProc *timeProc;
    /* 事件结束回调函数，析构一些资源*/
    aeEventFinalizerProc *finalizerProc;
    /* 私有数据 */
    void *clientData;
    /* 前驱节点 */
    struct aeTimeEvent *prev;
    /* 后继节点 */
    struct aeTimeEvent *next;
} aeTimeEvent;
```

- beforesleep

  对象是一个回调函数，在 redis-server 初始化时已经设置好了。

  功能： 

  - 检测集群状态 
  - 随机释放已过期的键 
  - 在数据同步复制阶段取消客户端的阻塞 
  - 处理输入数据，并且同步副本信息 
  - 处理非阻塞的客户端请求 
  - AOF持久化存储策略，类似于mysql的bin log 
  - 使用挂起的输出缓冲区处理写入 

- aftersleep

  对象是一个回调函数，在IO多路复用与IO事件处理之间被调用。

#####  2.3.4 aeMain

​	aeMain 函数其实就是一个封装的 while 循环，循环中的代码会一直运行直到 eventLoop的stop 被设 置为1(true)。它会不停尝试调用 aeProcessEvents 对可能存在的多种事件进行处理，而 aeProcessEvents 就是实际用于处理事件的函数。

```C
void aeMain(aeEventLoop *eventLoop) {
    eventLoop->stop = 0;
    while (!eventLoop->stop) {
        if (eventLoop->beforesleep != NULL)
        	eventLoop->beforesleep(eventLoop);
        aeProcessEvents(eventLoop, AE_ALL_EVENTS);
    }
}
```

​	aemain函数中，首先调用Beforesleep。这个方法在Redis每次进入sleep/wait去等待监听的端口发生 I/O事件之前被调用。当有事件发生时，调用aeProcessEvent进行处理。

#####  2.3.5 aeProcessEvent

首先计算距离当前时间最近的时间事件，以此计算一个超时时间； 

然后调用 aeApiPoll 函数去等待底层的I/O多路复用事件就绪； 

aeApiPoll 函数返回之后，会处理所有已经产生文件事件和已经达到的时间事件。

```c
int aeProcessEvents(aeEventLoop *eventLoop, int flags)
{
    //processed记录这次调度执行了多少事件
    int processed = 0, numevents;
    if (!(flags & AE_TIME_EVENTS) && !(flags & AE_FILE_EVENTS)) return 0;
    if (eventLoop->maxfd != -1 || ((flags & AE_TIME_EVENTS) && !(flags & AE_DONT_WAIT))) {
        int j;
        aeTimeEvent *shortest = NULL;
        struct timeval tv, *tvp;
        if (flags & AE_TIME_EVENTS && !(flags & AE_DONT_WAIT))
        //获取最近将要发生的时间事件
        shortest = aeSearchNearestTimer(eventLoop);
        //计算aeApiPoll的超时时间
        if (shortest) {
            long now_sec, now_ms;
            //获取当前时间
            aeGetTime(&now_sec, &now_ms);
            tvp = &tv;
            //计算距离下一次发生时间时间的时间间隔
            long long ms = (shortest->when_sec - now_sec)*1000 + shortest->when_ms - now_ms;
            if (ms > 0) {
                tvp->tv_sec = ms/1000;
                tvp->tv_usec = (ms % 1000)*1000;
            } else {
                tvp->tv_sec = 0;
                tvp->tv_usec = 0;
            }
        } else {
            //没有时间事件
            if (flags & AE_DONT_WAIT) {//马上返回，不阻塞
                tv.tv_sec = tv.tv_usec = 0;
                tvp = &tv;
            } else {
            	tvp = NULL; //阻塞到文件事件发生
            }
        }
        //等待文件事件发生，tvp为超时时间，超时马上返回(tvp为0表示马上，为null表示阻塞到事件发生)
        numevents = aeApiPoll(eventLoop, tvp);
        for (j = 0; j < numevents; j++) {//处理触发的文件事件
            aeFileEvent *fe = &eventLoop->events[eventLoop->fired[j].fd];
            int mask = eventLoop->fired[j].mask;
            int fd = eventLoop->fired[j].fd;
            int rfired = 0;
            if (fe->mask & mask & AE_READABLE) {
                rfired = 1;//处理读事件
                fe->rfileProc(eventLoop,fd,fe->clientData,mask);
            }
            if (fe->mask & mask & AE_WRITABLE) {
                if (!rfired || fe->wfileProc != fe->rfileProc)
                    //处理写事件
                    fe->wfileProc(eventLoop,fd,fe->clientData,mask);
                }
            processed++;
        }
    }
    if (flags & AE_TIME_EVENTS)//时间事件调度和执行
    	processed += processTimeEvents(eventLoop);
    return processed;
}
```

- 计算最早时间事件的执行时间，获取文件时间可执行时间 

  ​	aeSearchNearestTimer，aeProcessEvents 都会先 计算最近的时间事件发生所需要等待的时间 ，然后调用 aeApiPoll 方法，在这段时间中等待事件的发生，在这段时间中如果发生了文件事件，就会优先处理文件事件，否则就会一直 等待，直到最近的时间事件需要触发。

- 堵塞等待文件事件产生

  aeApiPoll 用到了epoll，select，kqueue和evport四种实现方式。

- 处理文件事件

  rfileProc 和 wfileProc 就是在文件事件被创建时传入的函数指针 

  处理读事件：rfileProc  

  处理写事件：wfileProc

- 处理时间事件 

  processTimeEvents 取得当前时间，循环时间事件链表，如果当前时间>=预订执行时间，则执行时间处理函数。

##  第四部分 Redis持久化

​		Redis是内存数据库，宕机后数据会消失，Redis重启后快速回复数据，要提供持久化机制。Redis持久化是为了快速的恢复数据而不是为了存储数据，Redis有两种持久化方式：RDB和AOF 

​		注意：Redis持久化不保证数据的完整性。 

​		当Redis用作DB时，DB数据要完整，所以一定要有一个完整的数据源（文件、mysql），在系统启动时，从这个完整的数据源中将数据load到Redis中，数据量较小，不易改变，比如：字典库（xml、Table）。

​		通过info命令可以查看关于持久化的信息

```shell
# Persistence
loading:0
rdb_changes_since_last_save:1
rdb_bgsave_in_progress:0
rdb_last_save_time:1589363051
rdb_last_bgsave_status:ok
rdb_last_bgsave_time_sec:-1
rdb_current_bgsave_time_sec:-1
rdb_last_cow_size:0
aof_enabled:1
aof_rewrite_in_progress:0
aof_rewrite_scheduled:0
aof_last_rewrite_time_sec:-1
aof_current_rewrite_time_sec:-1
aof_last_bgrewrite_status:ok
aof_last_write_status:ok
aof_last_cow_size:0
aof_current_size:58
aof_base_size:0
aof_pending_rewrite:0
aof_buffer_length:0
aof_rewrite_buffer_length:0
aof_pending_bio_fsync:0
aof_delayed_fsync:0
```

###  一、RDB(Redis DataBase)

RDB，是redis默认的存储方式，RDB方式是通过快照（snapshotting）完成的。只关注当前时刻的数据，不关注过程。

####  1.1 触发快照的方式

- 符合自定义配置的快照规则
- 执行save或者bgsave命令
- 执行flushall命令
- 执行主从复制操作（第一次）

#####  1.1.1 配置参数定期执行

在redis.conf中配置：save 多少秒内 数据变了多少

```shell
#漏斗设计 提供性能
save "" # 不使用RDB存储 不能主从
save 900 1 # 表示15分钟（900秒钟）内至少1个键被更改则进行快照。
save 300 10 # 表示5分钟（300秒）内至少10个键被更改则进行快照。
save 60 10000 # 表示1分钟内至少10000个键被更改则进行快照。
```

#####  1.1.2 命令显示触发

```shell
127.0.0.1:6379> bgsave
Background saving started
```

####  1.2 RBD执行流程（原理）

![](/Users/lixin/Documents/知识库/图片\redis\Snipaste_2021-12-13_17-08-16.png)

1. Redis父进程首先判断：当前是否在执行save，或bgsave/bgrewriteaof（aof文件重写命令）的子进程，如果在执行则bgsave命令直接返回。 
2. 父进程执行fork（调用OS函数复制主进程）操作创建子进程，这个复制过程中父进程是阻塞的， Redis不能执行来自客户端的任何命令。 
3. 父进程fork后，bgsave命令返回”Background saving started”信息并不再阻塞父进程，并可以响 应其他命令。 
4. 子进程创建RDB文件，根据父进程内存快照生成临时快照文件，完成后对原有文件进行原子替换。 （RDB始终完整） 
5. 子进程发送信号给父进程表示完成，父进程更新统计信息。 
6. 父进程fork子进程后，继续工作。

####  1.3 RBD文件结构

![](/Users/lixin/Documents/知识库/图片\redis\Snipaste_2021-12-13_17-13-00.png)

1. 头部5字节固定为“REDIS”字符串 

2. 4字节“RDB”版本号（不是Redis版本号），当前为9，填充后为0009 

3. 辅助字段，以key-value的形式

   | 字段名     | 字段值     | 字段名         | 字段值      |
   | ---------- | ---------- | -------------- | ----------- |
   | redis-ver  | 5.0.5      | aof-preamble   | 是否开启aof |
   | redis-bits | 64/32      | repl-stream-db | 主从复制    |
   | ctime      | 当前时间戳 | repl-id        | 主从复制    |
   | used-mem   | 使用内存   | repl-offset    | 主从复制    |

4. 存储数据库号码

5. 字典大小

6. 过期key 

7. 主要数据，以key-value的形式存储 

8. 结束标志 

9. 校验和，就是看文件是否损坏，或者是否被修改。 

   可以用winhex打开dump.rdb文件查看。

![](/Users/lixin/Documents/知识库/图片\redis\Snipaste_2021-12-13_17-16-54.png)

####  1.4 RBD的优缺点

- **优点** 

  ​	RDB是二进制压缩文件，占用空间小，便于传输（传给slaver） 主进程fork子进程，可以最大化Redis性能，主进程不能太大，Redis的数据量不能太大，复制过程中主进程阻塞 

- **缺点** 

  ​	不保证数据完整性，会丢失最后一次快照以后更改的所有数据

###  二、AOF(append only file)

​	AOF是Redis的另一种持久化方式。Redis默认情况下是不开启的。开启AOF持久化后Redis 将所有对数据库进行过写入的命令（及其参数）（RESP）记录到 AOF 文件， 以此达到记录数据库状态的目的， 这样当Redis重启后只要按顺序回放这些命令就会恢复到原始状态了。 AOF会记录过程，RDB只管结果。

####  2.1 AOF持久化实现

配置 redis.conf

```shell
# 可以通过修改redis.conf配置文件中的appendonly参数开启
appendonly yes
# AOF文件的保存位置和RDB文件的位置相同，都是通过dir参数设置的。
dir ./
# 默认的文件名是appendonly.aof，可以通过appendfilename参数修改
appendfilename appendonly.aof
```

####  2.2 AOF原理

​	AOF文件中存储的是redis的命令，同步命令到 AOF 文件的整个过程可以分为三个阶段： 

1. 命令传播：Redis 将执行完的命令、命令的参数、命令的参数个数等信息发送到 AOF 程序中。 

2. 缓存追加：AOF 程序根据接收到的命令数据，将命令转换为网络通讯协议的格式，然后将协议内容追加到服务器的 AOF 缓存中。 

3. 文件写入和保存：AOF 缓存中的内容被写入到 AOF 文件末尾，如果设定的 AOF 保存条件被满足的话， fsync函数或者fdatasync函数会被调用，将写入的内容真正地保存到磁盘中。

- **命令传播**

​	当一个 Redis 客户端需要执行命令时， 它通过网络连接， 将协议文本发送给 Redis 服务器。服务器在接到客户端的请求之后， 它会根据协议文本的内容， 选择适当的命令函数， 并将各个参数从字符串文本转换为 Redis 字符串对象（ StringObject ）。每当命令函数成功执行之后， 命令参数都会被传播到AOF程序。

- **缓存追加**

​	当命令被传播到 AOF 程序之后， 程序会根据命令以及命令的参数， 将命令从字符串对象转换回原来的协议文本。协议文本生成之后， 它会被追加到 redis.h/redisServer 结构的 aof_buf 末尾。 redisServer 结构维持着 Redis 服务器的状态， aof_buf 域则保存着所有等待写入到 AOF 文件的协议文本（RESP）。

- **文件写入和保存**

​		每当服务器常规任务函数被执行、 或者事件处理器被执行时， aof.c/flushAppendOnlyFile 函数都会被调用， 这个函数执行以下两个工作：

WRITE：根据条件，将 aof_buf 中的缓存写入到 AOF 文件。 

SAVE：根据条件，调用 fsync 或 fdatasync 函数，将 AOF 文件保存到磁盘中。

- **AOF保存模式**

  Redis 目前支持三种 AOF 保存模式，它们分别是： 

  - AOF_FSYNC_NO ：

    不保存。 在这种模式下， 每次调用 flushAppendOnlyFile 函数， WRITE 都会被执行， 但SAVE 会被略过。 SAVE 只会在以下任意一种情况中被执行： Redis 被关闭、AOF 功能被关闭、系统的写缓存被刷新（可能是缓存已经被写满，或者定期保存操作被执行） 这三种情况下的 SAVE 操作都会引起 Redis 主进程阻塞。

  - AOF_FSYNC_EVERYSEC ：每一秒钟保存一次。（默认/推荐）

    在这种模式中， SAVE 原则上每隔一秒钟就会执行一次， 因为 SAVE 操作是由后台子线程（fork）调用 的， 所以它不会引起服务器主进程阻塞。

  -  AOF_FSYNC_ALWAYS ：每执行一个命令保存一次。（不推荐）

    在这种模式下，每次执行完一个命令之后， WRITE 和 SAVE 都会被执行。 另外，因为 SAVE 是由 Redis 主进程执行的，所以在 SAVE 执行期间，主进程会被阻塞，不能接受命令 请求。 AOF 保存模式对性能和安全性的影响 对于三种 AOF 保存模式， 它们对服务器主进程的阻塞情况如下：

    ![](/Users/lixin/Documents/知识库/图片\redis\Snipaste_2021-12-13_17-41-53.png)

- **AOF重写**

  ​	AOF记录数据的变化过程，越来越大，需要重写“瘦身” Redis可以在 AOF体积变得过大时，自动地在后台（Fork子进程）对 AOF进行重写。重写后的新 AOF文 件包含了恢复当前数据集所需的最小命令集合。 所谓的“重写”其实是一个有歧义的词语， 实际上， AOF 重写并不需要对原有的 AOF 文件进行任何写入和读取， 它针对的是数据库中键的当前值。

  ```shell
   set s1 11set s1 22 ------- > set s1 33set s1 33	shell
  ```

  ​	Redis 不希望 AOF 重写造成服务器无法处理请求， 所以 Redis 决定将 AOF 重写程序放到（后台）子进 程里执行， 这样处理的最大好处是：

  1、子进程进行 AOF 重写期间，主进程可以继续处理命令请求。 

  2、子进程带有主进程的数据副本，使用子进程而不是线程，可以在避免锁的情况下，保证数据的安全性。

  ​	不过， 使用子进程也有一个问题需要解决： 因为子进程在进行 AOF 重写期间， 主进程还需要继续处理 命令， 而新的命令可能对现有的数据进行修改， 这会让当前数据库的数据和重写后的 AOF 文件中的数 据不一致。 

  ​	为了解决这个问题， Redis 增加了一个 AOF 重写缓存， 这个缓存在 fork出子进程之后开始启用， Redis 主进程在接到新的写命令之后， 除了会将这个写命令的协议内容追加到现有的 AOF 文件之外， 还会追加到这个缓存中。

  ![](/Users/lixin/Documents/知识库/图片\redis\Snipaste_2021-12-13_17-45-38.png)

  - **重写过程分析（整个重写操作是绝对安全的）：**

  ​	Redis 在创建新 AOF 文件的过程中，会继续将命令追加到现有的 AOF 文件里面，即使重写过程中发生 停机，现有的 AOF 文件也不会丢失。 而一旦新 AOF 文件创建完毕，Redis 就会从旧 AOF 文件切换到 新 AOF 文件，并开始对新 AOF 文件进行追加操作。 

  ​	当子进程在执行 AOF 重写时， 主进程需要执行以下三个工作：

  ​	1. 处理命令请求。 

  ​	2. 将写命令追加到现有的 AOF 文件中。 

  ​	3. 将写命令追加到 AOF 重写缓存中。 

  ​	这样一来可 以保证：现有的 AOF 功能会继续执行，即使在 AOF 重写期间发生停机，也不会有任何数据丢失。 所有对数据库进行修改的命令都会被记录到 AOF 重写缓存中。 当子进程完成 AOF 重写之后， 它会向父进程发送一个完成信号， 父进程在接到完成信号之后， 会调用一个信号处理函数， 并完成以下工作： 

  1. 将 AOF 重写缓存中的内容全部写入到新 AOF 文件中。 对新的 AOF 文件进行改名，覆盖原有的 AOF 文 件。 

  2. Redis数据库里的+AOF重写过程中的命令------->新的AOF文件---->覆盖老的 

    当步骤 1 执行完毕之后， 现有 AOF 文件、新 AOF 文件和数据库三者的状态就完全一致了。 

    当步骤 2 执行完毕之后， 程序就完成了新旧两个 AOF 文件的交替。 这个信号处理函数执行完毕之后， 主进程就可以继续像往常一样接受命令请求了。 在整个 AOF 后台重写过程中， 只有最后的写入缓存和改名操作会造成主进程阻塞， 在其他时候， AOF 后台重写都不会对主进程造成阻塞， 这将 AOF 重写对性能造成的影响降到了最低。 

  ​	以上就是 AOF 后台重写， 也即是 BGREWRITEAOF 命令(AOF重写)的工作原理。

  ![](/Users/lixin/Documents/知识库/图片\redis\Snipaste_2021-12-13_17-49-50.png)

  - 触发方式

    1. 配置触发

       在redis.conf中配置

       ```shell
       # 表示当前aof文件大小超过上一次aof文件大小的百分之多少的时候会进行重写。如果之前没有重写过，以启动时aof文件大小为准
       auto-aof-rewrite-percentage 100
       # 限制允许重写最小aof文件大小，也就是文件大小小于64mb的时候，不需要进行优化
       auto-aof-rewrite-min-size 64mb
       ```
       
2. 执行bgrewriteaof命令
   
   ```shell
       127.0.0.1:6379> bgrewriteaof
       Background append only file rewriting started
   ```

- 混合持久化

  ​	RDB和AOF各有优缺点，Redis 4.0 开始支持rdb 和aof 的混合持久化。

  ​	如果把混合持久化打开，aof rewrite 的时候就直接把 rdb 的内容写到 aof 文件开头。 RDB的头+AOF的身体---->appendonly.aof 

  ​	开启混合持久化。

  ```shell
  aof-use-rdb-preamble yes
  ```

  ![](/Users/lixin/Documents/知识库/图片\redis\Snipaste_2021-12-13_17-53-42.png)

  ​	我们可以看到该AOF文件是rdb文件的头和aof格式的内容，在加载时，首先会识别AOF文件是否以 REDIS字符串开头，如果是就按RDB格式加载，加载完RDB后继续按AOF格式加载剩余部分。

####  2.3 AOF文件的载入与数据还原

​	因为AOF文件里面包含了重建数据库状态所需的所有写命令，所以服务器只要读入并重新执行一遍AOF 文件里面保存的写命令，就可以还原服务器关闭之前的数据库状态。

​	Redis读取AOF文件并还原数据库状 态的详细步骤如下： 

1. 创建一个不带网络连接的伪客户端（fake client）：因为Redis的命令只能在客户端上下文中执行，而载入AOF文件时所使用的命令直接来源于AOF文件而不是网络连接，所以服务器使用了一个没有网络连接的伪客户端来执行AOF文件保存的写命令，伪客户端执行命令 的效果和带网络 连接的客户端执行命令的效果完全一样 

2. 从AOF文件中分析并读取出一条写命令 

3. 使用伪客户端执行被读出的写命令 

4. 一直执行步骤2和步骤3，直到AOF文件中的所有写命令都被处理完毕为止 

   当完成 以上步骤之后，AOF文件所保存的数据库状态就会被完整地还原出来，整个过程如下图所示：

![](/Users/lixin/Documents/知识库/图片\redis\Snipaste_2021-12-13_17-56-20.png)



###  三、RBD与AOF对比

1. RDB存某个时刻的数据快照，采用二进制压缩存储，AOF存操作命令，采用文本存储(混合) 
2. RDB性能高、AOF性能较低
3. RDB在配置触发状态会丢失最后一次快照以后更改的所有数据，AOF设置为每秒保存一次，则最多 丢2秒的数据 
4. Redis以主服务器模式运行，RDB不会保存过期键值对数据，Redis以从服务器模式运行，RDB会保 存过期键值对，当主服务器向从服务器同步时，再清空过期键值对。

AOF写入文件时，对过期的key会追加一条del命令，当执行AOF重写时，会忽略过期key和del命令。

![](/Users/lixin/Documents/知识库/图片\redis\Snipaste_2021-12-13_17-58-03.png)

**应用场景：**

内存数据库 rdb+aof 数据不容易丢

有原始数据源： 每次启动时都从原始数据源中初始化 ，则不用开启持久化 （数据量较小） 

缓存服务器：rdb 一般 性能高

数据还原时：有rdb+aof 则还原aof，因为RDB会造成文件的丢失，AOF相对数据要完整。 只有rdb，则还原rdb

##  第五部分 Redis扩展功能

###  一、发布与订阅

​	Redis提供了发布订阅功能，可以用于消息的传输 Redis的发布订阅机制包括三个部分，publisher，subscriber和Channel

![](/Users/lixin/Documents/知识库/图片\redis\Snipaste_2021-12-14_10-44-01.png)

​	发布者和订阅者都是Redis客户端，Channel则为Redis服务器端。 发布者将消息发送到某个的频道，订阅了这个频道的订阅者就能接收到这条消息。

####  1.1 频道/模式的订阅与退订

- **subscribe**

  订阅 subscribe channel1 channel2 .. 

  Redis客户端1订阅频道1和频道2

  ```shell
  127.0.0.1:6379> subscribe ch1 ch2
  Reading messages... (press Ctrl-C to quit)
  1) "subscribe"
  2) "ch1"
  3) (integer) 1
  1) "subscribe"
  2) "ch2"
  3) (integer) 2
  ```

- **publish**

  发布消息 publish channel message

  Redis客户端2将消息发布在频道1和频道2上

  ```shell
  127.0.0.1:6379> publish ch1 hello
  (integer) 1
  127.0.0.1:6379> publish ch2 world
  (integer) 1
  ```

- **unsubscribe**

  退订 channel

  Redis客户端1退订频道1

  ```shell
  127.0.0.1:6379> unsubscribe ch1
  1) "unsubscribe"
  2) "ch1"
  3) (integer) 0
  ```

- **psubscribe** 

  模式匹配 psubscribe +模式 Redis客户端1订阅所有以ch开头的频道

  ```shell
  127.0.0.1:6379> psubscribe ch*
  Reading messages... (press Ctrl-C to quit)
  1) "psubscribe"
  2) "ch*"
  3) (integer) 1
  ```

  Redis客户端2发布信息在频道5上

  ```shell
  127.0.0.1:6379> publish ch5 helloworld
  (integer) 1
  ```

  Redis客户端1收到频道5的信息

  ```shell
  1) "pmessage"
  2) "ch*"
  3) "ch5"
  4) "helloworld"
  ```

- **punsubscribe** 

  退订模式

  ```shell
  127.0.0.1:6379> punsubscribe ch*
  1) "punsubscribe"
  2) "ch*"
  3) (integer) 0
  ```

####  1.2 发布订阅的机制

- 订阅某个频道或模式： 

  - 客户端（client）： 

    属性为pubsub_channels，该属性表明了该客户端订阅的所有频道 

    属性为pubsub_patterns，该属性表示该客户端订阅的所有模式 

  - 服务器端（RedisServer）： 

    属性为pubsub_channels，该服务器端中的所有频道以及订阅了这个频道的客户端 

    属性为pubsub_patterns，该服务器端中的所有模式和订阅了这些模式的客户端

  ```c
  typedef struct redisClient {
      ...
      dict *pubsub_channels; //该client订阅的channels，以channel为key用dict的方式组织
      list *pubsub_patterns; //该client订阅的pattern，以list的方式组织
      ...
  } redisClient;
  
  struct redisServer {
      ...
      dict *pubsub_channels; //redis server进程中维护的channel dict，它以channel为key，订 阅channel的client list为value
      list *pubsub_patterns; //redis server进程中维护的pattern list
      int notify_keyspace_events;
      ...
  };
  ```

  ​	当客户端向某个频道发送消息时，Redis首先在redisServer中的pubsub_channels中找出键为该频道的 结点，遍历该结点的值，即遍历订阅了该频道的所有客户端，将消息发送给这些客户端。 然后，遍历结构体redisServer中的pubsub_patterns，找出包含该频道的模式的结点，将消息发送给订阅了该模式的客户端。

####  1.3 使用场景

- 哨兵模式

  ​	在Redis哨兵模式中，哨兵通过发布与订阅的方式与Redis主服务器和Redis从服务器进行通信。

- Redisson框架使用

  ​	Redisson是一个分布式锁框架，在Redisson分布式锁释放的时候，是使用发布与订阅的方式通知的.

###  二、Redis事务

​	Redis的事务是通过multi、exec、discard和watch这四个命令来完成的。 

​	Redis的单个命令都是原子性的，所以这里需要确保事务性的对象是命令集合。 

​	Redis将命令集合序列化并确保处于同一事务的命令集合连续且不被打断的执行。

​	注意，Redis不支持回滚操作。

####  2.1 事务命令

- multi：用于标记事务块的开始,Redis会将后续的命令逐个放入队列中，然后使用exec原子化地执行这个命令队列 
- exec：执行命令队列 
- discard：清除命令队列 
- watch：监视key 
- unwatch：清除监视key

![](/Users/lixin/Documents/知识库/图片\redis\Snipaste_2021-12-14_11-12-30.png)

```shell
127.0.0.1:6379> multi
OK
127.0.0.1:6379> set s1 222
QUEUED
127.0.0.1:6379> hset set1 name zhangfei
QUEUED
127.0.0.1:6379> exec
1) OK
2) (integer) 1
127.0.0.1:6379> multi
OK
127.0.0.1:6379> set s2 333
QUEUED
127.0.0.1:6379> hset set2 age 23
QUEUED
127.0.0.1:6379> discard
OK
127.0.0.1:6379> exec
(error) ERR EXEC without MULTI
127.0.0.1:6379> watch s1
OK
127.0.0.1:6379> multi
OK
127.0.0.1:6379> set s1 555
QUEUED
127.0.0.1:6379> exec # 此时在没有exec之前，通过另一个命令窗口对监控的s1字段进行修改
(nil)
127.0.0.1:6379> get s1
222
127.0.0.1:6379> unwatch
OK
```

####  2.2 事务机制

#####  2.2.1 事务的执行

1. 事务开始 在RedisClient中，有属性flags，用来表示是否在事务中 flags=REDIS_MULTI 
2. 命令入队 RedisClient将命令存放在事务队列中 （EXEC,DISCARD,WATCH,MULTI除外） 
3. 事务队列 multiCmd *commands 用于存放命令 
4. 执行事务 RedisClient向服务器端发送exec命令，RedisServer会遍历事务队列,执行队列中的命令,最后将执行的结果一次性返回给客户端。

如果某条命令在入队过程中发生错误，redisClient将flags置为REDIS_DIRTY_EXEC，EXEC命令将会失败返回。

```c
typedef struct redisClient{
    // flags
    int flags //状态
    // 事务状态
    multiState mstate;
    // .....
}redisClient;

// 事务状态
typedef struct multiState{
    // 事务队列,FIFO顺序
    // 是一个数组,先入队的命令在前,后入队在后
    multiCmd *commands;
    // 已入队命令数
    int count;
}multiState;

// 事务队列
typedef struct multiCmd{
    // 参数
    robj **argv;
    // 参数数量
    int argc;
    // 命令指针
    struct redisCommand *cmd;
}multiCmd
```

##### 2.2.2 Watch的执行

- 使用WATCH命令监视数据库键

  ​	redisDb有一个watched_keys字典,key是某个被监视的数据的key,值是一个链表.记录了所有监视这个数据的客户端。

- 监视机制的触发

  ​	当修改数据后，监视这个数据的客户端的flags置为REDIS_DIRTY_CAS 

- 事务执行

  ​	RedisClient向服务器端发送exec命令，服务器判断RedisClient的flags，如果为REDIS_DIRTY_CAS，则清空事务队列。

```c
typedef struct redisDb{
// .....
// 正在被WATCH命令监视的键
dict *watched_keys;
// .....
}redisDb;
```

####  2.3 Redis的弱事务性

- Redis语法错误

  整个事务的命令在队列里都清楚

  ```shell
  127.0.0.1:6379> multi
  OK
  127.0.0.1:6379> sets m1 44
  (error) ERR unknown command `sets`, with args beginning with: `m1`, `44`,
  127.0.0.1:6379> set m2 55
  QUEUED
  127.0.0.1:6379> exec
  (error) EXECABORT Transaction discarded because of previous errors.
  127.0.0.1:6379> get m1
  "22
  ```

  flags=multi_dirty 

- Redis运行错误

  在队列里正确的命令可以执行 （弱事务性） 

  弱事务性 ： 

  1、在队列里正确的命令可以执行 （非原子操作） 

  2、不支持回滚

  ```shell
  127.0.0.1:6379> multi
  OK
  127.0.0.1:6379> set m1 55
  QUEUED
  127.0.0.1:6379> lpush m1 1 2 3 #不能是语法错误
  QUEUED
  127.0.0.1:6379> exec
  1) OK
  2) (error) WRONGTYPE Operation against a key holding the wrong kind of value
  127.0.0.1:6379> get m1
  "55"
  ```

- Redis不支持事务回滚

  1、大多数事务失败是因为语法错误或者类型错误，这两种错误，在开发阶段都是可以预见的 

  2、Redis为了性能方面就忽略了事务回滚。 （回滚记录历史版本）

###  三、Lua脚本

​	lua是一种轻量小巧的脚本语言，用标准C语言编写并以源代码形式开放， 其设计目的是为了嵌入应用 程序中，从而为应用程序提供灵活的扩展和定制功能。 

​	Lua应用场景：游戏开发、独立应用脚本、Web应用脚本、扩展和数据库插件。 

​	OpenRestry：一个可伸缩的基于Nginx的Web平台，是在nginx之上集成了lua模块的第三方服务器 OpenResty是一个通过Lua扩展Nginx实现的可伸缩的Web平台，内部集成了大量精良的Lua库、第三方 模块以及大多数的依赖项。 用于方便地搭建能够处理超高并发（日活千万级别）、扩展性极高的动态Web应用、Web服务和动态网关。 功能和nginx类似，就是由于支持lua动态脚本，所以更加灵活，可以实现鉴权、限流、分流、日志记 录、灰度发布等功能。 

​	OpenResty通过Lua脚本扩展nginx功能，可提供负载均衡、请求路由、安全认证、服务鉴权、流量控 制与日志监控等服务。 类似的还有Kong（Api Gateway）、tengine（阿里）。

####  3.1 创建并修改lua环境

- 下载

  地址：http://www.lua.org/download.html

  可以本地下载上传到linux，也可以使用curl命令在linux系统中进行在线下载

  ```shell
  curl -R -O http://www.lua.org/ftp/lua-5.3.5.tar.gz
  ```

- 安装

  ```shell
  yum -y install readline-devel ncurses-devel
  tar -zxvf lua-5.3.5.tar.gz
  #在src目录下
  make linux
  #或make install
  ```

  如果报错，说找不到readline/readline.h, 可以通过yum命令安装

  ```shell
  yum -y install readline-devel ncurses-devel
  ```

  安装完以后

  ```shell
  make linux / make install
  ```

  最后，直接输入lua命令即可进入lua的控制台

####  3.2 Lua环境协作组件

​	从Redis2.6.0版本开始，通过内置的lua编译/解释器，可以使用EVAL命令对lua脚本进行求值。 脚本的命令是原子的，RedisServer在执行脚本命令中，不允许插入新的命令 脚本的命令可以复制，RedisServer在获得脚本后不执行，生成标识返回，Client根据标识就可以随时执行。

#####  3.2.1 EVAL命令

通过执行redis的eval命令，可以运行一段lua脚本

```shell
EVAL script numkeys key [key ...] arg [arg ...]
```

- 命令说明：
  - script参数：是一段Lua脚本程序，它会被运行在Redis服务器上下文中，这段脚本不必(也不应该) 定义为一个Lua函数。 	

  - numkeys参数：用于指定键名参数的个数。 

  - key [key ...]参数： 从EVAL的第三个参数开始算起，使用了numkeys个键（key），表示在脚本中所用到的那些Redis键(key)，这些键名参数可以在Lua中通过全局变量KEYS数组，用1为基址的形式访问( KEYS[1] ， KEYS[2] ，以此类推)。 

  - arg [arg ...]参数：可以在Lua中通过全局变量ARGV数组访问，访问的形式和KEYS变量类似( ARGV[1] 、 ARGV[2] ，诸如此类)。

    ```shell
    eval "return {KEYS[1],KEYS[2],ARGV[1],ARGV[2]}" 2 key1 key2 first second
    ```

- **lua脚本中调用Redis命令**

  - redis.call()： 

    - 返回值就是redis命令执行的返回值 
    - 如果出错，则返回错误信息，不继续执行 

  - redis.pcall()： 

    - 返回值就是redis命令执行的返回值 
    - 如果出错，则记录错误信息，继续执行 

  - 注意事项 

    - 在脚本中，使用return语句将返回值返回给客户端，如果没有return，则返回nil

      ```shell
      eval "return redis.call('set',KEYS[1],ARGV[1])" 1 n1 zhaoyun
      ```

#####  3.2.2 EVALSHA命令

​	EVAL 命令要求你在每次执行脚本的时候都发送一次脚本主体(script body)。 Redis 有一个内部的缓存机制，因此它不会每次都重新编译脚本，不过在很多场合，付出无谓的带宽来传送脚本主体并不是最佳选择。 为了减少带宽的消耗， Redis 实现了 EVALSHA 命令，它的作用和 EVAL一样，都用于对脚本求值，但它接受的第一个参数不是脚本，而是脚本的SHA1 校验和(sum)

#####  3.2.3 SCRIPT命令

- SCRIPT FLUSH ：清除所有脚本缓存 

- SCRIPT EXISTS ：根据给定的脚本校验和，检查指定的脚本是否存在于脚本缓存 

- SCRIPT LOAD ：将一个脚本装入脚本缓存，**返回SHA1摘要**，但并不立即运行它

  ```shell
  192.168.24.131:6380> script load "return redis.call('set',KEYS[1],ARGV[1])"
  "c686f316aaf1eb01d5a4de1b0b63cd233010e63d"
  192.168.24.131:6380> evalsha c686f316aaf1eb01d5a4de1b0b63cd233010e63d 1 n2
  zhangfei
  OK
  192.168.24.131:6380> get n2
  ```

- SCRIPT KILL ：杀死当前正在运行的脚本

####  3.3 脚本管理命令实现

使用redis-cli直接执行lua脚本。

test.lua

```shell
return redis.call('set',KEYS[1],ARGV[1])
./redis-cli -h 127.0.0.1 -p 6379 --eval test.lua name:6 , 'caocao' #，两边有空格
```

list.lua

```shell
local key=KEYS[1]
local list=redis.call("lrange",key,0,-1);
return list;
./redis-cli --eval list.lua list
```

利用Redis整合Lua，主要是为了性能以及事务的原子性。因为redis帮我们提供的事务功能太差。

####  3.4 脚本复制

​	Redis 传播 Lua 脚本，在使用主从模式和开启AOF持久化的前提下： 

​	当执行lua脚本时，Redis 服务器有两种模式：脚本传播模式和命令传播模式。

#####  3.4.1 脚本传播模式

​	脚本传播模式是 Redis 复制脚本时默认使用的模式 

​	Redis会将被执行的脚本及其参数复制到 AOF 文件以及从服务器里面。

​	执行以下命令：

```shell
eval "redis.call('set',KEYS[1],ARGV[1]);redis.call('set',KEYS[2],ARGV[2])" 2 n1
n2
zhaoyun1 zhaoyun2
```

​	那么主服务器将向从服务器发送完全相同的 eval 命令：

```shell
eval "redis.call('set',KEYS[1],ARGV[1]);redis.call('set',KEYS[2],ARGV[2])" 2 n1
n2
zhaoyun1 zhaoyun2
```

**注意**：在这一模式下执行的脚本不能有时间、内部状态、随机函数(spop)等。执行相同的脚本以及参数 必须产生相同的效果。在Redis5，也是处于同一个事务中。

#####  3.4.2 命令传播模式

​	处于命令传播模式的主服务器会将执行脚本产生的所有写命令用事务包裹起来，然后将事务复制到 AOF 文件以及从服务器里面。 

​	因为命令传播模式复制的是写命令而不是脚本本身，所以即使脚本本身包含时间、内部状态、随机函数 等，主服务器给所有从服务器复制的写命令仍然是相同的。 

​	为了开启命令传播模式，用户在使用脚本执行任何写操作之前，需要先在脚本里面调用以下函数：

```shell
redis.replicate_commands()
```

​	redis.replicate_commands() 只对调用该函数的脚本有效：在使用命令传播模式执行完当前脚本之后， 服务器将自动切换回默认的脚本传播模式。 

如果我们在主服务器执行以下命令：

```shell
eval
"redis.replicate_commands();redis.call('set',KEYS[1],ARGV[1]);redis.call('set',K
EYS[2],ARGV[2])" 2 n1 n2 zhaoyun11 zhaoyun22
```

那么主服务器将向从服务器复制以下命令：

```shell
EXEC
*1
$5
MULTI
*3
$3
set
$2
n1
$9
zhaoyun11
*3
$3
set
$2
n2
$9
zhaoyun22
*1
$4
EXEC
```

####  3.5 管道（pipeline）,事务和脚本(lua)三者的区别

​	三者都可以批量执行命令 。

​	管道无原子性，命令都是独立的，属于无状态的操作 。

​	事务和脚本是有原子性的，其区别在于脚本可借助Lua语言可在服务器端存储的便利性定制和简化操作 。

​	脚本的原子性要强于事务，脚本执行期间，另外的客户端其它任何脚本或者命令都无法执行，脚本的执行时间应该尽量短，不能太耗时的脚本。

###  四、慢查询日志

​	我们都知道MySQL有慢查询日志，Redis也有慢查询日志，可用于监视和优化查询

####  4.1 慢查询设置

在redis.conf中可以配置和慢查询日志相关的选项：

```shell
#执行时间超过多少微秒的命令请求会被记录到日志上 0 :全记录 <0 不记录
slowlog-log-slower-than 10000
#slowlog-max-len 存储慢查询日志条数
slowlog-max-len 128
```

Redis使用列表存储慢查询日志，采用队列方式（FIFO）

config set的方式可以临时设置，redis重启后就无效 

config set slowlog-log-slower-than 微秒 

config set slowlog-max-len 条数

查看日志：slowlog get [n]

```shell
127.0.0.1:6379> config set slowlog-log-slower-than 0
OK
127.0.0.1:6379> config set slowlog-max-len 2
OK
127.0.0.1:6379> set name:001 zhaoyun
OK
127.0.0.1:6379> set name:002 zhangfei
OK
127.0.0.1:6379> get name:002
"zhangfei"

127.0.0.1:6379> slowlog get
1)	1) (integer) 7 #日志的唯一标识符(uid)
    2) (integer) 1589774302 #命令执行时的UNIX时间戳
    3) (integer) 65 #命令执行的时长(微秒)
    4)	1) "get" #执行命令及参数
    	2) "name:002"
    5) "127.0.0.1:37277"
    6) ""
2)	1) (integer) 6
    2) (integer) 1589774281
    3) (integer) 7
    4)	1) "set"
    	2) "name:002"
    3) "zhangfei"
    5) "127.0.0.1:37277"
    6) ""
# set和get都记录，第一条被移除了。
```

####  4.2 慢查询记录的保存

在redisServer中保存和慢查询日志相关的信息

```c
struct redisServer {
    // ...
    // 下一条慢查询日志的 ID
    long long slowlog_entry_id;
    // 保存了所有慢查询日志的链表 FIFO
    list *slowlog;
    // 服务器配置 slowlog-log-slower-than 选项的值
    long long slowlog_log_slower_than;
    // 服务器配置 slowlog-max-len 选项的值
    unsigned long slowlog_max_len;
    // ...
};
```

lowlog 链表保存了服务器中的所有慢查询日志， 链表中的每个节点都保存了一个 slowlogEntry 结 构， 每个 slowlogEntry 结构代表一条慢查询日志。

```c
typedef struct slowlogEntry {
    // 唯一标识符
    long long id;
    // 命令执行时的时间，格式为 UNIX 时间戳
    time_t time;
    // 执行命令消耗的时间，以微秒为单位
    long long duration;
    // 命令与命令参数
    robj **argv;
    // 命令与命令参数的数量
    int argc;
} slowlogEntry;
```

####  4.3 慢查询日志的阅览&删除

初始化日志列表

```c
void slowlogInit(void) {
    server.slowlog = listCreate(); /* 创建一个list列表 */
    server.slowlog_entry_id = 0; /* 日志ID从0开始 */
    listSetFreeMethod(server.slowlog,slowlogFreeEntry); /* 指定慢查询日志list空间的释放方法 */
}
```

获得慢查询日志记录 

slowlog get [n]

```c
def SLOWLOG_GET(number=None):
    # 用户没有给定 number 参数
    # 那么打印服务器包含的全部慢查询日志
    if number is None:
    	number = SLOWLOG_LEN()
            
    # 遍历服务器中的慢查询日志
    for log in redisServer.slowlog:
        if number <= 0:
            # 打印的日志数量已经足够，跳出循环
            break
        else:
            # 继续打印，将计数器的值减一
            number -= 1
        # 打印日志
        printLog(log)
```

查看日志数量的 slowlog len

```c
def SLOWLOG_LEN():
    # slowlog 链表的长度就是慢查询日志的条目数量
    return len(redisServer.slowlog)
```

清除日志 slowlog reset

```c
def SLOWLOG_RESET():
    # 遍历服务器中的所有慢查询日志
    for log in redisServer.slowlog:
        # 删除日志
        deleteLog(log)
```

####  4.4 添加日志实现

​	在每次执行命令的之前和之后， 程序都会记录微秒格式的当前 UNIX 时间戳， 这两个时间戳之间的差 就是服务器执行命令所耗费的时长， 服务器会将这个时长作为参数之一传给 slowlogPushEntryIfNeeded 函数， 而 slowlogPushEntryIfNeeded 函数则负责检查是否需要为这 次执行的命令创建慢查询日志

```c
// 记录执行命令前的时间
before = unixtime_now_in_us()
//执行命令
execute_command(argv, argc, client)
//记录执行命令后的时间
after = unixtime_now_in_us()
// 检查是否需要创建新的慢查询日志
slowlogPushEntryIfNeeded(argv, argc, before-after)
    
void slowlogPushEntryIfNeeded(robj **argv, int argc, long long duration) {
    if (server.slowlog_log_slower_than < 0) return; /* Slowlog disabled */ /* 负数表示禁用 */
    if (duration >= server.slowlog_log_slower_than) /* 如果执行时间 > 指定阈值*/
    	listAddNodeHead(server.slowlog,slowlogCreateEntry(argv,argc,duration));
    /* 创建一个slowlogEntry对象,添加到列表首部*/
    while (listLength(server.slowlog) > server.slowlog_max_len) /* 如果列表长度 >  指定长度 */
    	listDelNode(server.slowlog,listLast(server.slowlog)); /* 移除列表尾部元素 */
}
```

slowlogPushEntryIfNeeded 函数的作用有两个：

1. 检查命令的执行时长是否超过 slowlog-log-slower-than 选项所设置的时间， 如果是的话， 就 为命令创建一个新的日志， 并将新日志添加到 slowlog 链表的表头。 
2. 检查慢查询日志的长度是否超过 slowlog-max-len 选项所设置的长度， 如果是的话， 那么将多出来的日志从 slowlog 链表中删除掉。

####  4.5 慢查询定位&处理

使用slowlog get 可以获得执行较慢的redis命令，针对该命令可以进行优化： 

1、尽量使用短的key，对于value有些也可精简，能使用int就int。 

2、避免使用keys *、hgetall等全量操作。 

3、减少大key的存取，打散为小key 

4、将rdb改为aof模式 ，rdb fork 子进程时主进程会阻塞导致redis性能大幅下降 

5、关闭持久化 ， （适合于数据量较小） 改aof 命令式  

6、想要一次添加多条数据的时候可以使用管道 

7、尽可能地使用哈希存储 

8、尽量限制下redis使用的内存大小，这样可以避免redis使用swap分区或者出现OOM错误，内存与硬盘的swap

###  五、监视器

​	Redis客户端通过执行MONITOR命令可以将自己变为一个监视器，实时地接受并打印出服务器当前处理的命令请求的相关信息。 此时，当其他客户端向服务器发送一条命令请求时，服务器除了会处理这条命令请求之外，还会将这条命令请求的信息发送给所有监视器。

![](/Users/lixin/Documents/知识库/图片\redis\Snipaste_2021-12-14_17-32-27.png)

Redis客户端1

```shell
127.0.0.1:6379> monitor
OK
1589706136.030138 [0 127.0.0.1:42907] "COMMAND"
1589706145.763523 [0 127.0.0.1:42907] "set" "name:10" "zhaoyun"
1589706163.756312 [0 127.0.0.1:42907] "get" "name:10"
```

Redis客户端2

```shell
127.0.0.1:6379>
127.0.0.1:6379> set name:10 zhaoyun
OK
127.0.0.1:6379> get name:10
"zhaoyun"
```

####  5.1 监视器实现

redisServer 维护一个 monitors 的链表，记录自己的监视器，每次收到 MONITOR 命令之后，将客户端追加到链表尾。

```c
void monitorCommand(redisClient *c) {
    /* ignore MONITOR if already slave or in monitor mode */
    if (c->flags & REDIS_SLAVE) return;
        c->flags |= (REDIS_SLAVE|REDIS_MONITOR);
        listAddNodeTail(server.monitors,c);
        addReply(c,shared.ok); //回复OK
}
```

利用call函数实现向监视器发送命令

```c
// call() 函数是执行命令的核心函数，这里只看监视器部分
/*src/redis.c/call*/
/* Call() is the core of Redis execution of a command */
void call(redisClient *c, int flags) {
    long long dirty, start = ustime(), duration;
    int client_old_flags = c->flags;
    /* Sent the command to clients in MONITOR mode, only if the commands are
    * not generated from reading an AOF. */
    if (listLength(server.monitors) &&
    !server.loading &&
    !(c->cmd->flags & REDIS_CMD_SKIP_MONITOR))
    {
    	replicationFeedMonitors(c,server.monitors,c->db->id,c->argv,c->argc);
    }
......
}
```

call 主要调用了 replicationFeedMonitors ，这个函数的作用就是将命令打包为协议，发送给监视器。

####  5.2 Redis监控平台

​	grafana、prometheus以及redis_exporter。 Grafana 是一个开箱即用的可视化工具，具有功能齐全的度量仪表盘和图形编辑器，有灵活丰富的图形 化选项，可以混合多种风格，支持多个数据源特点。 Prometheus是一个开源的服务监控系统，它通过HTTP协议从远程的机器收集数据并存储在本地的时序 数据库上。 redis_exporter为Prometheus提供了redis指标的导出，配合Prometheus以及grafana进行可视化及监控。

![](/Users/lixin/Documents/知识库/图片\redis\Snipaste_2021-12-14_17-37-31.png)

##  第六部分 Redis高可用方案

###  一、Redis主从复制

​	Redis支持主从复制功能，可以通过执行slaveof（Redis5以后改成replicaof）或者在配置文件中设置 slaveof(Redis5以后改成replicaof)来开启复制功能。

![](/Users/lixin/Documents/知识库/图片\redis\Snipaste_2021-12-14_17-41-11.png)

![](/Users/lixin/Documents/知识库/图片\redis\Snipaste_2021-12-14_17-41-35.png)

![](/Users/lixin/Documents/知识库/图片\redis\Snipaste_2021-12-14_17-42-07.png)

- 主对外从对内，主可写从不可写 
- 主挂了，从不可为主

####  1.1 主从配置

- 主Redis配置

  无需特殊配置

- 从Redis配置

  修改从服务器上的 redis.conf 文件：

  ```shell
  # slaveof <masterip> <masterport>
  # 表示当前【从服务器】对应的【主服务器】的IP是192.168.10.135，端口是6379。
  replicaof 127.0.0.1 6379
  ```

  

