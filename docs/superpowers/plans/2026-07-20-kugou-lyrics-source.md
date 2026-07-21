# Kugou Lyrics Source Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add Kugou as a selectable standard-LRC lyrics source and include it in the existing best-lyrics selection flow.

**Architecture:** Extend the existing Kugou backend service with an injectable HTTP client, an internal search result carrying `FileHash`, and standard LRC retrieval. Generalize `LyricService` from two fixed candidates to three stable candidates, then expose Kugou through a data-driven frontend source list and copy the verified frontend build into the backend's packaged static resources.

**Tech Stack:** Java 11, Spring Boot 2.6, Fastjson, Jsoup, JUnit 5, Mockito, React 19, TypeScript 5.9, Vite 7, Vitest.

---

## File Map

### Backend repository: `work/now-playing-service`

- Create `src/main/java/com/widdit/nowplaying/service/kugou/KuGouHttpClient.java`: network-only GET client.
- Create `src/main/java/com/widdit/nowplaying/service/kugou/KuGouSong.java`: package-private Track plus FileHash value.
- Modify `src/main/java/com/widdit/nowplaying/service/kugou/KuGouMusicService.java`: search refactor, candidate selection, LRC download and validation.
- Modify `src/main/java/com/widdit/nowplaying/service/LyricService.java`: Kugou routing and stable three-source selection.
- Modify `pom.xml`: explicitly enable modern JUnit 5 execution if required.
- Create `src/test/java/com/widdit/nowplaying/service/kugou/KuGouMusicServiceTest.java`: Kugou service behavior.
- Create `src/test/java/com/widdit/nowplaying/service/LyricServiceTest.java`: source routing and three-source selection.
- Replace generated files under `src/main/resources/static/` and `src/main/resources/templates/index.html`: verified frontend production bundle.

### Frontend repository: `work/now-playing-frontend`

- Create `src/constants/lyricSources.ts`: source keys, labels and icons.
- Create `src/constants/lyricSources.test.ts`: source configuration tests.
- Modify `src/pages/SettingsLyric.tsx`: render source tabs from configuration and update help text.
- Modify `package.json` and `package-lock.json`: add Vitest and a test script.

## Task 1: Create a Testable Kugou HTTP Boundary

**Files:**
- Create: `work/now-playing-service/src/main/java/com/widdit/nowplaying/service/kugou/KuGouHttpClient.java`
- Modify: `work/now-playing-service/src/main/java/com/widdit/nowplaying/service/kugou/KuGouMusicService.java`
- Modify: `work/now-playing-service/pom.xml`
- Create: `work/now-playing-service/src/test/java/com/widdit/nowplaying/service/kugou/KuGouMusicServiceTest.java`

- [ ] **Step 1: Write the failing constructor-boundary test**

Create a test that proves the service can receive a mocked network client:

```java
package com.widdit.nowplaying.service.kugou;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class KuGouMusicServiceTest {
    @Test
    void acceptsInjectedHttpClient() {
        KuGouMusicService service = new KuGouMusicService(mock(KuGouHttpClient.class));
        assertNotNull(service);
    }
}
```

- [ ] **Step 2: Run the test and verify RED**

Run:

```powershell
mvn -Dtest=KuGouMusicServiceTest#acceptsInjectedHttpClient test
```

Expected: compilation fails because `KuGouHttpClient` and the constructor do not exist.

- [ ] **Step 3: Add the minimal HTTP client**

Create:

```java
package com.widdit.nowplaying.service.kugou;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;

@Component
public class KuGouHttpClient {
    public String get(String url) throws IOException {
        String host = new URL(url).getHost();
        Connection.Response response = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .header("Accept", "*/*")
                .header("Cache-Control", "no-cache")
                .header("Connection", "keep-alive")
                .header("Host", host)
                .header("Accept-Language", "zh-CN,en-US;q=0.7,en;q=0.3")
                .header("Pragma", "no-cache")
                .ignoreContentType(true)
                .timeout(10000)
                .method(Connection.Method.GET)
                .execute();
        return response.body();
    }
}
```

Add constructor injection to `KuGouMusicService` and replace its private `sendGetRequest` call with `httpClient.get(url)`:

```java
private final KuGouHttpClient httpClient;

public KuGouMusicService(KuGouHttpClient httpClient) {
    this.httpClient = httpClient;
}
```

Remove the old private `sendGetRequest` method and its unused `Connection`, `Jsoup` and `URL` imports.

- [ ] **Step 4: Ensure Maven runs JUnit 5**

