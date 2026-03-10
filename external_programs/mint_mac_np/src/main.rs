use serde::{Deserialize, Serialize};
use std::env;
use std::fs;
use std::io::Write;
use std::path::{Path, PathBuf};
use std::process::Command;

const SWIFT_HELPER: &str = r#"
import Foundation
import Dispatch
import Darwin

typealias MRGetNowPlayingInfoFn = @convention(c) (DispatchQueue, @escaping (CFDictionary?) -> Void) -> Void
typealias MRGetNowPlayingAppPidFn = @convention(c) (DispatchQueue, @escaping (Int32) -> Void) -> Void

let path = "/System/Library/PrivateFrameworks/MediaRemote.framework/MediaRemote"
guard let handle = dlopen(path, RTLD_NOW) else {
    print("{\"ok\":false,\"error\":\"dlopen_failed\"}")
    exit(2)
}

guard let infoSym = dlsym(handle, "MRMediaRemoteGetNowPlayingInfo") else {
    print("{\"ok\":false,\"error\":\"symbol_info_not_found\"}")
    exit(3)
}

guard let pidSym = dlsym(handle, "MRMediaRemoteGetNowPlayingApplicationPID") else {
    print("{\"ok\":false,\"error\":\"symbol_pid_not_found\"}")
    exit(4)
}

let getInfo = unsafeBitCast(infoSym, to: MRGetNowPlayingInfoFn.self)
let getPid = unsafeBitCast(pidSym, to: MRGetNowPlayingAppPidFn.self)

let group = DispatchGroup()
let q = DispatchQueue.global(qos: .userInitiated)

var pid: Int32 = -1
var infoDict: [String: Any] = [:]

group.enter()
getPid(q) { p in
    pid = p
    group.leave()
}

group.enter()
getInfo(q) { dict in
    if let d = dict as? [String: Any] {
        infoDict = d
    }
    group.leave()
}

let timeout = DispatchTime.now() + .seconds(2)
if group.wait(timeout: timeout) == .timedOut {
    print("{\"ok\":false,\"error\":\"timeout\"}")
    exit(1)
}

func str(_ keys: [String]) -> String {
    for k in keys {
        if let v = infoDict[k] as? String, !v.isEmpty { return v }
    }
    return ""
}

func num(_ keys: [String]) -> Double {
    for k in keys {
        if let v = infoDict[k] as? NSNumber { return v.doubleValue }
        if let v = infoDict[k] as? Double { return v }
        if let v = infoDict[k] as? Int { return Double(v) }
    }
    return 0
}

let title = str(["kMRMediaRemoteNowPlayingInfoTitle", "Title"])
let artist = str(["kMRMediaRemoteNowPlayingInfoArtist", "Artist"])
let album = str(["kMRMediaRemoteNowPlayingInfoAlbum", "Album"])
let duration = num(["kMRMediaRemoteNowPlayingInfoDuration", "Duration"])
let elapsed = num(["kMRMediaRemoteNowPlayingInfoElapsedTime", "ElapsedTime"])
let rate = num(["kMRMediaRemoteNowPlayingInfoPlaybackRate", "PlaybackRate"])

let payload: [String: Any] = [
    "ok": true,
    "pid": pid,
    "title": title,
    "artist": artist,
    "album": album,
    "duration": duration,
    "elapsed": elapsed,
    "playbackRate": rate
]

let data = try JSONSerialization.data(withJSONObject: payload, options: [])
print(String(data: data, encoding: .utf8)!)
"#;

#[derive(Debug, Deserialize)]
struct HelperPayload {
    ok: bool,
    pid: Option<i32>,
    title: Option<String>,
    artist: Option<String>,
    album: Option<String>,
    duration: Option<f64>,
    elapsed: Option<f64>,
    #[serde(rename = "playbackRate")]
    playback_rate: Option<f64>,
    error: Option<String>,
}

#[derive(Debug, Serialize)]
struct OutputPayload {
    ok: bool,
    provider: String,
    app: String,
    pid: i32,
    process_path: String,
    title: String,
    artist: String,
    album: String,
    duration_ms: i64,
    position_ms: i64,
    is_playing: bool,
    error: String,
}

fn temp_bin_path() -> PathBuf {
    let base = env::temp_dir().join("mint_mac_np");
    let _ = fs::create_dir_all(&base);
    base.join("mr_probe.swift")
}

