// ========================================
// 可见性设置
// ========================================

const VISIBILITY_SETTINGS = {
  // 暂停时隐藏组件
  PAUSE_HIDE: false,
  // 隐藏延迟时间（秒）
  HIDE_DELAY: 2,
  
  // 仅切歌时显示
  CUT_ONLY: false,
  // 切歌展示时长（秒）
  CUT_SHOW_TIME: 5
};

// ========================================
// 组件动画设置
// ========================================

// 切歌动画配置（先缩小再放大）
const SONG_CHANGE_ANIMATION = {
  SHRINK_RATIO: 0.5,                               // 缩小比例
  SHRINK_DURATION: 600,                            // 缩小阶段时长 (ms)
  EXPAND_DURATION: 600,                            // 放大阶段时长 (ms)
  EASING: 'cubic-bezier(0.25, 0.8, 0.25, 1)'       // 缓动曲线
};

// 隐藏/显示动画配置
const VISIBILITY_ANIMATION = {
  HIDE_SHRINK_RATIO: 0.5,                          // 隐藏时的缩小比例
  SHOW_START_RATIO: 0.5,                           // 显示时的起始比例
  SCALE_DURATION: 600,                             // 缩放动画时长 (ms)
  OPACITY_DURATION: 300,                           // 透明度动画时长 (ms)
  EASING: 'cubic-bezier(0.25, 0.8, 0.25, 1)'       // 缓动曲线
};

// 切歌动画内容补偿配置：使文字/封面缩放幅度更温和，同时保持整体 rem 缩放的流畅感
const SONG_CHANGE_CONTENT_COMPENSATION = {
  TEXT_VISUAL_SHRINK_RATIO: 0.78,                  // 缩小时文字的视觉缩放比例
  COVER_VISUAL_SHRINK_RATIO: 0.75,                 // 缩小时封面的视觉缩放比例
  MAX_COMPENSATE_SCALE: 5                          // 最大补偿倍数
};

// ========================================
// 其它设置
// ========================================

// API 轮询频率 (ms)
const POLL_INTERVAL = 200;

// 波形动画更新间隔 (ms)
const WAVEFORM_UPDATE_INTERVAL = 700;

// 无歌曲播放时的默认显示内容
const DEFAULTS = {
  TITLE: "Nothing Playing",
  ARTIST: "Get the music started",
  COVER: "/assets/spotify_no_cover-a90a617b.svg"
};

// ========================================
// 全局状态
// ========================================

let currentCoverUrl = '';
let currentTitleStr = '';
let currentArtistStr = '';
let isPausedGlobal = true;
let coverImageObj = null;
let isInitialized = false;

// 可见性状态
let isPlayerVisible = true;
let hideTimer = null;                // 暂停隐藏延迟计时器
let cutShowTimer = null;             // 切歌显示计时器
let isVisibilityAnimating = false;   // 显示/隐藏动画进行中标记

// 切歌动画状态
let songChangeAnimationTimer = null;
let isSongChangeAnimating = false;

// 当前响应式计算的根字号（用于切歌时整体缩放）
let currentBaseFontSizePx = 16;

// ========================================
// DOM 元素引用
// ========================================

const dom = {
  player: document.getElementById('player'),
  titleContainer: document.getElementById('track-title'),
  titleScroller: document.getElementById('title-scroller'),
  artist: document.getElementById('track-artist'),
  cover: document.getElementById('album-cover'),
  timeCurrent: document.getElementById('time-current'),
  timeRemaining: document.getElementById('time-remaining'),
  progressFill: document.getElementById('progress-fill'),
  playPauseBtn: document.getElementById('play-pause-btn'),
  waveformCanvas: document.getElementById('waveform-canvas'),
  prevBtn: document.getElementById('prev-btn'),
  nextBtn: document.getElementById('next-btn')
};

const ctx = dom.waveformCanvas.getContext('2d');

// 静态资源路径
const ASSETS = {
  PLAY: 'assets/icon_play.svg',
  PAUSE: 'assets/icon_pause.svg'
};

// ========================================
// 波形动画 Spring 物理模型
// ========================================

const NUM_BARS = 6;
const springs = [];

for (let i = 0; i < NUM_BARS; i++) {
  springs.push({
    height: 0.3,
    target: 0.3,
    velocity: 0,
    tension: 2.5,
    friction: 2.5
  });
}

// 波形更新时间戳，控制所有柱子同步变化
let nextGlobalUpdate = Date.now();

// ========================================
// 响应式布局
// ========================================

/**
 * 根据窗口尺寸调整组件缩放
 * 通过修改根元素 font-size 实现 rem 单位的响应式缩放
 */
