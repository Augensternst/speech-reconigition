package com.example.backend.handler;

import com.example.backend.config.SslConfig;
import com.example.backend.utils.ASRUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public class AudioWebSocketHandler extends BinaryWebSocketHandler {
    private static final String APPID = "e071cad7";
    private static final String SECRET_KEY = "f4317031b9c24f2780084ecc1e27b3fb";
    private static final String HOST = "rtasr.xfyun.cn/v1/ws";
    private static final String BASE_URL = "wss://" + HOST;
    private static final String ORIGIN = "https://" + HOST;

    private final Map<String, CustomWebSocketClient> xunfeiClients = new ConcurrentHashMap<>();
    private final Map<String, AudioStreamProcessor> audioProcessors = new ConcurrentHashMap<>();


    @Override
    public void afterConnectionEstablished(@NotNull WebSocketSession session) throws Exception {

        String url = BASE_URL + ASRUtil.getHandShakeParams(APPID, SECRET_KEY);
        CustomWebSocketClient client = new CustomWebSocketClient(
                new URI(url),
                new DraftWithOrigin(ORIGIN),
                session
        );

        SslConfig.configureSslContext(client);

        CountDownLatch latch = new CountDownLatch(1);
        client.setConnectionLatch(latch);

        client.connect();
        latch.await();

        xunfeiClients.put(session.getId(), client);
        audioProcessors.put(session.getId(), new AudioStreamProcessor(client));
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, @NotNull BinaryMessage message) {
        AudioStreamProcessor processor = audioProcessors.get(session.getId());
        if (processor != null) {
            processor.processAudioChunk(message.getPayload().array());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, @NotNull CloseStatus status) {
        AudioStreamProcessor processor = audioProcessors.remove(session.getId());
        if (processor != null) {
            processor.close();
        }

        CustomWebSocketClient client = xunfeiClients.remove(session.getId());
        if (client != null) {
            client.close();
        }
    }
}