Add this plugin beside the existing compiler and Spring Boot plugins:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.2.5</version>
</plugin>
```

- [ ] **Step 5: Run the test and verify GREEN**

Run:

```powershell
mvn -Dtest=KuGouMusicServiceTest#acceptsInjectedHttpClient test
```

Expected: one test passes with zero failures.

- [ ] **Step 6: Commit the boundary**

```powershell
git add pom.xml src/main/java/com/widdit/nowplaying/service/kugou/KuGouHttpClient.java src/main/java/com/widdit/nowplaying/service/kugou/KuGouMusicService.java src/test/java/com/widdit/nowplaying/service/kugou/KuGouMusicServiceTest.java
git commit -m "test: make kugou requests injectable"
```

## Task 2: Preserve Kugou FileHash During Song Matching

**Files:**
- Create: `work/now-playing-service/src/main/java/com/widdit/nowplaying/service/kugou/KuGouSong.java`
- Modify: `work/now-playing-service/src/main/java/com/widdit/nowplaying/service/kugou/KuGouMusicService.java`
- Modify: `work/now-playing-service/src/test/java/com/widdit/nowplaying/service/kugou/KuGouMusicServiceTest.java`

- [ ] **Step 1: Write failing song-selection tests**

Add tests using a fixed search response with an incorrect live version first and the correct studio version second:

```java
private static final String SONG_SEARCH_RESPONSE = "{" +
        "\"error_code\":0,\"data\":{\"lists\":[" +
        "{\"ID\":\"live\",\"SongName\":\"晴天 (Live)\",\"AlbumName\":\"Live\",\"Duration\":299," +
        "\"FileHash\":\"LIVE_HASH\",\"Image\":\"https://img/{size}/live.jpg\",\"Singers\":[{\"name\":\"周杰伦\"}]}," +
        "{\"ID\":\"studio\",\"SongName\":\"晴天\",\"AlbumName\":\"叶惠美\",\"Duration\":269," +
        "\"FileHash\":\"STUDIO_HASH\",\"Image\":\"https://img/{size}/studio.jpg\",\"Singers\":[{\"name\":\"周杰伦\"}]}]}}";

@Test
void searchChoosesBestSongInsteadOfFirstResult() throws Exception {
    KuGouHttpClient client = mock(KuGouHttpClient.class);
    when(client.get(contains("song_search_v2"))).thenReturn(SONG_SEARCH_RESPONSE);
    KuGouMusicService service = new KuGouMusicService(client);

    Track track = service.search("晴天 - 周杰伦");

    assertEquals("studio", track.getId());
    assertEquals("晴天", track.getTitle());
    assertEquals(269, track.getDuration());
}

@Test
void internalSearchKeepsHashForLyricsLookup() throws Exception {
    KuGouHttpClient client = mock(KuGouHttpClient.class);
    when(client.get(contains("song_search_v2"))).thenReturn(SONG_SEARCH_RESPONSE);
    KuGouMusicService service = new KuGouMusicService(client);

    KuGouSong song = service.searchSong("晴天 - 周杰伦");

    assertEquals("STUDIO_HASH", song.getFileHash());
    assertEquals("studio", song.getTrack().getId());
}
```

- [ ] **Step 2: Run the tests and verify RED**

Run:

```powershell
mvn -Dtest=KuGouMusicServiceTest#searchChoosesBestSongInsteadOfFirstResult,KuGouMusicServiceTest#internalSearchKeepsHashForLyricsLookup test
```

Expected: the second test fails to compile because `KuGouSong` and `searchSong` do not exist.

- [ ] **Step 3: Add the internal song value**

Create:

```java
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
```

Change the Kugou cache from `Track prevTrack` to `KuGouSong prevSong`. Move the existing search body to package-private `KuGouSong searchSong(String keyword)` and construct the result with:

```java
KuGouSong result = new KuGouSong(track, bestMatchSong.getString("FileHash"));
```

Keep the public API stable:

```java
public Track search(String keyword) throws IOException {
    return searchSong(keyword).getTrack();
}
```

- [ ] **Step 4: Run the tests and verify GREEN**

Run:

```powershell
mvn -Dtest=KuGouMusicServiceTest test
```

Expected: all Kugou tests pass.

- [ ] **Step 5: Commit the search refactor**

```powershell
git add src/main/java/com/widdit/nowplaying/service/kugou/KuGouSong.java src/main/java/com/widdit/nowplaying/service/kugou/KuGouMusicService.java src/test/java/com/widdit/nowplaying/service/kugou/KuGouMusicServiceTest.java
git commit -m "refactor: retain kugou song hash"
```

## Task 3: Retrieve and Validate Standard Kugou LRC

**Files:**
- Modify: `work/now-playing-service/src/main/java/com/widdit/nowplaying/service/kugou/KuGouMusicService.java`
- Modify: `work/now-playing-service/src/test/java/com/widdit/nowplaying/service/kugou/KuGouMusicServiceTest.java`

- [ ] **Step 1: Write the successful LRC test**

Add fixed candidate and download responses:

```java
private static final String LYRIC_SEARCH_RESPONSE = "{" +
        "\"status\":200,\"candidates\":[" +
        "{\"id\":\"wrong\",\"accesskey\":\"WRONG\",\"singer\":\"其他歌手\",\"song\":\"晴天\",\"duration\":269000}," +
        "{\"id\":\"right\",\"accesskey\":\"RIGHT\",\"singer\":\"周杰伦\",\"song\":\"晴天\",\"duration\":269792}]}";
private static final String LRC = "[00:00.00]晴天 - 周杰伦\n[00:10.20]故事的小黄花";
private static final String LYRIC_DOWNLOAD_RESPONSE = "{" +
        "\"status\":200,\"content\":\"" +
        java.util.Base64.getEncoder().encodeToString(LRC.getBytes(java.nio.charset.StandardCharsets.UTF_8)) +
        "\"}";

