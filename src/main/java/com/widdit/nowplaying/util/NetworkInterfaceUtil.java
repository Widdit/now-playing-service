package com.widdit.nowplaying.util;

import com.widdit.nowplaying.entity.NetworkInterfaceInfo;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 网络工具类
 * 用于获取本机局域网网络接口列表，并按 "真实局域网 IP" 的可能性从高到低排序
 */
@Slf4j
public class NetworkInterfaceUtil {

    // ==================== 评分常量定义 ====================

    /** 有默认网关 - 加分很高 */
    private static final int SCORE_HAS_GATEWAY = 100;

    /** WiFi/无线网络 - 加分高 */
    private static final int SCORE_WIFI = 50;

    /** 私有 IP 范围 - 加分中高 */
    private static final int SCORE_PRIVATE_IP = 40;

    /** 以太网 - 加分中 */
    private static final int SCORE_ETHERNET = 30;

    /** 知名网卡厂商 - 加分低 */
    private static final int SCORE_KNOWN_VENDOR = 15;

    /** 支持多播 - 加分低 */
    private static final int SCORE_MULTICAST = 10;

    /** 有 MAC 地址 - 加分最低 */
    private static final int SCORE_HAS_MAC = 5;

    /** VPN/虚拟机 - 扣分很高 */
    private static final int PENALTY_VIRTUAL = -100;

    /** 隧道/代理 - 扣分高 */
    private static final int PENALTY_TUNNEL = -80;

    /** 虚拟网卡 MAC 地址 - 扣分中高 */
    private static final int PENALTY_VIRTUAL_MAC = -70;

    /** 蓝牙网络 - 扣分中高 */
    private static final int PENALTY_BLUETOOTH = -60;

    /** PPP/拨号 - 扣分中 */
    private static final int PENALTY_PPP = -50;

    /** 微软虚拟网络 - 扣分中 */
    private static final int PENALTY_MS_VIRTUAL = -40;

    /** 链路本地地址 - 扣分高 */
    private static final int PENALTY_LINK_LOCAL = -90;

    /** 回环地址 - 扣分最高 */
    private static final int PENALTY_LOOPBACK = -150;

    /**
     * 获取局域网网络接口列表
     * 按照 "真实局域网 IP" 的可能性从高到低排序
     *
     * @return 网络接口信息列表
     */
    public static List<NetworkInterfaceInfo> getNetworkInterfaceList() {
        List<ScoredInterface> scoredList = new ArrayList<>();

        // 预先获取有默认网关的 IP 地址集合
        Set<String> gatewayIps = getInterfacesWithGateway();

        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces == null) {
                log.warn("无法获取网络接口列表");
                return Collections.emptyList();
            }