function handleResize() {
  const vw = window.innerWidth;
  const vh = window.innerHeight;
  
  const componentWidth = 28;
  const componentHeight = 13;
  
  // 分别计算基于宽度/高度的最大字号
  const sizeBasedOnWidth = vw / componentWidth;
  const sizeBasedOnHeight = vh / componentHeight;
  
  let baseSize = Math.min(sizeBasedOnWidth, sizeBasedOnHeight);
  
  // 留白系数，避免组件贴近屏幕边缘
  const scaleRatio = 0.8; 
  baseSize = baseSize * scaleRatio;

  // 限制字号范围
  baseSize = Math.max(12, Math.min(baseSize, 80));

  currentBaseFontSizePx = baseSize;

  // 动画进行中时不覆盖 font-size，避免打断过渡效果
  if (!isSongChangeAnimating && !isVisibilityAnimating && isPlayerVisible) {
    document.documentElement.style.fontSize = `${baseSize}px`;
  }
  
  if (typeof checkTitleScroll === 'function') {
    checkTitleScroll(true);
  }
}

window.addEventListener('resize', handleResize);
handleResize();

// ========================================
// 工具函数
// ========================================

/**
 * 将秒数格式化为 m:ss 格式
 * @param {number} seconds - 秒数
 * @returns {string} 格式化后的时间字符串
 */
function formatTime(seconds) {
  if (isNaN(seconds)) return '0:00';
  const m = Math.floor(Math.abs(seconds) / 60);
  const s = Math.floor(Math.abs(seconds) % 60);
  return `${m}:${s.toString().padStart(2, '0')}`;
}

/**
 * 通过后端 API 获取封面图的 Base64 编码
 * @param {string} url - 封面图 URL
 * @returns {Promise<string|null>} Base64 编码的图片数据
 */
async function fetchCoverBase64(url) {
  try {
    const response = await fetch('/api/cover/convert', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ cover_url: url })
    });
    const data = await response.json();
    return data.base64Img;
  } catch (error) {
    console.error('Failed to convert cover:', error);
    return null;
  }
}

/**
 * 加载图片并返回 Image 对象
 * @param {string} src - 图片源地址
 * @returns {Promise<HTMLImageElement|null>} 加载完成的图片对象
 */
function loadCoverImage(src) {
  return new Promise((resolve) => {
    const img = new Image();
    img.crossOrigin = 'anonymous';
    img.onload = () => resolve(img);
    img.onerror = () => resolve(null);
    img.src = src;
  });
}

// ========================================
// 动画辅助函数
// ========================================

/**
 * 查找多个目标元素的公共祖先
 * @param {HTMLElement} startEl - 起始元素
 * @param {...HTMLElement} targets - 目标元素列表
 * @returns {HTMLElement|null} 公共祖先元素
 */
function findCommonAncestor(startEl, ...targets) {
  if (!startEl) return null;
  const maxDepth = 8;

  let node = startEl;
  for (let i = 0; i < maxDepth && node; i++) {
    if (node === dom.player || node === document.body || node === document.documentElement) {
      return null;
    }
    const ok = targets.every(t => t && node.contains(t));
    if (ok) return node;
    node = node.parentElement;
  }
  return null;
}

/**
 * 获取参与动画的所有元素分类
 * @returns {Object} 按类型分组的元素集合
 */
function getAnimationElements() {
  const playPauseScaleTarget = dom.playPauseBtn ? (dom.playPauseBtn.parentElement || dom.playPauseBtn) : null;
  const progressTrackEl = dom.progressFill ? dom.progressFill.parentElement : null;

  const progressGroupEl = (dom.timeCurrent && progressTrackEl && dom.timeRemaining)
    ? findCommonAncestor(dom.timeCurrent, progressTrackEl, dom.timeRemaining)
    : null;

  const controlsGroupEl = (playPauseScaleTarget && dom.prevBtn && dom.nextBtn)
    ? findCommonAncestor(playPauseScaleTarget, dom.prevBtn, dom.nextBtn)
    : null;

  const safeControlsGroupEl = (controlsGroupEl && (
    (dom.timeCurrent && controlsGroupEl.contains(dom.timeCurrent)) ||
    (dom.timeRemaining && controlsGroupEl.contains(dom.timeRemaining)) ||
    (progressTrackEl && controlsGroupEl.contains(progressTrackEl))
  )) ? null : controlsGroupEl;

  const compensateTextEls = [dom.titleContainer, dom.artist, dom.timeCurrent, dom.timeRemaining].filter(Boolean);
  const compensateCoverEls = [dom.waveformCanvas, dom.cover].filter(Boolean);
  
  const compensateUiEls = [safeControlsGroupEl, progressTrackEl].filter(Boolean);
  const fallbackUiEls = [];
  if (!safeControlsGroupEl) {
    if (playPauseScaleTarget) fallbackUiEls.push(playPauseScaleTarget);
    if (dom.prevBtn) fallbackUiEls.push(dom.prevBtn);
    if (dom.nextBtn) fallbackUiEls.push(dom.nextBtn);
  }

  const compensateTransformEls = Array.from(new Set([
    ...compensateCoverEls,
    ...compensateUiEls,
    ...fallbackUiEls
  ]));

  const compensateAllEls = Array.from(new Set([
    ...compensateTextEls,
    ...compensateTransformEls,
    ...(progressGroupEl ? [progressGroupEl] : [])
  ]));

  return {
    compensateTextEls,
    compensateTransformEls,
    compensateAllEls,
    progressTrackEl,
    progressGroupEl
  };
}