@Test
void getLyricDownloadsBestMatchingStandardLrc() throws Exception {
    KuGouHttpClient client = mock(KuGouHttpClient.class);
    when(client.get(contains("song_search_v2"))).thenReturn(SONG_SEARCH_RESPONSE);
    when(client.get(contains("lyrics.kugou.com/search"))).thenReturn(LYRIC_SEARCH_RESPONSE);
    when(client.get(contains("lyrics.kugou.com/download"))).thenReturn(LYRIC_DOWNLOAD_RESPONSE);
    KuGouMusicService service = new KuGouMusicService(client);

    Lyric lyric = service.getLyric("晴天 - 周杰伦");

    assertEquals("kugou", lyric.getSource());
    assertTrue(lyric.getHasLyric());
    assertEquals(LRC, lyric.getLrc());
    assertFalse(lyric.getHasKaraokeLyric());
    assertFalse(lyric.getHasTranslatedLyric());
    verify(client).get(contains("id=right"));
}
```

- [ ] **Step 2: Run the success test and verify RED**

Run:

```powershell
mvn -Dtest=KuGouMusicServiceTest#getLyricDownloadsBestMatchingStandardLrc test
```

Expected: compilation fails because `getLyric` does not exist.

- [ ] **Step 3: Implement minimal Kugou lyric retrieval**

Add imports for `Lyric`, `Base64`, `Pattern` and implement:

```java
private static final Pattern LRC_TIME_TAG = Pattern.compile("(?m)^\\[\\d{1,2}:\\d{2}(?:\\.\\d{1,3})?]");

public Lyric getLyric(String keyword) throws IOException {
    String[] parsed = SongUtil.parseWindowTitle(keyword);
    String realTitle = parsed[0];
    String realAuthor = parsed[1];
    KuGouSong song = searchSong(keyword);
    Track track = song.getTrack();

    Lyric lyric = new Lyric();
    lyric.setSource("kugou");
    lyric.setTitle(track.getTitle());
    lyric.setAuthor(track.getAuthor());
    lyric.setDuration(track.getDuration());

    int similarity = SongMatchingUtil.calculateSimilarity(
            realTitle, realAuthor, track.getTitle(), track.getAuthor());
    int threshold = realAuthor == null || realAuthor.isBlank()
            ? 75 : SongMatchingUtil.EXACT_MATCH_THRESHOLD;
    if (similarity < threshold) {
        lyric.setTitle(realTitle);
        lyric.setAuthor(realAuthor);
        return lyric;
    }

    JSONObject candidate = findBestLyricCandidate(keyword, song, realTitle, realAuthor);
    if (candidate == null) {
        return lyric;
    }

    String downloadUrl = UriComponentsBuilder
            .fromHttpUrl("https://lyrics.kugou.com/download")
            .queryParam("ver", 1)
            .queryParam("client", "pc")
            .queryParam("id", candidate.getString("id"))
            .queryParam("accesskey", candidate.getString("accesskey"))
            .queryParam("fmt", "lrc")
            .queryParam("charset", "utf8")
            .build().encode(StandardCharsets.UTF_8).toUriString();
    JSONObject download = JSON.parseObject(httpClient.get(downloadUrl));
    if (download == null || download.getIntValue("status") != 200) {
        return lyric;
    }
    String content = download.getString("content");
    if (content == null || content.isBlank()) {
        return lyric;
    }
    String lrc;
    try {
        lrc = new String(Base64.getDecoder().decode(content), StandardCharsets.UTF_8);
    } catch (IllegalArgumentException exception) {
        return lyric;
    }
    if (!LRC_TIME_TAG.matcher(lrc).find()) {
        return lyric;
    }
    lyric.setHasLyric(true);
    lyric.setLrc(lrc);
    return lyric;
}
```

Implement candidate search and stable selection:

```java
private JSONObject findBestLyricCandidate(
        String keyword, KuGouSong song, String realTitle, String realAuthor) throws IOException {
    String url = UriComponentsBuilder
            .fromHttpUrl("https://lyrics.kugou.com/search")
            .queryParam("ver", 1)
            .queryParam("man", "yes")
            .queryParam("client", "pc")
            .queryParam("keyword", keyword)
            .queryParam("duration", song.getTrack().getDuration() * 1000)
            .queryParam("hash", song.getFileHash())
            .build().encode(StandardCharsets.UTF_8).toUriString();
    JSONObject response = JSON.parseObject(httpClient.get(url));
    if (response == null || response.getIntValue("status") != 200) {
        return null;
    }
    JSONArray candidates = response.getJSONArray("candidates");
    if (candidates == null || candidates.isEmpty()) {
        return null;
    }

    JSONObject best = null;
    int bestSimilarity = Integer.MIN_VALUE;
    int bestDurationDiff = Integer.MAX_VALUE;
    for (int index = 0; index < candidates.size(); index++) {
        JSONObject candidate = candidates.getJSONObject(index);
        int candidateSimilarity = SongMatchingUtil.calculateSimilarity(
                realTitle, realAuthor,
                candidate.getString("song"), candidate.getString("singer"));
        int durationDiff = Math.abs(
                candidate.getIntValue("duration") - song.getTrack().getDuration() * 1000);
        if (candidateSimilarity > bestSimilarity
                || candidateSimilarity == bestSimilarity && durationDiff < bestDurationDiff) {
            best = candidate;
            bestSimilarity = candidateSimilarity;
            bestDurationDiff = durationDiff;
        }
    }
    return best;
}
```

- [ ] **Step 4: Run the success test and verify GREEN**

Run:

```powershell
mvn -Dtest=KuGouMusicServiceTest#getLyricDownloadsBestMatchingStandardLrc test
```

Expected: the test passes and verifies the matching candidate ID was downloaded.

- [ ] **Step 5: Add failure-path tests before changing code further**

Add parameterized or individual tests for:

```java
@Test
void getLyricReturnsEmptyWhenCandidatesAreMissing() throws Exception {
    KuGouHttpClient client = mock(KuGouHttpClient.class);
    when(client.get(contains("song_search_v2"))).thenReturn(SONG_SEARCH_RESPONSE);
    when(client.get(contains("lyrics.kugou.com/search")))
            .thenReturn("{\"status\":200,\"candidates\":[]}");
    Lyric lyric = new KuGouMusicService(client).getLyric("晴天 - 周杰伦");
    assertEquals("kugou", lyric.getSource());
    assertFalse(lyric.getHasLyric());
}

