import{c as je,r as n,j as e,u as Me,a as Ee,b as Ne,d as Ce,T as Ie}from"./index-BQcCveeF.js";import{b as D}from"./useOverlayTriggerState-B3akTpxf.js";import{u as re,m as se,a as ae,b as ie,c as le,d as ce}from"./chunk-TW2E3XVA-jJuHOZb5.js";import{R as Pe,M as ue,r as fe,a as de}from"./index-ossR-uwL.js";import"./vs2015-D9705uCu.js";import{C as U,v as Se}from"./versionCompare-BdJPOTJT.js";import{V as pe,S as Te,O as Ae,W as Oe,a as Le,P as ze,M as _e,C as W,b as F}from"./three.module-CeXYruXl.js";import"./chunk-6VC6TS2O-DoF-pi6W.js";import"./index-P5gYWK4j.js";import"./index-C68UZ9II.js";import"./chunk-736YWA4T-DCpY4-Mx.js";import"./useDialog-kjB8yFSE.js";const ke=[["path",{d:"M11 6a13 13 0 0 0 8.4-2.8A1 1 0 0 1 21 4v12a1 1 0 0 1-1.6.8A13 13 0 0 0 11 14H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2z",key:"q8bfy3"}],["path",{d:"M6 14a12 12 0 0 0 2.4 7.2 2 2 0 0 0 3.2-2.4A8 8 0 0 1 10 14",key:"1853fq"}],["path",{d:"M8 6v8",key:"15ugcq"}]],De=je("megaphone",ke);const Ue=({topColor:y="#5227FF",bottomColor:a="#FF9FFC",intensity:R=1,rotationSpeed:f=.3,interactive:h=!1,className:g="",glowAmount:I=.005,pillarWidth:j=3,pillarHeight:P=.4,noiseIntensity:S=.5,mixBlendMode:d="screen",pillarRotation:M=0,quality:x="high"})=>{const v=n.useRef(null),w=n.useRef(null),c=n.useRef(null),t=n.useRef(null),E=n.useRef(null),N=n.useRef(null),C=n.useRef(null),L=n.useRef(new pe(0,0)),p=n.useRef(0),o=n.useRef(f),[r,H]=n.useState(!0);return n.useEffect(()=>{const s=document.createElement("canvas");s.getContext("webgl")||s.getContext("experimental-webgl")||H(!1)},[]),n.useEffect(()=>{if(!v.current||!r)return;const s=v.current,b=s.clientWidth,m=s.clientHeight,V=/Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent),he=V||navigator.hardwareConcurrency&&navigator.hardwareConcurrency<=4;let T=x;he&&x==="high"&&(T="medium"),V&&x!=="low"&&(T="low");const B={low:{iterations:24,waveIterations:1,pixelRatio:.5,precision:"mediump",stepMultiplier:1.5},medium:{iterations:40,waveIterations:2,pixelRatio:.65,precision:"mediump",stepMultiplier:1.2},high:{iterations:80,waveIterations:4,pixelRatio:Math.min(window.devicePixelRatio,2),precision:"highp",stepMultiplier:1}},A=B[T]||B.medium,Y=new Te;E.current=Y;const xe=new Ae(-1,1,1,-1,0,1);N.current=xe;let O;try{O=new Oe({antialias:!1,alpha:!0,powerPreference:T==="low"?"low-power":"high-performance",precision:A.precision,stencil:!1,depth:!1})}catch(i){console.error("Failed to create WebGL renderer:",i),H(!1);return}O.setSize(b,m),O.setPixelRatio(A.pixelRatio),s.appendChild(O.domElement),c.current=O;const G=i=>{const l=new W(i);return new F(l.r,l.g,l.b)},ve=`
      varying vec2 vUv;
      void main() {
        vUv = uv;
        gl_Position = vec4(position, 1.0);
      }
    `,ge=`
      uniform float uTime;
      uniform vec2 uResolution;
      uniform vec2 uMouse;
      uniform vec3 uTopColor;
      uniform vec3 uBottomColor;
      uniform float uIntensity;
      uniform bool uInteractive;
      uniform float uGlowAmount;
      uniform float uPillarWidth;
      uniform float uPillarHeight;
      uniform float uNoiseIntensity;
      uniform float uPillarRotation;
      uniform float uRotCos;
      uniform float uRotSin;
      uniform float uPillarRotCos;
      uniform float uPillarRotSin;
      uniform float uWaveSin[4];
      uniform float uWaveCos[4];
      varying vec2 vUv;

      const float PI = 3.141592653589793;
      const float EPSILON = 0.001;
      const float E = 2.71828182845904523536;

      float noise(vec2 coord) {
        vec2 r = (E * sin(E * coord));
        return fract(r.x * r.y * (1.0 + coord.x));
      }

      void main() {
        vec2 fragCoord = vUv * uResolution;
        vec2 uv = (fragCoord * 2.0 - uResolution) / uResolution.y;

        // Apply 2D rotation to UV coordinates using pre-computed values
        uv = vec2(
          uv.x * uPillarRotCos - uv.y * uPillarRotSin,
          uv.x * uPillarRotSin + uv.y * uPillarRotCos
        );

        vec3 origin = vec3(0.0, 0.0, -10.0);
        vec3 direction = normalize(vec3(uv, 1.0));

        float maxDepth = 50.0;
        float depth = 0.1;

        // Use pre-computed rotation values (or mouse-based)
        float rotCos = uRotCos;
        float rotSin = uRotSin;
        if(uInteractive && length(uMouse) > 0.0) {
          float mouseAngle = uMouse.x * PI * 2.0;
          rotCos = cos(mouseAngle);
          rotSin = sin(mouseAngle);
        }

        vec3 color = vec3(0.0);

        const int ITERATIONS = ${A.iterations};
        const int WAVE_ITERATIONS = ${A.waveIterations};
        const float STEP_MULT = ${A.stepMultiplier.toFixed(1)};

        for(int i = 0; i < ITERATIONS; i++) {
          vec3 pos = origin + direction * depth;

          // Inline rotation: pos.xz *= rotMat
          float newX = pos.x * rotCos - pos.z * rotSin;
          float newZ = pos.x * rotSin + pos.z * rotCos;
          pos.x = newX;
          pos.z = newZ;

          // Apply vertical scaling and wave deformation
          vec3 deformed = pos;
          deformed.y *= uPillarHeight;
          deformed = deformed + vec3(0.0, uTime, 0.0);

          // Inlined wave deformation
          float frequency = 1.0;
          float amplitude = 1.0;
          for(int j = 0; j < WAVE_ITERATIONS; j++) {
            // Inline rotation: deformed.xz *= rot(0.4) using pre-computed
            float wx = deformed.x * uWaveCos[j] - deformed.z * uWaveSin[j];
            float wz = deformed.x * uWaveSin[j] + deformed.z * uWaveCos[j];
            deformed.x = wx;
            deformed.z = wz;

            float phase = uTime * float(j) * 2.0;
            vec3 oscillation = cos(deformed.zxy * frequency - phase);
            deformed += oscillation * amplitude;
            frequency *= 2.0;
            amplitude *= 0.5;
          }

          // Calculate distance field using cosine pattern
          vec2 cosinePair = cos(deformed.xz);
          float fieldDistance = length(cosinePair) - 0.2;

          // Radial boundary constraint (inlined blendMax)
          float radialBound = length(pos.xz) - uPillarWidth;
          float k = 4.0;
          float h = max(k - abs(-radialBound - (-fieldDistance)), 0.0);
          fieldDistance = -(min(-radialBound, -fieldDistance) - h * h * 0.25 / k);

          fieldDistance = abs(fieldDistance) * 0.15 + 0.01;

          vec3 gradient = mix(uBottomColor, uTopColor, smoothstep(15.0, -15.0, pos.y));
          color += gradient / fieldDistance;

          if(fieldDistance < EPSILON || depth > maxDepth) break;
          depth += fieldDistance * STEP_MULT;
        }

        // Normalize by pillar width to maintain consistent glow regardless of size
        float widthNormalization = uPillarWidth / 3.0;
        color = tanh(color * uGlowAmount / widthNormalization);

        // Add noise postprocessing
        float rnd = noise(gl_FragCoord.xy);
        color -= rnd / 15.0 * uNoiseIntensity;

        gl_FragColor = vec4(color * uIntensity, 1.0);
      }
    `,$=.4,q=new Float32Array(4),X=new Float32Array(4);for(let i=0;i<4;i++)q[i]=Math.sin($),X[i]=Math.cos($);const K=M*Math.PI/180,we=Math.cos(K),be=Math.sin(K),Z=new Le({vertexShader:ve,fragmentShader:ge,uniforms:{uTime:{value:0},uResolution:{value:new pe(b,m)},uMouse:{value:L.current},uTopColor:{value:G(y)},uBottomColor:{value:G(a)},uIntensity:{value:R},uInteractive:{value:h},uGlowAmount:{value:I},uPillarWidth:{value:j},uPillarHeight:{value:P},uNoiseIntensity:{value:S},uPillarRotation:{value:M},uRotCos:{value:1},uRotSin:{value:0},uPillarRotCos:{value:we},uPillarRotSin:{value:be},uWaveSin:{value:q},uWaveCos:{value:X}},transparent:!0,depthWrite:!1,depthTest:!1});t.current=Z;const Q=new ze(2,2);C.current=Q;const ye=new _e(Q,Z);Y.add(ye);let _=null;const J=i=>{if(!h||_)return;_=window.setTimeout(()=>{_=null},16);const l=s.getBoundingClientRect(),z=(i.clientX-l.left)/l.width*2-1,Re=-((i.clientY-l.top)/l.height)*2+1;L.current.set(z,Re)};h&&s.addEventListener("mousemove",J,{passive:!0});let ee=performance.now();const te=1e3/(T==="low"?30:60),ne=i=>{if(!t.current||!c.current||!E.current||!N.current)return;const l=i-ee;if(l>=te){p.current+=.016*o.current,t.current.uniforms.uTime.value=p.current;const z=p.current*.3;t.current.uniforms.uRotCos.value=Math.cos(z),t.current.uniforms.uRotSin.value=Math.sin(z),c.current.render(E.current,N.current),ee=i-l%te}w.current=requestAnimationFrame(ne)};w.current=requestAnimationFrame(ne);let k=null;const oe=()=>{k&&clearTimeout(k),k=window.setTimeout(()=>{if(!c.current||!t.current||!v.current)return;const i=v.current.clientWidth,l=v.current.clientHeight;c.current.setSize(i,l),t.current.uniforms.uResolution.value.set(i,l)},150)};return window.addEventListener("resize",oe,{passive:!0}),()=>{window.removeEventListener("resize",oe),h&&s.removeEventListener("mousemove",J),w.current&&cancelAnimationFrame(w.current),c.current&&(c.current.dispose(),c.current.forceContextLoss(),s.contains(c.current.domElement)&&s.removeChild(c.current.domElement)),t.current&&t.current.dispose(),C.current&&C.current.dispose(),c.current=null,t.current=null,E.current=null,N.current=null,C.current=null,w.current=null}},[r,x]),n.useEffect(()=>{o.current=f},[f]),n.useEffect(()=>{if(!t.current)return;const s=b=>{const m=new W(b);return new F(m.r,m.g,m.b)};t.current.uniforms.uTopColor.value=s(y)},[y]),n.useEffect(()=>{if(!t.current)return;const s=b=>{const m=new W(b);return new F(m.r,m.g,m.b)};t.current.uniforms.uBottomColor.value=s(a)},[a]),n.useEffect(()=>{t.current&&(t.current.uniforms.uIntensity.value=R)},[R]),n.useEffect(()=>{t.current&&(t.current.uniforms.uInteractive.value=h)},[h]),n.useEffect(()=>{t.current&&(t.current.uniforms.uGlowAmount.value=I)},[I]),n.useEffect(()=>{t.current&&(t.current.uniforms.uPillarWidth.value=j)},[j]),n.useEffect(()=>{t.current&&(t.current.uniforms.uPillarHeight.value=P)},[P]),n.useEffect(()=>{t.current&&(t.current.uniforms.uNoiseIntensity.value=S)},[S]),n.useEffect(()=>{if(!t.current)return;const s=M*Math.PI/180;t.current.uniforms.uPillarRotCos.value=Math.cos(s),t.current.uniforms.uPillarRotSin.value=Math.sin(s)},[M]),r?e.jsx("div",{ref:v,className:`w-full h-full absolute top-0 left-0 ${g}`,style:{mixBlendMode:d}}):e.jsx("div",{className:`w-full h-full absolute top-0 left-0 flex items-center justify-center bg-black/10 text-gray-500 text-sm ${g}`,style:{mixBlendMode:d},children:"WebGL not supported"})},u={MINUTE:60*1e3,HOUR:3600*1e3,DAY:1440*60*1e3,WEEK:10080*60*1e3,MONTH:720*60*60*1e3,YEAR:365*24*60*60*1e3};function me(y){const a=Date.now()-y;return a<u.MINUTE?"刚刚":a<u.HOUR?`${Math.floor(a/u.MINUTE)} 分钟前`:a<u.DAY?`${Math.floor(a/u.HOUR)} 小时前`:a<u.WEEK?`${Math.floor(a/u.DAY)} 天前`:a<u.MONTH?`${Math.floor(a/u.WEEK)} 星期前`:a<u.YEAR?`${Math.floor(a/u.MONTH)} 个月前`:`${Math.floor(a/u.YEAR)} 年前`}function Je(){const{isDesktop:R}=Me(),{openExternalUrl:f}=Ee(),[h]=Ne(),g=h.has("showAnnouncement"),I=h.has("showUpdate"),[j,P]=n.useState(!1),S=Ce(),[d,M]=n.useState({current:U,isLatest:!0,latestTimestamp:Date.now()}),[x,v]=n.useState({title:"",timestamp:Date.now(),content:""}),{isOpen:w,onOpen:c,onOpenChange:t}=re(),{isOpen:E,onOpen:N,onOpenChange:C}=re();n.useEffect(()=>{if(!g)return;(async()=>{try{let o;{const r=await fetch("/api/announcement");if(!r.ok)throw new Error("公告信息获取失败");o=await r.json()}v({title:o.title,timestamp:o.timestamp,content:o.content}),N()}catch(o){console.error("获取公告信息出错:",o)}})()},[g]),n.useEffect(()=>{if(g&&!j)return;(async()=>{try{let o;{const r=await fetch("/api/version");if(!r.ok)throw new Error("版本信息获取失败");o=await r.json()}if(o.latestVersion){const r=Se(o.latestVersion,U)<=0;M({current:U,latest:o.latestVersion,updateLog:o.updateLog,isLatest:r,latestTimestamp:o.timestamp}),!r&&I&&c()}}catch(o){console.error("获取版本信息出错:",o)}})()},[g,j]);const L=()=>{document.body.classList.add("fade-out"),setTimeout(()=>{S("/settings/general"),setTimeout(()=>{document.body.classList.contains("fade-out")&&window.location.reload()},300)},500)};return e.jsxs(e.Fragment,{children:[R&&e.jsx(Ie,{autoHide:!1}),e.jsxs("div",{children:[e.jsx("style",{children:`
          :root {
            --button-color: #4055ff;
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
        `}),e.jsx("div",{"data-overlay-container":"true",children:e.jsxs("main",{children:[e.jsx("div",{children:e.jsx("div",{className:"absolute bottom-0 top-0 flex h-screen w-full flex-col",children:e.jsx("div",{className:"relative flex flex-col gap-20 text-white md:gap-10",children:e.jsxs("div",{className:"flex h-screen w-full items-center justify-center relative overflow-hidden",children:[e.jsxs("div",{className:"flex flex-col items-center gap-10",id:"main-content",children:[e.jsx("div",{className:"absolute top-0 w-full h-full pointer-events-none z-0 bg-[#060010]",id:"bg-container",children:e.jsx(Ue,{topColor:"#5227FF",bottomColor:"#FF9FFC",intensity:1.4,rotationSpeed:.3,glowAmount:.002,pillarWidth:3,pillarHeight:.4,noiseIntensity:.5,pillarRotation:25,interactive:!1,mixBlendMode:"screen",quality:"high"})}),e.jsx("div",{className:"flex items-center justify-between",children:e.jsxs("h2",{className:"inline-block font-sourcehan text-center text-3xl lg:text-4xl md:text-3xl",id:"title",children:["欢迎使用",e.jsx("span",{className:"px-2 font-dela",id:"now-playing-text",children:"Now Playing"}),"服务"]})}),e.jsxs("button",{className:"animated-button",onClick:L,children:[e.jsx("svg",{className:"arr-2",viewBox:"0 0 24 24",xmlns:"http://www.w3.org/2000/svg",children:e.jsx("path",{d:"M16.1716 10.9999L10.8076 5.63589L12.2218 4.22168L20 11.9999L12.2218 19.778L10.8076 18.3638L16.1716 12.9999H4V10.9999H16.1716Z"})}),e.jsx("span",{className:"button-text",children:"前往设置"}),e.jsx("span",{className:"button-circle"}),e.jsx("svg",{className:"arr-1",viewBox:"0 0 24 24",xmlns:"http://www.w3.org/2000/svg",children:e.jsx("path",{d:"M16.1716 10.9999L10.8076 5.63589L12.2218 4.22168L20 11.9999L12.2218 19.778L10.8076 18.3638L16.1716 12.9999H4V10.9999H16.1716Z"})})]})]}),e.jsxs("div",{style:{position:"fixed",bottom:"2.0rem",width:"100%",textAlign:"center"},children:[e.jsxs("div",{className:"font-poppins",id:"current-version-div",style:{marginBottom:"0.2rem"},children:["版本号：",d.current]}),e.jsx("div",{className:"font-poppins",id:"update-text",children:d.isLatest?"当前已是最新版本":d.latest?e.jsxs("a",{className:"cursor-pointer",onClick:()=>{f("https://gitee.com/widdit/now-playing/releases")},children:["检测到新版本可用：",d.latest]}):null})]})]})})})}),e.jsx(se,{size:"xl",isDismissable:!1,scrollBehavior:"inside",hideCloseButton:!0,isOpen:w,onOpenChange:t,className:"px-3 py-2",children:e.jsx(ae,{className:"font-poppins",children:p=>e.jsxs(e.Fragment,{children:[e.jsxs(ie,{className:"flex justify-between items-center",children:[e.jsxs("div",{className:"flex items-center gap-2",children:[e.jsx("div",{className:"breathing-bg flex h-9 w-9 items-center justify-center rounded-full bg-[#15283c]",children:e.jsx(Pe,{size:20,strokeWidth:2,color:"#0485f7"})}),d.latest," 新版本可用"]}),e.jsx("div",{className:"font-normal text-sm text-default-500",children:me(d.latestTimestamp)})]}),e.jsx(le,{children:e.jsx("div",{className:"markdown-body",children:e.jsx(ue,{rehypePlugins:[fe,de],components:{img:({node:o,...r})=>e.jsx("img",{...r,referrerPolicy:"no-referrer",className:"max-w-full h-auto rounded-lg my-2"}),a:({node:o,...r})=>e.jsx("a",{...r,className:"text-primary hover:underline",target:"_blank",rel:"noopener noreferrer"})},children:d.updateLog})})}),e.jsxs(ce,{children:[e.jsx(D,{color:"default",variant:"flat",onPress:p,children:"取消"}),e.jsx(D,{color:"primary",onPress:()=>{p(),f("https://gitee.com/widdit/now-playing/releases")},children:"确定"})]})]})})}),e.jsx(se,{size:"xl",isDismissable:!1,isKeyboardDismissDisabled:!0,scrollBehavior:"inside",hideCloseButton:!0,isOpen:E,onOpenChange:C,className:"px-3 py-2",children:e.jsx(ae,{className:"font-poppins",children:p=>e.jsxs(e.Fragment,{children:[e.jsxs(ie,{className:"flex justify-between items-center",children:[e.jsxs("div",{className:"flex items-center gap-2",children:[e.jsx("div",{className:"breathing-bg flex h-9 w-9 items-center justify-center rounded-full bg-[#15283c]",children:e.jsx(De,{size:20,color:"#0485f7"})}),x.title]}),e.jsx("div",{className:"font-normal text-sm text-default-500",children:me(x.timestamp)})]}),e.jsx(le,{children:e.jsx("div",{className:"markdown-body",children:e.jsx(ue,{rehypePlugins:[fe,de],components:{img:({node:o,...r})=>e.jsx("img",{...r,referrerPolicy:"no-referrer",className:"max-w-full h-auto rounded-lg my-2"}),a:({node:o,...r})=>e.jsx("a",{...r,className:"text-primary hover:underline",target:"_blank",rel:"noopener noreferrer"})},children:x.content})})}),e.jsx(ce,{children:e.jsx(D,{color:"primary",onPress:()=>{p(),P(!0)},children:"确定"})})]})})})]})})]})]})}export{Je as default};