/**
 * 缓存元素的原始内联样式
 * @param {HTMLElement} el - 目标元素
 */
function cacheOriginalInlineStyles(el) {
  if (el.dataset.animCached === '1') return;
  el.dataset.animCached = '1';
  el.dataset.animOrigTransition = el.style.transition || '';
  el.dataset.animOrigTransform = el.style.transform || '';
  el.dataset.animOrigTransformOrigin = el.style.transformOrigin || '';
  el.dataset.animOrigWillChange = el.style.willChange || '';

  el.dataset.animOrigFontSize = el.style.fontSize || '';
  el.dataset.animOrigLineHeight = el.style.lineHeight || '';
  el.dataset.animOrigHeight = el.style.height || '';
  el.dataset.animOrigMaxHeight = el.style.maxHeight || '';
  el.dataset.animOrigMarginTop = el.style.marginTop || '';

  el.dataset.animOrigFlex = el.style.flex || '';
  el.dataset.animOrigFlexShrink = el.style.flexShrink || '';
  el.dataset.animOrigMinWidth = el.style.minWidth || '';
  el.dataset.animOrigWhiteSpace = el.style.whiteSpace || '';
  el.dataset.animOrigOverflow = el.style.overflow || '';
}

/**
 * 恢复元素的原始内联样式
 * @param {HTMLElement} el - 目标元素
 */
function restoreOriginalInlineStyles(el) {
  if (el.dataset.animCached !== '1') return;
  el.style.transition = el.dataset.animOrigTransition;
  el.style.transform = el.dataset.animOrigTransform;
  el.style.transformOrigin = el.dataset.animOrigTransformOrigin;
  el.style.willChange = el.dataset.animOrigWillChange;

  el.style.fontSize = el.dataset.animOrigFontSize;
  el.style.lineHeight = el.dataset.animOrigLineHeight;
  el.style.height = el.dataset.animOrigHeight;
  el.style.maxHeight = el.dataset.animOrigMaxHeight;
  el.style.marginTop = el.dataset.animOrigMarginTop;

  el.style.flex = el.dataset.animOrigFlex;
  el.style.flexShrink = el.dataset.animOrigFlexShrink;
  el.style.minWidth = el.dataset.animOrigMinWidth;
  el.style.whiteSpace = el.dataset.animOrigWhiteSpace;
  el.style.overflow = el.dataset.animOrigOverflow;
  
  // 清除缓存标记，允许下次重新缓存
  delete el.dataset.animCached;
}

/**
 * 准备元素的缩放动画样式（设置 flex、transformOrigin 等，防止布局异常）
 * @param {Object} elements - 元素分组对象
 * @param {Map} normalTextFontSizePxMap - 文字元素的正常字号映射
 */
function prepareElementsForScaling(elements, normalTextFontSizePxMap) {
  const { compensateTextEls, compensateTransformEls, compensateAllEls, progressTrackEl, progressGroupEl } = elements;

  // 缓存样式并移除 transition，避免设置准备样式时触发动画
  for (const el of compensateAllEls) {
    cacheOriginalInlineStyles(el);
    el.style.transition = 'none'; 
  }

  if (progressGroupEl) progressGroupEl.style.overflow = 'visible';

  // 文字元素准备
  for (const el of compensateTextEls) {
    const normalFontPx = normalTextFontSizePxMap.get(el) || 0;
    if (normalFontPx > 0) el.style.fontSize = `${normalFontPx}px`;
    
    el.style.willChange = 'font-size';
    el.style.lineHeight = '1.2';
    el.style.height = 'auto';
    el.style.maxHeight = 'none';

    if (el === dom.timeCurrent || el === dom.timeRemaining) {
      el.style.flex = '0 0 auto';
      el.style.whiteSpace = 'nowrap';
      el.style.flexShrink = '0';
    }
  }

  if (progressTrackEl) {
    progressTrackEl.style.flex = '1 1 auto';
    progressTrackEl.style.minWidth = '0px';
  }

  // Transform 元素准备
  for (const el of compensateTransformEls) {
    el.style.willChange = 'transform';
    if (el === dom.timeCurrent) el.style.transformOrigin = 'left center';
    else if (el === dom.timeRemaining) el.style.transformOrigin = 'right center';
    else el.style.transformOrigin = 'center';

    if (progressTrackEl && el === progressTrackEl) el.style.transform = 'scale(1, 1)';
    else el.style.transform = 'scale(1)';
  }
}