@Test
void getLyricRejectsInvalidBase64() throws Exception {
    KuGouHttpClient client = mock(KuGouHttpClient.class);
    when(client.get(contains("song_search_v2"))).thenReturn(SONG_SEARCH_RESPONSE);
    when(client.get(contains("lyrics.kugou.com/search"))).thenReturn(LYRIC_SEARCH_RESPONSE);
    when(client.get(contains("lyrics.kugou.com/download")))
            .thenReturn("{\"status\":200,\"content\":\"not-base64%%%\"}");
    assertFalse(new KuGouMusicService(client)
            .getLyric("晴天 - 周杰伦").getHasLyric());
}

@Test
void getLyricRejectsTextWithoutTimeTags() throws Exception {
    String encoded = Base64.getEncoder().encodeToString(
            "plain text".getBytes(StandardCharsets.UTF_8));
    KuGouHttpClient client = mock(KuGouHttpClient.class);
    when(client.get(contains("song_search_v2"))).thenReturn(SONG_SEARCH_RESPONSE);
    when(client.get(contains("lyrics.kugou.com/search"))).thenReturn(LYRIC_SEARCH_RESPONSE);
    when(client.get(contains("lyrics.kugou.com/download")))
            .thenReturn("{\"status\":200,\"content\":\"" + encoded + "\"}");
    assertFalse(new KuGouMusicService(client)
            .getLyric("晴天 - 周杰伦").getHasLyric());
}

@Test
void getLyricRejectsFailedDownloadStatus() throws Exception {
    KuGouHttpClient client = mock(KuGouHttpClient.class);
    when(client.get(contains("song_search_v2"))).thenReturn(SONG_SEARCH_RESPONSE);
    when(client.get(contains("lyrics.kugou.com/search"))).thenReturn(LYRIC_SEARCH_RESPONSE);
    when(client.get(contains("lyrics.kugou.com/download")))
            .thenReturn("{\"status\":500,\"content\":\"\"}");
    assertFalse(new KuGouMusicService(client)
            .getLyric("晴天 - 周杰伦").getHasLyric());
}

@Test
void getLyricRejectsEmptyDownloadContent() throws Exception {
    KuGouHttpClient client = mock(KuGouHttpClient.class);
    when(client.get(contains("song_search_v2"))).thenReturn(SONG_SEARCH_RESPONSE);
    when(client.get(contains("lyrics.kugou.com/search"))).thenReturn(LYRIC_SEARCH_RESPONSE);
    when(client.get(contains("lyrics.kugou.com/download")))
            .thenReturn("{\"status\":200,\"content\":\"\"}");
    assertFalse(new KuGouMusicService(client)
            .getLyric("晴天 - 周杰伦").getHasLyric());
}
```

- [ ] **Step 6: Run all Kugou tests**

```powershell
mvn -Dtest=KuGouMusicServiceTest test
```

Expected: every success and failure-path test passes.

- [ ] **Step 7: Commit Kugou LRC support**

```powershell
git add src/main/java/com/widdit/nowplaying/service/kugou/KuGouMusicService.java src/test/java/com/widdit/nowplaying/service/kugou/KuGouMusicServiceTest.java
git commit -m "feat: fetch standard kugou lyrics"
```

## Task 4: Route Explicit Kugou Source Requests

**Files:**
- Modify: `work/now-playing-service/src/main/java/com/widdit/nowplaying/service/LyricService.java`
- Create: `work/now-playing-service/src/test/java/com/widdit/nowplaying/service/LyricServiceTest.java`

- [ ] **Step 1: Write failing routing tests**

Use Mockito and `ReflectionTestUtils` to inject dependencies and settings:

```java
package com.widdit.nowplaying.service;

