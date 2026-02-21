import{c as li,j as p,r as z,R as ci,u as ui,a as fi,b as di,d as mi,T as vi}from"./index-Ct6JBOBo.js";import{C as Lt,U as Ke,M as gi,D as pi,u as be,a as Rt,B as Qe,O as Je,F as ve,b as hi,S as qe,V as nt,W as Ft,R as _i,L as me,E as xi,c as yi,d as M,P as Ci,e as bi,f as Ei,H as Ce,g as Re,h as we,i as It,j as Q,k as Ee,l as Ut,m as Ti,n as wi,o as Pi,p as zi,q as Pe,r as Ai,s as Di,t as Si,v as Bi,Q as at,w as Oi,x as Ni,y as Li}from"./react-three-fiber.esm-DqFpvbEb.js";import{b as Fe}from"./useOverlayTriggerState-BZ7zSRD6.js";import{u as ot,m as st,a as lt,b as ct,c as ut,d as ft}from"./chunk-TW2E3XVA-Bp5e4cmh.js";import{R as Ri,M as dt,r as mt,a as vt}from"./index-BOgBFTZL.js";import"./vs2015-D9705uCu.js";import{C as Ie,v as Fi}from"./versionCompare-BkPOv2mn.js";import"./chunk-6VC6TS2O-C-xcEz88.js";import"./index-4FLVpdB1.js";import"./index-DD6QnZ3t.js";import"./chunk-736YWA4T-DCpY4-Mx.js";import"./useDialog-BJauXF91.js";const Ii=[["path",{d:"M11 6a13 13 0 0 0 8.4-2.8A1 1 0 0 1 21 4v12a1 1 0 0 1-1.6.8A13 13 0 0 0 11 14H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2z",key:"q8bfy3"}],["path",{d:"M6 14a12 12 0 0 0 2.4 7.2 2 2 0 0 0 3.2-2.4A8 8 0 0 1 10 14",key:"1853fq"}],["path",{d:"M8 6v8",key:"15ugcq"}]],Ui=li("megaphone",Ii);var ze=192;function Mi({type:r}){return p.jsxs(p.Fragment,{children:[r==="plane"&&p.jsx("planeGeometry",{args:[10,10,1,ze]}),r==="sphere"&&p.jsx("icosahedronGeometry",{args:[1,ze/3]}),r==="waterPlane"&&p.jsx("planeGeometry",{args:[10,10,ze,ze]})]})}function ki(r){let e=/^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(r);return e?{r:parseInt(e[1],16),g:parseInt(e[2],16),b:parseInt(e[3],16)}:null}function Hi(r){let e=r.match(/^rgb\((\d+),\s*(\d+),\s*(\d+)\)$/);return e?{r:parseInt(e[1]),g:parseInt(e[2]),b:parseInt(e[3])}:null}function de(r){if(r.startsWith("#"))return ki(r);if(r.startsWith("rgb"))return Hi(r);throw new Error("Invalid color format")}function K(r=0){return r/255}var ji=Object.create,Ne=Object.defineProperty,Vi=Object.defineProperties,Yi=Object.getOwnPropertyDescriptor,$i=Object.getOwnPropertyDescriptors,Gi=Object.getOwnPropertyNames,Oe=Object.getOwnPropertySymbols,Wi=Object.getPrototypeOf,et=Object.prototype.hasOwnProperty,Mt=Object.prototype.propertyIsEnumerable,gt=(r,e,t)=>e in r?Ne(r,e,{enumerable:!0,configurable:!0,writable:!0,value:t}):r[e]=t,L=(r,e)=>{for(var t in e||(e={}))et.call(e,t)&&gt(r,t,e[t]);if(Oe)for(var t of Oe(e))Mt.call(e,t)&&gt(r,t,e[t]);return r},ge=(r,e)=>Vi(r,$i(e)),ne=(r,e)=>{var t={};for(var n in r)et.call(r,n)&&e.indexOf(n)<0&&(t[n]=r[n]);if(r!=null&&Oe)for(var n of Oe(r))e.indexOf(n)<0&&Mt.call(r,n)&&(t[n]=r[n]);return t},Te=(r,e)=>()=>(e||r((e={exports:{}}).exports,e),e.exports),U=(r,e)=>{for(var t in e)Ne(r,t,{get:e[t],enumerable:!0})},qi=(r,e,t,n)=>{if(e&&typeof e=="object"||typeof e=="function")for(let s of Gi(e))!et.call(r,s)&&s!==t&&Ne(r,s,{get:()=>e[s],enumerable:!(n=Yi(e,s))||n.enumerable});return r},Zi=(r,e,t)=>(t=r!=null?ji(Wi(r)):{},qi(!r||!r.__esModule?Ne(t,"default",{value:r,enumerable:!0}):t,r)),Xi=({animate:r,range:e,rangeStart:t,rangeEnd:n,loop:s,loopDuration:o,reflection:f,uniforms:_,vertexShader:c,fragmentShader:v,onInit:g,shader:b})=>{let w=z.useRef(new Lt),C=z.useMemo(()=>{let P=Object.entries(_),E=_.colors,x=de(E[0]),d=de(E[1]),i=de(E[2]),a={uC1r:{value:K(x?.r)},uC1g:{value:K(x?.g)},uC1b:{value:K(x?.b)},uC2r:{value:K(d?.r)},uC2g:{value:K(d?.g)},uC2b:{value:K(d?.b)},uC3r:{value:K(i?.r)},uC3g:{value:K(i?.g)},uC3b:{value:K(i?.b)}},m=P.reduce((y,[T,S])=>{let V=Ke.clone({[T]:{value:S}});return L(L({},y),V)},{}),u={userData:m,metalness:b==="glass"?0:.2,roughness:b==="glass"?.1:1-(typeof f=="number"?f:.1),side:pi,onBeforeCompile:y=>{y.uniforms=L(L(L({},y.uniforms),m),a),y.vertexShader=c,y.fragmentShader=v}};b==="glass"&&(u.transparent=!0,u.opacity=.3,u.transmission=.9,u.thickness=.5,u.clearcoat=1,u.clearcoatRoughness=0,u.ior=1.5,u.envMapIntensity=1);let l=new gi(u);return P.forEach(([y])=>Object.defineProperty(l,y,{get:()=>l.uniforms[y].value,set:T=>l.uniforms[y].value=T})),g&&g(l),l},[_,c,v,g,f,b]);return z.useEffect(()=>()=>{C.dispose()},[C]),z.useEffect(()=>{r==="on"?w.current.start():w.current.stop()},[r]),be(()=>{if(r==="on"&&C.userData.uTime){let P=w.current.getElapsedTime();s==="on"&&Number.isFinite(o)&&o>0?(P=P%o,C.userData.uLoop&&(C.userData.uLoop.value=1),C.userData.uLoopDuration&&(C.userData.uLoopDuration.value=o)):(C.userData.uLoop&&(C.userData.uLoop.value=0),e==="enabled"&&Number.isFinite(t)&&Number.isFinite(n)&&n>t&&(P=t+P,P>=n&&(P=t,w.current.start()))),C.userData.uTime.value=P}}),p.jsx("primitive",{attach:"material",object:C})},kt={};U(kt,{fragment:()=>Ki,vertex:()=>Qi});var Ki=`
#define STANDARD
#ifdef PHYSICAL
#define REFLECTIVITY
#define CLEARCOAT
#define TRANSMISSION
#endif

uniform vec3 diffuse;
uniform vec3 emissive;
uniform float roughness;
uniform float metalness;
uniform float opacity;

#ifdef TRANSMISSION
uniform float transmission;
#endif
#ifdef REFLECTIVITY
uniform float reflectivity;
#endif
#ifdef CLEARCOAT
uniform float clearcoat;
uniform float clearcoatRoughness;
#endif
#ifdef USE_SHEEN
uniform vec3 sheen;
#endif
varying vec3 vViewPosition;
#ifndef FLAT_SHADED
#ifdef USE_TANGENT
varying vec3 vTangent;
varying vec3 vBitangent;
#endif
#endif
#include <alphamap_pars_fragment>
#include <aomap_pars_fragment>
#include <color_pars_fragment>
#include <common>
#include <dithering_pars_fragment>
#include <emissivemap_pars_fragment>
#include <lightmap_pars_fragment>
#include <map_pars_fragment>
#include <packing>
#include <uv2_pars_fragment>
#include <uv_pars_fragment>
// #include <transmissionmap_pars_fragment>
#include <bsdfs>
#include <bumpmap_pars_fragment>
#include <clearcoat_pars_fragment>
#include <clipping_planes_pars_fragment>
#include <cube_uv_reflection_fragment>
#include <envmap_common_pars_fragment>
#include <envmap_physical_pars_fragment>
#include <fog_pars_fragment>
#include <lights_pars_begin>
#include <lights_physical_pars_fragment>
#include <logdepthbuf_pars_fragment>
#include <metalnessmap_pars_fragment>
#include <normalmap_pars_fragment>
#include <roughnessmap_pars_fragment>
#include <shadowmap_pars_fragment>
// include를 통해 가져온 값은 대부분 환경, 빛 등을 계산하기 위해서 기본 fragment
// shader의 값들을 받아왔습니다. 일단은 무시하셔도 됩니다.

varying vec3 vNormal;
varying float displacement;
varying vec3 vPos;
varying float vDistort;

uniform float uC1r;
uniform float uC1g;
uniform float uC1b;
uniform float uC2r;
uniform float uC2g;
uniform float uC2b;
uniform float uC3r;
uniform float uC3g;
uniform float uC3b;

varying vec3 color1;
varying vec3 color2;
varying vec3 color3;

// for npm package, need to add this manually
float linearToRelativeLuminance2( const in vec3 color ) {
    vec3 weights = vec3( 0.2126, 0.7152, 0.0722 );
    return dot( weights, color.rgb );
}

void main() {

  //-------- basic gradient ------------
  vec3 color1 = vec3(uC1r, uC1g, uC1b);
  vec3 color2 = vec3(uC2r, uC2g, uC2b);
  vec3 color3 = vec3(uC3r, uC3g, uC3b);
  float clearcoat = 1.0;
  float clearcoatRoughness = 0.5;

  #include <clipping_planes_fragment>

  vec4 diffuseColor = vec4(
      mix(mix(color1, color2, smoothstep(-3.0, 3.0, vPos.x)), color3, vPos.z),
      1);
  // diffuseColor는 오브젝트의 베이스 색상 (환경이나 빛이 고려되지 않은 본연의
  // 색)

  // mix(x, y, a): a를 축으로 했을 때 가장 낮은 값에서 x값의 영향력을 100%, 가장
  // 높은 값에서 y값의 영향력을 100%로 만든다. smoothstep(x, y, a): a축을
  // 기준으로 x를 최소값, y를 최대값으로 그 사이의 값을 쪼갠다. x와 y 사이를
  // 0-100 사이의 그라디언트처럼 단계별로 표현하고, x 미만의 값은 0, y 이상의
  // 값은 100으로 처리

  // 1. smoothstep(-3.0, 3.0,vPos.x)로 x축의 그라디언트가 표현 될 범위를 -3,
  // 3으로 정한다.
  // 2. mix(color1, color3, smoothstep(-3.0, 3.0,vPos.x))로 color1과 color3을
  // 위의 범위 안에서 그라디언트로 표현한다.
  // 예를 들어 color1이 노랑, color3이 파랑이라고 치면, x축 기준 -3부터 3까지
  // 노랑과 파랑 사이의 그라디언트가 나타나고, -3보다 작은 값에서는 계속 노랑,
  // 3보다 큰 값에서는 계속 파랑이 나타난다.
  // 3. mix()를 한 번 더 사용해서 위의 그라디언트와 color2를 z축 기준으로
  // 분배한다.

  //-------- materiality ------------
  ReflectedLight reflectedLight =
      ReflectedLight(vec3(0.0), vec3(0.0), vec3(0.0), vec3(0.0));
  vec3 totalEmissiveRadiance = emissive;

  #ifdef TRANSMISSION
    float totalTransmission = transmission;
  #endif
  #include <logdepthbuf_fragment>
  #include <map_fragment>
  #include <color_fragment>
  #include <alphamap_fragment>
  #include <alphatest_fragment>
  #include <roughnessmap_fragment>
  #include <metalnessmap_fragment>
  #include <normal_fragment_begin>
  #include <normal_fragment_maps>
  #include <clearcoat_normal_fragment_begin>
  #include <clearcoat_normal_fragment_maps>
  #include <emissivemap_fragment>
  // #include <transmissionmap_fragment>
  #include <lights_physical_fragment>
  #include <lights_fragment_begin>
  #include <lights_fragment_maps>
  #include <lights_fragment_end>
  #include <aomap_fragment>
    vec3 outgoingLight =
        reflectedLight.directDiffuse + reflectedLight.indirectDiffuse +
        reflectedLight.directSpecular + reflectedLight.indirectSpecular;
    //위에서 정의한 diffuseColor에 환경이나 반사값들을 반영한 값.
  #ifdef TRANSMISSION
    diffuseColor.a *=
        mix(saturate(1. - totalTransmission +
                    linearToRelativeLuminance2(reflectedLight.directSpecular +
                                              reflectedLight.indirectSpecular)),
            1.0, metalness);
  #endif


  #include <tonemapping_fragment>
  #include <encodings_fragment>
  #include <fog_fragment>
  #include <premultiplied_alpha_fragment>
  #include <dithering_fragment>


  gl_FragColor = vec4(outgoingLight, diffuseColor.a);
  // gl_FragColor가 fragment shader를 통해 나타나는 최종값으로, diffuseColor에서
  // 정의한 그라디언트 색상 위에 반사나 빛을 계산한 값을 최종값으로 정의.
  // gl_FragColor = vec4(mix(mix(color1, color3, smoothstep(-3.0, 3.0,vPos.x)),
  // color2, vNormal.z), 1.0); 위처럼 최종값을 그라디언트 값 자체를 넣으면 환경
  // 영향없는 그라디언트만 표현됨.
}
`,Qi=`// #pragma glslify: cnoise3 = require(glsl-noise/classic/3d) 

// noise source from https://github.com/hughsk/glsl-noise/blob/master/periodic/3d.glsl

vec3 mod289(vec3 x)
{
  return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec4 mod289(vec4 x)
{
  return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec4 permute(vec4 x)
{
  return mod289(((x*34.0)+1.0)*x);
}

vec4 taylorInvSqrt(vec4 r)
{
  return 1.79284291400159 - 0.85373472095314 * r;
}

vec3 fade(vec3 t) {
  return t*t*t*(t*(t*6.0-15.0)+10.0);
}

float cnoise(vec3 P)
{
  vec3 Pi0 = floor(P); // Integer part for indexing
  vec3 Pi1 = Pi0 + vec3(1.0); // Integer part + 1
  Pi0 = mod289(Pi0);
  Pi1 = mod289(Pi1);
  vec3 Pf0 = fract(P); // Fractional part for interpolation
  vec3 Pf1 = Pf0 - vec3(1.0); // Fractional part - 1.0
  vec4 ix = vec4(Pi0.x, Pi1.x, Pi0.x, Pi1.x);
  vec4 iy = vec4(Pi0.yy, Pi1.yy);
  vec4 iz0 = Pi0.zzzz;
  vec4 iz1 = Pi1.zzzz;

  vec4 ixy = permute(permute(ix) + iy);
  vec4 ixy0 = permute(ixy + iz0);
  vec4 ixy1 = permute(ixy + iz1);

  vec4 gx0 = ixy0 * (1.0 / 7.0);
  vec4 gy0 = fract(floor(gx0) * (1.0 / 7.0)) - 0.5;
  gx0 = fract(gx0);
  vec4 gz0 = vec4(0.5) - abs(gx0) - abs(gy0);
  vec4 sz0 = step(gz0, vec4(0.0));
  gx0 -= sz0 * (step(0.0, gx0) - 0.5);
  gy0 -= sz0 * (step(0.0, gy0) - 0.5);

  vec4 gx1 = ixy1 * (1.0 / 7.0);
  vec4 gy1 = fract(floor(gx1) * (1.0 / 7.0)) - 0.5;
  gx1 = fract(gx1);
  vec4 gz1 = vec4(0.5) - abs(gx1) - abs(gy1);
  vec4 sz1 = step(gz1, vec4(0.0));
  gx1 -= sz1 * (step(0.0, gx1) - 0.5);
  gy1 -= sz1 * (step(0.0, gy1) - 0.5);

  vec3 g000 = vec3(gx0.x,gy0.x,gz0.x);
  vec3 g100 = vec3(gx0.y,gy0.y,gz0.y);
  vec3 g010 = vec3(gx0.z,gy0.z,gz0.z);
  vec3 g110 = vec3(gx0.w,gy0.w,gz0.w);
  vec3 g001 = vec3(gx1.x,gy1.x,gz1.x);
  vec3 g101 = vec3(gx1.y,gy1.y,gz1.y);
  vec3 g011 = vec3(gx1.z,gy1.z,gz1.z);
  vec3 g111 = vec3(gx1.w,gy1.w,gz1.w);

  vec4 norm0 = taylorInvSqrt(vec4(dot(g000, g000), dot(g010, g010), dot(g100, g100), dot(g110, g110)));
  g000 *= norm0.x;
  g010 *= norm0.y;
  g100 *= norm0.z;
  g110 *= norm0.w;
  vec4 norm1 = taylorInvSqrt(vec4(dot(g001, g001), dot(g011, g011), dot(g101, g101), dot(g111, g111)));
  g001 *= norm1.x;
  g011 *= norm1.y;
  g101 *= norm1.z;
  g111 *= norm1.w;

  float n000 = dot(g000, Pf0);
  float n100 = dot(g100, vec3(Pf1.x, Pf0.yz));
  float n010 = dot(g010, vec3(Pf0.x, Pf1.y, Pf0.z));
  float n110 = dot(g110, vec3(Pf1.xy, Pf0.z));
  float n001 = dot(g001, vec3(Pf0.xy, Pf1.z));
  float n101 = dot(g101, vec3(Pf1.x, Pf0.y, Pf1.z));
  float n011 = dot(g011, vec3(Pf0.x, Pf1.yz));
  float n111 = dot(g111, Pf1);

  vec3 fade_xyz = fade(Pf0);
  vec4 n_z = mix(vec4(n000, n100, n010, n110), vec4(n001, n101, n011, n111), fade_xyz.z);
  vec2 n_yz = mix(n_z.xy, n_z.zw, fade_xyz.y);
  float n_xyz = mix(n_yz.x, n_yz.y, fade_xyz.x); 
  return 2.2 * n_xyz;
}

//-------- start here ------------

mat3 rotation3dY(float angle) {
  float s = sin(angle);
  float c = cos(angle);

  return mat3(c, 0.0, -s, 0.0, 1.0, 0.0, s, 0.0, c);
}

vec3 rotateY(vec3 v, float angle) { return rotation3dY(angle) * v; }

varying vec3 vNormal;
varying float displacement;
varying vec3 vPos;
varying float vDistort;

varying vec2 vUv;

uniform float uTime;
uniform float uSpeed;
uniform float uLoop;
uniform float uLoopDuration;

uniform float uLoadingTime;

uniform float uNoiseDensity;
uniform float uNoiseStrength;

#define STANDARD
varying vec3 vViewPosition;
#ifndef FLAT_SHADED
#ifdef USE_TANGENT
varying vec3 vTangent;
varying vec3 vBitangent;
#endif
#endif
#include <clipping_planes_pars_vertex>
#include <color_pars_vertex>
#include <common>
#include <displacementmap_pars_vertex>
#include <fog_pars_vertex>
#include <logdepthbuf_pars_vertex>
#include <morphtarget_pars_vertex>
#include <shadowmap_pars_vertex>
#include <skinning_pars_vertex>
#include <uv2_pars_vertex>
#include <uv_pars_vertex>

void main() {

  #include <beginnormal_vertex>
  #include <color_vertex>
  #include <defaultnormal_vertex>
  #include <morphnormal_vertex>
  #include <skinbase_vertex>
  #include <skinnormal_vertex>
  #include <uv2_vertex>
  #include <uv_vertex>
  #ifndef FLAT_SHADED
    vNormal = normalize(transformedNormal);
  #ifdef USE_TANGENT
    vTangent = normalize(transformedTangent);
    vBitangent = normalize(cross(vNormal, vTangent) * tangent.w);
  #endif
  #endif
  #include <begin_vertex>

  #include <clipping_planes_vertex>
  #include <displacementmap_vertex>
  #include <logdepthbuf_vertex>
  #include <morphtarget_vertex>
  #include <project_vertex>
  #include <skinning_vertex>
    vViewPosition = -mvPosition.xyz;
  #include <fog_vertex>
  #include <shadowmap_vertex>
  #include <worldpos_vertex>

  //-------- start vertex ------------
  vUv = uv;

  float t = uTime * uSpeed;
  
  // For seamless loops, sample noise using 4D-like circular interpolation
  vec3 noisePos = 0.43 * position * uNoiseDensity;
  float distortion;
  
  if (uLoop > 0.5) {
    // Create truly dynamic seamless loop using 4D noise simulation
    // Loop progress only depends on time and duration, not speed
    float loopProgress = uTime / uLoopDuration;
    float angle = loopProgress * 6.28318530718; // 2*PI
    
    // Radius scales with speed to maintain consistent visual speed
    // Larger radius = more distance traveled = faster perceived motion
    float radius = 5.0 * uSpeed;
    
    // Sample 4 noise values at cardinal points around the circle
    vec3 offset0 = vec3(cos(angle) * radius, sin(angle) * radius, 0.0);
    vec3 offset1 = vec3(cos(angle + 1.57079632679) * radius, sin(angle + 1.57079632679) * radius, 0.0);
    vec3 offset2 = vec3(cos(angle + 3.14159265359) * radius, sin(angle + 3.14159265359) * radius, 0.0);
    vec3 offset3 = vec3(cos(angle + 4.71238898038) * radius, sin(angle + 4.71238898038) * radius, 0.0);
    
    // Get noise at all 4 points
    float n0 = cnoise(noisePos + offset0);
    float n1 = cnoise(noisePos + offset1);
    float n2 = cnoise(noisePos + offset2);
    float n3 = cnoise(noisePos + offset3);
    
    // Smooth interpolation weights using cosine
    float w0 = (cos(angle) + 1.0) * 0.5;
    float w1 = (cos(angle + 1.57079632679) + 1.0) * 0.5;
    float w2 = (cos(angle + 3.14159265359) + 1.0) * 0.5;
    float w3 = (cos(angle + 4.71238898038) + 1.0) * 0.5;
    
    // Normalize weights
    float totalWeight = w0 + w1 + w2 + w3;
    w0 /= totalWeight;
    w1 /= totalWeight;
    w2 /= totalWeight;
    w3 /= totalWeight;
    
    // Blend all samples with amplitude boost to match single-sample strength
    // Blending reduces amplitude by ~30%, so we compensate
    float blendedNoise = n0 * w0 + n1 * w1 + n2 * w2 + n3 * w3;
    distortion = 0.75 * blendedNoise * 1.5;
  } else {
    // Normal linear time progression
    distortion = 0.75 * cnoise(noisePos + t);
  }

  vec3 pos = position + normal * distortion * uNoiseStrength * uLoadingTime;
  vPos = pos;

  gl_Position = projectionMatrix * modelViewMatrix * vec4(pos, 1.);
}
`,Ht={};U(Ht,{fragment:()=>Ji,vertex:()=>er});var Ji=`
#define STANDARD
#ifdef PHYSICAL
#define REFLECTIVITY
#define CLEARCOAT
#define TRANSMISSION
#endif
uniform vec3 diffuse;
uniform vec3 emissive;
uniform float roughness;
uniform float metalness;
uniform float opacity;
#ifdef TRANSMISSION
uniform float transmission;
#endif
#ifdef REFLECTIVITY
uniform float reflectivity;
#endif
#ifdef CLEARCOAT
uniform float clearcoat;
uniform float clearcoatRoughness;
#endif
#ifdef USE_SHEEN
uniform vec3 sheen;
#endif
varying vec3 vViewPosition;
#ifndef FLAT_SHADED
#ifdef USE_TANGENT
varying vec3 vTangent;
varying vec3 vBitangent;
#endif
#endif
#include <alphamap_pars_fragment>
#include <aomap_pars_fragment>
#include <color_pars_fragment>
#include <common>
#include <dithering_pars_fragment>
#include <emissivemap_pars_fragment>
#include <lightmap_pars_fragment>
#include <map_pars_fragment>
#include <packing>
#include <uv2_pars_fragment>
#include <uv_pars_fragment>
// #include <transmissionmap_pars_fragment>
#include <bsdfs>
#include <bumpmap_pars_fragment>
#include <clearcoat_pars_fragment>
#include <clipping_planes_pars_fragment>
#include <cube_uv_reflection_fragment>
#include <envmap_common_pars_fragment>
#include <envmap_physical_pars_fragment>
#include <fog_pars_fragment>
#include <lights_pars_begin>
#include <lights_physical_pars_fragment>
#include <logdepthbuf_pars_fragment>
#include <metalnessmap_pars_fragment>
#include <normalmap_pars_fragment>
#include <roughnessmap_pars_fragment>
#include <shadowmap_pars_fragment>
// include를 통해 가져온 값은 대부분 환경, 빛 등을 계산하기 위해서 기본 fragment
// shader의 값들을 받아왔습니다. 일단은 무시하셔도 됩니다.
varying vec3 vNormal;
varying float displacement;
varying vec3 vPos;
varying float vDistort;
uniform float uC1r;
uniform float uC1g;
uniform float uC1b;
uniform float uC2r;
uniform float uC2g;
uniform float uC2b;
uniform float uC3r;
uniform float uC3g;
uniform float uC3b;
varying vec3 color1;
varying vec3 color2;
varying vec3 color3;
varying float distanceToCenter;


// for npm package, need to add this manually
// 'linearToRelativeLuminance' : function already has a body
float linearToRelativeLuminance2( const in vec3 color ) {
    vec3 weights = vec3( 0.2126, 0.7152, 0.0722 );
    return dot( weights, color.rgb );
}

void main() {
  //-------- basic gradient ------------
  vec3 color1 = vec3(uC1r, uC1g, uC1b);
  vec3 color2 = vec3(uC2r, uC2g, uC2b);
  vec3 color3 = vec3(uC3r, uC3g, uC3b);
  float clearcoat = 1.0;
  float clearcoatRoughness = 0.5;
#include <clipping_planes_fragment>

  float distanceToCenter = distance(vPos, vec3(0, 0, 0));
  // distanceToCenter로 중심점과의 거리를 구함.

  vec4 diffuseColor =
      vec4(mix(color3, mix(color2, color1, smoothstep(-1.0, 1.0, vPos.y)),
               distanceToCenter),
           1);

  //-------- materiality ------------
  ReflectedLight reflectedLight =
      ReflectedLight(vec3(0.0), vec3(0.0), vec3(0.0), vec3(0.0));
  vec3 totalEmissiveRadiance = emissive;
#ifdef TRANSMISSION
  float totalTransmission = transmission;
#endif
#include <logdepthbuf_fragment>
#include <map_fragment>
#include <color_fragment>
#include <alphamap_fragment>
#include <alphatest_fragment>
#include <roughnessmap_fragment>
#include <metalnessmap_fragment>
#include <normal_fragment_begin>
#include <normal_fragment_maps>
#include <clearcoat_normal_fragment_begin>
#include <clearcoat_normal_fragment_maps>
#include <emissivemap_fragment>
// #include <transmissionmap_fragment>
#include <lights_physical_fragment>
#include <lights_fragment_begin>
#include <lights_fragment_maps>
#include <lights_fragment_end>
#include <aomap_fragment>
  vec3 outgoingLight =
      reflectedLight.directDiffuse + reflectedLight.indirectDiffuse +
      reflectedLight.directSpecular + reflectedLight.indirectSpecular;
//위에서 정의한 diffuseColor에 환경이나 반사값들을 반영한 값.
#ifdef TRANSMISSION
  diffuseColor.a *=
      mix(saturate(1. - totalTransmission +
                   linearToRelativeLuminance2(reflectedLight.directSpecular +
                                             reflectedLight.indirectSpecular)),
          1.0, metalness);
#endif
  gl_FragColor = vec4(outgoingLight, diffuseColor.a);
  // gl_FragColor가 fragment shader를 통해 나타나는 최종값으로, diffuseColor에서
  // 정의한 그라디언트 색상 위에 반사나 빛을 계산한 값을 최종값으로 정의.
  // gl_FragColor = vec4(mix(mix(color1, color3, smoothstep(-3.0, 3.0,vPos.x)),
  // color2, vNormal.z), 1.0); 위처럼 최종값을 그라디언트 값 자체를 넣으면 환경
  // 영향없는 그라디언트만 표현됨.

#include <tonemapping_fragment>
#include <encodings_fragment>
#include <fog_fragment>
#include <premultiplied_alpha_fragment>
#include <dithering_fragment>
}
`,er=`// #pragma glslify: pnoise = require(glsl-noise/periodic/3d)

vec3 mod289(vec3 x)
{
  return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec4 mod289(vec4 x)
{
  return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec4 permute(vec4 x)
{
  return mod289(((x*34.0)+1.0)*x);
}

vec4 taylorInvSqrt(vec4 r)
{
  return 1.79284291400159 - 0.85373472095314 * r;
}

vec3 fade(vec3 t) {
  return t*t*t*(t*(t*6.0-15.0)+10.0);
}

// Classic Perlin noise, periodic variant
float pnoise(vec3 P, vec3 rep)
{
  vec3 Pi0 = mod(floor(P), rep); // Integer part, modulo period
  vec3 Pi1 = mod(Pi0 + vec3(1.0), rep); // Integer part + 1, mod period
  Pi0 = mod289(Pi0);
  Pi1 = mod289(Pi1);
  vec3 Pf0 = fract(P); // Fractional part for interpolation
  vec3 Pf1 = Pf0 - vec3(1.0); // Fractional part - 1.0
  vec4 ix = vec4(Pi0.x, Pi1.x, Pi0.x, Pi1.x);
  vec4 iy = vec4(Pi0.yy, Pi1.yy);
  vec4 iz0 = Pi0.zzzz;
  vec4 iz1 = Pi1.zzzz;

  vec4 ixy = permute(permute(ix) + iy);
  vec4 ixy0 = permute(ixy + iz0);
  vec4 ixy1 = permute(ixy + iz1);

  vec4 gx0 = ixy0 * (1.0 / 7.0);
  vec4 gy0 = fract(floor(gx0) * (1.0 / 7.0)) - 0.5;
  gx0 = fract(gx0);
  vec4 gz0 = vec4(0.5) - abs(gx0) - abs(gy0);
  vec4 sz0 = step(gz0, vec4(0.0));
  gx0 -= sz0 * (step(0.0, gx0) - 0.5);
  gy0 -= sz0 * (step(0.0, gy0) - 0.5);

  vec4 gx1 = ixy1 * (1.0 / 7.0);
  vec4 gy1 = fract(floor(gx1) * (1.0 / 7.0)) - 0.5;
  gx1 = fract(gx1);
  vec4 gz1 = vec4(0.5) - abs(gx1) - abs(gy1);
  vec4 sz1 = step(gz1, vec4(0.0));
  gx1 -= sz1 * (step(0.0, gx1) - 0.5);
  gy1 -= sz1 * (step(0.0, gy1) - 0.5);

  vec3 g000 = vec3(gx0.x,gy0.x,gz0.x);
  vec3 g100 = vec3(gx0.y,gy0.y,gz0.y);
  vec3 g010 = vec3(gx0.z,gy0.z,gz0.z);
  vec3 g110 = vec3(gx0.w,gy0.w,gz0.w);
  vec3 g001 = vec3(gx1.x,gy1.x,gz1.x);
  vec3 g101 = vec3(gx1.y,gy1.y,gz1.y);
  vec3 g011 = vec3(gx1.z,gy1.z,gz1.z);
  vec3 g111 = vec3(gx1.w,gy1.w,gz1.w);

  vec4 norm0 = taylorInvSqrt(vec4(dot(g000, g000), dot(g010, g010), dot(g100, g100), dot(g110, g110)));
  g000 *= norm0.x;
  g010 *= norm0.y;
  g100 *= norm0.z;
  g110 *= norm0.w;
  vec4 norm1 = taylorInvSqrt(vec4(dot(g001, g001), dot(g011, g011), dot(g101, g101), dot(g111, g111)));
  g001 *= norm1.x;
  g011 *= norm1.y;
  g101 *= norm1.z;
  g111 *= norm1.w;

  float n000 = dot(g000, Pf0);
  float n100 = dot(g100, vec3(Pf1.x, Pf0.yz));
  float n010 = dot(g010, vec3(Pf0.x, Pf1.y, Pf0.z));
  float n110 = dot(g110, vec3(Pf1.xy, Pf0.z));
  float n001 = dot(g001, vec3(Pf0.xy, Pf1.z));
  float n101 = dot(g101, vec3(Pf1.x, Pf0.y, Pf1.z));
  float n011 = dot(g011, vec3(Pf0.x, Pf1.yz));
  float n111 = dot(g111, Pf1);

  vec3 fade_xyz = fade(Pf0);
  vec4 n_z = mix(vec4(n000, n100, n010, n110), vec4(n001, n101, n011, n111), fade_xyz.z);
  vec2 n_yz = mix(n_z.xy, n_z.zw, fade_xyz.y);
  float n_xyz = mix(n_yz.x, n_yz.y, fade_xyz.x);
  return 2.2 * n_xyz;
}


//-------- start here ------------

varying vec3 vNormal;
uniform float uTime;
uniform float uSpeed;
uniform float uLoop;
uniform float uLoopDuration;
uniform float uNoiseDensity;
uniform float uNoiseStrength;
uniform float uFrequency;
uniform float uAmplitude;
varying vec3 vPos;
varying float vDistort;
varying vec2 vUv;
varying vec3 vViewPosition;

#define STANDARD
#ifndef FLAT_SHADED
  #ifdef USE_TANGENT
    varying vec3 vTangent;
    varying vec3 vBitangent;
  #endif
#endif

#include <clipping_planes_pars_vertex>
#include <color_pars_vertex>
#include <common>
#include <displacementmap_pars_vertex>
#include <fog_pars_vertex>
#include <logdepthbuf_pars_vertex>
#include <morphtarget_pars_vertex>
#include <shadowmap_pars_vertex>
#include <skinning_pars_vertex>
#include <uv2_pars_vertex>
#include <uv_pars_vertex>


// rotation
mat3 rotation3dY(float angle) {
  float s = sin(angle);
  float c = cos(angle);
  return mat3(c, 0.0, -s, 0.0, 1.0, 0.0, s, 0.0, c);
}

vec3 rotateY(vec3 v, float angle) { return rotation3dY(angle) * v; }

void main() {
  #include <beginnormal_vertex>
  #include <color_vertex>
  #include <defaultnormal_vertex>
  #include <morphnormal_vertex>
  #include <skinbase_vertex>
  #include <skinnormal_vertex>
  #include <uv2_vertex>
  #include <uv_vertex>
  #ifndef FLAT_SHADED
    vNormal = normalize(transformedNormal);
  #ifdef USE_TANGENT
    vTangent = normalize(transformedTangent);
    vBitangent = normalize(cross(vNormal, vTangent) * tangent.w);
  #endif
  #endif
  #include <begin_vertex>

  #include <clipping_planes_vertex>
  #include <displacementmap_vertex>
  #include <logdepthbuf_vertex>
  #include <morphtarget_vertex>
  #include <project_vertex>
  #include <skinning_vertex>
    vViewPosition = -mvPosition.xyz;
  #include <fog_vertex>
  #include <shadowmap_vertex>
  #include <worldpos_vertex>

  //-------- start vertex ------------
  float t = uTime * uSpeed;
  
  // For seamless loops, sample noise using 4D-like circular interpolation
  float distortion;
  float angle;
  
  if (uLoop > 0.5) {
    // Create truly dynamic seamless loop using 4D noise simulation
    float loopProgress = uTime / uLoopDuration;
    float loopAngle = loopProgress * 6.28318530718; // 2*PI
    
    // Radius scales with speed to maintain consistent visual speed
    float radius = 5.0 * uSpeed;
    
    // Sample 4 noise values at cardinal points
    vec3 offset0 = vec3(cos(loopAngle) * radius, sin(loopAngle) * radius, 0.0);
    vec3 offset1 = vec3(cos(loopAngle + 1.57079632679) * radius, sin(loopAngle + 1.57079632679) * radius, 0.0);
    vec3 offset2 = vec3(cos(loopAngle + 3.14159265359) * radius, sin(loopAngle + 3.14159265359) * radius, 0.0);
    vec3 offset3 = vec3(cos(loopAngle + 4.71238898038) * radius, sin(loopAngle + 4.71238898038) * radius, 0.0);
    
    // Get noise at all 4 points
    float n0 = pnoise((normal + offset0) * uNoiseDensity, vec3(10.0));
    float n1 = pnoise((normal + offset1) * uNoiseDensity, vec3(10.0));
    float n2 = pnoise((normal + offset2) * uNoiseDensity, vec3(10.0));
    float n3 = pnoise((normal + offset3) * uNoiseDensity, vec3(10.0));
    
    // Smooth interpolation weights
    float w0 = (cos(loopAngle) + 1.0) * 0.5;
    float w1 = (cos(loopAngle + 1.57079632679) + 1.0) * 0.5;
    float w2 = (cos(loopAngle + 3.14159265359) + 1.0) * 0.5;
    float w3 = (cos(loopAngle + 4.71238898038) + 1.0) * 0.5;
    
    float totalWeight = w0 + w1 + w2 + w3;
    w0 /= totalWeight;
    w1 /= totalWeight;
    w2 /= totalWeight;
    w3 /= totalWeight;
    
    // Blend samples with amplitude boost to match single-sample strength
    float blendedNoise = n0 * w0 + n1 * w1 + n2 * w2 + n3 * w3;
    distortion = blendedNoise * 1.5 * uNoiseStrength;
    
    // Apply loop to spiral effect with blended offset
    float angleOffset = offset0.x * w0 + offset1.x * w1 + offset2.x * w2 + offset3.x * w3;
    angle = sin(uv.y * uFrequency + angleOffset) * uAmplitude;
  } else {
    // Normal linear time progression
    distortion = pnoise((normal + t) * uNoiseDensity, vec3(10.0)) * uNoiseStrength;
    angle = sin(uv.y * uFrequency + t) * uAmplitude;
  }
  
  vec3 pos = position + (normal * distortion);
  pos = rotateY(pos, angle);

  vPos = pos;
  vDistort = distortion;
  vNormal = normal;
  vUv = uv;

  gl_Position = projectionMatrix * modelViewMatrix * vec4(pos, 1.);
}
`,jt={};U(jt,{fragment:()=>tr,vertex:()=>ir});var tr=`
#define STANDARD
#ifdef PHYSICAL
#define REFLECTIVITY
#define CLEARCOAT
#define TRANSMISSION
#endif

uniform vec3 diffuse;
uniform vec3 emissive;
uniform float roughness;
uniform float metalness;
uniform float opacity;

#ifdef TRANSMISSION
uniform float transmission;
#endif
#ifdef REFLECTIVITY
uniform float reflectivity;
#endif
#ifdef CLEARCOAT
uniform float clearcoat;
uniform float clearcoatRoughness;
#endif
#ifdef USE_SHEEN
uniform vec3 sheen;
#endif
varying vec3 vViewPosition;
#ifndef FLAT_SHADED
#ifdef USE_TANGENT
varying vec3 vTangent;
varying vec3 vBitangent;
#endif
#endif
#include <alphamap_pars_fragment>
#include <aomap_pars_fragment>
#include <color_pars_fragment>
#include <common>
#include <dithering_pars_fragment>
#include <emissivemap_pars_fragment>
#include <lightmap_pars_fragment>
#include <map_pars_fragment>
#include <packing>
#include <uv2_pars_fragment>
#include <uv_pars_fragment>
// #include <transmissionmap_pars_fragment>
#include <bsdfs>
#include <bumpmap_pars_fragment>
#include <clearcoat_pars_fragment>
#include <clipping_planes_pars_fragment>
#include <cube_uv_reflection_fragment>
#include <envmap_common_pars_fragment>
#include <envmap_physical_pars_fragment>
#include <fog_pars_fragment>
#include <lights_pars_begin>
#include <lights_physical_pars_fragment>
#include <logdepthbuf_pars_fragment>
#include <metalnessmap_pars_fragment>
#include <normalmap_pars_fragment>
#include <roughnessmap_pars_fragment>
#include <shadowmap_pars_fragment>
// include를 통해 가져온 값은 대부분 환경, 빛 등을 계산하기 위해서 기본 fragment
// shader의 값들을 받아왔습니다. 일단은 무시하셔도 됩니다.

varying vec3 vNormal;
varying float displacement;
varying vec3 vPos;
varying float vDistort;

uniform float uC1r;
uniform float uC1g;
uniform float uC1b;
uniform float uC2r;
uniform float uC2g;
uniform float uC2b;
uniform float uC3r;
uniform float uC3g;
uniform float uC3b;

varying vec3 color1;
varying vec3 color2;
varying vec3 color3;

// for npm package, need to add this manually
// 'linearToRelativeLuminance' : function already has a body
float linearToRelativeLuminance2( const in vec3 color ) {
    vec3 weights = vec3( 0.2126, 0.7152, 0.0722 );
    return dot( weights, color.rgb );
}

void main() {

  //-------- basic gradient ------------
  vec3 color1 = vec3(uC1r, uC1g, uC1b);
  vec3 color2 = vec3(uC2r, uC2g, uC2b);
  vec3 color3 = vec3(uC3r, uC3g, uC3b);
  float clearcoat = 1.0;
  float clearcoatRoughness = 0.5;

  #include <clipping_planes_fragment>

  vec4 diffuseColor = vec4(
      mix(mix(color1, color2, smoothstep(-3.0, 3.0, vPos.x)), color3, vPos.z),
      1);
  // diffuseColor는 오브젝트의 베이스 색상 (환경이나 빛이 고려되지 않은 본연의
  // 색)

  // mix(x, y, a): a를 축으로 했을 때 가장 낮은 값에서 x값의 영향력을 100%, 가장
  // 높은 값에서 y값의 영향력을 100%로 만든다. smoothstep(x, y, a): a축을
  // 기준으로 x를 최소값, y를 최대값으로 그 사이의 값을 쪼갠다. x와 y 사이를
  // 0-100 사이의 그라디언트처럼 단계별로 표현하고, x 미만의 값은 0, y 이상의
  // 값은 100으로 처리

  // 1. smoothstep(-3.0, 3.0,vPos.x)로 x축의 그라디언트가 표현 될 범위를 -3,
  // 3으로 정한다.
  // 2. mix(color1, color3, smoothstep(-3.0, 3.0,vPos.x))로 color1과 color3을
  // 위의 범위 안에서 그라디언트로 표현한다.
  // 예를 들어 color1이 노랑, color3이 파랑이라고 치면, x축 기준 -3부터 3까지
  // 노랑과 파랑 사이의 그라디언트가 나타나고, -3보다 작은 값에서는 계속 노랑,
  // 3보다 큰 값에서는 계속 파랑이 나타난다.
  // 3. mix()를 한 번 더 사용해서 위의 그라디언트와 color2를 z축 기준으로
  // 분배한다.

  //-------- materiality ------------
  ReflectedLight reflectedLight =
      ReflectedLight(vec3(0.0), vec3(0.0), vec3(0.0), vec3(0.0));
  vec3 totalEmissiveRadiance = emissive;

  #ifdef TRANSMISSION
    float totalTransmission = transmission;
  #endif
  #include <logdepthbuf_fragment>
  #include <map_fragment>
  #include <color_fragment>
  #include <alphamap_fragment>
  #include <alphatest_fragment>
  #include <roughnessmap_fragment>
  #include <metalnessmap_fragment>
  #include <normal_fragment_begin>
  #include <normal_fragment_maps>
  #include <clearcoat_normal_fragment_begin>
  #include <clearcoat_normal_fragment_maps>
  #include <emissivemap_fragment>
  // #include <transmissionmap_fragment>
  #include <lights_physical_fragment>
  #include <lights_fragment_begin>
  #include <lights_fragment_maps>
  #include <lights_fragment_end>
  #include <aomap_fragment>
    vec3 outgoingLight =
        reflectedLight.directDiffuse + reflectedLight.indirectDiffuse +
        reflectedLight.directSpecular + reflectedLight.indirectSpecular;
    //위에서 정의한 diffuseColor에 환경이나 반사값들을 반영한 값.
  #ifdef TRANSMISSION
    diffuseColor.a *=
        mix(saturate(1. - totalTransmission +
                    linearToRelativeLuminance2(reflectedLight.directSpecular +
                                              reflectedLight.indirectSpecular)),
            1.0, metalness);
  #endif


  #include <tonemapping_fragment>
  #include <encodings_fragment>
  #include <fog_fragment>
  #include <premultiplied_alpha_fragment>
  #include <dithering_fragment>


  gl_FragColor = vec4(outgoingLight, diffuseColor.a);
  // gl_FragColor가 fragment shader를 통해 나타나는 최종값으로, diffuseColor에서
  // 정의한 그라디언트 색상 위에 반사나 빛을 계산한 값을 최종값으로 정의.
  // gl_FragColor = vec4(mix(mix(color1, color3, smoothstep(-3.0, 3.0,vPos.x)),
  // color2, vNormal.z), 1.0); 위처럼 최종값을 그라디언트 값 자체를 넣으면 환경
  // 영향없는 그라디언트만 표현됨.
}
`,ir=`// #pragma glslify: cnoise3 = require(glsl-noise/classic/3d) 
vec3 mod289(vec3 x)
{
  return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec4 mod289(vec4 x)
{
  return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec4 permute(vec4 x)
{
  return mod289(((x*34.0)+1.0)*x);
}

vec4 taylorInvSqrt(vec4 r)
{
  return 1.79284291400159 - 0.85373472095314 * r;
}

vec3 fade(vec3 t) {
  return t*t*t*(t*(t*6.0-15.0)+10.0);
}

float cnoise(vec3 P)
{
  vec3 Pi0 = floor(P); // Integer part for indexing
  vec3 Pi1 = Pi0 + vec3(1.0); // Integer part + 1
  Pi0 = mod289(Pi0);
  Pi1 = mod289(Pi1);
  vec3 Pf0 = fract(P); // Fractional part for interpolation
  vec3 Pf1 = Pf0 - vec3(1.0); // Fractional part - 1.0
  vec4 ix = vec4(Pi0.x, Pi1.x, Pi0.x, Pi1.x);
  vec4 iy = vec4(Pi0.yy, Pi1.yy);
  vec4 iz0 = Pi0.zzzz;
  vec4 iz1 = Pi1.zzzz;

  vec4 ixy = permute(permute(ix) + iy);
  vec4 ixy0 = permute(ixy + iz0);
  vec4 ixy1 = permute(ixy + iz1);

  vec4 gx0 = ixy0 * (1.0 / 7.0);
  vec4 gy0 = fract(floor(gx0) * (1.0 / 7.0)) - 0.5;
  gx0 = fract(gx0);
  vec4 gz0 = vec4(0.5) - abs(gx0) - abs(gy0);
  vec4 sz0 = step(gz0, vec4(0.0));
  gx0 -= sz0 * (step(0.0, gx0) - 0.5);
  gy0 -= sz0 * (step(0.0, gy0) - 0.5);

  vec4 gx1 = ixy1 * (1.0 / 7.0);
  vec4 gy1 = fract(floor(gx1) * (1.0 / 7.0)) - 0.5;
  gx1 = fract(gx1);
  vec4 gz1 = vec4(0.5) - abs(gx1) - abs(gy1);
  vec4 sz1 = step(gz1, vec4(0.0));
  gx1 -= sz1 * (step(0.0, gx1) - 0.5);
  gy1 -= sz1 * (step(0.0, gy1) - 0.5);

  vec3 g000 = vec3(gx0.x,gy0.x,gz0.x);
  vec3 g100 = vec3(gx0.y,gy0.y,gz0.y);
  vec3 g010 = vec3(gx0.z,gy0.z,gz0.z);
  vec3 g110 = vec3(gx0.w,gy0.w,gz0.w);
  vec3 g001 = vec3(gx1.x,gy1.x,gz1.x);
  vec3 g101 = vec3(gx1.y,gy1.y,gz1.y);
  vec3 g011 = vec3(gx1.z,gy1.z,gz1.z);
  vec3 g111 = vec3(gx1.w,gy1.w,gz1.w);

  vec4 norm0 = taylorInvSqrt(vec4(dot(g000, g000), dot(g010, g010), dot(g100, g100), dot(g110, g110)));
  g000 *= norm0.x;
  g010 *= norm0.y;
  g100 *= norm0.z;
  g110 *= norm0.w;
  vec4 norm1 = taylorInvSqrt(vec4(dot(g001, g001), dot(g011, g011), dot(g101, g101), dot(g111, g111)));
  g001 *= norm1.x;
  g011 *= norm1.y;
  g101 *= norm1.z;
  g111 *= norm1.w;

  float n000 = dot(g000, Pf0);
  float n100 = dot(g100, vec3(Pf1.x, Pf0.yz));
  float n010 = dot(g010, vec3(Pf0.x, Pf1.y, Pf0.z));
  float n110 = dot(g110, vec3(Pf1.xy, Pf0.z));
  float n001 = dot(g001, vec3(Pf0.xy, Pf1.z));
  float n101 = dot(g101, vec3(Pf1.x, Pf0.y, Pf1.z));
  float n011 = dot(g011, vec3(Pf0.x, Pf1.yz));
  float n111 = dot(g111, Pf1);

  vec3 fade_xyz = fade(Pf0);
  vec4 n_z = mix(vec4(n000, n100, n010, n110), vec4(n001, n101, n011, n111), fade_xyz.z);
  vec2 n_yz = mix(n_z.xy, n_z.zw, fade_xyz.y);
  float n_xyz = mix(n_yz.x, n_yz.y, fade_xyz.x); 
  return 2.2 * n_xyz;
}

//-------- start here ------------

mat3 rotation3dY(float angle) {
  float s = sin(angle);
  float c = cos(angle);

  return mat3(c, 0.0, -s, 0.0, 1.0, 0.0, s, 0.0, c);
}

vec3 rotateY(vec3 v, float angle) { return rotation3dY(angle) * v; }

varying vec3 vNormal;
varying float displacement;
varying vec3 vPos;
varying float vDistort;

uniform float uTime;
uniform float uSpeed;
uniform float uLoop;
uniform float uLoopDuration;
uniform float uNoiseDensity;
uniform float uNoiseStrength;

#define STANDARD
varying vec3 vViewPosition;
#ifndef FLAT_SHADED
#ifdef USE_TANGENT
varying vec3 vTangent;
varying vec3 vBitangent;
#endif
#endif
#include <clipping_planes_pars_vertex>
#include <color_pars_vertex>
#include <common>
#include <displacementmap_pars_vertex>
#include <fog_pars_vertex>
#include <logdepthbuf_pars_vertex>
#include <morphtarget_pars_vertex>
#include <shadowmap_pars_vertex>
#include <skinning_pars_vertex>
#include <uv2_pars_vertex>
#include <uv_pars_vertex>

void main() {

  #include <beginnormal_vertex>
  #include <color_vertex>
  #include <defaultnormal_vertex>
  #include <morphnormal_vertex>
  #include <skinbase_vertex>
  #include <skinnormal_vertex>
  #include <uv2_vertex>
  #include <uv_vertex>
  #ifndef FLAT_SHADED
    vNormal = normalize(transformedNormal);
  #ifdef USE_TANGENT
    vTangent = normalize(transformedTangent);
    vBitangent = normalize(cross(vNormal, vTangent) * tangent.w);
  #endif
  #endif
  #include <begin_vertex>

  #include <clipping_planes_vertex>
  #include <displacementmap_vertex>
  #include <logdepthbuf_vertex>
  #include <morphtarget_vertex>
  #include <project_vertex>
  #include <skinning_vertex>
    vViewPosition = -mvPosition.xyz;
  #include <fog_vertex>
  #include <shadowmap_vertex>
  #include <worldpos_vertex>

  //-------- start vertex ------------
  float t = uTime * uSpeed;
  
  // For seamless loops, sample noise using 4D-like circular interpolation
  vec3 noisePos = 0.43 * position * uNoiseDensity;
  float distortion;
  
  if (uLoop > 0.5) {
    // Create truly dynamic seamless loop using 4D noise simulation
    float loopProgress = uTime / uLoopDuration;
    float angle = loopProgress * 6.28318530718; // 2*PI
    
    // Radius scales with speed to maintain consistent visual speed
    float radius = 5.0 * uSpeed;
    
    // Sample 4 noise values at cardinal points
    vec3 offset0 = vec3(cos(angle) * radius, sin(angle) * radius, 0.0);
    vec3 offset1 = vec3(cos(angle + 1.57079632679) * radius, sin(angle + 1.57079632679) * radius, 0.0);
    vec3 offset2 = vec3(cos(angle + 3.14159265359) * radius, sin(angle + 3.14159265359) * radius, 0.0);
    vec3 offset3 = vec3(cos(angle + 4.71238898038) * radius, sin(angle + 4.71238898038) * radius, 0.0);
    
    // Get noise at all 4 points
    float n0 = cnoise(noisePos + offset0);
    float n1 = cnoise(noisePos + offset1);
    float n2 = cnoise(noisePos + offset2);
    float n3 = cnoise(noisePos + offset3);
    
    // Smooth interpolation weights
    float w0 = (cos(angle) + 1.0) * 0.5;
    float w1 = (cos(angle + 1.57079632679) + 1.0) * 0.5;
    float w2 = (cos(angle + 3.14159265359) + 1.0) * 0.5;
    float w3 = (cos(angle + 4.71238898038) + 1.0) * 0.5;
    
    float totalWeight = w0 + w1 + w2 + w3;
    w0 /= totalWeight;
    w1 /= totalWeight;
    w2 /= totalWeight;
    w3 /= totalWeight;
    
    // Blend samples with amplitude boost to match single-sample strength
    float blendedNoise = n0 * w0 + n1 * w1 + n2 * w2 + n3 * w3;
    distortion = 0.75 * blendedNoise * 1.5;
  } else {
    // Normal linear time progression
    distortion = 0.75 * cnoise(noisePos + t);
  }

  vec3 pos = position + normal * distortion * uNoiseStrength;
  vPos = pos;

  gl_Position = projectionMatrix * modelViewMatrix * vec4(pos, 1.);
}
`,Vt={};U(Vt,{plane:()=>kt,sphere:()=>Ht,waterPlane:()=>jt});var Yt={};U(Yt,{fragment:()=>rr,vertex:()=>nr});var rr=`// Glass Plane Fragment Shader - Transparency & Refraction

#define STANDARD
#ifdef PHYSICAL
#define REFLECTIVITY
#define CLEARCOAT
#define TRANSMISSION
#endif

uniform vec3 diffuse;
uniform vec3 emissive;
uniform float roughness;
uniform float metalness;
uniform float opacity;

// transmission is already defined by Three.js when TRANSMISSION is enabled
#ifdef REFLECTIVITY
uniform float reflectivity;
#endif
#ifdef CLEARCOAT
uniform float clearcoat;
uniform float clearcoatRoughness;
#endif
#ifdef USE_SHEEN
uniform vec3 sheen;
#endif

varying vec3 vViewPosition;
#ifndef FLAT_SHADED
#ifdef USE_TANGENT
varying vec3 vTangent;
varying vec3 vBitangent;
#endif
#endif

#include <alphamap_pars_fragment>
#include <aomap_pars_fragment>
#include <color_pars_fragment>
#include <common>
#include <dithering_pars_fragment>
#include <emissivemap_pars_fragment>
#include <lightmap_pars_fragment>
#include <map_pars_fragment>
#include <packing>
#include <uv2_pars_fragment>
#include <uv_pars_fragment>
#include <bsdfs>
#include <bumpmap_pars_fragment>
#include <clearcoat_pars_fragment>
#include <clipping_planes_pars_fragment>
#include <envmap_common_pars_fragment>
#include <envmap_pars_fragment>
#include <envmap_physical_pars_fragment>
#include <fog_pars_fragment>
#include <lights_pars_begin>
#include <lights_physical_pars_fragment>
#include <logdepthbuf_pars_fragment>
#include <metalnessmap_pars_fragment>
#include <normalmap_pars_fragment>
#include <roughnessmap_pars_fragment>
#include <shadowmap_pars_fragment>
#include <transmission_pars_fragment>

// Custom uniforms for glass effect
uniform float uTime;
uniform vec3 uColor1;
uniform vec3 uColor2;
uniform vec3 uColor3;
uniform float uTransparency;
uniform float uRefraction;
uniform float uChromaticAberration;
uniform float uFresnelPower;
uniform float uReflectivity;
// envMap and envMapIntensity are provided by Three.js

varying vec2 vUv;
varying vec3 vPosition;
varying vec3 vNormal;
varying vec3 vGlassWorldPos;
varying vec3 vReflect;
varying vec3 vRefract;

// Fresnel calculation
float fresnel(vec3 viewDirection, vec3 normal, float power) {
  return pow(1.0 - dot(viewDirection, normal), power);
}

// Chromatic aberration for refraction
vec3 chromaticRefraction(vec3 viewDirection, vec3 normal, float ior, float chromaticStrength) {
  vec3 refractedR = refract(viewDirection, normal, 1.0 / (ior - chromaticStrength));
  vec3 refractedG = refract(viewDirection, normal, 1.0 / ior);
  vec3 refractedB = refract(viewDirection, normal, 1.0 / (ior + chromaticStrength));
  
  #ifdef ENVMAP_TYPE_CUBE
  return vec3(
    textureCube(envMap, refractedR).r,
    textureCube(envMap, refractedG).g,
    textureCube(envMap, refractedB).b
  );
  #else
  return vec3(0.5);
  #endif
}

void main() {
  #include <clipping_planes_fragment>
  
  vec4 diffuseColor = vec4(diffuse, opacity);
  ReflectedLight reflectedLight = ReflectedLight(vec3(0.0), vec3(0.0), vec3(0.0), vec3(0.0));
  vec3 totalEmissiveRadiance = emissive;
  
  #include <logdepthbuf_fragment>
  #include <map_fragment>
  #include <color_fragment>
  #include <alphamap_fragment>
  #include <alphatest_fragment>
  #include <specularmap_fragment>
  #include <roughnessmap_fragment>
  #include <metalnessmap_fragment>
  #include <normal_fragment_begin>
  #include <normal_fragment_maps>
  #include <clearcoat_normal_fragment_begin>
  #include <clearcoat_normal_fragment_maps>
  #include <emissivemap_fragment>
  
  // Glass-specific calculations
  vec3 viewDirection = normalize(vViewPosition);
  vec3 worldNormal = normalize(vNormal);
  
  // Calculate Fresnel effect
  float fresnelFactor = fresnel(viewDirection, worldNormal, uFresnelPower);
  
  // Base glass color gradient
  vec3 gradientColor = mix(uColor1, uColor2, vUv.y);
  gradientColor = mix(gradientColor, uColor3, fresnelFactor);
  
  // Reflection
  #ifdef ENVMAP_TYPE_CUBE
  vec3 reflectionColor = textureCube(envMap, vReflect).rgb * envMapIntensity;
  #else
  vec3 reflectionColor = vec3(0.5);
  #endif
  
  // Refraction with chromatic aberration
  vec3 refractionColor;
  #ifdef ENVMAP_TYPE_CUBE
  if (uChromaticAberration > 0.0) {
    refractionColor = chromaticRefraction(-viewDirection, worldNormal, uRefraction, uChromaticAberration);
  } else {
    refractionColor = textureCube(envMap, vRefract).rgb;
  }
  refractionColor *= envMapIntensity;
  #else
  refractionColor = vec3(0.3);
  #endif
  
  // Mix reflection and refraction based on Fresnel
  vec3 envColor = mix(refractionColor, reflectionColor, fresnelFactor * uReflectivity);
  
  // Combine with gradient color
  vec3 finalColor = mix(gradientColor, envColor, 0.7);
  
  // Apply transparency
  float finalAlpha = mix(uTransparency, 1.0, fresnelFactor * 0.5);
  
  // Set diffuse color for standard lighting
  diffuseColor.rgb = finalColor;
  diffuseColor.a = finalAlpha;
  
  // Skip transmission_fragment to avoid conflicts
  
  vec3 outgoingLight = reflectedLight.directDiffuse + reflectedLight.indirectDiffuse + 
                       reflectedLight.directSpecular + reflectedLight.indirectSpecular + 
                       totalEmissiveRadiance;
  
  // Add our glass color contribution
  outgoingLight += finalColor * 0.8;
  
  gl_FragColor = vec4(outgoingLight, diffuseColor.a);
  
  #include <tonemapping_fragment>
  #include <colorspace_fragment>
  #include <fog_fragment>
  #include <premultiplied_alpha_fragment>
  #include <dithering_fragment>
}
`,nr=`// Glass Plane Vertex Shader - Refraction & Transparency Effects

#define STANDARD
varying vec3 vViewPosition;
#ifndef FLAT_SHADED
#ifdef USE_TANGENT
varying vec3 vTangent;
varying vec3 vBitangent;
#endif
#endif
#include <clipping_planes_pars_vertex>
#include <color_pars_vertex>
#include <common>
#include <displacementmap_pars_vertex>
#include <fog_pars_vertex>
#include <logdepthbuf_pars_vertex>
#include <morphtarget_pars_vertex>
#include <shadowmap_pars_vertex>
#include <skinning_pars_vertex>
#include <uv2_pars_vertex>
#include <uv_pars_vertex>

varying vec2 vUv;
varying vec3 vPosition;
varying vec3 vNormal;
varying vec3 vGlassWorldPos;
varying vec3 vReflect;
varying vec3 vRefract;

uniform float uTime;
uniform float uSpeed;
uniform float uWaveAmplitude;
uniform float uWaveFrequency;
uniform float uNoiseStrength;
uniform float uDistortion;

// Noise functions for glass distortion
vec3 mod289(vec3 x) {
  return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec4 mod289(vec4 x) {
  return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec4 permute(vec4 x) {
  return mod289(((x * 34.0) + 1.0) * x);
}

vec4 taylorInvSqrt(vec4 r) {
  return 1.79284291400159 - 0.85373472095314 * r;
}

float snoise(vec3 v) {
  const vec2 C = vec2(1.0 / 6.0, 1.0 / 3.0);
  const vec4 D = vec4(0.0, 0.5, 1.0, 2.0);

  vec3 i = floor(v + dot(v, C.yyy));
  vec3 x0 = v - i + dot(i, C.xxx);

  vec3 g = step(x0.yzx, x0.xyz);
  vec3 l = 1.0 - g;
  vec3 i1 = min(g.xyz, l.zxy);
  vec3 i2 = max(g.xyz, l.zxy);

  vec3 x1 = x0 - i1 + C.xxx;
  vec3 x2 = x0 - i2 + C.yyy;
  vec3 x3 = x0 - D.yyy;

  i = mod289(i);
  vec4 p = permute(permute(permute(
    i.z + vec4(0.0, i1.z, i2.z, 1.0))
    + i.y + vec4(0.0, i1.y, i2.y, 1.0))
    + i.x + vec4(0.0, i1.x, i2.x, 1.0));

  float n_ = 0.142857142857;
  vec3 ns = n_ * D.wyz - D.xzx;

  vec4 j = p - 49.0 * floor(p * ns.z * ns.z);

  vec4 x_ = floor(j * ns.z);
  vec4 y_ = floor(j - 7.0 * x_);

  vec4 x = x_ * ns.x + ns.yyyy;
  vec4 y = y_ * ns.x + ns.yyyy;
  vec4 h = 1.0 - abs(x) - abs(y);

  vec4 b0 = vec4(x.xy, y.xy);
  vec4 b1 = vec4(x.zw, y.zw);

  vec4 s0 = floor(b0) * 2.0 + 1.0;
  vec4 s1 = floor(b1) * 2.0 + 1.0;
  vec4 sh = -step(h, vec4(0.0));

  vec4 a0 = b0.xzyw + s0.xzyw * sh.xxyy;
  vec4 a1 = b1.xzyw + s1.xzyw * sh.zzww;

  vec3 p0 = vec3(a0.xy, h.x);
  vec3 p1 = vec3(a0.zw, h.y);
  vec3 p2 = vec3(a1.xy, h.z);
  vec3 p3 = vec3(a1.zw, h.w);

  vec4 norm = taylorInvSqrt(vec4(dot(p0, p0), dot(p1, p1), dot(p2, p2), dot(p3, p3)));
  p0 *= norm.x;
  p1 *= norm.y;
  p2 *= norm.z;
  p3 *= norm.w;

  vec4 m = max(0.6 - vec4(dot(x0, x0), dot(x1, x1), dot(x2, x2), dot(x3, x3)), 0.0);
  m = m * m;
  return 42.0 * dot(m * m, vec4(dot(p0, x0), dot(p1, x1),
    dot(p2, x2), dot(p3, x3)));
}

void main() {
  #include <uv_pars_vertex>
  #include <uv_vertex>
  #include <uv2_pars_vertex>
  #include <uv2_vertex>
  #include <color_pars_vertex>
  #include <color_vertex>
  #include <morphcolor_vertex>
  #include <beginnormal_vertex>
  #include <morphnormal_vertex>
  #include <skinbase_vertex>
  #include <skinnormal_vertex>
  #include <defaultnormal_vertex>
  #include <normal_vertex>
  
  #ifndef FLAT_SHADED
  vNormal = normalize(transformedNormal);
  #ifdef USE_TANGENT
  vTangent = normalize(transformedTangent);
  vBitangent = normalize(cross(vNormal, vTangent) * tangent.w);
  #endif
  #endif
  
  #include <begin_vertex>
  #include <morphtarget_vertex>
  #include <skinning_vertex>
  #include <displacementmap_vertex>
  
  // Pass UV coordinates
  vUv = uv;

  // Calculate time-based animation
  float time = uTime * uSpeed;
  
  // Create subtle wave distortion for glass effect
  float waveX = sin(position.x * uWaveFrequency + time) * uWaveAmplitude;
  float waveY = cos(position.y * uWaveFrequency + time) * uWaveAmplitude;
  float waveZ = sin(position.z * uWaveFrequency + time * 0.5) * uWaveAmplitude * 0.5;
  
  // Add noise for organic glass distortion
  vec3 noisePos = position + vec3(time * 0.1);
  float noise = snoise(noisePos * 0.5) * uNoiseStrength;
  
  // Apply distortion to transformed position
  transformed += vec3(waveX, waveY, waveZ) * uDistortion + normal * noise;
  
  #include <project_vertex>
  #include <logdepthbuf_vertex>
  #include <clipping_planes_vertex>
  
  vViewPosition = -mvPosition.xyz;
  vPosition = transformed;
  
  // Calculate world position for refraction
  vec4 worldPosition = modelMatrix * vec4(transformed, 1.0);
  vGlassWorldPos = worldPosition.xyz;
  
  // Calculate reflection and refraction vectors
  vec3 worldNormal = normalize(mat3(modelMatrix) * normal);
  vec3 viewVector = normalize(cameraPosition - worldPosition.xyz);
  
  // Reflection vector
  vReflect = reflect(-viewVector, worldNormal);
  
  // Refraction vector with index of refraction for glass (1.5)
  float ior = 1.5;
  vRefract = refract(-viewVector, worldNormal, 1.0 / ior);
  
  #include <fog_vertex>
  #include <shadowmap_vertex>
}
`,$t={};U($t,{fragment:()=>ar,vertex:()=>or});var ar=`// Glass Sphere Fragment Shader - Transparency & Refraction

#define STANDARD
#ifdef PHYSICAL
#define REFLECTIVITY
#define CLEARCOAT
#define TRANSMISSION
#endif

uniform vec3 diffuse;
uniform vec3 emissive;
uniform float roughness;
uniform float metalness;
uniform float opacity;

// transmission is already defined by Three.js when TRANSMISSION is enabled
#ifdef REFLECTIVITY
uniform float reflectivity;
#endif
#ifdef CLEARCOAT
uniform float clearcoat;
uniform float clearcoatRoughness;
#endif
#ifdef USE_SHEEN
uniform vec3 sheen;
#endif

varying vec3 vViewPosition;
#ifndef FLAT_SHADED
#ifdef USE_TANGENT
varying vec3 vTangent;
varying vec3 vBitangent;
#endif
#endif

#include <alphamap_pars_fragment>
#include <aomap_pars_fragment>
#include <color_pars_fragment>
#include <common>
#include <dithering_pars_fragment>
#include <emissivemap_pars_fragment>
#include <lightmap_pars_fragment>
#include <map_pars_fragment>
#include <packing>
#include <uv2_pars_fragment>
#include <uv_pars_fragment>
#include <bsdfs>
#include <bumpmap_pars_fragment>
#include <clearcoat_pars_fragment>
#include <clipping_planes_pars_fragment>
#include <envmap_common_pars_fragment>
#include <envmap_pars_fragment>
#include <envmap_physical_pars_fragment>
#include <fog_pars_fragment>
#include <lights_pars_begin>
#include <lights_physical_pars_fragment>
#include <logdepthbuf_pars_fragment>
#include <metalnessmap_pars_fragment>
#include <normalmap_pars_fragment>
#include <roughnessmap_pars_fragment>
#include <shadowmap_pars_fragment>
#include <transmission_pars_fragment>

// Custom uniforms for glass effect
uniform float uTime;
uniform vec3 uColor1;
uniform vec3 uColor2;
uniform vec3 uColor3;
uniform float uTransparency;
uniform float uRefraction;
uniform float uChromaticAberration;
uniform float uFresnelPower;
uniform float uReflectivity;
// envMap and envMapIntensity are provided by Three.js

varying vec2 vUv;
varying vec3 vPosition;
varying vec3 vNormal;
varying vec3 vGlassWorldPos;
varying vec3 vReflect;
varying vec3 vRefract;
varying float vDistortion;

// Fresnel calculation
float fresnel(vec3 viewDirection, vec3 normal, float power) {
  return pow(1.0 - abs(dot(viewDirection, normal)), power);
}

// Chromatic aberration for refraction
vec3 chromaticRefraction(vec3 viewDirection, vec3 normal, float ior, float chromaticStrength) {
  vec3 refractedR = refract(viewDirection, normal, 1.0 / (ior - chromaticStrength));
  vec3 refractedG = refract(viewDirection, normal, 1.0 / ior);
  vec3 refractedB = refract(viewDirection, normal, 1.0 / (ior + chromaticStrength));
  
  #ifdef ENVMAP_TYPE_CUBE
  return vec3(
    textureCube(envMap, refractedR).r,
    textureCube(envMap, refractedG).g,
    textureCube(envMap, refractedB).b
  );
  #else
  return vec3(0.5);
  #endif
}

// Caustics simulation for sphere
float caustics(vec3 position, float time) {
  float c1 = sin(position.x * 4.0 + time) * sin(position.y * 4.0 + time * 0.8);
  float c2 = sin(position.z * 3.0 - time * 1.2) * sin(position.x * 3.0 + time);
  return (c1 + c2) * 0.5 + 0.5;
}

void main() {
  #include <clipping_planes_fragment>
  
  vec4 diffuseColor = vec4(diffuse, opacity);
  ReflectedLight reflectedLight = ReflectedLight(vec3(0.0), vec3(0.0), vec3(0.0), vec3(0.0));
  vec3 totalEmissiveRadiance = emissive;
  
  #include <logdepthbuf_fragment>
  #include <map_fragment>
  #include <color_fragment>
  #include <alphamap_fragment>
  #include <alphatest_fragment>
  #include <specularmap_fragment>
  #include <roughnessmap_fragment>
  #include <metalnessmap_fragment>
  #include <normal_fragment_begin>
  #include <normal_fragment_maps>
  #include <clearcoat_normal_fragment_begin>
  #include <clearcoat_normal_fragment_maps>
  #include <emissivemap_fragment>
  
  // Glass-specific calculations
  vec3 viewDirection = normalize(vViewPosition);
  vec3 worldNormal = normalize(vNormal);
  
  // Calculate Fresnel effect
  float fresnelFactor = fresnel(viewDirection, worldNormal, uFresnelPower);
  
  // For sphere, use spherical UV mapping for gradient
  float sphericalU = atan(vPosition.z, vPosition.x) / (2.0 * PI) + 0.5;
  float sphericalV = acos(vPosition.y / length(vPosition)) / PI;
  vec2 sphericalUV = vec2(sphericalU, sphericalV);
  
  // Create color gradient based on spherical coordinates
  vec3 gradientColor = mix(uColor1, uColor2, sphericalUV.y);
  gradientColor = mix(gradientColor, uColor3, pow(fresnelFactor, 1.5));
  
  // Add caustics effect for sphere
  float causticsValue = caustics(vGlassWorldPos, uTime);
  gradientColor += vec3(causticsValue * 0.1);
  
  // Reflection
  #ifdef ENVMAP_TYPE_CUBE
  vec3 reflectionColor = textureCube(envMap, vReflect).rgb * envMapIntensity;
  #else
  vec3 reflectionColor = vec3(0.5);
  #endif
  
  // Refraction with chromatic aberration (enhanced for sphere)
  vec3 refractionColor;
  #ifdef ENVMAP_TYPE_CUBE
  if (uChromaticAberration > 0.0) {
    float chromaticIntensity = uChromaticAberration * (1.0 + vDistortion * 0.5);
    refractionColor = chromaticRefraction(-viewDirection, worldNormal, uRefraction, chromaticIntensity);
  } else {
    refractionColor = textureCube(envMap, vRefract).rgb;
  }
  refractionColor *= envMapIntensity;
  #else
  refractionColor = vec3(0.3);
  #endif
  
  // Mix reflection and refraction based on Fresnel (stronger effect for sphere)
  vec3 envColor = mix(refractionColor, reflectionColor, fresnelFactor * uReflectivity);
  
  // Add inner glow effect for sphere
  float innerGlow = pow(1.0 - abs(dot(viewDirection, worldNormal)), 3.0);
  vec3 glowColor = mix(uColor2, uColor3, innerGlow) * innerGlow * 0.5;
  
  // Combine all effects
  vec3 finalColor = mix(gradientColor, envColor, 0.8) + glowColor;
  
  // Apply transparency with sphere thickness consideration
  float thickness = 1.0 - pow(abs(dot(viewDirection, worldNormal)), 0.5);
  float finalAlpha = mix(uTransparency * thickness, 1.0, fresnelFactor * 0.7);
  
  // Set diffuse color for standard lighting
  diffuseColor.rgb = finalColor;
  diffuseColor.a = finalAlpha;
  
  // Skip transmission_fragment to avoid conflicts
  
  vec3 outgoingLight = reflectedLight.directDiffuse + reflectedLight.indirectDiffuse + 
                       reflectedLight.directSpecular + reflectedLight.indirectSpecular + 
                       totalEmissiveRadiance;
  
  // Add our glass color contribution
  outgoingLight += finalColor * 0.9;
  
  gl_FragColor = vec4(outgoingLight, diffuseColor.a);
  
  #include <tonemapping_fragment>
  #include <colorspace_fragment>
  #include <fog_fragment>
  #include <premultiplied_alpha_fragment>
  #include <dithering_fragment>
}
`,or=`// Glass Sphere Vertex Shader - Refraction & Transparency Effects

#define STANDARD
varying vec3 vViewPosition;
#ifndef FLAT_SHADED
#ifdef USE_TANGENT
varying vec3 vTangent;
varying vec3 vBitangent;
#endif
#endif
#include <clipping_planes_pars_vertex>
#include <color_pars_vertex>
#include <common>
#include <displacementmap_pars_vertex>
#include <fog_pars_vertex>
#include <logdepthbuf_pars_vertex>
#include <morphtarget_pars_vertex>
#include <shadowmap_pars_vertex>
#include <skinning_pars_vertex>
#include <uv2_pars_vertex>
#include <uv_pars_vertex>

varying vec2 vUv;
varying vec3 vPosition;
varying vec3 vNormal;
varying vec3 vGlassWorldPos;
varying vec3 vReflect;
varying vec3 vRefract;
varying float vDistortion;

uniform float uTime;
uniform float uSpeed;
uniform float uWaveAmplitude;
uniform float uWaveFrequency;
uniform float uNoiseStrength;
uniform float uDistortion;

// Noise functions for glass distortion
vec3 mod289(vec3 x) {
  return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec4 mod289(vec4 x) {
  return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec4 permute(vec4 x) {
  return mod289(((x * 34.0) + 1.0) * x);
}

vec4 taylorInvSqrt(vec4 r) {
  return 1.79284291400159 - 0.85373472095314 * r;
}

float snoise(vec3 v) {
  const vec2 C = vec2(1.0 / 6.0, 1.0 / 3.0);
  const vec4 D = vec4(0.0, 0.5, 1.0, 2.0);

  vec3 i = floor(v + dot(v, C.yyy));
  vec3 x0 = v - i + dot(i, C.xxx);

  vec3 g = step(x0.yzx, x0.xyz);
  vec3 l = 1.0 - g;
  vec3 i1 = min(g.xyz, l.zxy);
  vec3 i2 = max(g.xyz, l.zxy);

  vec3 x1 = x0 - i1 + C.xxx;
  vec3 x2 = x0 - i2 + C.yyy;
  vec3 x3 = x0 - D.yyy;

  i = mod289(i);
  vec4 p = permute(permute(permute(
    i.z + vec4(0.0, i1.z, i2.z, 1.0))
    + i.y + vec4(0.0, i1.y, i2.y, 1.0))
    + i.x + vec4(0.0, i1.x, i2.x, 1.0));

  float n_ = 0.142857142857;
  vec3 ns = n_ * D.wyz - D.xzx;

  vec4 j = p - 49.0 * floor(p * ns.z * ns.z);

  vec4 x_ = floor(j * ns.z);
  vec4 y_ = floor(j - 7.0 * x_);

  vec4 x = x_ * ns.x + ns.yyyy;
  vec4 y = y_ * ns.x + ns.yyyy;
  vec4 h = 1.0 - abs(x) - abs(y);

  vec4 b0 = vec4(x.xy, y.xy);
  vec4 b1 = vec4(x.zw, y.zw);

  vec4 s0 = floor(b0) * 2.0 + 1.0;
  vec4 s1 = floor(b1) * 2.0 + 1.0;
  vec4 sh = -step(h, vec4(0.0));

  vec4 a0 = b0.xzyw + s0.xzyw * sh.xxyy;
  vec4 a1 = b1.xzyw + s1.xzyw * sh.zzww;

  vec3 p0 = vec3(a0.xy, h.x);
  vec3 p1 = vec3(a0.zw, h.y);
  vec3 p2 = vec3(a1.xy, h.z);
  vec3 p3 = vec3(a1.zw, h.w);

  vec4 norm = taylorInvSqrt(vec4(dot(p0, p0), dot(p1, p1), dot(p2, p2), dot(p3, p3)));
  p0 *= norm.x;
  p1 *= norm.y;
  p2 *= norm.z;
  p3 *= norm.w;

  vec4 m = max(0.6 - vec4(dot(x0, x0), dot(x1, x1), dot(x2, x2), dot(x3, x3)), 0.0);
  m = m * m;
  return 42.0 * dot(m * m, vec4(dot(p0, x0), dot(p1, x1),
    dot(p2, x2), dot(p3, x3)));
}

void main() {
  #include <uv_pars_vertex>
  #include <uv_vertex>
  #include <uv2_pars_vertex>
  #include <uv2_vertex>
  #include <color_pars_vertex>
  #include <color_vertex>
  #include <morphcolor_vertex>
  #include <beginnormal_vertex>
  #include <morphnormal_vertex>
  #include <skinbase_vertex>
  #include <skinnormal_vertex>
  #include <defaultnormal_vertex>
  #include <normal_vertex>
  
  #ifndef FLAT_SHADED
  vNormal = normalize(transformedNormal);
  #ifdef USE_TANGENT
  vTangent = normalize(transformedTangent);
  vBitangent = normalize(cross(vNormal, vTangent) * tangent.w);
  #endif
  #endif
  
  #include <begin_vertex>
  #include <morphtarget_vertex>
  #include <skinning_vertex>
  #include <displacementmap_vertex>
  
  // Pass UV coordinates
  vUv = uv;

  // Calculate time-based animation
  float time = uTime * uSpeed;
  
  // For sphere, use spherical coordinates for better distortion
  float theta = atan(position.z, position.x);
  float phi = acos(position.y / length(position));
  
  // Create waves based on spherical coordinates
  float waveTheta = sin(theta * uWaveFrequency * 2.0 + time) * uWaveAmplitude;
  float wavePhi = cos(phi * uWaveFrequency + time * 1.5) * uWaveAmplitude;
  
  // Add noise for organic glass distortion
  vec3 noisePos = position + vec3(time * 0.1);
  float noise = snoise(noisePos * 0.8) * uNoiseStrength;
  
  // Calculate distortion based on position on sphere
  float distortionAmount = (waveTheta + wavePhi) * uDistortion + noise;
  vDistortion = distortionAmount;
  
  // Apply distortion along normal for sphere
  transformed += normal * distortionAmount;
  
  #include <project_vertex>
  #include <logdepthbuf_vertex>
  #include <clipping_planes_vertex>
  
  vViewPosition = -mvPosition.xyz;
  vPosition = transformed;
  
  // Calculate world position for refraction
  vec4 worldPosition = modelMatrix * vec4(transformed, 1.0);
  vGlassWorldPos = worldPosition.xyz;
  
  // Calculate reflection and refraction vectors
  vec3 worldNormal = normalize(mat3(modelMatrix) * normal);
  vec3 viewVector = normalize(cameraPosition - worldPosition.xyz);
  
  // Reflection vector
  vReflect = reflect(-viewVector, worldNormal);
  
  // Refraction vector with index of refraction for glass (1.5)
  // For sphere, adjust IOR based on curvature
  float ior = 1.5 + sin(theta * 2.0 + time) * 0.1;
  vRefract = refract(-viewVector, worldNormal, 1.0 / ior);
  
  #include <fog_vertex>
  #include <shadowmap_vertex>
}
`,Gt={};U(Gt,{fragment:()=>sr,vertex:()=>lr});var sr=`// Glass WaterPlane Fragment Shader - Liquid Glass Effect

#define STANDARD
#ifdef PHYSICAL
#define REFLECTIVITY
#define CLEARCOAT
#define TRANSMISSION
#endif

uniform vec3 diffuse;
uniform vec3 emissive;
uniform float roughness;
uniform float metalness;
uniform float opacity;

// transmission is already defined by Three.js when TRANSMISSION is enabled
#ifdef REFLECTIVITY
uniform float reflectivity;
#endif
#ifdef CLEARCOAT
uniform float clearcoat;
uniform float clearcoatRoughness;
#endif
#ifdef USE_SHEEN
uniform vec3 sheen;
#endif

varying vec3 vViewPosition;
#ifndef FLAT_SHADED
#ifdef USE_TANGENT
varying vec3 vTangent;
varying vec3 vBitangent;
#endif
#endif

#include <alphamap_pars_fragment>
#include <aomap_pars_fragment>
#include <color_pars_fragment>
#include <common>
#include <dithering_pars_fragment>
#include <emissivemap_pars_fragment>
#include <lightmap_pars_fragment>
#include <map_pars_fragment>
#include <packing>
#include <uv2_pars_fragment>
#include <uv_pars_fragment>
#include <bsdfs>
#include <bumpmap_pars_fragment>
#include <clearcoat_pars_fragment>
#include <clipping_planes_pars_fragment>
#include <envmap_common_pars_fragment>
#include <envmap_pars_fragment>
#include <envmap_physical_pars_fragment>
#include <fog_pars_fragment>
#include <lights_pars_begin>
#include <lights_physical_pars_fragment>
#include <logdepthbuf_pars_fragment>
#include <metalnessmap_pars_fragment>
#include <normalmap_pars_fragment>
#include <roughnessmap_pars_fragment>
#include <shadowmap_pars_fragment>
#include <transmission_pars_fragment>

// Custom uniforms for liquid glass effect
uniform float uTime;
uniform vec3 uColor1;
uniform vec3 uColor2;
uniform vec3 uColor3;
uniform float uTransparency;
uniform float uRefraction;
uniform float uChromaticAberration;
uniform float uFresnelPower;
uniform float uReflectivity;
// envMap and envMapIntensity are provided by Three.js
uniform float uLiquidEffect;
uniform float uFoamIntensity;

varying vec2 vUv;
varying vec3 vPosition;
varying vec3 vNormal;
varying vec3 vGlassWorldPos;
varying vec3 vReflect;
varying vec3 vRefract;
varying float vWaveHeight;
varying vec3 vWaveNormal;

// Fresnel calculation
float fresnel(vec3 viewDirection, vec3 normal, float power) {
  return pow(1.0 - abs(dot(viewDirection, normal)), power);
}

// Chromatic aberration for refraction
vec3 chromaticRefraction(vec3 viewDirection, vec3 normal, float ior, float chromaticStrength) {
  vec3 refractedR = refract(viewDirection, normal, 1.0 / (ior - chromaticStrength));
  vec3 refractedG = refract(viewDirection, normal, 1.0 / ior);
  vec3 refractedB = refract(viewDirection, normal, 1.0 / (ior + chromaticStrength));
  
  #ifdef ENVMAP_TYPE_CUBE
  vec3 result = vec3(
    textureCube(envMap, refractedR).r,
    textureCube(envMap, refractedG).g,
    textureCube(envMap, refractedB).b
  );
  
  // Add distortion based on wave height
  float distortion = vWaveHeight * 0.1;
  result = mix(result, textureCube(envMap, refractedG + vec3(distortion)).rgb, 0.3);
  #else
  vec3 result = vec3(0.5);
  #endif
  
  return result;
}

// Foam effect for water surface
float foam(vec2 uv, float waveHeight, float time) {
  float foamThreshold = 0.3;
  float foamAmount = smoothstep(foamThreshold - 0.1, foamThreshold + 0.1, abs(waveHeight));
  
  // Add foam texture pattern
  float foamPattern = sin(uv.x * 40.0 + time) * cos(uv.y * 30.0 - time * 0.5);
  foamPattern += sin(uv.x * 25.0 - time * 0.8) * sin(uv.y * 35.0 + time);
  foamPattern = clamp(foamPattern * 0.5 + 0.5, 0.0, 1.0);
  
  return foamAmount * foamPattern;
}

// Caustics for underwater effect
vec3 caustics(vec3 position, float time) {
  float c1 = sin(position.x * 6.0 + time * 1.5) * sin(position.z * 6.0 + time);
  float c2 = cos(position.x * 4.0 - time) * cos(position.z * 5.0 + time * 1.2);
  float c3 = sin((position.x + position.z) * 3.0 + time * 0.8);
  
  float causticPattern = (c1 + c2 + c3) / 3.0;
  causticPattern = pow(max(0.0, causticPattern), 2.0);
  
  return vec3(causticPattern) * vec3(0.3, 0.6, 1.0);
}

void main() {
  #include <clipping_planes_fragment>
  
  vec4 diffuseColor = vec4(diffuse, opacity);
  ReflectedLight reflectedLight = ReflectedLight(vec3(0.0), vec3(0.0), vec3(0.0), vec3(0.0));
  vec3 totalEmissiveRadiance = emissive;
  
  #include <logdepthbuf_fragment>
  #include <map_fragment>
  #include <color_fragment>
  #include <alphamap_fragment>
  #include <alphatest_fragment>
  #include <specularmap_fragment>
  #include <roughnessmap_fragment>
  #include <metalnessmap_fragment>
  #include <normal_fragment_begin>
  #include <normal_fragment_maps>
  #include <clearcoat_normal_fragment_begin>
  #include <clearcoat_normal_fragment_maps>
  #include <emissivemap_fragment>
  
  // Use wave normal for more accurate water surface
  vec3 viewDirection = normalize(vViewPosition);
  vec3 worldNormal = normalize(vWaveNormal);
  
  // Calculate Fresnel effect
  float fresnelFactor = fresnel(viewDirection, worldNormal, uFresnelPower);
  
  // Water color gradient with depth effect
  float depth = 1.0 - abs(vWaveHeight) * 2.0;
  vec3 shallowColor = mix(uColor1, uColor2, vUv.y);
  vec3 deepColor = mix(uColor2, uColor3, depth);
  vec3 gradientColor = mix(shallowColor, deepColor, fresnelFactor);
  
  // Add foam effect
  float foamAmount = foam(vUv, vWaveHeight, uTime) * uFoamIntensity;
  vec3 foamColor = vec3(1.0, 1.0, 1.0);
  gradientColor = mix(gradientColor, foamColor, foamAmount);
  
  // Reflection
  #ifdef ENVMAP_TYPE_CUBE
  vec3 reflectionColor = textureCube(envMap, vReflect).rgb * envMapIntensity;
  
  // Add slight blur to reflection for water effect
  vec3 blurredReflection = reflectionColor;
  for (int i = 0; i < 4; i++) {
    vec3 offset = vec3(
      sin(float(i) * 2.0) * 0.01,
      0.0,
      cos(float(i) * 2.0) * 0.01
    );
    blurredReflection += textureCube(envMap, vReflect + offset).rgb * envMapIntensity;
  }
  blurredReflection /= 5.0;
  reflectionColor = mix(reflectionColor, blurredReflection, uLiquidEffect);
  #else
  vec3 reflectionColor = vec3(0.5);
  #endif
  
  // Refraction with chromatic aberration (stronger for water)
  vec3 refractionColor;
  #ifdef ENVMAP_TYPE_CUBE
  if (uChromaticAberration > 0.0) {
    float waterIOR = 1.33 + vWaveHeight * 0.1;
    refractionColor = chromaticRefraction(-viewDirection, worldNormal, waterIOR, uChromaticAberration * 1.5);
  } else {
    refractionColor = textureCube(envMap, vRefract).rgb;
  }
  refractionColor *= envMapIntensity;
  #else
  refractionColor = vec3(0.3);
  #endif
  
  // Add caustics to refraction
  vec3 causticsColor = caustics(vGlassWorldPos, uTime);
  refractionColor += causticsColor * 0.3 * uLiquidEffect;
  
  // Mix reflection and refraction based on Fresnel and wave
  float reflectionMix = fresnelFactor * uReflectivity * (1.0 + abs(vWaveHeight));
  vec3 envColor = mix(refractionColor, reflectionColor, clamp(reflectionMix, 0.0, 1.0));
  
  // Combine all effects
  vec3 finalColor = mix(gradientColor, envColor, 0.85);
  
  // Apply transparency with wave variation
  float waveAlpha = 1.0 - abs(vWaveHeight) * 0.3;
  float finalAlpha = mix(uTransparency * waveAlpha, 1.0, fresnelFactor * 0.6 + foamAmount * 0.4);
  
  // Set diffuse color for standard lighting
  diffuseColor.rgb = finalColor;
  diffuseColor.a = finalAlpha;
  
  // Skip transmission_fragment to avoid conflicts
  
  vec3 outgoingLight = reflectedLight.directDiffuse + reflectedLight.indirectDiffuse + 
                       reflectedLight.directSpecular + reflectedLight.indirectSpecular + 
                       totalEmissiveRadiance;
  
  // Add our liquid glass color contribution
  outgoingLight += finalColor * 0.95;
  
  gl_FragColor = vec4(outgoingLight, diffuseColor.a);
  
  #include <tonemapping_fragment>
  #include <colorspace_fragment>
  #include <fog_fragment>
  #include <premultiplied_alpha_fragment>
  #include <dithering_fragment>
}
`,lr=`// Glass WaterPlane Vertex Shader - Liquid Glass Effect

#define STANDARD
varying vec3 vViewPosition;
#ifndef FLAT_SHADED
#ifdef USE_TANGENT
varying vec3 vTangent;
varying vec3 vBitangent;
#endif
#endif
#include <clipping_planes_pars_vertex>
#include <color_pars_vertex>
#include <common>
#include <displacementmap_pars_vertex>
#include <fog_pars_vertex>
#include <logdepthbuf_pars_vertex>
#include <morphtarget_pars_vertex>
#include <shadowmap_pars_vertex>
#include <skinning_pars_vertex>
#include <uv2_pars_vertex>
#include <uv_pars_vertex>

varying vec2 vUv;
varying vec3 vPosition;
varying vec3 vNormal;
varying vec3 vGlassWorldPos;
varying vec3 vReflect;
varying vec3 vRefract;
varying float vWaveHeight;
varying vec3 vWaveNormal;

uniform float uTime;
uniform float uSpeed;
uniform float uWaveAmplitude;
uniform float uWaveFrequency;
uniform float uNoiseStrength;
uniform float uDistortion;
uniform float uFlowSpeed;
uniform vec2 uFlowDirection;

// Noise functions for water-like glass distortion
vec3 mod289(vec3 x) {
  return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec4 mod289(vec4 x) {
  return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec4 permute(vec4 x) {
  return mod289(((x * 34.0) + 1.0) * x);
}

vec4 taylorInvSqrt(vec4 r) {
  return 1.79284291400159 - 0.85373472095314 * r;
}

float snoise(vec3 v) {
  const vec2 C = vec2(1.0 / 6.0, 1.0 / 3.0);
  const vec4 D = vec4(0.0, 0.5, 1.0, 2.0);

  vec3 i = floor(v + dot(v, C.yyy));
  vec3 x0 = v - i + dot(i, C.xxx);

  vec3 g = step(x0.yzx, x0.xyz);
  vec3 l = 1.0 - g;
  vec3 i1 = min(g.xyz, l.zxy);
  vec3 i2 = max(g.xyz, l.zxy);

  vec3 x1 = x0 - i1 + C.xxx;
  vec3 x2 = x0 - i2 + C.yyy;
  vec3 x3 = x0 - D.yyy;

  i = mod289(i);
  vec4 p = permute(permute(permute(
    i.z + vec4(0.0, i1.z, i2.z, 1.0))
    + i.y + vec4(0.0, i1.y, i2.y, 1.0))
    + i.x + vec4(0.0, i1.x, i2.x, 1.0));

  float n_ = 0.142857142857;
  vec3 ns = n_ * D.wyz - D.xzx;

  vec4 j = p - 49.0 * floor(p * ns.z * ns.z);

  vec4 x_ = floor(j * ns.z);
  vec4 y_ = floor(j - 7.0 * x_);

  vec4 x = x_ * ns.x + ns.yyyy;
  vec4 y = y_ * ns.x + ns.yyyy;
  vec4 h = 1.0 - abs(x) - abs(y);

  vec4 b0 = vec4(x.xy, y.xy);
  vec4 b1 = vec4(x.zw, y.zw);

  vec4 s0 = floor(b0) * 2.0 + 1.0;
  vec4 s1 = floor(b1) * 2.0 + 1.0;
  vec4 sh = -step(h, vec4(0.0));

  vec4 a0 = b0.xzyw + s0.xzyw * sh.xxyy;
  vec4 a1 = b1.xzyw + s1.xzyw * sh.zzww;

  vec3 p0 = vec3(a0.xy, h.x);
  vec3 p1 = vec3(a0.zw, h.y);
  vec3 p2 = vec3(a1.xy, h.z);
  vec3 p3 = vec3(a1.zw, h.w);

  vec4 norm = taylorInvSqrt(vec4(dot(p0, p0), dot(p1, p1), dot(p2, p2), dot(p3, p3)));
  p0 *= norm.x;
  p1 *= norm.y;
  p2 *= norm.z;
  p3 *= norm.w;

  vec4 m = max(0.6 - vec4(dot(x0, x0), dot(x1, x1), dot(x2, x2), dot(x3, x3)), 0.0);
  m = m * m;
  return 42.0 * dot(m * m, vec4(dot(p0, x0), dot(p1, x1),
    dot(p2, x2), dot(p3, x3)));
}

// Water wave function
vec3 waterWave(vec2 pos, float time) {
  // Flow effect
  vec2 flowPos = pos + uFlowDirection * time * uFlowSpeed;
  
  // Multiple wave layers for realistic water
  float wave1 = sin(flowPos.x * uWaveFrequency + time) * cos(flowPos.y * uWaveFrequency * 0.8 + time * 0.7);
  float wave2 = sin(flowPos.x * uWaveFrequency * 1.7 - time * 1.3) * sin(flowPos.y * uWaveFrequency * 1.3 + time);
  float wave3 = cos(flowPos.x * uWaveFrequency * 0.5 + time * 0.5) * sin(flowPos.y * uWaveFrequency * 0.6 - time * 0.8);
  
  // Combine waves
  float height = (wave1 * 0.5 + wave2 * 0.3 + wave3 * 0.2) * uWaveAmplitude;
  
  // Calculate wave normals
  float dx = cos(flowPos.x * uWaveFrequency + time) * uWaveFrequency * 0.5 * uWaveAmplitude;
  float dz = -sin(flowPos.y * uWaveFrequency * 0.8 + time * 0.7) * uWaveFrequency * 0.8 * 0.5 * uWaveAmplitude;
  
  return vec3(dx, height, dz);
}

void main() {
  #include <uv_pars_vertex>
  #include <uv_vertex>
  #include <uv2_pars_vertex>
  #include <uv2_vertex>
  #include <color_pars_vertex>
  #include <color_vertex>
  #include <morphcolor_vertex>
  #include <beginnormal_vertex>
  #include <morphnormal_vertex>
  #include <skinbase_vertex>
  #include <skinnormal_vertex>
  #include <defaultnormal_vertex>
  #include <normal_vertex>
  
  // Pass UV coordinates
  vUv = uv;

  // Calculate time-based animation
  float time = uTime * uSpeed;
  
  // Calculate water waves
  vec3 waveData = waterWave(position.xz, time);
  float waveHeight = waveData.y;
  vec2 waveGradient = waveData.xz;
  
  // Add noise for organic water movement
  vec3 noisePos = vec3(position.x, position.y, position.z) + vec3(time * 0.05);
  float noise = snoise(noisePos * 1.2) * uNoiseStrength * 0.5;
  
  // Store wave height for fragment shader
  vWaveHeight = waveHeight + noise;
  
  // Calculate perturbed normal for water surface
  vec3 waveNormal = normalize(vec3(-waveGradient.x, 1.0, -waveGradient.y));
  vWaveNormal = waveNormal;
  
  // Blend original normal with wave normal
  vec3 blendedNormal = normalize(mix(normal, waveNormal, 0.7));
  
  #ifndef FLAT_SHADED
  vNormal = normalize(mat3(modelViewMatrix) * blendedNormal);
  #ifdef USE_TANGENT
  vTangent = normalize(transformedTangent);
  vBitangent = normalize(cross(vNormal, vTangent) * tangent.w);
  #endif
  #endif
  
  #include <begin_vertex>
  #include <morphtarget_vertex>
  #include <skinning_vertex>
  #include <displacementmap_vertex>
  
  // Apply wave displacement and additional distortion
  transformed.y += waveHeight + noise;
  transformed += blendedNormal * uDistortion * noise;
  
  #include <project_vertex>
  #include <logdepthbuf_vertex>
  #include <clipping_planes_vertex>
  
  vViewPosition = -mvPosition.xyz;
  vPosition = transformed;
  
  // Calculate world position for refraction
  vec4 worldPosition = modelMatrix * vec4(transformed, 1.0);
  vGlassWorldPos = worldPosition.xyz;
  
  // Calculate reflection and refraction vectors with wave normal
  vec3 worldNormal = normalize(mat3(modelMatrix) * blendedNormal);
  vec3 viewVector = normalize(cameraPosition - worldPosition.xyz);
  
  // Reflection vector
  vReflect = reflect(-viewVector, worldNormal);
  
  // Refraction vector with varying IOR for water effect
  float ior = 1.33 + sin(time + position.x * 2.0) * 0.1; // Water IOR ~1.33
  vRefract = refract(-viewVector, worldNormal, 1.0 / ior);
  
  #include <fog_vertex>
  #include <shadowmap_vertex>
}
`,Wt={};U(Wt,{plane:()=>Yt,sphere:()=>$t,waterPlane:()=>Gt});var qt={};U(qt,{fragment:()=>cr,vertex:()=>ur});var cr=`uniform float uC1r;
uniform float uC1g;
uniform float uC1b;
uniform float uC2r;
uniform float uC2g;
uniform float uC2b;
uniform float uC3r;
uniform float uC3g;
uniform float uC3b;


varying vec3 vNormal;
varying vec3 vPos;

void main() {
  vec3 color1 = vec3(uC1r, uC1g, uC1b);
  vec3 color2 = vec3(uC2r, uC2g, uC2b);
  vec3 color3 = vec3(uC3r, uC3g, uC3b);

  gl_FragColor = vec4(color1 * vPos.x + color2 * vPos.y + color3 * vPos.z, 1.);

}
`,ur=`// #pragma glslify: cnoise3 = require(glsl-noise/classic/3d) 

// noise source from https://github.com/hughsk/glsl-noise/blob/master/periodic/3d.glsl

vec3 mod289(vec3 x)
{
  return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec4 mod289(vec4 x)
{
  return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec4 permute(vec4 x)
{
  return mod289(((x*34.0)+1.0)*x);
}

vec4 taylorInvSqrt(vec4 r)
{
  return 1.79284291400159 - 0.85373472095314 * r;
}

vec3 fade(vec3 t) {
  return t*t*t*(t*(t*6.0-15.0)+10.0);
}

float cnoise(vec3 P)
{
  vec3 Pi0 = floor(P); // Integer part for indexing
  vec3 Pi1 = Pi0 + vec3(1.0); // Integer part + 1
  Pi0 = mod289(Pi0);
  Pi1 = mod289(Pi1);
  vec3 Pf0 = fract(P); // Fractional part for interpolation
  vec3 Pf1 = Pf0 - vec3(1.0); // Fractional part - 1.0
  vec4 ix = vec4(Pi0.x, Pi1.x, Pi0.x, Pi1.x);
  vec4 iy = vec4(Pi0.yy, Pi1.yy);
  vec4 iz0 = Pi0.zzzz;
  vec4 iz1 = Pi1.zzzz;

  vec4 ixy = permute(permute(ix) + iy);
  vec4 ixy0 = permute(ixy + iz0);
  vec4 ixy1 = permute(ixy + iz1);

  vec4 gx0 = ixy0 * (1.0 / 7.0);
  vec4 gy0 = fract(floor(gx0) * (1.0 / 7.0)) - 0.5;
  gx0 = fract(gx0);
  vec4 gz0 = vec4(0.5) - abs(gx0) - abs(gy0);
  vec4 sz0 = step(gz0, vec4(0.0));
  gx0 -= sz0 * (step(0.0, gx0) - 0.5);
  gy0 -= sz0 * (step(0.0, gy0) - 0.5);

  vec4 gx1 = ixy1 * (1.0 / 7.0);
  vec4 gy1 = fract(floor(gx1) * (1.0 / 7.0)) - 0.5;
  gx1 = fract(gx1);
  vec4 gz1 = vec4(0.5) - abs(gx1) - abs(gy1);
  vec4 sz1 = step(gz1, vec4(0.0));
  gx1 -= sz1 * (step(0.0, gx1) - 0.5);
  gy1 -= sz1 * (step(0.0, gy1) - 0.5);

  vec3 g000 = vec3(gx0.x,gy0.x,gz0.x);
  vec3 g100 = vec3(gx0.y,gy0.y,gz0.y);
  vec3 g010 = vec3(gx0.z,gy0.z,gz0.z);
  vec3 g110 = vec3(gx0.w,gy0.w,gz0.w);
  vec3 g001 = vec3(gx1.x,gy1.x,gz1.x);
  vec3 g101 = vec3(gx1.y,gy1.y,gz1.y);
  vec3 g011 = vec3(gx1.z,gy1.z,gz1.z);
  vec3 g111 = vec3(gx1.w,gy1.w,gz1.w);

  vec4 norm0 = taylorInvSqrt(vec4(dot(g000, g000), dot(g010, g010), dot(g100, g100), dot(g110, g110)));
  g000 *= norm0.x;
  g010 *= norm0.y;
  g100 *= norm0.z;
  g110 *= norm0.w;
  vec4 norm1 = taylorInvSqrt(vec4(dot(g001, g001), dot(g011, g011), dot(g101, g101), dot(g111, g111)));
  g001 *= norm1.x;
  g011 *= norm1.y;
  g101 *= norm1.z;
  g111 *= norm1.w;

  float n000 = dot(g000, Pf0);
  float n100 = dot(g100, vec3(Pf1.x, Pf0.yz));
  float n010 = dot(g010, vec3(Pf0.x, Pf1.y, Pf0.z));
  float n110 = dot(g110, vec3(Pf1.xy, Pf0.z));
  float n001 = dot(g001, vec3(Pf0.xy, Pf1.z));
  float n101 = dot(g101, vec3(Pf1.x, Pf0.y, Pf1.z));
  float n011 = dot(g011, vec3(Pf0.x, Pf1.yz));
  float n111 = dot(g111, Pf1);

  vec3 fade_xyz = fade(Pf0);
  vec4 n_z = mix(vec4(n000, n100, n010, n110), vec4(n001, n101, n011, n111), fade_xyz.z);
  vec2 n_yz = mix(n_z.xy, n_z.zw, fade_xyz.y);
  float n_xyz = mix(n_yz.x, n_yz.y, fade_xyz.x); 
  return 2.2 * n_xyz;
}

//-------- start here ------------

mat3 rotation3dY(float angle) {
  float s = sin(angle);
  float c = cos(angle);

  return mat3(c, 0.0, -s, 0.0, 1.0, 0.0, s, 0.0, c);
}

vec3 rotateY(vec3 v, float angle) { return rotation3dY(angle) * v; }

varying vec3 vNormal;
varying float displacement;
varying vec3 vPos;
varying float vDistort;

varying vec2 vUv;

uniform float uTime;
uniform float uSpeed;

uniform float uLoadingTime;

uniform float uNoiseDensity;
uniform float uNoiseStrength;

#define STANDARD
varying vec3 vViewPosition;
#ifndef FLAT_SHADED
#ifdef USE_TANGENT
varying vec3 vTangent;
varying vec3 vBitangent;
#endif
#endif
#include <clipping_planes_pars_vertex>
#include <color_pars_vertex>
#include <common>
#include <displacementmap_pars_vertex>
#include <fog_pars_vertex>
#include <logdepthbuf_pars_vertex>
#include <morphtarget_pars_vertex>
#include <shadowmap_pars_vertex>
#include <skinning_pars_vertex>
#include <uv2_pars_vertex>
#include <uv_pars_vertex>

void main() {

  #include <beginnormal_vertex>
  #include <color_vertex>
  #include <defaultnormal_vertex>
  #include <morphnormal_vertex>
  #include <skinbase_vertex>
  #include <skinnormal_vertex>
  #include <uv2_vertex>
  #include <uv_vertex>
  #ifndef FLAT_SHADED
    vNormal = normalize(transformedNormal);
  #ifdef USE_TANGENT
    vTangent = normalize(transformedTangent);
    vBitangent = normalize(cross(vNormal, vTangent) * tangent.w);
  #endif
  #endif
  #include <begin_vertex>

  #include <clipping_planes_vertex>
  #include <displacementmap_vertex>
  #include <logdepthbuf_vertex>
  #include <morphtarget_vertex>
  #include <project_vertex>
  #include <skinning_vertex>
    vViewPosition = -mvPosition.xyz;
  #include <fog_vertex>
  #include <shadowmap_vertex>
  #include <worldpos_vertex>

  //-------- start vertex ------------
  vUv = uv;

  // vNormal = normal;

  float t = uTime * uSpeed;
  // Create a sine wave from top to bottom of the sphere
  float distortion = 0.75 * cnoise(0.43 * position * uNoiseDensity + t);

  vec3 pos = position + normal * distortion * uNoiseStrength * uLoadingTime;
  vPos = pos;

  gl_Position = projectionMatrix * modelViewMatrix * vec4(pos, 1.);
}
`,Zt={};U(Zt,{fragment:()=>fr,vertex:()=>dr});var fr=`
#define STANDARD
#ifdef PHYSICAL
#define REFLECTIVITY
#define CLEARCOAT
#define TRANSMISSION
#endif
uniform vec3 diffuse;
uniform vec3 emissive;
uniform float roughness;
uniform float metalness;
uniform float opacity;
#ifdef TRANSMISSION
uniform float transmission;
#endif
#ifdef REFLECTIVITY
uniform float reflectivity;
#endif
#ifdef CLEARCOAT
uniform float clearcoat;
uniform float clearcoatRoughness;
#endif
#ifdef USE_SHEEN
uniform vec3 sheen;
#endif
varying vec3 vViewPosition;
#ifndef FLAT_SHADED
#ifdef USE_TANGENT
varying vec3 vTangent;
varying vec3 vBitangent;
#endif
#endif
#include <alphamap_pars_fragment>
#include <aomap_pars_fragment>
#include <color_pars_fragment>
#include <common>
#include <dithering_pars_fragment>
#include <emissivemap_pars_fragment>
#include <lightmap_pars_fragment>
#include <map_pars_fragment>
#include <packing>
#include <uv2_pars_fragment>
#include <uv_pars_fragment>
// #include <transmissionmap_pars_fragment>
#include <bsdfs>
#include <bumpmap_pars_fragment>
#include <clearcoat_pars_fragment>
#include <clipping_planes_pars_fragment>
#include <cube_uv_reflection_fragment>
#include <envmap_common_pars_fragment>
#include <envmap_physical_pars_fragment>
#include <fog_pars_fragment>
#include <lights_pars_begin>
#include <lights_physical_pars_fragment>
#include <logdepthbuf_pars_fragment>
#include <metalnessmap_pars_fragment>
#include <normalmap_pars_fragment>
#include <roughnessmap_pars_fragment>
#include <shadowmap_pars_fragment>
// include를 통해 가져온 값은 대부분 환경, 빛 등을 계산하기 위해서 기본 fragment
// shader의 값들을 받아왔습니다. 일단은 무시하셔도 됩니다.
varying vec3 vNormal;
varying float displacement;
varying vec3 vPos;
varying float vDistort;
uniform float uC1r;
uniform float uC1g;
uniform float uC1b;
uniform float uC2r;
uniform float uC2g;
uniform float uC2b;
uniform float uC3r;
uniform float uC3g;
uniform float uC3b;
varying vec3 color1;
varying vec3 color2;
varying vec3 color3;
varying float distanceToCenter;
void main() {
  //-------- basic gradient ------------
  vec3 color1 = vec3(uC1r, uC1g, uC1b);
  vec3 color2 = vec3(uC2r, uC2g, uC2b);
  vec3 color3 = vec3(uC3r, uC3g, uC3b);
  float clearcoat = 1.0;
  float clearcoatRoughness = 0.5;
#include <clipping_planes_fragment>

  float distanceToCenter = distance(vPos, vec3(0, 0, 0));
  // distanceToCenter로 중심점과의 거리를 구함.

  vec4 diffuseColor =
      vec4(mix(color3, mix(color2, color1, smoothstep(-1.0, 1.0, vPos.y)),
               distanceToCenter),
           1);

  //-------- materiality ------------
  ReflectedLight reflectedLight =
      ReflectedLight(vec3(0.0), vec3(0.0), vec3(0.0), vec3(0.0));
  vec3 totalEmissiveRadiance = emissive;
#ifdef TRANSMISSION
  float totalTransmission = transmission;
#endif
#include <logdepthbuf_fragment>
#include <map_fragment>
#include <color_fragment>
#include <alphamap_fragment>
#include <alphatest_fragment>
#include <roughnessmap_fragment>
#include <metalnessmap_fragment>
#include <normal_fragment_begin>
#include <normal_fragment_maps>
#include <clearcoat_normal_fragment_begin>
#include <clearcoat_normal_fragment_maps>
#include <emissivemap_fragment>
// #include <transmissionmap_fragment>
#include <lights_physical_fragment>
#include <lights_fragment_begin>
#include <lights_fragment_maps>
#include <lights_fragment_end>
#include <aomap_fragment>
  vec3 outgoingLight =
      reflectedLight.directDiffuse + reflectedLight.indirectDiffuse +
      reflectedLight.directSpecular + reflectedLight.indirectSpecular;
//위에서 정의한 diffuseColor에 환경이나 반사값들을 반영한 값.
#ifdef TRANSMISSION
  diffuseColor.a *=
      mix(saturate(1. - totalTransmission +
                   linearToRelativeLuminance(reflectedLight.directSpecular +
                                             reflectedLight.indirectSpecular)),
          1.0, metalness);
#endif
  gl_FragColor = vec4(outgoingLight, diffuseColor.a);
  // gl_FragColor가 fragment shader를 통해 나타나는 최종값으로, diffuseColor에서
  // 정의한 그라디언트 색상 위에 반사나 빛을 계산한 값을 최종값으로 정의.
  // gl_FragColor = vec4(mix(mix(color1, color3, smoothstep(-3.0, 3.0,vPos.x)),
  // color2, vNormal.z), 1.0); 위처럼 최종값을 그라디언트 값 자체를 넣으면 환경
  // 영향없는 그라디언트만 표현됨.

#include <tonemapping_fragment>
#include <encodings_fragment>
#include <fog_fragment>
#include <premultiplied_alpha_fragment>
#include <dithering_fragment>
}
`,dr=`// #pragma glslify: pnoise = require(glsl-noise/periodic/3d)

vec3 mod289(vec3 x)
{
  return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec4 mod289(vec4 x)
{
  return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec4 permute(vec4 x)
{
  return mod289(((x*34.0)+1.0)*x);
}

vec4 taylorInvSqrt(vec4 r)
{
  return 1.79284291400159 - 0.85373472095314 * r;
}

vec3 fade(vec3 t) {
  return t*t*t*(t*(t*6.0-15.0)+10.0);
}

// Classic Perlin noise, periodic variant
float pnoise(vec3 P, vec3 rep)
{
  vec3 Pi0 = mod(floor(P), rep); // Integer part, modulo period
  vec3 Pi1 = mod(Pi0 + vec3(1.0), rep); // Integer part + 1, mod period
  Pi0 = mod289(Pi0);
  Pi1 = mod289(Pi1);
  vec3 Pf0 = fract(P); // Fractional part for interpolation
  vec3 Pf1 = Pf0 - vec3(1.0); // Fractional part - 1.0
  vec4 ix = vec4(Pi0.x, Pi1.x, Pi0.x, Pi1.x);
  vec4 iy = vec4(Pi0.yy, Pi1.yy);
  vec4 iz0 = Pi0.zzzz;
  vec4 iz1 = Pi1.zzzz;

  vec4 ixy = permute(permute(ix) + iy);
  vec4 ixy0 = permute(ixy + iz0);
  vec4 ixy1 = permute(ixy + iz1);

  vec4 gx0 = ixy0 * (1.0 / 7.0);
  vec4 gy0 = fract(floor(gx0) * (1.0 / 7.0)) - 0.5;
  gx0 = fract(gx0);
  vec4 gz0 = vec4(0.5) - abs(gx0) - abs(gy0);
  vec4 sz0 = step(gz0, vec4(0.0));
  gx0 -= sz0 * (step(0.0, gx0) - 0.5);
  gy0 -= sz0 * (step(0.0, gy0) - 0.5);

  vec4 gx1 = ixy1 * (1.0 / 7.0);
  vec4 gy1 = fract(floor(gx1) * (1.0 / 7.0)) - 0.5;
  gx1 = fract(gx1);
  vec4 gz1 = vec4(0.5) - abs(gx1) - abs(gy1);
  vec4 sz1 = step(gz1, vec4(0.0));
  gx1 -= sz1 * (step(0.0, gx1) - 0.5);
  gy1 -= sz1 * (step(0.0, gy1) - 0.5);

  vec3 g000 = vec3(gx0.x,gy0.x,gz0.x);
  vec3 g100 = vec3(gx0.y,gy0.y,gz0.y);
  vec3 g010 = vec3(gx0.z,gy0.z,gz0.z);
  vec3 g110 = vec3(gx0.w,gy0.w,gz0.w);
  vec3 g001 = vec3(gx1.x,gy1.x,gz1.x);
  vec3 g101 = vec3(gx1.y,gy1.y,gz1.y);
  vec3 g011 = vec3(gx1.z,gy1.z,gz1.z);
  vec3 g111 = vec3(gx1.w,gy1.w,gz1.w);

  vec4 norm0 = taylorInvSqrt(vec4(dot(g000, g000), dot(g010, g010), dot(g100, g100), dot(g110, g110)));
  g000 *= norm0.x;
  g010 *= norm0.y;
  g100 *= norm0.z;
  g110 *= norm0.w;
  vec4 norm1 = taylorInvSqrt(vec4(dot(g001, g001), dot(g011, g011), dot(g101, g101), dot(g111, g111)));
  g001 *= norm1.x;
  g011 *= norm1.y;
  g101 *= norm1.z;
  g111 *= norm1.w;

  float n000 = dot(g000, Pf0);
  float n100 = dot(g100, vec3(Pf1.x, Pf0.yz));
  float n010 = dot(g010, vec3(Pf0.x, Pf1.y, Pf0.z));
  float n110 = dot(g110, vec3(Pf1.xy, Pf0.z));
  float n001 = dot(g001, vec3(Pf0.xy, Pf1.z));
  float n101 = dot(g101, vec3(Pf1.x, Pf0.y, Pf1.z));
  float n011 = dot(g011, vec3(Pf0.x, Pf1.yz));
  float n111 = dot(g111, Pf1);

  vec3 fade_xyz = fade(Pf0);
  vec4 n_z = mix(vec4(n000, n100, n010, n110), vec4(n001, n101, n011, n111), fade_xyz.z);
  vec2 n_yz = mix(n_z.xy, n_z.zw, fade_xyz.y);
  float n_xyz = mix(n_yz.x, n_yz.y, fade_xyz.x);
  return 2.2 * n_xyz;
}


//-------- start here ------------

varying vec3 vNormal;
uniform float uTime;
uniform float uSpeed;
uniform float uNoiseDensity;
uniform float uNoiseStrength;
uniform float uFrequency;
uniform float uAmplitude;
varying vec3 vPos;
varying float vDistort;
varying vec2 vUv;
varying vec3 vViewPosition;

#define STANDARD
#ifndef FLAT_SHADED
  #ifdef USE_TANGENT
    varying vec3 vTangent;
    varying vec3 vBitangent;
  #endif
#endif

#include <clipping_planes_pars_vertex>
#include <color_pars_vertex>
#include <common>
#include <displacementmap_pars_vertex>
#include <fog_pars_vertex>
#include <logdepthbuf_pars_vertex>
#include <morphtarget_pars_vertex>
#include <shadowmap_pars_vertex>
#include <skinning_pars_vertex>
#include <uv2_pars_vertex>
#include <uv_pars_vertex>


// rotation
mat3 rotation3dY(float angle) {
  float s = sin(angle);
  float c = cos(angle);
  return mat3(c, 0.0, -s, 0.0, 1.0, 0.0, s, 0.0, c);
}

vec3 rotateY(vec3 v, float angle) { return rotation3dY(angle) * v; }

void main() {
  #include <beginnormal_vertex>
  #include <color_vertex>
  #include <defaultnormal_vertex>
  #include <morphnormal_vertex>
  #include <skinbase_vertex>
  #include <skinnormal_vertex>
  #include <uv2_vertex>
  #include <uv_vertex>
  #ifndef FLAT_SHADED
    vNormal = normalize(transformedNormal);
  #ifdef USE_TANGENT
    vTangent = normalize(transformedTangent);
    vBitangent = normalize(cross(vNormal, vTangent) * tangent.w);
  #endif
  #endif
  #include <begin_vertex>

  #include <clipping_planes_vertex>
  #include <displacementmap_vertex>
  #include <logdepthbuf_vertex>
  #include <morphtarget_vertex>
  #include <project_vertex>
  #include <skinning_vertex>
    vViewPosition = -mvPosition.xyz;
  #include <fog_vertex>
  #include <shadowmap_vertex>
  #include <worldpos_vertex>

  //-------- start vertex ------------
  float t = uTime * uSpeed;
  float distortion =
      pnoise((normal + t) * uNoiseDensity, vec3(10.0)) * uNoiseStrength;
  vec3 pos = position + (normal * distortion);
  float angle = sin(uv.y * uFrequency + t) * uAmplitude;
  pos = rotateY(pos, angle);

  vPos = pos;
  vDistort = distortion;
  vNormal = normal;
  vUv = uv;

  gl_Position = projectionMatrix * modelViewMatrix * vec4(pos, 1.);
}
`,Xt={};U(Xt,{fragment:()=>mr,vertex:()=>vr});var mr=`uniform float uC1r;
uniform float uC1g;
uniform float uC1b;
uniform float uC2r;
uniform float uC2g;
uniform float uC2b;
uniform float uC3r;
uniform float uC3g;
uniform float uC3b;


varying vec3 vNormal;
varying vec3 vPos;

void main() {
  vec3 color1 = vec3(uC1r, uC1g, uC1b);
  vec3 color2 = vec3(uC2r, uC2g, uC2b);
  vec3 color3 = vec3(uC3r, uC3g, uC3b);

  gl_FragColor = vec4(color1 * vPos.x + color2 * vPos.y + color3 * vPos.z, 1.);

}
`,vr=`// #pragma glslify: cnoise3 = require(glsl-noise/classic/3d) 

// noise source from https://github.com/hughsk/glsl-noise/blob/master/periodic/3d.glsl

vec3 mod289(vec3 x)
{
  return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec4 mod289(vec4 x)
{
  return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec4 permute(vec4 x)
{
  return mod289(((x*34.0)+1.0)*x);
}

vec4 taylorInvSqrt(vec4 r)
{
  return 1.79284291400159 - 0.85373472095314 * r;
}

vec3 fade(vec3 t) {
  return t*t*t*(t*(t*6.0-15.0)+10.0);
}

float cnoise(vec3 P)
{
  vec3 Pi0 = floor(P); // Integer part for indexing
  vec3 Pi1 = Pi0 + vec3(1.0); // Integer part + 1
  Pi0 = mod289(Pi0);
  Pi1 = mod289(Pi1);
  vec3 Pf0 = fract(P); // Fractional part for interpolation
  vec3 Pf1 = Pf0 - vec3(1.0); // Fractional part - 1.0
  vec4 ix = vec4(Pi0.x, Pi1.x, Pi0.x, Pi1.x);
  vec4 iy = vec4(Pi0.yy, Pi1.yy);
  vec4 iz0 = Pi0.zzzz;
  vec4 iz1 = Pi1.zzzz;

  vec4 ixy = permute(permute(ix) + iy);
  vec4 ixy0 = permute(ixy + iz0);
  vec4 ixy1 = permute(ixy + iz1);

  vec4 gx0 = ixy0 * (1.0 / 7.0);
  vec4 gy0 = fract(floor(gx0) * (1.0 / 7.0)) - 0.5;
  gx0 = fract(gx0);
  vec4 gz0 = vec4(0.5) - abs(gx0) - abs(gy0);
  vec4 sz0 = step(gz0, vec4(0.0));
  gx0 -= sz0 * (step(0.0, gx0) - 0.5);
  gy0 -= sz0 * (step(0.0, gy0) - 0.5);

  vec4 gx1 = ixy1 * (1.0 / 7.0);
  vec4 gy1 = fract(floor(gx1) * (1.0 / 7.0)) - 0.5;
  gx1 = fract(gx1);
  vec4 gz1 = vec4(0.5) - abs(gx1) - abs(gy1);
  vec4 sz1 = step(gz1, vec4(0.0));
  gx1 -= sz1 * (step(0.0, gx1) - 0.5);
  gy1 -= sz1 * (step(0.0, gy1) - 0.5);

  vec3 g000 = vec3(gx0.x,gy0.x,gz0.x);
  vec3 g100 = vec3(gx0.y,gy0.y,gz0.y);
  vec3 g010 = vec3(gx0.z,gy0.z,gz0.z);
  vec3 g110 = vec3(gx0.w,gy0.w,gz0.w);
  vec3 g001 = vec3(gx1.x,gy1.x,gz1.x);
  vec3 g101 = vec3(gx1.y,gy1.y,gz1.y);
  vec3 g011 = vec3(gx1.z,gy1.z,gz1.z);
  vec3 g111 = vec3(gx1.w,gy1.w,gz1.w);

  vec4 norm0 = taylorInvSqrt(vec4(dot(g000, g000), dot(g010, g010), dot(g100, g100), dot(g110, g110)));
  g000 *= norm0.x;
  g010 *= norm0.y;
  g100 *= norm0.z;
  g110 *= norm0.w;
  vec4 norm1 = taylorInvSqrt(vec4(dot(g001, g001), dot(g011, g011), dot(g101, g101), dot(g111, g111)));
  g001 *= norm1.x;
  g011 *= norm1.y;
  g101 *= norm1.z;
  g111 *= norm1.w;

  float n000 = dot(g000, Pf0);
  float n100 = dot(g100, vec3(Pf1.x, Pf0.yz));
  float n010 = dot(g010, vec3(Pf0.x, Pf1.y, Pf0.z));
  float n110 = dot(g110, vec3(Pf1.xy, Pf0.z));
  float n001 = dot(g001, vec3(Pf0.xy, Pf1.z));
  float n101 = dot(g101, vec3(Pf1.x, Pf0.y, Pf1.z));
  float n011 = dot(g011, vec3(Pf0.x, Pf1.yz));
  float n111 = dot(g111, Pf1);

  vec3 fade_xyz = fade(Pf0);
  vec4 n_z = mix(vec4(n000, n100, n010, n110), vec4(n001, n101, n011, n111), fade_xyz.z);
  vec2 n_yz = mix(n_z.xy, n_z.zw, fade_xyz.y);
  float n_xyz = mix(n_yz.x, n_yz.y, fade_xyz.x); 
  return 2.2 * n_xyz;
}

//-------- start here ------------

mat3 rotation3dY(float angle) {
  float s = sin(angle);
  float c = cos(angle);

  return mat3(c, 0.0, -s, 0.0, 1.0, 0.0, s, 0.0, c);
}

vec3 rotateY(vec3 v, float angle) { return rotation3dY(angle) * v; }

varying vec3 vNormal;
varying float displacement;
varying vec3 vPos;
varying float vDistort;

varying vec2 vUv;

uniform float uTime;
uniform float uSpeed;

uniform float uLoadingTime;

uniform float uNoiseDensity;
uniform float uNoiseStrength;

#define STANDARD
varying vec3 vViewPosition;
#ifndef FLAT_SHADED
#ifdef USE_TANGENT
varying vec3 vTangent;
varying vec3 vBitangent;
#endif
#endif
#include <clipping_planes_pars_vertex>
#include <color_pars_vertex>
#include <common>
#include <displacementmap_pars_vertex>
#include <fog_pars_vertex>
#include <logdepthbuf_pars_vertex>
#include <morphtarget_pars_vertex>
#include <shadowmap_pars_vertex>
#include <skinning_pars_vertex>
#include <uv2_pars_vertex>
#include <uv_pars_vertex>

void main() {

  #include <beginnormal_vertex>
  #include <color_vertex>
  #include <defaultnormal_vertex>
  #include <morphnormal_vertex>
  #include <skinbase_vertex>
  #include <skinnormal_vertex>
  #include <uv2_vertex>
  #include <uv_vertex>
  #ifndef FLAT_SHADED
    vNormal = normalize(transformedNormal);
  #ifdef USE_TANGENT
    vTangent = normalize(transformedTangent);
    vBitangent = normalize(cross(vNormal, vTangent) * tangent.w);
  #endif
  #endif
  #include <begin_vertex>

  #include <clipping_planes_vertex>
  #include <displacementmap_vertex>
  #include <logdepthbuf_vertex>
  #include <morphtarget_vertex>
  #include <project_vertex>
  #include <skinning_vertex>
    vViewPosition = -mvPosition.xyz;
  #include <fog_vertex>
  #include <shadowmap_vertex>
  #include <worldpos_vertex>

  //-------- start vertex ------------
  vUv = uv;

  // vNormal = normal;

  float t = uTime * uSpeed;
  // Create a sine wave from top to bottom of the sphere
  float distortion = 0.75 * cnoise(0.43 * position * uNoiseDensity + t);

  vec3 pos = position + normal * distortion * uNoiseStrength * uLoadingTime;
  vPos = pos;

  gl_Position = projectionMatrix * modelViewMatrix * vec4(pos, 1.);
}
`,Kt={};U(Kt,{plane:()=>qt,sphere:()=>Zt,waterPlane:()=>Xt});var Qt={};U(Qt,{fragment:()=>gr,vertex:()=>pr});var gr=`// Cosmic Plane Fragment Shader - Holographic Gradient

#define STANDARD
#ifdef PHYSICAL
#define REFLECTIVITY
#define CLEARCOAT
#define TRANSMISSION
#endif

uniform vec3 diffuse;
uniform vec3 emissive;
uniform float roughness;
uniform float metalness;
uniform float opacity;

#ifdef TRANSMISSION
uniform float transmission;
#endif
#ifdef REFLECTIVITY
uniform float reflectivity;
#endif
#ifdef CLEARCOAT
uniform float clearcoat;
uniform float clearcoatRoughness;
#endif
#ifdef USE_SHEEN
uniform vec3 sheen;
#endif
varying vec3 vViewPosition;
#ifndef FLAT_SHADED
#ifdef USE_TANGENT
varying vec3 vTangent;
varying vec3 vBitangent;
#endif
#endif
#include <alphamap_pars_fragment>
#include <aomap_pars_fragment>
#include <color_pars_fragment>
#include <common>
#include <dithering_pars_fragment>
#include <emissivemap_pars_fragment>
#include <lightmap_pars_fragment>
#include <map_pars_fragment>
#include <packing>
#include <uv2_pars_fragment>
#include <uv_pars_fragment>
#include <bsdfs>
#include <bumpmap_pars_fragment>
#include <clearcoat_pars_fragment>
#include <clipping_planes_pars_fragment>
#include <cube_uv_reflection_fragment>
#include <envmap_common_pars_fragment>
#include <envmap_physical_pars_fragment>
#include <fog_pars_fragment>
#include <lights_pars_begin>
#include <lights_physical_pars_fragment>
#include <logdepthbuf_pars_fragment>
#include <metalnessmap_pars_fragment>
#include <normalmap_pars_fragment>
#include <roughnessmap_pars_fragment>
#include <shadowmap_pars_fragment>

varying vec3 vNormal;
varying float displacement;
varying vec3 vPos;
varying float vDistort;
varying vec2 vUv;
varying float vHolographicIntensity;
varying float vCosmicWave;

uniform float uTime;
uniform float uSpeed;

uniform float uC1r;
uniform float uC1g;
uniform float uC1b;
uniform float uC2r;
uniform float uC2g;
uniform float uC2b;
uniform float uC3r;
uniform float uC3g;
uniform float uC3b;

// Holographic helper functions
float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453123);
}

float noise2D(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 u = f * f * (3.0 - 2.0 * f);
    
    return mix(mix(hash(i + vec2(0.0, 0.0)), 
                   hash(i + vec2(1.0, 0.0)), u.x),
               mix(hash(i + vec2(0.0, 1.0)), 
                   hash(i + vec2(1.0, 1.0)), u.x), u.y);
}

// for npm package, need to add this manually
float linearToRelativeLuminance2( const in vec3 color ) {
    vec3 weights = vec3( 0.2126, 0.7152, 0.0722 );
    return dot( weights, color.rgb );
}

void main() {

  //-------- Cosmic Holographic Gradient ------------
  vec3 color1 = vec3(uC1r, uC1g, uC1b);
  vec3 color2 = vec3(uC2r, uC2g, uC2b);
  vec3 color3 = vec3(uC3r, uC3g, uC3b);
  
  float clearcoat = 1.0;
  float clearcoatRoughness = 0.2; // More reflective for holographic effect

  #include <clipping_planes_fragment>

  float t = uTime * uSpeed;
  
  // Create holographic interference patterns
  float interference1 = sin(vPos.x * 20.0 + t * 3.0) * cos(vPos.y * 15.0 + t * 2.0);
  float interference2 = sin(vPos.x * 35.0 + t * 4.0) * sin(vPos.y * 30.0 + t * 3.5);
  float interference3 = cos(vPos.x * 50.0 + t * 5.0) * cos(vPos.y * 45.0 + t * 4.5);
  
  // Combine interference patterns
  float holographicPattern = (interference1 + interference2 * 0.5 + interference3 * 0.25) / 1.75;
  
  // Create cosmic shimmer effect
  float shimmer = noise2D(vPos.xy * 40.0 + t * 2.0) * 0.3;
  float cosmicGlow = noise2D(vPos.xy * 8.0 + t * 0.5) * 0.5;
  
  // Holographic color shifting
  vec3 holographicShift = vec3(
    sin(vPos.x * 10.0 + t * 2.0 + 0.0) * 0.1,
    sin(vPos.x * 10.0 + t * 2.0 + 2.094) * 0.1,  // 120 degrees
    sin(vPos.x * 10.0 + t * 2.0 + 4.188) * 0.1   // 240 degrees
  );
  
  // Enhanced gradient mixing with cosmic effects
  float gradientX = smoothstep(-4.0, 4.0, vPos.x + holographicPattern * 2.0);
  float gradientY = smoothstep(-4.0, 4.0, vPos.y + vCosmicWave * 1.5);
  float gradientZ = smoothstep(-2.0, 2.0, vPos.z + shimmer);
  
  // Multi-layer color mixing for depth
  vec3 baseGradient = mix(
    mix(color1, color2, gradientX), 
    color3, 
    gradientY * 0.7 + gradientZ * 0.3
  );
  
  // Apply holographic color shifts
  vec3 holographicColor = baseGradient + holographicShift;
  
  // Add cosmic glow and shimmer
  vec3 cosmicEnhancement = vec3(
    cosmicGlow * 0.2,
    shimmer * 0.15,
    (cosmicGlow + shimmer) * 0.1
  );
  
  // Holographic intensity modulation
  float intensityMod = 1.0 + vHolographicIntensity * 0.5 + abs(holographicPattern) * 0.3;
  
  // Final color with cosmic and holographic effects
  vec3 finalColor = (holographicColor + cosmicEnhancement) * intensityMod;
  
  // Add subtle iridescence
  float iridescence = sin(vPos.x * 25.0 + t * 3.0) * cos(vPos.y * 20.0 + t * 2.5) * 0.1;
  finalColor += vec3(iridescence * 0.2, iridescence * 0.3, iridescence * 0.4);

  vec4 diffuseColor = vec4(finalColor, 1.0);

  //-------- Enhanced Materiality for Holographic Effect ------------
  ReflectedLight reflectedLight = ReflectedLight(vec3(0.0), vec3(0.0), vec3(0.0), vec3(0.0));
  vec3 totalEmissiveRadiance = emissive + finalColor * 0.1; // Add some emission for glow

  #ifdef TRANSMISSION
    float totalTransmission = transmission;
  #endif
  #include <logdepthbuf_fragment>
  #include <map_fragment>
  #include <color_fragment>
  #include <alphamap_fragment>
  #include <alphatest_fragment>
  #include <roughnessmap_fragment>
  #include <metalnessmap_fragment>
  #include <normal_fragment_begin>
  #include <normal_fragment_maps>
  #include <clearcoat_normal_fragment_begin>
  #include <clearcoat_normal_fragment_maps>
  #include <emissivemap_fragment>
  #include <lights_physical_fragment>
  #include <lights_fragment_begin>
  #include <lights_fragment_maps>
  #include <lights_fragment_end>
  #include <aomap_fragment>
  
  vec3 outgoingLight = reflectedLight.directDiffuse + reflectedLight.indirectDiffuse +
                      reflectedLight.directSpecular + reflectedLight.indirectSpecular +
                      totalEmissiveRadiance;

  #ifdef TRANSMISSION
    diffuseColor.a *= mix(saturate(1. - totalTransmission +
                        linearToRelativeLuminance2(reflectedLight.directSpecular +
                                                  reflectedLight.indirectSpecular)),
                1.0, metalness);
  #endif

  #include <tonemapping_fragment>
  #include <encodings_fragment>
  #include <fog_fragment>
  #include <premultiplied_alpha_fragment>
  #include <dithering_fragment>

  gl_FragColor = vec4(outgoingLight, diffuseColor.a);
}
`,pr=`// Cosmic Plane Vertex Shader - Holographic Effect
// #pragma glslify: cnoise3 = require(glsl-noise/classic/3d) 

// noise source from https://github.com/hughsk/glsl-noise/blob/master/periodic/3d.glsl

vec3 mod289(vec3 x)
{
  return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec4 mod289(vec4 x)
{
  return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec4 permute(vec4 x)
{
  return mod289(((x*34.0)+1.0)*x);
}

vec4 taylorInvSqrt(vec4 r)
{
  return 1.79284291400159 - 0.85373472095314 * r;
}

vec3 fade(vec3 t) {
  return t*t*t*(t*(t*6.0-15.0)+10.0);
}

float cnoise(vec3 P)
{
  vec3 Pi0 = floor(P); // Integer part for indexing
  vec3 Pi1 = Pi0 + vec3(1.0); // Integer part + 1
  Pi0 = mod289(Pi0);
  Pi1 = mod289(Pi1);
  vec3 Pf0 = fract(P); // Fractional part for interpolation
  vec3 Pf1 = Pf0 - vec3(1.0); // Fractional part - 1.0
  vec4 ix = vec4(Pi0.x, Pi1.x, Pi0.x, Pi1.x);
  vec4 iy = vec4(Pi0.yy, Pi1.yy);
  vec4 iz0 = Pi0.zzzz;
  vec4 iz1 = Pi1.zzzz;

  vec4 ixy = permute(permute(ix) + iy);
  vec4 ixy0 = permute(ixy + iz0);
  vec4 ixy1 = permute(ixy + iz1);

  vec4 gx0 = ixy0 * (1.0 / 7.0);
  vec4 gy0 = fract(floor(gx0) * (1.0 / 7.0)) - 0.5;
  gx0 = fract(gx0);
  vec4 gz0 = vec4(0.5) - abs(gx0) - abs(gy0);
  vec4 sz0 = step(gz0, vec4(0.0));
  gx0 -= sz0 * (step(0.0, gx0) - 0.5);
  gy0 -= sz0 * (step(0.0, gy0) - 0.5);

  vec4 gx1 = ixy1 * (1.0 / 7.0);
  vec4 gy1 = fract(floor(gx1) * (1.0 / 7.0)) - 0.5;
  gx1 = fract(gx1);
  vec4 gz1 = vec4(0.5) - abs(gx1) - abs(gy1);
  vec4 sz1 = step(gz1, vec4(0.0));
  gx1 -= sz1 * (step(0.0, gx1) - 0.5);
  gy1 -= sz1 * (step(0.0, gy1) - 0.5);

  vec3 g000 = vec3(gx0.x,gy0.x,gz0.x);
  vec3 g100 = vec3(gx0.y,gy0.y,gz0.y);
  vec3 g010 = vec3(gx0.z,gy0.z,gz0.z);
  vec3 g110 = vec3(gx0.w,gy0.w,gz0.w);
  vec3 g001 = vec3(gx1.x,gy1.x,gz1.x);
  vec3 g101 = vec3(gx1.y,gy1.y,gz1.y);
  vec3 g011 = vec3(gx1.z,gy1.z,gz1.z);
  vec3 g111 = vec3(gx1.w,gy1.w,gz1.w);

  vec4 norm0 = taylorInvSqrt(vec4(dot(g000, g000), dot(g010, g010), dot(g100, g100), dot(g110, g110)));
  g000 *= norm0.x;
  g010 *= norm0.y;
  g100 *= norm0.z;
  g110 *= norm0.w;
  vec4 norm1 = taylorInvSqrt(vec4(dot(g001, g001), dot(g011, g011), dot(g101, g101), dot(g111, g111)));
  g001 *= norm1.x;
  g011 *= norm1.y;
  g101 *= norm1.z;
  g111 *= norm1.w;

  float n000 = dot(g000, Pf0);
  float n100 = dot(g100, vec3(Pf1.x, Pf0.yz));
  float n010 = dot(g010, vec3(Pf0.x, Pf1.y, Pf0.z));
  float n110 = dot(g110, vec3(Pf1.xy, Pf0.z));
  float n001 = dot(g001, vec3(Pf0.xy, Pf1.z));
  float n101 = dot(g101, vec3(Pf1.x, Pf0.y, Pf1.z));
  float n011 = dot(g011, vec3(Pf0.x, Pf1.yz));
  float n111 = dot(g111, Pf1);

  vec3 fade_xyz = fade(Pf0);
  vec4 n_z = mix(vec4(n000, n100, n010, n110), vec4(n001, n101, n011, n111), fade_xyz.z);
  vec2 n_yz = mix(n_z.xy, n_z.zw, fade_xyz.y);
  float n_xyz = mix(n_yz.x, n_yz.y, fade_xyz.x); 
  return 2.2 * n_xyz;
}

//-------- Holographic Effect Functions ------------

mat3 rotation3dY(float angle) {
  float s = sin(angle);
  float c = cos(angle);
  return mat3(c, 0.0, -s, 0.0, 1.0, 0.0, s, 0.0, c);
}

mat3 rotation3dX(float angle) {
  float s = sin(angle);
  float c = cos(angle);
  return mat3(1.0, 0.0, 0.0, 0.0, c, s, 0.0, -s, c);
}

vec3 rotateY(vec3 v, float angle) { return rotation3dY(angle) * v; }
vec3 rotateX(vec3 v, float angle) { return rotation3dX(angle) * v; }

varying vec3 vNormal;
varying float displacement;
varying vec3 vPos;
varying float vDistort;
varying vec2 vUv;
varying float vHolographicIntensity;
varying float vCosmicWave;

uniform float uTime;
uniform float uSpeed;
uniform float uLoadingTime;
uniform float uNoiseDensity;
uniform float uNoiseStrength;

#define STANDARD
varying vec3 vViewPosition;
#ifndef FLAT_SHADED
#ifdef USE_TANGENT
varying vec3 vTangent;
varying vec3 vBitangent;
#endif
#endif
#include <clipping_planes_pars_vertex>
#include <color_pars_vertex>
#include <common>
#include <displacementmap_pars_vertex>
#include <fog_pars_vertex>
#include <logdepthbuf_pars_vertex>
#include <morphtarget_pars_vertex>
#include <shadowmap_pars_vertex>
#include <skinning_pars_vertex>
#include <uv2_pars_vertex>
#include <uv_pars_vertex>

void main() {

  #include <beginnormal_vertex>
  #include <color_vertex>
  #include <defaultnormal_vertex>
  #include <morphnormal_vertex>
  #include <skinbase_vertex>
  #include <skinnormal_vertex>
  #include <uv2_vertex>
  #include <uv_vertex>
  #ifndef FLAT_SHADED
    vNormal = normalize(transformedNormal);
  #ifdef USE_TANGENT
    vTangent = normalize(transformedTangent);
    vBitangent = normalize(cross(vNormal, vTangent) * tangent.w);
  #endif
  #endif
  #include <begin_vertex>

  #include <clipping_planes_vertex>
  #include <displacementmap_vertex>
  #include <logdepthbuf_vertex>
  #include <morphtarget_vertex>
  #include <project_vertex>
  #include <skinning_vertex>
    vViewPosition = -mvPosition.xyz;
  #include <fog_vertex>
  #include <shadowmap_vertex>
  #include <worldpos_vertex>

  //-------- Cosmic Holographic Effect ------------
  vUv = uv;
  
  float t = uTime * uSpeed;
  
  // Create holographic interference patterns
  float holographicPattern = sin(position.x * 15.0 + t * 2.0) * 
                            sin(position.y * 12.0 + t * 1.5) * 0.1;
  
  // Cosmic wave distortion
  float cosmicWave = cnoise(position * uNoiseDensity * 0.5 + vec3(t * 0.3, t * 0.2, t * 0.4));
  vCosmicWave = cosmicWave;
  
  // Multi-layer noise for depth
  float noise1 = cnoise(position * uNoiseDensity * 2.0 + t * 0.8);
  float noise2 = cnoise(position * uNoiseDensity * 0.3 + t * 0.2) * 0.5;
  float noise3 = cnoise(position * uNoiseDensity * 4.0 + t * 1.2) * 0.25;
  
  float combinedNoise = noise1 + noise2 + noise3;
  
  // Holographic shimmer effect
  float shimmer = sin(position.x * 30.0 + t * 4.0) * 
                  cos(position.y * 25.0 + t * 3.0) * 0.05;
  
  // Calculate holographic intensity for fragment shader
  vHolographicIntensity = abs(holographicPattern) + abs(shimmer) * 2.0;
  
  // Apply displacement with holographic and cosmic effects
  float totalDisplacement = (combinedNoise + holographicPattern + shimmer) * uNoiseStrength * uLoadingTime;
  
  vec3 pos = position + normal * totalDisplacement;
  vPos = pos;
  
  // Add subtle rotation effect for cosmic feel
  pos = rotateY(pos, sin(t * 0.1) * 0.05);
  pos = rotateX(pos, cos(t * 0.07) * 0.03);

  gl_Position = projectionMatrix * modelViewMatrix * vec4(pos, 1.0);
}
`,Jt={};U(Jt,{fragment:()=>hr,vertex:()=>_r});var hr=`// Cosmic Sphere Fragment Shader - Nebula Particle Effect

#define STANDARD
#ifdef PHYSICAL
#define REFLECTIVITY
#define CLEARCOAT
#define TRANSMISSION
#endif

uniform vec3 diffuse;
uniform vec3 emissive;
uniform float roughness;
uniform float metalness;
uniform float opacity;

#ifdef TRANSMISSION
uniform float transmission;
#endif
#ifdef REFLECTIVITY
uniform float reflectivity;
#endif
#ifdef CLEARCOAT
uniform float clearcoat;
uniform float clearcoatRoughness;
#endif
#ifdef USE_SHEEN
uniform vec3 sheen;
#endif
varying vec3 vViewPosition;
#ifndef FLAT_SHADED
#ifdef USE_TANGENT
varying vec3 vTangent;
varying vec3 vBitangent;
#endif
#endif
#include <alphamap_pars_fragment>
#include <aomap_pars_fragment>
#include <color_pars_fragment>
#include <common>
#include <dithering_pars_fragment>
#include <emissivemap_pars_fragment>
#include <lightmap_pars_fragment>
#include <map_pars_fragment>
#include <packing>
#include <uv2_pars_fragment>
#include <uv_pars_fragment>
#include <bsdfs>
#include <bumpmap_pars_fragment>
#include <clearcoat_pars_fragment>
#include <clipping_planes_pars_fragment>
#include <cube_uv_reflection_fragment>
#include <envmap_common_pars_fragment>
#include <envmap_physical_pars_fragment>
#include <fog_pars_fragment>
#include <lights_pars_begin>
#include <lights_physical_pars_fragment>
#include <logdepthbuf_pars_fragment>
#include <metalnessmap_pars_fragment>
#include <normalmap_pars_fragment>
#include <roughnessmap_pars_fragment>
#include <shadowmap_pars_fragment>

varying vec3 vNormal;
varying float displacement;
varying vec3 vPos;
varying float vDistort;
varying vec2 vUv;
varying float vNebulaIntensity;
varying float vParticleDensity;
varying vec3 vCosmicSwirl;

uniform float uTime;
uniform float uSpeed;

uniform float uC1r;
uniform float uC1g;
uniform float uC1b;
uniform float uC2r;
uniform float uC2g;
uniform float uC2b;
uniform float uC3r;
uniform float uC3g;
uniform float uC3b;

// Nebula helper functions
float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453123);
}

float noise2D(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 u = f * f * (3.0 - 2.0 * f);
    
    return mix(mix(hash(i + vec2(0.0, 0.0)), 
                   hash(i + vec2(1.0, 0.0)), u.x),
               mix(hash(i + vec2(0.0, 1.0)), 
                   hash(i + vec2(1.0, 1.0)), u.x), u.y);
}

// Fractal Brownian Motion for complex nebula patterns
float fbm(vec2 p) {
    float value = 0.0;
    float amplitude = 0.5;
    float frequency = 1.0;
    
    for(int i = 0; i < 5; i++) {
        value += amplitude * noise2D(p * frequency);
        amplitude *= 0.5;
        frequency *= 2.0;
    }
    return value;
}

// Star field generation
float stars(vec2 p, float density) {
    vec2 n = floor(p * density);
    vec2 f = fract(p * density);
    
    float d = 1.0;
    for(int i = -1; i <= 1; i++) {
        for(int j = -1; j <= 1; j++) {
            vec2 g = vec2(float(i), float(j));
            vec2 o = hash(n + g) * vec2(1.0);
            vec2 r = g + o - f;
            d = min(d, dot(r, r));
        }
    }
    
    return 1.0 - smoothstep(0.0, 0.02, sqrt(d));
}

// for npm package, need to add this manually
float linearToRelativeLuminance2( const in vec3 color ) {
    vec3 weights = vec3( 0.2126, 0.7152, 0.0722 );
    return dot( weights, color.rgb );
}

void main() {

  //-------- Cosmic Nebula Gradient ------------
  vec3 color1 = vec3(uC1r, uC1g, uC1b);
  vec3 color2 = vec3(uC2r, uC2g, uC2b);
  vec3 color3 = vec3(uC3r, uC3g, uC3b);
  
  float clearcoat = 1.0;
  float clearcoatRoughness = 0.1; // Very reflective for cosmic shine

  #include <clipping_planes_fragment>

  float t = uTime * uSpeed;
  
  // Calculate distance from center for radial effects
  float distanceFromCenter = length(vPos);
  float angle = atan(vPos.y, vPos.x);
  
  // Create complex nebula patterns using FBM
  vec2 nebulaCoords = vPos.xy * 3.0 + vCosmicSwirl.xy;
  float nebulaPattern1 = fbm(nebulaCoords + t * 0.1);
  float nebulaPattern2 = fbm(nebulaCoords * 2.0 + t * 0.15);
  float nebulaPattern3 = fbm(nebulaCoords * 4.0 + t * 0.2);
  
  // Combine nebula patterns
  float combinedNebula = (nebulaPattern1 + nebulaPattern2 * 0.5 + nebulaPattern3 * 0.25) / 1.75;
  
  // Create particle-like bright spots
  float particleField = stars(vPos.xy * 20.0 + t * 0.5, 50.0);
  float microParticles = stars(vPos.xy * 80.0 + t * 1.0, 200.0) * 0.5;
  
  // Create cosmic dust clouds
  float dustClouds = fbm(vPos.xy * 8.0 + t * 0.05) * 0.3;
  
  // Energy streams
  float energyStream1 = sin(vPos.x * 15.0 + t * 3.0 + angle * 2.0) * 0.1;
  float energyStream2 = cos(vPos.y * 20.0 + t * 2.5 + distanceFromCenter * 5.0) * 0.1;
  
  // Cosmic gradient mixing with nebula influence
  float gradientX = smoothstep(-3.0, 3.0, vPos.x + combinedNebula * 2.0 + vCosmicSwirl.x * 3.0);
  float gradientY = smoothstep(-3.0, 3.0, vPos.y + vNebulaIntensity * 1.5 + vCosmicSwirl.y * 2.0);
  float gradientZ = smoothstep(-2.0, 2.0, vPos.z + dustClouds * 2.0);
  
  // Multi-layer color mixing
  vec3 baseGradient = mix(
    mix(color1, color2, gradientX), 
    color3, 
    gradientY * 0.6 + gradientZ * 0.4
  );
  
  // Add nebula color variations
  vec3 nebulaColor = baseGradient;
  nebulaColor.r += combinedNebula * 0.3 + energyStream1;
  nebulaColor.g += vNebulaIntensity * 0.2 + energyStream2;
  nebulaColor.b += dustClouds * 0.4 + abs(vCosmicSwirl.z) * 0.5;
  
  // Add particle brightness
  vec3 particleGlow = vec3(
    particleField * 0.8 + microParticles * 0.4,
    particleField * 0.6 + microParticles * 0.3,
    particleField * 0.9 + microParticles * 0.5
  );
  
  // Create pulsing cosmic energy
  float cosmicPulse = sin(t * 1.5 + distanceFromCenter * 3.0) * 0.1 + 1.0;
  
  // Combine all effects
  vec3 finalColor = (nebulaColor + particleGlow * 2.0) * cosmicPulse;
  
  // Add cosmic rim lighting effect
  float rimLight = pow(1.0 - abs(dot(normalize(vNormal), normalize(vViewPosition))), 2.0);
  finalColor += rimLight * 0.3 * (color1 + color2 + color3) / 3.0;
  
  // Enhance particle density areas
  finalColor = mix(finalColor, finalColor * 1.5, vParticleDensity * 0.5);
  
  // Add subtle color temperature variation
  float temperature = sin(angle * 3.0 + t * 0.8) * 0.1;
  finalColor.r += temperature * 0.1;
  finalColor.b -= temperature * 0.1;

  vec4 diffuseColor = vec4(finalColor, 1.0);

  //-------- Enhanced Materiality for Cosmic Effect ------------
  ReflectedLight reflectedLight = ReflectedLight(vec3(0.0), vec3(0.0), vec3(0.0), vec3(0.0));
  vec3 totalEmissiveRadiance = emissive + finalColor * 0.2; // Strong emission for nebula glow

  #ifdef TRANSMISSION
    float totalTransmission = transmission;
  #endif
  #include <logdepthbuf_fragment>
  #include <map_fragment>
  #include <color_fragment>
  #include <alphamap_fragment>
  #include <alphatest_fragment>
  #include <roughnessmap_fragment>
  #include <metalnessmap_fragment>
  #include <normal_fragment_begin>
  #include <normal_fragment_maps>
  #include <clearcoat_normal_fragment_begin>
  #include <clearcoat_normal_fragment_maps>
  #include <emissivemap_fragment>
  #include <lights_physical_fragment>
  #include <lights_fragment_begin>
  #include <lights_fragment_maps>
  #include <lights_fragment_end>
  #include <aomap_fragment>
  
  vec3 outgoingLight = reflectedLight.directDiffuse + reflectedLight.indirectDiffuse +
                      reflectedLight.directSpecular + reflectedLight.indirectSpecular +
                      totalEmissiveRadiance;

  #ifdef TRANSMISSION
    diffuseColor.a *= mix(saturate(1. - totalTransmission +
                        linearToRelativeLuminance2(reflectedLight.directSpecular +
                                                  reflectedLight.indirectSpecular)),
                1.0, metalness);
  #endif

  #include <tonemapping_fragment>
  #include <encodings_fragment>
  #include <fog_fragment>
  #include <premultiplied_alpha_fragment>
  #include <dithering_fragment>

  gl_FragColor = vec4(outgoingLight, diffuseColor.a);
}
`,_r=`// Cosmic Sphere Vertex Shader - Nebula Effect
// #pragma glslify: cnoise3 = require(glsl-noise/classic/3d) 

// noise source from https://github.com/hughsk/glsl-noise/blob/master/periodic/3d.glsl

vec3 mod289(vec3 x)
{
  return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec4 mod289(vec4 x)
{
  return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec4 permute(vec4 x)
{
  return mod289(((x*34.0)+1.0)*x);
}

vec4 taylorInvSqrt(vec4 r)
{
  return 1.79284291400159 - 0.85373472095314 * r;
}

vec3 fade(vec3 t) {
  return t*t*t*(t*(t*6.0-15.0)+10.0);
}

float cnoise(vec3 P)
{
  vec3 Pi0 = floor(P); // Integer part for indexing
  vec3 Pi1 = Pi0 + vec3(1.0); // Integer part + 1
  Pi0 = mod289(Pi0);
  Pi1 = mod289(Pi1);
  vec3 Pf0 = fract(P); // Fractional part for interpolation
  vec3 Pf1 = Pf0 - vec3(1.0); // Fractional part - 1.0
  vec4 ix = vec4(Pi0.x, Pi1.x, Pi0.x, Pi1.x);
  vec4 iy = vec4(Pi0.yy, Pi1.yy);
  vec4 iz0 = Pi0.zzzz;
  vec4 iz1 = Pi1.zzzz;

  vec4 ixy = permute(permute(ix) + iy);
  vec4 ixy0 = permute(ixy + iz0);
  vec4 ixy1 = permute(ixy + iz1);

  vec4 gx0 = ixy0 * (1.0 / 7.0);
  vec4 gy0 = fract(floor(gx0) * (1.0 / 7.0)) - 0.5;
  gx0 = fract(gx0);
  vec4 gz0 = vec4(0.5) - abs(gx0) - abs(gy0);
  vec4 sz0 = step(gz0, vec4(0.0));
  gx0 -= sz0 * (step(0.0, gx0) - 0.5);
  gy0 -= sz0 * (step(0.0, gy0) - 0.5);

  vec4 gx1 = ixy1 * (1.0 / 7.0);
  vec4 gy1 = fract(floor(gx1) * (1.0 / 7.0)) - 0.5;
  gx1 = fract(gx1);
  vec4 gz1 = vec4(0.5) - abs(gx1) - abs(gy1);
  vec4 sz1 = step(gz1, vec4(0.0));
  gx1 -= sz1 * (step(0.0, gx1) - 0.5);
  gy1 -= sz1 * (step(0.0, gy1) - 0.5);

  vec3 g000 = vec3(gx0.x,gy0.x,gz0.x);
  vec3 g100 = vec3(gx0.y,gy0.y,gz0.y);
  vec3 g010 = vec3(gx0.z,gy0.z,gz0.z);
  vec3 g110 = vec3(gx0.w,gy0.w,gz0.w);
  vec3 g001 = vec3(gx1.x,gy1.x,gz1.x);
  vec3 g101 = vec3(gx1.y,gy1.y,gz1.y);
  vec3 g011 = vec3(gx1.z,gy1.z,gz1.z);
  vec3 g111 = vec3(gx1.w,gy1.w,gz1.w);

  vec4 norm0 = taylorInvSqrt(vec4(dot(g000, g000), dot(g010, g010), dot(g100, g100), dot(g110, g110)));
  g000 *= norm0.x;
  g010 *= norm0.y;
  g100 *= norm0.z;
  g110 *= norm0.w;
  vec4 norm1 = taylorInvSqrt(vec4(dot(g001, g001), dot(g011, g011), dot(g101, g101), dot(g111, g111)));
  g001 *= norm1.x;
  g011 *= norm1.y;
  g101 *= norm1.z;
  g111 *= norm1.w;

  float n000 = dot(g000, Pf0);
  float n100 = dot(g100, vec3(Pf1.x, Pf0.yz));
  float n010 = dot(g010, vec3(Pf0.x, Pf1.y, Pf0.z));
  float n110 = dot(g110, vec3(Pf1.xy, Pf0.z));
  float n001 = dot(g001, vec3(Pf0.xy, Pf1.z));
  float n101 = dot(g101, vec3(Pf1.x, Pf0.y, Pf1.z));
  float n011 = dot(g011, vec3(Pf0.x, Pf1.yz));
  float n111 = dot(g111, Pf1);

  vec3 fade_xyz = fade(Pf0);
  vec4 n_z = mix(vec4(n000, n100, n010, n110), vec4(n001, n101, n011, n111), fade_xyz.z);
  vec2 n_yz = mix(n_z.xy, n_z.zw, fade_xyz.y);
  float n_xyz = mix(n_yz.x, n_yz.y, fade_xyz.x); 
  return 2.2 * n_xyz;
}

//-------- Nebula Effect Functions ------------

mat3 rotation3dY(float angle) {
  float s = sin(angle);
  float c = cos(angle);
  return mat3(c, 0.0, -s, 0.0, 1.0, 0.0, s, 0.0, c);
}

mat3 rotation3dX(float angle) {
  float s = sin(angle);
  float c = cos(angle);
  return mat3(1.0, 0.0, 0.0, 0.0, c, s, 0.0, -s, c);
}

mat3 rotation3dZ(float angle) {
  float s = sin(angle);
  float c = cos(angle);
  return mat3(c, s, 0.0, -s, c, 0.0, 0.0, 0.0, 1.0);
}

vec3 rotateY(vec3 v, float angle) { return rotation3dY(angle) * v; }
vec3 rotateX(vec3 v, float angle) { return rotation3dX(angle) * v; }
vec3 rotateZ(vec3 v, float angle) { return rotation3dZ(angle) * v; }

varying vec3 vNormal;
varying float displacement;
varying vec3 vPos;
varying float vDistort;
varying vec2 vUv;
varying float vNebulaIntensity;
varying float vParticleDensity;
varying vec3 vCosmicSwirl;

uniform float uTime;
uniform float uSpeed;
uniform float uLoadingTime;
uniform float uNoiseDensity;
uniform float uNoiseStrength;

#define STANDARD
varying vec3 vViewPosition;
#ifndef FLAT_SHADED
#ifdef USE_TANGENT
varying vec3 vTangent;
varying vec3 vBitangent;
#endif
#endif
#include <clipping_planes_pars_vertex>
#include <color_pars_vertex>
#include <common>
#include <displacementmap_pars_vertex>
#include <fog_pars_vertex>
#include <logdepthbuf_pars_vertex>
#include <morphtarget_pars_vertex>
#include <shadowmap_pars_vertex>
#include <skinning_pars_vertex>
#include <uv2_pars_vertex>
#include <uv_pars_vertex>

void main() {

  #include <beginnormal_vertex>
  #include <color_vertex>
  #include <defaultnormal_vertex>
  #include <morphnormal_vertex>
  #include <skinbase_vertex>
  #include <skinnormal_vertex>
  #include <uv2_vertex>
  #include <uv_vertex>
  #ifndef FLAT_SHADED
    vNormal = normalize(transformedNormal);
  #ifdef USE_TANGENT
    vTangent = normalize(transformedTangent);
    vBitangent = normalize(cross(vNormal, vTangent) * tangent.w);
  #endif
  #endif
  #include <begin_vertex>

  #include <clipping_planes_vertex>
  #include <displacementmap_vertex>
  #include <logdepthbuf_vertex>
  #include <morphtarget_vertex>
  #include <project_vertex>
  #include <skinning_vertex>
    vViewPosition = -mvPosition.xyz;
  #include <fog_vertex>
  #include <shadowmap_vertex>
  #include <worldpos_vertex>

  //-------- Cosmic Nebula Effect ------------
  vUv = uv;
  
  float t = uTime * uSpeed;
  
  // Create swirling nebula patterns
  vec3 swirlCenter = vec3(0.0, 0.0, 0.0);
  vec3 toCenter = position - swirlCenter;
  float distanceFromCenter = length(toCenter);
  
  // Create spiral motion
  float angle = atan(toCenter.y, toCenter.x);
  float spiralAngle = angle + distanceFromCenter * 2.0 + t * 0.5;
  
  // Multi-octave noise for nebula density
  float nebula1 = cnoise(position * uNoiseDensity * 0.8 + vec3(t * 0.2, t * 0.3, t * 0.1));
  float nebula2 = cnoise(position * uNoiseDensity * 1.5 + vec3(t * 0.4, t * 0.2, t * 0.5)) * 0.7;
  float nebula3 = cnoise(position * uNoiseDensity * 3.0 + vec3(t * 0.8, t * 0.6, t * 0.9)) * 0.4;
  float nebula4 = cnoise(position * uNoiseDensity * 6.0 + vec3(t * 1.2, t * 1.0, t * 1.4)) * 0.2;
  
  // Combine nebula layers for complexity
  float nebulaPattern = nebula1 + nebula2 + nebula3 + nebula4;
  vNebulaIntensity = abs(nebulaPattern);
  
  // Create particle-like density variations
  float particleDensity = cnoise(position * uNoiseDensity * 8.0 + vec3(t * 2.0, t * 1.5, t * 2.5));
  vParticleDensity = smoothstep(-0.3, 0.8, particleDensity);
  
  // Create cosmic swirl effect
  vec3 swirl = vec3(
    sin(spiralAngle + t * 0.3) * distanceFromCenter * 0.1,
    cos(spiralAngle + t * 0.2) * distanceFromCenter * 0.1,
    sin(distanceFromCenter * 3.0 + t * 0.4) * 0.05
  );
  vCosmicSwirl = swirl;
  
  // Create pulsing effect for cosmic energy
  float pulse = sin(t * 2.0 + distanceFromCenter * 5.0) * 0.1 + 1.0;
  
  // Apply complex displacement
  float totalDisplacement = nebulaPattern * uNoiseStrength * uLoadingTime * pulse;
  
  // Add swirl displacement
  vec3 pos = position + normal * totalDisplacement + swirl * 0.3;
  vPos = pos;
  
  // Add cosmic rotation for dynamic feel
  pos = rotateY(pos, sin(t * 0.1 + distanceFromCenter) * 0.1);
  pos = rotateX(pos, cos(t * 0.08 + angle) * 0.08);
  pos = rotateZ(pos, sin(t * 0.05 + spiralAngle) * 0.05);

  gl_Position = projectionMatrix * modelViewMatrix * vec4(pos, 1.0);
}
`,ei={};U(ei,{fragment:()=>xr,vertex:()=>yr});var xr=`// Cosmic WaterPlane Fragment Shader - Aurora Wave Effect

#define STANDARD
#ifdef PHYSICAL
#define REFLECTIVITY
#define CLEARCOAT
#define TRANSMISSION
#endif

uniform vec3 diffuse;
uniform vec3 emissive;
uniform float roughness;
uniform float metalness;
uniform float opacity;

#ifdef TRANSMISSION
uniform float transmission;
#endif
#ifdef REFLECTIVITY
uniform float reflectivity;
#endif
#ifdef CLEARCOAT
uniform float clearcoat;
uniform float clearcoatRoughness;
#endif
#ifdef USE_SHEEN
uniform vec3 sheen;
#endif
varying vec3 vViewPosition;
#ifndef FLAT_SHADED
#ifdef USE_TANGENT
varying vec3 vTangent;
varying vec3 vBitangent;
#endif
#endif
#include <alphamap_pars_fragment>
#include <aomap_pars_fragment>
#include <color_pars_fragment>
#include <common>
#include <dithering_pars_fragment>
#include <emissivemap_pars_fragment>
#include <lightmap_pars_fragment>
#include <map_pars_fragment>
#include <packing>
#include <uv2_pars_fragment>
#include <uv_pars_fragment>
#include <bsdfs>
#include <bumpmap_pars_fragment>
#include <clearcoat_pars_fragment>
#include <clipping_planes_pars_fragment>
#include <cube_uv_reflection_fragment>
#include <envmap_common_pars_fragment>
#include <envmap_physical_pars_fragment>
#include <fog_pars_fragment>
#include <lights_pars_begin>
#include <lights_physical_pars_fragment>
#include <logdepthbuf_pars_fragment>
#include <metalnessmap_pars_fragment>
#include <normalmap_pars_fragment>
#include <roughnessmap_pars_fragment>
#include <shadowmap_pars_fragment>

varying vec3 vNormal;
varying float displacement;
varying vec3 vPos;
varying float vDistort;
varying vec2 vUv;
varying float vAuroraIntensity;
varying float vWaveHeight;
varying vec3 vFlowDirection;

uniform float uTime;
uniform float uSpeed;

uniform float uC1r;
uniform float uC1g;
uniform float uC1b;
uniform float uC2r;
uniform float uC2g;
uniform float uC2b;
uniform float uC3r;
uniform float uC3g;
uniform float uC3b;

// Aurora helper functions
float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453123);
}

float noise2D(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 u = f * f * (3.0 - 2.0 * f);
    
    return mix(mix(hash(i + vec2(0.0, 0.0)), 
                   hash(i + vec2(1.0, 0.0)), u.x),
               mix(hash(i + vec2(0.0, 1.0)), 
                   hash(i + vec2(1.0, 1.0)), u.x), u.y);
}

// Fractal Brownian Motion for aurora patterns
float fbm(vec2 p) {
    float value = 0.0;
    float amplitude = 0.5;
    float frequency = 1.0;
    
    for(int i = 0; i < 4; i++) {
        value += amplitude * noise2D(p * frequency);
        amplitude *= 0.5;
        frequency *= 2.0;
    }
    return value;
}

// Aurora curtain effect
float aurora(vec2 p, float time) {
    vec2 q = vec2(fbm(p + vec2(0.0, time * 0.1)),
                  fbm(p + vec2(5.2, time * 0.15)));
    
    vec2 r = vec2(fbm(p + 4.0 * q + vec2(1.7, time * 0.2)),
                  fbm(p + 4.0 * q + vec2(8.3, time * 0.18)));
    
    return fbm(p + 4.0 * r);
}

// Water caustics effect
float caustics(vec2 p, float time) {
    vec2 uv = p * 4.0;
    vec2 p0 = uv + vec2(time * 0.3, time * 0.2);
    vec2 p1 = uv + vec2(time * -0.4, time * 0.3);
    
    float c1 = sin(length(p0) * 8.0 - time * 2.0) * 0.5 + 0.5;
    float c2 = sin(length(p1) * 6.0 - time * 1.5) * 0.5 + 0.5;
    
    return (c1 + c2) * 0.5;
}

// for npm package, need to add this manually
float linearToRelativeLuminance2( const in vec3 color ) {
    vec3 weights = vec3( 0.2126, 0.7152, 0.0722 );
    return dot( weights, color.rgb );
}

void main() {

  //-------- Cosmic Aurora Water Gradient ------------
  vec3 color1 = vec3(uC1r, uC1g, uC1b);
  vec3 color2 = vec3(uC2r, uC2g, uC2b);
  vec3 color3 = vec3(uC3r, uC3g, uC3b);
  
  float clearcoat = 1.0;
  float clearcoatRoughness = 0.05; // Very smooth for water-like reflection

  #include <clipping_planes_fragment>

  float t = uTime * uSpeed;
  
  // Create aurora patterns
  vec2 auroraCoords = vPos.xy * 2.0 + vFlowDirection.xy * t * 0.5;
  float auroraPattern1 = aurora(auroraCoords, t);
  float auroraPattern2 = aurora(auroraCoords * 1.5 + vec2(3.0, 1.0), t * 1.2);
  float auroraPattern3 = aurora(auroraCoords * 0.7 + vec2(-2.0, 4.0), t * 0.8);
  
  // Combine aurora layers
  float combinedAurora = (auroraPattern1 + auroraPattern2 * 0.7 + auroraPattern3 * 0.5) / 2.2;
  
  // Create water caustics
  float causticsPattern = caustics(vPos.xy, t);
  
  // Create flowing light streams
  float lightStream1 = sin(vPos.x * 8.0 + t * 2.0 + combinedAurora * 3.0) * 0.2;
  float lightStream2 = cos(vPos.y * 6.0 + t * 1.5 + vWaveHeight * 4.0) * 0.15;
  float lightStream3 = sin((vPos.x + vPos.y) * 10.0 + t * 2.5) * 0.1;
  
  // Create cosmic energy waves
  float distanceFromCenter = length(vPos.xy);
  float energyWave = sin(distanceFromCenter * 5.0 - t * 3.0) * 
                     exp(-distanceFromCenter * 0.05) * 0.3;
  
  // Aurora color shifting effect
  vec3 auroraShift = vec3(
    sin(combinedAurora * 6.28 + t * 1.0) * 0.2,
    sin(combinedAurora * 6.28 + t * 1.0 + 2.094) * 0.2,  // 120 degrees
    sin(combinedAurora * 6.28 + t * 1.0 + 4.188) * 0.2   // 240 degrees
  );
  
  // Enhanced gradient mixing with aurora and water effects
  float gradientX = smoothstep(-4.0, 4.0, vPos.x + combinedAurora * 3.0 + vFlowDirection.x * 2.0);
  float gradientY = smoothstep(-4.0, 4.0, vPos.y + vWaveHeight * 2.0 + lightStream1 * 3.0);
  float gradientZ = smoothstep(-3.0, 3.0, vPos.z + causticsPattern * 2.0);
  
  // Multi-layer color mixing
  vec3 baseGradient = mix(
    mix(color1, color2, gradientX), 
    color3, 
    gradientY * 0.7 + gradientZ * 0.3
  );
  
  // Apply aurora color shifts
  vec3 auroraColor = baseGradient + auroraShift;
  
  // Add water caustics coloring
  vec3 causticsColor = vec3(
    causticsPattern * 0.3,
    causticsPattern * 0.4,
    causticsPattern * 0.5
  );
  
  // Add light streams
  vec3 lightStreams = vec3(
    abs(lightStream1) * 0.4,
    abs(lightStream2) * 0.3,
    abs(lightStream3) * 0.5
  );
  
  // Aurora intensity modulation
  float auroraIntensityMod = 1.0 + vAuroraIntensity * 0.8 + abs(combinedAurora) * 0.6;
  
  // Combine all effects
  vec3 finalColor = (auroraColor + causticsColor + lightStreams + vec3(energyWave * 0.2)) * auroraIntensityMod;
  
  // Add water-like shimmer
  float shimmer = sin(vPos.x * 20.0 + t * 4.0) * 
                  cos(vPos.y * 18.0 + t * 3.5) * 
                  vWaveHeight * 0.1;
  finalColor += vec3(shimmer * 0.3, shimmer * 0.4, shimmer * 0.6);
  
  // Add aurora dancing effect
  float auroraMovement = sin(vPos.x * 3.0 + t * 1.2 + combinedAurora * 2.0) * 
                         cos(vPos.y * 2.5 + t * 0.9) * 0.15;
  finalColor.g += abs(auroraMovement) * 0.4;
  finalColor.b += abs(auroraMovement) * 0.2;
  
  // Add cosmic depth variation
  float depthVariation = noise2D(vPos.xy * 5.0 + t * 0.3) * 0.1;
  finalColor *= (1.0 + depthVariation);

  vec4 diffuseColor = vec4(finalColor, 1.0);

  //-------- Enhanced Materiality for Water Aurora Effect ------------
  ReflectedLight reflectedLight = ReflectedLight(vec3(0.0), vec3(0.0), vec3(0.0), vec3(0.0));
  vec3 totalEmissiveRadiance = emissive + finalColor * 0.15; // Moderate emission for aurora glow

  #ifdef TRANSMISSION
    float totalTransmission = transmission;
  #endif
  #include <logdepthbuf_fragment>
  #include <map_fragment>
  #include <color_fragment>
  #include <alphamap_fragment>
  #include <alphatest_fragment>
  #include <roughnessmap_fragment>
  #include <metalnessmap_fragment>
  #include <normal_fragment_begin>
  #include <normal_fragment_maps>
  #include <clearcoat_normal_fragment_begin>
  #include <clearcoat_normal_fragment_maps>
  #include <emissivemap_fragment>
  #include <lights_physical_fragment>
  #include <lights_fragment_begin>
  #include <lights_fragment_maps>
  #include <lights_fragment_end>
  #include <aomap_fragment>
  
  vec3 outgoingLight = reflectedLight.directDiffuse + reflectedLight.indirectDiffuse +
                      reflectedLight.directSpecular + reflectedLight.indirectSpecular +
                      totalEmissiveRadiance;

  #ifdef TRANSMISSION
    diffuseColor.a *= mix(saturate(1. - totalTransmission +
                        linearToRelativeLuminance2(reflectedLight.directSpecular +
                                                  reflectedLight.indirectSpecular)),
                1.0, metalness);
  #endif

  #include <tonemapping_fragment>
  #include <encodings_fragment>
  #include <fog_fragment>
  #include <premultiplied_alpha_fragment>
  #include <dithering_fragment>

  gl_FragColor = vec4(outgoingLight, diffuseColor.a);
}
`,yr=`// Cosmic WaterPlane Vertex Shader - Aurora Wave Effect
// #pragma glslify: cnoise3 = require(glsl-noise/classic/3d) 

// noise source from https://github.com/hughsk/glsl-noise/blob/master/periodic/3d.glsl

vec3 mod289(vec3 x)
{
  return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec4 mod289(vec4 x)
{
  return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec4 permute(vec4 x)
{
  return mod289(((x*34.0)+1.0)*x);
}

vec4 taylorInvSqrt(vec4 r)
{
  return 1.79284291400159 - 0.85373472095314 * r;
}

vec3 fade(vec3 t) {
  return t*t*t*(t*(t*6.0-15.0)+10.0);
}

float cnoise(vec3 P)
{
  vec3 Pi0 = floor(P); // Integer part for indexing
  vec3 Pi1 = Pi0 + vec3(1.0); // Integer part + 1
  Pi0 = mod289(Pi0);
  Pi1 = mod289(Pi1);
  vec3 Pf0 = fract(P); // Fractional part for interpolation
  vec3 Pf1 = Pf0 - vec3(1.0); // Fractional part - 1.0
  vec4 ix = vec4(Pi0.x, Pi1.x, Pi0.x, Pi1.x);
  vec4 iy = vec4(Pi0.yy, Pi1.yy);
  vec4 iz0 = Pi0.zzzz;
  vec4 iz1 = Pi1.zzzz;

  vec4 ixy = permute(permute(ix) + iy);
  vec4 ixy0 = permute(ixy + iz0);
  vec4 ixy1 = permute(ixy + iz1);

  vec4 gx0 = ixy0 * (1.0 / 7.0);
  vec4 gy0 = fract(floor(gx0) * (1.0 / 7.0)) - 0.5;
  gx0 = fract(gx0);
  vec4 gz0 = vec4(0.5) - abs(gx0) - abs(gy0);
  vec4 sz0 = step(gz0, vec4(0.0));
  gx0 -= sz0 * (step(0.0, gx0) - 0.5);
  gy0 -= sz0 * (step(0.0, gy0) - 0.5);

  vec4 gx1 = ixy1 * (1.0 / 7.0);
  vec4 gy1 = fract(floor(gx1) * (1.0 / 7.0)) - 0.5;
  gx1 = fract(gx1);
  vec4 gz1 = vec4(0.5) - abs(gx1) - abs(gy1);
  vec4 sz1 = step(gz1, vec4(0.0));
  gx1 -= sz1 * (step(0.0, gx1) - 0.5);
  gy1 -= sz1 * (step(0.0, gy1) - 0.5);

  vec3 g000 = vec3(gx0.x,gy0.x,gz0.x);
  vec3 g100 = vec3(gx0.y,gy0.y,gz0.y);
  vec3 g010 = vec3(gx0.z,gy0.z,gz0.z);
  vec3 g110 = vec3(gx0.w,gy0.w,gz0.w);
  vec3 g001 = vec3(gx1.x,gy1.x,gz1.x);
  vec3 g101 = vec3(gx1.y,gy1.y,gz1.y);
  vec3 g011 = vec3(gx1.z,gy1.z,gz1.z);
  vec3 g111 = vec3(gx1.w,gy1.w,gz1.w);

  vec4 norm0 = taylorInvSqrt(vec4(dot(g000, g000), dot(g010, g010), dot(g100, g100), dot(g110, g110)));
  g000 *= norm0.x;
  g010 *= norm0.y;
  g100 *= norm0.z;
  g110 *= norm0.w;
  vec4 norm1 = taylorInvSqrt(vec4(dot(g001, g001), dot(g011, g011), dot(g101, g101), dot(g111, g111)));
  g001 *= norm1.x;
  g011 *= norm1.y;
  g101 *= norm1.z;
  g111 *= norm1.w;

  float n000 = dot(g000, Pf0);
  float n100 = dot(g100, vec3(Pf1.x, Pf0.yz));
  float n010 = dot(g010, vec3(Pf0.x, Pf1.y, Pf0.z));
  float n110 = dot(g110, vec3(Pf1.xy, Pf0.z));
  float n001 = dot(g001, vec3(Pf0.xy, Pf1.z));
  float n101 = dot(g101, vec3(Pf1.x, Pf0.y, Pf1.z));
  float n011 = dot(g011, vec3(Pf0.x, Pf1.yz));
  float n111 = dot(g111, Pf1);

  vec3 fade_xyz = fade(Pf0);
  vec4 n_z = mix(vec4(n000, n100, n010, n110), vec4(n001, n101, n011, n111), fade_xyz.z);
  vec2 n_yz = mix(n_z.xy, n_z.zw, fade_xyz.y);
  float n_xyz = mix(n_yz.x, n_yz.y, fade_xyz.x); 
  return 2.2 * n_xyz;
}

//-------- Aurora Wave Effect Functions ------------

mat3 rotation3dY(float angle) {
  float s = sin(angle);
  float c = cos(angle);
  return mat3(c, 0.0, -s, 0.0, 1.0, 0.0, s, 0.0, c);
}

vec3 rotateY(vec3 v, float angle) { return rotation3dY(angle) * v; }

varying vec3 vNormal;
varying float displacement;
varying vec3 vPos;
varying float vDistort;
varying vec2 vUv;
varying float vAuroraIntensity;
varying float vWaveHeight;
varying vec3 vFlowDirection;

uniform float uTime;
uniform float uSpeed;
uniform float uLoadingTime;
uniform float uNoiseDensity;
uniform float uNoiseStrength;

#define STANDARD
varying vec3 vViewPosition;
#ifndef FLAT_SHADED
#ifdef USE_TANGENT
varying vec3 vTangent;
varying vec3 vBitangent;
#endif
#endif
#include <clipping_planes_pars_vertex>
#include <color_pars_vertex>
#include <common>
#include <displacementmap_pars_vertex>
#include <fog_pars_vertex>
#include <logdepthbuf_pars_vertex>
#include <morphtarget_pars_vertex>
#include <shadowmap_pars_vertex>
#include <skinning_pars_vertex>
#include <uv2_pars_vertex>
#include <uv_pars_vertex>

void main() {

  #include <beginnormal_vertex>
  #include <color_vertex>
  #include <defaultnormal_vertex>
  #include <morphnormal_vertex>
  #include <skinbase_vertex>
  #include <skinnormal_vertex>
  #include <uv2_vertex>
  #include <uv_vertex>
  #ifndef FLAT_SHADED
    vNormal = normalize(transformedNormal);
  #ifdef USE_TANGENT
    vTangent = normalize(transformedTangent);
    vBitangent = normalize(cross(vNormal, vTangent) * tangent.w);
  #endif
  #endif
  #include <begin_vertex>

  #include <clipping_planes_vertex>
  #include <displacementmap_vertex>
  #include <logdepthbuf_vertex>
  #include <morphtarget_vertex>
  #include <project_vertex>
  #include <skinning_vertex>
    vViewPosition = -mvPosition.xyz;
  #include <fog_vertex>
  #include <shadowmap_vertex>
  #include <worldpos_vertex>

  //-------- Cosmic Aurora Wave Effect ------------
  vUv = uv;
  
  float t = uTime * uSpeed;
  
  // Create flowing aurora patterns
  float auroraFlow1 = sin(position.x * 5.0 + t * 1.5) * cos(position.y * 3.0 + t * 1.0);
  float auroraFlow2 = sin(position.x * 8.0 + t * 2.0) * sin(position.y * 6.0 + t * 1.8);
  float auroraFlow3 = cos(position.x * 12.0 + t * 2.5) * cos(position.y * 9.0 + t * 2.2);
  
  // Combine aurora flows
  float auroraPattern = (auroraFlow1 + auroraFlow2 * 0.7 + auroraFlow3 * 0.4) / 2.1;
  vAuroraIntensity = abs(auroraPattern);
  
  // Create multi-layered waves
  float wave1 = cnoise(vec3(position.xy * uNoiseDensity * 0.5, t * 0.3));
  float wave2 = cnoise(vec3(position.xy * uNoiseDensity * 1.2, t * 0.5)) * 0.6;
  float wave3 = cnoise(vec3(position.xy * uNoiseDensity * 2.5, t * 0.8)) * 0.3;
  float wave4 = cnoise(vec3(position.xy * uNoiseDensity * 5.0, t * 1.2)) * 0.15;
  
  // Combine waves for complex water surface
  float combinedWaves = wave1 + wave2 + wave3 + wave4;
  vWaveHeight = combinedWaves;
  
  // Create flowing current patterns
  vec2 flowDirection = vec2(
    sin(position.x * 2.0 + t * 0.8) + cos(position.y * 1.5 + t * 0.6),
    cos(position.x * 1.8 + t * 0.7) + sin(position.y * 2.2 + t * 0.9)
  );
  vFlowDirection = vec3(normalize(flowDirection), 0.0);
  
  // Aurora-influenced wave distortion
  float auroraWave = sin(position.x * 15.0 + t * 3.0 + auroraPattern * 5.0) * 
                     cos(position.y * 12.0 + t * 2.5 + auroraPattern * 4.0) * 0.2;
  
  // Create cosmic energy ripples
  float distanceFromCenter = length(position.xy);
  float cosmicRipple = sin(distanceFromCenter * 8.0 - t * 4.0) * 
                       exp(-distanceFromCenter * 0.1) * 0.3;
  
  // Pulsing effect for cosmic energy
  float cosmicPulse = sin(t * 1.5 + distanceFromCenter * 2.0) * 0.1 + 1.0;
  
  // Apply complex displacement
  float totalDisplacement = (combinedWaves + auroraWave + cosmicRipple) * 
                           uNoiseStrength * uLoadingTime * cosmicPulse;
  
  vec3 pos = position + normal * totalDisplacement;
  vPos = pos;
  
  // Add subtle rotation for cosmic flow
  pos = rotateY(pos, sin(t * 0.05 + distanceFromCenter * 0.1) * 0.02);

  gl_Position = projectionMatrix * modelViewMatrix * vec4(pos, 1.0);
}
`,ti={};U(ti,{plane:()=>Qt,sphere:()=>Jt,waterPlane:()=>ei});var ii={};U(ii,{cosmic:()=>ti,defaults:()=>Vt,glass:()=>Wt,positionMix:()=>Kt});var J={performance:!1,render:!0},ri={enable:r=>{J[r]=!0},disable:r=>{J[r]=!1},enableAll:()=>{Object.keys(J).forEach(r=>{J[r]=!0})},disableAll:()=>{Object.keys(J).forEach(r=>{J[r]=!1})},performance:(...r)=>{J.performance&&console.log("[Performance]",...r)},render:(...r)=>{J.render&&console.log("[Render]",...r)}};typeof window<"u"&&(window.debug=ri);function Ze(r){return r/180*Math.PI}function Cr(r){return r.map(e=>Ze(e))}function br(r){return r.replace("http://localhost:3001/customize","").replace("https://shadergradient.co/customize","").replace("https://www.shadergradient.co/customize","")}function Er({animate:r,range:e,rangeStart:t,rangeEnd:n,loop:s,loopDuration:o,positionX:f,positionY:_,positionZ:c,rotationX:v,rotationY:g,rotationZ:b,type:w,color1:C,color2:P,color3:E,reflection:x,uTime:d,uSpeed:i,uDensity:a,uStrength:m,uFrequency:u,uAmplitude:l,shader:y}){let{vertex:T,fragment:S}=ii[y][w],V={colors:[C,P,E],uTime:d,uSpeed:i,uLoadingTime:1,uNoiseDensity:a,uNoiseStrength:m,uFrequency:u,uAmplitude:l,uIntensity:.5,uLoop:s==="on"?1:0,uLoopDuration:o||5},G=y==="glass"?{uColor1:de(C),uColor2:de(P),uColor3:de(E),uTransparency:.1,uRefraction:1.5,uChromaticAberration:.1,uFresnelPower:2,uReflectivity:.9,uWaveAmplitude:.02,uWaveFrequency:5,uDistortion:.1,uFlowSpeed:.1,uFlowDirection:{x:1,y:.5},uLiquidEffect:.5,uFoamIntensity:.3,envMapIntensity:1}:{},N=L(L({},V),G);return p.jsxs("mesh",{name:"shadergradient-mesh",position:[f,_,c],rotation:Cr([v,g,b]),children:[p.jsx(Mi,{type:w}),p.jsx(Xi,{animate:r,range:e,rangeStart:t,rangeEnd:n,loop:s,loopDuration:o,reflection:x,shader:y,uniforms:N,vertexShader:T,fragmentShader:S,onInit:te=>{ri.performance("material (onInit)",te)}})]})}var Le=class{constructor(){this.enabled=!0,this.needsSwap=!0,this.clear=!1,this.renderToScreen=!1}setSize(){}render(){console.error("THREE.Pass: .render() must be implemented in derived pass.")}},Tr=new Je(-1,1,1,-1,0,1),tt=new Qe;tt.setAttribute("position",new ve([-1,3,0,-1,-1,0,3,-1,0],3));tt.setAttribute("uv",new ve([0,2,0,0,2,0],2));var wr=class{constructor(e){this._mesh=new Rt(tt,e)}dispose(){this._mesh.geometry.dispose()}render(e){e.render(this._mesh,Tr)}get material(){return this._mesh.material}set material(e){this._mesh.material=e}},Pr=class extends Le{constructor(e,t,n,s,o){super(),this.scene=e,this.camera=t,this.overrideMaterial=n,this.clearColor=s,this.clearAlpha=o!==void 0?o:0,this.clear=!0,this.clearDepth=!1,this.needsSwap=!1,this._oldClearColor=new hi}render(e,t,n){let s=e.autoClear;e.autoClear=!1;let o,f;this.overrideMaterial!==void 0&&(f=this.scene.overrideMaterial,this.scene.overrideMaterial=this.overrideMaterial),this.clearColor&&(e.getClearColor(this._oldClearColor),o=e.getClearAlpha(),e.setClearColor(this.clearColor,this.clearAlpha)),this.clearDepth&&e.clearDepth(),e.setRenderTarget(this.renderToScreen?null:n),this.clear&&e.clear(e.autoClearColor,e.autoClearDepth,e.autoClearStencil),e.render(this.scene,this.camera),this.clearColor&&e.setClearColor(this._oldClearColor,o),this.overrideMaterial!==void 0&&(this.scene.overrideMaterial=f),e.autoClear=s}},pt=class extends Le{constructor(e,t){super(),this.textureID=t!==void 0?t:"tDiffuse",e instanceof qe?(this.uniforms=e.uniforms,this.material=e):e&&(this.uniforms=Ke.clone(e.uniforms),this.material=new qe({defines:Object.assign({},e.defines),uniforms:this.uniforms,vertexShader:e.vertexShader,fragmentShader:e.fragmentShader})),this.fsQuad=new wr(this.material)}render(e,t,n){this.uniforms[this.textureID]&&(this.uniforms[this.textureID].value=n.texture),this.fsQuad.material=this.material,this.renderToScreen?(e.setRenderTarget(null),this.fsQuad.render(e)):(e.setRenderTarget(t),this.clear&&e.clear(e.autoClearColor,e.autoClearDepth,e.autoClearStencil),this.fsQuad.render(e))}},ht={uniforms:{tDiffuse:{value:null},opacity:{value:1}},vertexShader:`

		varying vec2 vUv;

		void main() {

			vUv = uv;
			gl_Position = projectionMatrix * modelViewMatrix * vec4( position, 1.0 );

		}`,fragmentShader:`

		uniform float opacity;

		uniform sampler2D tDiffuse;

		varying vec2 vUv;

		void main() {

			vec4 texel = texture2D( tDiffuse, vUv );
			gl_FragColor = opacity * texel;

		}`},_t=class extends Le{constructor(e,t){super(),this.scene=e,this.camera=t,this.clear=!0,this.needsSwap=!1,this.inverse=!1}render(e,t,n){let s=e.getContext(),o=e.state;o.buffers.color.setMask(!1),o.buffers.depth.setMask(!1),o.buffers.color.setLocked(!0),o.buffers.depth.setLocked(!0);let f,_;this.inverse?(f=0,_=1):(f=1,_=0),o.buffers.stencil.setTest(!0),o.buffers.stencil.setOp(s.REPLACE,s.REPLACE,s.REPLACE),o.buffers.stencil.setFunc(s.ALWAYS,f,4294967295),o.buffers.stencil.setClear(_),o.buffers.stencil.setLocked(!0),e.setRenderTarget(n),this.clear&&e.clear(),e.render(this.scene,this.camera),e.setRenderTarget(t),this.clear&&e.clear(),e.render(this.scene,this.camera),o.buffers.color.setLocked(!1),o.buffers.depth.setLocked(!1),o.buffers.stencil.setLocked(!1),o.buffers.stencil.setFunc(s.EQUAL,1,4294967295),o.buffers.stencil.setOp(s.KEEP,s.KEEP,s.KEEP),o.buffers.stencil.setLocked(!0)}},zr=class extends Le{constructor(){super(),this.needsSwap=!1}render(e){e.state.buffers.stencil.setLocked(!1),e.state.buffers.stencil.setTest(!1)}},Ar=class{constructor(e,t){if(this.renderer=e,t===void 0){let n={minFilter:me,magFilter:me,format:_i},s=e.getSize(new nt);this._pixelRatio=e.getPixelRatio(),this._width=s.width,this._height=s.height,t=new Ft(this._width*this._pixelRatio,this._height*this._pixelRatio,n),t.texture.name="EffectComposer.rt1"}else this._pixelRatio=1,this._width=t.width,this._height=t.height;this.renderTarget1=t,this.renderTarget2=t.clone(),this.renderTarget2.texture.name="EffectComposer.rt2",this.writeBuffer=this.renderTarget1,this.readBuffer=this.renderTarget2,this.renderToScreen=!0,this.passes=[],ht===void 0&&console.error("THREE.EffectComposer relies on CopyShader"),pt===void 0&&console.error("THREE.EffectComposer relies on ShaderPass"),this.copyPass=new pt(ht),this.clock=new Lt}swapBuffers(){let e=this.readBuffer;this.readBuffer=this.writeBuffer,this.writeBuffer=e}addPass(e){this.passes.push(e),e.setSize(this._width*this._pixelRatio,this._height*this._pixelRatio)}insertPass(e,t){this.passes.splice(t,0,e),e.setSize(this._width*this._pixelRatio,this._height*this._pixelRatio)}removePass(e){let t=this.passes.indexOf(e);t!==-1&&this.passes.splice(t,1)}isLastEnabledPass(e){for(let t=e+1;t<this.passes.length;t++)if(this.passes[t].enabled)return!1;return!0}render(e){e===void 0&&(e=this.clock.getDelta());let t=this.renderer.getRenderTarget(),n=!1;for(let s=0,o=this.passes.length;s<o;s++){let f=this.passes[s];if(f.enabled!==!1){if(f.renderToScreen=this.renderToScreen&&this.isLastEnabledPass(s),f.render(this.renderer,this.writeBuffer,this.readBuffer,e,n),f.needsSwap){if(n){let _=this.renderer.getContext(),c=this.renderer.state.buffers.stencil;c.setFunc(_.NOTEQUAL,1,4294967295),this.copyPass.render(this.renderer,this.writeBuffer,this.readBuffer,e),c.setFunc(_.EQUAL,1,4294967295)}this.swapBuffers()}_t!==void 0&&(f instanceof _t?n=!0:f instanceof zr&&(n=!1))}}this.renderer.setRenderTarget(t)}reset(e){if(e===void 0){let t=this.renderer.getSize(new nt);this._pixelRatio=this.renderer.getPixelRatio(),this._width=t.width,this._height=t.height,e=this.renderTarget1.clone(),e.setSize(this._width*this._pixelRatio,this._height*this._pixelRatio)}this.renderTarget1.dispose(),this.renderTarget2.dispose(),this.renderTarget1=e,this.renderTarget2=e.clone(),this.writeBuffer=this.renderTarget1,this.readBuffer=this.renderTarget2}setSize(e,t){this._width=e,this._height=t;let n=this._width*this._pixelRatio,s=this._height*this._pixelRatio;this.renderTarget1.setSize(n,s),this.renderTarget2.setSize(n,s);for(let o=0;o<this.passes.length;o++)this.passes[o].setSize(n,s)}setPixelRatio(e){this._pixelRatio=e,this.setSize(this._width,this._height)}};new Je(-1,1,1,-1,0,1);var ni=new Qe;ni.setAttribute("position",new ve([-1,3,0,-1,-1,0,3,-1,0],3));ni.setAttribute("uv",new ve([0,2,0,0,2,0],2));var ae={uniforms:{tDiffuse:{value:null},shape:{value:1},radius:{value:2},rotateR:{value:Math.PI/12*1},rotateG:{value:Math.PI/12*2},rotateB:{value:Math.PI/12*3},scatter:{value:1},width:{value:20},height:{value:20},blending:{value:1},blendingMode:{value:1},greyscale:{value:!1},disable:{value:!1}},vertexShader:`

		varying vec2 vUV;
		varying vec3 vPosition;

		void main() {

			vUV = uv;
			vPosition = position;

			gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);

		}`,fragmentShader:`

		#define SQRT2_MINUS_ONE 0.41421356
		#define SQRT2_HALF_MINUS_ONE 0.20710678
		#define PI2 6.28318531
		#define SHAPE_DOT 1
		#define SHAPE_ELLIPSE 2
		#define SHAPE_LINE 3
		#define SHAPE_SQUARE 4
		#define BLENDING_LINEAR 1
		#define BLENDING_MULTIPLY 2
		#define BLENDING_ADD 3
		#define BLENDING_LIGHTER 4
		#define BLENDING_DARKER 5
		uniform sampler2D tDiffuse;
		uniform float radius;
		uniform float rotateR;
		uniform float rotateG;
		uniform float rotateB;
		uniform float scatter;
		uniform float width;
		uniform float height;
		uniform int shape;
		uniform bool disable;
		uniform float blending;
		uniform int blendingMode;
		varying vec2 vUV;
		varying vec3 vPosition;
		uniform bool greyscale;
		const int samples = 8;

		float blend( float a, float b, float t ) {

		// linear blend
			return a * ( 1.0 - t ) + b * t;

		}

		float hypot( float x, float y ) {

		// vector magnitude
			return sqrt( x * x + y * y );

		}

		float rand( vec2 seed ){

		// get pseudo-random number
			return fract( sin( dot( seed.xy, vec2( 12.9898, 78.233 ) ) ) * 43758.5453 );

		}

		float distanceToDotRadius( float channel, vec2 coord, vec2 normal, vec2 p, float angle, float rad_max ) {

		// apply shape-specific transforms
			float dist = hypot( coord.x - p.x, coord.y - p.y );
			float rad = channel;

			if ( shape == SHAPE_DOT ) {

				rad = pow( abs( rad ), 1.125 ) * rad_max;

			} else if ( shape == SHAPE_ELLIPSE ) {

				rad = pow( abs( rad ), 1.125 ) * rad_max;

				if ( dist != 0.0 ) {
					float dot_p = abs( ( p.x - coord.x ) / dist * normal.x + ( p.y - coord.y ) / dist * normal.y );
					dist = ( dist * ( 1.0 - SQRT2_HALF_MINUS_ONE ) ) + dot_p * dist * SQRT2_MINUS_ONE;
				}

			} else if ( shape == SHAPE_LINE ) {

				rad = pow( abs( rad ), 1.5) * rad_max;
				float dot_p = ( p.x - coord.x ) * normal.x + ( p.y - coord.y ) * normal.y;
				dist = hypot( normal.x * dot_p, normal.y * dot_p );

			} else if ( shape == SHAPE_SQUARE ) {

				float theta = atan( p.y - coord.y, p.x - coord.x ) - angle;
				float sin_t = abs( sin( theta ) );
				float cos_t = abs( cos( theta ) );
				rad = pow( abs( rad ), 1.4 );
				rad = rad_max * ( rad + ( ( sin_t > cos_t ) ? rad - sin_t * rad : rad - cos_t * rad ) );

			}

			return rad - dist;

		}

		struct Cell {

		// grid sample positions
			vec2 normal;
			vec2 p1;
			vec2 p2;
			vec2 p3;
			vec2 p4;
			float samp2;
			float samp1;
			float samp3;
			float samp4;

		};

		vec4 getSample( vec2 point ) {

		// multi-sampled point
			vec4 tex = texture2D( tDiffuse, vec2( point.x / width, point.y / height ) );
			float base = rand( vec2( floor( point.x ), floor( point.y ) ) ) * PI2;
			float step = PI2 / float( samples );
			// float dist = radius * 0.66;
			float dist = radius * 0.0;

			for ( int i = 0; i < samples; ++i ) {

				float r = base + step * float( i );
				vec2 coord = point + vec2( cos( r ) * dist, sin( r ) * dist );
				tex += texture2D( tDiffuse, vec2( coord.x / width, coord.y / height ) );

			}

			tex /= float( samples ) + 1.0;
			return tex;

		}

		float getDotColour( Cell c, vec2 p, int channel, float angle, float aa ) {

		// get colour for given point
			float dist_c_1, dist_c_2, dist_c_3, dist_c_4, res;

			if ( channel == 0 ) {

				c.samp1 = getSample( c.p1 ).r;
				c.samp2 = getSample( c.p2 ).r;
				c.samp3 = getSample( c.p3 ).r;
				c.samp4 = getSample( c.p4 ).r;

			} else if (channel == 1) {

				c.samp1 = getSample( c.p1 ).g;
				c.samp2 = getSample( c.p2 ).g;
				c.samp3 = getSample( c.p3 ).g;
				c.samp4 = getSample( c.p4 ).g;

			} else {

				c.samp1 = getSample( c.p1 ).b;
				c.samp3 = getSample( c.p3 ).b;
				c.samp2 = getSample( c.p2 ).b;
				c.samp4 = getSample( c.p4 ).b;

			}

			dist_c_1 = distanceToDotRadius( c.samp1, c.p1, c.normal, p, angle, radius );
			dist_c_2 = distanceToDotRadius( c.samp2, c.p2, c.normal, p, angle, radius );
			dist_c_3 = distanceToDotRadius( c.samp3, c.p3, c.normal, p, angle, radius );
			dist_c_4 = distanceToDotRadius( c.samp4, c.p4, c.normal, p, angle, radius );
			res = ( dist_c_1 > 0.0 ) ? clamp( dist_c_1 / aa, 0.0, 1.0 ) : 0.0;
			// res = 0.0;
			res += ( dist_c_2 > 0.0 ) ? clamp( dist_c_2 / aa, 0.0, 1.0 ) : 0.0;
			res += ( dist_c_3 > 0.0 ) ? clamp( dist_c_3 / aa, 0.0, 1.0 ) : 0.0;
			res += ( dist_c_4 > 0.0 ) ? clamp( dist_c_4 / aa, 0.0, 1.0 ) : 0.0;
			res = clamp( res, 0.0, 1.0 );

			return res;
			// return 2

		}

		Cell getReferenceCell( vec2 p, vec2 origin, float grid_angle, float step ) {

		// get containing cell
			Cell c;

		// calc grid
			vec2 n = vec2( cos( grid_angle ), sin( grid_angle ) );
			float threshold = step * 0.5;
			float dot_normal = n.x * ( p.x - origin.x ) + n.y * ( p.y - origin.y );
			float dot_line = -n.y * ( p.x - origin.x ) + n.x * ( p.y - origin.y );
			vec2 offset = vec2( n.x * dot_normal, n.y * dot_normal );
			float offset_normal = mod( hypot( offset.x, offset.y ), step );
			float normal_dir = ( dot_normal < 0.0 ) ? 1.0 : -1.0;
			float normal_scale = ( ( offset_normal < threshold ) ? -offset_normal : step - offset_normal ) * normal_dir;
			float offset_line = mod( hypot( ( p.x - offset.x ) - origin.x, ( p.y - offset.y ) - origin.y ), step );
			float line_dir = ( dot_line < 0.0 ) ? 1.0 : -1.0;
			float line_scale = ( ( offset_line < threshold ) ? -offset_line : step - offset_line ) * line_dir;

		// get closest corner
			c.normal = n;
			c.p1.x = p.x - n.x * normal_scale + n.y * line_scale;
			c.p1.y = p.y - n.y * normal_scale - n.x * line_scale;

		// scatter
			if ( scatter != 0.0 ) {

				float off_mag = scatter * threshold * 0.5;
				float off_angle = rand( vec2( floor( c.p1.x ), floor( c.p1.y ) ) ) * PI2;
				c.p1.x += cos( off_angle ) * off_mag;
				c.p1.y += sin( off_angle ) * off_mag;

			}

		// find corners
			float normal_step = normal_dir * ( ( offset_normal < threshold ) ? step : -step );
			float line_step = line_dir * ( ( offset_line < threshold ) ? step : -step );
			c.p2.x = c.p1.x - n.x * normal_step;
			c.p2.y = c.p1.y - n.y * normal_step;
			c.p3.x = c.p1.x + n.y * line_step;
			c.p3.y = c.p1.y - n.x * line_step;
			c.p4.x = c.p1.x - n.x * normal_step + n.y * line_step;
			c.p4.y = c.p1.y - n.y * normal_step - n.x * line_step;

			return c;

		}

		float blendColour( float a, float b, float t ) {

		// blend colours
			if ( blendingMode == BLENDING_LINEAR ) {
				return blend( a, b, 1.0 - t );
			} else if ( blendingMode == BLENDING_ADD ) {
				return blend( a, min( 1.0, a + b ), t );
			} else if ( blendingMode == BLENDING_MULTIPLY ) {
				return blend( a, max( 0.0, a * b ), t );
			} else if ( blendingMode == BLENDING_LIGHTER ) {
				return blend( a, max( a, b ), t );
			} else if ( blendingMode == BLENDING_DARKER ) {
				return blend( a, min( a, b ), t );
			} else {
				return blend( a, b, 1.0 - t );
			}

		}

		void main() {

			if ( ! disable ) {

		// setup
				vec2 p = vec2( vUV.x * width, vUV.y * height ) - vec2(vPosition.x, vPosition.y) * 3.0; // - position values to remove black borders.
				vec2 origin = vec2( 0, 0 );
				float aa = ( radius < 2.5 ) ? radius * 0.5 : 1.25;
				// float aa = 0.0;

		// get channel samples
				Cell cell_r = getReferenceCell( p, origin, rotateR, radius );
				Cell cell_g = getReferenceCell( p, origin, rotateG, radius );
				Cell cell_b = getReferenceCell( p, origin, rotateB, radius );
				float r = getDotColour( cell_r, p, 0, rotateR, aa );
				float g = getDotColour( cell_g, p, 1, rotateG, aa );
				float b = getDotColour( cell_b, p, 2, rotateB, aa );

		// blend with original
				vec4 colour = texture2D( tDiffuse, vUV );
				
				// add masking before blendColour
				if (colour.r == 0.0) {
					r = 0.0;
				} else {
					r = blendColour( r, colour.r, blending );
				}

				if (colour.g == 0.0) {
					g = 0.0;
				} else {
					g = blendColour( g, colour.g, blending );
				}

				if (colour.b == 0.0) {
					b = 0.0;
				} else {
					b = blendColour( b, colour.b, blending );
				}
				
				
				

				if ( greyscale ) {
					r = g = b = (r + b + g) / 3.0;
				}

				// add alpha channel to each r, g, b colors
				vec4 vR;
				vec4 vG;
				vec4 vB;
	
				// apply transparent to outside of mesh
				if (r == 0.0 && colour.r == 0.0) {
					vR = vec4( 0, 0, 0, 0 );
				} else {
					vR = vec4( r, 0, 0, 1 );
				}
	
				if (g == 0.0 && colour.g == 0.0) {
					vG = vec4( 0, 0, 0, 0 );
				} else {
					vG = vec4( 0, g, 0, 1 );
				}
	
				if (b == 0.0 && colour.b == 0.0) {
					vB = vec4( 0, 0, 0, 0 );
				} else {
					vB = vec4( 0, 0, b, 1 );
				}

				// gl_FragColor = vec4( r, g, b, 1.0 );
				gl_FragColor = vR + vG + vB;

			} else {

				gl_FragColor = texture2D( tDiffuse, vUV );

			}

		}`},Dr=class{constructor(){this.enabled=!0,this.needsSwap=!0,this.clear=!1,this.renderToScreen=!1}setSize(){}render(){console.error("THREE.Pass: .render() must be implemented in derived pass.")}},Sr=new Je(-1,1,1,-1,0,1),it=new Qe;it.setAttribute("position",new ve([-1,3,0,-1,-1,0,3,-1,0],3));it.setAttribute("uv",new ve([0,2,0,0,2,0],2));var Br=class{constructor(e){this._mesh=new Rt(it,e)}dispose(){this._mesh.geometry.dispose()}render(e){e.render(this._mesh,Sr)}get material(){return this._mesh.material}set material(e){this._mesh.material=e}},I={SKIP:0,ADD:1,ALPHA:2,AVERAGE:3,COLOR_BURN:4,COLOR_DODGE:5,DARKEN:6,DIFFERENCE:7,EXCLUSION:8,LIGHTEN:9,MULTIPLY:10,DIVIDE:11,NEGATION:12,NORMAL:13,OVERLAY:14,REFLECT:15,SCREEN:16,SOFT_LIGHT:17,SUBTRACT:18},Or=`vec4 blend(const in vec4 x, const in vec4 y, const in float opacity) {

	return min(x + y, 1.0) * opacity + x * (1.0 - opacity);

}
`,Nr=`vec3 blend(const in vec3 x, const in vec3 y, const in float opacity) {

	return y * opacity + x * (1.0 - opacity);

}

vec4 blend(const in vec4 x, const in vec4 y, const in float opacity) {

	float a = min(y.a, opacity);

	return vec4(blend(x.rgb, y.rgb, a), max(x.a, a));

}
`,Lr=`vec4 blend(const in vec4 x, const in vec4 y, const in float opacity) {

	return (x + y) * 0.5 * opacity + x * (1.0 - opacity);

}
`,Rr=`float blend(const in float x, const in float y) {

	return (y == 0.0) ? y : max(1.0 - (1.0 - x) / y, 0.0);

}

vec4 blend(const in vec4 x, const in vec4 y, const in float opacity) {

	vec4 z = vec4(
		blend(x.r, y.r),
		blend(x.g, y.g),
		blend(x.b, y.b),
		blend(x.a, y.a)
	);

	return z * opacity + x * (1.0 - opacity);

}
`,Fr=`float blend(const in float x, const in float y) {

	return (y == 1.0) ? y : min(x / (1.0 - y), 1.0);

}

vec4 blend(const in vec4 x, const in vec4 y, const in float opacity) {

	vec4 z = vec4(
		blend(x.r, y.r),
		blend(x.g, y.g),
		blend(x.b, y.b),
		blend(x.a, y.a)
	);

	return z * opacity + x * (1.0 - opacity);

}
`,Ir=`vec4 blend(const in vec4 x, const in vec4 y, const in float opacity) {

	return min(x, y) * opacity + x * (1.0 - opacity);

}
`,Ur=`vec4 blend(const in vec4 x, const in vec4 y, const in float opacity) {

	return abs(x - y) * opacity + x * (1.0 - opacity);

}
`,Mr=`float blend(const in float x, const in float y) {

	return (y > 0.0) ? min(x / y, 1.0) : 1.0;

}

vec4 blend(const in vec4 x, const in vec4 y, const in float opacity) {

	vec4 z = vec4(
		blend(x.r, y.r),
		blend(x.g, y.g),
		blend(x.b, y.b),
		blend(x.a, y.a)
	);

	return z * opacity + x * (1.0 - opacity);

}
`,kr=`vec4 blend(const in vec4 x, const in vec4 y, const in float opacity) {

	return (x + y - 2.0 * x * y) * opacity + x * (1.0 - opacity);

}
`,Hr=`vec4 blend(const in vec4 x, const in vec4 y, const in float opacity) {

	return max(x, y) * opacity + x * (1.0 - opacity);

}
`,jr=`vec4 blend(const in vec4 x, const in vec4 y, const in float opacity) {

	return x * y * opacity + x * (1.0 - opacity);

}
`,Vr=`vec4 blend(const in vec4 x, const in vec4 y, const in float opacity) {

	return (1.0 - abs(1.0 - x - y)) * opacity + x * (1.0 - opacity);

}
`,Yr=`vec4 blend(const in vec4 x, const in vec4 y, const in float opacity) {

	return y * opacity + x * (1.0 - opacity);

}
`,$r=`float blend(const in float x, const in float y) {

	return (x < 0.5) ? (2.0 * x * y) : (1.0 - 2.0 * (1.0 - x) * (1.0 - y));

}

vec4 blend(const in vec4 x, const in vec4 y, const in float opacity) {

	vec4 z = vec4(
		blend(x.r, y.r),
		blend(x.g, y.g),
		blend(x.b, y.b),
		blend(x.a, y.a)
	);

	return z * opacity + x * (1.0 - opacity);

}
`,Gr=`float blend(const in float x, const in float y) {

	return (y == 1.0) ? y : min(x * x / (1.0 - y), 1.0);

}

vec4 blend(const in vec4 x, const in vec4 y, const in float opacity) {

	vec4 z = vec4(
		blend(x.r, y.r),
		blend(x.g, y.g),
		blend(x.b, y.b),
		blend(x.a, y.a)
	);

	return z * opacity + x * (1.0 - opacity);

}
`,Wr=`vec4 blend(const in vec4 x, const in vec4 y, const in float opacity) {

	return (1.0 - (1.0 - x) * (1.0 - y)) * opacity + x * (1.0 - opacity);

}
`,qr=`float blend(const in float x, const in float y) {

	return (y < 0.5) ?
		(2.0 * x * y + x * x * (1.0 - 2.0 * y)) :
		(sqrt(x) * (2.0 * y - 1.0) + 2.0 * x * (1.0 - y));

}

vec4 blend(const in vec4 x, const in vec4 y, const in float opacity) {

	vec4 z = vec4(
		blend(x.r, y.r),
		blend(x.g, y.g),
		blend(x.b, y.b),
		blend(x.a, y.a)
	);

	return z * opacity + x * (1.0 - opacity);

}
`,Zr=`vec4 blend(const in vec4 x, const in vec4 y, const in float opacity) {

	return max(x + y - 1.0, 0.0) * opacity + x * (1.0 - opacity);

}
`,Xr=new Map([[I.SKIP,null],[I.ADD,Or],[I.ALPHA,Nr],[I.AVERAGE,Lr],[I.COLOR_BURN,Rr],[I.COLOR_DODGE,Fr],[I.DARKEN,Ir],[I.DIFFERENCE,Ur],[I.EXCLUSION,kr],[I.LIGHTEN,Hr],[I.MULTIPLY,jr],[I.DIVIDE,Mr],[I.NEGATION,Vr],[I.NORMAL,Yr],[I.OVERLAY,$r],[I.REFLECT,Gr],[I.SCREEN,Wr],[I.SOFT_LIGHT,qr],[I.SUBTRACT,Zr]]),Kr=class extends xi{constructor(r,e=1){super(),this.blendFunction=r,this.opacity=new yi(e)}getBlendFunction(){return this.blendFunction}setBlendFunction(r){this.blendFunction=r,this.dispatchEvent({type:"change"})}getShaderCode(){return Xr.get(this.blendFunction)}},Qr=class extends Dr{constructor(e,t,n){super(),ae===void 0&&console.error("THREE.HalftonePass requires HalftoneShader"),this.uniforms=Ke.clone(ae.uniforms),this.material=new qe({uniforms:this.uniforms,fragmentShader:ae.fragmentShader,vertexShader:ae.vertexShader}),this.uniforms.width.value=e,this.uniforms.height.value=t,this.uniforms.disable.value=n.disable,this.fsQuad=new Br(this.material),this.blendMode=new Kr(I.SCREEN),this.extensions=null}render(e,t,n){this.material.uniforms.tDiffuse.value=n.texture,this.renderToScreen?(e.setRenderTarget(null),this.fsQuad.render(e)):(e.setRenderTarget(t),this.clear&&e.clear(),this.fsQuad.render(e))}setSize(e,t){this.uniforms.width.value=e,this.uniforms.height.value=t}initialize(e,t,n){}addEventListener(){}getAttributes(){return this.attributes}getFragmentShader(){return ae.fragmentShader}getVertexShader(){return ae.vertexShader}update(e,t,n){}};function Jr({disable:r=!1}){let{gl:e,scene:t,camera:n,size:s}=M(),o=z.useRef(null),f=z.useRef(null),_=z.useMemo(()=>({shape:1,radius:2,rotateR:Math.PI/12,rotateB:Math.PI/12*2,rotateG:Math.PI/12*3,scatter:1,blending:1,blendingMode:1,greyscale:!1}),[]);return z.useEffect(()=>{let c=new Ar(e),v=new Pr(t,n),g=new Qr(s.width,s.height,ge(L({},_),{disable:r}));return c.addPass(v),c.addPass(g),o.current=c,f.current=g,()=>{var b,w,C,P,E,x,d,i;(b=v.dispose)==null||b.call(v),(w=g.fsQuad)!=null&&w.dispose&&g.fsQuad.dispose(),(P=(C=g.material)==null?void 0:C.dispose)==null||P.call(C),(x=(E=c.renderTarget1)==null?void 0:E.dispose)==null||x.call(E),(i=(d=c.renderTarget2)==null?void 0:d.dispose)==null||i.call(d),o.current=null,f.current=null}},[n,e,_,t]),z.useEffect(()=>{var c;let v=o.current;v&&(v.setSize(s.width,s.height),(c=f.current)==null||c.setSize(s.width,s.height))},[s.height,s.width]),z.useEffect(()=>{var c,v;(v=(c=f.current)==null?void 0:c.uniforms)!=null&&v.disable&&(f.current.uniforms.disable.value=r)},[r]),be((c,v)=>{let g=o.current;g&&(e.autoClear=!0,g.render(v))},1),p.jsx(p.Fragment,{})}var en=(r,e,t)=>({dpr:r,camera:{fov:e},linear:!0,flat:!0,gl:{preserveDrawingBuffer:t?.preserveDrawingBuffer,powerPreference:t?.powerPreference}}),tn=1,rn=14,xt={zoom:1,distance:14},yt={zoom:5,distance:14},nn="https://ruucm.github.io/shadergradient/ui@0.0.0/assets/hdr/";function an({type:r,cAzimuthAngle:e,cPolarAngle:t,cDistance:n,cameraZoom:s,zoomOut:o,enableTransition:f=!0}){let _=z.useRef();return be((c,v)=>_.current.update(v)),z.useEffect(()=>{let c=_.current;c?.rotateTo(Ze(e),Ze(t),f)},[_,e,t,f]),z.useEffect(()=>{let c=_.current;o?r==="sphere"?(c?.dollyTo(yt.distance,f),c?.zoomTo(yt.zoom,f)):(c?.dollyTo(xt.distance,f),c?.zoomTo(xt.zoom,f)):r==="sphere"?(c?.zoomTo(s,f),c?.dollyTo(rn,f)):(c?.dollyTo(n,f),c?.zoomTo(tn,f))},[_,o,r,s,n,f]),_}var F={LEFT:1,RIGHT:2,MIDDLE:4},h=Object.freeze({NONE:0,ROTATE:1,TRUCK:2,OFFSET:4,DOLLY:8,ZOOM:16,TOUCH_ROTATE:32,TOUCH_TRUCK:64,TOUCH_OFFSET:128,TOUCH_DOLLY:256,TOUCH_ZOOM:512,TOUCH_DOLLY_TRUCK:1024,TOUCH_DOLLY_OFFSET:2048,TOUCH_DOLLY_ROTATE:4096,TOUCH_ZOOM_TRUCK:8192,TOUCH_ZOOM_OFFSET:16384,TOUCH_ZOOM_ROTATE:32768}),oe={NONE:0,IN:1,OUT:-1};function ie(r){return r.isPerspectiveCamera}function ee(r){return r.isOrthographicCamera}var se=Math.PI*2,Ct=Math.PI/2,ai=1e-5,pe=Math.PI/180;function q(r,e,t){return Math.max(e,Math.min(t,r))}function R(r,e=ai){return Math.abs(r)<e}function O(r,e,t=ai){return R(r-e,t)}function bt(r,e){return Math.round(r/e)*e}function he(r){return isFinite(r)?r:r<0?-Number.MAX_VALUE:Number.MAX_VALUE}function _e(r){return Math.abs(r)<Number.MAX_VALUE?r:r*(1/0)}function Ae(r,e,t,n,s=1/0,o){n=Math.max(1e-4,n);let f=2/n,_=f*o,c=1/(1+_+.48*_*_+.235*_*_*_),v=r-e,g=e,b=s*n;v=q(v,-b,b),e=r-v;let w=(t.value+f*v)*o;t.value=(t.value-f*w)*c;let C=e+(v+w)*c;return g-r>0==C>g&&(C=g,t.value=(C-g)/o),C}function Et(r,e,t,n,s=1/0,o,f){n=Math.max(1e-4,n);let _=2/n,c=_*o,v=1/(1+c+.48*c*c+.235*c*c*c),g=e.x,b=e.y,w=e.z,C=r.x-g,P=r.y-b,E=r.z-w,x=g,d=b,i=w,a=s*n,m=a*a,u=C*C+P*P+E*E;if(u>m){let k=Math.sqrt(u);C=C/k*a,P=P/k*a,E=E/k*a}g=r.x-C,b=r.y-P,w=r.z-E;let l=(t.x+_*C)*o,y=(t.y+_*P)*o,T=(t.z+_*E)*o;t.x=(t.x-_*l)*v,t.y=(t.y-_*y)*v,t.z=(t.z-_*T)*v,f.x=g+(C+l)*v,f.y=b+(P+y)*v,f.z=w+(E+T)*v;let S=x-r.x,V=d-r.y,G=i-r.z,N=f.x-x,te=f.y-d,H=f.z-i;return S*N+V*te+G*H>0&&(f.x=x,f.y=d,f.z=i,t.x=(f.x-x)/o,t.y=(f.y-d)/o,t.z=(f.z-i)/o),f}function Ue(r,e){e.set(0,0),r.forEach(t=>{e.x+=t.clientX,e.y+=t.clientY}),e.x/=r.length,e.y/=r.length}function Me(r,e){return ee(r)?(console.warn(`${e} is not supported in OrthographicCamera`),!0):!1}var on=class{constructor(){this._listeners={}}addEventListener(r,e){let t=this._listeners;t[r]===void 0&&(t[r]=[]),t[r].indexOf(e)===-1&&t[r].push(e)}hasEventListener(r,e){let t=this._listeners;return t[r]!==void 0&&t[r].indexOf(e)!==-1}removeEventListener(r,e){let t=this._listeners[r];if(t!==void 0){let n=t.indexOf(e);n!==-1&&t.splice(n,1)}}removeAllEventListeners(r){if(!r){this._listeners={};return}Array.isArray(this._listeners[r])&&(this._listeners[r].length=0)}dispatchEvent(r){let e=this._listeners[r.type];if(e!==void 0){r.target=this;let t=e.slice(0);for(let n=0,s=t.length;n<s;n++)t[n].call(this,r)}}},ke,sn="2.9.0",De=1/8,ln=/Mac/.test((ke=globalThis?.navigator)===null||ke===void 0?void 0:ke.platform),A,Tt,Se,He,j,D,B,le,xe,Z,X,re,wt,Pt,Y,ye,ce,zt,je,At,Ve,Ye,Be,W=class Xe extends on{static install(e){A=e.THREE,Tt=Object.freeze(new A.Vector3(0,0,0)),Se=Object.freeze(new A.Vector3(0,1,0)),He=Object.freeze(new A.Vector3(0,0,1)),j=new A.Vector2,D=new A.Vector3,B=new A.Vector3,le=new A.Vector3,xe=new A.Vector3,Z=new A.Vector3,X=new A.Vector3,re=new A.Vector3,wt=new A.Vector3,Pt=new A.Vector3,Y=new A.Spherical,ye=new A.Spherical,ce=new A.Box3,zt=new A.Box3,je=new A.Sphere,At=new A.Quaternion,Ve=new A.Quaternion,Ye=new A.Matrix4,Be=new A.Raycaster}static get ACTION(){return h}constructor(e,t){super(),this.minPolarAngle=0,this.maxPolarAngle=Math.PI,this.minAzimuthAngle=-1/0,this.maxAzimuthAngle=1/0,this.minDistance=Number.EPSILON,this.maxDistance=1/0,this.infinityDolly=!1,this.minZoom=.01,this.maxZoom=1/0,this.smoothTime=.25,this.draggingSmoothTime=.125,this.maxSpeed=1/0,this.azimuthRotateSpeed=1,this.polarRotateSpeed=1,this.dollySpeed=1,this.dollyDragInverted=!1,this.truckSpeed=2,this.dollyToCursor=!1,this.dragToOffset=!1,this.verticalDragToForward=!1,this.boundaryFriction=0,this.restThreshold=.01,this.colliderMeshes=[],this.cancel=()=>{},this._enabled=!0,this._state=h.NONE,this._viewport=null,this._changedDolly=0,this._changedZoom=0,this._hasRested=!0,this._boundaryEnclosesCamera=!1,this._needsUpdate=!0,this._updatedLastTime=!1,this._elementRect=new DOMRect,this._isDragging=!1,this._dragNeedsUpdate=!0,this._activePointers=[],this._lockedPointer=null,this._interactiveArea=new DOMRect(0,0,1,1),this._isUserControllingRotate=!1,this._isUserControllingDolly=!1,this._isUserControllingTruck=!1,this._isUserControllingOffset=!1,this._isUserControllingZoom=!1,this._lastDollyDirection=oe.NONE,this._thetaVelocity={value:0},this._phiVelocity={value:0},this._radiusVelocity={value:0},this._targetVelocity=new A.Vector3,this._focalOffsetVelocity=new A.Vector3,this._zoomVelocity={value:0},this._truckInternal=(d,i,a)=>{let m,u;if(ie(this._camera)){let l=D.copy(this._camera.position).sub(this._target),y=this._camera.getEffectiveFOV()*pe,T=l.length()*Math.tan(y*.5);m=this.truckSpeed*d*T/this._elementRect.height,u=this.truckSpeed*i*T/this._elementRect.height}else if(ee(this._camera)){let l=this._camera;m=d*(l.right-l.left)/l.zoom/this._elementRect.width,u=i*(l.top-l.bottom)/l.zoom/this._elementRect.height}else return;this.verticalDragToForward?(a?this.setFocalOffset(this._focalOffsetEnd.x+m,this._focalOffsetEnd.y,this._focalOffsetEnd.z,!0):this.truck(m,0,!0),this.forward(-u,!0)):a?this.setFocalOffset(this._focalOffsetEnd.x+m,this._focalOffsetEnd.y+u,this._focalOffsetEnd.z,!0):this.truck(m,u,!0)},this._rotateInternal=(d,i)=>{let a=se*this.azimuthRotateSpeed*d/this._elementRect.height,m=se*this.polarRotateSpeed*i/this._elementRect.height;this.rotate(a,m,!0)},this._dollyInternal=(d,i,a)=>{let m=Math.pow(.95,-d*this.dollySpeed),u=this._sphericalEnd.radius,l=this._sphericalEnd.radius*m,y=q(l,this.minDistance,this.maxDistance),T=y-l;this.infinityDolly&&this.dollyToCursor?this._dollyToNoClamp(l,!0):this.infinityDolly&&!this.dollyToCursor?(this.dollyInFixed(T,!0),this._dollyToNoClamp(y,!0)):this._dollyToNoClamp(y,!0),this.dollyToCursor&&(this._changedDolly+=(this.infinityDolly?l:y)-u,this._dollyControlCoord.set(i,a)),this._lastDollyDirection=Math.sign(-d)},this._zoomInternal=(d,i,a)=>{let m=Math.pow(.95,d*this.dollySpeed),u=this._zoom,l=this._zoom*m;this.zoomTo(l,!0),this.dollyToCursor&&(this._changedZoom+=l-u,this._dollyControlCoord.set(i,a))},typeof A>"u"&&console.error("camera-controls: `THREE` is undefined. You must first run `CameraControls.install( { THREE: THREE } )`. Check the docs for further information."),this._camera=e,this._yAxisUpSpace=new A.Quaternion().setFromUnitVectors(this._camera.up,Se),this._yAxisUpSpaceInverse=this._yAxisUpSpace.clone().invert(),this._state=h.NONE,this._target=new A.Vector3,this._targetEnd=this._target.clone(),this._focalOffset=new A.Vector3,this._focalOffsetEnd=this._focalOffset.clone(),this._spherical=new A.Spherical().setFromVector3(D.copy(this._camera.position).applyQuaternion(this._yAxisUpSpace)),this._sphericalEnd=this._spherical.clone(),this._lastDistance=this._spherical.radius,this._zoom=this._camera.zoom,this._zoomEnd=this._zoom,this._lastZoom=this._zoom,this._nearPlaneCorners=[new A.Vector3,new A.Vector3,new A.Vector3,new A.Vector3],this._updateNearPlaneCorners(),this._boundary=new A.Box3(new A.Vector3(-1/0,-1/0,-1/0),new A.Vector3(1/0,1/0,1/0)),this._cameraUp0=this._camera.up.clone(),this._target0=this._target.clone(),this._position0=this._camera.position.clone(),this._zoom0=this._zoom,this._focalOffset0=this._focalOffset.clone(),this._dollyControlCoord=new A.Vector2,this.mouseButtons={left:h.ROTATE,middle:h.DOLLY,right:h.TRUCK,wheel:ie(this._camera)?h.DOLLY:ee(this._camera)?h.ZOOM:h.NONE},this.touches={one:h.TOUCH_ROTATE,two:ie(this._camera)?h.TOUCH_DOLLY_TRUCK:ee(this._camera)?h.TOUCH_ZOOM_TRUCK:h.NONE,three:h.TOUCH_TRUCK};let n=new A.Vector2,s=new A.Vector2,o=new A.Vector2,f=d=>{if(!this._enabled||!this._domElement)return;if(this._interactiveArea.left!==0||this._interactiveArea.top!==0||this._interactiveArea.width!==1||this._interactiveArea.height!==1){let m=this._domElement.getBoundingClientRect(),u=d.clientX/m.width,l=d.clientY/m.height;if(u<this._interactiveArea.left||u>this._interactiveArea.right||l<this._interactiveArea.top||l>this._interactiveArea.bottom)return}let i=d.pointerType!=="mouse"?null:(d.buttons&F.LEFT)===F.LEFT?F.LEFT:(d.buttons&F.MIDDLE)===F.MIDDLE?F.MIDDLE:(d.buttons&F.RIGHT)===F.RIGHT?F.RIGHT:null;if(i!==null){let m=this._findPointerByMouseButton(i);m&&this._disposePointer(m)}if((d.buttons&F.LEFT)===F.LEFT&&this._lockedPointer)return;let a={pointerId:d.pointerId,clientX:d.clientX,clientY:d.clientY,deltaX:0,deltaY:0,mouseButton:i};this._activePointers.push(a),this._domElement.ownerDocument.removeEventListener("pointermove",_,{passive:!1}),this._domElement.ownerDocument.removeEventListener("pointerup",c),this._domElement.ownerDocument.addEventListener("pointermove",_,{passive:!1}),this._domElement.ownerDocument.addEventListener("pointerup",c),this._isDragging=!0,w(d)},_=d=>{d.cancelable&&d.preventDefault();let i=d.pointerId,a=this._lockedPointer||this._findPointerById(i);if(a){if(a.clientX=d.clientX,a.clientY=d.clientY,a.deltaX=d.movementX,a.deltaY=d.movementY,this._state=0,d.pointerType==="touch")switch(this._activePointers.length){case 1:this._state=this.touches.one;break;case 2:this._state=this.touches.two;break;case 3:this._state=this.touches.three;break}else(!this._isDragging&&this._lockedPointer||this._isDragging&&(d.buttons&F.LEFT)===F.LEFT)&&(this._state=this._state|this.mouseButtons.left),this._isDragging&&(d.buttons&F.MIDDLE)===F.MIDDLE&&(this._state=this._state|this.mouseButtons.middle),this._isDragging&&(d.buttons&F.RIGHT)===F.RIGHT&&(this._state=this._state|this.mouseButtons.right);C()}},c=d=>{let i=this._findPointerById(d.pointerId);if(!(i&&i===this._lockedPointer)){if(i&&this._disposePointer(i),d.pointerType==="touch")switch(this._activePointers.length){case 0:this._state=h.NONE;break;case 1:this._state=this.touches.one;break;case 2:this._state=this.touches.two;break;case 3:this._state=this.touches.three;break}else this._state=h.NONE;P()}},v=-1,g=d=>{if(!this._domElement||!this._enabled||this.mouseButtons.wheel===h.NONE)return;if(this._interactiveArea.left!==0||this._interactiveArea.top!==0||this._interactiveArea.width!==1||this._interactiveArea.height!==1){let l=this._domElement.getBoundingClientRect(),y=d.clientX/l.width,T=d.clientY/l.height;if(y<this._interactiveArea.left||y>this._interactiveArea.right||T<this._interactiveArea.top||T>this._interactiveArea.bottom)return}if(d.preventDefault(),this.dollyToCursor||this.mouseButtons.wheel===h.ROTATE||this.mouseButtons.wheel===h.TRUCK){let l=performance.now();v-l<1e3&&this._getClientRect(this._elementRect),v=l}let i=ln?-1:-3,a=d.deltaMode===1?d.deltaY/i:d.deltaY/(i*10),m=this.dollyToCursor?(d.clientX-this._elementRect.x)/this._elementRect.width*2-1:0,u=this.dollyToCursor?(d.clientY-this._elementRect.y)/this._elementRect.height*-2+1:0;switch(this.mouseButtons.wheel){case h.ROTATE:{this._rotateInternal(d.deltaX,d.deltaY),this._isUserControllingRotate=!0;break}case h.TRUCK:{this._truckInternal(d.deltaX,d.deltaY,!1),this._isUserControllingTruck=!0;break}case h.OFFSET:{this._truckInternal(d.deltaX,d.deltaY,!0),this._isUserControllingOffset=!0;break}case h.DOLLY:{this._dollyInternal(-a,m,u),this._isUserControllingDolly=!0;break}case h.ZOOM:{this._zoomInternal(-a,m,u),this._isUserControllingZoom=!0;break}}this.dispatchEvent({type:"control"})},b=d=>{if(!(!this._domElement||!this._enabled)){if(this.mouseButtons.right===Xe.ACTION.NONE){let i=d instanceof PointerEvent?d.pointerId:0,a=this._findPointerById(i);a&&this._disposePointer(a),this._domElement.ownerDocument.removeEventListener("pointermove",_,{passive:!1}),this._domElement.ownerDocument.removeEventListener("pointerup",c);return}d.preventDefault()}},w=d=>{if(this._enabled){if(Ue(this._activePointers,j),this._getClientRect(this._elementRect),n.copy(j),s.copy(j),this._activePointers.length>=2){let i=j.x-this._activePointers[1].clientX,a=j.y-this._activePointers[1].clientY,m=Math.sqrt(i*i+a*a);o.set(0,m);let u=(this._activePointers[0].clientX+this._activePointers[1].clientX)*.5,l=(this._activePointers[0].clientY+this._activePointers[1].clientY)*.5;s.set(u,l)}if(this._state=0,!d)this._lockedPointer&&(this._state=this._state|this.mouseButtons.left);else if("pointerType"in d&&d.pointerType==="touch")switch(this._activePointers.length){case 1:this._state=this.touches.one;break;case 2:this._state=this.touches.two;break;case 3:this._state=this.touches.three;break}else!this._lockedPointer&&(d.buttons&F.LEFT)===F.LEFT&&(this._state=this._state|this.mouseButtons.left),(d.buttons&F.MIDDLE)===F.MIDDLE&&(this._state=this._state|this.mouseButtons.middle),(d.buttons&F.RIGHT)===F.RIGHT&&(this._state=this._state|this.mouseButtons.right);((this._state&h.ROTATE)===h.ROTATE||(this._state&h.TOUCH_ROTATE)===h.TOUCH_ROTATE||(this._state&h.TOUCH_DOLLY_ROTATE)===h.TOUCH_DOLLY_ROTATE||(this._state&h.TOUCH_ZOOM_ROTATE)===h.TOUCH_ZOOM_ROTATE)&&(this._sphericalEnd.theta=this._spherical.theta,this._sphericalEnd.phi=this._spherical.phi,this._thetaVelocity.value=0,this._phiVelocity.value=0),((this._state&h.TRUCK)===h.TRUCK||(this._state&h.TOUCH_TRUCK)===h.TOUCH_TRUCK||(this._state&h.TOUCH_DOLLY_TRUCK)===h.TOUCH_DOLLY_TRUCK||(this._state&h.TOUCH_ZOOM_TRUCK)===h.TOUCH_ZOOM_TRUCK)&&(this._targetEnd.copy(this._target),this._targetVelocity.set(0,0,0)),((this._state&h.DOLLY)===h.DOLLY||(this._state&h.TOUCH_DOLLY)===h.TOUCH_DOLLY||(this._state&h.TOUCH_DOLLY_TRUCK)===h.TOUCH_DOLLY_TRUCK||(this._state&h.TOUCH_DOLLY_OFFSET)===h.TOUCH_DOLLY_OFFSET||(this._state&h.TOUCH_DOLLY_ROTATE)===h.TOUCH_DOLLY_ROTATE)&&(this._sphericalEnd.radius=this._spherical.radius,this._radiusVelocity.value=0),((this._state&h.ZOOM)===h.ZOOM||(this._state&h.TOUCH_ZOOM)===h.TOUCH_ZOOM||(this._state&h.TOUCH_ZOOM_TRUCK)===h.TOUCH_ZOOM_TRUCK||(this._state&h.TOUCH_ZOOM_OFFSET)===h.TOUCH_ZOOM_OFFSET||(this._state&h.TOUCH_ZOOM_ROTATE)===h.TOUCH_ZOOM_ROTATE)&&(this._zoomEnd=this._zoom,this._zoomVelocity.value=0),((this._state&h.OFFSET)===h.OFFSET||(this._state&h.TOUCH_OFFSET)===h.TOUCH_OFFSET||(this._state&h.TOUCH_DOLLY_OFFSET)===h.TOUCH_DOLLY_OFFSET||(this._state&h.TOUCH_ZOOM_OFFSET)===h.TOUCH_ZOOM_OFFSET)&&(this._focalOffsetEnd.copy(this._focalOffset),this._focalOffsetVelocity.set(0,0,0)),this.dispatchEvent({type:"controlstart"})}},C=()=>{if(!this._enabled||!this._dragNeedsUpdate)return;this._dragNeedsUpdate=!1,Ue(this._activePointers,j);let d=this._domElement&&this._domElement.ownerDocument.pointerLockElement===this._domElement?this._lockedPointer||this._activePointers[0]:null,i=d?-d.deltaX:s.x-j.x,a=d?-d.deltaY:s.y-j.y;if(s.copy(j),((this._state&h.ROTATE)===h.ROTATE||(this._state&h.TOUCH_ROTATE)===h.TOUCH_ROTATE||(this._state&h.TOUCH_DOLLY_ROTATE)===h.TOUCH_DOLLY_ROTATE||(this._state&h.TOUCH_ZOOM_ROTATE)===h.TOUCH_ZOOM_ROTATE)&&(this._rotateInternal(i,a),this._isUserControllingRotate=!0),(this._state&h.DOLLY)===h.DOLLY||(this._state&h.ZOOM)===h.ZOOM){let m=this.dollyToCursor?(n.x-this._elementRect.x)/this._elementRect.width*2-1:0,u=this.dollyToCursor?(n.y-this._elementRect.y)/this._elementRect.height*-2+1:0,l=this.dollyDragInverted?-1:1;(this._state&h.DOLLY)===h.DOLLY?(this._dollyInternal(l*a*De,m,u),this._isUserControllingDolly=!0):(this._zoomInternal(l*a*De,m,u),this._isUserControllingZoom=!0)}if((this._state&h.TOUCH_DOLLY)===h.TOUCH_DOLLY||(this._state&h.TOUCH_ZOOM)===h.TOUCH_ZOOM||(this._state&h.TOUCH_DOLLY_TRUCK)===h.TOUCH_DOLLY_TRUCK||(this._state&h.TOUCH_ZOOM_TRUCK)===h.TOUCH_ZOOM_TRUCK||(this._state&h.TOUCH_DOLLY_OFFSET)===h.TOUCH_DOLLY_OFFSET||(this._state&h.TOUCH_ZOOM_OFFSET)===h.TOUCH_ZOOM_OFFSET||(this._state&h.TOUCH_DOLLY_ROTATE)===h.TOUCH_DOLLY_ROTATE||(this._state&h.TOUCH_ZOOM_ROTATE)===h.TOUCH_ZOOM_ROTATE){let m=j.x-this._activePointers[1].clientX,u=j.y-this._activePointers[1].clientY,l=Math.sqrt(m*m+u*u),y=o.y-l;o.set(0,l);let T=this.dollyToCursor?(s.x-this._elementRect.x)/this._elementRect.width*2-1:0,S=this.dollyToCursor?(s.y-this._elementRect.y)/this._elementRect.height*-2+1:0;(this._state&h.TOUCH_DOLLY)===h.TOUCH_DOLLY||(this._state&h.TOUCH_DOLLY_ROTATE)===h.TOUCH_DOLLY_ROTATE||(this._state&h.TOUCH_DOLLY_TRUCK)===h.TOUCH_DOLLY_TRUCK||(this._state&h.TOUCH_DOLLY_OFFSET)===h.TOUCH_DOLLY_OFFSET?(this._dollyInternal(y*De,T,S),this._isUserControllingDolly=!0):(this._zoomInternal(y*De,T,S),this._isUserControllingZoom=!0)}((this._state&h.TRUCK)===h.TRUCK||(this._state&h.TOUCH_TRUCK)===h.TOUCH_TRUCK||(this._state&h.TOUCH_DOLLY_TRUCK)===h.TOUCH_DOLLY_TRUCK||(this._state&h.TOUCH_ZOOM_TRUCK)===h.TOUCH_ZOOM_TRUCK)&&(this._truckInternal(i,a,!1),this._isUserControllingTruck=!0),((this._state&h.OFFSET)===h.OFFSET||(this._state&h.TOUCH_OFFSET)===h.TOUCH_OFFSET||(this._state&h.TOUCH_DOLLY_OFFSET)===h.TOUCH_DOLLY_OFFSET||(this._state&h.TOUCH_ZOOM_OFFSET)===h.TOUCH_ZOOM_OFFSET)&&(this._truckInternal(i,a,!0),this._isUserControllingOffset=!0),this.dispatchEvent({type:"control"})},P=()=>{Ue(this._activePointers,j),s.copy(j),this._dragNeedsUpdate=!1,(this._activePointers.length===0||this._activePointers.length===1&&this._activePointers[0]===this._lockedPointer)&&(this._isDragging=!1),this._activePointers.length===0&&this._domElement&&(this._domElement.ownerDocument.removeEventListener("pointermove",_,{passive:!1}),this._domElement.ownerDocument.removeEventListener("pointerup",c),this.dispatchEvent({type:"controlend"}))};this.lockPointer=()=>{!this._enabled||!this._domElement||(this.cancel(),this._lockedPointer={pointerId:-1,clientX:0,clientY:0,deltaX:0,deltaY:0,mouseButton:null},this._activePointers.push(this._lockedPointer),this._domElement.ownerDocument.removeEventListener("pointermove",_,{passive:!1}),this._domElement.ownerDocument.removeEventListener("pointerup",c),this._domElement.requestPointerLock(),this._domElement.ownerDocument.addEventListener("pointerlockchange",E),this._domElement.ownerDocument.addEventListener("pointerlockerror",x),this._domElement.ownerDocument.addEventListener("pointermove",_,{passive:!1}),this._domElement.ownerDocument.addEventListener("pointerup",c),w())},this.unlockPointer=()=>{var d,i,a;this._lockedPointer!==null&&(this._disposePointer(this._lockedPointer),this._lockedPointer=null),(d=this._domElement)===null||d===void 0||d.ownerDocument.exitPointerLock(),(i=this._domElement)===null||i===void 0||i.ownerDocument.removeEventListener("pointerlockchange",E),(a=this._domElement)===null||a===void 0||a.ownerDocument.removeEventListener("pointerlockerror",x),this.cancel()};let E=()=>{this._domElement&&this._domElement.ownerDocument.pointerLockElement===this._domElement||this.unlockPointer()},x=()=>{this.unlockPointer()};this._addAllEventListeners=d=>{this._domElement=d,this._domElement.style.touchAction="none",this._domElement.style.userSelect="none",this._domElement.style.webkitUserSelect="none",this._domElement.addEventListener("pointerdown",f),this._domElement.addEventListener("pointercancel",c),this._domElement.addEventListener("wheel",g,{passive:!1}),this._domElement.addEventListener("contextmenu",b)},this._removeAllEventListeners=()=>{this._domElement&&(this._domElement.style.touchAction="",this._domElement.style.userSelect="",this._domElement.style.webkitUserSelect="",this._domElement.removeEventListener("pointerdown",f),this._domElement.removeEventListener("pointercancel",c),this._domElement.removeEventListener("wheel",g,{passive:!1}),this._domElement.removeEventListener("contextmenu",b),this._domElement.ownerDocument.removeEventListener("pointermove",_,{passive:!1}),this._domElement.ownerDocument.removeEventListener("pointerup",c),this._domElement.ownerDocument.removeEventListener("pointerlockchange",E),this._domElement.ownerDocument.removeEventListener("pointerlockerror",x))},this.cancel=()=>{this._state!==h.NONE&&(this._state=h.NONE,this._activePointers.length=0,P())},t&&this.connect(t),this.update(0)}get camera(){return this._camera}set camera(e){this._camera=e,this.updateCameraUp(),this._camera.updateProjectionMatrix(),this._updateNearPlaneCorners(),this._needsUpdate=!0}get enabled(){return this._enabled}set enabled(e){this._enabled=e,this._domElement&&(e?(this._domElement.style.touchAction="none",this._domElement.style.userSelect="none",this._domElement.style.webkitUserSelect="none"):(this.cancel(),this._domElement.style.touchAction="",this._domElement.style.userSelect="",this._domElement.style.webkitUserSelect=""))}get active(){return!this._hasRested}get currentAction(){return this._state}get distance(){return this._spherical.radius}set distance(e){this._spherical.radius===e&&this._sphericalEnd.radius===e||(this._spherical.radius=e,this._sphericalEnd.radius=e,this._needsUpdate=!0)}get azimuthAngle(){return this._spherical.theta}set azimuthAngle(e){this._spherical.theta===e&&this._sphericalEnd.theta===e||(this._spherical.theta=e,this._sphericalEnd.theta=e,this._needsUpdate=!0)}get polarAngle(){return this._spherical.phi}set polarAngle(e){this._spherical.phi===e&&this._sphericalEnd.phi===e||(this._spherical.phi=e,this._sphericalEnd.phi=e,this._needsUpdate=!0)}get boundaryEnclosesCamera(){return this._boundaryEnclosesCamera}set boundaryEnclosesCamera(e){this._boundaryEnclosesCamera=e,this._needsUpdate=!0}set interactiveArea(e){this._interactiveArea.width=q(e.width,0,1),this._interactiveArea.height=q(e.height,0,1),this._interactiveArea.x=q(e.x,0,1-this._interactiveArea.width),this._interactiveArea.y=q(e.y,0,1-this._interactiveArea.height)}addEventListener(e,t){super.addEventListener(e,t)}removeEventListener(e,t){super.removeEventListener(e,t)}rotate(e,t,n=!1){return this.rotateTo(this._sphericalEnd.theta+e,this._sphericalEnd.phi+t,n)}rotateAzimuthTo(e,t=!1){return this.rotateTo(e,this._sphericalEnd.phi,t)}rotatePolarTo(e,t=!1){return this.rotateTo(this._sphericalEnd.theta,e,t)}rotateTo(e,t,n=!1){this._isUserControllingRotate=!1;let s=q(e,this.minAzimuthAngle,this.maxAzimuthAngle),o=q(t,this.minPolarAngle,this.maxPolarAngle);this._sphericalEnd.theta=s,this._sphericalEnd.phi=o,this._sphericalEnd.makeSafe(),this._needsUpdate=!0,n||(this._spherical.theta=this._sphericalEnd.theta,this._spherical.phi=this._sphericalEnd.phi);let f=!n||O(this._spherical.theta,this._sphericalEnd.theta,this.restThreshold)&&O(this._spherical.phi,this._sphericalEnd.phi,this.restThreshold);return this._createOnRestPromise(f)}dolly(e,t=!1){return this.dollyTo(this._sphericalEnd.radius-e,t)}dollyTo(e,t=!1){return this._isUserControllingDolly=!1,this._lastDollyDirection=oe.NONE,this._changedDolly=0,this._dollyToNoClamp(q(e,this.minDistance,this.maxDistance),t)}_dollyToNoClamp(e,t=!1){let n=this._sphericalEnd.radius;if(this.colliderMeshes.length>=1){let o=this._collisionTest(),f=O(o,this._spherical.radius);if(!(n>e)&&f)return Promise.resolve();this._sphericalEnd.radius=Math.min(e,o)}else this._sphericalEnd.radius=e;this._needsUpdate=!0,t||(this._spherical.radius=this._sphericalEnd.radius);let s=!t||O(this._spherical.radius,this._sphericalEnd.radius,this.restThreshold);return this._createOnRestPromise(s)}dollyInFixed(e,t=!1){this._targetEnd.add(this._getCameraDirection(xe).multiplyScalar(e)),t||this._target.copy(this._targetEnd);let n=!t||O(this._target.x,this._targetEnd.x,this.restThreshold)&&O(this._target.y,this._targetEnd.y,this.restThreshold)&&O(this._target.z,this._targetEnd.z,this.restThreshold);return this._createOnRestPromise(n)}zoom(e,t=!1){return this.zoomTo(this._zoomEnd+e,t)}zoomTo(e,t=!1){this._isUserControllingZoom=!1,this._zoomEnd=q(e,this.minZoom,this.maxZoom),this._needsUpdate=!0,t||(this._zoom=this._zoomEnd);let n=!t||O(this._zoom,this._zoomEnd,this.restThreshold);return this._changedZoom=0,this._createOnRestPromise(n)}pan(e,t,n=!1){return console.warn("`pan` has been renamed to `truck`"),this.truck(e,t,n)}truck(e,t,n=!1){this._camera.updateMatrix(),Z.setFromMatrixColumn(this._camera.matrix,0),X.setFromMatrixColumn(this._camera.matrix,1),Z.multiplyScalar(e),X.multiplyScalar(-t);let s=D.copy(Z).add(X),o=B.copy(this._targetEnd).add(s);return this.moveTo(o.x,o.y,o.z,n)}forward(e,t=!1){D.setFromMatrixColumn(this._camera.matrix,0),D.crossVectors(this._camera.up,D),D.multiplyScalar(e);let n=B.copy(this._targetEnd).add(D);return this.moveTo(n.x,n.y,n.z,t)}elevate(e,t=!1){return D.copy(this._camera.up).multiplyScalar(e),this.moveTo(this._targetEnd.x+D.x,this._targetEnd.y+D.y,this._targetEnd.z+D.z,t)}moveTo(e,t,n,s=!1){this._isUserControllingTruck=!1;let o=D.set(e,t,n).sub(this._targetEnd);this._encloseToBoundary(this._targetEnd,o,this.boundaryFriction),this._needsUpdate=!0,s||this._target.copy(this._targetEnd);let f=!s||O(this._target.x,this._targetEnd.x,this.restThreshold)&&O(this._target.y,this._targetEnd.y,this.restThreshold)&&O(this._target.z,this._targetEnd.z,this.restThreshold);return this._createOnRestPromise(f)}lookInDirectionOf(e,t,n,s=!1){let o=D.set(e,t,n).sub(this._targetEnd).normalize().multiplyScalar(-this._sphericalEnd.radius).add(this._targetEnd);return this.setPosition(o.x,o.y,o.z,s)}fitToBox(e,t,{cover:n=!1,paddingLeft:s=0,paddingRight:o=0,paddingBottom:f=0,paddingTop:_=0}={}){let c=[],v=e.isBox3?ce.copy(e):ce.setFromObject(e);v.isEmpty()&&(console.warn("camera-controls: fitTo() cannot be used with an empty box. Aborting"),Promise.resolve());let g=bt(this._sphericalEnd.theta,Ct),b=bt(this._sphericalEnd.phi,Ct);c.push(this.rotateTo(g,b,t));let w=D.setFromSpherical(this._sphericalEnd).normalize(),C=At.setFromUnitVectors(w,He),P=O(Math.abs(w.y),1);P&&C.multiply(Ve.setFromAxisAngle(Se,g)),C.multiply(this._yAxisUpSpaceInverse);let E=zt.makeEmpty();B.copy(v.min).applyQuaternion(C),E.expandByPoint(B),B.copy(v.min).setX(v.max.x).applyQuaternion(C),E.expandByPoint(B),B.copy(v.min).setY(v.max.y).applyQuaternion(C),E.expandByPoint(B),B.copy(v.max).setZ(v.min.z).applyQuaternion(C),E.expandByPoint(B),B.copy(v.min).setZ(v.max.z).applyQuaternion(C),E.expandByPoint(B),B.copy(v.max).setY(v.min.y).applyQuaternion(C),E.expandByPoint(B),B.copy(v.max).setX(v.min.x).applyQuaternion(C),E.expandByPoint(B),B.copy(v.max).applyQuaternion(C),E.expandByPoint(B),E.min.x-=s,E.min.y-=f,E.max.x+=o,E.max.y+=_,C.setFromUnitVectors(He,w),P&&C.premultiply(Ve.invert()),C.premultiply(this._yAxisUpSpace);let x=E.getSize(D),d=E.getCenter(B).applyQuaternion(C);if(ie(this._camera)){let i=this.getDistanceToFitBox(x.x,x.y,x.z,n);c.push(this.moveTo(d.x,d.y,d.z,t)),c.push(this.dollyTo(i,t)),c.push(this.setFocalOffset(0,0,0,t))}else if(ee(this._camera)){let i=this._camera,a=i.right-i.left,m=i.top-i.bottom,u=n?Math.max(a/x.x,m/x.y):Math.min(a/x.x,m/x.y);c.push(this.moveTo(d.x,d.y,d.z,t)),c.push(this.zoomTo(u,t)),c.push(this.setFocalOffset(0,0,0,t))}return Promise.all(c)}fitToSphere(e,t){let n=[],s="isObject3D"in e?Xe.createBoundingSphere(e,je):je.copy(e);if(n.push(this.moveTo(s.center.x,s.center.y,s.center.z,t)),ie(this._camera)){let o=this.getDistanceToFitSphere(s.radius);n.push(this.dollyTo(o,t))}else if(ee(this._camera)){let o=this._camera.right-this._camera.left,f=this._camera.top-this._camera.bottom,_=2*s.radius,c=Math.min(o/_,f/_);n.push(this.zoomTo(c,t))}return n.push(this.setFocalOffset(0,0,0,t)),Promise.all(n)}setLookAt(e,t,n,s,o,f,_=!1){this._isUserControllingRotate=!1,this._isUserControllingDolly=!1,this._isUserControllingTruck=!1,this._lastDollyDirection=oe.NONE,this._changedDolly=0;let c=B.set(s,o,f),v=D.set(e,t,n);this._targetEnd.copy(c),this._sphericalEnd.setFromVector3(v.sub(c).applyQuaternion(this._yAxisUpSpace)),this.normalizeRotations(),this._needsUpdate=!0,_||(this._target.copy(this._targetEnd),this._spherical.copy(this._sphericalEnd));let g=!_||O(this._target.x,this._targetEnd.x,this.restThreshold)&&O(this._target.y,this._targetEnd.y,this.restThreshold)&&O(this._target.z,this._targetEnd.z,this.restThreshold)&&O(this._spherical.theta,this._sphericalEnd.theta,this.restThreshold)&&O(this._spherical.phi,this._sphericalEnd.phi,this.restThreshold)&&O(this._spherical.radius,this._sphericalEnd.radius,this.restThreshold);return this._createOnRestPromise(g)}lerpLookAt(e,t,n,s,o,f,_,c,v,g,b,w,C,P=!1){this._isUserControllingRotate=!1,this._isUserControllingDolly=!1,this._isUserControllingTruck=!1,this._lastDollyDirection=oe.NONE,this._changedDolly=0;let E=D.set(s,o,f),x=B.set(e,t,n);Y.setFromVector3(x.sub(E).applyQuaternion(this._yAxisUpSpace));let d=le.set(g,b,w),i=B.set(_,c,v);ye.setFromVector3(i.sub(d).applyQuaternion(this._yAxisUpSpace)),this._targetEnd.copy(E.lerp(d,C));let a=ye.theta-Y.theta,m=ye.phi-Y.phi,u=ye.radius-Y.radius;this._sphericalEnd.set(Y.radius+u*C,Y.phi+m*C,Y.theta+a*C),this.normalizeRotations(),this._needsUpdate=!0,P||(this._target.copy(this._targetEnd),this._spherical.copy(this._sphericalEnd));let l=!P||O(this._target.x,this._targetEnd.x,this.restThreshold)&&O(this._target.y,this._targetEnd.y,this.restThreshold)&&O(this._target.z,this._targetEnd.z,this.restThreshold)&&O(this._spherical.theta,this._sphericalEnd.theta,this.restThreshold)&&O(this._spherical.phi,this._sphericalEnd.phi,this.restThreshold)&&O(this._spherical.radius,this._sphericalEnd.radius,this.restThreshold);return this._createOnRestPromise(l)}setPosition(e,t,n,s=!1){return this.setLookAt(e,t,n,this._targetEnd.x,this._targetEnd.y,this._targetEnd.z,s)}setTarget(e,t,n,s=!1){let o=this.getPosition(D),f=this.setLookAt(o.x,o.y,o.z,e,t,n,s);return this._sphericalEnd.phi=q(this._sphericalEnd.phi,this.minPolarAngle,this.maxPolarAngle),f}setFocalOffset(e,t,n,s=!1){this._isUserControllingOffset=!1,this._focalOffsetEnd.set(e,t,n),this._needsUpdate=!0,s||this._focalOffset.copy(this._focalOffsetEnd);let o=!s||O(this._focalOffset.x,this._focalOffsetEnd.x,this.restThreshold)&&O(this._focalOffset.y,this._focalOffsetEnd.y,this.restThreshold)&&O(this._focalOffset.z,this._focalOffsetEnd.z,this.restThreshold);return this._createOnRestPromise(o)}setOrbitPoint(e,t,n){this._camera.updateMatrixWorld(),Z.setFromMatrixColumn(this._camera.matrixWorldInverse,0),X.setFromMatrixColumn(this._camera.matrixWorldInverse,1),re.setFromMatrixColumn(this._camera.matrixWorldInverse,2);let s=D.set(e,t,n),o=s.distanceTo(this._camera.position),f=s.sub(this._camera.position);Z.multiplyScalar(f.x),X.multiplyScalar(f.y),re.multiplyScalar(f.z),D.copy(Z).add(X).add(re),D.z=D.z+o,this.dollyTo(o,!1),this.setFocalOffset(-D.x,D.y,-D.z,!1),this.moveTo(e,t,n,!1)}setBoundary(e){if(!e){this._boundary.min.set(-1/0,-1/0,-1/0),this._boundary.max.set(1/0,1/0,1/0),this._needsUpdate=!0;return}this._boundary.copy(e),this._boundary.clampPoint(this._targetEnd,this._targetEnd),this._needsUpdate=!0}setViewport(e,t,n,s){if(e===null){this._viewport=null;return}this._viewport=this._viewport||new A.Vector4,typeof e=="number"?this._viewport.set(e,t,n,s):this._viewport.copy(e)}getDistanceToFitBox(e,t,n,s=!1){if(Me(this._camera,"getDistanceToFitBox"))return this._spherical.radius;let o=e/t,f=this._camera.getEffectiveFOV()*pe,_=this._camera.aspect;return((s?o>_:o<_)?t:e/_)*.5/Math.tan(f*.5)+n*.5}getDistanceToFitSphere(e){if(Me(this._camera,"getDistanceToFitSphere"))return this._spherical.radius;let t=this._camera.getEffectiveFOV()*pe,n=Math.atan(Math.tan(t*.5)*this._camera.aspect)*2,s=1<this._camera.aspect?t:n;return e/Math.sin(s*.5)}getTarget(e,t=!0){return(e&&e.isVector3?e:new A.Vector3).copy(t?this._targetEnd:this._target)}getPosition(e,t=!0){return(e&&e.isVector3?e:new A.Vector3).setFromSpherical(t?this._sphericalEnd:this._spherical).applyQuaternion(this._yAxisUpSpaceInverse).add(t?this._targetEnd:this._target)}getSpherical(e,t=!0){return(e||new A.Spherical).copy(t?this._sphericalEnd:this._spherical)}getFocalOffset(e,t=!0){return(e&&e.isVector3?e:new A.Vector3).copy(t?this._focalOffsetEnd:this._focalOffset)}normalizeRotations(){this._sphericalEnd.theta=this._sphericalEnd.theta%se,this._sphericalEnd.theta<0&&(this._sphericalEnd.theta+=se),this._spherical.theta+=se*Math.round((this._sphericalEnd.theta-this._spherical.theta)/se)}stop(){this._focalOffset.copy(this._focalOffsetEnd),this._target.copy(this._targetEnd),this._spherical.copy(this._sphericalEnd),this._zoom=this._zoomEnd}reset(e=!1){if(!O(this._camera.up.x,this._cameraUp0.x)||!O(this._camera.up.y,this._cameraUp0.y)||!O(this._camera.up.z,this._cameraUp0.z)){this._camera.up.copy(this._cameraUp0);let n=this.getPosition(D);this.updateCameraUp(),this.setPosition(n.x,n.y,n.z)}let t=[this.setLookAt(this._position0.x,this._position0.y,this._position0.z,this._target0.x,this._target0.y,this._target0.z,e),this.setFocalOffset(this._focalOffset0.x,this._focalOffset0.y,this._focalOffset0.z,e),this.zoomTo(this._zoom0,e)];return Promise.all(t)}saveState(){this._cameraUp0.copy(this._camera.up),this.getTarget(this._target0),this.getPosition(this._position0),this._zoom0=this._zoom,this._focalOffset0.copy(this._focalOffset)}updateCameraUp(){this._yAxisUpSpace.setFromUnitVectors(this._camera.up,Se),this._yAxisUpSpaceInverse.copy(this._yAxisUpSpace).invert()}applyCameraUp(){let e=D.subVectors(this._target,this._camera.position).normalize(),t=B.crossVectors(e,this._camera.up);this._camera.up.crossVectors(t,e).normalize(),this._camera.updateMatrixWorld();let n=this.getPosition(D);this.updateCameraUp(),this.setPosition(n.x,n.y,n.z)}update(e){let t=this._sphericalEnd.theta-this._spherical.theta,n=this._sphericalEnd.phi-this._spherical.phi,s=this._sphericalEnd.radius-this._spherical.radius,o=wt.subVectors(this._targetEnd,this._target),f=Pt.subVectors(this._focalOffsetEnd,this._focalOffset),_=this._zoomEnd-this._zoom;if(R(t))this._thetaVelocity.value=0,this._spherical.theta=this._sphericalEnd.theta;else{let g=this._isUserControllingRotate?this.draggingSmoothTime:this.smoothTime;this._spherical.theta=Ae(this._spherical.theta,this._sphericalEnd.theta,this._thetaVelocity,g,1/0,e),this._needsUpdate=!0}if(R(n))this._phiVelocity.value=0,this._spherical.phi=this._sphericalEnd.phi;else{let g=this._isUserControllingRotate?this.draggingSmoothTime:this.smoothTime;this._spherical.phi=Ae(this._spherical.phi,this._sphericalEnd.phi,this._phiVelocity,g,1/0,e),this._needsUpdate=!0}if(R(s))this._radiusVelocity.value=0,this._spherical.radius=this._sphericalEnd.radius;else{let g=this._isUserControllingDolly?this.draggingSmoothTime:this.smoothTime;this._spherical.radius=Ae(this._spherical.radius,this._sphericalEnd.radius,this._radiusVelocity,g,this.maxSpeed,e),this._needsUpdate=!0}if(R(o.x)&&R(o.y)&&R(o.z))this._targetVelocity.set(0,0,0),this._target.copy(this._targetEnd);else{let g=this._isUserControllingTruck?this.draggingSmoothTime:this.smoothTime;Et(this._target,this._targetEnd,this._targetVelocity,g,this.maxSpeed,e,this._target),this._needsUpdate=!0}if(R(f.x)&&R(f.y)&&R(f.z))this._focalOffsetVelocity.set(0,0,0),this._focalOffset.copy(this._focalOffsetEnd);else{let g=this._isUserControllingOffset?this.draggingSmoothTime:this.smoothTime;Et(this._focalOffset,this._focalOffsetEnd,this._focalOffsetVelocity,g,this.maxSpeed,e,this._focalOffset),this._needsUpdate=!0}if(R(_))this._zoomVelocity.value=0,this._zoom=this._zoomEnd;else{let g=this._isUserControllingZoom?this.draggingSmoothTime:this.smoothTime;this._zoom=Ae(this._zoom,this._zoomEnd,this._zoomVelocity,g,1/0,e)}if(this.dollyToCursor){if(ie(this._camera)&&this._changedDolly!==0){let g=this._spherical.radius-this._lastDistance,b=this._camera,w=this._getCameraDirection(xe),C=D.copy(w).cross(b.up).normalize();C.lengthSq()===0&&(C.x=1);let P=B.crossVectors(C,w),E=this._sphericalEnd.radius*Math.tan(b.getEffectiveFOV()*pe*.5),x=(this._sphericalEnd.radius-g-this._sphericalEnd.radius)/this._sphericalEnd.radius,d=le.copy(this._targetEnd).add(C.multiplyScalar(this._dollyControlCoord.x*E*b.aspect)).add(P.multiplyScalar(this._dollyControlCoord.y*E)),i=D.copy(this._targetEnd).lerp(d,x),a=this._lastDollyDirection===oe.IN&&this._spherical.radius<=this.minDistance,m=this._lastDollyDirection===oe.OUT&&this.maxDistance<=this._spherical.radius;if(this.infinityDolly&&(a||m)){this._sphericalEnd.radius-=g,this._spherical.radius-=g;let l=B.copy(w).multiplyScalar(-g);i.add(l)}this._boundary.clampPoint(i,i);let u=B.subVectors(i,this._targetEnd);this._targetEnd.copy(i),this._target.add(u),this._changedDolly-=g,R(this._changedDolly)&&(this._changedDolly=0)}else if(ee(this._camera)&&this._changedZoom!==0){let g=this._zoom-this._lastZoom,b=this._camera,w=D.set(this._dollyControlCoord.x,this._dollyControlCoord.y,(b.near+b.far)/(b.near-b.far)).unproject(b),C=B.set(0,0,-1).applyQuaternion(b.quaternion),P=le.copy(w).add(C.multiplyScalar(-w.dot(b.up))),E=-(this._zoom-g-this._zoom)/this._zoom,x=this._getCameraDirection(xe),d=this._targetEnd.dot(x),i=D.copy(this._targetEnd).lerp(P,E),a=i.dot(x),m=x.multiplyScalar(a-d);i.sub(m),this._boundary.clampPoint(i,i);let u=B.subVectors(i,this._targetEnd);this._targetEnd.copy(i),this._target.add(u),this._changedZoom-=g,R(this._changedZoom)&&(this._changedZoom=0)}}this._camera.zoom!==this._zoom&&(this._camera.zoom=this._zoom,this._camera.updateProjectionMatrix(),this._updateNearPlaneCorners(),this._needsUpdate=!0),this._dragNeedsUpdate=!0;let c=this._collisionTest();this._spherical.radius=Math.min(this._spherical.radius,c),this._spherical.makeSafe(),this._camera.position.setFromSpherical(this._spherical).applyQuaternion(this._yAxisUpSpaceInverse).add(this._target),this._camera.lookAt(this._target),(!R(this._focalOffset.x)||!R(this._focalOffset.y)||!R(this._focalOffset.z))&&(this._camera.updateMatrixWorld(),Z.setFromMatrixColumn(this._camera.matrix,0),X.setFromMatrixColumn(this._camera.matrix,1),re.setFromMatrixColumn(this._camera.matrix,2),Z.multiplyScalar(this._focalOffset.x),X.multiplyScalar(-this._focalOffset.y),re.multiplyScalar(this._focalOffset.z),D.copy(Z).add(X).add(re),this._camera.position.add(D)),this._boundaryEnclosesCamera&&this._encloseToBoundary(this._camera.position.copy(this._target),D.setFromSpherical(this._spherical).applyQuaternion(this._yAxisUpSpaceInverse),1);let v=this._needsUpdate;return v&&!this._updatedLastTime?(this._hasRested=!1,this.dispatchEvent({type:"wake"}),this.dispatchEvent({type:"update"})):v?(this.dispatchEvent({type:"update"}),R(t,this.restThreshold)&&R(n,this.restThreshold)&&R(s,this.restThreshold)&&R(o.x,this.restThreshold)&&R(o.y,this.restThreshold)&&R(o.z,this.restThreshold)&&R(f.x,this.restThreshold)&&R(f.y,this.restThreshold)&&R(f.z,this.restThreshold)&&R(_,this.restThreshold)&&!this._hasRested&&(this._hasRested=!0,this.dispatchEvent({type:"rest"}))):!v&&this._updatedLastTime&&this.dispatchEvent({type:"sleep"}),this._lastDistance=this._spherical.radius,this._lastZoom=this._zoom,this._updatedLastTime=v,this._needsUpdate=!1,v}toJSON(){return JSON.stringify({enabled:this._enabled,minDistance:this.minDistance,maxDistance:he(this.maxDistance),minZoom:this.minZoom,maxZoom:he(this.maxZoom),minPolarAngle:this.minPolarAngle,maxPolarAngle:he(this.maxPolarAngle),minAzimuthAngle:he(this.minAzimuthAngle),maxAzimuthAngle:he(this.maxAzimuthAngle),smoothTime:this.smoothTime,draggingSmoothTime:this.draggingSmoothTime,dollySpeed:this.dollySpeed,truckSpeed:this.truckSpeed,dollyToCursor:this.dollyToCursor,verticalDragToForward:this.verticalDragToForward,target:this._targetEnd.toArray(),position:D.setFromSpherical(this._sphericalEnd).add(this._targetEnd).toArray(),zoom:this._zoomEnd,focalOffset:this._focalOffsetEnd.toArray(),target0:this._target0.toArray(),position0:this._position0.toArray(),zoom0:this._zoom0,focalOffset0:this._focalOffset0.toArray()})}fromJSON(e,t=!1){let n=JSON.parse(e);this.enabled=n.enabled,this.minDistance=n.minDistance,this.maxDistance=_e(n.maxDistance),this.minZoom=n.minZoom,this.maxZoom=_e(n.maxZoom),this.minPolarAngle=n.minPolarAngle,this.maxPolarAngle=_e(n.maxPolarAngle),this.minAzimuthAngle=_e(n.minAzimuthAngle),this.maxAzimuthAngle=_e(n.maxAzimuthAngle),this.smoothTime=n.smoothTime,this.draggingSmoothTime=n.draggingSmoothTime,this.dollySpeed=n.dollySpeed,this.truckSpeed=n.truckSpeed,this.dollyToCursor=n.dollyToCursor,this.verticalDragToForward=n.verticalDragToForward,this._target0.fromArray(n.target0),this._position0.fromArray(n.position0),this._zoom0=n.zoom0,this._focalOffset0.fromArray(n.focalOffset0),this.moveTo(n.target[0],n.target[1],n.target[2],t),Y.setFromVector3(D.fromArray(n.position).sub(this._targetEnd).applyQuaternion(this._yAxisUpSpace)),this.rotateTo(Y.theta,Y.phi,t),this.dollyTo(Y.radius,t),this.zoomTo(n.zoom,t),this.setFocalOffset(n.focalOffset[0],n.focalOffset[1],n.focalOffset[2],t),this._needsUpdate=!0}connect(e){if(this._domElement){console.warn("camera-controls is already connected.");return}e.setAttribute("data-camera-controls-version",sn),this._addAllEventListeners(e),this._getClientRect(this._elementRect)}disconnect(){this.cancel(),this._removeAllEventListeners(),this._domElement&&(this._domElement.removeAttribute("data-camera-controls-version"),this._domElement=void 0)}dispose(){this.removeAllEventListeners(),this.disconnect()}_getTargetDirection(e){return e.setFromSpherical(this._spherical).divideScalar(this._spherical.radius).applyQuaternion(this._yAxisUpSpaceInverse)}_getCameraDirection(e){return this._getTargetDirection(e).negate()}_findPointerById(e){return this._activePointers.find(t=>t.pointerId===e)}_findPointerByMouseButton(e){return this._activePointers.find(t=>t.mouseButton===e)}_disposePointer(e){this._activePointers.splice(this._activePointers.indexOf(e),1)}_encloseToBoundary(e,t,n){let s=t.lengthSq();if(s===0)return e;let o=B.copy(t).add(e),f=this._boundary.clampPoint(o,le).sub(o),_=f.lengthSq();if(_===0)return e.add(t);if(_===s)return e;if(n===0)return e.add(t).add(f);{let c=1+n*_/t.dot(f);return e.add(B.copy(t).multiplyScalar(c)).add(f.multiplyScalar(1-n))}}_updateNearPlaneCorners(){if(ie(this._camera)){let e=this._camera,t=e.near,n=e.getEffectiveFOV()*pe,s=Math.tan(n*.5)*t,o=s*e.aspect;this._nearPlaneCorners[0].set(-o,-s,0),this._nearPlaneCorners[1].set(o,-s,0),this._nearPlaneCorners[2].set(o,s,0),this._nearPlaneCorners[3].set(-o,s,0)}else if(ee(this._camera)){let e=this._camera,t=1/e.zoom,n=e.left*t,s=e.right*t,o=e.top*t,f=e.bottom*t;this._nearPlaneCorners[0].set(n,o,0),this._nearPlaneCorners[1].set(s,o,0),this._nearPlaneCorners[2].set(s,f,0),this._nearPlaneCorners[3].set(n,f,0)}}_collisionTest(){let e=1/0;if(!(this.colliderMeshes.length>=1)||Me(this._camera,"_collisionTest"))return e;let t=this._getTargetDirection(xe);Ye.lookAt(Tt,t,this._camera.up);for(let n=0;n<4;n++){let s=B.copy(this._nearPlaneCorners[n]);s.applyMatrix4(Ye);let o=le.addVectors(this._target,s);Be.set(o,t),Be.far=this._spherical.radius+1;let f=Be.intersectObjects(this.colliderMeshes);f.length!==0&&f[0].distance<e&&(e=f[0].distance)}return e}_getClientRect(e){if(!this._domElement)return;let t=this._domElement.getBoundingClientRect();return e.x=t.left,e.y=t.top,this._viewport?(e.x+=this._viewport.x,e.y+=t.height-this._viewport.w-this._viewport.y,e.width=this._viewport.z,e.height=this._viewport.w):(e.width=t.width,e.height=t.height),e}_createOnRestPromise(e){return e?Promise.resolve():(this._hasRested=!1,this.dispatchEvent({type:"transitionstart"}),new Promise(t=>{let n=()=>{this.removeEventListener("rest",n),t()};this.addEventListener("rest",n)}))}_addAllEventListeners(e){}_removeAllEventListeners(){}get dampingFactor(){return console.warn(".dampingFactor has been deprecated. use smoothTime (in seconds) instead."),0}set dampingFactor(e){console.warn(".dampingFactor has been deprecated. use smoothTime (in seconds) instead.")}get draggingDampingFactor(){return console.warn(".draggingDampingFactor has been deprecated. use draggingSmoothTime (in seconds) instead."),0}set draggingDampingFactor(e){console.warn(".draggingDampingFactor has been deprecated. use draggingSmoothTime (in seconds) instead.")}static createBoundingSphere(e,t=new A.Sphere){let n=t,s=n.center;ce.makeEmpty(),e.traverseVisible(f=>{f.isMesh&&ce.expandByObject(f)}),ce.getCenter(s);let o=0;return e.traverseVisible(f=>{if(!f.isMesh)return;let _=f,c=_.geometry.clone();c.applyMatrix4(_.matrixWorld);let v=c.attributes.position;for(let g=0,b=v.count;g<b;g++)D.fromBufferAttribute(v,g),o=Math.max(o,s.distanceToSquared(D))}),n.radius=Math.sqrt(o),n}};function cn(r){var e=r,{smoothTime:t=.05}=e,n=ne(e,["smoothTime"]);W.install({THREE:Ci}),bi({CameraControls:W});let s=M(v=>v.camera),o=M(v=>v.gl),f=an(n),[_,c]=z.useState(!1);return z.useEffect(()=>{let v=f.current;if(!v)return;let{type:g,onCameraUpdate:b}=n||{};if(!b)return;let w=d=>Math.round(d*180/Math.PI),C=()=>({cAzimuthAngle:w(v.azimuthAngle),cPolarAngle:w(v.polarAngle)}),P=()=>{var d;let i={};if(g==="sphere"){let a=v?.zoom;if(Number.isFinite(a))i.cameraZoom=Number(a.toFixed(2));else{let m=(d=v?.camera)==null?void 0:d.zoom;Number.isFinite(m)&&(i.cameraZoom=Number(m.toFixed(2)))}}else Number.isFinite(v.distance)&&(i.cDistance=Number(v.distance.toFixed(2)));return i},E=()=>{c(!0)},x=()=>{c(!1),b(L(L({},C()),P()))};return v.addEventListener("controlstart",E),v.addEventListener("rest",x),()=>{v.removeEventListener("controlstart",E),v.removeEventListener("rest",x)}},[f,n]),p.jsx("cameraControls",{ref:f,args:[s,o.domElement],smoothTime:_?0:t,zoomSpeed:10,dollySpeed:5,maxDistance:1e3,restThreshold:.01,mouseButtons:{left:W.ACTION.ROTATE,middle:n.type==="sphere"?W.ACTION.ZOOM:W.ACTION.DOLLY,right:W.ACTION.NONE,wheel:n.type==="sphere"?W.ACTION.ZOOM:W.ACTION.DOLLY},touches:{one:W.ACTION.ROTATE,two:W.ACTION.NONE,three:W.ACTION.NONE}})}function un(r){return p.jsx(p.Fragment,{children:p.jsx(cn,L({},r))})}var fn=class extends Ei{constructor(r){super(r),this.type=Ce}parse(r){let e=function(x,d){switch(x){case 1:throw new Error("THREE.RGBELoader: Read Error: "+(d||""));case 2:throw new Error("THREE.RGBELoader: Write Error: "+(d||""));case 3:throw new Error("THREE.RGBELoader: Bad File Format: "+(d||""));default:case 4:throw new Error("THREE.RGBELoader: Memory Error: "+(d||""))}},t=`
`,n=function(x,d,i){d=d||1024;let a=x.pos,m=-1,u=0,l="",y=String.fromCharCode.apply(null,new Uint16Array(x.subarray(a,a+128)));for(;0>(m=y.indexOf(t))&&u<d&&a<x.byteLength;)l+=y,u+=y.length,a+=128,y+=String.fromCharCode.apply(null,new Uint16Array(x.subarray(a,a+128)));return-1<m?(x.pos+=u+m+1,l+y.slice(0,m)):!1},s=function(x){let d=/^#\?(\S+)/,i=/^\s*GAMMA\s*=\s*(\d+(\.\d+)?)\s*$/,a=/^\s*EXPOSURE\s*=\s*(\d+(\.\d+)?)\s*$/,m=/^\s*FORMAT=(\S+)\s*$/,u=/^\s*\-Y\s+(\d+)\s+\+X\s+(\d+)\s*$/,l={valid:0,string:"",comments:"",programtype:"RGBE",format:"",gamma:1,exposure:1,width:0,height:0},y,T;for((x.pos>=x.byteLength||!(y=n(x)))&&e(1,"no header found"),(T=y.match(d))||e(3,"bad initial token"),l.valid|=1,l.programtype=T[1],l.string+=y+`
`;y=n(x),y!==!1;){if(l.string+=y+`
`,y.charAt(0)==="#"){l.comments+=y+`
`;continue}if((T=y.match(i))&&(l.gamma=parseFloat(T[1])),(T=y.match(a))&&(l.exposure=parseFloat(T[1])),(T=y.match(m))&&(l.valid|=2,l.format=T[1]),(T=y.match(u))&&(l.valid|=4,l.height=parseInt(T[1],10),l.width=parseInt(T[2],10)),l.valid&2&&l.valid&4)break}return l.valid&2||e(3,"missing format specifier"),l.valid&4||e(3,"missing image size specifier"),l},o=function(x,d,i){let a=d;if(a<8||a>32767||x[0]!==2||x[1]!==2||x[2]&128)return new Uint8Array(x);a!==(x[2]<<8|x[3])&&e(3,"wrong scanline width");let m=new Uint8Array(4*d*i);m.length||e(4,"unable to allocate buffer space");let u=0,l=0,y=4*a,T=new Uint8Array(4),S=new Uint8Array(y),V=i;for(;V>0&&l<x.byteLength;){l+4>x.byteLength&&e(1),T[0]=x[l++],T[1]=x[l++],T[2]=x[l++],T[3]=x[l++],(T[0]!=2||T[1]!=2||(T[2]<<8|T[3])!=a)&&e(3,"bad rgbe scanline format");let G=0,N;for(;G<y&&l<x.byteLength;){N=x[l++];let H=N>128;if(H&&(N-=128),(N===0||G+N>y)&&e(3,"bad scanline data"),H){let k=x[l++];for(let rt=0;rt<N;rt++)S[G++]=k}else S.set(x.subarray(l,l+N),G),G+=N,l+=N}let te=a;for(let H=0;H<te;H++){let k=0;m[u]=S[H+k],k+=a,m[u+1]=S[H+k],k+=a,m[u+2]=S[H+k],k+=a,m[u+3]=S[H+k],u+=4}V--}return m},f=function(x,d,i,a){let m=x[d+3],u=Math.pow(2,m-128)/255;i[a+0]=x[d+0]*u,i[a+1]=x[d+1]*u,i[a+2]=x[d+2]*u,i[a+3]=1},_=function(x,d,i,a){let m=x[d+3],u=Math.pow(2,m-128)/255;i[a+0]=we.toHalfFloat(Math.min(x[d+0]*u,65504)),i[a+1]=we.toHalfFloat(Math.min(x[d+1]*u,65504)),i[a+2]=we.toHalfFloat(Math.min(x[d+2]*u,65504)),i[a+3]=we.toHalfFloat(1)},c=new Uint8Array(r);c.pos=0;let v=s(c),g=v.width,b=v.height,w=o(c.subarray(c.pos),g,b),C,P,E;switch(this.type){case Re:E=w.length/4;let x=new Float32Array(E*4);for(let i=0;i<E;i++)f(w,i*4,x,i*4);C=x,P=Re;break;case Ce:E=w.length/4;let d=new Uint16Array(E*4);for(let i=0;i<E;i++)_(w,i*4,d,i*4);C=d,P=Ce;break;default:throw new Error("THREE.RGBELoader: Unsupported type: "+this.type)}return{width:g,height:b,data:C,header:v.string,gamma:v.gamma,exposure:v.exposure,type:P}}setDataType(r){return this.type=r,this}load(r,e,t,n){function s(o,f){switch(o.type){case Re:case Ce:"colorSpace"in o?o.colorSpace="srgb-linear":o.encoding=3e3,o.minFilter=me,o.magFilter=me,o.generateMipmaps=!1,o.flipY=!0;break}e&&e(o,f)}return super.load(r,s,t,n)}};new It;new Q;new Ee;new Q;new Q;new Ee;new Ee;new Ee;new Q;new Ut;new Ti;new Q;new It;new wi;new Ee;function $e(r,{path:e}){return Pi(fn,r,t=>t.setPath(e))}function dn(r=!0,e=.1,t="0px"){let[n,s]=z.useState(!r),o=z.useRef(null);return z.useEffect(()=>{if(!r)return;let f=new IntersectionObserver(([_])=>{s(_.isIntersecting)},{threshold:e,rootMargin:t});return o.current&&f.observe(o.current),()=>f.disconnect()},[r,e,t]),{isInView:n,containerRef:o}}var oi=z.createContext({}),mn=()=>z.useContext(oi);function vn({children:r,style:e={},pixelDensity:t=1,fov:n=45,pointerEvents:s,className:o,envBasePath:f,lazyLoad:_=!0,threshold:c=.1,rootMargin:v="0px",preserveDrawingBuffer:g,powerPreference:b}){let{isInView:w,containerRef:C}=dn(_,c,v),P=z.useMemo(()=>({envBasePath:f}),[f]);return gn(),p.jsx("div",{ref:C,style:L({width:"100%",height:"100%"},e),children:(!_||w)&&p.jsx(oi.Provider,{value:P,children:p.jsx(zi,ge(L({id:"gradientCanvas",style:{pointerEvents:s},resize:{offsetSize:!0},className:o},en(t,n,{preserveDrawingBuffer:g,powerPreference:b})),{children:r}),t+n)})})}function gn(){z.useEffect(()=>{Pe.uv2_pars_vertex="",Pe.uv2_vertex="",Pe.uv2_pars_fragment="",Pe.encodings_fragment=""},[])}var pn=r=>r.current&&r.current.isScene,hn=r=>pn(r)?r.current:r;function _n({background:r=!1,envPreset:e}){let{envBasePath:t}=mn(),n=t||nn,s=$e("city.hdr",{path:n}),o=$e("dawn.hdr",{path:n}),f=$e("lobby.hdr",{path:n}),_={city:s,dawn:o,lobby:f}[e],c=M(g=>g.scene);ci.useLayoutEffect(()=>{if(_){let g=hn(c);g.background;let b=g.environment;return r!=="only"&&(g.environment=_),r&&(g.background=_),()=>{r!=="only"&&(g.environment=b),r&&(g.background="black")}}},[c,_,r]);let v=_;return v.mapping=Ai,null}function xn({lightType:r,brightness:e,envPreset:t}){return p.jsxs(p.Fragment,{children:[r==="3d"&&p.jsx("ambientLight",{intensity:(e||1)*Math.PI}),r==="env"&&p.jsx(z.Suspense,{fallback:p.jsx(yn,{}),children:p.jsx(_n,{envPreset:t,background:!1,loadingCallback:()=>{}})})]})}function yn(){return p.jsx("ambientLight",{intensity:.4})}function Cn(r,e){let t=M(s=>s.pointer),[n]=z.useState(()=>{let s=new Di;return function(o,f){s.setFromCamera(t,r instanceof Si?r:r.current);let _=this.constructor.prototype.raycast.bind(this);_&&_(s,f)}});return n}function bn(r,e,t){let{gl:n,size:s,viewport:o}=M(),f=typeof r=="number"?r:s.width*o.dpr,_=s.height*o.dpr,c=(typeof r=="number"?t:r)||{},{samples:v}=c,g=ne(c,["samples"]),b=z.useMemo(()=>{let w;return w=new Ft(f,_,L({minFilter:me,magFilter:me,encoding:n.outputEncoding,type:Ce},g)),w.samples=v,w},[]);return z.useLayoutEffect(()=>{b.setSize(f,_),v&&(b.samples=v)},[v,b,f,_]),z.useEffect(()=>()=>b.dispose(),[]),b}function En(r,e){if(typeof r=="function")return r(e);r&&(r.current=e)}function Tn(r){return e=>{for(let t of r)En(t,e)}}var wn=r=>typeof r=="function",Pn=z.forwardRef((r,e)=>{var t=r,{envMap:n,resolution:s=256,frames:o=1/0,children:f,makeDefault:_}=t,c=ne(t,["envMap","resolution","frames","children","makeDefault"]);let v=M(({set:i})=>i),g=M(({camera:i})=>i),b=M(({size:i})=>i),w=z.useRef(null),C=z.useRef(null),P=bn(s);z.useLayoutEffect(()=>{c.manual||w.current.updateProjectionMatrix()},[b,c]),z.useLayoutEffect(()=>{w.current.updateProjectionMatrix()}),z.useLayoutEffect(()=>{if(_){let i=g;return v(()=>({camera:w.current})),()=>v(()=>({camera:i}))}},[w,_,v]);let E=0,x=null,d=wn(f);return be(i=>{d&&(o===1/0||E<o)&&(C.current.visible=!1,i.gl.setRenderTarget(P),x=i.scene.background,n&&(i.scene.background=n),i.gl.render(i.scene,w.current),i.scene.background=x,i.gl.setRenderTarget(null),C.current.visible=!0,E++)}),p.jsxs(p.Fragment,{children:[p.jsx("orthographicCamera",ge(L({left:b.width/-2,right:b.width/2,top:b.height/2,bottom:b.height/-2,ref:Tn([w,e])},c),{children:!d&&f})),p.jsx("group",{ref:C,children:d&&f(P.texture)})]})}),si=z.createContext({}),zn=()=>z.useContext(si),An=2*Math.PI,Dt=new Oi,St=new Ut,[ue,Ge]=[new at,new at],Bt=new Q,Ot=new Q,Dn=r=>"minPolarAngle"in r,Sn=({alignment:r="bottom-right",margin:e=[80,80],renderPriority:t=0,autoClear:n=!0,onUpdate:s,onTarget:o,children:f})=>{let _=M(({size:N})=>N),c=M(({camera:N})=>N),v=M(({controls:N})=>N),g=M(({gl:N})=>N),b=M(({scene:N})=>N),w=M(({invalidate:N})=>N),C=z.useRef(),P=z.useRef(),E=z.useRef(null),[x]=z.useState(()=>new Bi),d=z.useRef(!1),i=z.useRef(0),a=z.useRef(new Q(0,0,0)),m=z.useRef(new Q(0,0,0));z.useEffect(()=>{m.current.copy(c.up)},[c]);let u=z.useCallback(N=>{d.current=!0,(v||o)&&(a.current=v?.target||o?.()),i.current=c.position.distanceTo(Bt),ue.copy(c.quaternion),Ot.copy(N).multiplyScalar(i.current).add(Bt),Dt.lookAt(Ot),Ge.copy(Dt.quaternion),w()},[v,c,o,w]);z.useEffect(()=>(b.background&&(C.current=b.background,b.background=null,x.background=C.current),()=>{C.current&&(b.background=C.current)}),[]),be((N,te)=>{var H;if(E.current&&P.current){if(d.current)if(ue.angleTo(Ge)<.01)d.current=!1,Dn(v)&&c.up.copy(m.current);else{let k=te*An;ue.rotateTowards(Ge,k),c.position.set(0,0,1).applyQuaternion(ue).multiplyScalar(i.current).add(a.current),c.up.set(0,1,0).applyQuaternion(ue).normalize(),c.quaternion.copy(ue),s?s():v&&v.update(),w()}St.copy(c.matrix).invert(),(H=P.current)==null||H.quaternion.setFromRotationMatrix(St),n&&(g.autoClear=!1),g.clearDepth(),g.render(x,E.current)}},t);let l=Cn(E),y=z.useMemo(()=>({tweenCamera:u,raycast:l}),[u]),[T,S]=e,V=r.endsWith("-center")?0:r.endsWith("-left")?-_.width/2+T:_.width/2-T,G=r.startsWith("center-")?0:r.startsWith("top-")?_.height/2-S:-_.height/2+S;return Ni(p.jsxs(si.Provider,{value:y,children:[p.jsx(Pn,{ref:E,position:[0,0,200]}),p.jsx("group",{ref:P,position:[V,G,0],children:f})]}),x)};function We({scale:r=[.8,.05,.05],color:e,rotation:t}){return p.jsx("group",{rotation:t,children:p.jsxs("mesh",{position:[.4,0,0],children:[p.jsx("boxGeometry",{args:r}),p.jsx("meshBasicMaterial",{color:e,toneMapped:!1})]})})}function fe(r){var e=r,{onClick:t,font:n,disabled:s,arcStyle:o,label:f,labelColor:_,axisHeadScale:c=1}=e,v=ne(e,["onClick","font","disabled","arcStyle","label","labelColor","axisHeadScale"]);let g=M(E=>E.gl),b=z.useMemo(()=>{let E=document.createElement("canvas");E.width=64,E.height=64;let x=E.getContext("2d");return x.beginPath(),x.arc(32,32,16,0,2*Math.PI),x.closePath(),x.fillStyle=o,x.fill(),f&&(x.font=n,x.textAlign="center",x.fillStyle=_,x.fillText(f,32,41)),new Li(E)},[o,f,_,n]),[w,C]=z.useState(!1),P=(f?1:.75)*(w?1.2:1)*c;return p.jsx("sprite",ge(L({scale:P,onPointerOver:s?void 0:E=>{E.stopPropagation(),C(!0)},onPointerOut:s?void 0:t||(E=>{E.stopPropagation(),C(!1)})},v),{children:p.jsx("spriteMaterial",{map:b,"map-encoding":g.outputEncoding,"map-anisotropy":g.capabilities.getMaxAnisotropy()||1,alphaTest:.3,opacity:f?1:.75,toneMapped:!1})}))}var Bn=r=>{var e=r,{hideNegativeAxes:t,hideAxisHeads:n,disabled:s,font:o="18px Inter var, Arial, sans-serif",axisColors:f=["#ff2060","#20df80","#2080ff"],axisHeadScale:_=1,axisScale:c,labels:v=["X","Y","Z"],labelColor:g="#000",onClick:b}=e,w=ne(e,["hideNegativeAxes","hideAxisHeads","disabled","font","axisColors","axisHeadScale","axisScale","labels","labelColor","onClick"]);let[C,P,E]=f,{tweenCamera:x,raycast:d}=zn(),i={font:o,disabled:s,labelColor:g,raycast:d,onClick:b,axisHeadScale:_,onPointerDown:s?void 0:a=>{x(a.object.position),a.stopPropagation()}};return p.jsxs("group",ge(L({scale:40},w),{children:[p.jsx(We,{color:C,rotation:[0,0,0],scale:c}),p.jsx(We,{color:P,rotation:[0,0,Math.PI/2],scale:c}),p.jsx(We,{color:E,rotation:[0,-Math.PI/2,0],scale:c}),!n&&p.jsxs(p.Fragment,{children:[p.jsx(fe,L({arcStyle:C,position:[1,0,0],label:v[0]},i)),p.jsx(fe,L({arcStyle:P,position:[0,1,0],label:v[1]},i)),p.jsx(fe,L({arcStyle:E,position:[0,0,1],label:v[2]},i)),!t&&p.jsxs(p.Fragment,{children:[p.jsx(fe,L({arcStyle:C,position:[-1,0,0]},i)),p.jsx(fe,L({arcStyle:P,position:[0,-1,0]},i)),p.jsx(fe,L({arcStyle:E,position:[0,0,-1]},i))]})]}),p.jsx("ambientLight",{intensity:.5}),p.jsx("pointLight",{position:[10,10,10],intensity:.5})]}))};function On({margin:r=[65,110]}){return p.jsx(p.Fragment,{children:p.jsx(Sn,{alignment:"bottom-right",margin:r,renderPriority:2,children:p.jsx(Bn,{axisColors:["#FF430A","#FF430A","#FF430A"],labelColor:"white",hideNegativeAxes:!0,axisHeadScale:.8})})})}var Nn={halo:{props:{type:"plane",uAmplitude:1,uDensity:1.3,uSpeed:.4,uStrength:4,uTime:0,uFrequency:5.5,range:"disabled",rangeStart:0,rangeEnd:40,frameRate:10,destination:"onCanvas",format:"gif",axesHelper:"off",brightness:1.2,cAzimuthAngle:180,cDistance:3.6,cPolarAngle:90,cameraZoom:1,color1:"#ff5005",color2:"#dbba95",color3:"#d0bce1",embedMode:"off",envPreset:"city",gizmoHelper:"hide",grain:"on",lightType:"3d",pixelDensity:1,fov:45,positionX:-1.4,positionY:0,positionZ:0,reflection:.1,rotationX:0,rotationY:10,rotationZ:50,shader:"defaults",animate:"on",wireframe:!1}}},Ln=Te((r,e)=>{e.exports=t=>encodeURIComponent(t).replace(/[!'()*]/g,n=>`%${n.charCodeAt(0).toString(16).toUpperCase()}`)}),Rn=Te((r,e)=>{var t="%[a-f0-9]{2}",n=new RegExp("("+t+")|([^%]+?)","gi"),s=new RegExp("("+t+")+","gi");function o(c,v){try{return[decodeURIComponent(c.join(""))]}catch{}if(c.length===1)return c;v=v||1;var g=c.slice(0,v),b=c.slice(v);return Array.prototype.concat.call([],o(g),o(b))}function f(c){try{return decodeURIComponent(c)}catch{for(var v=c.match(n)||[],g=1;g<v.length;g++)c=o(v,g).join(""),v=c.match(n)||[];return c}}function _(c){for(var v={"%FE%FF":"��","%FF%FE":"��"},g=s.exec(c);g;){try{v[g[0]]=decodeURIComponent(g[0])}catch{var b=f(g[0]);b!==g[0]&&(v[g[0]]=b)}g=s.exec(c)}v["%C2"]="�";for(var w=Object.keys(v),C=0;C<w.length;C++){var P=w[C];c=c.replace(new RegExp(P,"g"),v[P])}return c}e.exports=function(c){if(typeof c!="string")throw new TypeError("Expected `encodedURI` to be of type `string`, got `"+typeof c+"`");try{return c=c.replace(/\+/g," "),decodeURIComponent(c)}catch{return _(c)}}}),Fn=Te((r,e)=>{e.exports=(t,n)=>{if(!(typeof t=="string"&&typeof n=="string"))throw new TypeError("Expected the arguments to be of type `string`");if(n==="")return[t];let s=t.indexOf(n);return s===-1?[t]:[t.slice(0,s),t.slice(s+n.length)]}}),In=Te((r,e)=>{e.exports=function(t,n){for(var s={},o=Object.keys(t),f=Array.isArray(n),_=0;_<o.length;_++){var c=o[_],v=t[c];(f?n.indexOf(c)!==-1:n(c,v,t))&&(s[c]=v)}return s}}),Un=Te(r=>{var e=Ln(),t=Rn(),n=Fn(),s=In(),o=i=>i==null,f=Symbol("encodeFragmentIdentifier");function _(i){switch(i.arrayFormat){case"index":return a=>(m,u)=>{let l=m.length;return u===void 0||i.skipNull&&u===null||i.skipEmptyString&&u===""?m:u===null?[...m,[g(a,i),"[",l,"]"].join("")]:[...m,[g(a,i),"[",g(l,i),"]=",g(u,i)].join("")]};case"bracket":return a=>(m,u)=>u===void 0||i.skipNull&&u===null||i.skipEmptyString&&u===""?m:u===null?[...m,[g(a,i),"[]"].join("")]:[...m,[g(a,i),"[]=",g(u,i)].join("")];case"colon-list-separator":return a=>(m,u)=>u===void 0||i.skipNull&&u===null||i.skipEmptyString&&u===""?m:u===null?[...m,[g(a,i),":list="].join("")]:[...m,[g(a,i),":list=",g(u,i)].join("")];case"comma":case"separator":case"bracket-separator":{let a=i.arrayFormat==="bracket-separator"?"[]=":"=";return m=>(u,l)=>l===void 0||i.skipNull&&l===null||i.skipEmptyString&&l===""?u:(l=l===null?"":l,u.length===0?[[g(m,i),a,g(l,i)].join("")]:[[u,g(l,i)].join(i.arrayFormatSeparator)])}default:return a=>(m,u)=>u===void 0||i.skipNull&&u===null||i.skipEmptyString&&u===""?m:u===null?[...m,g(a,i)]:[...m,[g(a,i),"=",g(u,i)].join("")]}}function c(i){let a;switch(i.arrayFormat){case"index":return(m,u,l)=>{if(a=/\[(\d*)\]$/.exec(m),m=m.replace(/\[\d*\]$/,""),!a){l[m]=u;return}l[m]===void 0&&(l[m]={}),l[m][a[1]]=u};case"bracket":return(m,u,l)=>{if(a=/(\[\])$/.exec(m),m=m.replace(/\[\]$/,""),!a){l[m]=u;return}if(l[m]===void 0){l[m]=[u];return}l[m]=[].concat(l[m],u)};case"colon-list-separator":return(m,u,l)=>{if(a=/(:list)$/.exec(m),m=m.replace(/:list$/,""),!a){l[m]=u;return}if(l[m]===void 0){l[m]=[u];return}l[m]=[].concat(l[m],u)};case"comma":case"separator":return(m,u,l)=>{let y=typeof u=="string"&&u.includes(i.arrayFormatSeparator),T=typeof u=="string"&&!y&&b(u,i).includes(i.arrayFormatSeparator);u=T?b(u,i):u;let S=y||T?u.split(i.arrayFormatSeparator).map(V=>b(V,i)):u===null?u:b(u,i);l[m]=S};case"bracket-separator":return(m,u,l)=>{let y=/(\[\])$/.test(m);if(m=m.replace(/\[\]$/,""),!y){l[m]=u&&b(u,i);return}let T=u===null?[]:u.split(i.arrayFormatSeparator).map(S=>b(S,i));if(l[m]===void 0){l[m]=T;return}l[m]=[].concat(l[m],T)};default:return(m,u,l)=>{if(l[m]===void 0){l[m]=u;return}l[m]=[].concat(l[m],u)}}}function v(i){if(typeof i!="string"||i.length!==1)throw new TypeError("arrayFormatSeparator must be single character string")}function g(i,a){return a.encode?a.strict?e(i):encodeURIComponent(i):i}function b(i,a){return a.decode?t(i):i}function w(i){return Array.isArray(i)?i.sort():typeof i=="object"?w(Object.keys(i)).sort((a,m)=>Number(a)-Number(m)).map(a=>i[a]):i}function C(i){let a=i.indexOf("#");return a!==-1&&(i=i.slice(0,a)),i}function P(i){let a="",m=i.indexOf("#");return m!==-1&&(a=i.slice(m)),a}function E(i){i=C(i);let a=i.indexOf("?");return a===-1?"":i.slice(a+1)}function x(i,a){return a.parseNumbers&&!Number.isNaN(Number(i))&&typeof i=="string"&&i.trim()!==""?i=Number(i):a.parseBooleans&&i!==null&&(i.toLowerCase()==="true"||i.toLowerCase()==="false")&&(i=i.toLowerCase()==="true"),i}function d(i,a){a=Object.assign({decode:!0,sort:!0,arrayFormat:"none",arrayFormatSeparator:",",parseNumbers:!1,parseBooleans:!1},a),v(a.arrayFormatSeparator);let m=c(a),u=Object.create(null);if(typeof i!="string"||(i=i.trim().replace(/^[?#&]/,""),!i))return u;for(let l of i.split("&")){if(l==="")continue;let[y,T]=n(a.decode?l.replace(/\+/g," "):l,"=");T=T===void 0?null:["comma","separator","bracket-separator"].includes(a.arrayFormat)?T:b(T,a),m(b(y,a),T,u)}for(let l of Object.keys(u)){let y=u[l];if(typeof y=="object"&&y!==null)for(let T of Object.keys(y))y[T]=x(y[T],a);else u[l]=x(y,a)}return a.sort===!1?u:(a.sort===!0?Object.keys(u).sort():Object.keys(u).sort(a.sort)).reduce((l,y)=>{let T=u[y];return T&&typeof T=="object"&&!Array.isArray(T)?l[y]=w(T):l[y]=T,l},Object.create(null))}r.extract=E,r.parse=d,r.stringify=(i,a)=>{if(!i)return"";a=Object.assign({encode:!0,strict:!0,arrayFormat:"none",arrayFormatSeparator:","},a),v(a.arrayFormatSeparator);let m=T=>a.skipNull&&o(i[T])||a.skipEmptyString&&i[T]==="",u=_(a),l={};for(let T of Object.keys(i))m(T)||(l[T]=i[T]);let y=Object.keys(l);return a.sort!==!1&&y.sort(a.sort),y.map(T=>{let S=i[T];return S===void 0?"":S===null?g(T,a):Array.isArray(S)?S.length===0&&a.arrayFormat==="bracket-separator"?g(T,a)+"[]":S.reduce(u(T),[]).join("&"):g(T,a)+"="+g(S,a)}).filter(T=>T.length>0).join("&")},r.parseUrl=(i,a)=>{a=Object.assign({decode:!0},a);let[m,u]=n(i,"#");return Object.assign({url:m.split("?")[0]||"",query:d(E(i),a)},a&&a.parseFragmentIdentifier&&u?{fragmentIdentifier:b(u,a)}:{})},r.stringifyUrl=(i,a)=>{a=Object.assign({encode:!0,strict:!0,[f]:!0},a);let m=C(i.url).split("?")[0]||"",u=r.extract(i.url),l=r.parse(u,{sort:!1}),y=Object.assign(l,i.query),T=r.stringify(y,a);T&&(T=`?${T}`);let S=P(i.url);return i.fragmentIdentifier&&(S=`#${a[f]?g(i.fragmentIdentifier,a):i.fragmentIdentifier}`),`${m}${T}${S}`},r.pick=(i,a,m)=>{m=Object.assign({parseFragmentIdentifier:!0,[f]:!1},m);let{url:u,query:l,fragmentIdentifier:y}=r.parseUrl(i,m);return r.stringifyUrl({url:u,query:s(l,a),fragmentIdentifier:y},m)},r.exclude=(i,a,m)=>{let u=Array.isArray(a)?l=>!a.includes(l):(l,y)=>!a(l,y);return r.pick(i,u,m)}}),Mn=Zi(Un());function kn(r){let e=L(L({},Nn.halo.props),r),{control:t,urlString:n,onCameraUpdate:s}=e,o=ne(e,["control","urlString","onCameraUpdate"]);t==="query"&&(o=Mn.parse(br(n),{parseNumbers:!0,parseBooleans:!0,arrayFormat:"index"}));let f=o,{lightType:_,envPreset:c,brightness:v,grain:g,toggleAxis:b}=f;return ne(f,["lightType","envPreset","brightness","grain","toggleAxis"]),p.jsxs(p.Fragment,{children:[p.jsx(Er,L({},o)),p.jsx(xn,{lightType:_,brightness:v,envPreset:c}),g!=="off"&&p.jsx(Jr,{}),b&&p.jsx(On,{}),p.jsx(un,ge(L({},o),{onCameraUpdate:s}))]})}const $={MINUTE:60*1e3,HOUR:3600*1e3,DAY:1440*60*1e3,WEEK:10080*60*1e3,MONTH:720*60*60*1e3,YEAR:365*24*60*60*1e3};function Nt(r){const e=Date.now()-r;return e<$.MINUTE?"刚刚":e<$.HOUR?`${Math.floor(e/$.MINUTE)} 分钟前`:e<$.DAY?`${Math.floor(e/$.HOUR)} 小时前`:e<$.WEEK?`${Math.floor(e/$.DAY)} 天前`:e<$.MONTH?`${Math.floor(e/$.WEEK)} 星期前`:e<$.YEAR?`${Math.floor(e/$.MONTH)} 个月前`:`${Math.floor(e/$.YEAR)} 年前`}function ca(){const{isDesktop:t}=ui(),{openExternalUrl:n}=fi(),[s]=di(),o=s.has("showAnnouncement"),f=s.has("showUpdate"),[_,c]=z.useState(!1),v=mi(),[g,b]=z.useState({current:Ie,isLatest:!0,latestTimestamp:Date.now()}),[w,C]=z.useState({title:"",timestamp:Date.now(),content:""}),{isOpen:P,onOpen:E,onOpenChange:x}=ot(),{isOpen:d,onOpen:i,onOpenChange:a}=ot();z.useEffect(()=>{if(!o)return;(async()=>{try{let l;{const y=await fetch("/api/announcement");if(!y.ok)throw new Error("公告信息获取失败");l=await y.json()}C({title:l.title,timestamp:l.timestamp,content:l.content}),i()}catch(l){console.error("获取公告信息出错:",l)}})()},[o]),z.useEffect(()=>{if(o&&!_)return;(async()=>{try{let l;{const y=await fetch("/api/version");if(!y.ok)throw new Error("版本信息获取失败");l=await y.json()}if(l.latestVersion){const y=Fi(l.latestVersion,Ie)<=0;b({current:Ie,latest:l.latestVersion,updateLog:l.updateLog,isLatest:y,latestTimestamp:l.timestamp}),!y&&f&&E()}}catch(l){console.error("获取版本信息出错:",l)}})()},[o,_]);const m=()=>{document.body.classList.add("fade-out"),setTimeout(()=>{v("/settings/general"),setTimeout(()=>{document.body.classList.contains("fade-out")&&window.location.reload()},300)},500)};return p.jsxs(p.Fragment,{children:[t&&p.jsx(vi,{autoHide:!1}),p.jsxs("div",{children:[p.jsx("style",{children:`
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

          #shader-gradient {
            width: 100vw;
            z-index: 0;
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
        `}),p.jsx("div",{"data-overlay-container":"true",children:p.jsxs("main",{children:[p.jsx("div",{children:p.jsx("div",{className:"absolute bottom-0 top-0 flex h-screen w-full flex-col",children:p.jsx("div",{className:"relative flex flex-col gap-20 text-white md:gap-10",children:p.jsxs("div",{className:"flex h-screen w-full items-center justify-center relative overflow-hidden",children:[p.jsxs("div",{className:"flex flex-col items-center gap-10",id:"main-content",children:[p.jsx("div",{className:"w-full h-full pointer-events-none",id:"shader-gradient",children:p.jsx(vn,{style:{position:"absolute",top:0},children:p.jsx(kn,{control:"query",urlString:"https://www.shadergradient.co/customize?animate=on&axesHelper=off&bgColor1=%23000000&bgColor2=%23000000&brightness=1.4&cAzimuthAngle=180&cDistance=2.8&cPolarAngle=80&cameraZoom=9.1&color1=%23606080&color2=%238d7dca&color3=%23212121&destination=onCanvas&embedMode=off&envPreset=city&format=gif&fov=45&frameRate=10&gizmoHelper=hide&grain=on&lightType=3d&pixelDensity=1&positionX=0&positionY=0&positionZ=0&range=disabled&rangeEnd=2.8&rangeStart=0&reflection=0&rotationX=50&rotationY=0&rotationZ=-60&shader=defaults&type=waterPlane&uAmplitude=0&uDensity=0.9&uFrequency=0&uSpeed=0.3&uStrength=1.5&uTime=8&wireframe=false"})})}),p.jsx("div",{className:"flex items-center justify-between",children:p.jsxs("h2",{className:"inline-block font-sourcehan text-center text-3xl lg:text-4xl md:text-3xl",id:"title",children:["欢迎使用",p.jsx("span",{className:"px-2 font-dela",id:"now-playing-text",children:"Now Playing"}),"服务"]})}),p.jsxs("button",{className:"animated-button",onClick:m,children:[p.jsx("svg",{className:"arr-2",viewBox:"0 0 24 24",xmlns:"http://www.w3.org/2000/svg",children:p.jsx("path",{d:"M16.1716 10.9999L10.8076 5.63589L12.2218 4.22168L20 11.9999L12.2218 19.778L10.8076 18.3638L16.1716 12.9999H4V10.9999H16.1716Z"})}),p.jsx("span",{className:"button-text",children:"前往设置"}),p.jsx("span",{className:"button-circle"}),p.jsx("svg",{className:"arr-1",viewBox:"0 0 24 24",xmlns:"http://www.w3.org/2000/svg",children:p.jsx("path",{d:"M16.1716 10.9999L10.8076 5.63589L12.2218 4.22168L20 11.9999L12.2218 19.778L10.8076 18.3638L16.1716 12.9999H4V10.9999H16.1716Z"})})]})]}),p.jsxs("div",{style:{position:"fixed",bottom:"2.0rem",width:"100%",textAlign:"center"},children:[p.jsxs("div",{className:"font-poppins",id:"current-version-div",style:{marginBottom:"0.2rem"},children:["版本号：",g.current]}),p.jsx("div",{className:"font-poppins",id:"update-text",children:g.isLatest?"当前已是最新版本":g.latest?p.jsxs("a",{className:"cursor-pointer",onClick:()=>{n("https://gitee.com/widdit/now-playing/releases")},children:["检测到新版本可用：",g.latest]}):null})]})]})})})}),p.jsx(st,{size:"xl",isDismissable:!1,scrollBehavior:"inside",hideCloseButton:!0,isOpen:P,onOpenChange:x,className:"px-3 py-2",children:p.jsx(lt,{className:"font-poppins",children:u=>p.jsxs(p.Fragment,{children:[p.jsxs(ct,{className:"flex justify-between items-center",children:[p.jsxs("div",{className:"flex items-center gap-2",children:[p.jsx("div",{className:"breathing-bg flex h-9 w-9 items-center justify-center rounded-full bg-[#15283c]",children:p.jsx(Ri,{size:20,strokeWidth:2,color:"#0485f7"})}),g.latest," 新版本可用"]}),p.jsx("div",{className:"font-normal text-sm text-default-500",children:Nt(g.latestTimestamp)})]}),p.jsx(ut,{children:p.jsx("div",{className:"markdown-body",children:p.jsx(dt,{rehypePlugins:[mt,vt],components:{img:({node:l,...y})=>p.jsx("img",{...y,referrerPolicy:"no-referrer",className:"max-w-full h-auto rounded-lg my-2"}),a:({node:l,...y})=>p.jsx("a",{...y,className:"text-primary hover:underline",target:"_blank",rel:"noopener noreferrer"})},children:g.updateLog})})}),p.jsxs(ft,{children:[p.jsx(Fe,{color:"default",variant:"flat",onPress:u,children:"取消"}),p.jsx(Fe,{color:"primary",onPress:()=>{u(),n("https://gitee.com/widdit/now-playing/releases")},children:"确定"})]})]})})}),p.jsx(st,{size:"xl",isDismissable:!1,isKeyboardDismissDisabled:!0,scrollBehavior:"inside",hideCloseButton:!0,isOpen:d,onOpenChange:a,className:"px-3 py-2",children:p.jsx(lt,{className:"font-poppins",children:u=>p.jsxs(p.Fragment,{children:[p.jsxs(ct,{className:"flex justify-between items-center",children:[p.jsxs("div",{className:"flex items-center gap-2",children:[p.jsx("div",{className:"breathing-bg flex h-9 w-9 items-center justify-center rounded-full bg-[#15283c]",children:p.jsx(Ui,{size:20,color:"#0485f7"})}),w.title]}),p.jsx("div",{className:"font-normal text-sm text-default-500",children:Nt(w.timestamp)})]}),p.jsx(ut,{children:p.jsx("div",{className:"markdown-body",children:p.jsx(dt,{rehypePlugins:[mt,vt],components:{img:({node:l,...y})=>p.jsx("img",{...y,referrerPolicy:"no-referrer",className:"max-w-full h-auto rounded-lg my-2"}),a:({node:l,...y})=>p.jsx("a",{...y,className:"text-primary hover:underline",target:"_blank",rel:"noopener noreferrer"})},children:w.content})})}),p.jsx(ft,{children:p.jsx(Fe,{color:"primary",onPress:()=>{u(),c(!0)},children:"确定"})})]})})})]})})]})]})}export{ca as default};
