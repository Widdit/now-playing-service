package com.widdit.nowplaying.service.kugou;

import com.widdit.nowplaying.entity.Track;

final class KuGouSong {
    private final Track track;
    private final String fileHash;

    KuGouSong(Track track, String fileHash) {
        this.track = track;
        this.fileHash = fileHash;
    }

    Track getTrack() {
        return track;
    }

    String getFileHash() {
        return fileHash;
    }
}