/**
 * 应用缩放补偿样式
 * @param {Object} elements - 元素分组对象
 * @param {number} targetFontSizePx - 目标字号
 * @param {number} shrinkRatio - 缩小比例
 * @param {Map} normalTextFontSizePxMap - 文字元素的正常字号映射
 */
function applyScaleCompensation(elements, targetFontSizePx, shrinkRatio, normalTextFontSizePxMap) {
  const { compensateTextEls, compensateTransformEls, progressTrackEl } = elements;

  const shrinkFontSizePx = Math.max(1, targetFontSizePx * shrinkRatio);
  const actualShrinkRatio = shrinkFontSizePx / targetFontSizePx;

  const textVisualRatio = Math.max(
    actualShrinkRatio,
    Math.min(
      SONG_CHANGE_CONTENT_COMPENSATION.TEXT_VISUAL_SHRINK_RATIO,
      actualShrinkRatio * SONG_CHANGE_CONTENT_COMPENSATION.MAX_COMPENSATE_SCALE
    )
  );

  const uiVisualRatio = Math.max(
    actualShrinkRatio,
    Math.min(
      SONG_CHANGE_CONTENT_COMPENSATION.COVER_VISUAL_SHRINK_RATIO,
      actualShrinkRatio * SONG_CHANGE_CONTENT_COMPENSATION.MAX_COMPENSATE_SCALE
    )
  );
  const uiCompScale = uiVisualRatio / actualShrinkRatio;

  // 应用根元素字号
  document.documentElement.style.fontSize = `${shrinkFontSizePx}px`;

  // 应用文字补偿
  for (const el of compensateTextEls) {
    const normalFontPx = normalTextFontSizePxMap.get(el) || 0;
    if (normalFontPx > 0) {
      if (el === dom.timeCurrent || el === dom.timeRemaining) {
        el.style.fontSize = `${Math.max(8, normalFontPx * textVisualRatio)}px`;
      } else {
        el.style.fontSize = `${normalFontPx * textVisualRatio}px`;
      }
    }
  }

  if (dom.artist) dom.artist.style.marginTop = '0.2em';

  // 应用 Transform 补偿
  for (const el of compensateTransformEls) {
    if (progressTrackEl && el === progressTrackEl) {
      el.style.transform = `scale(1, ${uiCompScale})`;
    } else {
      el.style.transform = `scale(${uiCompScale})`;
    }
  }
}

// ========================================
// 隐藏/显示动画逻辑
// ========================================

/**
 * 执行隐藏动画：缩小 -> 消失
 */
function performHideAnimation() {
  if (isVisibilityAnimating || !isPlayerVisible) return;
  isVisibilityAnimating = true;

  // 取消可能残留的 WAAPI 动画，避免其继续占用 opacity 导致无法淡出
  if (typeof dom.player.getAnimations === 'function') {
    dom.player.getAnimations().forEach(a => a.cancel());
  }

  // 准备阶段
  const htmlEl = document.documentElement;
  const targetFontSizePx = currentBaseFontSizePx;
  const elements = getAnimationElements();
  
  // 获取当前实际字号
  const normalTextFontSizePxMap = new Map();
  for (const el of elements.compensateTextEls) {
    normalTextFontSizePxMap.set(el, parseFloat(getComputedStyle(el).fontSize) || 0);
  }

  // 暂停 font-size 过渡
  const originalHtmlTransition = htmlEl.style.transition;
  htmlEl.style.transition = 'none';
  prepareElementsForScaling(elements, normalTextFontSizePxMap);
  
  // 强制回流
  void htmlEl.offsetHeight;

  // 缩小动画阶段
  const duration = VISIBILITY_ANIMATION.SCALE_DURATION;
  const easing = VISIBILITY_ANIMATION.EASING;

  htmlEl.style.transition = `font-size ${duration}ms ${easing}`;
  for (const el of elements.compensateTextEls) {
    el.style.transition = `font-size ${duration}ms ${easing}`;
  }
  for (const el of elements.compensateTransformEls) {
    el.style.transition = `transform ${duration}ms ${easing}`;
  }

  requestAnimationFrame(() => {
    applyScaleCompensation(elements, targetFontSizePx, VISIBILITY_ANIMATION.HIDE_SHRINK_RATIO, normalTextFontSizePxMap);
  });

  // 消失阶段（透明度变化）
  setTimeout(() => {
    dom.player.style.opacity = '0';
    
    // 等待透明度过渡完成
    setTimeout(() => {
      // 清理样式
      htmlEl.style.transition = originalHtmlTransition;
      for (const el of elements.compensateAllEls) {
        restoreOriginalInlineStyles(el);
      }
      
      // 恢复 font-size（组件已隐藏，恢复是为了 resize 计算正常）
      htmlEl.style.fontSize = `${targetFontSizePx}px`;
      
      isPlayerVisible = false;
      isVisibilityAnimating = false;
    }, VISIBILITY_ANIMATION.OPACITY_DURATION);

  }, duration);
}

/**
 * 执行显示动画：出现 -> 放大
 */
