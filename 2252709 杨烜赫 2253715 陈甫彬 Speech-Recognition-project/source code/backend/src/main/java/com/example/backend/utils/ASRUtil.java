package com.example.backend.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.antlr.v4.runtime.misc.Pair;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ASRUtil {

    // 生成握手参数
    public static String getHandShakeParams(String appId, String secretKey) {
        String ts = System.currentTimeMillis() / 1000 + "";
        try {
            String signa = EncryptUtil.HmacSHA1Encrypt(EncryptUtil.MD5(appId + ts), secretKey);
            return "?appid=" + appId + "&ts=" + ts + "&signa=" + URLEncoder.encode(signa, StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.out.println("生成握手参数失败");
        }
        return "";
    }

    // 把转写结果解析为句子
    public static Pair<Boolean, String> getContent(String message) {
        StringBuilder resultBuilder = new StringBuilder();
        boolean sentenceEnd;
        try {
            JSONObject messageObj = JSON.parseObject(message);
            JSONObject cn = messageObj.getJSONObject("cn");
            JSONObject st = cn.getJSONObject("st");
            sentenceEnd = st.getInteger("type") == 0;
            JSONArray rtArr = st.getJSONArray("rt");
            for (int i = 0; i < rtArr.size(); i++) {
                JSONObject rtArrObj = rtArr.getJSONObject(i);
                JSONArray wsArr = rtArrObj.getJSONArray("ws");
                for (int j = 0; j < wsArr.size(); j++) {
                    JSONObject wsArrObj = wsArr.getJSONObject(j);
                    JSONArray cwArr = wsArrObj.getJSONArray("cw");
                    for (int k = 0; k < cwArr.size(); k++) {
                        JSONObject cwArrObj = cwArr.getJSONObject(k);
                        String wStr = cwArrObj.getString("w");
                        resultBuilder.append(wStr);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("解析转写结果失败");
            return new Pair<>(false, message);
        }

        return new Pair<>(sentenceEnd, resultBuilder.toString());
    }
}
