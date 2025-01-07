package com.widdit.nowplaying.entity;

import cn.hutool.json.JSONObject;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@EqualsAndHashCode
public class ReqJsonObject {

    private JSONObject jsonObject;

    public void set(String key, Object value) {
        jsonObject.set(key, value);
    }

    public ReqJsonObject() {
        this.jsonObject = new JSONObject();
    }

    public ReqJsonObject(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    @Override
    public String toString() {
        return jsonObject.toString();
    }

}