function performShowAnimation() {
  if (isVisibilityAnimating || isPlayerVisible) return;
  isVisibilityAnimating = true;

  const htmlEl = document.documentElement;
  const playerEl = dom.player;
  const targetFontSizePx = currentBaseFontSizePx;
  const elements = getAnimationElements();
  
  // 获取标准字号（此时 opacity 为 0，但 display 不是 none，依然可以获取计算样式）
  const normalTextFontSizePxMap = new Map();
  for (const el of elements.compensateTextEls) {
    normalTextFontSizePxMap.set(el, parseFloat(getComputedStyle(el).fontSize) || 0);
  }

  const originalHtmlTransition = htmlEl.style.transition;
  
  // ============================================
  // 阶段 1：初始化状态（瞬间完成，无动画）
  // ============================================
  
  // 禁用所有过渡
  htmlEl.style.transition = 'none'; 
  playerEl.style.transition = 'none'; 

  // 确保初始状态是透明的
  playerEl.style.opacity = '0';

  // 准备缩小状态的样式
  prepareElementsForScaling(elements, normalTextFontSizePxMap);
  
  // 应用缩小状态（此时 font-size 变小，rem 布局变小）
  applyScaleCompensation(elements, targetFontSizePx, VISIBILITY_ANIMATION.SHOW_START_RATIO, normalTextFontSizePxMap);

  // 强制浏览器回流，让浏览器立刻计算出"小尺寸、透明"的布局
  void htmlEl.offsetHeight; 
  void playerEl.offsetHeight;

  // 取消可能存在的历史 WAAPI 动画（避免叠加导致时长异常或占用 opacity）
  if (typeof playerEl.getAnimations === 'function') {
    playerEl.getAnimations().forEach(a => a.cancel());
  }

  // ============================================
  // 阶段 2：淡入动画（WAAPI）
  // 使用 fill: 'none' 避免动画持续占用 opacity 导致下一次 hide 无法淡出
  // ============================================
  let finished = false;
  const fade = playerEl.animate(
    [{ opacity: 0 }, { opacity: 1 }],
    {
      duration: VISIBILITY_ANIMATION.OPACITY_DURATION,
      easing: 'linear',
      fill: 'none'
    }
  );

  fade.onfinish = () => {
    finished = true;

    // 固化最终值到内联样式（之后交给 CSS transition 控制淡出）
    playerEl.style.opacity = '1';

    // 立即 cancel 清掉动画效果层，避免后续 opacity 被它覆盖
    fade.cancel();

    // ============================================
    // 阶段 3：放大动画（等待淡入完成后执行）
    // ============================================
    const duration = VISIBILITY_ANIMATION.SCALE_DURATION;
    const easing = VISIBILITY_ANIMATION.EASING;

    // 恢复 html 的 font-size 过渡
    htmlEl.style.transition = `font-size ${duration}ms ${easing}`;
    
    // 恢复容器的过渡（opacity 交给 CSS 默认值 + 内联值变化控制）
    playerEl.style.transition = `width ${duration}ms ${easing}, height ${duration}ms ${easing}`;

    // 设置文字和 Transform 元素的过渡
    for (const el of elements.compensateTextEls) {
      el.style.transition = `font-size ${duration}ms ${easing}`;
    }
    for (const el of elements.compensateTransformEls) {
      el.style.transition = `transform ${duration}ms ${easing}`;
    }

    // 执行放大逻辑
    requestAnimationFrame(() => {
      // 恢复到正常大小（rem 基准恢复）
      htmlEl.style.fontSize = `${targetFontSizePx}px`;

      // 恢复文字大小补偿
      for (const el of elements.compensateTextEls) {
        const normalFontPx = normalTextFontSizePxMap.get(el) || 0;
        if (normalFontPx > 0) el.style.fontSize = `${normalFontPx}px`;
      }
      if (dom.artist) dom.artist.style.marginTop = '';
      
      // 恢复 Transform 补偿
      for (const el of elements.compensateTransformEls) {
        if (elements.progressTrackEl && el === elements.progressTrackEl) {
          el.style.transform = 'scale(1, 1)';
        } else {
          el.style.transform = 'scale(1)';
        }
      }
    });

    // ============================================
    // 阶段 4：清理工作（动画彻底结束后）
    // ============================================
    setTimeout(() => {
      htmlEl.style.transition = originalHtmlTransition;
      playerEl.style.transition = '';
      
      for (const el of elements.compensateAllEls) {
        restoreOriginalInlineStyles(el);
      }
      isPlayerVisible = true;
      isVisibilityAnimating = false;
      
      if (typeof checkTitleScroll === 'function') {
        checkTitleScroll(true);
      }
    }, duration);
  };

  fade.oncancel = () => {
    // 如果是 onfinish 里主动 cancel 的，不做状态回滚
    if (finished) return;

    // 其它情况下确保不会卡死在 animating 状态
    isVisibilityAnimating = false;
  };
}