import com.widdit.nowplaying.entity.Lyric;
import com.widdit.nowplaying.entity.SettingsLyricCommon;
import com.widdit.nowplaying.service.kugou.KuGouMusicService;
import com.widdit.nowplaying.service.netease.NeteaseMusicService;
import com.widdit.nowplaying.service.qq.QQMusicService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LyricServiceTest {
    @Test
    void routesExplicitKugouSource() throws Exception {
        AudioService audio = mock(AudioService.class);
        KuGouMusicService kugou = mock(KuGouMusicService.class);
        Lyric expected = lyric("kugou", "晴天", "周杰伦", true, false, false, 269);
        when(audio.getWindowTitle()).thenReturn("晴天 - 周杰伦");
        when(audio.getStatus()).thenReturn("Playing");
        when(kugou.getLyric("晴天 - 周杰伦")).thenReturn(expected);

        LyricService service = serviceWith(audio, mock(NeteaseMusicService.class),
                mock(QQMusicService.class), kugou, "kugou", false);

        assertEquals(expected, service.getLyric());
        verify(kugou).getLyric("晴天 - 周杰伦");
    }
}
```

Add private test helpers with concrete values:

```java
private static Lyric lyric(String source, String title, String author,
                           boolean lrc, boolean translated, boolean karaoke, int duration) {
    Lyric value = new Lyric();
    value.setSource(source);
    value.setTitle(title);
    value.setAuthor(author);
    value.setDuration(duration);
    value.setHasLyric(lrc);
    value.setHasTranslatedLyric(translated);
    value.setHasKaraokeLyric(karaoke);
    return value;
}

private static LyricService serviceWith(
        AudioService audio, NeteaseMusicService netease, QQMusicService qq,
        KuGouMusicService kugou, String source, boolean auto) {
    LyricService service = new LyricService();
    ReflectionTestUtils.setField(service, "audioService", audio);
    ReflectionTestUtils.setField(service, "neteaseMusicService", netease);
    ReflectionTestUtils.setField(service, "qqMusicService", qq);
    ReflectionTestUtils.setField(service, "kuGouMusicService", kugou);
    SettingsLyricCommon settings = new SettingsLyricCommon();
    settings.setLyricSource(source);
    settings.setAutoSelectBestLyric(auto);
    ReflectionTestUtils.setField(service, "settingsCommon", settings);
    return service;
}
```

Add the unknown-source fallback test:

```java
@Test
void unknownExplicitSourceFallsBackToNetease() throws Exception {
    AudioService audio = mock(AudioService.class);
    NeteaseMusicService netease = mock(NeteaseMusicService.class);
    Lyric expected = lyric("netease", "普通朋友", "陶喆", true, false, false, 244);
    when(audio.getWindowTitle()).thenReturn("普通朋友 - 陶喆");
    when(audio.getStatus()).thenReturn("Playing");
    when(netease.getLyric("普通朋友 - 陶喆")).thenReturn(expected);

    LyricService service = serviceWith(audio, netease, mock(QQMusicService.class),
            mock(KuGouMusicService.class), "broken-source", false);

    assertEquals(expected, service.getLyric());
    verify(netease).getLyric("普通朋友 - 陶喆");
}
```

- [ ] **Step 2: Run routing tests and verify RED**

```powershell
mvn -Dtest=LyricServiceTest#routesExplicitKugouSource test
```

Expected: failure because `LyricService` has no Kugou dependency or route.

- [ ] **Step 3: Implement source routing**

Add:

```java
@Autowired
private KuGouMusicService kuGouMusicService;
```

Extract explicit dispatch:

```java
private Lyric fetchFromSource(String source, String windowTitle) throws Exception {
    switch (source) {
        case "qq":
            return qqMusicService.getLyric(windowTitle);
        case "kugou":
            return kuGouMusicService.getLyric(windowTitle);
        case "netease":
        default:
            if (windowTitle.contains("周杰伦") || windowTitle.contains("周杰倫")) {
                return qqMusicService.getLyric(windowTitle);
            }
            return neteaseMusicService.getLyric(windowTitle);
    }
}
```

Replace the existing explicit-source `if/else` with `fetchFromSource(source, windowTitle)`.

- [ ] **Step 4: Run routing tests and verify GREEN**

```powershell
mvn -Dtest=LyricServiceTest test
```

Expected: explicit Kugou and unknown-source fallback tests pass.

- [ ] **Step 5: Commit source routing**

```powershell
git add src/main/java/com/widdit/nowplaying/service/LyricService.java src/test/java/com/widdit/nowplaying/service/LyricServiceTest.java
git commit -m "feat: route kugou lyric source"
```

## Task 5: Generalize Best-Lyrics Selection to Three Sources

**Files:**
- Modify: `work/now-playing-service/src/main/java/com/widdit/nowplaying/service/LyricService.java`
- Modify: `work/now-playing-service/src/test/java/com/widdit/nowplaying/service/LyricServiceTest.java`

- [ ] **Step 1: Write failing three-source tests**

Add tests proving all three services are requested, one failure is tolerated, completeness wins, and configured source breaks a tie:

```java
@Test
void autoSelectionRequestsAllThreeSourcesAndUsesConfiguredTieBreaker() throws Exception {
    AudioService audio = mock(AudioService.class);
    NeteaseMusicService netease = mock(NeteaseMusicService.class);
    QQMusicService qq = mock(QQMusicService.class);
    KuGouMusicService kugou = mock(KuGouMusicService.class);
    when(audio.getWindowTitle()).thenReturn("晴天 - 周杰伦");
    when(audio.getStatus()).thenReturn("Playing");
    when(netease.getLyric("晴天 - 周杰伦"))
            .thenReturn(lyric("netease", "晴天", "周杰伦", true, false, false, 269));
    when(qq.getLyric("晴天 - 周杰伦"))
            .thenReturn(lyric("qq", "晴天", "周杰伦", true, false, false, 269));
    when(kugou.getLyric("晴天 - 周杰伦"))
            .thenReturn(lyric("kugou", "晴天", "周杰伦", true, false, false, 269));

    Lyric result = serviceWith(audio, netease, qq, kugou, "kugou", true).getLyric();

    assertEquals("kugou", result.getSource());
    verify(netease).getLyric("晴天 - 周杰伦");
    verify(qq).getLyric("晴天 - 周杰伦");
    verify(kugou).getLyric("晴天 - 周杰伦");
}

