package com.lagou.edu.filter;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 方法和每个时间窗口的对应关系
 */
public class MethodIpPercent {

    private final int calculateTimeWindow;
    private final int maxTimes;

    /**
     * 记录下当前时间窗格下所有的执行情况信息
     */
    private final MethodTopPercentileSecond[] topPercentileSeconds;

    /**
     * 创建一个新的时间窗格监控工具
     *
     * @param calculateTimeWindow 需要监控最近多少秒的数据
     * @return
     */
    public static MethodIpPercent create(int calculateTimeWindow, int times) {
        // 创建实例
        return new MethodIpPercent(calculateTimeWindow, times);
    }

    private MethodIpPercent(int calculateTimeWindow, int times) {
        this.calculateTimeWindow = calculateTimeWindow;
        topPercentileSeconds = new MethodTopPercentileSecond[calculateTimeWindow];
        maxTimes = times;
        for (int i = 0; i < calculateTimeWindow; i++) {
            topPercentileSeconds[i] = new MethodTopPercentileSecond();
        }
    }


    /**
     * 增加使用情况
     *
     * @param methodName
     * @param useTime
     */
    public void increment(String methodName, long useTime) {
        final long currentTimeMillis = System.currentTimeMillis();

        // 获取当前时间所使用的时间
        final int idx = (int) (currentTimeMillis / 1000 % calculateTimeWindow);

        // 进行递增数据
        topPercentileSeconds[idx].increment(methodName, useTime, currentTimeMillis);
    }

    public boolean isLimited(String methodName) {
        AtomicLong result = new AtomicLong();
        Arrays.stream(topPercentileSeconds).forEach(second -> {
            long count = second.count();
            result.addAndGet(count);
        });
        System.out.println("methodName:" + methodName + " count:" + result.get());
        return result.get() >= maxTimes;
    }

    /**
     * 每秒中的数据信息
     */
    private static class MethodTopPercentileSecond {

        /**
         * 当前秒下面的执行情况
         */
        public volatile AtomicLong atomicLong = new AtomicLong();

        /**
         * 最后一次执行递增的时间信息
         */
        private volatile long lastIncrementTime = -1;

        public void increment(String invokeMethod, long useTime, long currentMillis) {
            if (lastIncrementTime > 0 && currentMillis - lastIncrementTime > 1000) {
                synchronized (this) {
                    // 二次检查保证只会有一次生效
                    if (currentMillis - lastIncrementTime > 1000) {
                        atomicLong.set(0);
                        lastIncrementTime = currentMillis;
                    }
                }
            }
            atomicLong.incrementAndGet();

            lastIncrementTime = currentMillis;
        }

        public long count() {

            final long currentTimeMillis = System.currentTimeMillis() - 10000; // 10s钟内请求10次，可以调整成变量
            if (lastIncrementTime > currentTimeMillis) {
                return atomicLong.get();
            }
            return 0L;
        }
    }
}