// ========================================
// 切歌动画
// ========================================

/**
 * 触发切歌动画（缩小 -> 放大）
 */
function triggerSongChangeAnimation() {
  if (songChangeAnimationTimer) {
    clearTimeout(songChangeAnimationTimer);
    songChangeAnimationTimer = null;
  }
  isSongChangeAnimating = true;

  const htmlEl = document.documentElement;
  const targetFontSizePx = currentBaseFontSizePx;
  const elements = getAnimationElements();
  
  const normalTextFontSizePxMap = new Map();
  for (const el of elements.compensateTextEls) {
    normalTextFontSizePxMap.set(el, parseFloat(getComputedStyle(el).fontSize) || 0);
  }

  const originalHtmlTransition = htmlEl.style.transition;
  htmlEl.style.transition = 'none';
  htmlEl.style.fontSize = `${targetFontSizePx}px`;

  prepareElementsForScaling(elements, normalTextFontSizePxMap);
  void htmlEl.offsetHeight;

  // 第一段：缩小
  const shrinkDuration = SONG_CHANGE_ANIMATION.SHRINK_DURATION;
  const expandDuration = SONG_CHANGE_ANIMATION.EXPAND_DURATION;
  const easing = SONG_CHANGE_ANIMATION.EASING;

  htmlEl.style.transition = `font-size ${shrinkDuration}ms ${easing}`;
  for (const el of elements.compensateTextEls) el.style.transition = `font-size ${shrinkDuration}ms ${easing}`;
  for (const el of elements.compensateTransformEls) el.style.transition = `transform ${shrinkDuration}ms ${easing}`;

  requestAnimationFrame(() => {
    applyScaleCompensation(elements, targetFontSizePx, SONG_CHANGE_ANIMATION.SHRINK_RATIO, normalTextFontSizePxMap);
  });

  songChangeAnimationTimer = setTimeout(() => {
    // 第二段：放大
    htmlEl.style.transition = `font-size ${expandDuration}ms ${easing}`;
    for (const el of elements.compensateTextEls) el.style.transition = `font-size ${expandDuration}ms ${easing}`;
    for (const el of elements.compensateTransformEls) el.style.transition = `transform ${expandDuration}ms ${easing}`;

    requestAnimationFrame(() => {
      htmlEl.style.fontSize = `${targetFontSizePx}px`;
      for (const el of elements.compensateTextEls) {
        const px = normalTextFontSizePxMap.get(el) || 0;
        if(px > 0) el.style.fontSize = `${px}px`;
      }
      if (dom.artist) dom.artist.style.marginTop = '';
      for (const el of elements.compensateTransformEls) {
        if (elements.progressTrackEl && el === elements.progressTrackEl) el.style.transform = 'scale(1, 1)';
        else el.style.transform = 'scale(1)';
      }
    });

    songChangeAnimationTimer = setTimeout(() => {
      htmlEl.style.transition = originalHtmlTransition;
      for (const el of elements.compensateAllEls) restoreOriginalInlineStyles(el);
      isSongChangeAnimating = false;
      songChangeAnimationTimer = null;
      if (typeof checkTitleScroll === 'function') checkTitleScroll(true);
    }, expandDuration);
  }, shrinkDuration);
}

// ========================================
// 波形动画 - 物理计算与渲染
// ========================================

/**
 * 使用 Spring 物理模型更新波形柱状图高度
 * @param {number} dt - 时间增量（秒）
 */
function updateSprings(dt) {
  const now = Date.now();

  if (!isPausedGlobal) {
    // 播放状态：定时更新所有柱子的目标高度
    if (now > nextGlobalUpdate) {
      for (let i = 0; i < NUM_BARS; i++) {
        springs[i].target = 0.2 + Math.random() * 0.6;
      }
      nextGlobalUpdate = now + WAVEFORM_UPDATE_INTERVAL;
    }
  } else {
    // 暂停状态：所有柱子回归静止高度
    for (let i = 0; i < NUM_BARS; i++) {
      springs[i].target = 0.2;
    }
  }

  // 欧拉积分计算弹簧运动
  for (let i = 0; i < NUM_BARS; i++) {
    const spring = springs[i];

    const displacement = spring.height - spring.target;
    const force = -spring.tension * displacement;
    const damping = -spring.friction * spring.velocity;
    const acceleration = force + damping;

    spring.velocity += acceleration * dt;
    spring.height += spring.velocity * dt;

    // 限制高度范围
    spring.height = Math.max(0.05, Math.min(1.0, spring.height));
  }
}

/**
 * Canvas 绘制循环
 * 将封面图以柱状波形遮罩的形式绘制
 */
