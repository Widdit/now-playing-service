import{c as ce,r as a,j as e,u as ue,a as fe,b as pe,d as de,T as me}from"./index-Dacehpz3.js";import{b as z}from"./useDialog-CVUVFbfg.js";import{u as Y,m as X,a as $,b as G,c as K,d as J}from"./chunk-TW2E3XVA-Bnvyx6df.js";import{R as he,M as Q,r as Z,a as ee}from"./index-UWsrisR_.js";import"./vs2015-D9705uCu.js";import{C as D,v as xe}from"./versionCompare-B2m1vOrK.js";import{V as _,S as ve,O as ge,P as ye,a as ne,b as we,M as be,W as je,c as Me,C as Ne}from"./three.module-B_9urBBX.js";import"./useOverlayTriggerState-aQZRu42W.js";import"./chunk-6VC6TS2O-CskcvG1B.js";import"./index-0O0INnlk.js";import"./index-DsLXtDGU.js";import"./chunk-736YWA4T-DCpY4-Mx.js";const Ce=[["path",{d:"M11 6a13 13 0 0 0 8.4-2.8A1 1 0 0 1 21 4v12a1 1 0 0 1-1.6.8A13 13 0 0 0 11 14H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2z",key:"q8bfy3"}],["path",{d:"M6 14a12 12 0 0 0 2.4 7.2 2 2 0 0 0 3.2-2.4A8 8 0 0 1 10 14",key:"1853fq"}],["path",{d:"M8 6v8",key:"15ugcq"}]],Re=ce("megaphone",Ce);const U=8,Ee=`
#define MAX_COLORS ${U}
uniform vec2 uCanvas;
uniform float uTime;
uniform float uSpeed;
uniform vec2 uRot;
uniform int uColorCount;
uniform vec3 uColors[MAX_COLORS];
uniform int uTransparent;
uniform float uScale;
uniform float uFrequency;
uniform float uWarpStrength;
uniform vec2 uPointer; // in NDC [-1,1]
uniform float uMouseInfluence;
uniform float uParallax;
uniform float uNoise;
uniform int uIterations;
uniform float uIntensity;
uniform float uBandWidth;
varying vec2 vUv;

void main() {
  float t = uTime * uSpeed;
  vec2 p = vUv * 2.0 - 1.0;
  p += uPointer * uParallax * 0.1;
  vec2 rp = vec2(p.x * uRot.x - p.y * uRot.y, p.x * uRot.y + p.y * uRot.x);
  vec2 q = vec2(rp.x * (uCanvas.x / uCanvas.y), rp.y);
  q /= max(uScale, 0.0001);
  q /= 0.5 + 0.2 * dot(q, q);
  q += 0.2 * cos(t) - 7.56;
  vec2 toward = (uPointer - rp);
  q += toward * uMouseInfluence * 0.2;

    for (int j = 0; j < 5; j++) {
      if (j >= uIterations - 1) break;
      vec2 rr = sin(1.5 * (q.yx * uFrequency) + 2.0 * cos(q * uFrequency));
      q += (rr - q) * 0.15;
    }

    vec3 col = vec3(0.0);
    float a = 1.0;

    if (uColorCount > 0) {
      vec2 s = q;
      vec3 sumCol = vec3(0.0);
      float cover = 0.0;
      for (int i = 0; i < MAX_COLORS; ++i) {
            if (i >= uColorCount) break;
            s -= 0.01;
            vec2 r = sin(1.5 * (s.yx * uFrequency) + 2.0 * cos(s * uFrequency));
            float m0 = length(r + sin(5.0 * r.y * uFrequency - 3.0 * t + float(i)) / 4.0);
            float kBelow = clamp(uWarpStrength, 0.0, 1.0);
            float kMix = pow(kBelow, 0.3); // strong response across 0..1
            float gain = 1.0 + max(uWarpStrength - 1.0, 0.0); // allow >1 to amplify displacement
            vec2 disp = (r - s) * kBelow;
            vec2 warped = s + disp * gain;
            float m1 = length(warped + sin(5.0 * warped.y * uFrequency - 3.0 * t + float(i)) / 4.0);
            float m = mix(m0, m1, kMix);
            float w = 1.0 - exp(-uBandWidth / exp(uBandWidth * m));
            sumCol += uColors[i] * w;
            cover = max(cover, w);
      }
      col = clamp(sumCol, 0.0, 1.0);
      a = uTransparent > 0 ? cover : 1.0;
    } else {
        vec2 s = q;
        for (int k = 0; k < 3; ++k) {
            s -= 0.01;
            vec2 r = sin(1.5 * (s.yx * uFrequency) + 2.0 * cos(s * uFrequency));
            float m0 = length(r + sin(5.0 * r.y * uFrequency - 3.0 * t + float(k)) / 4.0);
            float kBelow = clamp(uWarpStrength, 0.0, 1.0);
            float kMix = pow(kBelow, 0.3);
            float gain = 1.0 + max(uWarpStrength - 1.0, 0.0);
            vec2 disp = (r - s) * kBelow;
            vec2 warped = s + disp * gain;
            float m1 = length(warped + sin(5.0 * warped.y * uFrequency - 3.0 * t + float(k)) / 4.0);
            float m = mix(m0, m1, kMix);
            col[k] = 1.0 - exp(-uBandWidth / exp(uBandWidth * m));
        }
        a = uTransparent > 0 ? max(max(col.r, col.g), col.b) : 1.0;
    }

    col *= uIntensity;

    if (uNoise > 0.0001) {
      float n = fract(sin(dot(gl_FragCoord.xy + vec2(uTime), vec2(12.9898, 78.233))) * 43758.5453123);
      col += (n - 0.5) * uNoise;
      col = clamp(col, 0.0, 1.0);
    }

    vec3 rgb = (uTransparent > 0) ? col * a : col;
    gl_FragColor = vec4(rgb, a);
}
`,Ie=`
varying vec2 vUv;
void main() {
  vUv = uv;
  gl_Position = vec4(position, 1.0);
}
`;function Se({className:L,style:r,rotation:y=90,speed:u=.2,colors:M=[],transparent:p=!0,autoRotate:N=0,scale:w=1,frequency:C=1,warpStrength:R=1,mouseInfluence:f=1,parallax:E=.5,noise:x=.15,iterations:I=1,intensity:S=1.5,bandWidth:k=6}){const T=a.useRef(null),q=a.useRef(null),b=a.useRef(null),O=a.useRef(null),A=a.useRef(null),m=a.useRef(y),n=a.useRef(N),o=a.useRef(new _(0,0)),se=a.useRef(new _(0,0)),oe=a.useRef(8);return a.useEffect(()=>{const t=T.current,d=new ve,j=new ge(-1,1,1,-1,0,1),h=new ye(2,2),i=Array.from({length:U},()=>new ne(0,0,0)),s=new we({vertexShader:Ie,fragmentShader:Ee,uniforms:{uCanvas:{value:new _(1,1)},uTime:{value:0},uSpeed:{value:u},uRot:{value:new _(1,0)},uColorCount:{value:0},uColors:{value:i},uTransparent:{value:p?1:0},uScale:{value:w},uFrequency:{value:C},uWarpStrength:{value:R},uPointer:{value:new _(0,0)},uMouseInfluence:{value:f},uParallax:{value:E},uNoise:{value:x},uIterations:{value:I},uIntensity:{value:S},uBandWidth:{value:k}},premultipliedAlpha:!0,transparent:!0});O.current=s;const v=new be(h,s);d.add(v);const l=new je({antialias:!1,powerPreference:"high-performance",alpha:!0});q.current=l,l.outputColorSpace=Me,l.setPixelRatio(Math.min(window.devicePixelRatio||1,2)),l.setClearColor(0,p?0:1),l.domElement.style.width="100%",l.domElement.style.height="100%",l.domElement.style.display="block",t.appendChild(l.domElement);const F=new Ne,B=()=>{const g=t.clientWidth||1,P=t.clientHeight||1;l.setSize(g,P,!1),s.uniforms.uCanvas.value.set(g,P)};if(B(),"ResizeObserver"in window){const g=new ResizeObserver(B);g.observe(t),A.current=g}else window.addEventListener("resize",B);const H=()=>{const g=F.getDelta(),P=F.elapsedTime;s.uniforms.uTime.value=P;const W=(m.current%360+n.current*P)*Math.PI/180,ae=Math.cos(W),re=Math.sin(W);s.uniforms.uRot.value.set(ae,re);const V=se.current,le=o.current,ie=Math.min(1,g*oe.current);V.lerp(le,ie),s.uniforms.uPointer.value.copy(V),l.render(d,j),b.current=requestAnimationFrame(H)};return b.current=requestAnimationFrame(H),()=>{b.current!==null&&cancelAnimationFrame(b.current),A.current?A.current.disconnect():window.removeEventListener("resize",B),h.dispose(),s.dispose(),l.dispose(),l.forceContextLoss(),l.domElement&&l.domElement.parentElement===t&&t.removeChild(l.domElement)}},[]),a.useEffect(()=>{const t=O.current,d=q.current;if(!t)return;m.current=y,n.current=N,t.uniforms.uSpeed.value=u,t.uniforms.uScale.value=w,t.uniforms.uFrequency.value=C,t.uniforms.uWarpStrength.value=R,t.uniforms.uMouseInfluence.value=f,t.uniforms.uParallax.value=E,t.uniforms.uNoise.value=x,t.uniforms.uIterations.value=I,t.uniforms.uIntensity.value=S,t.uniforms.uBandWidth.value=k;const j=i=>{const s=i.replace("#","").trim(),v=s.length===3?[parseInt(s[0]+s[0],16),parseInt(s[1]+s[1],16),parseInt(s[2]+s[2],16)]:[parseInt(s.slice(0,2),16),parseInt(s.slice(2,4),16),parseInt(s.slice(4,6),16)];return new ne(v[0]/255,v[1]/255,v[2]/255)},h=(M||[]).filter(Boolean).slice(0,U).map(j);for(let i=0;i<U;i++){const s=t.uniforms.uColors.value[i];i<h.length?s.copy(h[i]):s.set(0,0,0)}t.uniforms.uColorCount.value=h.length,t.uniforms.uTransparent.value=p?1:0,d&&d.setClearColor(0,p?0:1)},[y,N,u,w,C,R,f,E,x,I,S,k,M,p]),a.useEffect(()=>{const t=O.current,d=T.current;if(!t||!d)return;const j=h=>{const i=d.getBoundingClientRect(),s=(h.clientX-i.left)/(i.width||1)*2-1,v=-((h.clientY-i.top)/(i.height||1)*2-1);o.current.set(s,v)};return d.addEventListener("pointermove",j),()=>{d.removeEventListener("pointermove",j)}},[]),e.jsx("div",{ref:T,className:`w-full h-full relative overflow-hidden ${L}`,style:r})}const c={MINUTE:60*1e3,HOUR:3600*1e3,DAY:1440*60*1e3,WEEK:10080*60*1e3,MONTH:720*60*60*1e3,YEAR:365*24*60*60*1e3};function te(L){const r=Date.now()-L;return r<c.MINUTE?"刚刚":r<c.HOUR?`${Math.floor(r/c.MINUTE)} 分钟前`:r<c.DAY?`${Math.floor(r/c.HOUR)} 小时前`:r<c.WEEK?`${Math.floor(r/c.DAY)} 天前`:r<c.MONTH?`${Math.floor(r/c.WEEK)} 星期前`:r<c.YEAR?`${Math.floor(r/c.MONTH)} 个月前`:`${Math.floor(r/c.YEAR)} 年前`}function He(){const{isDesktop:y}=ue(),{openExternalUrl:u}=fe(),[M]=pe(),p=M.has("showAnnouncement"),N=M.has("showUpdate"),[w,C]=a.useState(!1),R=de(),[f,E]=a.useState({current:D,isLatest:!0,latestTimestamp:Date.now()}),[x,I]=a.useState({title:"",timestamp:Date.now(),content:""}),{isOpen:S,onOpen:k,onOpenChange:T}=Y(),{isOpen:q,onOpen:b,onOpenChange:O}=Y();a.useEffect(()=>{if(!p)return;(async()=>{try{let n;{const o=await fetch("/api/announcement");if(!o.ok)throw new Error("公告信息获取失败");n=await o.json()}I({title:n.title,timestamp:n.timestamp,content:n.content,expireDays:n.expireDays}),b()}catch(n){console.error("获取公告信息出错:",n)}})()},[p]),a.useEffect(()=>{if(p&&!w)return;(async()=>{try{let n;{const o=await fetch("/api/version");if(!o.ok)throw new Error("版本信息获取失败");n=await o.json()}if(n.latestVersion){const o=xe(n.latestVersion,D)<=0;E({current:D,latest:n.latestVersion,updateLog:n.updateLog,isLatest:o,latestTimestamp:n.timestamp}),!o&&N&&k()}}catch(n){console.error("获取版本信息出错:",n)}})()},[p,w]);const A=()=>{document.body.classList.add("fade-out"),setTimeout(()=>{R("/settings/general"),setTimeout(()=>{document.body.classList.contains("fade-out")&&window.location.reload()},300)},500)};return e.jsxs(e.Fragment,{children:[y&&e.jsx(me,{autoHide:!1}),e.jsxs("div",{children:[e.jsx("style",{children:`
          :root {
            --button-color: #de40ff;
          }

          body {
            background: #0f0f0f;
          }

          #title {
            font-size: 60px;
            color: white;
            user-select: none;
            line-height: 3rem;
            z-index: 5;
            transform: translateY(-10px);
          }

          #now-playing-text {
            color: #ffffff;
            font-weight: normal;
            margin: 0 0.75rem;
            user-select: none;
          }

          body.fade-out {
            animation: dissolve 0.5s forwards;
          }

          @keyframes dissolve {
            0% {
              filter: blur(0) brightness(1) hue-rotate(0deg) saturate(100%) contrast(100%) drop-shadow(0 0 0 rgba(255, 255, 255, 0));
            }

            25% {
              filter: blur(2px) brightness(1.8) hue-rotate(30deg) saturate(125%) contrast(125%) drop-shadow(0 0 6px #ff00ff);
            }

            50% {
              filter: blur(8px) brightness(2.2) hue-rotate(0deg) saturate(150%) contrast(150%) drop-shadow(0 0 12px #00ffff);
            }

            75% {
              filter: blur(12px) brightness(1.2) hue-rotate(-30deg) saturate(100%) contrast(100%) drop-shadow(0 0 16px #ffff00);
            }

            100% {
              filter: blur(20px) brightness(0) hue-rotate(0deg) saturate(0%) contrast(100%) drop-shadow(0 0 0 rgba(255, 255, 255, 0));
            }
          }

          #current-version-div, #update-text {
            user-select: none;
          }

          /* 动画按钮样式 */
          .animated-button {
            position: relative;
            display: flex;
            align-items: center;
            gap: 4px;
            padding: 16px 36px;
            border: 4px solid;
            border-color: transparent;
            font-size: 16px;
            background-color: inherit;
            border-radius: 100px;
            font-weight: 600;
            color: var(--button-color);
            box-shadow: 0 0 0 2px var(--button-color);
            cursor: pointer;
            overflow: hidden;
            transition: all 0.6s cubic-bezier(0.23, 1, 0.32, 1);
            mix-blend-mode: plus-lighter;
            filter: brightness(2.0) saturate(1.2);
            transform: translateY(-10px);
          }
          .animated-button svg {
            position: absolute;
            width: 24px;
            fill: var(--button-color);
            z-index: 9;
            transition: all 0.8s cubic-bezier(0.23, 1, 0.32, 1);
          }
          .animated-button .arr-1 {
            right: 16px;
          }
          .animated-button .arr-2 {
            left: -25%;
          }
          .animated-button .button-circle {
            position: absolute;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            width: 20px;
            height: 20px;
            background-color: var(--button-color);
            border-radius: 50%;
            opacity: 0;
            transition: all 0.8s cubic-bezier(0.23, 1, 0.32, 1);
          }
          .animated-button .button-text {
            position: relative;
            z-index: 1;
            transform: translateX(-12px);
            transition: all 0.8s cubic-bezier(0.23, 1, 0.32, 1);
          }
          .animated-button:hover {
            box-shadow: 0 0 0 12px transparent;
            color: #0f0f0f;
            border-radius: 100px;
          }
          .animated-button:hover .arr-1 {
            right: -25%;
          }
          .animated-button:hover .arr-2 {
            left: 16px;
          }
          .animated-button:hover .button-text {
            transform: translateX(12px);
          }
          .animated-button:hover svg {
            fill: #0f0f0f;
          }
          .animated-button:active {
            scale: 0.95;
            box-shadow: 0 0 0 4px var(--button-color);
          }
          .animated-button:hover .button-circle {
            width: 220px;
            height: 220px;
            opacity: 1;
          }
        `}),e.jsx("div",{"data-overlay-container":"true",children:e.jsxs("main",{children:[e.jsx("div",{children:e.jsx("div",{className:"absolute bottom-0 top-0 flex h-screen w-full flex-col",children:e.jsx("div",{className:"relative flex flex-col gap-20 text-white md:gap-10",children:e.jsxs("div",{className:"flex h-screen w-full items-center justify-center relative overflow-hidden",children:[e.jsxs("div",{className:"flex flex-col items-center gap-10",id:"main-content",children:[e.jsx("div",{className:"absolute top-0 w-full h-full z-0 bg-[#060010]",id:"bg-container",children:e.jsx(Se,{colors:["#a855f7"],rotation:90,speed:.2,scale:1,frequency:1,warpStrength:1,mouseInfluence:1,noise:.15,parallax:.5,iterations:1,intensity:1.5,bandWidth:6,transparent:!0,autoRotate:0})}),e.jsx("div",{className:"flex items-center justify-between",children:e.jsxs("h2",{className:"inline-block font-sourcehan text-center text-3xl lg:text-4xl md:text-3xl",id:"title",children:["欢迎使用",e.jsx("span",{className:"px-2 font-dela",id:"now-playing-text",children:"Now Playing"}),"服务"]})}),e.jsxs("button",{className:"animated-button",onClick:A,children:[e.jsx("svg",{className:"arr-2",viewBox:"0 0 24 24",xmlns:"http://www.w3.org/2000/svg",children:e.jsx("path",{d:"M16.1716 10.9999L10.8076 5.63589L12.2218 4.22168L20 11.9999L12.2218 19.778L10.8076 18.3638L16.1716 12.9999H4V10.9999H16.1716Z"})}),e.jsx("span",{className:"button-text",children:"前往设置"}),e.jsx("span",{className:"button-circle"}),e.jsx("svg",{className:"arr-1",viewBox:"0 0 24 24",xmlns:"http://www.w3.org/2000/svg",children:e.jsx("path",{d:"M16.1716 10.9999L10.8076 5.63589L12.2218 4.22168L20 11.9999L12.2218 19.778L10.8076 18.3638L16.1716 12.9999H4V10.9999H16.1716Z"})})]})]}),e.jsxs("div",{style:{position:"fixed",bottom:"2.0rem",width:"100%",textAlign:"center"},children:[e.jsxs("div",{className:"font-poppins",id:"current-version-div",style:{marginBottom:"0.2rem"},children:["版本号：",f.current]}),e.jsx("div",{className:"font-poppins",id:"update-text",children:f.isLatest?"当前已是最新版本":f.latest?e.jsxs("a",{className:"cursor-pointer",onClick:()=>{u("https://gitee.com/widdit/now-playing/releases")},children:["检测到新版本可用：",f.latest]}):null})]})]})})})}),e.jsx(X,{size:"xl",isDismissable:!1,scrollBehavior:"inside",hideCloseButton:!0,isOpen:S,onOpenChange:T,className:"px-3 py-2",children:e.jsx($,{className:"font-poppins",children:m=>e.jsxs(e.Fragment,{children:[e.jsxs(G,{className:"flex justify-between items-center",children:[e.jsxs("div",{className:"flex items-center gap-2",children:[e.jsx("div",{className:"breathing-bg flex h-9 w-9 items-center justify-center rounded-full bg-[#15283c]",children:e.jsx(he,{size:20,strokeWidth:2,color:"#0485f7"})}),f.latest," 新版本可用"]}),e.jsx("div",{className:"font-normal text-sm text-default-500",children:te(f.latestTimestamp)})]}),e.jsx(K,{children:e.jsx("div",{className:"markdown-body",children:e.jsx(Q,{rehypePlugins:[Z,ee],components:{img:({node:n,...o})=>e.jsx("img",{...o,referrerPolicy:"no-referrer",className:"max-w-full h-auto rounded-lg my-2"}),a:({node:n,...o})=>e.jsx("a",{...o,className:"text-primary hover:underline",target:"_blank",rel:"noopener noreferrer"})},children:f.updateLog})})}),e.jsxs(J,{children:[e.jsx(z,{color:"default",variant:"flat",onPress:m,children:"取消"}),e.jsx(z,{color:"primary",onPress:()=>{m(),u("https://gitee.com/widdit/now-playing/releases")},children:"确定"})]})]})})}),e.jsx(X,{size:"xl",isDismissable:!1,isKeyboardDismissDisabled:!0,scrollBehavior:"inside",hideCloseButton:!0,isOpen:q,onOpenChange:O,className:"px-3 py-2",children:e.jsx($,{className:"font-poppins",children:m=>e.jsxs(e.Fragment,{children:[e.jsxs(G,{className:"flex justify-between items-center",children:[e.jsxs("div",{className:"flex items-center gap-2",children:[e.jsx("div",{className:"breathing-bg flex h-9 w-9 items-center justify-center rounded-full bg-[#15283c]",children:e.jsx(Re,{size:20,color:"#0485f7"})}),x.title]}),e.jsx("div",{className:"font-normal text-sm text-default-500",children:te(x.timestamp)})]}),e.jsx(K,{children:e.jsx("div",{className:"markdown-body",children:e.jsx(Q,{rehypePlugins:[Z,ee],components:{img:({node:n,...o})=>e.jsx("img",{...o,referrerPolicy:"no-referrer",className:"max-w-full h-auto rounded-lg my-2"}),a:({node:n,...o})=>e.jsx("a",{...o,className:"text-primary hover:underline",target:"_blank",rel:"noopener noreferrer"})},children:x.content})})}),e.jsx(J,{children:e.jsx(z,{color:"primary",onPress:()=>{m(),C(!0)},children:"确定"})})]})})})]})})]})]})}export{He as default};
