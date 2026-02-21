package com.widdit.nowplaying.util;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public final class NetworkScannerUtil {

    private NetworkScannerUtil() {}

    /**
     * 扫描给定局域网IP（本机某网卡IPv4）所在网段内的可达IP地址（包含本机）。
     * 返回List中第一个元素固定为本机IP。
     *
     * @param localIp 本机某网卡的IPv4地址，例如 "192.168.1.10"
     * @return 可达IP列表，本机IP排在第一个
     */
    public static List<String> scanLanDevices(String localIp) {
        return scanLanDevices(localIp, 800, Math.max(32, Runtime.getRuntime().availableProcessors() * 8));
    }

    /**
     * 可自定义超时与并发度的扫描方法。
     *
     * @param localIp   本机某网卡IPv4地址
     * @param timeoutMs ping超时（毫秒），例如 500~1500
     * @param threads   扫描线程数，例如 32~256（视网段大小与机器性能）
     */
    public static List<String> scanLanDevices(String localIp, int timeoutMs, int threads) {
        Objects.requireNonNull(localIp, "localIp");
        if (!isValidIpv4(localIp)) {
            throw new IllegalArgumentException("Invalid IPv4: " + localIp);
        }
        if (timeoutMs <= 0) {
            throw new IllegalArgumentException("timeoutMs must be > 0");
        }
        if (threads <= 0) {
            throw new IllegalArgumentException("threads must be > 0");
        }

        // 1) 获取该IP对应网卡的子网前缀长度；找不到则默认 /24（常见局域网）
        int prefixLen = findPrefixLengthForLocalIp(localIp);
        if (prefixLen < 0) prefixLen = 24;

        // 2) 计算网段范围（网络地址/广播地址），并生成要扫描的主机地址范围
        long ip = ipv4ToLong(localIp);
        long mask = prefixLen == 0 ? 0L : (0xFFFFFFFFL << (32 - prefixLen)) & 0xFFFFFFFFL;
        long network = ip & mask;
        long broadcast = network | (~mask & 0xFFFFFFFFL);

        // 可用主机范围通常为 (network+1) ~ (broadcast-1)
        long start = network + 1;
        long end = broadcast - 1;

        // /32 或极小网段处理：至少保证把本机放入结果
        if (prefixLen >= 31) {
            // /31和/32在很多环境下比较特殊，这里直接只返回本机（需求只要求包含本机）
            return Collections.singletonList(localIp);
        }
        if (start > end) {
            return Collections.singletonList(localIp);
        }

        // 3) 并发扫描：ping 可达则加入结果
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CompletionService<String> cs = new ExecutorCompletionService<>(pool);

        int taskCount = 0;
        for (long cur = start; cur <= end; cur++) {
            String targetIp = longToIpv4(cur);
            if (targetIp.equals(localIp)) {
                continue; // 本机不需要ping，后面固定放在第一个
            }
            taskCount++;
            cs.submit(() -> pingReachableWindows(targetIp, timeoutMs) ? targetIp : null);
        }

        List<String> reachable = new ArrayList<>();
        try {
            for (int i = 0; i < taskCount; i++) {
                Future<String> f = cs.take();
                String ok = f.get();
                if (ok != null) reachable.add(ok);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            // 被打断时，仍然保证返回本机
        } catch (ExecutionException ignored) {
            // 单个任务异常忽略，继续汇总
        } finally {
            pool.shutdownNow();
        }

        // 4) 排序（按IP数值升序），并保证本机IP第一个
        reachable = reachable.stream()
                .distinct()
                .sorted(Comparator.comparingLong(NetworkScannerUtil::ipv4ToLong))
                .collect(Collectors.toList());

        List<String> result = new ArrayList<>(reachable.size() + 1);
        result.add(localIp);
        for (String ipStr : reachable) {
            if (!ipStr.equals(localIp)) result.add(ipStr);
        }
        return result;
    }

    // ------------------------- helpers -------------------------

    private static boolean pingReachableWindows(String ip, int timeoutMs) {
        // Windows: ping -n 1 -w <timeoutMs> <ip>
        ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "ping -n 1 -w " + timeoutMs + " " + ip);
        pb.redirectErrorStream(true);
        try {
            Process p = pb.start();
            int code = p.waitFor();
            return code == 0;
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * 找到 localIp 所属网卡的前缀长度（CIDR），如 24 表示 /24。
     * 找不到则返回 -1。
     */
    private static int findPrefixLengthForLocalIp(String localIp) {
        try {
            InetAddress target = InetAddress.getByName(localIp);
            Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
            if (nis == null) return -1;

            while (nis.hasMoreElements()) {
                NetworkInterface ni = nis.nextElement();
                // 可按需过滤：ni.isUp(), !ni.isLoopback(), !ni.isVirtual() 等
                for (InterfaceAddress ia : ni.getInterfaceAddresses()) {
                    InetAddress addr = ia.getAddress();
                    if (addr instanceof Inet4Address && addr.equals(target)) {
                        return ia.getNetworkPrefixLength();
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return -1;
    }

    private static boolean isValidIpv4(String ip) {
        // 简单校验：四段0-255
        String[] parts = ip.split("\\.");
        if (parts.length != 4) return false;
        for (String p : parts) {
            if (p.isEmpty() || p.length() > 3) return false;
            try {
                int v = Integer.parseInt(p);
                if (v < 0 || v > 255) return false;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    private static long ipv4ToLong(String ip) {
        String[] parts = ip.split("\\.");
        long a = Long.parseLong(parts[0]);
        long b = Long.parseLong(parts[1]);
        long c = Long.parseLong(parts[2]);
        long d = Long.parseLong(parts[3]);
        return ((a << 24) | (b << 16) | (c << 8) | d) & 0xFFFFFFFFL;
    }

    private static String longToIpv4(long v) {
        return ((v >> 24) & 0xFF) + "." +
                ((v >> 16) & 0xFF) + "." +
                ((v >> 8) & 0xFF) + "." +
                (v & 0xFF);
    }
}