fn ensure_swift_helper() -> Result<PathBuf, String> {
    let swift = temp_bin_path();
    let mut file = fs::File::create(&swift).map_err(|e| format!("write swift: {e}"))?;
    file.write_all(SWIFT_HELPER.as_bytes())
        .map_err(|e| format!("write swift source: {e}"))?;
    Ok(swift)
}

fn process_path_by_pid(pid: i32) -> String {
    if pid <= 0 {
        return String::new();
    }
    let out = Command::new("/bin/ps")
        .arg("-p")
        .arg(pid.to_string())
        .arg("-o")
        .arg("comm=")
        .output();
    match out {
        Ok(out) if out.status.success() => String::from_utf8_lossy(&out.stdout).trim().to_string(),
        _ => String::new(),
    }
}

fn provider_from_path(path: &str) -> String {
    let p = path.to_lowercase();
    if p.contains("neteasemusic") || p.contains("网易云音乐") {
        "netease".to_string()
    } else if p.contains("music.app") {
        "apple_music".to_string()
    } else if p.contains("spotify") {
        "spotify".to_string()
    } else {
        "unknown".to_string()
    }
}

fn app_from_path(path: &str) -> String {
    if path.is_empty() {
        return String::new();
    }
    Path::new(path)
        .file_name()
        .map(|s| s.to_string_lossy().to_string())
        .unwrap_or_default()
}

fn err_output(error: &str, provider: &str) -> String {
    serde_json::to_string(&OutputPayload {
        ok: false,
        provider: provider.to_string(),
        app: String::new(),
        pid: -1,
        process_path: String::new(),
        title: String::new(),
        artist: String::new(),
        album: String::new(),
        duration_ms: 0,
        position_ms: 0,
        is_playing: false,
        error: error.to_string(),
    })
    .unwrap_or_else(|_| "{\"ok\":false,\"error\":\"serialize_failed\"}".to_string())
}

fn main() {
    let mut provider_filter = String::new();
    let mut args = env::args().skip(1);
    while let Some(arg) = args.next() {
        if arg == "--provider" {
            if let Some(v) = args.next() {
                provider_filter = v;
            }
        }
    }

    let helper_script = match ensure_swift_helper() {
        Ok(p) => p,
        Err(e) => {
            println!("{}", err_output(&e, &provider_filter));
            return;
        }
    };

    let out = match Command::new("xcrun").arg("swift").arg(&helper_script).output() {
        Ok(v) => v,
        Err(e) => {
            println!("{}", err_output(&format!("spawn helper: {e}"), &provider_filter));
            return;
        }
    };

    if !out.status.success() {
        let err = String::from_utf8_lossy(&out.stderr).trim().to_string();
        println!(
            "{}",
            err_output(
                if err.is_empty() { "helper failed" } else { &err },
                &provider_filter
            )
        );
        return;
    }

    let raw = String::from_utf8_lossy(&out.stdout).trim().to_string();
    let parsed: HelperPayload = match serde_json::from_str(&raw) {
        Ok(v) => v,
        Err(e) => {
            println!("{}", err_output(&format!("parse helper json: {e}"), &provider_filter));
            return;
        }
    };

    if !parsed.ok {
        println!(
            "{}",
            err_output(
                parsed.error.as_deref().unwrap_or("helper_not_ok"),
                &provider_filter
            )
        );
        return;
    }

    let pid = parsed.pid.unwrap_or(-1);
    let process_path = process_path_by_pid(pid);
    let provider = provider_from_path(&process_path);
    if !provider_filter.is_empty() && provider != provider_filter {
        println!("{}", err_output("provider_mismatch", &provider_filter));
        return;
    }

    let duration_ms = (parsed.duration.unwrap_or(0.0) * 1000.0).round() as i64;
    let position_ms = (parsed.elapsed.unwrap_or(0.0) * 1000.0).round() as i64;
    let is_playing = parsed.playback_rate.unwrap_or(0.0) > 0.0;

    let output = OutputPayload {
        ok: true,
        provider,
        app: app_from_path(&process_path),
        pid,
        process_path,
        title: parsed.title.unwrap_or_default(),
        artist: parsed.artist.unwrap_or_default(),
        album: parsed.album.unwrap_or_default(),
        duration_ms,
        position_ms,
        is_playing,
        error: String::new(),
    };

    println!(
        "{}",
        serde_json::to_string(&output).unwrap_or_else(|_| err_output("serialize_failed", ""))
    );
}