            while (interfaces.hasMoreElements()) {
                NetworkInterface nif = interfaces.nextElement();

                // 排除不可用/回环/虚拟网卡
                if (!nif.isUp() || nif.isLoopback() || nif.isVirtual()) {
                    continue;
                }

                // 遍历该网络接口的所有地址
                Enumeration<InetAddress> addresses = nif.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();

                    // 仅处理 IPv4 地址
                    if (addr instanceof Inet4Address) {
                        String ip = addr.getHostAddress();
                        String displayName = nif.getDisplayName();
                        String name = nif.getName();

                        // 计算评分
                        int score = calculateScore(nif, ip, displayName, name, gatewayIps);

                        scoredList.add(new ScoredInterface(displayName, ip, score));
                    }
                }
            }

        } catch (SocketException e) {
            log.error("获取网络接口时发生异常", e);
            return Collections.emptyList();
        }

        // 按分数从高到低排序
        scoredList.sort((a, b) -> Integer.compare(b.score, a.score));

        // 转换为 NetworkInterfaceInfo 列表
        List<NetworkInterfaceInfo> result = new ArrayList<>();
        for (ScoredInterface scored : scoredList) {
            result.add(NetworkInterfaceInfo.builder()
                    .name(scored.name)
                    .ipAddress(scored.ip)
                    .build());
        }

        return result;
    }

    /**
     * 获取最可能的局域网 IP 地址
     *
     * @return 最可能的局域网 IP，如果没有则返回 null
     */
    public static String getMostLikelyLanIp() {
        List<NetworkInterfaceInfo> list = getNetworkInterfaceList();
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0).getIpAddress();
    }

    /**
     * 计算网络接口的评分
     */
    private static int calculateScore(NetworkInterface nif, String ip,
                                      String displayName, String name,
                                      Set<String> gatewayIps) {
        int score = 0;
        String displayNameLower = displayName.toLowerCase();
        String nameLower = name.toLowerCase();
        String combined = displayNameLower + " " + nameLower;

        // ==================== 加分项 ====================

        // 1. 有默认网关 - 这是最强的"真实局域网"特征
        if (gatewayIps.contains(ip)) {
            score += SCORE_HAS_GATEWAY;
        }

        // 2. WiFi/无线网络 - 常见的局域网连接方式
        if (containsAny(combined, "wlan", "wi-fi", "wifi", "wireless", "无线", "wlp")) {
            score += SCORE_WIFI;
        }

        // 3. 私有 IP 范围 - 局域网的必要条件
        if (isPrivateIp(ip)) {
            score += SCORE_PRIVATE_IP;
        }

        // 4. 以太网 - 常见的局域网连接方式
        if (containsAny(combined, "ethernet", "以太网", "eth", "enp", "eno", "lan")) {
            score += SCORE_ETHERNET;
        }

        // 5. 知名网卡厂商 - 真实物理网卡的标志
        if (containsAny(displayNameLower,
                "intel", "realtek", "broadcom", "qualcomm", "atheros",
                "killer", "mediatek", "marvell", "nvidia", "aquantia",
                "mellanox", "tp-link", "d-link", "netgear", "asus")) {
            score += SCORE_KNOWN_VENDOR;
        }

        // 6. 支持多播 - 正常局域网网卡的特征
        try {
            if (nif.supportsMulticast()) {
                score += SCORE_MULTICAST;
            }
        } catch (SocketException ignored) {}

        // 7. 有 MAC 地址 - 物理网卡的标志
        try {
            byte[] mac = nif.getHardwareAddress();
            if (mac != null && mac.length >= 6) {
                score += SCORE_HAS_MAC;
            }
        } catch (SocketException ignored) {}

        // ==================== 扣分项 ====================

        // 8. VPN/虚拟机/容器等虚拟网络
        if (containsAny(combined,
                "vpn", "clash", "v2ray", "shadowsocks", "ssr", "trojan",
                "vmware", "vmnet", "virtual", "virtualbox", "vbox",
                "docker", "hyper-v", "vethernet", "wsl", "podman",
                "container", "lxc", "qemu", "kvm", "xen", "parallels")) {
            score += PENALTY_VIRTUAL;
        }

        // 9. 隧道/代理
        if (containsAny(combined,
                "tunnel", "tap", "tun", "pptp", "l2tp", "ipsec",
                "wireguard", "openvpn", "softether", "zerotier", "tailscale",
                "hamachi", "radmin", "ngrok", "frp", "nps")) {
            score += PENALTY_TUNNEL;
        }

        // 10. 游戏加速器
        if (containsAny(combined,
                "netch", "sstap", "proxifier", "sockscap", "tun2socks",
                "游戏加速", "加速器", "uu加速", "雷神", "迅游", "奇游",
                "网易uu", "腾讯加速", "biubiu")) {
            score += PENALTY_TUNNEL;
        }

        // 11. 蓝牙网络
        if (containsAny(combined, "bluetooth", "蓝牙", "pan network")) {
            score += PENALTY_BLUETOOTH;
        }

        // 12. PPP/拨号连接
        if (containsAny(combined, "ppp", "dialup", "modem", "拨号", "adsl")) {
            score += PENALTY_PPP;
        }

        // 13. 微软虚拟网络
        if (containsAny(combined,
                "microsoft wi-fi direct", "microsoft hosted", "mobile hotspot",
                "teredo", "isatap", "6to4", "microsoft kernel debug")) {
            score += PENALTY_MS_VIRTUAL;
        }

        // 14. 检查虚拟网卡的 MAC 地址特征
        try {
            byte[] mac = nif.getHardwareAddress();
            if (isVirtualMac(mac)) {
                score += PENALTY_VIRTUAL_MAC;
            }
        } catch (SocketException ignored) {}

        // 15. 链路本地地址 (169.254.x.x) - APIPA，表示DHCP失败
        if (isLinkLocalIp(ip)) {
            score += PENALTY_LINK_LOCAL;
        }

        // 16. 回环地址
        if (containsAny(combined, "loopback", "localhost") || ip.equals("127.0.0.1")) {
            score += PENALTY_LOOPBACK;
        }

        return score;
    }

    /**
     * 获取有默认网关的网络接口 IP 集合
     * 通过执行 Windows 命令 route print 来获取
     */
    private static Set<String> getInterfacesWithGateway() {
        Set<String> result = new HashSet<>();

        try {
            ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "route", "print", "-4");
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), "GBK"))) {

                String line;
                boolean inRouteTable = false;

                // 路由表格式示例:
                // 网络目标        网络掩码          网关       接口   跃点数
                // 0.0.0.0          0.0.0.0      192.168.1.1    192.168.1.100     35
                Pattern defaultRoutePattern = Pattern.compile(
                        "^\\s*0\\.0\\.0\\.0\\s+0\\.0\\.0\\.0\\s+" +
                                "(\\d+\\.\\d+\\.\\d+\\.\\d+)\\s+" +
                                "(\\d+\\.\\d+\\.\\d+\\.\\d+)\\s+\\d+");

                while ((line = reader.readLine()) != null) {
                    // 查找 IPv4 路由表的开始
                    if (line.contains("IPv4") && line.contains("路由表") ||
                            line.contains("IPv4") && line.contains("Route Table")) {
                        inRouteTable = true;
                        continue;
                    }

                    if (inRouteTable) {
                        Matcher matcher = defaultRoutePattern.matcher(line);
                        if (matcher.find()) {
                            String gateway = matcher.group(1);
                            String localIp = matcher.group(2);

                            // 确保网关不是 0.0.0.0（表示没有网关）
                            if (!gateway.equals("0.0.0.0")) {
                                result.add(localIp);
                            }
                        }
                    }
                }
            }

            process.waitFor();

        } catch (Exception e) {
            log.warn("通过 route 命令获取网关信息失败，尝试备用方案", e);
            // 备用方案：使用 netsh 命令
            result.addAll(getGatewaysByNetsh());
        }

        return result;
    }

    /**
     * 备用方案：通过 netsh 命令获取有网关的接口
     */
    private static Set<String> getGatewaysByNetsh() {
        Set<String> result = new HashSet<>();

        try {
            ProcessBuilder pb = new ProcessBuilder("cmd", "/c",
                    "netsh", "interface", "ipv4", "show", "config");
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), "GBK"))) {

                String line;
                String currentIp = null;

                while ((line = reader.readLine()) != null) {
                    // 匹配IP地址行
                    if (line.contains("IP 地址") || line.contains("IP Address")) {
                        Pattern ipPattern = Pattern.compile("(\\d+\\.\\d+\\.\\d+\\.\\d+)");
                        Matcher matcher = ipPattern.matcher(line);
                        if (matcher.find()) {
                            currentIp = matcher.group(1);
                        }
                    }

                    // 匹配默认网关行
                    if ((line.contains("默认网关") || line.contains("Default Gateway"))
                            && currentIp != null) {
                        Pattern gwPattern = Pattern.compile("(\\d+\\.\\d+\\.\\d+\\.\\d+)");
                        Matcher matcher = gwPattern.matcher(line);
                        if (matcher.find()) {
                            String gateway = matcher.group(1);
                            if (!gateway.equals("0.0.0.0")) {
                                result.add(currentIp);
                            }
                        }
                    }
                }
            }

            process.waitFor();

        } catch (Exception e) {
            log.error("通过 netsh 命令获取网关信息也失败", e);
        }

        return result;
    }

    /**
     * 检查是否为私有 IP 地址
     */
    private static boolean isPrivateIp(String ip) {
        try {
            String[] parts = ip.split("\\.");
            if (parts.length != 4) {
                return false;
            }

            int first = Integer.parseInt(parts[0]);
            int second = Integer.parseInt(parts[1]);

            // A类：10.0.0.0 – 10.255.255.255
            if (first == 10) {
                return true;
            }

            // B类：172.16.0.0 – 172.31.255.255
            if (first == 172 && second >= 16 && second <= 31) {
                return true;
            }

            // C类：192.168.0.0 – 192.168.255.255
            if (first == 192 && second == 168) {
                return true;
            }

            return false;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 检查是否为链路本地地址 (169.254.x.x)
     */
    private static boolean isLinkLocalIp(String ip) {
        return ip != null && ip.startsWith("169.254.");
    }

    /**
     * 检查 MAC 地址是否为虚拟网卡特征
     */
    private static boolean isVirtualMac(byte[] mac) {
        if (mac == null || mac.length < 3) {
            return false;
        }

        int b0 = mac[0] & 0xFF;
        int b1 = mac[1] & 0xFF;
        int b2 = mac[2] & 0xFF;

        // VMware: 00:0C:29, 00:50:56, 00:05:69
        if ((b0 == 0x00 && b1 == 0x0C && b2 == 0x29) ||
                (b0 == 0x00 && b1 == 0x50 && b2 == 0x56) ||
                (b0 == 0x00 && b1 == 0x05 && b2 == 0x69)) {
            return true;
        }

        // VirtualBox: 08:00:27
        if (b0 == 0x08 && b1 == 0x00 && b2 == 0x27) {
            return true;
        }

        // Hyper-V: 00:15:5D
        if (b0 == 0x00 && b1 == 0x15 && b2 == 0x5D) {
            return true;
        }

        // Parallels: 00:1C:42
        if (b0 == 0x00 && b1 == 0x1C && b2 == 0x42) {
            return true;
        }

        // QEMU/KVM: 52:54:00
        if (b0 == 0x52 && b1 == 0x54 && b2 == 0x00) {
            return true;
        }

        // Xen: 00:16:3E
        if (b0 == 0x00 && b1 == 0x16 && b2 == 0x3E) {
            return true;
        }

        // Docker: 02:42:xx (本地管理地址)
        if (b0 == 0x02 && b1 == 0x42) {
            return true;
        }

        return false;
    }

    /**
     * 检查字符串是否包含任意一个关键词（忽略大小写）
     */
    private static boolean containsAny(String str, String... keywords) {
        if (str == null) {
            return false;
        }
        for (String keyword : keywords) {
            if (str.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 内部类：带评分的网络接口
     */
    private static class ScoredInterface {
        final String name;
        final String ip;
        final int score;

        ScoredInterface(String name, String ip, int score) {
            this.name = name;
            this.ip = ip;
            this.score = score;
        }
    }
}