@Test
void autoSelectionSurvivesOneProviderFailure() throws Exception {
    AudioService audio = mock(AudioService.class);
    NeteaseMusicService netease = mock(NeteaseMusicService.class);
    QQMusicService qq = mock(QQMusicService.class);
    KuGouMusicService kugou = mock(KuGouMusicService.class);
    when(audio.getWindowTitle()).thenReturn("晴天 - 周杰伦");
    when(audio.getStatus()).thenReturn("Playing");
    when(netease.getLyric("晴天 - 周杰伦")).thenThrow(new RuntimeException("offline"));
    when(qq.getLyric("晴天 - 周杰伦"))
            .thenReturn(lyric("qq", "晴天", "周杰伦", true, true, true, 269));
    when(kugou.getLyric("晴天 - 周杰伦"))
            .thenReturn(lyric("kugou", "晴天", "周杰伦", true, false, false, 269));

    Lyric result = serviceWith(audio, netease, qq, kugou, "netease", true).getLyric();

    assertEquals("qq", result.getSource());
}
```

- [ ] **Step 2: Run tests and verify RED**

```powershell
mvn -Dtest=LyricServiceTest#autoSelectionRequestsAllThreeSourcesAndUsesConfiguredTieBreaker,LyricServiceTest#autoSelectionSurvivesOneProviderFailure test
```

Expected: Kugou is never requested or tie-breaking returns a two-source result.

- [ ] **Step 3: Implement stable multi-source selection**

Replace the fixed pair implementation with ordered futures:

```java
private static final List<String> LYRIC_SOURCE_ORDER = List.of("netease", "qq", "kugou");

private Lyric selectBestLyric(String preferredSource, String windowTitle) {
    Map<String, CompletableFuture<Lyric>> futures = new LinkedHashMap<>();
    for (String source : LYRIC_SOURCE_ORDER) {
        futures.put(source, CompletableFuture.supplyAsync(() -> {
            try {
                return fetchFromSourceWithoutFallback(source, windowTitle);
            } catch (Exception exception) {
                log.error("获取 {} 歌词失败：{}", source, exception.getMessage());
                return null;
            }
        }));
    }
    CompletableFuture.allOf(futures.values().toArray(new CompletableFuture[0])).join();
    List<Lyric> candidates = futures.values().stream()
            .map(CompletableFuture::join)
            .filter(java.util.Objects::nonNull)
            .collect(java.util.stream.Collectors.toList());
    if (candidates.isEmpty()) {
        return createEmptyLyric(windowTitle, preferredSource);
    }
    return chooseBestCandidate(candidates, preferredSource, windowTitle);
}
```

Use a no-fallback dispatcher so automatic Netease requests do not silently become QQ results:

```java
private Lyric fetchFromSourceWithoutFallback(String source, String windowTitle) throws Exception {
    switch (source) {
        case "qq":
            return qqMusicService.getLyric(windowTitle);
        case "kugou":
            return kuGouMusicService.getLyric(windowTitle);
        case "netease":
        default:
            return neteaseMusicService.getLyric(windowTitle);
    }
}
```

Implement candidate selection while preserving the old closeness rules:

```java
private Lyric chooseBestCandidate(List<Lyric> candidates, String preferredSource, String windowTitle) {
    String[] parsed = SongUtil.parseWindowTitle(windowTitle);
    Map<Lyric, Integer> similarities = new LinkedHashMap<>();
    for (Lyric candidate : candidates) {
        similarities.put(candidate, SongMatchingUtil.calculateSimilarity(
                parsed[0], parsed[1], candidate.getTitle(), candidate.getAuthor()));
    }
    Lyric highest = candidates.stream()
            .max(Comparator.comparingInt(similarities::get))
            .orElse(candidates.get(0));
    int highestSimilarity = similarities.get(highest);
    List<Lyric> close = candidates.stream()
            .filter(candidate -> {
                int similarityDiff = highestSimilarity - similarities.get(candidate);
                int durationDiff = Math.abs(highest.getDuration() - candidate.getDuration());
                return similarityDiff <= 2 || durationDiff <= 2 && similarityDiff <= 8;
            })
            .collect(Collectors.toList());
    if (close.size() == 1) {
        return close.get(0);
    }
    int bestScore = close.stream().mapToInt(this::lyricCompletenessScore).max().orElse(0);
    List<Lyric> best = close.stream()
            .filter(candidate -> lyricCompletenessScore(candidate) == bestScore)
            .collect(Collectors.toList());
    return best.stream()
            .filter(candidate -> preferredSource.equals(candidate.getSource()))
            .findFirst()
            .orElseGet(() -> LYRIC_SOURCE_ORDER.stream()
                    .flatMap(source -> best.stream()
                            .filter(candidate -> source.equals(candidate.getSource())))
                    .findFirst()
                    .orElse(best.get(0)));
}

