package com.widdit.nowplaying.service.netease;

import com.alibaba.fastjson.JSONObject;

public class UrlParamPair {

    public String url;
    public JSONObject paras;

    public UrlParamPair() {
        this.paras = new JSONObject();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public JSONObject getParas() {
        return paras;
    }

    public void setParas(JSONObject paras) {
        this.paras = paras;
    }

    public UrlParamPair addPara(String key, Object value) {
        this.paras.put(key, value.toString());
        return this;
    }

    public UrlParamPair(String url, JSONObject paras) {
        this.url = url;
        this.paras = paras;
    }

    @Override
    public String toString() {
        return "UrlParamPair{" +
                "url='" + url + '\'' +
                ", paras=" + paras +
                '}';
    }

}
