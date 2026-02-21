package com.widdit.nowplaying.controller;

import com.alibaba.fastjson.JSON;
import com.widdit.nowplaying.entity.WebSocketMessage;
import com.widdit.nowplaying.service.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.net.SocketException;
import java.nio.channels.ClosedChannelException;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint("/api/ws/lyric")
@Component
@Slf4j
public class WebSocketLyricController {

    // 存储所有连接的 Session
    private static final CopyOnWriteArraySet<Session> nowPlayingSessions = new CopyOnWriteArraySet<>();

    // Spring 上下文，用于获取 Service
    private static ApplicationContext applicationContext;

    public static void setApplicationContext(ApplicationContext context) {
        applicationContext = context;
    }

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session) {
        nowPlayingSessions.add(session);
        log.info("歌词 WebSocket 连接建立，当前连接数：{}", nowPlayingSessions.size());

        // 更新歌词获取状态
        updateFetchLyricEnabled();

        // 发送初始数据
        if (applicationContext != null) {
            WebSocketService webSocketService = applicationContext.getBean(WebSocketService.class);
            webSocketService.sendInitialDataToSession(session);
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        boolean removed = nowPlayingSessions.remove(session);
        log.info("歌词 WebSocket 连接关闭（{}），当前连接数：{}",
                closeReason != null ? closeReason.getCloseCode() : "unknown",
                nowPlayingSessions.size());

        // 避免 onClose/onError 都触发时重复更新
        if (removed) {
            updateFetchLyricEnabled();
        }
    }

    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error) {
        // 页面关闭/网络抖动等导致的“正常断开”，不当成错误打印。真正需要关注的异常才打 error，并把堆栈打出来便于排查
        if (!isIgnorableDisconnect(error)) {
            log.error("歌词 WebSocket 发生错误：{}", safeMsg(error), error);
        }

        boolean removed = (session != null) && nowPlayingSessions.remove(session);
        if (removed) {
            updateFetchLyricEnabled();
        }
    }

    /**
     * 收到客户端消息后调用的方法
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        // 暂不处理客户端消息
    }

    /**
     * 向所有连接的客户端发送消息
     */
    public static void sendToAllClients(WebSocketMessage message) {
        String jsonMessage = JSON.toJSONString(message);
        for (Session session : nowPlayingSessions) {
            try {
                if (session.isOpen()) {
                    session.getBasicRemote().sendText(jsonMessage);
                }
            } catch (IOException e) {
                log.warn("发送 WebSocket 消息失败：{}", e.getMessage());
            }
        }
    }

    /**
     * 向指定 Session 发送消息
     */
    public static void sendToSession(Session session, WebSocketMessage message) {
        try {
            if (session != null && session.isOpen()) {
                String jsonMessage = JSON.toJSONString(message);
                session.getBasicRemote().sendText(jsonMessage);
            }
        } catch (IOException e) {
            log.warn("发送 WebSocket 消息失败：{}", e.getMessage());
        }
    }

    /**
     * 获取当前连接数
     */
    public static int getConnectionCount() {
        return nowPlayingSessions.size();
    }

    /**
     * 更新歌词获取状态
     */
    private void updateFetchLyricEnabled() {
        if (applicationContext != null) {
            WebSocketService webSocketService = applicationContext.getBean(WebSocketService.class);
            webSocketService.updateLyricFetchState(nowPlayingSessions.size());
        }
    }

    /**
     * 判断是否属于 "客户端关闭/连接正常断开" 等可忽略异常
     */
    private boolean isIgnorableDisconnect(Throwable t) {
        if (t == null) return true;

        // 常见：关闭时 message 为 null
        if (t.getMessage() == null) return true;

        // 各种实现/网络层可能抛出的典型断开异常
        if (t instanceof EOFException) return true;
        if (t instanceof ClosedChannelException) return true;
        if (t instanceof SocketException) return true; // Broken pipe / Connection reset 等

        // 递归检查根因
        return isIgnorableDisconnect(t.getCause());
    }

    private String safeMsg(Throwable t) {
        if (t == null) return "null";
        return t.getMessage() != null ? t.getMessage() : t.getClass().getSimpleName();
    }

    // 兼容 EOFException 引用
    private static class EOFException extends IOException {
        private static final long serialVersionUID = 1L;
    }

}