function drawWaveform() {
  const canvas = dom.waveformCanvas;
  const rect = canvas.getBoundingClientRect();

  if (rect.width === 0 || rect.height === 0) {
    requestAnimationFrame(drawWaveform);
    return;
  }

  const dpr = window.devicePixelRatio || 1;
  const w = Math.round(rect.width * dpr);
  const h = Math.round(rect.height * dpr);

  if (canvas.width !== w || canvas.height !== h) {
    canvas.width = w;
    canvas.height = h;
  }

  ctx.clearRect(0, 0, w, h);

  updateSprings(0.016);

  if (!coverImageObj) {
    requestAnimationFrame(drawWaveform);
    return;
  }

  // 计算柱状图参数
  const barGapRatio = 0.5;
  const totalBarWidth = w / NUM_BARS;
  const gap = totalBarWidth * barGapRatio;
  const barWidth = totalBarWidth - gap;

  ctx.save();

  // 绘制圆角矩形遮罩路径
  ctx.beginPath();
  for (let i = 0; i < NUM_BARS; i++) {
    const x = i * totalBarWidth + gap / 2;
    const barH = springs[i].height * h;
    const y = (h - barH) / 2;
    const radius = Math.min(barWidth / 2, barH / 2);

    ctx.moveTo(x + radius, y);
    ctx.lineTo(x + barWidth - radius, y);
    ctx.quadraticCurveTo(x + barWidth, y, x + barWidth, y + radius);
    ctx.lineTo(x + barWidth, y + barH - radius);
    ctx.quadraticCurveTo(x + barWidth, y + barH, x + barWidth - radius, y + barH);
    ctx.lineTo(x + radius, y + barH);
    ctx.quadraticCurveTo(x, y + barH, x, y + barH - radius);
    ctx.lineTo(x, y + radius);
    ctx.quadraticCurveTo(x, y, x + radius, y);
  }
  ctx.clip();

  // 居中裁切绘制封面图
  const imgAspect = coverImageObj.width / coverImageObj.height;
  const canvasAspect = w / h;
  let sx, sy, sw, sh;

  if (imgAspect > canvasAspect) {
    sh = coverImageObj.height;
    sw = sh * canvasAspect;
    sx = (coverImageObj.width - sw) / 2;
    sy = 0;
  } else {
    sw = coverImageObj.width;
    sh = sw / canvasAspect;
    sx = 0;
    sy = (coverImageObj.height - sh) / 2;
  }

  ctx.drawImage(coverImageObj, sx, sy, sw, sh, 0, 0, w, h);

  ctx.restore();

  requestAnimationFrame(drawWaveform);
}

requestAnimationFrame(drawWaveform);

// ========================================
// 播放/暂停按钮动画
// ========================================

/**
 * 切换播放/暂停图标，带缩放动画
 * @param {boolean} isPaused - 当前是否暂停
 */
function updatePlayPauseIcon(isPaused) {
  const targetSrc = isPaused ? ASSETS.PLAY : ASSETS.PAUSE;
  const btn = dom.playPauseBtn;

  // 如果图标已正确，无需更新
  if (btn.src.includes(isPaused ? 'icon_play.svg' : 'icon_pause.svg')) return;

  btn.classList.add('animating');
  setTimeout(() => {
    btn.src = targetSrc;
    btn.classList.remove('animating');
  }, 200);
}

// ========================================
// 歌名滚动
// ========================================

/**
 * 检查并设置歌名滚动动画
 * 当歌名超出容器宽度时启用无限滚动
 * @param {boolean} force - 是否强制重新检查
 */
function checkTitleScroll(force = false) {
  if (!force && dom.titleScroller.dataset.text === currentTitleStr) return;

  const container = dom.titleContainer;
  const scroller = dom.titleScroller;

  scroller.innerHTML = `<span>${currentTitleStr}</span>`;
  scroller.classList.remove('animate');
  container.classList.remove('is-scrolling');
  scroller.style.animationDuration = '0s';
  scroller.dataset.text = currentTitleStr;

  const containerWidth = container.clientWidth;
  const textSpan = scroller.querySelector('span');
  const textWidth = textSpan.offsetWidth;

  if (textWidth > containerWidth) {
    // 克隆文字用于无缝滚动
    const clone = textSpan.cloneNode(true);
    scroller.appendChild(clone);

    // 根据文字宽度计算滚动时长
    const speed = 25; // px/s
    const duration = textWidth / speed;

    scroller.style.animationDuration = `${duration}s`;
    scroller.classList.add('animate');
    container.classList.add('is-scrolling');
  }
}

// ========================================
// 数据更新与 UI 同步
// ========================================

/**
 * 处理可见性逻辑
 * @param {boolean} isPaused - 是否暂停
 * @param {boolean} isSongChanged - 是否切歌
 */
