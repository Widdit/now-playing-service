import{bh as u,d as g,r as a,j as s}from"./index-Dacehpz3.js";import{D as f,u as p,f as m,U as b,i as h,s as v,L as k,c as w}from"./LyricView-BU6MjKX3.js";function x(){const{profileId:n}=u(),i=g(),[e,o]=a.useState(f),r=p(w),[c,l]=a.useState();a.useEffect(()=>(document.documentElement.classList.add("widget-transparent"),()=>document.documentElement.classList.remove("widget-transparent")),[]),a.useEffect(()=>{if(n&&!new Set(["main","profileA","profileB","profileC","profileD","player","playerMobile","playerWidget"]).has(n)){i("/404",{replace:!0});return}},[n,i]),a.useEffect(()=>{if(!e.backgroundEnabled){l(void 0);return}r?m(r).then(t=>{t&&l(t)}):l(void 0)},[r,e.backgroundEnabled]);const d=a.useCallback(t=>{o(t)},[]);return s.jsxs("div",{id:"player-container",className:"absolute isolate w-full h-full overflow-hidden",style:{opacity:e.opacity,filter:`brightness(${e.brightness}) contrast(${e.contrast}) saturate(${e.saturate})`},children:[e.backgroundEnabled&&s.jsx(b,{className:"absolute inset-0 z-0",album:c,renderer:e.backgroundRenderer==="PixiRenderer"?h:v}),s.jsx("div",{className:"absolute top-0 left-0 w-full h-full",children:s.jsx("div",{id:"lyric-player-wrapper",className:"w-full h-full",style:{mixBlendMode:"plus-lighter",paddingRight:"4%",contain:"paint",maskImage:`linear-gradient(
              transparent,
              black ${e.topFadeRange}%,
              black ${100-e.bottomFadeRange}%,
              transparent
            )`,WebkitMaskImage:`linear-gradient(
              transparent,
              black ${e.topFadeRange}%,
              black ${100-e.bottomFadeRange}%,
              transparent
            )`},children:s.jsx(k,{className:"w-full h-full",profileId:n,alignPosition:e.alignPosition,alignAnchor:e.alignAnchor,onSettingsChange:d})})})]})}export{x as default};