private int lyricCompletenessScore(Lyric lyric) {
    int score = 0;
    if (Boolean.TRUE.equals(lyric.getHasLyric())) score++;
    if (Boolean.TRUE.equals(lyric.getHasTranslatedLyric())) score++;
    if (Boolean.TRUE.equals(lyric.getHasKaraokeLyric())) score++;
    return score;
}
```

Add required imports for `ArrayList` only if used, `Comparator`, `LinkedHashMap`, `Collectors` and `Objects`. Remove the old two-argument `selectByScore` method.

- [ ] **Step 4: Run all LyricService tests**

```powershell
mvn -Dtest=LyricServiceTest test
```

Expected: all routing, failure and tie-break tests pass.

- [ ] **Step 5: Run the backend test suite**

```powershell
mvn test
```

Expected: zero failures and zero errors.

- [ ] **Step 6: Commit three-source selection**

```powershell
git add src/main/java/com/widdit/nowplaying/service/LyricService.java src/test/java/com/widdit/nowplaying/service/LyricServiceTest.java
git commit -m "feat: select lyrics from three sources"
```

## Task 6: Add Kugou to the Frontend Source Selector

**Files:**
- Create: `work/now-playing-frontend/src/constants/lyricSources.ts`
- Create: `work/now-playing-frontend/src/constants/lyricSources.test.ts`
- Modify: `work/now-playing-frontend/src/pages/SettingsLyric.tsx`
- Modify: `work/now-playing-frontend/package.json`
- Modify: `work/now-playing-frontend/package-lock.json`

- [ ] **Step 1: Create an implementation branch and install Vitest**

```powershell
git switch -c feat/kugou-lyrics-source
npm install --save-dev vitest
```

Add the script:

```json
"test": "vitest run"
```

- [ ] **Step 2: Write the failing source configuration test**

Create:

```typescript
import { describe, expect, it } from "vitest";
import { LYRIC_SOURCES } from "./lyricSources";
import { DEFAULT_SETTINGS_LYRIC_COMMON } from "@/types/backend/settingsLyricCommon";