function handleVisibility(isPaused, isSongChanged) {
  // 模式 1：仅切歌时显示
  if (VISIBILITY_SETTINGS.CUT_ONLY) {
    if (isSongChanged) {
      // 切歌时显示组件
      if (!isPlayerVisible) {
        performShowAnimation();
      }
      
      // 重置隐藏倒计时
      if (cutShowTimer) clearTimeout(cutShowTimer);
      cutShowTimer = setTimeout(() => {
        performHideAnimation();
      }, VISIBILITY_SETTINGS.CUT_SHOW_TIME * 1000);
    }
    // 如果刚初始化且设置了仅切歌显示，默认保持隐藏（除非触发切歌）
    if (isPlayerVisible && !isSongChanged && !cutShowTimer && !isVisibilityAnimating) {
      // 页面刚加载且没有触发切歌，这里不做额外处理，等待下一次切歌
    }
    return; 
  }

  // 模式 2：暂停时隐藏
  if (VISIBILITY_SETTINGS.PAUSE_HIDE) {
    if (isPaused) {
      // 暂停状态：设置延迟隐藏
      if (isPlayerVisible && !hideTimer && !isVisibilityAnimating) {
        const delayMs = Math.max(0, VISIBILITY_SETTINGS.HIDE_DELAY * 1000);
        hideTimer = setTimeout(() => {
          performHideAnimation();
          hideTimer = null;
        }, delayMs);
      }
    } else {
      // 播放状态：取消隐藏计时器并显示
      if (hideTimer) {
        clearTimeout(hideTimer);
        hideTimer = null;
      }
      if (!isPlayerVisible) {
        performShowAnimation();
      }
    }
  } else {
    // 默认模式：始终显示
    if (!isPlayerVisible) {
      performShowAnimation();
    }
  }
}

/**
 * 根据 API 返回的数据更新界面
 * @param {Object|null} data - API 返回的播放状态数据
 */
async function updateUI(data) {
  const hasData = data && data.player && data.player.hasSong && data.track && data.track.title;

  const player = hasData ? data.player : { isPaused: true, seekbarCurrentPosition: 0 };
  const track = hasData ? data.track : {};

  const displayTitle = hasData ? track.title : DEFAULTS.TITLE;
  const displayArtist = hasData ? track.author : DEFAULTS.ARTIST;
  let displayCoverUrl = hasData ? track.cover : DEFAULTS.COVER;

  isPausedGlobal = player.isPaused;

  // 检测是否切歌
  const isSongChanged = displayTitle !== currentTitleStr || displayArtist !== currentArtistStr;

  // 更新文字内容
  if (displayTitle !== currentTitleStr) {
    currentTitleStr = displayTitle;
    dom.titleContainer.title = displayTitle;
    setTimeout(() => checkTitleScroll(true), 0);
  }

  if (displayArtist !== currentArtistStr) {
    currentArtistStr = displayArtist;
    dom.artist.textContent = displayArtist;
    dom.artist.title = displayArtist;
  }

  // 处理可见性逻辑（决定是否显示/隐藏）
  handleVisibility(player.isPaused, isSongChanged);

  // 处理切歌动画（仅当组件保持可见时播放弹跳动画）
  // 如果组件刚从隐藏变为显示，显示动画本身已包含放大过程，无需再触发切歌动画
  if (isSongChanged) {
    if (isInitialized) {
      // 当前可见且不在进行显示动画，则播放切歌动画
      if (isPlayerVisible && !isVisibilityAnimating) {
        triggerSongChangeAnimation();
      }
    }
    isInitialized = true;
  }

  // 更新封面图
  if (displayCoverUrl !== currentCoverUrl) {
    currentCoverUrl = displayCoverUrl;

    let imgSourceToLoad = null;
    if (hasData && displayCoverUrl) {
      const base64 = await fetchCoverBase64(displayCoverUrl);
      if (base64) imgSourceToLoad = base64;
    } else if (!hasData) {
      imgSourceToLoad = displayCoverUrl;
    }

    if (imgSourceToLoad) {
      dom.cover.classList.remove('loaded');
      dom.cover.src = imgSourceToLoad;
      coverImageObj = await loadCoverImage(imgSourceToLoad);
      if (coverImageObj) {
        dom.cover.classList.add('loaded');
      }
    }
  }

  updatePlayPauseIcon(player.isPaused);

  // 更新进度条和时间
  const current = player.seekbarCurrentPosition || 0;
  const duration = track.duration || 0;
  const remaining = current - duration;

  dom.timeCurrent.textContent = formatTime(current);
  dom.timeRemaining.textContent = (duration > 0 && remaining !== 0 ? '-' : '') + formatTime(remaining);

  const progressPercent = duration > 0 ? (current / duration) * 100 : 0;
  dom.progressFill.style.width = `${progressPercent}%`;
}

// ========================================
// 数据轮询
// ========================================

/**
 * 定期从后端获取播放状态
 */
async function pollData() {
  try {
    const response = await fetch('/api/query');
    if (response.ok) {
      const data = await response.json();
      await updateUI(data);
    } else {
      await updateUI(null);
    }
  } catch (error) {
    console.error('Polling error:', error);
    await updateUI(null);
  }
}

// 启动轮询
setInterval(pollData, POLL_INTERVAL);
pollData();
