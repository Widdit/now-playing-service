package com.widdit.nowplaying.util;

import java.util.*;
import java.util.regex.*;

/**
 * 歌曲匹配工具类
 * 用于比较本地歌曲信息与云端搜索结果的匹配程度
 */
public class SongMatchingUtil {

    // ==================== 配置常量 ====================

    /**
     * 精确匹配阈值 - 精确匹配，且版本正确。高于此分数可认为匹配成功
     */
    public static final int EXACT_MATCH_THRESHOLD = 85;

    /**
     * 及格线阈值 - 同一首歌，但是不同版本
     */
    public static final int ALTERNATE_VERSION_THRESHOLD = 60;

    /**
     * 改变歌曲性质的关键词（严格版本匹配）
     * 这些关键词表示歌曲的特殊版本，需要严格匹配
     */
    private static final Set<String> SIGNIFICANT_KEYWORDS = new HashSet<>(Arrays.asList(
            // 英文关键词 - 版本类型
            "remix", "live", "acoustic", "instrumental", "cover",
            "edit", "mix", "dj", "radio", "extended", "remaster", "remastered",
            "unplugged", "demo", "bootleg", "mashup", "orchestral", "symphony",
            "stripped", "sped up", "slowed", "reverb", "0.8x", "1.1x", "1.2x",
            // 中文关键词
            "现场", "翻唱", "伴奏", "钢琴版", "吉他版", "改编", "翻自",
            "重制", "混音", "慢速", "快速", "加速", "倍速", "粤语版", "填词",
            "DJ版"
    ));

    /**
     * 需要特殊处理的版本关键词（模式匹配）
     * 用于识别 "ver.", "ver", "version" 等变体
     */
    private static final List<Pattern> SIGNIFICANT_PATTERNS = Arrays.asList(
            Pattern.compile("\\bver\\.?\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bversion\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bedition\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bremix\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\blive\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bacoustic\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\binstrumental\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bcover\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bdemo\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bremaster(ed)?\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bunplugged\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bkaraoke\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bextended\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bradio\\b", Pattern.CASE_INSENSITIVE)
    );

    /**
     * 表示翻译或别名的无害关键词
     */
    private static final Set<String> HARMLESS_PATTERNS = new HashSet<>(Arrays.asList(
            "explicit", "clean", "original mix", "feat", "ft.", "翻译", "译名", "又名", "别名", "原名", "aka"
    ));

    // ==================== 核心公共方法 ====================

    /**
     * 计算本地歌曲信息与云端歌曲信息的匹配分数（相似度）（范围：0-100）
     *
     * @param localTitle  本地歌名
     * @param localArtist 本地歌手名（多歌手用 " / " 分隔）
     * @param cloudTitle  云端歌名
     * @param cloudArtist 云端歌手名（多歌手用 " / " 分隔）
     * @return 匹配分数 (0-100)，分数越高表示匹配程度越高
     */
    public static int calculateSimilarity(String localTitle, String localArtist,
                                          String cloudTitle, String cloudArtist) {
        // 空值检查
        if (isNullOrEmpty(localTitle) || isNullOrEmpty(cloudTitle)) {
            return 0;
        }

        // 预处理：统一格式
        localTitle = normalize(localTitle);
        localArtist = normalize(localArtist);
        cloudTitle = normalize(cloudTitle);
        cloudArtist = normalize(cloudArtist);

        // 计算歌名分数（权重 65%）
        int titleScore = calculateTitleScore(localTitle, cloudTitle);

        // 如果歌名分数过低，直接返回低分
        if (titleScore < 30) {
            return titleScore;
        }

        // 计算歌手分数（权重 35%）
        int artistScore = calculateArtistScore(localArtist, cloudArtist);

        // 综合分数计算
        // 如果歌手完全不匹配（0分），需要大幅降低总分
        if (artistScore == 0 && !isNullOrEmpty(localArtist) && !isNullOrEmpty(cloudArtist)) {
            return Math.min(titleScore / 2, 40);
        }

        int totalScore = (int) (titleScore * 0.65 + artistScore * 0.35);

        return clamp(totalScore, 0, 100);
    }

    // ==================== 预处理方法 ====================

    /**
     * 预处理文本：统一格式，便于比较
     */
    private static String normalize(String text) {
        if (text == null) return "";

        // 转小写
        text = text.toLowerCase();

        // 统一各种类型的括号为半角圆括号
        text = unifyBrackets(text);

        // 全角转半角
        text = fullWidthToHalfWidth(text);

        // 替换括号中的竖线
        text = replacePipeInParentheses(text);

        // 规范化空格
        text = text.replaceAll("\\s+", " ").trim();

        return text;
    }

