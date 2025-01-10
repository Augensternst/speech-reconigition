package com.example.backend.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.backend.utils.ASRUtil;
import lombok.Setter;
import org.antlr.v4.runtime.misc.Pair;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CountDownLatch;

public class CustomWebSocketClient extends WebSocketClient {
    private final WebSocketSession browserSession;
    private String currentPartialResult; // 存储当前的中间结果
    private String finalResult; // 存储最终结果
    @Setter
    private CountDownLatch connectionLatch;

    public CustomWebSocketClient(URI serverUri, DraftWithOrigin draft, WebSocketSession browserSession) {
        super(serverUri, draft);
        this.browserSession = browserSession;
        this.currentPartialResult = "";
        this.finalResult = "";
    }

    @Override
    public void onMessage(String message) {
        try {
            JSONObject msgObj = JSON.parseObject(message);
            String action = msgObj.getString("action");

            if ("result".equals(action)) {
                System.out.println(msgObj);
                Pair<Boolean, String> parseResult= ASRUtil.getContent(msgObj.getString("data"));
                String result = parseResult.b;
                Boolean sentenceEnd = parseResult.a;

                if (sentenceEnd) {
                    finalResult += result;
                    currentPartialResult = "";
                    System.out.println("最终识别结果: " + finalResult);
                    sendResultToFrontend(finalResult);
                    return;
                }

                if (result != null && !result.trim().isEmpty()) {
                    currentPartialResult = result.trim();
                    System.out.println("中间识别结果: " + currentPartialResult);
                    sendResultToFrontend(finalResult + currentPartialResult);
                }
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private void sendResultToFrontend(String text) {
        if (browserSession == null || !browserSession.isOpen()) {
            System.out.println("WebSocket session is closed, skipping message send");
            return;
        }

        try {
            JSONObject resultObj = new JSONObject();
            resultObj.put("text", text);
            resultObj.put("isEnd", false);
            browserSession.sendMessage(new TextMessage(resultObj.toJSONString()));
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
    
    @Override
    public void onOpen(ServerHandshake handshake) {
        System.out.println("成功与讯飞握手");
        if (connectionLatch != null) {
            connectionLatch.countDown();
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("与讯飞分手");
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("与讯飞交互出现错误！" + ex.getMessage());
    }
}