describe("LYRIC_SOURCES", () => {
  it("contains the three supported lyric source keys in stable order", () => {
    expect(LYRIC_SOURCES.map(source => source.key)).toEqual([
      "netease",
      "qq",
      "kugou",
    ]);
  });

  it("uses the existing Kugou label and icon", () => {
    expect(LYRIC_SOURCES.find(source => source.key === "kugou")).toEqual({
      key: "kugou",
      label: "酷狗音乐",
      icon: "/assets/kugou_icon.png",
      widthClass: "w-[100px]",
    });
  });

  it("keeps Netease as the default source", () => {
    expect(DEFAULT_SETTINGS_LYRIC_COMMON.lyricSource).toBe("netease");
  });
});
```

- [ ] **Step 3: Run the frontend test and verify RED**

```powershell
npx vitest run src/constants/lyricSources.test.ts
```

Expected: module resolution fails because `lyricSources.ts` does not exist.

- [ ] **Step 4: Add the source configuration**

Create:

```typescript
export const LYRIC_SOURCES = [
  {
    key: "netease",
    label: "网易云音乐",
    icon: "/assets/netease_icon.png",
    widthClass: "w-[100px]",
  },
  {
    key: "qq",
    label: "QQ音乐",
    icon: "/assets/qq_icon.png",
    widthClass: "w-[80px]",
  },
  {
    key: "kugou",
    label: "酷狗音乐",
    icon: "/assets/kugou_icon.png",
    widthClass: "w-[100px]",
  },
] as const;
```

- [ ] **Step 5: Run the source test and verify GREEN**

```powershell
npx vitest run src/constants/lyricSources.test.ts
```

Expected: three tests pass.

- [ ] **Step 6: Render tabs from configuration**

Import:

```typescript
import { LYRIC_SOURCES } from "@/constants/lyricSources";
```

Replace the two hard-coded `<Tab>` elements with:

```tsx
{LYRIC_SOURCES.map(source => (
  <Tab
    key={source.key}
    title={
      <div className={`flex items-center justify-center space-x-2 ${source.widthClass}`}>
        <img alt="" src={source.icon} className="h-4.5" />
        <span className="font-poppins">{source.label}</span>
      </div>
    }
  />
))}
```

Change the description text to:

```tsx
同时从多个歌词源获取歌词，返回最佳结果
```

- [ ] **Step 7: Run tests and build**

```powershell
npm test
npm run build
```

Expected: all tests pass; TypeScript and Vite exit with code 0 and create `dist/`.

- [ ] **Step 8: Commit frontend support**

```powershell
git add package.json package-lock.json src/constants/lyricSources.ts src/constants/lyricSources.test.ts src/pages/SettingsLyric.tsx
git commit -m "feat: add kugou lyric source option"
```

## Task 7: Package the Updated Frontend in the Backend

**Files:**
- Replace: `work/now-playing-service/src/main/resources/static/**`
- Replace: `work/now-playing-service/src/main/resources/templates/index.html`

- [ ] **Step 1: Verify the frontend build before copying**

Run in `work/now-playing-frontend`:

```powershell
npm test
npm run build
```

Expected: both commands exit 0 and `dist/index.html` exists.

- [ ] **Step 2: Replace the tracked backend bundle with `dist`**

Use PowerShell end-to-end and verify resolved paths before removing generated files:

```powershell
$frontendDist = (Resolve-Path -LiteralPath "dist").Path
$backendRoot = (Resolve-Path -LiteralPath "../now-playing-service").Path
$backendStatic = Join-Path $backendRoot "src/main/resources/static"
$backendTemplate = Join-Path $backendRoot "src/main/resources/templates/index.html"
if (-not $frontendDist.StartsWith((Resolve-Path ".").Path)) { throw "Unexpected frontend dist path" }
if (-not $backendStatic.StartsWith($backendRoot)) { throw "Unexpected backend static path" }
Remove-Item -LiteralPath $backendStatic -Recurse -Force
New-Item -ItemType Directory -Path $backendStatic | Out-Null
Copy-Item -Path (Join-Path $frontendDist "*") -Destination $backendStatic -Recurse -Force
Copy-Item -LiteralPath (Join-Path $frontendDist "index.html") -Destination $backendTemplate -Force
Remove-Item -LiteralPath (Join-Path $backendStatic "index.html") -Force
```

- [ ] **Step 3: Confirm the generated bundle contains Kugou UI text**

Run in `work/now-playing-service`:

```powershell
rg -n "酷狗音乐|多个歌词源" src/main/resources/static/vite-assets src/main/resources/templates/index.html
```

Expected: the generated SettingsLyric asset contains both strings.

- [ ] **Step 4: Build the packaged backend**

```powershell
mvn clean package
```

Expected: tests pass and the Spring Boot package is created under `target/`.

- [ ] **Step 5: Commit generated bundle changes separately**

```powershell
git add src/main/resources/static src/main/resources/templates/index.html
git commit -m "build: bundle kugou lyric source frontend"
```

## Task 8: Final Verification and Real-API Smoke Test

**Files:**
- No production file changes expected.

- [ ] **Step 1: Run complete backend verification**

```powershell
mvn clean test
mvn package
git diff --check
git status --short
```

Expected: both Maven commands exit 0, no whitespace errors, and only intentional commits are present.

- [ ] **Step 2: Run complete frontend verification**

```powershell
npm test
npm run build
git diff --check
git status --short
```

Expected: tests and build pass with no uncommitted source changes.

- [ ] **Step 3: Run a read-only real Kugou API smoke test**

Execute the dedicated integration test only when network is available:

```powershell
mvn '-Dtest=KuGouMusicServiceTest#realApiReturnsTimedLrc' test '-Dkugou.integration=true'
```

Add this guarded method to `KuGouMusicServiceTest` and run it with a real client:

```java
@Test
void realApiReturnsTimedLrc() throws Exception {
    org.junit.jupiter.api.Assumptions.assumeTrue(
            Boolean.getBoolean("kugou.integration"));
    Lyric lyric = new KuGouMusicService(new KuGouHttpClient())
            .getLyric("晴天 - 周杰伦");
    assertEquals("kugou", lyric.getSource());
    assertTrue(lyric.getHasLyric());
    assertTrue(lyric.getLrc().matches(
            "(?s).*\\[\\d{1,2}:\\d{2}(?:\\.\\d{1,3})?].*"));
}
```

Expected: the guarded integration test passes. If the third-party API is temporarily unavailable, report it separately without representing unit/build verification as failed.

- [ ] **Step 4: Verify requirements against the design**

Confirm each item explicitly:

- `kugou` can be saved as `lyricSource`.
- Explicit Kugou requests return standard timed LRC.
- Automatic selection considers Netease, QQ and Kugou.
- Single-provider failure does not prevent a result.
- Equal results prefer the configured source.
- The packaged backend UI contains the Kugou tab.
- No KRC, translated lyric or karaoke flags are emitted by Kugou.

- [ ] **Step 5: Record final commit IDs and handoff**

Run in both repositories:

```powershell
git log --oneline --decorate -8
git status --short --branch
```

Expected: both branches are clean and the implementation commits are visible.