    /**
     * 统一各种类型的括号为半角圆括号
     */
    private static String unifyBrackets(String text) {
        // 中文括号
        text = text.replace('（', '(').replace('）', ')');
        // 方括号
        text = text.replace('[', '(').replace(']', ')');
        // 中文方括号
        text = text.replace('【', '(').replace('】', ')');
        // 日文括号
        text = text.replace('「', '(').replace('」', ')');
        text = text.replace('『', '(').replace('』', ')');
        // 其他括号类型
        text = text.replace('〔', '(').replace('〕', ')');
        text = text.replace('〈', '(').replace('〉', ')');

        return text;
    }

    /**
     * 全角字符转半角字符
     */
    private static String fullWidthToHalfWidth(String text) {
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            // 全角ASCII字符范围 FF01-FF5E 对应 半角 0021-007E
            if (c >= '\uFF01' && c <= '\uFF5E') {
                sb.append((char) (c - 0xFEE0));
            } else if (c == '\u3000') { // 全角空格
                sb.append(' ');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 替换括号中的竖线
     * 例："Beckham (UK Mix|Explicit)" → "Beckham (UK Mix) (Explicit)"
     */
    private static String replacePipeInParentheses(String str) {
        if (str == null || !str.contains("|")) {
            return str;
        }

        StringBuilder result = new StringBuilder();
        int i = 0;

        while (i < str.length()) {
            char c = str.charAt(i);

            if (c == '(') {
                // 找到对应的 ')'
                int closeIndex = str.indexOf(')', i);

                if (closeIndex != -1) {
                    // 获取括号内的内容
                    String inside = str.substring(i + 1, closeIndex);

                    // 如果包含 '|'，则进行替换
                    if (inside.contains("|")) {
                        inside = inside.replace("|", ") (");
                    }

                    result.append("(").append(inside).append(")");
                    i = closeIndex + 1;
                } else {
                    // 没有找到匹配的 ')'，原样添加
                    result.append(c);
                    i++;
                }
            } else {
                result.append(c);
                i++;
            }
        }

        return result.toString();
    }

    // ==================== 标题处理方法 ====================

    /**
     * 提取主标题（去除所有括号及其内容）
     */
    private static String extractBaseTitle(String title) {
        if (title == null) return "";
        // 移除所有括号及其内容，同时处理可能产生的多余空格
        return title.replaceAll("\\s*\\([^)]*\\)\\s*", " ").trim().replaceAll("\\s+", " ");
    }

    /**
     * 提取所有括号内的信息
     */
    private static List<String> extractExtraInfo(String title) {
        List<String> extras = new ArrayList<>();
        if (title == null) return extras;

        Pattern pattern = Pattern.compile("\\(([^)]+)\\)");
        Matcher matcher = pattern.matcher(title);
        while (matcher.find()) {
            String content = matcher.group(1).trim().toLowerCase();
            if (!content.isEmpty()) {
                extras.add(content);
            }
        }
        return extras;
    }

    /**
     * 计算歌名匹配分数
     * 采用"满分扣分制"：从100分开始，根据不匹配程度扣分
     */
    private static int calculateTitleScore(String localTitle, String cloudTitle) {
        // 完全相等直接返回满分
        if (localTitle.equals(cloudTitle)) {
            return 100;
        }

        // 提取主标题
        String localBase = extractBaseTitle(localTitle);
        String cloudBase = extractBaseTitle(cloudTitle);

        // 提取括号内信息
        List<String> localExtras = extractExtraInfo(localTitle);
        List<String> cloudExtras = extractExtraInfo(cloudTitle);

        // ========== 第一步：检查基础标题 ==========
        int baseSimilarity = calculateStringSimilarity(localBase, cloudBase);

        // 基础标题必须严格匹配
        if (baseSimilarity < 95) {
            // 基础标题不匹配，根据相似度给予惩罚性分数
            if (baseSimilarity < 70) {
                return baseSimilarity / 4; // 0-17分
            } else if (baseSimilarity < 85) {
                return baseSimilarity / 3; // 23-28分
            } else {
                return baseSimilarity / 2; // 42-47分
            }
        }

        // 基础标题匹配成功，开始计算括号内容的惩罚
        int score = 100;

        // 基础标题不是完全匹配时，轻微扣分
        if (baseSimilarity < 100) {
            score -= (100 - baseSimilarity);
        }

        // ========== 第二步：计算括号内容的惩罚 ==========
        int extraPenalty = calculateExtraPenalty(localExtras, cloudExtras);
        score -= extraPenalty;

        return Math.max(0, score);
    }

    /**
     * 计算括号内容不匹配的惩罚分数
     * 返回需要扣除的分数（0-70）
     */
    private static int calculateExtraPenalty(List<String> localExtras, List<String> cloudExtras) {
        boolean localHasExtra = !localExtras.isEmpty();
        boolean cloudHasExtra = !cloudExtras.isEmpty();

        // 情况0：两者都没有括号信息 - 无惩罚
        if (!localHasExtra && !cloudHasExtra) {
            return 0;
        }

        // 情况1：本地有括号，云端没有
        if (localHasExtra && !cloudHasExtra) {
            return handleLocalHasExtraPenalty(localExtras);
        }

        // 情况2：本地没括号，云端有括号
        if (!localHasExtra && cloudHasExtra) {
            return handleCloudHasExtraPenalty(cloudExtras);
        }

        // 情况3：两者都有括号 - 详细比较内容
        return compareBothExtras(localExtras, cloudExtras);
    }

    /**
     * 处理情况1：本地有括号信息，云端没有
     * 返回惩罚分数
     */
    private static int handleLocalHasExtraPenalty(List<String> localExtras) {
        boolean hasSignificant = false;
        boolean onlyHarmless = true;

        for (String extra : localExtras) {
            if (containsSignificantKeyword(extra)) {
                hasSignificant = true;
                onlyHarmless = false;
                break;
            } else if (!isLikelyTranslationOrAlias(extra)) {
                onlyHarmless = false;
            }
        }

        if (hasSignificant) {
            // 用户想要特定版本（如 Remix/Live），但云端是原版
            // 这是严重错误，大幅扣分
            return 55;
        }

        if (onlyHarmless) {
            // 本地括号只是翻译/别名等，轻微扣分
            return 5;
        }

        // 其他情况，中等扣分（本地有一些括号内容，但不是重要关键词）
        return 25;
    }

    /**
     * 处理情况2：云端有括号信息，本地没有
     * 返回惩罚分数
     */
    private static int handleCloudHasExtraPenalty(List<String> cloudExtras) {
        boolean hasSignificant = false;
        boolean onlyHarmless = true;
        int aliasCount = 0;

        for (String extra : cloudExtras) {
            // 先检查是否是翻译类标注
            if (isLikelyTranslationOrAlias(extra)) {
                aliasCount++;
                continue;
            }
            if (containsSignificantKeyword(extra)) {
                hasSignificant = true;
                onlyHarmless = false;
                break;
            }
            onlyHarmless = false;
        }

        if (hasSignificant) {
            // 云端是特殊版本（Live/Remix等），但用户想要原版
            return 50;
        }

        if (onlyHarmless) {
            // 云端括号只是翻译/别名，轻微惩罚
            return 2 * aliasCount;
        }

        // 其他情况，轻微扣分
        return 15;
    }

    /**
     * 处理情况3：两者都有括号信息
     * 返回惩罚分数
     */
    private static int compareBothExtras(List<String> localExtras, List<String> cloudExtras) {
        // 提取双方的重要关键词
        Set<String> localKeywords = extractSignificantKeywords(localExtras);
        Set<String> cloudKeywords = extractSignificantKeywords(cloudExtras);

        boolean localHasSignificant = !localKeywords.isEmpty();
        boolean cloudHasSignificant = !cloudKeywords.isEmpty();

        int basePenalty = 0;

        // 情况3a：本地有重要关键词，云端没有
        if (localHasSignificant && !cloudHasSignificant) {
            return 50; // 用户要特定版本，云端可能是原版或只有翻译
        }

        // 情况3b：本地没有重要关键词，云端有
        if (!localHasSignificant && cloudHasSignificant) {
            return 45; // 云端是特殊版本，但用户没指定
        }

        // 情况3c：两者都有重要关键词
        if (localHasSignificant && cloudHasSignificant) {
            // 计算关键词的匹配程度
            Set<String> intersection = new HashSet<>(localKeywords);
            intersection.retainAll(cloudKeywords);

            if (intersection.isEmpty()) {
                // 没有交集，版本类型完全不同
                return 55;
            }

            // 检查关键词是否完全匹配
            if (intersection.size() == localKeywords.size() &&
                    intersection.size() == cloudKeywords.size()) {
                // 关键词完全匹配，检查详细内容
                basePenalty = calculateDetailedExtraPenalty(localExtras, cloudExtras);
            } else {
                // 部分匹配
                double matchRatio = (double) intersection.size() /
                        Math.max(localKeywords.size(), cloudKeywords.size());
                basePenalty = (int) ((1 - matchRatio) * 40);
            }
        } else {
            // 情况3d：两者都没有重要关键词，比较内容相似度
            basePenalty = calculateDetailedExtraPenalty(localExtras, cloudExtras);
        }

        // 计算额外/不匹配括号内容（alias）的惩罚
        int aliasPenalty = calculateAliasPenalty(localExtras, cloudExtras);

        return basePenalty + aliasPenalty;
    }

    /**
     * 详细比较括号内容，返回惩罚分数
     */
    private static int calculateDetailedExtraPenalty(List<String> localExtras, List<String> cloudExtras) {
        // 计算最佳匹配的相似度
        int totalSimilarity = 0;
        int count = 0;

        for (String local : localExtras) {
            int bestMatch = 0;
            for (String cloud : cloudExtras) {
                int similarity = calculateStringSimilarity(local, cloud);
                bestMatch = Math.max(bestMatch, similarity);
            }
            totalSimilarity += bestMatch;
            count++;
        }

        if (count == 0) return 0;

        int avgSimilarity = totalSimilarity / count;

        // 相似度越低，惩罚越高
        if (avgSimilarity >= 90) {
            return 5;
        } else if (avgSimilarity >= 70) {
            return 15;
        } else if (avgSimilarity >= 50) {
            return 30;
        } else {
            return 45;
        }
    }

    /**
     * 从括号内容列表中提取重要关键词
     */
    private static Set<String> extractSignificantKeywords(List<String> extras) {
        Set<String> keywords = new HashSet<>();
        for (String extra : extras) {
            String lower = extra.toLowerCase();

            // 检查普通关键词
            for (String keyword : SIGNIFICANT_KEYWORDS) {
                if (lower.contains(keyword)) {
                    keywords.add(keyword);
                }
            }

            // 检查模式匹配关键词
            for (Pattern pattern : SIGNIFICANT_PATTERNS) {
                if (pattern.matcher(lower).find()) {
                    // 提取匹配的关键词类型
                    String patternStr = pattern.pattern().replaceAll("\\\\b|\\?|\\(.*\\)", "");
                    keywords.add(patternStr.toLowerCase());
                }
            }
        }
        return keywords;
    }

    /**
     * 计算多余或不匹配的括号内容（alias）惩罚
     *
     * @return 惩罚分数
     */
    private static int calculateAliasPenalty(List<String> localExtras, List<String> cloudExtras) {
        if (localExtras.isEmpty() && cloudExtras.isEmpty()) {
            return 0;
        }

        // 创建匹配标记
        boolean[] localMatched = new boolean[localExtras.size()];
        boolean[] cloudMatched = new boolean[cloudExtras.size()];

        // 尝试为每个本地括号找到匹配的云端括号
        for (int i = 0; i < localExtras.size(); i++) {
            String local = localExtras.get(i);
            for (int j = 0; j < cloudExtras.size(); j++) {
                if (!cloudMatched[j]) {
                    if (isExtraContentMatch(local, cloudExtras.get(j))) {
                        localMatched[i] = true;
                        cloudMatched[j] = true;
                        break;
                    }
                }
            }
        }

        // 计算未匹配的括号数量（排除无害关键词）
        int unmatchedCount = 0;

        for (int i = 0; i < localExtras.size(); i++) {
            if (!localMatched[i] && !containsHarmlessKeyword(localExtras.get(i))) {
                unmatchedCount++;
            }
        }

        for (int j = 0; j < cloudExtras.size(); j++) {
            if (!cloudMatched[j] && !containsHarmlessKeyword(cloudExtras.get(j))) {
                unmatchedCount++;
            }
        }

        // 每个未匹配的括号扣2分
        return unmatchedCount * 2;
    }

    /**
     * 检查文本是否包含无害关键词
     */
    private static boolean containsHarmlessKeyword(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        for (String pattern : HARMLESS_PATTERNS) {
            if (text.contains(pattern)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 判断两个括号内容是否匹配
     */
    private static boolean isExtraContentMatch(String extra1, String extra2) {
        if (extra1 == null || extra2 == null) {
            return false;
        }

        // 完全相等
        if (extra1.equals(extra2)) {
            return true;
        }

        // 相似度匹配
        int similarity = calculateStringSimilarity(extra1, extra2);
        return similarity >= 65;
    }

    /**
     * 检查是否包含改变歌曲性质的关键词
     */
    private static boolean containsSignificantKeyword(String text) {
        String lower = text.toLowerCase();

        // 检查普通关键词
        for (String keyword : SIGNIFICANT_KEYWORDS) {
            if (lower.contains(keyword)) {
                return true;
            }
        }

        // 检查模式匹配关键词
        for (Pattern pattern : SIGNIFICANT_PATTERNS) {
            if (pattern.matcher(lower).find()) {
                return true;
            }
        }

        return false;
    }

    /**
     * 检查文本是否看起来像翻译或别名
     */
    private static boolean isLikelyTranslationOrAlias(String text) {
        // 检查是否包含无害关键词
        if (containsHarmlessKeyword(text)) {
            return true;
        }

        // 检查是否有重要关键词
        if (containsSignificantKeyword(text)) {
            return false;
        }

        // 检查是否是纯粹的其他语言文本（可能是翻译）
        // 规则：如果文本较短，且主要包含 CJK 字符（中、日、韩字符），且不含重要关键词
        if (text.length() < 30) {
            boolean hasCJK = text.matches(".*[\\u4e00-\\u9fa5\\u3040-\\u309f\\u30a0-\\u30ff\\uac00-\\ud7af]+.*");
            boolean fewLatinWords = !text.matches(".*[a-zA-Z]{4,}.*");

            if (hasCJK && fewLatinWords) {
                return true;
            }
        }

        return false;
    }

    // ==================== 歌手处理方法 ====================

    /**
     * 计算歌手匹配分数
     */
    private static int calculateArtistScore(String localArtist, String cloudArtist) {
        // 解析歌手列表
        List<String> localArtists = parseArtists(localArtist);
        List<String> cloudArtists = parseArtists(cloudArtist);

        // 空值处理
        if (localArtists.isEmpty() && cloudArtists.isEmpty()) {
            return 100; // 都没有歌手信息，认为匹配
        }
        if (localArtists.isEmpty() || cloudArtists.isEmpty()) {
            return 50; // 一方没有歌手信息
        }

        // 计算本地歌手在云端匹配的数量
        int localMatchCount = countMatches(localArtists, cloudArtists);

        // 没有任何歌手匹配
        if (localMatchCount == 0) {
            return 0;
        }

        // 计算云端歌手在本地匹配的数量
        int cloudMatchCount = countMatches(cloudArtists, localArtists);

        // 特殊情况：本地只有一个歌手，且匹配成功
        if (localArtists.size() == 1 && localMatchCount == 1) {
            return 100;
        }

        // 计算综合匹配度
        double localRatio = (double) localMatchCount / localArtists.size();
        double cloudRatio = (double) cloudMatchCount / cloudArtists.size();

        // 加权平均：本地歌手的匹配更重要（用户的本地歌曲是参照标准）
        int score = (int) (localRatio * 70 + cloudRatio * 30);

        // 至少有一个匹配，保证基础分
        return Math.max(score, 55);
    }

    /**
     * 计算 sourceList 中有多少歌手在 targetList 中能找到匹配
     */
    private static int countMatches(List<String> sourceList, List<String> targetList) {
        int matchCount = 0;
        for (String source : sourceList) {
            for (String target : targetList) {
                int similarity = calculateStringSimilarity(source, target);
                if (similarity >= 80) {
                    matchCount++;
                    break;
                }
            }
        }
        return matchCount;
    }

    /**
     * 解析歌手字符串为歌手列表
     */
    private static List<String> parseArtists(String artistStr) {
        List<String> artists = new ArrayList<>();
        if (isNullOrEmpty(artistStr)) {
            return artists;
        }

        // 按 / 分割歌手
        String[] parts = artistStr.split("/");
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                // 去除歌手名中的括号（如艺名标注）
                String baseName = extractBaseTitle(trimmed);
                if (!baseName.isEmpty()) {
                    artists.add(baseName);
                }
            }
        }
        return artists;
    }

    // ==================== 字符串相似度计算 ====================

    /**
     * 计算两个字符串的相似度 (0-100)
     */
    private static int calculateStringSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return 0;
        }

        s1 = s1.trim().toLowerCase();
        s2 = s2.trim().toLowerCase();

        // 完全相等
        if (s1.equals(s2)) {
            return 100;
        }

        // 空字符串检查
        if (s1.isEmpty() || s2.isEmpty()) {
            return 0;
        }

        // 包含关系检查
        if (s1.contains(s2)) {
            int ratio = s2.length() * 100 / s1.length();
            return 70 + ratio * 30 / 100;
        }
        if (s2.contains(s1)) {
            int ratio = s1.length() * 100 / s2.length();
            return 70 + ratio * 30 / 100;
        }

        // 使用编辑距离计算相似度
        int distance = levenshteinDistance(s1, s2);
        int maxLen = Math.max(s1.length(), s2.length());

        int similarity = Math.max(0, 100 - (distance * 100 / maxLen));

        return similarity;
    }

    /**
     * 计算编辑距离（Levenshtein Distance）
     */
    private static int levenshteinDistance(String s1, String s2) {
        int m = s1.length();
        int n = s2.length();

        // 优化：使用一维数组减少空间复杂度
        int[] prev = new int[n + 1];
        int[] curr = new int[n + 1];

        for (int j = 0; j <= n; j++) {
            prev[j] = j;
        }

        for (int i = 1; i <= m; i++) {
            curr[0] = i;
            for (int j = 1; j <= n; j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                curr[j] = Math.min(
                        Math.min(prev[j] + 1, curr[j - 1] + 1),
                        prev[j - 1] + cost
                );
            }
            int[] temp = prev;
            prev = curr;
            curr = temp;
        }

        return prev[n];
    }

    // ==================== 工具方法 ====================

    private static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    // ==================== 测试代码 ====================

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        System.out.println("╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║                      歌曲匹配工具 - 测试与演示                        ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("📌 精确匹配阈值: " + EXACT_MATCH_THRESHOLD + " 分（精确匹配，且版本正确）");
        System.out.println("📌 及格线阈值: " + ALTERNATE_VERSION_THRESHOLD + " 分（同一首歌，但是不同版本）");
        System.out.println();

        int[] results = runAllTests();

        System.out.println("══════════════════════════════════════════════════════════════════════");
        System.out.println("📊 测试统计: 通过 " + results[0] + "/" + results[1] + " (" +
                String.format("%.1f", results[0] * 100.0 / results[1]) + "%)");
        System.out.println("✅ 测试完成！");

        long endTime = System.currentTimeMillis();

        System.out.println("总耗时：" + (endTime - startTime) + " ms");
    }

    private static int passCount = 0;
    private static int totalCount = 0;

    private static int[] runAllTests() {
        passCount = 0;
        totalCount = 0;

        // 测试组1: 翻译/别名处理
        printTestGroup("测试组1: 翻译/别名处理（云端添加翻译信息）");
        testMatch("夜に駆ける", "YOASOBI",
                "夜に駆ける (向夜晚奔去)", "YOASOBI (ヨアソビ)",
                "云端添加中文翻译", true);
        testMatch("Shape of You", "Ed Sheeran",
                "Shape of You (塑造你)", "Ed Sheeran",
                "云端添加翻译", true);
        testMatch("Lemon", "米津玄師",
                "Lemon (柠檬)", "米津玄師 (Kenshi Yonezu)",
                "多语言翻译标注", true);

        // 测试组2: 完全匹配
        printTestGroup("测试组2: 完全匹配");
        testMatch("夜に駆ける", "YOASOBI",
                "夜に駆ける", "YOASOBI",
                "完全匹配", true);
        testMatch("Counting Stars (Tom Remix)", "OneRepublic",
                "Counting Stars (Tom Remix)", "OneRepublic",
                "带括号完全匹配", true);
        testMatch("Hello", "Adele",
                "Hello", "Adele",
                "简单匹配", true);

        // 测试组3: 本地有括号，云端没有（用户要特定版本）
        printTestGroup("测试组3: 本地有括号，云端没有（用户要特定版本）");
        testMatch("Counting Stars (Tom Remix)", "OneRepublic",
                "Counting Stars", "OneRepublic",
                "本地要Remix版，云端是原版", false);
        testMatch("Shape of You (Acoustic)", "Ed Sheeran",
                "Shape of You", "Ed Sheeran",
                "本地要Acoustic版，云端是原版", false);
        testMatch("Hello (Live)", "Adele",
                "Hello", "Adele",
                "本地要Live版，云端是原版", false);
        testMatch("Bohemian Rhapsody (Remastered 2011)", "Queen",
                "Bohemian Rhapsody", "Queen",
                "本地要重制版，云端是原版", false);

        // 测试组4: 本地没括号，云端有（云端是特殊版本）
        printTestGroup("测试组4: 本地没括号，云端有（云端是特殊版本）");
        testMatch("Counting Stars", "OneRepublic",
                "Counting Stars (Live)", "OneRepublic",
                "本地要原版，云端是Live版", false);
        testMatch("Hello", "Adele",
                "Hello (Remix)", "Adele",
                "本地要原版，云端是Remix版", false);
        testMatch("Castle on the Hill", "Ed Sheeran",
                "Castle on the Hill (Acoustic)", "Ed Sheeran",
                "本地要原版，云端是Acoustic版", false);
        testMatch("Perfect", "Ed Sheeran",
                "Perfect (伴奏)", "Ed Sheeran",
                "本地要原版，云端是伴奏版", false);

        // 测试组5: 多括号处理
        printTestGroup("测试组5: 多括号处理");
        testMatch("Quicksand (Don't Go) (Tom Remix)", "Artist",
                "Quicksand (Don't Go) (Tom Remix)", "Artist",
                "多括号完全匹配", true);
        testMatch("Quicksand (Don't Go) (Tom Remix)", "Artist",
                "Quicksand (Don't Go)", "Artist",
                "本地多一个Remix括号", false);
        testMatch("Quicksand (Don't Go)", "Artist",
                "Quicksand (Don't Go) (Live)", "Artist",
                "云端多一个Live括号", false);
        testMatch("Song (Part 1) (Extended)", "Artist",
                "Song (Part 1) (Extended)", "Artist",
                "复杂多括号匹配", true);

        // 测试组6: 多歌手处理
        printTestGroup("测试组6: 多歌手处理");
        testMatch("Song Name", "Tom / Jack / Amy",
                "Song Name", "Jack / Tom / Amy",
                "歌手顺序不同", true);
        testMatch("Song Name", "Tom",
                "Song Name", "Tom / Jack",
                "本地1人，云端2人", true);
        testMatch("Song Name", "Tom / Jack / Amy",
                "Song Name", "Tom / Jack",
                "本地3人，云端2人（2人匹配）", true);
        testMatch("Song Name", "Tom / Jack",
                "Song Name", "Amy / Bob",
                "歌手完全不同", false);
        testMatch("Song Name", "Tom (Thomas)",
                "Song Name", "Tom",
                "歌手名带括号（艺名）", true);
        testMatch("Despacito", "Luis Fonsi / Daddy Yankee",
                "Despacito", "Daddy Yankee / Luis Fonsi",
                "西班牙语歌曲多歌手", true);

        // 测试组7: 不匹配情况
        printTestGroup("测试组7: 不匹配情况");
        testMatch("Different Song", "Artist1",
                "Another Song", "Artist2",
                "歌名和歌手都不同", false);
        testMatch("Love Story", "Taylor Swift",
                "Love Song", "Taylor Swift",
                "歌名相似但不同", false);
        testMatch("Hello", "Adele",
                "Hello", "Lionel Richie",
                "同名歌曲不同歌手", false);
        testMatch("Let It Go", "Idina Menzel",
                "Let It Go", "James Bay",
                "同名歌曲不同版本", false);

        // 测试组8: 特殊版本标记
        printTestGroup("测试组8: 特殊版本标记");
        testMatch("Love Story", "Taylor Swift",
                "Love Story (Taylor's Version)", "Taylor Swift",
                "Taylor's Version重录版 - 本地原版", false);
        testMatch("Love Story (Taylor's Version)", "Taylor Swift",
                "Love Story", "Taylor Swift",
                "本地要重录版，云端是原版", false);
        testMatch("Love Story (Taylor's Version)", "Taylor Swift",
                "Love Story (Taylor's Version)", "Taylor Swift",
                "Taylor's Version完全匹配", true);
        testMatch("All Too Well (10 Minute Version)", "Taylor Swift",
                "All Too Well", "Taylor Swift",
                "本地要10分钟版", false);

        // 测试组9: 括号类型不一致
        printTestGroup("测试组9: 括号类型不一致");
        testMatch("Song Name (Remix)", "Artist",
                "Song Name [Remix]", "Artist",
                "圆括号 vs 方括号", true);
        testMatch("Song Name 【Live】", "Artist",
                "Song Name (Live)", "Artist",
                "中文方括号 vs 圆括号", true);
        testMatch("夜に駆ける【向夜晚奔去】", "YOASOBI",
                "夜に駆ける (向夜晚奔去)", "YOASOBI",
                "中文方括号翻译 vs 圆括号翻译", true);
        testMatch("Song「Bonus Track」", "Artist",
                "Song (Bonus Track)", "Artist",
                "日文括号 vs 圆括号", true);

        // 测试组10: 边界情况
        printTestGroup("测试组10: 边界情况");
        testMatch("", "", "Song", "Artist", "本地信息为空", false);
        testMatch("Song", "Artist", "", "", "云端信息为空", false);
        testMatch("Song", "", "Song", "", "只有歌名，无歌手", true);
        testMatch("A", "B", "A", "B", "极短歌名", true);
        testMatch("Song ()", "Artist", "Song", "Artist", "空括号", true);

        // 测试组11: 中文歌曲
        printTestGroup("测试组11: 中文歌曲");
        testMatch("晴天", "周杰伦",
                "晴天", "周杰伦",
                "中文歌曲完全匹配", true);
        testMatch("晴天 (Live)", "周杰伦",
                "晴天", "周杰伦",
                "中文歌曲Live版", false);
        testMatch("稻香", "周杰伦",
                "稻香 (Rice Fragrance)", "周杰伦",
                "中文歌曲带英文翻译", true);
        testMatch("七里香", "周杰伦",
                "七里香 (翻唱)", "Various Artists",
                "翻唱版本检测", false);

        // 测试组12: 日文歌曲
        printTestGroup("测试组12: 日文歌曲");
        testMatch("紅蓮華", "LiSA",
                "紅蓮華 (鬼灭之刃 OP)", "LiSA",
                "日文歌曲带番剧信息", true);
        testMatch("STAY", "BTS",
                "STAY (Japanese Ver.)", "BTS",
                "日文版本", false);
        testMatch("Pretender", "Official髭男dism",
                "Pretender", "Official髭男dism (Official HIGE DANdism)",
                "歌手名带罗马音", true);

        // 测试组13: 特殊字符处理
        printTestGroup("测试组13: 特殊字符和全角处理");
        testMatch("Ｓｈａｐｅ　ｏｆ　Ｙｏｕ", "Ｅｄ　Ｓｈｅｅｒａｎ",
                "Shape of You", "Ed Sheeran",
                "全角转半角", true);
        testMatch("DON'T START NOW", "Dua Lipa",
                "Don't Start Now", "Dua Lipa",
                "大小写不敏感", true);

        // 测试组14: 更多边界情况
        printTestGroup("测试组14: 更多边界情况和特殊场景");
        testMatch("Sugar", "Maroon 5",
                "Sugar", "Maroon 5",
                "普通歌曲完全匹配", true);
        testMatch("Blinding Lights", "The Weeknd",
                "Blinding Lights (Radio Edit)", "The Weeknd",
                "云端是Radio Edit版本", false);
        testMatch("Bad Guy (Remix)", "Billie Eilish",
                "Bad Guy (Live)", "Billie Eilish",
                "Remix vs Live - 不同版本", false);
        testMatch("Dance Monkey", "Tones and I",
                "Dance Monkey (Acoustic Version)", "Tones and I",
                "云端是Acoustic Version", false);
        testMatch("Watermelon Sugar (Explicit)", "Harry Styles",
                "Watermelon Sugar (Explicit)", "Harry Styles",
                "Explicit版本完全匹配", true);
        testMatch("Watermelon Sugar", "Harry Styles",
                "Watermelon Sugar (Explicit)", "Harry Styles",
                "云端有Explicit标识", true);

        // 测试组15: 其它测试用例
        printTestGroup("测试组15: 其它测试用例");
        testMatch("lemon（中文填词）（翻自 米津玄师）", "荔枝odihs",
                "lemon", "荔枝odihs",
                "中文填词+翻唱版", false);
        testMatch("Unity", "The Fat Rat",
                "Unity (Original Mix)", "The Fat Rat",
                "云端有Original Mix标识", true);
        testMatch("等你下课 (feat. 杨瑞代)", "周杰伦",
                "等你下课", "周杰伦 / 杨瑞代",
                "本地有feat标识且本地歌手数量小于云端", true);
        testMatch("等你下课", "周杰伦 / 杨瑞代",
                "等你下课 (feat. 杨瑞代)", "周杰伦",
                "云端有feat标识且本地歌手数量大于云端", true);
        testMatch("普通朋友", "陶喆",
                "普通朋友 (Regular Friends)", "陶喆",
                "中文歌曲带英文翻译", true);
        testMatch("鳥の詩", "Lia",
                "鳥の詩 (游戏《AIR》主题曲：TV 动画《青空》OP1)", "Lia",
                "日文歌曲带出处", true);
        testMatch("Unity (Tom Remix)", "The Fat Rat",
                "Unity (Jack Remix)", "The Fat Rat",
                "不同的人Remix", false);

        return new int[]{passCount, totalCount};
    }

    private static void printTestGroup(String groupName) {
        System.out.println();
        System.out.println("【" + groupName + "】");
        System.out.println("────────────────────────────────────────────────────────────────");
    }

    private static void testMatch(String localTitle, String localArtist,
                                  String cloudTitle, String cloudArtist,
                                  String description, boolean expectMatch) {
        totalCount++;
        int similarity = calculateSimilarity(localTitle, localArtist, cloudTitle, cloudArtist);
        boolean actualMatch = similarity >= EXACT_MATCH_THRESHOLD;
        String result = actualMatch ? "✓ 匹配成功" : "✗ 匹配失败";
        String expectation = expectMatch ? "[期望匹配]" : "[期望不匹配]";
        boolean testPassed = (actualMatch == expectMatch);
        String testResult = testPassed ? "✅" : "❌ 测试失败";

        if (testPassed) {
            passCount++;
        }

        System.out.println("📝 " + description + " " + expectation);
        System.out.println("   本地: [" + localTitle + "] - [" + localArtist + "]");
        System.out.println("   云端: [" + cloudTitle + "] - [" + cloudArtist + "]");
        System.out.println("   相似度: " + String.format("%3d", similarity) + " " + generateScoreBar(similarity) + " " + result + " " + testResult);
        System.out.println();
    }

    private static String generateScoreBar(int score) {
        int filled = score / 10;
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < 10; i++) {
            if (i < filled) {
                sb.append("█");
            } else {
                sb.append("░");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}