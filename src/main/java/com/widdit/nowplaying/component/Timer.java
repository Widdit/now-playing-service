package com.widdit.nowplaying.component;

/**
 * 毫秒计时器
 * <p>基于 {@link System#nanoTime()} 实现的毫秒级计时器，支持启动、暂停、获取、设置时间。</p>
 * <p>所有公共方法均为线程安全。</p>
 */
public class Timer {

    /** 累计基准时间（包含所有暂停前的时间），单位：毫秒 */
    private long baseTime;

    /** 当前计时周期的开始时间戳，单位：毫秒（单调时钟） */
    private long startTime;

    /** 计时器运行状态标志 */
    private boolean running;

    /**
     * 构造函数，创建计时器实例（初始状态为停止，时间为 0）
     */
    public Timer() {
        this.baseTime = 0L;
        this.startTime = 0L;
        this.running = false;
    }

    /**
     * 获取当前单调时钟的毫秒值
     * <p>使用 {@link System#nanoTime()} 而非 {@link System#currentTimeMillis()}，
     * 保证单调递增，不受系统时钟回拨影响。</p>
     *
     * @return 单调时钟毫秒值
     */
    private static long nowMillis() {
        return System.nanoTime() / 1_000_000L;
    }

    /**
     * 启动 / 恢复计时器
     * <p>如果计时器未运行，则开始新的计时周期或继续暂停的计时。</p>
     *
     * <pre>{@code
     * timer.start(); // 开始计时
     * }</pre>
     */
    public synchronized void start() {
        if (!running) {
            startTime = nowMillis();
            running = true;
        }
    }

    /**
     * 获取时间
     * <p>返回精确到毫秒的累计时间，运行时动态计算时间差。</p>
     *
     * @return 毫秒为单位的累计时间
     *
     * <pre>{@code
     * long elapsed = timer.getTime(); // 获取已过时间
     * }</pre>
     */
    public synchronized long getTime() {
        return running
                ? baseTime + (nowMillis() - startTime)
                : baseTime;
    }

    /**
     * 暂停计时器
     * <p>暂停计时并冻结当前时间，后续可通过 {@link #start()} 恢复计时。</p>
     *
     * <pre>{@code
     * timer.pause(); // 暂停计时
     * }</pre>
     */
    public synchronized void pause() {
        if (running) {
            baseTime += nowMillis() - startTime;
            running = false;
        }
    }

    /**
     * 设置时间
     * <p>直接修改计时器基准时间，运行时会重置计时起点以保持时间连续性。</p>
     *
     * @param time 要设置的毫秒时间值
     *
     * <pre>{@code
     * timer.setTime(1000L); // 设置为 1 秒
     * }</pre>
     */
    public synchronized void setTime(long time) {
        baseTime = time;
        if (running) {
            startTime = nowMillis();
        }
    }

    /**
     * 重置计时器
     * <p>将时间归零并强制停止计时。</p>
     *
     * <pre>{@code
     * timer.reset(); // 完全重置计时器
     * }</pre>
     */
    public synchronized void reset() {
        baseTime = 0L;
        startTime = 0L;
        running = false;
    }

    /**
     * 获取计时器当前是否正在运行
     *
     * @return 如果正在运行返回 {@code true}
     */
    public synchronized boolean isRunning() {
        return running;
    }

}