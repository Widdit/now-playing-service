package com.widdit.nowplaying.service;

import com.widdit.nowplaying.controller.WebSocketLyricController;
import com.widdit.nowplaying.entity.WebSocketMessage;
import com.widdit.nowplaying.event.LyricChangedEvent;
import com.widdit.nowplaying.event.PlayerPauseStateChangedEvent;
import com.widdit.nowplaying.event.PlayerProgressReplayEvent;
import com.widdit.nowplaying.event.PlayerProgressSyncEvent;
import com.widdit.nowplaying.event.TrackChangedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import javax.websocket.Session;

@Service
@Slf4j
public class WebSocketService {

    @Autowired
    private NowPlayingService nowPlayingService;
    @Autowired
    private LyricService lyricService;

    /**
     * 监听歌曲改变事件
     */
    @EventListener
    public void handleTrackChanged(TrackChangedEvent event) {
        // 发送Track数据
        WebSocketMessage trackMessage = new WebSocketMessage("Track", nowPlayingService.queryTrack());
        WebSocketLyricController.sendToAllClients(trackMessage);

        // 发送PlayerProgress数据
        WebSocketMessage progressMessage = new WebSocketMessage("PlayerProgress", nowPlayingService.queryProgress());
        WebSocketLyricController.sendToAllClients(progressMessage);
    }

    /**
     * 监听歌词改变事件
     */
    @EventListener
    public void handleLyricChanged(LyricChangedEvent event) {
        // 发送Lyric数据
        WebSocketMessage lyricMessage = new WebSocketMessage("Lyric", lyricService.getLyric());
        WebSocketLyricController.sendToAllClients(lyricMessage);

        // 发送PlayerProgress数据
        WebSocketMessage progressMessage = new WebSocketMessage("PlayerProgress", nowPlayingService.queryProgress());
        WebSocketLyricController.sendToAllClients(progressMessage);
    }

    /**
     * 监听播放器暂停状态改变事件
     */
    @EventListener
    public void handlePlayerPauseStateChanged(PlayerPauseStateChangedEvent event) {
        WebSocketMessage message = new WebSocketMessage("PlayerPauseState", nowPlayingService.queryPlayer());
        WebSocketLyricController.sendToAllClients(message);
    }

    /**
     * 监听播放器重新播放事件
     */
    @EventListener
    public void handlePlayerProgressReplay(PlayerProgressReplayEvent event) {
        WebSocketMessage message = new WebSocketMessage("PlayerProgressReplay", nowPlayingService.queryProgress());
        WebSocketLyricController.sendToAllClients(message);
    }

    /**
     * 监听进度同步事件（kg 平台每秒同步 C# 端的实际进度到前端）
     */
    @EventListener
    public void handlePlayerProgressSync(PlayerProgressSyncEvent event) {
        WebSocketMessage message = new WebSocketMessage("PlayerProgress", nowPlayingService.queryProgress());
        WebSocketLyricController.sendToAllClients(message);
    }

    /**
     * 向指定 Session 发送初始数据
     */
    public void sendInitialDataToSession(Session session) {
        // 发送Track数据
        WebSocketLyricController.sendToSession(session, new WebSocketMessage("Track", nowPlayingService.queryTrack()));

        // 发送Lyric数据
        WebSocketLyricController.sendToSession(session, new WebSocketMessage("Lyric", lyricService.getLyric()));

        // 发送PlayerPauseState数据
        WebSocketLyricController.sendToSession(session, new WebSocketMessage("PlayerPauseState", nowPlayingService.queryPlayer()));

        // 发送PlayerProgress数据
        WebSocketLyricController.sendToSession(session, new WebSocketMessage("PlayerProgress", nowPlayingService.queryProgress()));
    }

    /**
     * 更新歌词获取状态
     */
    public void updateLyricFetchState(int connectionCount) {
        // 仅当有 WebSocket 连接时，才需要更新歌词
        lyricService.setFetchLyricEnabled(connectionCount > 0);
    }

}
