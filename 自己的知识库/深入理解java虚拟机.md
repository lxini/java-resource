#  《深入理解Java虚拟机》

[toc]

案例工程下载地址：

- Github：[GitHub - fenixsoft/jvm_book: 《深入理解Java虚拟机（第3版）》样例代码&勘误](https://github.com/fenixsoft/jvm_book)
- Gitee：[jvm_book: 《深入理解Java虚拟机》案例实战工程 (gitee.com)](https://gitee.com/mgua/jvm_book)

##  第 一 部分 走进Java

###  第 1 章 走进Java

- Java技术体系
- Java的发展历程
- JVM的发展历程
- Java技术展望

##  第 二 部分 自动内存管理

###  第 2 章 Java内存区域与内存溢出

####  2.1 运行时内存区域

<img src="/Users/lixin/Documents/知识库/图片/JVM/JVMRuntimeMemory.svg" alt="jvm运行时数据区域" style="zoom:200%;" />

#####  2.1.1 程序计数器

程序计数器（Program Counter Register）是一块较小的内存，用来指明**下一条**需要执行的字节码指令地址。它是程序控制流的指示器，分支、跳转、循环、异常处理、线程恢复等基础功能都需要依赖它来完成。

- 它是线程独享的，每个线程会有自己的程序计数器，程序计数器的生命周期与线程相同。
- 当执行的是Java方法时，程序计数器指向下一条字节码指令；当执行的是native方法时，程序计数器的值为空（Undefined）。
- 程序计数器是唯一一个在《Java虚拟机规范》中，没有规定任何OutOfMemoryError情况的区域。

#####  2.1.2  Java虚拟机栈

Java虚拟机栈（Java Virtual Machine Stack）是描述Java方法执行的**线程内存模型**，每个方法被执行的时候，JVM都会同步创建一个栈帧，用来存储局部变量表、操作数栈、动态链接、方法出口等信息。每一个方法从被调用直至执行完毕的过程，就对应着一个栈帧在Java虚拟机栈中入栈到出栈的过程。

- 它是线程独享的，Java虚拟机栈的生命周期与线程相同。
- 局部变量表中的存储空间以局部变量槽（Slot）来表示，64位的long和double类型的数据会占用两个变量槽，其余类型只占用一个。
- Java虚拟机栈有两类异常：StackOverFlowError异常、OutOfMemoryError异常。

#####  2.1.3 本地方法栈

本地方法栈（Native Method Stack）和Java虚拟机栈功能类似，区别在于虚拟机栈为虚拟机执行Java程序服务，本地方法栈为虚拟机使用本地（Native）方法服务。

- 它是线程独享的，Java虚拟机栈的生命周期与线程相同。
- 局部变量表中的存储空间以局部变量槽（Slot）来表示，64位的long和double类型的数据会占用两个变量槽，其余类型只占用一个。
- Java虚拟机栈有两类异常：StackOverFlowError异常、OutOfMemoryError异常。

#####  2.1.4 Java堆

Java堆（Java Heap）是虚拟机中最大的一部分内存区域，用来存放几乎所有的对象实例。

- 它是线程共享的，所有线程只要持有对象的引用，都可以访问堆空间的对象。
- 所谓堆空间的分代（年轻代、老年代等），只是主流虚拟机为了更好的管理堆空间，以分代理论来进一步细分堆空间，实现垃圾收集器，《Java虚拟机规范》并没有对堆空间布局进行细分，而且最前沿的垃圾收集器（ZGC，Shenandoah）也已经出现不以分代理论实现的垃圾收集器了，所以说分代理论并不是堆空间的固定内存布局。
- 堆空间可以固定，也可以扩展，不过主流的虚拟机都是支持扩展的，通过Xmx（最大）和Xms（最小）参数来控制。
- 当堆空间无法再提供内存分配新的实例，也无法扩展，会抛出OutOfMemoryError异常。

#####  2.1.5 方法区

方法区（Method Area）用来存放类型信息、常量、静态变量、即时编译器编译过的代码缓存等数据。

- 它与堆一样，是线程共享的，逻辑上属于堆，但别名非堆，为了和堆区分开来。
- 方法区不等于永久代，这种说法是因为HotSpot虚拟机将垃圾收集器的分代理论扩展到了方法区，其他一些虚拟机实现（JRocket、J9等），是不存在永久代的概念的。
- 永久代弊大于利，这种设计导致了Java程序更容易发生OOM（有-XX:MaxPermSize的上限），HotSpot团队在JDK6决定放弃永久代，逐步使用本地内存来实现方法区。
- JDK7 时Hotspt团队已经把永久代的字符串常量池、静态变量等移至堆中了，JDK8完全放弃了永久代，采用本地内存实现的元空间来代替，将JDK7中永久代还剩余的类型信息全部移到了元空间中。
- 方法区无法满足内存分配需求时，会抛出OutOfMemoryError异常。

#####  2.1.6 运行时常量池

运行时常量池（Runtime Constant Pool）是方法区的一部分，在类加载后，会将Class文件中常量池部分的字面量、符号引用以及一部分符号引用翻译过后的直接引用放入运行时常量池中。

- 运行时常量池是动态的，并不是只有写入Class文件常量池的常量才能被放进常量池，在运行过程中，也可以动态的往里面放入常量，例如常用的String.intern()方法。
- 作为方法区的一部分，受方法区约束，当常量池无法再申请到内存时，会抛出OutOfMemoryError异常。

#####  2.1.7 直接内存

直接内存（Direct Memory）不是虚拟机规定及管理的内存区域，不过再JDK 1.4新增的NIO可以用过Native方法直接分配堆外内存，然后通过堆空间的DirectByteBuffer对象作为这块空间的引用操作该内存空间，受本机实际内存的影响，它同样会抛出OutOfMemoryError异常。

####  2.2 HotSpot虚拟机对象探秘

#####  2.2.1 对象的创建

1. 对象创建的字节码指令是new，带参数。
2. 遇到new指令时，虚拟机会先去检查指令参数是否能在常量池中定位到一个类的引用符号，并检查这个符号引用代表的类是否已经被加载、解析、初始化过，如果没有，则先执行类加载过程。
3. 类加载检查通过之后，会给对象分配内存。
   - 对象所需内存大小，在类加载完成后便可完全确定。
   - 内存分配方式：
     - 指针碰撞：内存块整齐划一，以中间指针为分割，左边是已使用内存块，右边为空闲内存块，分配内存就将指针往右移动与对象大小相等的距离。为保证分配内存修改指针时的并发安全：
       1. 采用CAS配上失败重试的方式保证更新的原子性。
       2. 采用本地线程分配缓冲（Thread Local Allocation Buffer，TLAB）来事先给线程分配好一小块内存用于对象的创建，缓冲区用完时，才需要同步锁定。可通过-XX:+/-UseTLAB参数控制。
     - 空闲列表：内存块不规整，已使用的和空闲的内存块相互交叉，以一个列表记录空闲的内存地址空间，分配内存时从列表上找一块足够大的内存块分给对象。
4. 虚拟机将分配给对象的内存空间（不包括对象头）都初始化为零值。
5. 设置对象头：
   - 对象对应的类信息
   - 对象的HashCode
   - 对象的GC分代年龄
6. 调用构造函数，即Class文件中的<init>()方法。

#####  2.2.2 对象的内存布局

- 对象头（Header）：
  - 运行时数据（Mark Word）：哈希码、GC分代年龄、锁状态标志、线程持有的锁、偏向锁ID、偏向时间戳等，在32位和64位的虚拟机中分别为32bit和64bit。
  
  |               存储内容               | 标志位 |        状态        |
  | :----------------------------------: | :----: | :----------------: |
  |       对象哈希码、对象分代年龄       |   01   |       未锁定       |
  |           指向锁记录的指针           |   00   |     轻量级锁定     |
  |          指向重量级锁的指针          |   10   | 膨胀（重量级锁定） |
  |          空、不需要记录信息          |   11   |       GC标记       |
  | 偏向线程ID、偏向时间戳、对象分代年龄 |   01   |       可偏向       |
  
  - 类型指针：指向对象类型元数据的指针。
  - 数组长度：对象为数组时，会在对象头记录数组长度。
  
- 实例数据（Instance Data）：对象真正存储的有效信息。

- 对其填充（Padding）：用于填充字节，保证对象整体大小位8字节的整数倍。

#####  2.2.3 对象的访问

- 句柄访问对象：Java堆中会划分出一块内存作为句柄池，每个reference就会存储对象的句柄地址，句柄包含了实例数据与类型数据给自具体的地址信息，多一次寻址，但垃圾回收移动对象时，只用修改句柄地址。
- 直接指针访问（HotSpot的实现方式）：reference中直接存储对象的具体地址，类型信息地址放在对象头，访问速度快。

####  2.3 内存溢出（OOM）的实战

###  第 3 章 垃圾收集器与内存分配

###  第 4 章 虚拟机性能监控、故障处理工具

###  第 5 章 调优案例分析与实战



## 第 三 部分 虚拟机执行子系统

###  第 6 章 类文件结构

###  第 7 章 虚拟机类加载机制

###  第 8 章 虚拟机字节码执行引擎



###  第 9 章 类加载及执行子系统的案例与实战



##  第 四 部分 程序编译与代码优化

###  第 10 章 前端编译与优化

###  第 11 章 后端编译与优化



##  第 五 部分 高效并发

###  第 12 章 Java内存模型与线程

###  第 13 章 线程安全与锁优化



